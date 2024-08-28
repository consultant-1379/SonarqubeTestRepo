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

package com.ericsson.oss.itpf.security.sso.ejb.beans;

import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Rest;
import com.ericsson.oss.itpf.security.sso.service.UserAuthenticator;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.ServiceBootstrap;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by ekrzsia on 2/6/17.
 */
@Stateless
@Rest
public class UserAuthenticatorRest implements UserAuthenticator {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String X_OPEN_AM_USERNAME = "X-OpenAM-Username";
    public static final String X_OPEN_AM_PASSWORD = "X-OpenAM-Password";
    public static final String APPLICATION_JSON = "application/json";
    private final Logger logger = LoggerFactory.getLogger(UserAuthenticatorRest.class);

    private static String userAuthenticationUrl;
    private static final String userAuthenticationQuery = "/json/authenticate?noSession=true";

    @Inject
    private ServiceBootstrap serviceBootstrap;

    @PostConstruct
    private void init() {
        userAuthenticationUrl = serviceBootstrap.getSsoInstanceUrl() + userAuthenticationQuery;
    }

    @Override
    public Boolean verifyUser(final String username, final String password) {

        final ClientResponse<String> response = authenticationRequest(username, password);
        final int httpCode = response.getResponseStatus().getStatusCode();

        if(httpCode == 200){
            logger.debug("User {} is authenticated.", username);
            return new Boolean(true);
        }
        else {
            logger.debug("Authentication of user {} failed, httpCode: {}, entity: {}", username, httpCode, response.getEntity(String.class));
            return new Boolean(false);
        }
    }

    public ClientResponse<String> authenticationRequest(final String username, final String password) {

        ClientResponse<String> clientResponse = null;

        final ClientRequest clientRequest = new ClientRequest(userAuthenticationUrl);

        clientRequest.header(CONTENT_TYPE, APPLICATION_JSON)
                .header(X_OPEN_AM_USERNAME, username)
                .header(X_OPEN_AM_PASSWORD, password);

        try {
            logger.debug("Requesting for checking authentication");
            clientResponse = clientRequest.post();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clientResponse;
    }
}
