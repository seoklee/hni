package org.hni.order.service.rs.client;

import org.hni.order.om.rs.OrderConfirmedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;

@Component
public class SlackWebHookClient extends BaseRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackWebHookClient.class);

    @Value("#{hniProperties['slackWebhookUrl']}")
    private String SLACK_WEBHOOK_URL;

    @Override
    protected String getBaseUrl() {
        return SLACK_WEBHOOK_URL;
    }

    public Response postMessage(final OrderConfirmedMessage message) {
        return getWebClient().post(message);
    }
}
