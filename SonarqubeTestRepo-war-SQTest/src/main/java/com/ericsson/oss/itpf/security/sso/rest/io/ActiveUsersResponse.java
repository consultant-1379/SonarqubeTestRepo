package com.ericsson.oss.itpf.security.sso.rest.io;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.Map;


/**
 * Created by jagu on 2015-12-03.
 */

@XmlRootElement
public class ActiveUsersResponse {

  public Map<String, ?> users;

  public ActiveUsersResponse(Map<String, ?> users) {
    this.users = users;
  }
}
