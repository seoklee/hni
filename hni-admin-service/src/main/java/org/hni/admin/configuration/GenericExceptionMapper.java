package org.hni.admin.configuration;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
	private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);
	@Override
	public Response toResponse(Exception e) {
		logger.error("Unknown error has occurred "+e.getMessage(), e);
		return Response.status(Response.Status.BAD_REQUEST).
                entity(String.format("{\"message\":\"An unknown error occurred\"}"+e.getMessage())).
                type(MediaType.APPLICATION_JSON).
                build();
	}

}
