package com.ericsson.oss.itpf.security.sso.ejb.beans;

import com.ericsson.oss.itpf.security.sso.ejb.sysinit.ServiceBootstrap;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenID;
import com.iplanet.sso.SSOTokenManager;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by epatjuc on 4/18/2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthManagerRest.class, SSOTokenManager.class})
public class AuthManagerRestTest {

    private static final String LOGIN_URL = "http://sso.enmapache.athtem.eei.ericsson.se:8080/heimdallr/UI/Login?service=DataStore&IDToken1=user&IDToken2=password";
    private static final String LOGOUT_URL = "http://sso.enmapache.athtem.eei.ericsson.se:8080/heimdallr/json/sessions/?_action=logout";

    @Mock
    private ServiceBootstrap serviceBootstrap;

    @Spy
    @InjectMocks
    private AuthManagerRest authManagerRest = new AuthManagerRest();

    /**
     The testcase tests method login() of class AuthManagerRest.
     */
    @Test
    public void testLogin1() throws Exception {
        //Mocks
        SSOToken ssoToken = Mockito.mock(SSOToken.class);
        PowerMockito.doReturn(ssoToken).when(authManagerRest,  "createToken", Mockito.<ClientResponse<String>>any());
        ClientRequest clientRequest = Mockito.mock(ClientRequest.class);
        ClientResponse<String> authResponse =  Mockito.mock(ClientResponse.class);
        Mockito.when(authResponse.getStatus()).thenReturn(200);
        Mockito.when(clientRequest.get()).thenReturn(authResponse);
        PowerMockito.whenNew(ClientRequest.class).withParameterTypes(String.class).withArguments(LOGIN_URL).thenReturn(clientRequest);
        Whitebox.setInternalState(AuthManagerRest.class, "loginUrl", LOGIN_URL);

        //Invoke tested method
        SSOToken response = authManagerRest.login();

        //Verify result
        Assert.assertEquals(ssoToken, response);
        Mockito.verify(clientRequest, Mockito.times(1)).get();
        PowerMockito.verifyPrivate(authManagerRest, Mockito.times(1)).invoke("createToken", authResponse);
    }

    /**
     The testcase tests method login(String) of class AuthManagerRest.
     */
    @Test
    public void testLogin2() throws Exception {
        //Mocks
        SSOToken ssoToken = Mockito.mock(SSOToken.class);
        PowerMockito.doReturn(ssoToken).when(authManagerRest, "createToken", Mockito.<ClientResponse<String>>any());
        ClientRequest clientRequest = Mockito.mock(ClientRequest.class);
        ClientResponse<String> authResponse =  Mockito.mock(ClientResponse.class);
        Mockito.when(authResponse.getStatus()).thenReturn(200);
        Mockito.when(clientRequest.get()).thenReturn(authResponse);
        PowerMockito.whenNew(ClientRequest.class).withParameterTypes(String.class).withArguments(LOGIN_URL).thenReturn(clientRequest);
        Whitebox.setInternalState(AuthManagerRest.class, "superUser", "user");
        Whitebox.setInternalState(AuthManagerRest.class, "superPassword", "password");

        //Invoke tested method
        SSOToken response = authManagerRest.login("http://sso.enmapache.athtem.eei.ericsson.se:8080/heimdallr");

        //Verify result
        Assert.assertEquals(ssoToken, response);
        Mockito.verify(clientRequest, Mockito.times(1)).get();
        PowerMockito.verifyPrivate(authManagerRest, Mockito.times(1)).invoke("createToken", authResponse);
    }

    /**
     The testcase tests method logout(SSOToken) of class AuthManagerRest.
     */
    @Test
    public void testLogout1() throws Exception {
        //Mocks
        SSOToken ssoToken = Mockito.mock(SSOToken.class);
        SSOTokenID ssoTokenID = Mockito.mock(SSOTokenID.class);
        Mockito.when(ssoToken.getTokenID()).thenReturn(ssoTokenID);
        ClientRequest clientRequest = Mockito.mock(ClientRequest.class);
        PowerMockito.whenNew(ClientRequest.class).withParameterTypes(String.class).withArguments(LOGOUT_URL).thenReturn(clientRequest);
        Mockito.when(clientRequest.header("iPlanetDirectoryPro", ssoToken.getTokenID())).thenReturn(clientRequest);
        ClientResponse<String> logoutResponse =  Mockito.mock(ClientResponse.class);
        Mockito.when(clientRequest.post()).thenReturn(logoutResponse);
        Whitebox.setInternalState(AuthManagerRest.class, "logoutUrl", LOGOUT_URL);

        //Invoke tested method
        authManagerRest.logout(ssoToken);

        //Verify result
        Mockito.verify(clientRequest, Mockito.times(1)).header("iPlanetDirectoryPro", ssoToken.getTokenID());
        Mockito.verify(clientRequest, Mockito.times(1)).post();
    }

    /**
     The testcase tests method logout(SSOToken, String) of class AuthManagerRest.
     */
    @Test
    public void testLogout2() throws Exception {
        //Mocks
        SSOToken ssoToken = Mockito.mock(SSOToken.class);
        SSOTokenID ssoTokenID = Mockito.mock(SSOTokenID.class);
        Mockito.when(ssoToken.getTokenID()).thenReturn(ssoTokenID);
        ClientRequest clientRequest = Mockito.mock(ClientRequest.class);
        PowerMockito.whenNew(ClientRequest.class).withParameterTypes(String.class).withArguments(LOGOUT_URL).thenReturn(clientRequest);
        Mockito.when(clientRequest.header("iPlanetDirectoryPro", ssoToken.getTokenID())).thenReturn(clientRequest);
        ClientResponse<String> logoutResponse =  Mockito.mock(ClientResponse.class);
        Mockito.when(clientRequest.post()).thenReturn(logoutResponse);

        //Invoke tested method
        authManagerRest.logout(ssoToken, "http://sso.enmapache.athtem.eei.ericsson.se:8080/heimdallr");

        //Verify result
        Mockito.verify(clientRequest, Mockito.times(1)).header("iPlanetDirectoryPro", ssoToken.getTokenID());
        Mockito.verify(clientRequest, Mockito.times(1)).post();
    }


