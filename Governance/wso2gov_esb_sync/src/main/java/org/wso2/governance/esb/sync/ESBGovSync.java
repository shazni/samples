package org.wso2.governance.esb.sync;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;
import org.wso2.governance.esb.sync.ServiceAdminClient;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ESBGovSync {
    private static final Log log = LogFactory.getLog(ESBGovSync.class);

    private static String authenticate(String serviceURL, String username, String password)
            throws AxisFault, AuthenticationException {
        String cookie = null;
        String serviceEndpoint = serviceURL + "AuthenticationAdmin";

        AuthenticationAdminStub stub = new AuthenticationAdminStub(serviceEndpoint);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        try {
            boolean result = stub.login(username, password, new URL(serviceEndpoint).getHost());
            if (result) {
                cookie = (String) stub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING);
            }
            return cookie;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw new AuthenticationException(msg, e);
        }
    }

    private static ServiceMetaData[] getServiceMetaData() throws AuthenticationException, RemoteException {
        ServiceAdminClient client = DataHolder.getServiceAdminClient();
        ServiceMetaDataWrapper servicesInfo = client.getAllServices("ALL", "", 0);
        return servicesInfo.getServices();
    }

    private static void initialize() {
        DataHolder.setEsbUserName((System.getenv("ESB_USERNAME")) != null ? System.getenv("ESB_USERNAME") : "admin");
        DataHolder.setEsbPassword((System.getenv("ESB_PWD")) != null ? System.getenv("ESB_PWD") : "admin");
        DataHolder.setEsbServicesURL((System.getenv("ESB_SERVICE_URL")) != null ? System.getenv("ESB_SERVICE_URL") : "https://localhost:9443/services/");

        DataHolder.setGovernanceUserName((System.getenv("GOV_USERNAME")) != null ? System.getenv("GOV_USERNAME") : "admin");
        DataHolder.setGovernancePassword((System.getenv("GOV_PWD")) != null ? System.getenv("GOV_PWD") : "admin");
        DataHolder.setGovernanceWSDLURL((System.getenv("GOV_URL")) != null ? System.getenv("GOV_URL") : "https://localhost:9444/governance/");

        DataHolder.setEsbTrustoreLocation((System.getenv("ESB_TRUSTSTORE")) != null ? System.getenv("ESB_TRUSTSTORE") : "/home/esb/wso2carbon.jks");
        DataHolder.setEsbTrustorePassword((System.getenv("ESB_TRUSTSTORE_PWD")) != null ? System.getenv("ESB_TRUSTSTORE_PWD") : "wso2carbon");
        DataHolder.setEsbTrustoreType((System.getenv("ESB_TRUSTSTORE_TYPE")) != null ? System.getenv("ESB_TRUSTSTORE_TYPE") : "JKS");

        DataHolder.setGovTrustoreLocation((System.getenv("GOV_TRUSTSTORE")) != null ? System.getenv("GOV_TRUSTSTORE") : "/home/governance/wso2carbon.jks");
        DataHolder.setGovTrustorePassword((System.getenv("GOV_TRUSTSTORE_PWD")) != null ? System.getenv("GOV_TRUSTSTORE_PWD") : "wso2carbon");
        DataHolder.setGovTrustoreType((System.getenv("GOV_TRUSTSTORE_TYPE")) != null ? System.getenv("GOV_TRUSTSTORE_TYPE") : "JKS");
    }

    public static void main(String[] args) {
        System.out.println("Welcome to ESBGovSync");
        log.info("Welcome to ESBGovSync");
        initialize();

        // Use System.getProperty("name"); for VM arguments or System.getEnv()
        System.setProperty("javax.net.ssl.trustStore", DataHolder.getEsbTrustoreLocation());
        System.setProperty("javax.net.ssl.trustStorePassword", DataHolder.getEsbTrustorePassword());
        System.setProperty("javax.net.ssl.trustStoreType", DataHolder.getEsbTrustoreType());

        try {
            String cookie = authenticate(DataHolder.getEsbServicesURL(), DataHolder.getEsbUserName(), DataHolder.getEsbPassword());
            DataHolder.setCookie(cookie);
            ServiceAdminClient client = new ServiceAdminClient(cookie, DataHolder.getEsbServicesURL());
            DataHolder.setServiceAdminClient(client);

            ServiceMetaData[] servicesInfo = getServiceMetaData();

            GovernaneRESTClient govClient = new GovernaneRESTClient(DataHolder.getGovTrustoreLocation(), DataHolder.getGovTrustorePassword());
            govClient.getWSDLMetaData();
            Map<String, String> mapWSDL = govClient.getMapWSDLNameToContentURL();

            for (ServiceMetaData serviceInfo : servicesInfo) {
                String wsdlURL = serviceInfo.getWsdlURLs()[0];
                final String name = serviceInfo.getName();

                log.info("Service name = " + name + ", WSDL = " + wsdlURL);
                log.info("======>");

                ServiceMetaData serviceMetaData = client.getServiceMetaDta(name);

                log.info("Active = " + serviceMetaData.getActive() + " CApp = " + serviceMetaData.getCAppArtifact()
                        + " Scope = " + serviceMetaData.getScope() + " Deployed time = " + serviceMetaData.getServiceDeployedTime());

                String wsdlContent = Utils.getUrlContents(wsdlURL);
                log.info("WSDL content = " + wsdlContent);
                log.info("---------------------------------------------------------------");

                if(mapWSDL.containsKey(name + ".wsdl")) {
                    String wsdl = govClient.getWSDLContent(name + ".wsdl");
                } else {
                    govClient.addWSDL(name, "1.0.0", wsdlContent);
                }
            }

            // Following is failing
//            log.info("WSDL content = " + client.getWsdl("RuleMediatorTest"));
        } catch (AuthenticationException ex) {
            log.error("An error occurred while authenticating");
        } catch(ServiceAdminException ex) {
            log.error("ServiceAdminException occurred");
        } catch (RemoteException ex) {
            log.error("An error occurred while calling a remote endpoint");
        }

//        GovernaneRESTClient govClient = new GovernaneRESTClient(DataHolder.getGovTrustoreLocation(), DataHolder.getGovTrustorePassword());
//        govClient.getWSDLMetaData();
    }
}
