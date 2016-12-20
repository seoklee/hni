package org.hni.order.service.rs.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseRestClient {

    private final static Logger logger = LoggerFactory.getLogger(BaseRestClient.class);

    protected final WebClient getWebClient() {
        final List<Object> providers = new ArrayList<>();
        final JacksonJsonProvider provider = new JacksonJsonProvider();
        provider.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        provider.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        providers.add(provider);
        return WebClient.create(getBaseUrl(), providers, true)
                .type(MediaType.APPLICATION_JSON);
    }

    protected abstract String getBaseUrl();
}