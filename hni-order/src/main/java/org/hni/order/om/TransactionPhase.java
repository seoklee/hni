package org.hni.order.om;

/**
 * Enum for holding the current step of the order process.
 */
public enum TransactionPhase {

    MEAL,
    PROVIDING_ADDRESS,
    CHOOSING_LOCATION,
    CHOOSING_MENU_ITEM,
    CONFIRM_OR_REDO,
    MULTIPLE_ORDER

}
