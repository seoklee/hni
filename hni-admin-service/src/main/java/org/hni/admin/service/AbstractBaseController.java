package org.hni.admin.service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.ThreadContext;
import org.hni.common.Constants;
import org.hni.organization.service.OrganizationUserService;
import org.hni.user.om.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.JsonViewSerializer;

public class AbstractBaseController {

	@Inject protected OrganizationUserService organizationUserService;
	
    protected ObjectMapper mapper = new ObjectMapper();
    protected SimpleModule module = new SimpleModule();
    
	@PostConstruct
    public void configureJackson() {
            module.addSerializer(JsonView.class, new JsonViewSerializer());
            mapper.registerModule(module);
    }
	
	protected User getLoggedInUser() {
		Long userId = (Long)ThreadContext.get(Constants.USERID);
		return organizationUserService.get(userId);
	}
    protected boolean isPermitted(String domain, String permission, Long id) {
    	if ( SecurityUtils.getSubject().isPermitted(createPermission(domain,permission,id))) {
    		return true;
    	}
    	return false;
    	
    }

    private String createPermission(String domain, String action, Long instance)
    {
    	return String.format("%s:%s:%d", domain, action, instance);
    }

    protected Response createResponse(String message, Response.Status status) {
    	return Response.status(status).entity(String.format("{\"message\":\"%s\"}", message.toString())).type(MediaType.APPLICATION_JSON).build();
    }

    protected Response createResponse(String message) {
    	return createResponse(message, Response.Status.OK);
    }

}
