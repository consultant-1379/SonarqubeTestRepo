/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.itpf.security.sso.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.ericsson.oss.itpf.security.sso.rest.externalidp.ExtIdpRestService;
import com.ericsson.oss.itpf.security.sso.rest.io.JsonExceptionMapper;
import com.ericsson.oss.itpf.security.sso.rest.session.TimeoutManagementRest;
import com.ericsson.oss.itpf.security.sso.rest.session.UserManagementRest;
import com.ericsson.oss.itpf.security.sso.rest.sonom.SonOmServiceRest;
import com.ericsson.oss.itpf.security.sso.rest.was.WindowsApplicationServerServiceRest;

@ApplicationPath("/")
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(UserManagementRest.class);
        classes.add(TimeoutManagementRest.class);
        classes.add(WindowsApplicationServerServiceRest.class);
        classes.add(SonOmServiceRest.class);
        classes.add(JsonExceptionMapper.class);
        classes.add(ExtIdpRestService.class);
        return classes;
    }
}
