package com.ericsson.oss.itpf.security.sso.ejb.services;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;


import javax.ejb.Local;

/**
 * Created by ejakgub on 12/7/15.
 */

@Local
public interface AuthService {

  SSOToken login() throws SSOException;

  SSOToken login(String server) throws SSOException;

  void logout(SSOToken ssoToken);

  void logout(SSOToken ssoToken, String server);

}
