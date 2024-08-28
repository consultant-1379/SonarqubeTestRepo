/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.itpf.security.sso.resource.interceptor;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.interceptor.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.*;

/**
 * The intercepter class for checking the access control for user.
 */
@Authorize
@Interceptor
public class AuthorizeInterceptor {

    public static final String ACCESS_DENIED = "Access Denied: <br/> One of the following capabilities: <br/><br/>%s <br/>is required";

    @Inject
    private EAccessControl accessControl;

    @Inject
    private Logger logger;

    /**
     * Checks if the user has access to the application.
     */
    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception { // NOPMD

        logger.debug("AuthorizeInterceptor");

        final Method calledMethod = ic.getMethod();
        final Authorize authAnnotation = calledMethod.getAnnotation(Authorize.class);
        final String action = authAnnotation.action();
        final String[] resource = authAnnotation.resource();
        logger.debug("Action: {} ---- resource: {}", action, resource);

        boolean isAuthorized = false;

        for (final String capability : resource) {
            try {

                logger.debug("Testing, if is allowed to {} on {}", action, capability);

                isAuthorized = accessControl.isAuthorized(new ESecurityResource(capability), new ESecurityAction(action), new EPredefinedRole[] {});

                if (isAuthorized) {
                    logger.debug("Is authorized");
                    return ic.proceed();
                }
            } catch (final SecurityViolationException e) {
                logger.debug(e.getMessage(), e);
            }
        }

        if (!isAuthorized) {

            logger.warn("User is not authorized to access {} ", calledMethod.getName());

            final StringBuilder strBuilder = new StringBuilder();

            for (int i = 0; i < resource.length; i++) {
                strBuilder.append(resource[i]).append("<br/>");
            }

            return Response.status(Status.UNAUTHORIZED).entity(String.format(ACCESS_DENIED, strBuilder)).build();
        }
        return ic.proceed();
    }
}
