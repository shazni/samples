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

import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.apim.integration.utils.Constants;
import org.wso2.carbon.governance.apim.integration.utils.HttpRequestUtil;
import org.wso2.carbon.governance.apim.integration.utils.HttpResponse;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class APIMRestClient {
    private static final Log log = LogFactory.getLog(APIMRestClient.class);

    public APIMRestClient() {
    }

    public APIArtifact createAPIArtifact(GenericArtifact apiGenericArtifact) throws RegistryException {
        APIArtifact apiArtifact = new APIArtifact(apiGenericArtifact.getAttribute(Constants.OVERVIEW_NAME), apiGenericArtifact.getAttribute(Constants.OVERVIEW_VERSION));

        apiArtifact.setApiProvider(apiGenericArtifact.getAttribute(Constants.OVERVIEW_PROVIDER));
        apiArtifact.setApiContext(apiGenericArtifact.getAttribute(Constants.OVERVIEW_CONTEXT));
        apiArtifact.setApiDescription(apiGenericArtifact.getAttribute(Constants.OVERVIEW_DESCRIPTION));

        String endpointType = apiGenericArtifact.getAttribute(Constants.IMPLEMENT_ENDPOINT_TYPE);

        switch(endpointType) {
            case Constants.HTTP_ENDPOINT_KEYWORD:
                apiArtifact.setEndpointType(Constants.HTTP_ENDPOINT);
                break;
            case Constants.ADDRESS_ENDPOINT_KEYWORD:
                apiArtifact.setEndpointType(Constants.ADDRESS_ENDPOINT);
                break;
            case Constants.FAILOVER_ENDPOINT_KEYWORD:
                apiArtifact.setEndpointType(Constants.FAILOVER_ENDPOINT);
                break;
            case Constants.LOAD_BALANCED_ENDPOINT_KEYWORD:
                apiArtifact.setEndpointType(Constants.LOAD_BALANCED_ENDPOINT);
                break;
            case Constants.DEFAULT_ENDPOINT_KEYWORD:
                apiArtifact.setEndpointType(Constants.DEFAULT_ENDPOINT);
                break;
            default:
                break;
        }

        apiArtifact.setProductionEndpoint(apiGenericArtifact.getAttribute(Constants.IMPLEMENT_ENDPOINT_URL));
        apiArtifact.setSandboxEndpoint(apiGenericArtifact.getAttribute(Constants.IMPLEMENT_SANDBOX_URL));

        String enableMessageMediationFlow = apiGenericArtifact.getAttribute(Constants.IMPLEMENT_ENABLE_MESSAGE_MEDIATION_FLOW);

        if(enableMessageMediationFlow != null && enableMessageMediationFlow.equals(Constants.TRUE)) {
            apiArtifact.setEnableMessageMediationFlow(true);
        }

        apiArtifact.setInMediationFlow(apiGenericArtifact.getAttribute(Constants.IMPLEMENT_IN_FLOW));
        apiArtifact.setOutMediationFlow(apiGenericArtifact.getAttribute(Constants.IMPLEMENT_OUT_FLOW));
        apiArtifact.setFaultMediationFlow(apiGenericArtifact.getAttribute(Constants.IMPLEMENT_FAULT_FLOW));

        String isDefaultVersion = apiGenericArtifact.getAttribute(Constants.MANAGE_MAKE_THIS_DEFAULT_VERSION);

        if(isDefaultVersion != null && isDefaultVersion.equals(Constants.TRUE)) {
            apiArtifact.setDefaultVersion(true);
        }

        List<String> tiers = new ArrayList<>();

        String isUnlimitedSelected = apiGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_UNLIMITED);
        if(isUnlimitedSelected != null && isUnlimitedSelected.equals(Constants.TRUE)) {
            tiers.add("Unlimited");
        }

        String isGoldSelected = apiGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_GOLD);
        if(isGoldSelected != null && isGoldSelected.equals(Constants.TRUE)) {
            tiers.add("Gold");
        }

        String isSilverSelected = apiGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_SILVER);
        if(isSilverSelected != null && isSilverSelected.equals(Constants.TRUE)) {
            tiers.add("Silver");
        }

        String isBronzeSelected = apiGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_BRONZE);
        if(isBronzeSelected != null && isBronzeSelected.equals(Constants.TRUE)) {
            tiers.add("Bronze");
        }

        apiArtifact.setTiers(tiers);

        String isHTTPEnabled = apiGenericArtifact.getAttribute(Constants.MANAGE_ENABLE_HTTP);
        List<String> transports = new ArrayList<>();
        transports.add(Constants.HTTPS_ENDPOINT);
        if(isHTTPEnabled != null && isHTTPEnabled.equals(Constants.TRUE)) {
            transports.add(Constants.HTTP_ENDPOINT);
        }

        apiArtifact.setApiTransports(transports);

        String isEnableHardThrottlingLimit = apiGenericArtifact.getAttribute(Constants.MANAGE_ENABLE_HARD_THROTTLING_LIMIT);

        if(isEnableHardThrottlingLimit != null && isEnableHardThrottlingLimit.equals(Constants.TRUE)) {
            apiArtifact.setEnableHardThrottlingLimit(true);
        }

        String isResponseCachingEnabled = apiGenericArtifact.getAttribute(Constants.MANAGE_RESPONSE_CACHING);

        if(isResponseCachingEnabled != null && isResponseCachingEnabled.equals("Enabled")) {
            apiArtifact.setResponseCaching("Enabled");
        } else {
            apiArtifact.setResponseCaching("Disabled");
        }

        String visibility = apiGenericArtifact.getAttribute(Constants.MANAGE_VISIBILITY);
        apiArtifact.setVisibility(visibility);

        String visibleRoles = apiGenericArtifact.getAttribute(Constants.MANAGE_VISIBLE_ROLES);

        if(visibleRoles != null) {
            String[] roles = visibleRoles.split(",");
            List<String> roleList = new ArrayList<>();
            Collections.addAll(roleList, roles);
            apiArtifact.setVisibleRoles(roleList);
        }

        String visibleTenants = apiGenericArtifact.getAttribute(Constants.MANAGE_VISIBLE_TENANTS);

        if(visibleTenants != null) {
            String[] tenants = visibleTenants.split(",");
            List<String> tenantList = new ArrayList<>();
            Collections.addAll(tenantList, tenants);
            apiArtifact.setVisibleTenants(tenantList);
        }

        String isLatest = apiGenericArtifact.getAttribute(Constants.MANAGE_IS_LATEST);

        if(isLatest != null && isLatest.equals(Constants.TRUE)) {
            apiArtifact.setLatest(true);
        }

        String businessOwner = apiGenericArtifact.getAttribute(Constants.BUSINESS_INFO_BUSINESS_OWNER);
        String businessOwnerEmail = apiGenericArtifact.getAttribute(Constants.BUSINESS_INFO_BUSINESS_OWNER_EMAIL);
        String technicalOwner = apiGenericArtifact.getAttribute(Constants.BUSINESS_INFO_TECHNICAL_OWNER);
        String technicalOwnerEmail = apiGenericArtifact.getAttribute(Constants.BUSINESS_INFO_TECHNICAL_OWNER_EMAIL);

        if(businessOwner != null) {
            apiArtifact.getBusinessInformation().setBusinessOwner(businessOwner);
        }

        if(businessOwnerEmail != null) {
            apiArtifact.getBusinessInformation().setBusinessOwnerEmail(businessOwnerEmail);
        }

        if(technicalOwner != null) {
            apiArtifact.getBusinessInformation().setTechnicalOwner(technicalOwner);
        }

        if(technicalOwnerEmail != null) {
            apiArtifact.getBusinessInformation().setTechnicalOwnerEmail(technicalOwnerEmail);
        }

        String[] scopeKeys = apiGenericArtifact.getAttributes(Constants.SCOPES_SCOPE_KEY);
        String[] scopeNames = apiGenericArtifact.getAttributes(Constants.SCOPES_SCOPE_NAME);
        String[] scopeRoles = apiGenericArtifact.getAttributes(Constants.SCOPES_ROLES);
        String[] scopeDescriptions = apiGenericArtifact.getAttributes(Constants.SCOPES_DESCRIPTION);

        List<APIArtifact.Scope> scopesList = new ArrayList<>();

        if(scopeKeys != null && scopeNames != null && scopeRoles != null && scopeDescriptions != null) {
            int lengthScopes = scopeKeys.length;
            for(int k = 0; k < lengthScopes ; k++) {
                APIArtifact.Scope scope = apiArtifact.getNewScopeInstance();

                scope.setScopeKey(scopeKeys[k]);
                scope.setScopeName(scopeNames[k]);
                scope.setRoles(scopeRoles[k]);
                scope.setScopeDescription(scopeDescriptions[k]);

                scopesList.add(scope);
            }
        }

        apiArtifact.setScopes(scopesList);

        String[] apiDefinitionMethods = apiGenericArtifact.getAttributes(Constants.API_DEFINITION_METHOD);
        String[] apiDefinitionURLPatterns = apiGenericArtifact.getAttributes(Constants.API_DEFINITION_URL_PATTERN);
        String[] apiDefinitionAuthTypes = apiGenericArtifact.getAttributes(Constants.API_DEFINITION_AUTH_TYPE);
        String[] apiDefinitionThrottling = apiGenericArtifact.getAttributes(Constants.API_DEFINITION_THROTTLING);
        String[] apiDefinitionScopeNames = apiGenericArtifact.getAttributes(Constants.API_DEFINITION_SCOPE_NAME);

        List<APIArtifact.APIDefinition> apiDefinitionList = new ArrayList<>();

        if(apiDefinitionMethods != null && apiDefinitionURLPatterns != null &&
                apiDefinitionAuthTypes != null && apiDefinitionThrottling != null) {
            int numberOfDefinitions = apiDefinitionMethods.length;

            for(int k = 0; k < numberOfDefinitions ; k++) {
                APIArtifact.APIDefinition apiDefinition = apiArtifact.getNewAPIDefinitionInstance();

                apiDefinition.setApiMethod(apiDefinitionMethods[k]);
                apiDefinition.setApiURLPattern(apiDefinitionURLPatterns[k]);
                apiDefinition.setApiAuthType(apiDefinitionAuthTypes[k]);
                apiDefinition.setApiThrottling(apiDefinitionThrottling[k]);

                if(apiDefinitionScopeNames != null) {
                    apiDefinition.setApiScopeName(apiDefinitionScopeNames[k]);
                }

                apiDefinitionList.add(apiDefinition);
            }
        }

        apiArtifact.setApiDefinitions(apiDefinitionList);

        String[] documentNames = apiGenericArtifact.getAttributes(Constants.DOCUMENT_NAME);
        String[] documentSummaries = apiGenericArtifact.getAttributes(Constants.DOCUMENT_SUMMARY);
        String[] documentTypes = apiGenericArtifact.getAttributes(Constants.DOCUMENT_TYPE);
        String[] documentURLs = apiGenericArtifact.getAttributes(Constants.DOCUMENT_URL);

        List<APIArtifact.Document> documentList = new ArrayList<>();

        if(documentNames != null && documentSummaries != null && documentTypes != null && documentURLs != null) {
            int numberOfDocs = documentNames.length;

            for(int k = 0; k < numberOfDocs ; k++) {
                APIArtifact.Document document = apiArtifact.getNewDocumentInstance();

                document.setDocumentName(documentNames[k]);
                document.setDocumentSummary(documentSummaries[k]);
                document.setDocumentType(documentTypes[k]);
                document.setDocumentURL(documentURLs[k]);

                documentList.add(document);
            }
        }

        apiArtifact.setDocuments(documentList);

        return apiArtifact;
    }

    public String createAPIInAPIM(APIArtifact artifact, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) throws RegistryException {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        JsonObject apiCreationRequestBody = buildAPICreationPayload(artifact);

        Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        if(log.isDebugEnabled()) {
            log.debug("API creation payload = " + gson.toJson(apiCreationRequestBody));
        }
        String apiCreationRequestBodyString = gson.toJson(apiCreationRequestBody);

        String apiCreationEp = apiEnvironmentConfig.getPublishingEndpoint();

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        HttpResponse apiCreationResponse;
        String apiId = null;
        int responseCode;

        try {
            apiCreationResponse = HttpRequestUtil.sendRequest(new URL(apiCreationEp), apiCreationRequestBodyString, requestHeaders, Constants.HTTP_METHOD_POST);

            responseCode = apiCreationResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code for API creation in API-M = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                apiCreationResponse = HttpRequestUtil.sendRequest(new URL(apiCreationEp), apiCreationRequestBodyString, requestHeaders, Constants.HTTP_METHOD_POST);

                responseCode = apiCreationResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided for the environment");
                }
            }

            Map clientRegistrationResponseMap = new Gson().fromJson(apiCreationResponse.getData(), Map.class);
            apiId = (String) clientRegistrationResponseMap.get("id");

            if(log.isDebugEnabled()) {
                log.debug("API Creation is successful in the environment");
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred in the send request while creating the API in API Manager", e);
        }

        return apiId;
    }

    public String updateAPIInAPIM(APIArtifact artifact, String apiUUID, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) throws RegistryException {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        JsonObject apiCreationPayload = buildAPICreationPayload(artifact);

        apiCreationPayload.addProperty("id", apiUUID);

        if(artifact.getPermanentLink() != null) {
            apiCreationPayload.addProperty("thumbnailUrl", artifact.getPermanentLink());
        }

        Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        if(log.isDebugEnabled()) {
            log.debug("API update payload = " + gson.toJson(apiCreationPayload));
        }
        String apiUpdateRequestBodyString = gson.toJson(apiCreationPayload);

        String apiCreationEp = apiEnvironmentConfig.getPublishingEndpoint() + apiUUID;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        HttpResponse apiCreationResponse;
        String apiId = null;
        int responseCode;

        try {
            apiCreationResponse = HttpRequestUtil.sendRequest(new URL(apiCreationEp), apiUpdateRequestBodyString, requestHeaders, Constants.HTTP_METHOD_PUT);

            responseCode = apiCreationResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code of update API = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                apiCreationResponse = HttpRequestUtil.sendRequest(new URL(apiCreationEp), apiUpdateRequestBodyString, requestHeaders, Constants.HTTP_METHOD_PUT);

                responseCode = apiCreationResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided for the environment");
                }
            } else if(responseCode == 404) {
                throw new RegistryException("Updating API doesn't exist in the API Manager.");
            }

            Map clientRegistrationResponseMap = new Gson().fromJson(apiCreationResponse.getData(), Map.class);

            apiId = (String) clientRegistrationResponseMap.get("id");

            if(log.isDebugEnabled()) {
                log.debug("API Update successful in the environment");
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred in the send request while updating the API in API Manager", e);
        }

        return apiId;
    }

    private JsonObject buildAPICreationPayload(APIArtifact apiArtifact) {
        JsonObject jsonApiPayLoad = new JsonObject();

        jsonApiPayLoad.addProperty("name", apiArtifact.getApiName());
        jsonApiPayLoad.addProperty("provider", apiArtifact.getApiProvider());
        jsonApiPayLoad.addProperty("version", apiArtifact.getApiVersion());
        jsonApiPayLoad.addProperty("description", apiArtifact.getApiDescription());
        jsonApiPayLoad.addProperty("context", apiArtifact.getApiContext());

        jsonApiPayLoad.addProperty("cacheTimeout", Constants.CACHE_TIMEOUT);
        jsonApiPayLoad.add("subscriptionAvailability", null);
        jsonApiPayLoad.addProperty("isDefaultVersion", apiArtifact.isDefaultVersion());
        jsonApiPayLoad.addProperty("responseCaching", apiArtifact.getResponseCaching());
        jsonApiPayLoad.addProperty("visibility", apiArtifact.getVisibility().toUpperCase());

        JsonObject businessInformation = new JsonObject();

        businessInformation.addProperty("businessOwner", apiArtifact.getBusinessInformation().getBusinessOwner());
        businessInformation.addProperty("businessOwnerEmail", apiArtifact.getBusinessInformation().getBusinessOwnerEmail());
        businessInformation.addProperty("technicalOwner", apiArtifact.getBusinessInformation().getTechnicalOwner());
        businessInformation.addProperty("technicalOwnerEmail", apiArtifact.getBusinessInformation().getTechnicalOwnerEmail());

        JsonArray sequenceArray = new JsonArray();

        String inSequenceFlow = apiArtifact.getInMediationFlow();
        String outSequenceFlow = apiArtifact.getOutMediationFlow();
        String faultSequenceFlow = apiArtifact.getFaultMediationFlow();

        if(!inSequenceFlow.equals("None")) {
            JsonObject jsonInSequenceFlow = new JsonObject();
            jsonInSequenceFlow.addProperty("name", inSequenceFlow);
            jsonInSequenceFlow.add("config", null);
            jsonInSequenceFlow.addProperty("type", "in");

            sequenceArray.add(jsonInSequenceFlow);
        }

        if(!outSequenceFlow.equals("None")) {
            JsonObject jsonOutSequenceFlow = new JsonObject();
            jsonOutSequenceFlow.addProperty("name", outSequenceFlow);
            jsonOutSequenceFlow.add("config", null);
            jsonOutSequenceFlow.addProperty("type", "out");

            sequenceArray.add(jsonOutSequenceFlow);
        }

        if(!faultSequenceFlow.equals("None")) {
            JsonObject jsonFaultSequenceFlow = new JsonObject();
            jsonFaultSequenceFlow.addProperty("name", faultSequenceFlow);
            jsonFaultSequenceFlow.add("config", null);
            jsonFaultSequenceFlow.addProperty("type", "fault");

            sequenceArray.add(jsonFaultSequenceFlow);
        }

        jsonApiPayLoad.add("sequences", sequenceArray);

        jsonApiPayLoad.add("businessInformation", businessInformation);

        JsonArray tiers = new JsonArray();
        if(apiArtifact.getTiers() != null) {
            for (String tier : apiArtifact.getTiers()) {
                JsonPrimitive element = new JsonPrimitive(tier);
                tiers.add(element);
            }
        }
        jsonApiPayLoad.add("tiers", tiers);

        JsonArray transports = new JsonArray();
        if(apiArtifact.getApiTransports() != null) {
            for (String transport : apiArtifact.getApiTransports()) {
                JsonPrimitive element = new JsonPrimitive(transport);
                transports.add(element);
            }
        }
        jsonApiPayLoad.add("transport", transports);

        JsonArray tags = new JsonArray();
        if(apiArtifact.getApiTags() != null) {
            for (String tag : apiArtifact.getApiTags()) {
                JsonPrimitive element = new JsonPrimitive(tag);
                tags.add(element);
            }
        }
        jsonApiPayLoad.add("tags", tags);

        JsonArray visibleRoles = new JsonArray();
        if(apiArtifact.getVisibleRoles() != null) {
            for (String role : apiArtifact.getVisibleRoles()) {
                JsonPrimitive element = new JsonPrimitive(role);
                visibleRoles.add(element);
            }
        }
        jsonApiPayLoad.add("visibleRoles", visibleRoles);

        JsonArray visibleTenants = new JsonArray();
        if(apiArtifact.getVisibleTenants() != null) {
            for (String tenant : apiArtifact.getVisibleTenants()) {
                JsonPrimitive element = new JsonPrimitive(tenant);
                visibleTenants.add(element);
            }
        }
        jsonApiPayLoad.add("visibleTenants", visibleTenants);

        jsonApiPayLoad.addProperty("endpointConfig", createEndpointConfig(apiArtifact));
        jsonApiPayLoad.addProperty("apiDefinition", createApiDefinitionConfig(apiArtifact));

        return jsonApiPayLoad;
    }

    private String createEndpointConfig(APIArtifact apiArtifact) {
        String productionEndpoint = apiArtifact.getProductionEndpoint();
        String sandboxEndpoint = apiArtifact.getSandboxEndpoint();
        String endpointType = apiArtifact.getEndpointType();

        JsonObject endpointJsonConfig = new JsonObject();

        endpointJsonConfig.addProperty("endpoint_type", endpointType);

        if(productionEndpoint != null) {
            JsonObject jsonProductionEndpoint = new JsonObject();
            jsonProductionEndpoint.addProperty("url", productionEndpoint);
            jsonProductionEndpoint.add("config", null);

            endpointJsonConfig.add("production_endpoints", jsonProductionEndpoint);
        }

        if(sandboxEndpoint != null) {
            JsonObject jsonSandboxEndpoint = new JsonObject();
            jsonSandboxEndpoint.addProperty("url", sandboxEndpoint);
            jsonSandboxEndpoint.add("config", null);

            endpointJsonConfig.add("sandbox_endpoints", jsonSandboxEndpoint);
        }

        return endpointJsonConfig.toString();
    }

    private String createApiDefinitionConfig(APIArtifact apiArtifact) {
        JsonObject apiJsonDefinition = new JsonObject();

        JsonObject infoJsonElement = new JsonObject();

        infoJsonElement.addProperty("title", apiArtifact.getApiName());
        infoJsonElement.addProperty("description", apiArtifact.getApiDescription());
        infoJsonElement.addProperty("version", apiArtifact.getApiVersion());

        JsonObject jsonContact = new JsonObject();
        jsonContact.addProperty("email", apiArtifact.getBusinessInformation().getBusinessOwnerEmail());
        jsonContact.addProperty("name", apiArtifact.getBusinessInformation().getBusinessOwner());

        infoJsonElement.add("contact", jsonContact);

        apiJsonDefinition.add("info", infoJsonElement);
        apiJsonDefinition.addProperty("swagger", Constants.SWAGGER_VERSION);

        JsonObject securityElement = new JsonObject();

        JsonObject scopesElement = new JsonObject();
        JsonArray jsonScopes = new JsonArray();

        List<APIArtifact.Scope> scopes = apiArtifact.getScopes();

        for(APIArtifact.Scope scope : scopes) {
            JsonObject scopeElement = new JsonObject();

            scopeElement.addProperty("description", scope.getScopeDescription());
            scopeElement.addProperty("roles", scope.getRoles());
            scopeElement.addProperty("name", scope.getScopeName());
            scopeElement.addProperty("key", scope.getScopeKey());

            jsonScopes.add(scopeElement);
        }

        scopesElement.add("x-wso2-scopes", jsonScopes);
        securityElement.add("apim", scopesElement);
        apiJsonDefinition.add("x-wso2-security", securityElement);

        JsonObject jsonPathsElements = new JsonObject();

        List<APIArtifact.APIDefinition> apiDefinitions = apiArtifact.getApiDefinitions();

        for(APIArtifact.APIDefinition apiDefinition : apiDefinitions) {
            String urlPattern = apiDefinition.getApiURLPattern();
            String method = apiDefinition.getApiMethod().toLowerCase();

            JsonObject jsonOperationElement = new JsonObject();
            jsonOperationElement.addProperty("x-auth-type", apiDefinition.getApiAuthType());
            if(apiDefinition.getApiScopeName() != null && !apiDefinition.getApiScopeName().isEmpty()) {
                jsonOperationElement.addProperty("x-scope", apiDefinition.getApiScopeName());
            }

            if(apiDefinition.getApiThrottling().equals("Application and Application User")) {
                jsonOperationElement.addProperty("x-throttling-tier", "Application & Application User");
            } else {
                jsonOperationElement.addProperty("x-throttling-tier", apiDefinition.getApiThrottling());
            }

            JsonObject methodElement = new JsonObject();
            methodElement.add(method, jsonOperationElement);

            jsonPathsElements.add(urlPattern, methodElement);
        }

        apiJsonDefinition.add("paths", jsonPathsElements);

        return apiJsonDefinition.toString();
    }

    public void publishTheAPI(String currentState, String targetState, String apiUUID,
                              APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) throws RegistryException {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        String changeAPIStatusEp = apiEnvironmentConfig.getPublishingEndpoint() + "change-lifecycle";

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        String action = "";

        switch(targetState) {
            case "Created":
                action = Constants.DEMOTE_TO_CREATED;
                break;
            case "Prototyped":
                if(currentState.equals("Published")) {
                    action = Constants.DEMOTE_TO_PROTOTYPED;
                } else {
                    action = Constants.DEPLOY_AS_A_PROTOTYPE;
                }
                break;
            case "Published":
                if(currentState.equals("Blocked")) {
                    action = Constants.REPUBLISH;
                } else {
                    action = Constants.PUBLISH;
                }
                break;
            case "Blocked":
                action = Constants.BLOCK;
                break;
            case "Deprecated":
                action = Constants.DEPRECATE;
                break;
            case "Retired":
                action = Constants.RETIRE;
                break;
        }

        changeAPIStatusEp = changeAPIStatusEp + "?apiId=" + apiUUID + "&action=" + action;
        HttpResponse apiPublishResponse;

        try {
            apiPublishResponse = HttpRequestUtil.sendRequest(new URL(changeAPIStatusEp), "", requestHeaders, Constants.HTTP_METHOD_POST);
            int responseCode = apiPublishResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code of publishing the API = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                apiPublishResponse = HttpRequestUtil.sendRequest(new URL(changeAPIStatusEp), "", requestHeaders, Constants.HTTP_METHOD_POST);

                responseCode = apiPublishResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided");
                }
            } else if(responseCode == 404) {
                throw new RegistryException("Publishing API doesn't exist in the API Manager.");
            }

            if(log.isDebugEnabled()) {
                log.debug("Response code of publishing the API = " + responseCode);
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred while publishing the API  Check your endpoint address and whether the API has any resources", e);
            throw new RegistryException(e.getMessage());
        }
    }

    public void deleteAPI(String apiUUID, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        String deleteAPIEp = apiEnvironmentConfig.getPublishingEndpoint();
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        deleteAPIEp = deleteAPIEp + apiUUID;

        HttpResponse apiDeleteResponse;

        try {
            apiDeleteResponse = HttpRequestUtil.sendRequest(new URL(deleteAPIEp), "", requestHeaders, Constants.HTTP_METHOD_DELETE);
            int responseCode = apiDeleteResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code of deleting the API = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                apiDeleteResponse = HttpRequestUtil.sendRequest(new URL(deleteAPIEp), "", requestHeaders, Constants.HTTP_METHOD_DELETE);

                responseCode = apiDeleteResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided");
                }
            } else if(responseCode == 404) {
                throw new RegistryException("Deleting API doesn't exist in the API Manager.");
            }

            if(log.isDebugEnabled()) {
                log.debug("Response code of API deletion = " + responseCode);
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred in the send request while changing the lc state", e);
        }
    }

    public String copyAPI(String apiUUID, String version, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        String copyAPIEp =apiEnvironmentConfig.getPublishingEndpoint();
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        copyAPIEp = copyAPIEp + "copy-api?apiId=" + apiUUID + "&newVersion=" + version;

        HttpResponse apiCopyResponse;
        String apiId = null;

        try {
            int responseCode;
            apiCopyResponse = HttpRequestUtil.sendRequest(new URL(copyAPIEp), "", requestHeaders, Constants.HTTP_METHOD_POST);
            responseCode = apiCopyResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code of copying API = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                apiCopyResponse = HttpRequestUtil.sendRequest(new URL(copyAPIEp), "", requestHeaders, Constants.HTTP_METHOD_POST);

                responseCode = apiCopyResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided");
                }
            } else if(responseCode == 404) {
                throw new RegistryException("Copying API doesn't exist in the API Manager.");
            }

            Map copyResponseMap = new Gson().fromJson(apiCopyResponse.getData(), Map.class);
            apiId = (String) copyResponseMap.get("id");

            if(log.isDebugEnabled()) {
                log.debug("API Copying is successful");
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred in the send request while copying the API", e);
        }

        return apiId;
    }

    public String addNewDocument(APIArtifact.Document document, String apiId, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        JsonObject documentAddRequestBody = buildDocumentCreationPayload(document);

        Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        if(log.isDebugEnabled()) {
            log.debug("Add new document payload " + gson.toJson(documentAddRequestBody));
        }
        String documentAddRequestBodyString = gson.toJson(documentAddRequestBody);

        String documentCreationEp = apiEnvironmentConfig.getPublishingEndpoint();
        documentCreationEp = documentCreationEp + apiId + "/documents" ;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        HttpResponse documentCreationResponse;
        int responseCode;

        String documentId = null;
        try {
            documentCreationResponse = HttpRequestUtil.sendRequest(new URL(documentCreationEp), documentAddRequestBodyString, requestHeaders, Constants.HTTP_METHOD_POST);

            responseCode = documentCreationResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code of document creation = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                documentCreationResponse = HttpRequestUtil.sendRequest(new URL(documentCreationEp), documentAddRequestBodyString, requestHeaders, Constants.HTTP_METHOD_POST);

                responseCode = documentCreationResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided");
                }
            }

            Map clientRegistrationResponseMap = new Gson().fromJson(documentCreationResponse.getData(), Map.class);
            documentId = (String) clientRegistrationResponseMap.get("documentId");

            if(log.isDebugEnabled()) {
                log.debug("Document Creation successful, id = " + documentId);
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred in the send request while adding the document", e);
        }

        return documentId;
    }

    public String updateDocument(APIArtifact.Document document, String apiId, String documentId, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        JsonObject documentAddRequestBody = buildDocumentCreationPayload(document);

        Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        documentAddRequestBody.addProperty("documentId", documentId);

        if(log.isDebugEnabled()) {
            log.debug("Update document payload = " + gson.toJson(documentAddRequestBody));
        }
        String documentAddRequestBodyString = gson.toJson(documentAddRequestBody);

        String documentCreationEp = apiEnvironmentConfig.getPublishingEndpoint();
        documentCreationEp = documentCreationEp + apiId + "/documents/" + documentId;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        HttpResponse documentUpdateResponse;
        int responseCode;

        String updatedDocumentId = null;
        try {
            documentUpdateResponse = HttpRequestUtil.sendRequest(new URL(documentCreationEp), documentAddRequestBodyString, requestHeaders, Constants.HTTP_METHOD_PUT);

            responseCode = documentUpdateResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code of update document = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                documentUpdateResponse = HttpRequestUtil.sendRequest(new URL(documentCreationEp), documentAddRequestBodyString, requestHeaders, Constants.HTTP_METHOD_PUT);

                responseCode = documentUpdateResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided");
                }
            } else if(responseCode == 404) {
                throw new RegistryException("Updating document doesn't exist in the API Manager");
            }

            Map clientRegistrationResponseMap = new Gson().fromJson(documentUpdateResponse.getData(), Map.class);
            updatedDocumentId = (String) clientRegistrationResponseMap.get("id");

            if(log.isDebugEnabled()) {
                log.debug("Doc Update is successful in the environment");
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred in the send request while adding the document", e);
        }

        return updatedDocumentId;
    }

    public void deleteDocument(String apiUUID, String documentUUID, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) {
        APIMClientRegistration clientRegistration = APIMClientRegistration.getInstance();

        String deleteDocumentEp = apiEnvironmentConfig.getPublishingEndpoint();
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
        requestHeaders.put("Content-Type", "application/json");

        deleteDocumentEp = deleteDocumentEp + apiUUID + "/documents/" + documentUUID;

        HttpResponse apiDeleteResponse;

        try {
            apiDeleteResponse = HttpRequestUtil.sendRequest(new URL(deleteDocumentEp), "", requestHeaders, Constants.HTTP_METHOD_DELETE);

            int responseCode = apiDeleteResponse.getResponseCode();

            if(log.isDebugEnabled()) {
                log.debug("Response code of delete document = " + responseCode);
            }

            // Unauthorized or token expired. Let's
            if(responseCode == 401) {
                clientRegistration.generateAccessToken(apiEnvironmentConfig);
                requestHeaders.put("Authorization", "Bearer " + clientRegistration.getUserAccessToken());
                apiDeleteResponse = HttpRequestUtil.sendRequest(new URL(deleteDocumentEp), "", requestHeaders, Constants.HTTP_METHOD_DELETE);

                responseCode = apiDeleteResponse.getResponseCode();
                if(responseCode == 401) {
                    log.warn("Looks like your credentials are invalid. Please check your credentials before trying again");
                    throw new RegistryException("Invalid credential provided");
                }
            } else if(responseCode == 404) {
                throw new RegistryException("Deleting document doesn't exist in the API Manager");
            }
        } catch(MalformedURLException | RegistryException e) {
            log.error("An error occurred in the send request while calling the DELETE document", e);
        }
    }

    private JsonObject buildDocumentCreationPayload(APIArtifact.Document document) {
        JsonObject documentObj = new JsonObject();

        documentObj.addProperty(Constants.API_DOCUMENT_VISIBILITY, "API_LEVEL");
        documentObj.addProperty(Constants.API_DOCUMENT_SOURCE_TYPE, "URL");
        documentObj.addProperty(Constants.API_DOCUMENT_SOURCE_URL, document.getDocumentURL());
        documentObj.add(Constants.API_DOCUMENT_OTHER_TYPE_NAME, null);
        documentObj.addProperty(Constants.API_DOCUMENT_SUMMARY, document.getDocumentSummary());
        documentObj.addProperty(Constants.API_DOCUMENT_NAME, document.getDocumentName());
        documentObj.addProperty(Constants.API_DOCUMENT_TYPE, document.getDocumentType());

        return documentObj;
    }
}
