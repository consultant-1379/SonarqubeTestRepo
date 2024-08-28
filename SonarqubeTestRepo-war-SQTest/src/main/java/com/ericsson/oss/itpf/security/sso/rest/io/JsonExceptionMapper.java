package com.ericsson.oss.itpf.security.sso.rest.io;


/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.JsonProcessingException;

/**
 * Created by emapawl on 2016-01-19.
 */

@Provider
public class JsonExceptionMapper implements ExceptionMapper<JsonProcessingException>{

    @Override
    public Response toResponse(JsonProcessingException arg0) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTime(ErrorResponse.getCurrentTime());
        errorResponse.setUserMessage("Invalid request: " + arg0.toString());
        errorResponse.setHttpStatusCode(Status.BAD_REQUEST.getStatusCode());

        return Response.status(errorResponse.getHttpStatusCode()).entity(errorResponse).build();

    }

}