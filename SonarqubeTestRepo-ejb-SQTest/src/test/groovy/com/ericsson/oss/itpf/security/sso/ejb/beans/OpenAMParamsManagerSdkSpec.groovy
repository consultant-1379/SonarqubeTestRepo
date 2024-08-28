package com.ericsson.oss.itpf.security.sso.ejb.beans

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.security.sso.ejb.services.AuthService
import com.iplanet.sso.SSOToken
import com.sun.identity.sm.ServiceConfig
import com.sun.identity.sm.ServiceSchema

class OpenAMParamsManagerSdkSpec extends CdiSpecification{


    def String IPLANET_AM_AUTH_CUSTOMLDAP_SEARCH_SCOPE="iplanet-am-auth-customldap-search-scope";
    def String AM_AUTH_CUSTOM_LDAP_SERVICE = "iPlanetAMAuthCustomLDAPService";

    @MockedImplementation
    private AuthService authService;

    def manager = Spy(OpenAMParamsManagerSdk);
    def spy = Spy(OpenAMParamsManagerSdk);

    private Map<String, Set<String>> attrs;

    def setup(){
        attrs =  new HashMap<String, Set<String>>()
    }


/*
    def 'TORF-195033 - Add a PIB Listener on SSO   setAttribute'(){

        given: 'iPlanetAMAuthService'

        def token = Mock(SSOToken)
        manager.login() >> token
        def config = Mock(ServiceConfig)
        def schema = Mock(ServiceSchema)
        def attributeSchema = Mock(AttributeSchema)
        manager.getOrganizationConfig(_,_) >> config
        manager.getServiceSchema(_,_,_) >> schema
        config.getAttributes() >> attrs
        schema.getAttributeSchema(_) >>
        manager.logout(token) >> null

        when: 'Setting the searchScope attribute'

        manager.setOrganizationServiceAttribute(AM_AUTH_CUSTOM_LDAP_SERVICE,IPLANET_AM_AUTH_CUSTOMLDAP_SEARCH_SCOPE,'SUBTREE')

        then: 'attribute is set to openAM'

        1 *  manager.login() >> token
        1 *  manager.logout(token) >> null

    } */

}
