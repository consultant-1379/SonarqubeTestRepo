package com.ericsson.oss.itpf.security.sso.ejb.beans;


import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Rest;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.ServiceBootstrap;
import com.ericsson.oss.itpf.security.sso.ejb.services.AuthService;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.resteasy.client.ClientRequest;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by ejakgub on 11/30/15.
 */

@Stateless
@Rest
public class AuthManagerRest implements AuthService {

  public static final String ssoCookieName = "iPlanetDirectoryPro";
  private static String cookieSet = "Set-Cookie";
  private static String superUser;
  private static String superPassword;
  private static String loginUrl;
  private static String logoutUrl;
  private Logger logger = LoggerFactory.getLogger(AuthManagerRest.class);

  @Inject
  private ServiceBootstrap serviceBootstrap;

  @PostConstruct
  private void init() {
    superUser = serviceBootstrap.getSsoAdminUsername();
    superPassword = serviceBootstrap.getSsoAdminPwd();
    loginUrl = serviceBootstrap.getSsoInstanceUrl() + "/UI/Login?service=DataStore&IDToken1=" + superUser + "&IDToken2=" + superPassword;
    logoutUrl = serviceBootstrap.getSsoInstanceUrl().concat("/json/sessions/?_action=logout");
  }

  @Override
  public SSOToken login() throws SSOException {
    ClientRequest loginRequest = new ClientRequest(loginUrl);
    ClientResponse<String> authResponse = null;

    try {
      logger.debug("Authentication request");
      authResponse = loginRequest.get();
      logger.debug("Authentication response status={}", authResponse.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return createToken(authResponse);
  }

  public SSOToken login(String server) throws SSOException {
    String serverLoginUrl = server + "/UI/Login?service=DataStore&IDToken1=" + superUser + "&IDToken2=" + superPassword;
    ClientRequest loginRequest = new ClientRequest(serverLoginUrl);
    ClientResponse<String> authResponse = null;

    try {
      logger.debug("Authentication request");
      authResponse = loginRequest.get();
      logger.debug("Authentication response status={}", authResponse.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return createToken(authResponse);
  }

  public void logout(SSOToken ssoToken) {
    ClientRequest logoutRequest = new ClientRequest(logoutUrl);

    try {
      ClientResponse<String> logoutResponse = logoutRequest.header(ssoCookieName, ssoToken.getTokenID()).post();
      logger.debug("Logout response: {}", logoutResponse.getEntity(String.class));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void logout(SSOToken ssoToken, String server) {
    String serverLogoutUrl = server.concat("/json/sessions/?_action=logout");
    ClientRequest logoutRequest = new ClientRequest(serverLogoutUrl);

    try {
      ClientResponse<String> logoutResponse = logoutRequest.header(ssoCookieName, ssoToken.getTokenID()).post();
      logger.debug("Logout response: {}", logoutResponse.getEntity(String.class));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private SSOToken createToken(ClientResponse<String> response) throws SSOException {
    SSOTokenManager ssoTokenManager = SSOTokenManager.getInstance();
    SSOToken ssoToken = null;
    List<String> l = response.getHeaders().get(cookieSet);

    for (String s : l) {
      if (s.contains(ssoCookieName)) {
        ssoToken = ssoTokenManager.createSSOToken(s.split(";")[0].split("=")[1]);
      }
    }
    if (ssoToken != null) {
      logger.debug("new token id={}", ssoToken.getTokenID().toString());
    } else {
      logger.error("Could not create {} for {}",
              ssoCookieName,serviceBootstrap.getSsoAdminUsername());
    }
    return ssoToken;
  }
}
