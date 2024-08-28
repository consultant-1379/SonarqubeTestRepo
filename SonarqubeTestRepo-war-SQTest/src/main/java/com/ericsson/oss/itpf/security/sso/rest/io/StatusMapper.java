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


import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emapawl on 2016-01-19.
 */

public class StatusMapper {

    @Inject
    private static Logger logger = LoggerFactory.getLogger(StatusMapper.class);

    public static ErrorResponse mapException(Throwable thrownException) {
        Throwable exception = thrownException;

            logger.error("Caught unknown exception", exception);

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setTime(ErrorResponse.getCurrentTime());
            errorResponse.setUserMessage(thrownException
                    .getMessage());
            errorResponse.setHttpStatusCode(Status.INTERNAL_SERVER_ERROR
                    .getStatusCode());
            return errorResponse;

    }
}
