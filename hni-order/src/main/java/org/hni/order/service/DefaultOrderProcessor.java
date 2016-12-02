package org.hni.order.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.hni.events.service.EventRouter;
import org.hni.events.service.om.Event;
import org.hni.events.service.om.EventName;
import org.hni.order.dao.DefaultPartialOrderDAO;
import org.hni.order.om.Order;
import org.hni.order.om.OrderItem;
import org.hni.order.om.PartialOrder;
import org.hni.order.om.TransactionPhase;
import org.hni.order.om.type.OrderStatus;
import org.hni.provider.om.AddressException;
import org.hni.provider.om.Menu;
import org.hni.provider.om.MenuItem;
import org.hni.provider.om.ProviderLocation;
import org.hni.provider.service.ProviderLocationService;
import org.hni.security.om.ActivationCode;
import org.hni.security.service.ActivationCodeService;
import org.hni.user.dao.UserDAO;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrderProcessor implements OrderProcessor {

    private static Logger logger = LoggerFactory.getLogger(DefaultOrderProcessor.class);

    public static String MSG_ENDMEAL = "ENDMEAL";
    public static String MSG_STATUS = "STATUS";
    public static String MSG_MEAL = "MEAL";
    public static String MSG_ORDER = "ORDER";
    public static String MSG_CONFIRM = "CONFIRM";
    public static String MSG_REDO = "REDO";
    
	public static String REPLY_NOT_CURRENTLY_ORDERING = "You're not currently ordering, please respond with MEAL to place an order.";
    public static String REPLY_ORDER_CANCELLED = "You've cancelled your order.";
    public static String REPLY_ORDER_GET_STARTED = "Yes! Let's get started to order a meal for you. ";
	public static String REPLY_ORDER_REQUEST_ADDRESS = "Reply with your location (e.g. #3 Smith St. 72758) or ENDMEAL to quit";
    public static String REPLY_PROVIDERS_UNAVAILABLE = "Providers currently unavailable. Reply with new location or try again later. Reply ENDMEAL to quit. ";
    public static String REPLY_NO_PROVIDERS = "There are no providers near your location. Reply with new location or ENDMEAL to quit.";
    // In this flow, user chooses meal here : Reply 1) Ham sandwich 2) Tacos 3) Chicken Salad
    public static String REPLY_MULTIPLE_ORDERS = "You can order up to %d meals. How many would you like?";
    public static String REPLY_CONFIRM_ORDER = "You've chosen %s at %s. Reply CONFIRM to place this order, REDO to try again or ENDMEAL to quit.";
    public static String REPLY_ORDER_COMPLETE = "Success! Order confirmed. Reply with STATUS after 5 minutes to check to status of your order.";
    public static String REPLY_NEED_VALID_RESPONSE = "Please respond with CONFIRM, REDO, or ENDMEAL";
    public static String REPLY_ORDER_PENDING = "Your order is still open, please respond with STATUS in 5 minutes to check again.";
    public static String REPLY_ORDER_READY = "Your order has been placed and should be ready to pick up shortly from %s at %s %s.";
    public static String REPLY_ORDER_CLOSED = "Your order has been marked as closed.";
    public static String REPLY_ORDER_NOT_FOUND = "I can't find a recent order for you, please reply MEAL to place an order.";

    public static String REPLY_ORDER_ITEM = "%d) %s from %s %s %s. ";
    public static String REPLY_ORDER_CHOICE = "Reply %s to choose your meal. ";
    
    public static String REPLY_NO_UNDERSTAND = "I don't understand that. Reply with MEAL to place an order.";
    public static String REPLY_INVALID_INPUT = "Invalid input! ";
    public static String REPLY_EXCEPTION_REGISTER_FIRST = "Youll need to reply with REGISTER to sign up first.";
    public static String REPLY_MAX_ORDERS_REACHED = "You've reached the maximum number of orders for today. Please come back tomorrow.";
    
    @Inject
    private UserDAO userDao;

    @Inject
    private DefaultPartialOrderDAO partialOrderDAO;

    @Inject
    private ProviderLocationService locationService;

    @Inject
    private OrderService orderService;
    @Inject
    private ActivationCodeService activationCodeService;

    @Inject
    private EventRouter eventRouter;
    
    // TODO: @Value("${address.search.radius}")
    private static final double RADIUS = 6371.01;
    
    // TODO: @Value("${address.search.distance}")
    private static final double DISTANCE_IN_MILES = 10.0;
    
    // TODO: @Value("${address.search.itemsPerPage}")
    private static final Integer ITEMS_PER_PAGE = 3;

    @PostConstruct
    void init() {
        if (eventRouter.getRegistered(EventName.MEAL) != this) {
            eventRouter.registerService(EventName.MEAL, this);
        }
    }

    public String processMessage(User user, String message) {
        PartialOrder order = partialOrderDAO.byUser(user);
        boolean cancellation = message.equalsIgnoreCase(MSG_ENDMEAL);

        if (order == null && cancellation) {
            return REPLY_NOT_CURRENTLY_ORDERING;
        } else if (order == null && !message.equalsIgnoreCase(MSG_STATUS)) {
        	
        	if (orderService.maxDailyOrdersReached(user)) {
        		return REPLY_MAX_ORDERS_REACHED;
        	}
            order = new PartialOrder();
            order.setTransactionPhase(TransactionPhase.MEAL);
            order.setUser(user);
        } else if (order == null) {
            //status check
            return checkOrderStatus(user);
        } else if (cancellation) {
            partialOrderDAO.delete(order);
            return REPLY_ORDER_CANCELLED;
        }

        TransactionPhase phase = order.getTransactionPhase();
        String output = "";

        switch (phase) {
            case MEAL:
                output = requestingMeal(user, message, order);
                break;
            case PROVIDING_ADDRESS:
                output = findNearbyMeals(message, order);
                break;
            case CHOOSING_LOCATION:
                output = chooseLocation(user, message, order);
                break;
            case CHOOSING_MENU_ITEM:
                //this is chosen w/ provider for now
                break;
            case MULTIPLE_ORDER:
            	handleMultipleOrders(user, message, order);
            	break;
            case CONFIRM_OR_REDO:
                return confirmOrContinueOrder(message, order);
            default:
                //shouldn't get here
        }
        partialOrderDAO.save(order);
        return output;
    }

    public String processMessage(Long userId, String message) {
        return processMessage(userDao.get(userId), message);
    }

    private String requestingMeal(User user, String request, PartialOrder order) {
        if (request.equalsIgnoreCase(MSG_MEAL) || request.equalsIgnoreCase(MSG_ORDER)) {
        	order.setTransactionPhase(TransactionPhase.PROVIDING_ADDRESS);
            return REPLY_ORDER_GET_STARTED + REPLY_ORDER_REQUEST_ADDRESS;
        } else {
            return REPLY_NO_UNDERSTAND;
        }
    }

    private String findNearbyMeals(String addressString, PartialOrder order) {
        String output = "";
        try {
            List<ProviderLocation> nearbyProviders = (ArrayList<ProviderLocation>) locationService.providersNearCustomer(addressString, 
                    ITEMS_PER_PAGE, DISTANCE_IN_MILES, RADIUS);
            if (!nearbyProviders.isEmpty()) {
                order.setAddress(addressString);
                List<ProviderLocation> nearbyWithMenu = new ArrayList<>();
                List<MenuItem> items = new ArrayList<>();
                for (ProviderLocation location : nearbyProviders) {
                    Optional<Menu> currentMenu = location.getProvider().getMenus().stream()
                            .filter(menu -> isCurrent(menu)).findFirst();
                    if (currentMenu.isPresent()) {
                        nearbyWithMenu.add(location);
                        items.add(currentMenu.get().getMenuItems().iterator().next());
                    }
                }
                if (!nearbyWithMenu.isEmpty()) {
                    order.setProviderLocationsForSelection(nearbyWithMenu);
                    order.setMenuItemsForSelection(items);
                    output += providerLocationMenuOutput(order);
                    order.setTransactionPhase(TransactionPhase.CHOOSING_LOCATION);
                } else {
                    output = REPLY_PROVIDERS_UNAVAILABLE;
                }
            } else {
                output = REPLY_NO_PROVIDERS;
            }
        } catch (AddressException e) {
            output = e.getMessage();
        }

        return output;
    }


    private String chooseLocation(User user, String message, PartialOrder order) {
        String output = "";
        try {
            int index = Integer.parseInt(message);
            if (index < 1 || index > 3) {
                throw new IndexOutOfBoundsException();
            }
            ProviderLocation location = order.getProviderLocationsForSelection().get(index - 1);
            order.setChosenProvider(location);
            MenuItem chosenItem = order.getMenuItemsForSelection().get(index - 1);
            order.getMenuItemsSelected().add(chosenItem);
            logger.debug("Location {} has been chosen with item {}", location.getName(), chosenItem.getName());
            
            // If this user has multiple auth codes we'll want to ask them how many of this item 
            List<ActivationCode> activationCodes = activationCodeService.getByUser(user);
            if (activationCodes.size() > 1) {
                order.setTransactionPhase(TransactionPhase.MULTIPLE_ORDER);
                output = String.format(REPLY_MULTIPLE_ORDERS, activationCodes.size());
            }
            else {
	            order.setTransactionPhase(TransactionPhase.CONFIRM_OR_REDO);
	            output = String.format(REPLY_CONFIRM_ORDER, chosenItem.getName(), location.getName());
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            output += REPLY_INVALID_INPUT;
            output += providerLocationMenuOutput(order);
        }
        return output;
    }

    private String handleMultipleOrders(User user, String message, PartialOrder order) {
    	String output = "" ;
    	
    	int num;
		try {
			num = Integer.parseInt(message);
		} catch (NumberFormatException e) {
			// if they can't type in a valid number, set to ZERO and next phase is REDO
			num = 0;
		}
    	
    	List<ActivationCode> activationCodes = activationCodeService.getByUser(user);
    	logger.debug("# activationCodes=" + activationCodes.size());
    	if (num <= 0) {
			logger.info("Reset order choices for PartialOrder {} by user request", order.getId());
			//clear out previous choices
			output = findNearbyMeals(order.getAddress(), order);
    	}
    	else {
    		if (num > activationCodes.size()) {
	    		// Too many - order them the maximum and move on
    			logger.debug("User requested more meals than they have. Req=" + num + " actual=" + activationCodes.size());
	    		num = activationCodes.size();
    		}
	 		Collection<MenuItem> menuItems = order.getMenuItemsSelected();
			MenuItem menuItem = menuItems.iterator().next();
			
	 		for (int x=0; x < num-1; x++) {
	 			logger.debug("Adding menuItem to partialOrder");
				menuItems.add(menuItem);
			}
	 		// Set next phase to confirm order
	 		order.setTransactionPhase(TransactionPhase.CONFIRM_OR_REDO);
	 		output = REPLY_CONFIRM_ORDER;
    	}
		partialOrderDAO.save(order);
		return output;

    }
    private String confirmOrContinueOrder(String message, PartialOrder order) {
        String output = "";
        
        if (message.equalsIgnoreCase(MSG_CONFIRM)) {
                //create a final order and set initial info
                Order finalOrder = new Order();
                finalOrder.setUserId(order.getUser().getId());
                finalOrder.setOrderDate(new Date());
                finalOrder.setProviderLocation(order.getChosenProvider());
                //set items being ordered
                Collection<MenuItem> chosenItems = order.getMenuItemsSelected();
                Set<OrderItem> orderedItems = chosenItems.stream().map(mItem -> new OrderItem(1L, mItem.getPrice(), mItem))
                        .collect(Collectors.toSet());
                orderedItems.forEach(item -> item.setOrder(finalOrder));
                finalOrder.setOrderItems(orderedItems);
                finalOrder.setSubTotal(orderedItems.stream().map(item -> (item.getAmount() * item.getQuantity())).reduce(0.0, Double::sum));
                finalOrder.setStatus(OrderStatus.OPEN);
                orderService.save(finalOrder);
                partialOrderDAO.delete(order);
                logger.info("Successfully created order {}", finalOrder.getId());
                output = REPLY_ORDER_COMPLETE;
 	    }
        else if (message.equalsIgnoreCase(MSG_REDO)) {
                 logger.info("Reset order choices for PartialOrder {} by user request", order.getId());
                //clear out previous choices
                output = findNearbyMeals(order.getAddress(), order);
                //save here because I know caller won't
                partialOrderDAO.save(order);
        }
        else {
                output += REPLY_NEED_VALID_RESPONSE;
        }
        return output;
    }

    private String checkOrderStatus(User user) {
        Collection<Order> orders = orderService.get(user, LocalDate.now());
        Optional<Order> order = orders.stream().sorted((a,b) -> b.getOrderDate().compareTo(a.getOrderDate())).findFirst();
        if (order.isPresent()) {
            OrderStatus status = order.get().getOrderStatus();
            if (status.equals(OrderStatus.OPEN)) {
                return REPLY_ORDER_PENDING;
            } else if (status.equals(OrderStatus.ORDERED)) {
                return String.format(REPLY_ORDER_READY, order.get().getProviderLocation().getAddress().getAddress1(), order.get().getProviderLocation().getAddress().getCity());
            } else {
                //TODO should we say anything for if they suspect an error
                return REPLY_ORDER_CLOSED;
            }
        } else {
            return REPLY_ORDER_NOT_FOUND;
        }
    }

    /**
     * This method loops through the providerLocations of an order to create a string output of them the menu items
     * they contain.
     *
     * @param order
     * @return
     */
    private String providerLocationMenuOutput(PartialOrder order) {
    	
    	// Note: spaces are significant in the Strings below! 
    	String options = "";
    	
        String meals = "";
        for (int i = 0; i < order.getProviderLocationsForSelection().size(); i ++) {
            ProviderLocation location = order.getProviderLocationsForSelection().get(i);
            options += (i+1) + ", ";
            meals += String.format(REPLY_ORDER_ITEM, (i+1), order.getMenuItemsForSelection().get(i).getName(), location.getName(), location.getAddress().getAddress1() + (StringUtils.isNotEmpty(location.getAddress().getAddress2())?" " + location.getAddress().getAddress2():""), location.getAddress().getCity());
        }
        // remove training comma and space
        options = options.substring(0, options.length() - 2);
        
        // if there's more than 1 option remove the last number, space and comma and replace with or #
        if (options.length() > 1) { 
        	options = options.substring(0, options.length() - 3 );
        	options += " or " + order.getProviderLocationsForSelection().size();
        }
        return String.format(REPLY_ORDER_CHOICE, options) + meals;
    }

    private boolean isCurrent(Menu menu) {
        //TODO this has issues between 11:00 and 11:59 pm because minutes are not stored in db
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(menu.getStartHourAvailable().intValue(), 0);
        LocalTime end = LocalTime.of(menu.getEndHourAvailable().intValue(), 0);

        if (start.compareTo(end) < 0) {
            //eg start at 06:00 and end at 12:00
            return start.compareTo(now) < 0 && now.compareTo(end) < 0;
        } else {
            //eg start at 11:00 end at 04:00
            return (start.compareTo(now) < 0 && now.compareTo(LocalTime.MAX) < 0) ||
                    (now.compareTo(LocalTime.MIN) > 0 && now.compareTo(end) < 0);
        }
    }

    @Override
    public String handleEvent(Event event) {

        // Look up the user
        String phoneNumber = event.getPhoneNumber();
        List<User> users = userDao.byMobilePhone(phoneNumber);

        if (users != null && !users.isEmpty()) {
            // process the text message
            return processMessage(users.get(0), event.getTextMessage().trim());
        } else {
            String message = "OrderProcessor failed to lookup user by phone " + phoneNumber;
            logger.error(message);
            return REPLY_EXCEPTION_REGISTER_FIRST;
        }
    }
}
