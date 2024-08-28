package com.ericsson.oss.itpf.security.sso.ejb.exceptions;


/**
 * Created by epatjuc on 10/6/2016.
 */
public class PasswordExpirationException extends RuntimeException {

    public PasswordExpirationException() {
        super();
    }

    public PasswordExpirationException(String message) {
        super(message);
    }

    public PasswordExpirationException(String message, Exception cause) {
        super(message, cause);
    }

}
