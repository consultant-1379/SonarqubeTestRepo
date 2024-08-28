package com.ericsson.oss.itpf.security.sso.ejb.services;

import javax.ejb.Local;
import java.util.Map;


/**
 * Created by jagu on 2015-11-27.
 */

@Local
public interface UserManagementService {

  Map<String, ?> getActiveUsers();

  void deactivateUser(String userId);

}
