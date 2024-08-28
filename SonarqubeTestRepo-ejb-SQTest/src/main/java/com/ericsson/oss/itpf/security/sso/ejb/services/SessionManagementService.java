package com.ericsson.oss.itpf.security.sso.ejb.services;

import com.iplanet.sso.SSOToken;

import javax.ejb.Local;
import java.util.Set;

/**
 * Created by jagu on 2015-12-03.
 */

@Local
public interface SessionManagementService {

  Set<SSOToken> getValidSessions();

  Set<SSOToken> getValidSessions(String property, String value);

  void destroySessions(Set<SSOToken> sessions);

}
