package com.ericsson.oss.itpf.security.sso.ejb.beans;

import java.util.*;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.security.sso.ejb.constants.OpenAMConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Rest;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Sdk;
import com.ericsson.oss.itpf.security.sso.ejb.services.AuthService;
import com.ericsson.oss.itpf.security.sso.ejb.services.OpenAMParamsManager;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.sm.*;

/**
 * @author egicass/emimarz
 */
@Stateless
@Sdk
public class OpenAMParamsManagerSdk implements OpenAMParamsManager {

    private Logger logger = LoggerFactory.getLogger(OpenAMParamsManagerSdk.class);

    @Inject
    @Rest
    private AuthService authService;

    @Override
    public void setOrganizationServiceAttribute(final String serviceName, final String key, final String value) {
        if (serviceName == null) {
            logger.error("serviceName argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        if (key == null) {
            logger.error("key argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        if (value == null) {
            logger.error("value argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        SSOToken token = null;
        try {
            token = login();
            logger.debug("logged In");
            final ServiceConfig config = getOrganizationConfig(token, serviceName, OpenAMConfigConstants.SERVICE_VERSION);
            if (config == null) {
                logger.error("Service config for service {} null!", serviceName);
                throw new SSOException("Service config NULL for service " + serviceName);
            }
            final ServiceSchema schema = getServiceSchema(token, serviceName, OpenAMConfigConstants.SERVICE_VERSION);
            if (schema == null) {
                logger.error("Service schema for service {} null!", serviceName);
                throw new SSOException("Service schema NULL for service " + serviceName);
            }
            setAttribute(config, schema, key, value);

        } catch (final SMSException e) {
            final String strError = e.getMessage();
            logger.error("SMS_EXCEPTION_SET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);

        } catch (final SSOException e) {
            final String strError = e.getMessage();
            logger.error("SSO_EXCEPTION_SET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);

        } finally {
            if (token != null) {
                logout(token);
                logger.debug("{}:logged Out", getClass().getCanonicalName());
            }
        }
    }

    @Override
    public void setGlobalServiceAttribute(final String serviceName, final String key, final String value) {
        if (serviceName == null) {
            logger.error("serviceName argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        if (key == null) {
            logger.error("key argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        if (value == null) {
            logger.error("value argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        SSOToken token = null;
        try {
            token = login();
            logger.debug("logged In");

            ServiceSchemaManager scm = new ServiceSchemaManager(serviceName, token);
            ServiceSchema sessionSchema = scm.getGlobalSchema();

            if (sessionSchema == null) {
                logger.error("Service schema for service {} null!", serviceName);
                throw new SSOException("Service schema NULL for service " + serviceName);
            }
            final Set<String> valuesSet = new HashSet<String>(new ArrayList<String>(Arrays.asList(value)));

            logger.debug("Getting previous service attributes");
            printSchemaAttributes(sessionSchema);

            sessionSchema.setAttributeDefaults(key,valuesSet);

            logger.debug("Getting service attributes after setting");
            printSchemaAttributes(sessionSchema);

        } catch (final SMSException e) {
            final String strError = e.getMessage();
            logger.error("SMS_EXCEPTION_SET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);

        } catch (final SSOException e) {
            final String strError = e.getMessage();
            logger.error("SSO_EXCEPTION_SET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);

        } finally {
            if (token != null) {
                logout(token);
                logger.debug("{}:logged Out", getClass().getCanonicalName());
            }
        }
    }

    @Override
    public void setOrganizationServiceAttributes(final String serviceName, final String key, final Set<String> valuesSet) {
        if (serviceName == null) {
            logger.error("serviceName argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        if (key == null) {
            logger.error("key argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        if (valuesSet == null) {
            logger.error("valuesSet argument NULL to setOrganizationServiceAttribute");
            throw new IllegalArgumentException("Null argument to setOrganizationServiceAttribute method");
        }
        SSOToken token = null;
        try {
            token = login();
            logger.debug("logged In");
            final ServiceConfig config = getOrganizationConfig(token, serviceName, OpenAMConfigConstants.SERVICE_VERSION);
            if (config == null) {
                logger.error("Service config for service {} null!", serviceName);
                throw new SSOException("Service config NULL for service " + serviceName);
            }
            final ServiceSchema schema = getServiceSchema(token, serviceName, OpenAMConfigConstants.SERVICE_VERSION);
            if (schema == null) {
                logger.error("Service schema for service {} null!", serviceName);
                throw new SSOException("Service schema NULL for service " + serviceName);
            }
            setAttributes(config, schema, key, valuesSet);

        } catch (final SMSException e) {
            final String strError = e.getMessage();
            logger.error("SMS_EXCEPTION_SET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);

        } catch (final SSOException e) {
            final String strError = e.getMessage();
            logger.error("SSO_EXCEPTION_SET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);

        } finally {
            if (token != null) {
                logout(token);
                logger.debug("{}:logged Out", getClass().getCanonicalName());
            }
        }
    }

    protected void setAttribute(final ServiceConfig config, final ServiceSchema schema, final String key, final String value) {
        final Set<String> valuesSet = new HashSet<String>(new ArrayList<String>(Arrays.asList(value)));
        final Map<String, Set<String>> attrs = new HashMap<String, Set<String>>();
        attrs.put(key, valuesSet);
        setAttribute(config, schema, attrs);
    }

    protected void setAttributes(final ServiceConfig config, final ServiceSchema schema, final String key, final Set<String> valuesSet) {
        final Map<String, Set<String>> attrs = new HashMap<String, Set<String>>();
        attrs.put(key, valuesSet);
        setAttribute(config, schema, attrs);
    }

    protected void setAttribute(final ServiceConfig config, final ServiceSchema schema, final Map<String, Set<String>> attrs) {
        try {
            logger.debug("Getting previous service attributes");
            printAttributes(config, schema);

            config.setAttributes(attrs);
            logger.info("Parameters set successfull");

            logger.debug("Getting service attributes after setting");
            printAttributes(config, schema);
        } catch (final SMSException e) {
            final String strError = e.getMessage();
            logger.error("SMS_EXCEPTION_GET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);

        } catch (final SSOException e) {
            final String strError = e.getMessage();
            logger.error("SSO_EXCEPTION_GET_ATTR_VALUE_OF_SERVICE_UNDER_REALM: [{}]", strError);
        }
    }

    protected ServiceConfig getOrganizationConfig(final SSOToken token, final String serviceName, final String version) throws SMSException, SSOException {
        //final OrganizationConfigManager ocm = new OrganizationConfigManager(token, ExtIdpConstants.REALM_NAME);
        final ServiceConfigManager scmanager = new ServiceConfigManager(token, serviceName, version);
        return scmanager.getOrganizationConfig(OpenAMConfigConstants.REALM_NAME, null);
    }


    protected ServiceSchema getServiceSchema(final SSOToken token, final String serviceName, final String version) throws SMSException, SSOException {
        final ServiceSchemaManager ssmanager = new ServiceSchemaManager(token, serviceName, version);
        return ssmanager.getSchema(SchemaType.ORGANIZATION);
    }

    protected SSOToken login() throws SSOException {
        return authService.login();
    }

    protected void logout(final SSOToken ssoToken) {
        authService.logout(ssoToken);
    }

    protected void printAttributes(final ServiceConfig config, final ServiceSchema schema) {
        logger.debug("{}: Printing attributes for service {}... ", OpenAMParamsManagerSdk.class.getName(), config.getServiceName());
        final Map<String, Set<String>> attrs = config.getAttributes();

        for (final String attrKey : attrs.keySet()) {

                final AttributeSchema attrSchema = schema.getAttributeSchema(attrKey);
                final AttributeSchema.Syntax attrSyntax = attrSchema.getSyntax();
                if (AttributeSchema.Syntax.PASSWORD.equals(attrSyntax) || AttributeSchema.Syntax.ENCRYPTED_PASSWORD.equals(attrSyntax)) {
                    logger.debug("{}: Values {*********}", attrKey, attrs.get(attrKey));
                } else {
                    logger.debug("{}: Values {}", attrKey, attrs.get(attrKey));
                }
        }
    }



    protected void printSchemaAttributes(final ServiceSchema schema) {
        logger.debug("{}: Printing attributes for service {}... ", OpenAMParamsManagerSdk.class.getName(), schema.getServiceName());
        final Map<String, Set<String>> attrs = schema.getAttributeDefaults();

        for (final String attrKey : attrs.keySet()) {
                    logger.debug("{}: Values {}", attrKey, attrs.get(attrKey));
        }
    }
}
