/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.governance.apim.integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.apim.integration.utils.Constants;
import org.wso2.carbon.governance.apim.integration.utils.HttpRequestUtil;
import org.wso2.carbon.governance.apim.integration.utils.HttpResponse;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class APIMClientRegistration {
    private static final Log log = LogFactory.getLog(APIMClientRegistration.class);

    private static APIMClientRegistration clientRegistration = new APIMClientRegistration() ;

    // For multiple environments these should be changed for maps
    private String consumerSecret;
    private String consumerKey;
    private String userAccessToken;
    private String refreshToken;

    private String encodedConsumerKeySecret;

    public static APIMClientRegistration getInstance() {
        return clientRegistration;
    }

    // These methods should take the environment name as an input - if multiple environments are supported
    @SuppressWarnings("unused")
    public String getConsumerSecret() {
        return consumerSecret;
    }

    @SuppressWarnings("unused")
    public String getConsumerKey() {
        return consumerKey;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    @SuppressWarnings("unused")
    public String getRefreshToken() {
        return refreshToken;
    }

    @SuppressWarnings("unused")
    public String getEncodedConsumerKeySecret() {
        return encodedConsumerKeySecret;
    }

    private APIMClientRegistration() {
        try {
            Registry userRegistry = RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(CarbonContext.getThreadLocalCarbonContext().getUsername(),
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());

            APIMConfigurations apimConfigurations = APIMConfigurations.getInstance();
            // Taking only the first element. When multiple environment is supported. Get the relevant Config
            APIMConfigurations.APIMEnvironmentConfig environmentConfig = apimConfigurations.getApiEnvironmentConfigList().get(0);

            if(userRegistry.resourceExists(Constants.SECRET_KEY_LOCATION)) {
                // If multiple environments need to be supported there has to be multiple consumer secrets stored in the registry
                Resource secretResource = userRegistry.get(Constants.SECRET_KEY_LOCATION);

                byte[] encodedConsumerKeyBytes = (byte []) secretResource.getContent();
                encodedConsumerKeySecret = new String(encodedConsumerKeyBytes);
            } else {
                generateConsumerKeySecret(environmentConfig);

                if(encodedConsumerKeySecret != null && !encodedConsumerKeySecret.isEmpty()) {
                    Resource encodedSecretResource = userRegistry.newResource();
                    encodedSecretResource.setContent(encodedConsumerKeySecret);
                    userRegistry.put(Constants.SECRET_KEY_LOCATION, encodedSecretResource);
                }
            }

            generateAccessToken(environmentConfig);
        } catch(RegistryException ex) {
            log.error("Error while retrieving or inserting the consumer key secret resource - ", ex);
        }
    }

    private void generateConsumerKeySecret(APIMConfigurations.APIMEnvironmentConfig environmentConfig) {
        log.debug("generating consumer secret");

        String clientRegistrationEp = environmentConfig.getClientRegistrationEndpoint();

        String basicAuthHeader = environmentConfig.getAuthUserName() + ":" + environmentConfig.getAuthPassword();

        String applicationRequestBody = createApplicationRequestBody(environmentConfig).toString();
        Map<String, String> requestHeaders = new HashMap<>();

        try {
            byte[] encodedAuthHeaderBytes = Base64.encodeBase64(basicAuthHeader.getBytes("UTF-8"));
            requestHeaders.put("Authorization", "Basic " + new String(encodedAuthHeaderBytes, "UTF-8"));

            requestHeaders.put("Content-Type", "application/json");

            HttpResponse clientRegistrationResponse = HttpRequestUtil.sendRequest(new URL(clientRegistrationEp), applicationRequestBody, requestHeaders, "POST");

            Map clientRegistrationResponseMap = new Gson().fromJson(clientRegistrationResponse.getData(), Map.class);

            log.debug("response code for the client registration request = " + clientRegistrationResponse.getResponseCode());

            if(clientRegistrationResponse.getResponseCode() == 401) {
                throw new RegistryException("Invalid credentials provided. Check your username and password.");
            }

            consumerSecret = (String) clientRegistrationResponseMap.get("clientSecret");
            consumerKey = (String) clientRegistrationResponseMap.get("clientId");

            String basicConsumerAuthHeader = consumerKey + ":" + consumerSecret;
            byte[] encodedBytes = Base64.encodeBase64(basicConsumerAuthHeader.getBytes("UTF-8"));

            encodedConsumerKeySecret = new String(encodedBytes, "UTF-8");
        } catch(UnsupportedEncodingException | MalformedURLException | RegistryException ex) {
            if(ex instanceof  MalformedURLException) {
                log.error("Error in URL. Please check your URL", ex);
            } else if(ex instanceof RegistryException) {
                log.error("An exception generating consumer key and secret - ", ex);
            }
        }
    }

    public void generateAccessToken(APIMConfigurations.APIMEnvironmentConfig environmentConfig) {
        try {
            String requestBody = "grant_type=password&username=" + environmentConfig.getAuthUserName() + "&password=" +
                    environmentConfig.getAuthPassword() + "&scope=apim:api_create apim:api_view apim:api_publish";

            String tokenEndpointURL = environmentConfig.getTokenEndpoint();

            Map<String, String> authenticationRequestHeaders = new HashMap<>();
            authenticationRequestHeaders.put("Authorization", "Basic " + encodedConsumerKeySecret);
            HttpResponse clientAccessTokenResponse = HttpRequestUtil.sendRequest(new URL(tokenEndpointURL), requestBody, authenticationRequestHeaders, "POST");

            Map clientAccessTokenResponseMap = new Gson().fromJson(clientAccessTokenResponse.getData(), Map.class);

            userAccessToken = (String) clientAccessTokenResponseMap.get("access_token");
            refreshToken = (String) clientAccessTokenResponseMap.get("refresh_token");
        } catch(RegistryException ex) {
            log.error("An exception in generating the access token - ", ex);
        } catch (MalformedURLException ex) {
            log.error("Error in URL. Please check your URL", ex);
        }
    }

    private JsonObject createApplicationRequestBody(APIMConfigurations.APIMEnvironmentConfig environmentConfig) {
        JsonObject applicationJsonRequest = new JsonObject();

        applicationJsonRequest.addProperty("callbackUrl", environmentConfig.getCallBackURL());
        applicationJsonRequest.addProperty("clientName", environmentConfig.getClientName());
        applicationJsonRequest.addProperty("tokenScope", environmentConfig.getTokenScope());
        applicationJsonRequest.addProperty("owner", environmentConfig.getClientOwner());
        applicationJsonRequest.addProperty("grantType", environmentConfig.getGrantType());
        applicationJsonRequest.addProperty("saasApp", environmentConfig.getIsSaasApp());

        return applicationJsonRequest;
    }
}