    /**
     The testcase tests private method createLogin(ClientResponse<String>) of class AuthManagerRest.
     It verifies that tested method creates proper SSOToken when input parameter contains iPlanetDirectoryPro cookie.
     */
    @Test
    public void testCreateToken1() throws Exception {
        //Mocks
        final SSOTokenManager ssoTokenManager = Mockito.mock(SSOTokenManager.class);
        PowerMockito.mockStatic(SSOTokenManager.class);
        PowerMockito.when(SSOTokenManager.getInstance()).thenReturn(ssoTokenManager);

        Mockito.when(ssoTokenManager.createSSOToken(Mockito.anyString())).thenAnswer(
                new Answer<SSOToken>() {
                    @Override
                    public SSOToken answer(InvocationOnMock invocationOnMock) throws Throwable {
                        SSOToken ssoToken = Mockito.mock(SSOToken.class);
                        SSOTokenID ssoTokenID = Mockito.mock(SSOTokenID.class);
                        Mockito.when(ssoTokenID.toString()).thenReturn((String) invocationOnMock.getArguments()[0]);
                        Mockito.when(ssoToken.getTokenID()).thenReturn(ssoTokenID);
                        return ssoToken;
                    }
                }
        );

        //Mock argument of method createToken(ClientResponse<String>) of class AuthManagerRest
        List<String> cookieList = new ArrayList<>();
        cookieList.add("AMAuthCookie=AQIC5wM2LY4Sfcys-21QgzjDL0SR09Fp834fv6VyvGt8Mg4.*AAJTSQACMDIAAlNLABMzMjAxNjM0NjU1ODg5NzgyNDg5AAJTMQACMDE.*; Domain=.ericsson.se; Path=/");
        cookieList.add("amlbcookie=01; Domain=.ericsson.se; Path=/");
        cookieList.add("iPlanetDirectoryPro=AQIC5wM2LY4SfcylYb274fTPzxDoj3n0lqySx04QdPZ3g7k.*AAJTSQACMDIAAlNLABQtNDE5OTU0NzUyNDM0MDQ0NTUxMQACUzEAAjAx*; Domain=.ericsson.se; Path=/");
        cookieList.add("AMAuthCookie=LOGOUT; Domain=.ericsson.se; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/");
        cookieList.add("ssocookie=ssocookie-2; path=/");
        ClientResponse<String> response = Mockito.mock(ClientResponse.class);
        MultivaluedMap<String, String> multivaluedMap = Mockito.mock(MultivaluedMap.class);
        Mockito.when(multivaluedMap.get("Set-Cookie")).thenReturn(cookieList);
        Mockito.when(response.getHeaders()).thenReturn(multivaluedMap);

        //Invoke tested method
        SSOToken resultToken = Whitebox.invokeMethod(authManagerRest, "createToken", response);

        //Verify result
        Assert.assertEquals("AQIC5wM2LY4SfcylYb274fTPzxDoj3n0lqySx04QdPZ3g7k.*AAJTSQACMDIAAlNLABQtNDE5OTU0NzUyNDM0MDQ0NTUxMQACUzEAAjAx*", resultToken.getTokenID().toString());
        Mockito.verify(ssoTokenManager, Mockito.times(1)).createSSOToken("AQIC5wM2LY4SfcylYb274fTPzxDoj3n0lqySx04QdPZ3g7k.*AAJTSQACMDIAAlNLABQtNDE5OTU0NzUyNDM0MDQ0NTUxMQACUzEAAjAx*");
    }

    /**
     The testcase tests private method createLogin(ClientResponse<String>) of class AuthManagerRest.
     It verifies that tested method does not create any SSOToken when input parameter  does not contain iPlanetDirectoryPro cookie.
     */
    @Test
    public void testCreateToken2() throws Exception {
        //Mocks
        final SSOTokenManager ssoTokenManager = Mockito.mock(SSOTokenManager.class);
        PowerMockito.mockStatic(SSOTokenManager.class);
        PowerMockito.when(SSOTokenManager.getInstance()).thenReturn(ssoTokenManager);

        //Mock argument of method createToken(ClientResponse<String>) of class AuthManagerRest
        ClientResponse<String> response = Mockito.mock(ClientResponse.class);
        MultivaluedMap<String, String> multivaluedMap = Mockito.mock(MultivaluedMap.class);
        Mockito.when(multivaluedMap.get("Set-Cookie")).thenReturn(new ArrayList<String>(Arrays.asList("anotherCookie1=anotherCookie1; Domain=.ericsson.se;", "anotherCookie2=anotherCookie2; Domain=.ericsson.se;")));
        Mockito.when(response.getHeaders()).thenReturn(multivaluedMap);

        //Invoke tested method
        SSOToken resultToken = Whitebox.invokeMethod(authManagerRest, "createToken", response);

        //Verify result
        Assert.assertEquals(null, resultToken);
        Mockito.verify(ssoTokenManager, Mockito.times(0)).createSSOToken(Mockito.anyString());
    }

}
