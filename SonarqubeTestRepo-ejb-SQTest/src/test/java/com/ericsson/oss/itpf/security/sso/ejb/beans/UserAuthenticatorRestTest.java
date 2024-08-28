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

import com.ericsson.oss.itpf.security.sso.ejb.sysinit.ServiceBootstrap;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.cache.BrowserCache;
import org.jboss.resteasy.client.cache.CacheInterceptor;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Created by ekrzsia on 2/8/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAuthenticatorRestTest {

    private static final String USERNAME = "operator1";
    private static final String PASSWORD = "TestPassw0rd";

    private final Boolean success = new Boolean(true);
    private final Boolean fail = new Boolean(false);

    @Mock
    private ServiceBootstrap serviceBootstrap;

    @Spy
    private UserAuthenticatorRest userAuthenticatorRestTest;

    @Test
    public void authenticationUserSuccess() throws Exception {

        final String message = "{\"message\":\"Authentication Successful\",\"successUrl\":\"/heimdallr/console\"}";

        ClientResponse<String> response = Mockito.mock(ClientResponse.class);
        Mockito.when(response.getResponseStatus()).thenReturn(Response.Status.OK);
        Mockito.when(response.getEntity()).thenReturn(message);

        Mockito.doReturn(response).when(userAuthenticatorRestTest).authenticationRequest(USERNAME, PASSWORD);

        Assert.assertEquals(userAuthenticatorRestTest.verifyUser(USERNAME, PASSWORD), success);

    }

    @Test
    public void authenticationUserSuccessForceChangePassword() throws Exception {

        final String message = "{\"template\":\"password_change.jsp\",\"authId\":\"eyAidHlwIjogIkpXVCIsICJhbGciOiAiSFMyNTYiIH0.eyAib3RrIjogIm8zcDFmcmJkb3NxdGNmY3I3a2Y1anBkN2NwIiwgInJlYWxtIjogImRjPW9wZW5zc28sZGM9amF2YSxkYz1uZXQiLCAic2Vzc2lvbklkIjogIkFRSUM1d00yTFk0U2ZjenpWQm5HVmIwRWVzc0FVbVlfX3lwbWJTblpQbmVEdDV3LipBQUpUU1FBQ01ESUFBbE5MQUJRdE9UQXpPREl6T0RRek1UazFPVFkwTkRnM05nQUNVekVBQWpBeCoiIH0.VtOjIpIzOjdRLiGK9Dh-R7zDYhoGkiwx41umDn8ykkY\",\"callbacks\":[{\"input\":[{\"name\":\"IDToken1\",\"value\":\"\"}],\"output\":[{\"name\":\"prompt\",\"value\":\"Old Password\"}],\"type\":\"PasswordCallback\"},{\"input\":[{\"name\":\"IDToken2\",\"value\":\"\"}],\"output\":[{\"name\":\"prompt\",\"value\":\"New Password\"}],\"type\":\"PasswordCallback\"},{\"input\":[{\"name\":\"IDToken3\",\"value\":\"\"}],\"output\":[{\"name\":\"prompt\",\"value\":\"Confirm Password\"}],\"type\":\"PasswordCallback\"},{\"input\":[{\"name\":\"IDToken4\",\"value\":0}],\"output\":[{\"name\":\"prompt\",\"value\":\"\"},{\"name\":\"messageType\",\"value\":0},{\"name\":\"options\",\"value\":[\"Submit\",\"Cancel\"]},{\"name\":\"optionType\",\"value\":-1},{\"name\":\"defaultOption\",\"value\":0}],\"type\":\"ConfirmationCallback\"}],\"stage\":\"LDAP2\",\"header\":\"Change Password<BR></BR>Password must be reset.\"}";

        final ClientResponse<String> response = Mockito.mock(ClientResponse.class);
        Mockito.when(response.getResponseStatus()).thenReturn(Response.Status.OK);
        Mockito.when(response.getEntity()).thenReturn(message);

        Mockito.doReturn(response).when(userAuthenticatorRestTest).authenticationRequest(USERNAME, PASSWORD);

        Assert.assertEquals(userAuthenticatorRestTest.verifyUser(USERNAME, PASSWORD), success);

    }

    @Test
    public void authenticationFailed() throws Exception {

        final String message = "{\"code\":401,\"reason\":\"Unauthorized\",\"message\":\"Authentication Failed\"}";

        final ClientResponse<String> response = Mockito.mock(ClientResponse.class);
        Mockito.when(response.getResponseStatus()).thenReturn(Response.Status.UNAUTHORIZED);
        Mockito.when(response.getEntity()).thenReturn(message);

        Mockito.doReturn(response).when(userAuthenticatorRestTest).authenticationRequest(USERNAME, PASSWORD);

        Assert.assertEquals(userAuthenticatorRestTest.verifyUser(USERNAME, PASSWORD), fail);

    }

    @Test
    public void authenticationFailedAccountLocked() throws Exception {

        final String message = "{\"code\":401,\"reason\":\"Unauthorized\",\"message\":\" Your account is locked. Please contact service desk to unlock your account\"}";

        final ClientResponse<String> response = Mockito.mock(ClientResponse.class);
        Mockito.when(response.getResponseStatus()).thenReturn(Response.Status.UNAUTHORIZED);
        Mockito.when(response.getEntity()).thenReturn(message);

        Mockito.doReturn(response).when(userAuthenticatorRestTest).authenticationRequest(USERNAME, PASSWORD);

        Assert.assertEquals(userAuthenticatorRestTest.verifyUser(USERNAME, PASSWORD), fail);

    }
}