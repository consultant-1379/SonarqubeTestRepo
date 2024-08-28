package com.ericsson.oss.itpf.security.sso.ejb.services;

import java.util.Set;

import javax.ejb.Local;

@Local
public interface OpenAMParamsManager {

    void setOrganizationServiceAttribute(String serviceName, String key, String value);
    void setOrganizationServiceAttributes(final String serviceName, final String key, final Set<String> values);
    void setGlobalServiceAttribute(final String serviceName, final String key, final String value);
}
