package com.ericsson.oss.itpf.security.sso.ejb.beans

import com.ericsson.cds.cdi.support.rule.CdiInjectorRule
import com.ericsson.cds.cdi.support.rule.ImplementationClasses
import com.ericsson.cds.cdi.support.rule.ImplementationInstance
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Rest
import com.ericsson.oss.itpf.security.sso.ejb.services.AuthService
import com.ericsson.oss.itpf.security.sso.ejb.services.UserManagementService
import com.ericsson.oss.itpf.security.sso.ejb.utils.OpenDSConfiguration
import com.iplanet.sso.SSOToken
import com.iplanet.sso.SSOTokenManager
import groovy.mock.interceptor.MockFor
import org.forgerock.opendj.ldap.*
import org.forgerock.opendj.ldap.requests.AbandonRequest
import org.forgerock.opendj.ldap.requests.AddRequest
import org.forgerock.opendj.ldap.requests.BindRequest
import org.forgerock.opendj.ldap.requests.CompareRequest
import org.forgerock.opendj.ldap.requests.DeleteRequest
import org.forgerock.opendj.ldap.requests.ExtendedRequest
import org.forgerock.opendj.ldap.requests.ModifyDNRequest
import org.forgerock.opendj.ldap.requests.ModifyRequest
import org.forgerock.opendj.ldap.requests.SearchRequest
import org.forgerock.opendj.ldap.requests.UnbindRequest
import org.forgerock.opendj.ldap.responses.BindResult
import org.forgerock.opendj.ldap.responses.CompareResult
import org.forgerock.opendj.ldap.responses.ExtendedResult
import org.forgerock.opendj.ldap.responses.GenericExtendedResult
import org.forgerock.opendj.ldap.responses.Result
import org.forgerock.opendj.ldap.responses.SearchResultEntry
import org.forgerock.opendj.ldap.responses.SearchResultReference
import org.forgerock.opendj.ldif.ChangeRecord
import org.forgerock.opendj.ldif.ConnectionEntryReader
import org.junit.Rule
import org.junit.runner.RunWith;
import org.mockito.InjectMocks
import org.mockito.Spy
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import spock.lang.Shared

import javax.inject.Inject
import javax.xml.rpc.handler.HandlerChain
import java.util.concurrent.TimeUnit


class UserManagerSdkSpec extends CdiSpecification{



    @ObjectUnderTest
    UserManagerSdk userManagerSdk

    @MockedImplementation
    OpenDSConfiguration openDSConfiguration

    @MockedImplementation
    private AuthService authService;


    def reader  = Mock(ConnectionEntryReader)
    def connection = Mock(Connection)
    def entry = Mock(SearchResultEntry)
    def attr = Mock(Attribute)

    def setup() {

        GroovyMock(SSOTokenManager, global: true)
        List<String> ipAddresses = new ArrayList<>()
        ipAddresses.add("1.1.1.1")
        ipAddresses.add("2.2.2.2")
        openDSConfiguration.getLdapConnection() >> connection
        openDSConfiguration.getLdapIpAddresses() >> ipAddresses
        openDSConfiguration.getLdapAdminCn() >> "cn=Directory Manager"
        openDSConfiguration.getLdapPassword() >> ['a','b','c']
    }


  /*  def 'Get active users test'(){

        given: '2 active users'

        Map<String, Integer> userIDs;
        connection.search(_,_,_,_) >> reader
        reader.hasNext() >>> [true,true,true,false]
        reader.isEntry() >> true
        reader.readEntry() >> entry
        entry.getAttribute(_ as String) >> attr
        attr.firstValueAsString() >>> [
                "id=administrator,ou=people,dc=ericsson,dc=com",
                "id=administrator,ou=people,dc=ericsson,dc=com",
                "id=egicass,ou=people,dc=ericsson,dc=com"
        ]

        when:  'get the active session'

        userIDs =userManagerSdk.getActiveUsers()

        then: ' I can verify the Sessions reported for that USer are the expected'

        1 * connection.bind('cn=Directory Manager', ["a","b","c"])
        1 * reader.close()
        userIDs.get("administrator") ==2
        userIDs.get("egicass") ==1

    }*/

   /* def 'Terminate session'(){

        given: '2 active session for a given user'
        def ssoToken = Mock(SSOToken)
        authService.login() >> ssoToken
        connection.search(_,_,_,_) >> reader
        reader.hasNext() >>> [true,true,false]
        reader.isEntry() >> true
        reader.readEntry() >> entry
        entry.getAttribute(_ as String) >> attr
        attr.firstValueAsString() >>> [
                "id=administrator,ou=people,dc=ericsson,dc=com",
                "id=administrator,ou=people,dc=ericsson,dc=com",
        ]


        def ssoTokenManager =  Mock(SSOTokenManager);
        SSOTokenManager.getInstance() >> ssoTokenManager
        ssoTokenManager.createSSOToken(_ as String) >> ssoToken

        when: 'terminate sessions for that user'
        userManagerSdk.deactivateUser("administrator")


        then: 'The count of active sessions for that user is 0'
        1 *  reader.close();
        2 *  ssoTokenManager.destroyToken(_ as SSOToken,_ as SSOToken)
    }*/



}
