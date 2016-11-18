package org.hni.admin.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hni.events.service.EventServiceFactory;
import org.hni.events.service.om.Event;
import org.hni.provider.om.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Api(value = "/auth", description = "endpoint to accept SMS messages")
@Component
@Path("/usermessage")
public class SMSUserMessageController extends AbstractBaseController {
    private static final Logger logger = LoggerFactory.getLogger(SMSUserMessageController.class);

    @Autowired
    private EventServiceFactory eventServiceFactory;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(value = "processes an SMS workflow and returns the response as HTML"
            , notes = ""
            , response = Provider.class
            , responseContainer = "")
    public String RespondToMessageHTML(@QueryParam("auth_key") String authKey,
                                       @QueryParam("phonenumber") String phoneNumber,
                                       @QueryParam("sessionid") String sessionId,
                                       @QueryParam("usertext") String userMessage,
                                       @QueryParam("testmode") String testMode) {

        // TODO: return html message
        return null;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "processes an SMS workflow and returns the response as text"
            , notes = ""
            , response = Provider.class
            , responseContainer = "")
    public String RespondToMessage(@QueryParam("auth_key") String authKey,
                                   @QueryParam("phonenumber") String phoneNumber,
                                   @QueryParam("sessionid") String sessionId,
                                   @QueryParam("usertext") String userMessage,
                                   @QueryParam("testmode") String testMode) {
        final Event event = new Event(sessionId, phoneNumber, userMessage);
        return eventServiceFactory.handleEvent(event);

    }
}
