package com.ericsson.oss.itpf.security.sso.ejb.beans;

import com.ericsson.cds.cdi.support.rule.CdiInjectorRule;
import com.ericsson.cds.cdi.support.rule.MockedImplementation;
import com.ericsson.oss.itpf.security.sso.ejb.services.SessionManagementService;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.ServiceBootstrap;
import com.ericsson.oss.itpf.security.sso.ejb.utils.OpenDSConfiguration;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import org.forgerock.opendj.ldap.Attribute;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.Connections;
import org.forgerock.opendj.ldap.SearchScope;
import org.forgerock.opendj.ldap.requests.Requests;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.forgerock.opendj.ldif.ConnectionEntryReader;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by egicass on 5/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthManagerRest.class,UserManagerSdk.class, SSOTokenManager.class,SSOTokenManager.class})
public class UserManagerSdkTest {


    private static final String LOGIN_URL = "http://sso.enmapache.athtem.eei.ericsson.se:8080/heimdallr/UI/Login?service=DataStore&IDToken1=user&IDToken2=password";
    private static final String LOGOUT_URL = "http://sso.enmapache.athtem.eei.ericsson.se:8080/heimdallr/json/sessions/?_action=logout";

    @Rule
    public CdiInjectorRule cdiInjectorRule = new CdiInjectorRule(this);

    @MockedImplementation
    private ServiceBootstrap serviceBootstrap;

    @Spy
    @InjectMocks
    private UserManagerSdk userManagerSdk = new UserManagerSdk();

    @Spy
    @InjectMocks
    private AuthManagerRest authManagerRest = new AuthManagerRest();

    @MockedImplementation
    OpenDSConfiguration openDSConfiguration = new OpenDSConfiguration();

    @Test
    public void  testDeactivateUsers() throws Exception {


        SSOToken ssoToken = Mockito.mock(SSOToken.class);
        PowerMockito.doReturn(ssoToken).when(authManagerRest,  "createToken", Mockito.any());
        PowerMockito.mockStatic(SSOTokenManager.class);

        ClientRequest clientRequest = Mockito.mock(ClientRequest.class);
        Connection connection =  Mockito.mock(Connection.class);
        ConnectionEntryReader reader =  Mockito.mock(ConnectionEntryReader.class);

        ClientResponse<String> authResponse =  Mockito.mock(ClientResponse.class);
        Mockito.when(authResponse.getStatus()).thenReturn(200);
        Mockito.when(clientRequest.get()).thenReturn(authResponse);
        PowerMockito.whenNew(ClientRequest.class).withParameterTypes(String.class).withArguments(LOGIN_URL).thenReturn(clientRequest);
        Whitebox.setInternalState(AuthManagerRest.class, "loginUrl", LOGIN_URL);

        PowerMockito.whenNew(ClientRequest.class).withParameterTypes(String.class).withArguments(LOGOUT_URL).thenReturn(clientRequest);
        Mockito.when(clientRequest.header("iPlanetDirectoryPro", ssoToken.getTokenID())).thenReturn(clientRequest);
        ClientResponse<String> logoutResponse =  Mockito.mock(ClientResponse.class);
        Mockito.when(clientRequest.post()).thenReturn(logoutResponse);
        Whitebox.setInternalState(AuthManagerRest.class, "logoutUrl", LOGOUT_URL);
        SearchResultEntry entry = Mockito.mock(SearchResultEntry.class);
        Attribute tokenId = Mockito.mock(Attribute.class);
        SSOTokenManager ssoTokenManager = Mockito.mock(SSOTokenManager.class);


        //Mock necessary methods
        Set<SSOToken> exampleSet = new HashSet<>();
        PowerMockito.when(SSOTokenManager.getInstance()).thenReturn(ssoTokenManager);
        Mockito.when(openDSConfiguration.getLdapConnection()).thenReturn(connection);
        Mockito.when(connection.search(Mockito.anyString(), (SearchScope) Mockito.any(),Mockito.anyString(),Mockito.anyString())).thenReturn(reader);
        Mockito.when(reader.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(reader.isEntry()).thenReturn(true);
        Mockito.when(reader.readEntry()).thenReturn(entry);
        Mockito.when(entry.getAttribute(Mockito.anyString())).thenReturn(tokenId);
        Mockito.when(ssoTokenManager.createSSOToken(Mockito.anyString())).thenReturn(ssoToken);

        //Invoke tested method
        userManagerSdk.deactivateUser("user1");

        //Verify result
        Mockito.verify(ssoTokenManager, Mockito.times(1)).destroyToken(ssoToken,ssoToken);
        Mockito.verify(openDSConfiguration, Mockito.times(1)).getLdapConnection();
        Mockito.verify(connection, Mockito.times(1)).close();
    }

    @Test
      public void testGetActiveUsers() throws Exception {

        SSOToken ssoToken = Mockito.mock(SSOToken.class);
        PowerMockito.doReturn(ssoToken).when(authManagerRest,  "createToken", Mockito.any());
        PowerMockito.mockStatic(SSOTokenManager.class);
        ClientRequest clientRequest = Mockito.mock(ClientRequest.class);
        Connection connection =  Mockito.mock(Connection.class);
        ConnectionEntryReader reader =  Mockito.mock(ConnectionEntryReader.class);
        SearchResultEntry entry = Mockito.mock(SearchResultEntry.class);
        Attribute tokenId = Mockito.mock(Attribute.class);
        SSOTokenManager ssoTokenManager = Mockito.mock(SSOTokenManager.class);

        //Mock necessary methods
        Set<SSOToken> exampleSet = new HashSet<>();
        PowerMockito.when(SSOTokenManager.getInstance()).thenReturn(ssoTokenManager);
        Mockito.when(openDSConfiguration.getLdapConnection()).thenReturn(connection);
        Mockito.when(connection.search(Mockito.anyString(), (SearchScope) Mockito.any(),Mockito.anyString(),Mockito.anyString())).thenReturn(reader);
        Mockito.when(reader.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(reader.isEntry()).thenReturn(true);
        Mockito.when(reader.readEntry()).thenReturn(entry);
        Mockito.when(entry.getAttribute(Mockito.anyString())).thenReturn(tokenId);
        Mockito.when(tokenId.firstValueAsString()).thenReturn("id=user1").thenReturn("id=user2");
        Mockito.when(ssoTokenManager.createSSOToken(Mockito.anyString())).thenReturn(ssoToken);

        //Invoke tested method
        Map<String, Integer> userList = (Map<String, Integer>) userManagerSdk.getActiveUsers();
        //Verify result
        Assert.assertEquals(2, userList.size());
        Assert.assertEquals(new Integer(1), userList.get("user1"));
        Assert.assertEquals(new Integer(1), userList.get("user2"));
        Mockito.verify(openDSConfiguration, Mockito.times(1)).getLdapConnection();
        Mockito.verify(connection, Mockito.times(1)).close();
    }
}
