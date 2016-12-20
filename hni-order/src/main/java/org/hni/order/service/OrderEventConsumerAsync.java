package org.hni.order.service;

import org.hni.order.om.Order;
import org.redisson.api.RFuture;
import org.redisson.api.annotation.RRemoteAsync;

/**
 * an asynchronous interface for remote invocation
 */
@RRemoteAsync(OrderEventConsumer.class)
public interface OrderEventConsumerAsync {

    RFuture<Void> process(Order order);
}
