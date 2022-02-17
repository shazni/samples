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

import org.wso2.carbon.governance.apim.integration.utils.Constants;

import java.util.List;

public class APIArtifact {

    @SuppressWarnings("unused")
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    @SuppressWarnings("unused")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setApiProvider(String apiProvider) {
        this.apiProvider = apiProvider;
    }

    public void setApiDescription(String apiDescription) {
        this.apiDescription = apiDescription;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public void setApiTags(List<String> apiTags) {
        this.apiTags = apiTags;
    }

    public void setApiTransports(List<String> apiTransports) {
        this.apiTransports = apiTransports;
    }

    public void setDefaultVersion(boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public void setProductionEndpoint(String productionEndpoint) {
        if(productionEndpoint != null) {
            this.productionEndpoint = productionEndpoint;
        } else {
            this.productionEndpoint = "";
        }
    }

    public void setSandboxEndpoint(String sandboxEndpoint) {
        if(sandboxEndpoint != null) {
            this.sandboxEndpoint = sandboxEndpoint;
        } else {
            this.sandboxEndpoint = "";
        }
    }

    public void setInMediationFlow(String inMediationFlow) {
        this.inMediationFlow = inMediationFlow;
    }

    public void setOutMediationFlow(String outMediationFlow) {
        this.outMediationFlow = outMediationFlow;
    }

    public void setFaultMediationFlow(String faultMediationFlow) {
        this.faultMediationFlow = faultMediationFlow;
    }

    public String getApiName() {
        return apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getApiProvider() {
        return apiProvider;
    }

    public String getApiDescription() {
        return apiDescription;
    }

    public String getApiContext() {
        return apiContext;
    }

    public List<String> getApiTags() {
        return apiTags;
    }

    public List<String> getApiTransports() {
        return apiTransports;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public String getProductionEndpoint() {
        return productionEndpoint;
    }

    public String getSandboxEndpoint() {
        return sandboxEndpoint;
    }

    public String getInMediationFlow() {
        return inMediationFlow;
    }

    public String getOutMediationFlow() {
        return outMediationFlow;
    }

    public String getFaultMediationFlow() {
        return faultMediationFlow;
    }

    public APIArtifact(String apiName, String apiVersion) {
        this.apiName = apiName;
        this.apiVersion = apiVersion;

        businessInformation = new BusinessInformation();
    }

    @SuppressWarnings("unused")
    public boolean isEnableMessageMediationFlow() {
        return isEnableMessageMediationFlow;
    }

    public void setEnableMessageMediationFlow(boolean isEnableMessageMediationFlow) {
        this.isEnableMessageMediationFlow = isEnableMessageMediationFlow;
    }

    @SuppressWarnings("unused")
    public boolean isEnableHardThrottlingLimit() {
        return isEnableHardThrottlingLimit;
    }

    public void setEnableHardThrottlingLimit(boolean isEnableHardThrottlingLimit) {
        this.isEnableHardThrottlingLimit = isEnableHardThrottlingLimit;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(List<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public List<String> getVisibleTenants() {
        return visibleTenants;
    }

    public void setVisibleTenants(List<String> visibleTenants) {
        this.visibleTenants = visibleTenants;
    }

    @SuppressWarnings("unused")
    public boolean isLatest() {
        return isLatest;
    }

    public void setLatest(boolean isLatest) {
        this.isLatest = isLatest;
    }

    public BusinessInformation getBusinessInformation() {
        return businessInformation;
    }

    public String getResponseCaching() {
        return responseCaching;
    }

    public void setResponseCaching(String responseCaching) {
        this.responseCaching = responseCaching;
    }

    public List<String> getTiers() {
        return tiers;
    }

    public void setTiers(List<String> tiers) {
        this.tiers = tiers;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public Scope getNewScopeInstance() {
        return new Scope();
    }

    public APIDefinition getNewAPIDefinitionInstance() {
        return new APIDefinition();
    }

    public List<APIDefinition> getApiDefinitions() {
        return apiDefinitions;
    }

    public void setApiDefinitions(List<APIDefinition> apiDefinitions) {
        this.apiDefinitions = apiDefinitions;
    }

    public Document getNewDocumentInstance() {
        return new Document();
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public String getPermanentLink() {
        return permanentLink;
    }

    public void setPermanentLink(String permanentLink) {
        this.permanentLink = permanentLink;
    }

    private String apiName;
    private String apiVersion;
    private String apiProvider;
    private String apiDescription;
    private String apiContext;

    private List<String> apiTags;
    private List<String> apiTransports;
    private List<String> tiers;

    private boolean isDefaultVersion;
    private String responseCaching;

    private boolean isEnableMessageMediationFlow;
    private boolean isEnableHardThrottlingLimit;

    private String endpointType;

    private String productionEndpoint;
    private String sandboxEndpoint;

    private String inMediationFlow;
    private String outMediationFlow;
    private String faultMediationFlow;

    private String visibility = "PUBLIC";
    private List<String> visibleRoles;
    private List<String> visibleTenants;

    private List<Scope> scopes;
    private List<APIDefinition> apiDefinitions;
    private List<Document> documents;

    private boolean isLatest;

    private BusinessInformation businessInformation;
    private String permanentLink;


    class BusinessInformation {
        private String businessOwner;
        private String businessOwnerEmail;
        private String technicalOwner;
        private String technicalOwnerEmail;

        public BusinessInformation() {
            businessOwner = "";
            businessOwnerEmail = "";
            technicalOwner = "";
            technicalOwnerEmail = "";
        }

        public String getBusinessOwner() {
            return businessOwner;
        }

        public void setBusinessOwner(String businessOwner) {
            this.businessOwner = businessOwner;
        }

        public String getBusinessOwnerEmail() {
            return businessOwnerEmail;
        }

        public void setBusinessOwnerEmail(String businessOwnerEmail) {
            this.businessOwnerEmail = businessOwnerEmail;
        }

        public String getTechnicalOwner() {
            return technicalOwner;
        }

        public void setTechnicalOwner(String technicalOwner) {
            this.technicalOwner = technicalOwner;
        }

        public String getTechnicalOwnerEmail() {
            return technicalOwnerEmail;
        }

        public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
            this.technicalOwnerEmail = technicalOwnerEmail;
        }
    }

    class APIDefinition {
        public APIDefinition() {}

        public String getApiMethod() {
            return apiMethod;
        }

        public void setApiMethod(String apiMethod) {
            this.apiMethod = apiMethod;
        }

        public String getApiURLPattern() {
            return apiURLPattern;
        }

        public void setApiURLPattern(String apiURLPattern) {
            this.apiURLPattern = apiURLPattern;
        }

        public String getApiAuthType() {
            return apiAuthType;
        }

        public void setApiAuthType(String apiAuthType) {
            if(apiAuthType.equals("Application and Application User")) {
                this.apiAuthType = "Application & Application User";
            } else {
                this.apiAuthType = apiAuthType;
            }
        }

        public String getApiThrottling() {
            return apiThrottling;
        }

        public void setApiThrottling(String apiThrottling) {
            this.apiThrottling = apiThrottling;
        }

        public String getApiScopeName() {
            return apiScopeName;
        }

        public void setApiScopeName(String apiScopeName) {
            this.apiScopeName = apiScopeName;
        }

        private String apiMethod;
        private String apiURLPattern;
        private String apiAuthType;
        private String apiThrottling;
        private String apiScopeName;
    }

    class Scope {
        public Scope() {
        }

        public String getScopeKey() {
            return scopeKey;
        }

        public String getScopeName() {
            return scopeName;
        }

        public String getRoles() {
            return roles;
        }

        public String getScopeDescription() {
            return scopeDescription;
        }

        public void setScopeKey(String scopeKey) {
            this.scopeKey = scopeKey;
        }

        public void setScopeName(String scopeName) {
            this.scopeName = scopeName;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }

        public void setScopeDescription(String scopeDescription) {
            this.scopeDescription = scopeDescription;
        }

        private String scopeKey;
        private String scopeName;
        private String roles;
        private String scopeDescription;
    }

    class Document {
        public Document() {
        }

        public String getDocumentName() {
            return documentName;
        }

        public void setDocumentName(String documentName) {
            this.documentName = documentName;
        }

        public String getDocumentSummary() {
            return documentSummary;
        }

        public void setDocumentSummary(String documentSummary) {
            this.documentSummary = documentSummary;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            switch (documentType) {
                case "How To":
                    this.documentType = Constants.DOC_TYPE_HOW_TO;
                    break;
                case "Sample and SDK":
                    this.documentType = Constants.DOC_TYPE_SAMPLE_SDK;
                    break;
                case "Public Forum":
                    this.documentType = Constants.DOC_TYPE_PUBLIC_FORUM;
                    break;
                case "Support Forum":
                    this.documentType = Constants.DOC_TYPE_SUPPORT_FORUM;
                    break;
            }
        }

        public String getDocumentURL() {
            return documentURL;
        }

        public void setDocumentURL(String documentURL) {
            this.documentURL = documentURL;
        }

        private String documentName;
        private String documentSummary;
        private String documentType;
        private String documentURL;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Document document = (Document) obj;

            return documentName.equals(document.documentName);
        }

        @Override
        public int hashCode() {
            return documentName.hashCode();
        }
    }
}
