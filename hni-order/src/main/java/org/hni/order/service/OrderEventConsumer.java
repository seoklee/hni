package org.hni.order.service;

import org.hni.order.om.Order;

public interface OrderEventConsumer {

    void process(Order order);
}
