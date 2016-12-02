package org.hni.admin.service;

import java.time.LocalDate;
import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.hni.common.Constants;
import org.hni.common.DateUtils;
import org.hni.common.exception.HNIException;
import org.hni.order.om.Order;
import org.hni.order.om.OrderItem;
import org.hni.order.service.OrderService;
import org.hni.payment.om.OrderPayment;
import org.hni.payment.service.OrderPaymentService;
import org.hni.provider.om.Menu;
import org.hni.provider.om.MenuItem;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.hni.provider.om.ProviderLocationHour;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "/orders", description = "Operations on Orders and OrderItems")
@Component
@Path("/orders")
public class OrderController extends AbstractBaseController {
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	@Inject private OrderService orderService;
	@Inject private OrderPaymentService orderPaymentService; // for resets
	
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the order with the given id"
		, notes = ""
		, response = Order.class
		, responseContainer = "")
	public String getOrder(@PathParam("id") Long id) {
		return serializeOrderToJson(orderService.get(id));
	}

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Creates a new order or saves the order with the given id"
		, notes = "An order without an ID field will be created"
		, response = Order.class
		, responseContainer = "")
	public String saveOrder(Order order) {
		if (isPermitted(Constants.ORDER, Constants.CREATE, 0L)) {
			return serializeOrderToJson(orderService.save(order));
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@DELETE
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Deletes the order with the given id"
		, notes = ""
		, response = Order.class
		, responseContainer = "")
	public String getDelete(@PathParam("id") Long id) {
		if (isPermitted(Constants.ORDER, Constants.DELETE, 0L)) {
			return serializeOrderToJson(orderService.delete(new Order(id)));
		}
		throw new HNIException("You must have elevated permissions to do this.");
	}

	@GET
	@Path("/next")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the next order obtaining a lock on it so others cannot work on it at the same time.  You may pass an optional parameter 'providerId' to filter to a specific provider"
		, notes = ""
		, response = Order.class
		, responseContainer = "")
	public Response getNextOrder(@QueryParam("providerId") Long providerId) {
		Order order = null;
		if ( null != providerId ) {
			order = orderService.next(new Provider(providerId));
		} 
		if (null == order) { // if no order is availale for a provider just grab whatever comes next.
			order = orderService.next();
		}
		if ( null != order ) {
			logger.info("Returning Order "+order.getId());
			logger.info(serializeOrderToJson(order));
			return Response.status(Response.Status.OK).
	                entity(serializeOrderToJson(order)).
	                type(MediaType.APPLICATION_JSON).
	                build();
		}
		return Response.status(Response.Status.NO_CONTENT).
                entity("{}").
                type(MediaType.APPLICATION_JSON).
                build();

	}

	@GET
	@Path("/{id}/reset")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "resets the order for testing"
		, notes = ""
		, response = Order.class
		, responseContainer = "")
	public String resetOrder(@PathParam("id") Long id) {
		if (isPermitted(Constants.ORDER, Constants.CREATE, 0L)) {
			if ( null != id ) {
				Order order = orderService.get(id);
				orderService.reset(order);
				for(OrderPayment op : orderPaymentService.paymentsFor(order)) {
					orderPaymentService.delete(op);
				}
			}
			return String.format("{\"message\":\"reset order %d\"}", id);
		}
		throw new HNIException("You must have elevated permissions to do this.");
				
	}

	@DELETE
	@Path("/lock/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Forcibly, removes a lock, if it exists, from the order"
		, notes = ""
		, response = Order.class
		, responseContainer = "")
	public void removeLock(@PathParam("id") Long id) {
		//if (isPermitted(Constants.ORDER, Constants.CREATE, 0L)) {
			logger.info("Unlocking Order "+id);
			orderService.releaseLock(orderService.get(id));
		//}
		//throw new HNIException("You must have elevated permissions to do this.");
	}

	private static final String ORDER_COUNT = "{\"order-count\":\"%d\"}";
	@GET
	@Path("/count")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns the # pending orders. You may pass an optional parameter 'providerId' to filter to a specific provider"
		, notes = ""
		, response = Order.class
		, responseContainer = "")
	public String getOrderCount(@QueryParam("providerId") Long providerId) {
		if ( null != providerId ) {
			return String.format(ORDER_COUNT, orderService.countOrders(new Provider(providerId)));
		}
		return String.format(ORDER_COUNT, orderService.countOrders());
	}
	
	
	@GET
	@Path("/users/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Returns a collection of orders for the given user between the given dates.  If the endDate is not supplied it will default to current date"
	, notes = "accepted date formats yyyy-mm-dd, yyyy/mm/dd, mm-dd-yyyy, mm/dd/yyyy"
	, response = Order.class
	, responseContainer = "")
	public Collection<Order> getUserOrdersBetweenDates(@PathParam("id") Long id, @QueryParam("startDate") String startDate , @QueryParam("endDate") String endDate) {
		
		LocalDate start = DateUtils.parseDate(startDate);
		LocalDate end = LocalDate.now();
		if ( !StringUtils.isEmpty(endDate) ) {
			end = DateUtils.parseDate(endDate);
		}
		return orderService.get(new User(id), start, end);
	}

	private String serializeOrderToJson(Order order) {
		try {
			String json = mapper.writeValueAsString(JsonView.with(order)
					.onClass(User.class, Match.match().exclude("*").include("id", "firstName", "lastName"))
					.onClass(ProviderLocation.class, Match.match().include("*").exclude("created", "createdById"))
					.onClass(ProviderLocationHour.class, Match.match().exclude("*").include("dow", "openHour", "closeHour"))
					.onClass(Provider.class, Match.match().include("*").exclude("created", "createdById"))
					.onClass(OrderItem.class, Match.match().include("*").exclude("order"))
					.onClass(Menu.class, Match.match().include("*").exclude("provider"))
					.onClass(MenuItem.class, Match.match().include("*").exclude("menu")));
			return json;
		} catch (Exception e) {
			logger.error("Serializing User object:"+e.getMessage(), e);
		}
		return "{}";
	}
}
