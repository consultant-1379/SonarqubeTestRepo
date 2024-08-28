package com.ericsson.oss.itpf.security.sso.service;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import javax.ejb.Remote;

/**
 * Created by ekrzsia on 2/7/17.
 */
@EService
@Remote
public interface UserAuthenticator {

    public Boolean verifyUser(String username, String password);
}
