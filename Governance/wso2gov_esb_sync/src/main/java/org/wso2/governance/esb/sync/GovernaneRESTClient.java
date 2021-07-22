package org.wso2.governance.esb.sync;

import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.net.ssl.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GovernaneRESTClient {
    private static final Log log = LogFactory.getLog(GovernaneRESTClient.class);
    Client jersey_client;
    WebTarget webTarget;

    private static String truststorePath;
    private static String truststorePassword = "wso2carbon";


    private Map<String, String> mapWSDLNameToContentURL = new HashMap<>();

    public Map<String, String> getMapWSDLNameToContentURL() {
        return mapWSDLNameToContentURL;
    }

    public static String getTruststorePath() {
        return truststorePath;
    }

    public static void setTruststorePath(String truststorePath) {
        GovernaneRESTClient.truststorePath = truststorePath;
    }

    public static String getTruststorePassword() {
        return truststorePassword;
    }

    public static void setTruststorePassword(String truststorePassword) {
        GovernaneRESTClient.truststorePassword = truststorePassword;
    }

    GovernaneRESTClient(String trustStorePath, String truststorePassword) {
//        jersey_client = JerseyClientBuilder.newClient();

        ClientBuilder clientBuilder = new JerseyClientBuilder();

        setTruststorePath(trustStorePath);
        setTruststorePassword(truststorePassword);

        SSLContext sslContext = null;
        TrustManager[] trustAllCerts = new X509TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("An error occurred while configuring the SSL Context");
        }

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        ClientBuilder newBuilder = ClientBuilder.newBuilder();
        ClientConfig newConfig = new ClientConfig();
        jersey_client = newBuilder.sslContext(sslContext).hostnameVerifier(allHostsValid).withConfig(newConfig).build();
    }

    private static String getAuthorizationHeader() {
        String basicAuth = DataHolder.getGovernanceUserName() + ":" + DataHolder.getGovernancePassword();
        String encodedString = Base64.getEncoder().encodeToString(basicAuth.getBytes());

        return "Basic " + encodedString;
    }

    public String getWSDLContent(String wsdlName) {
        if(mapWSDLNameToContentURL.containsKey(wsdlName)) {
            log.info("The WSDL " + wsdlName + " already exists in Governance");
            String wsdlContentLink = mapWSDLNameToContentURL.get(wsdlName);
            webTarget = jersey_client.target(wsdlContentLink);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            invocationBuilder.header("Authorization", getAuthorizationHeader());
            Response response = invocationBuilder.get();
            String responseString = response.readEntity(String.class);
            log.info("WSDL " + wsdlName + " in Governance = " + responseString);
            return responseString;
        } else {
            log.info("WSDL " + wsdlName + " does not exists in the Governance");
            return "";
        }
    }

    public void addWSDL(String name, String version, String wsdlContent) {
        webTarget = jersey_client.target(DataHolder.getGovernanceWSDLURL() + "wsdls");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization", getAuthorizationHeader());

        ContentAsset contentAsset = new ContentAsset(name, "wsdl", version);
        contentAsset.setAsset_content(wsdlContent);
        String contentAssetObject = new Gson().toJson(contentAsset);
        log.debug("Asset creation request " + contentAssetObject);
        Response response = invocationBuilder.post(Entity.json(contentAssetObject));
        log.info("WSDL creation governance response status = " + response.getStatus());
    }

    public void getWSDLMetaData() {
        webTarget = jersey_client.target(DataHolder.getGovernanceWSDLURL() + "wsdls");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization", getAuthorizationHeader());
        Response response = invocationBuilder.get();
        String responseString = response.readEntity(String.class);

        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(responseString);

        if(jsonTree.isJsonObject()) {
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonElement assetsElement = jsonObject.get("assets");

            if(assetsElement.isJsonArray()) {
                JsonArray assetsArray = assetsElement.getAsJsonArray();
                log.info("Number of WSDLs found " + assetsArray.size());

                for(int k=0; k < assetsArray.size(); ++k) {
                    JsonElement assetElement = assetsArray.get(k);
                    JsonObject assetObject = assetElement.getAsJsonObject();

                    JsonElement nameElement = assetObject.get("name");
                    JsonElement idElement = assetObject.get("id");
                    JsonElement contentLinkElement = assetObject.get("content-link");

                    log.info("Governance WSDL details - " + nameElement.getAsString() + " id - " + idElement.getAsString() +
                            " content-link - " + contentLinkElement.getAsString());
                    mapWSDLNameToContentURL.put(nameElement.getAsString(), contentLinkElement.getAsString());
                }
            }
        }
    }
}
