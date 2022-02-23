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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.operations.Bool;
import org.wso2.carbon.governance.apim.integration.utils.Constants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class APIMConfigurations {
    private static final Log log = LogFactory.getLog(APIMConfigurations.class);

    public static APIMConfigurations getInstance() {
        if (apimConfigurations == null) {
            apimConfigurations = new APIMConfigurations();
        }
        return apimConfigurations;
    }

    public List<APIMEnvironmentConfig> getApiEnvironmentConfigList() {
        return apiEnvironmentConfigList;
    }

    public boolean isEnableAPIMSync() {
        return enableAPIMSync;
    }

    public boolean isCertValidationDisabled() {
        return disableCertValidation;
    }

    private APIMConfigurations() {
        File apiConfigXML = new File(getAPIConfigXML());
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(apiConfigXML);
        } catch (FileNotFoundException e) {
            log.error("The " + Constants.API_MANAGER_CONFIG_XML + " configuration file not found at " + apiConfigXML.getAbsolutePath());
        }

        StAXOMBuilder builder;
        OMElement configElement = null;
        try {
            if(inputStream != null) {
                builder = new StAXOMBuilder(inputStream);
                configElement = builder.getDocumentElement();
            }
        } catch (XMLStreamException e) {
            log.error(e.getMessage());
        }

        if (configElement != null) {
            SecretResolver secretResolver = SecretResolverFactory.create(configElement, false);
            OMElement enableAPISyncElement = configElement.getFirstChildWithName(new QName("enableAPIMSync"));

            if(Constants.TRUE.equals(enableAPISyncElement.getText())) {
                enableAPIMSync = true;
            }

            OMElement disableCertValidationElement = configElement.getFirstChildWithName(new QName("disableCertificateValidation"));

            if(Constants.TRUE.equals(disableCertValidationElement.getText())) {
                disableCertValidation = true;
            }

            OMElement loadBalanceConfigurationElement = configElement.getFirstChildWithName(new QName("loadBalance"));
            setPublishingRegistryLoadBalanced(Boolean.valueOf(loadBalanceConfigurationElement.getAttributeValue(new QName("enabled")).toLowerCase()));
            setLoadBalancerPort(loadBalanceConfigurationElement.getFirstChildWithName(new QName("port")).getText());

            OMElement environmentsConfigurationElement = configElement.getFirstChildWithName(new QName("environments"));
            Iterator environmentConfigs = environmentsConfigurationElement.getChildrenWithName(new QName("environment"));

            while (environmentConfigs.hasNext()) {
                OMElement environmentConfig = (OMElement) environmentConfigs.next();

                APIMEnvironmentConfig apimEnvironmentConfig = new APIMEnvironmentConfig();

                String environmentName = environmentConfig.getAttributeValue(new QName("name"));
                apimEnvironmentConfig.setEnvironmentName(environmentName);

                OMElement publishingEndpointElement = environmentConfig.getFirstChildWithName(new QName("publishingEndpoint"));
                apimEnvironmentConfig.setPublishingEndpoint(publishingEndpointElement.getText());

                OMElement tokenEndpointElement = environmentConfig.getFirstChildWithName(new QName("tokenEndpoint"));
                apimEnvironmentConfig.setTokenEndpoint(tokenEndpointElement.getText());

                apimEnvironmentConfig.setAuthUserName(environmentConfig.getFirstChildWithName(new QName("userName")).getText());
                String secretAlias = environmentConfig.getFirstChildWithName(new QName("secretAlias")).getText();

                OMElement enableOpenAPIImport = environmentConfig.getFirstChildWithName(new QName("enableOpenAPIImport"));
                if(Constants.TRUE.equals(enableOpenAPIImport.getText())) {
                    apimEnvironmentConfig.setOpenAPIImportEnabled(true);
                }

                OMElement enablePublishingInAPIMElement = environmentConfig.getFirstChildWithName(new QName("enablePublishingInAPIM"));
                if(Constants.TRUE.equals(enablePublishingInAPIMElement.getText())) {
                    apimEnvironmentConfig.setEnablePublishingInAPIM(true);
                }

                if(secretResolver != null && secretResolver.isInitialized()) {
                    if(secretResolver.isTokenProtected(secretAlias)) {
                        apimEnvironmentConfig.setAuthPassword(secretResolver.resolve(secretAlias));
                    } else {
                        apimEnvironmentConfig.setAuthPassword(environmentConfig.getFirstChildWithName(new QName("password")).getText());
                    }
                } else {
                    apimEnvironmentConfig.setAuthPassword(environmentConfig.getFirstChildWithName(new QName("password")).getText());
                }

                OMElement clientRegistrationElement = environmentConfig.getFirstChildWithName(new QName("clientRegistration"));

                apimEnvironmentConfig.setClientRegistrationEndpoint(clientRegistrationElement.getFirstChildWithName(new QName("clientRegistrationEndpoint")).getText());
                apimEnvironmentConfig.setCallBackURL(clientRegistrationElement.getFirstChildWithName(new QName("callBackURL")).getText());
                apimEnvironmentConfig.setClientName(clientRegistrationElement.getFirstChildWithName(new QName("clientName")).getText());
                apimEnvironmentConfig.setTokenScope(clientRegistrationElement.getFirstChildWithName(new QName("tokenScope")).getText());
                apimEnvironmentConfig.setClientOwner(clientRegistrationElement.getFirstChildWithName(new QName("owner")).getText());
                apimEnvironmentConfig.setGrantType(clientRegistrationElement.getFirstChildWithName(new QName("grantType")).getText());
                apimEnvironmentConfig.setIsSaasApp(clientRegistrationElement.getFirstChildWithName(new QName("saasApp")).getText());

                apiEnvironmentConfigList.add(apimEnvironmentConfig);
            }
        }
    }

    private static String getAPIConfigXML() {
        String apiConfigXML = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        if (apiConfigXML == null) {
            return CarbonUtils.getCarbonConfigDirPath() + File.separator + Constants.API_MANAGER_CONFIG_XML;
        }
        return apiConfigXML + File.separator + Constants.API_MANAGER_CONFIG_XML;
    }

    private static APIMConfigurations apimConfigurations;

    private boolean enableAPIMSync = false;
    private boolean disableCertValidation = false;
    private List<APIMEnvironmentConfig> apiEnvironmentConfigList = new ArrayList<>();

    private boolean isPublishingRegistryLoadBalanced;
    private String loadBalancerPort;

    public boolean isPublishingRegistryLoadBalanced() {
        return isPublishingRegistryLoadBalanced;
    }

    public void setPublishingRegistryLoadBalanced(boolean isPublishingRegistryLoadBalanced) {
        this.isPublishingRegistryLoadBalanced = isPublishingRegistryLoadBalanced;
    }

    public String getLoadBalancerPort() {
        return loadBalancerPort;
    }

    public void setLoadBalancerPort(String loadBalancerPort) {
        this.loadBalancerPort = loadBalancerPort;
    }

    class APIMEnvironmentConfig {
        @SuppressWarnings("unused")
        public String getEnvironmentName() {
            return environmentName;
        }

        public void setEnvironmentName(String environmentName) {
            this.environmentName = environmentName;
        }

        public String getPublishingEndpoint() {
            return publishingEndpoint;
        }

        public void setPublishingEndpoint(String publishingEndpoint) {
            this.publishingEndpoint = publishingEndpoint;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public void setTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
        }

        public String getAuthUserName() {
            return authUserName;
        }

        public void setAuthUserName(String authUserName) {
            this.authUserName = authUserName;
        }

        public String getAuthPassword() {
            return authPassword;
        }

        public void setAuthPassword(String authPassword) {
            this.authPassword = authPassword;
        }

        public String getClientRegistrationEndpoint() {
            return clientRegistrationEndpoint;
        }

        public void setClientRegistrationEndpoint(String clientRegistrationEndpoint) {
            this.clientRegistrationEndpoint = clientRegistrationEndpoint;
        }

        public String getCallBackURL() {
            return callBackURL;
        }

        public void setCallBackURL(String callBackURL) {
            this.callBackURL = callBackURL;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public String getTokenScope() {
            return tokenScope;
        }

        public void setTokenScope(String tokenScope) {
            this.tokenScope = tokenScope;
        }

        public String getClientOwner() {
            return clientOwner;
        }

        public void setClientOwner(String clientOwner) {
            this.clientOwner = clientOwner;
        }

        public String getGrantType() {
            return grantType;
        }

        public void setGrantType(String grantType) {
            this.grantType = grantType;
        }

        public String getIsSaasApp() {
            return isSaasApp;
        }

        public void setIsSaasApp(String isSaasApp) {
            this.isSaasApp = isSaasApp;
        }

        public Boolean getOpenAPIImportEnabled() {
            return isOpenAPIImportEnabled;
        }

        public void setOpenAPIImportEnabled(Boolean openAPIImportEnabled) {
            isOpenAPIImportEnabled = openAPIImportEnabled;
        }

        public Boolean getEnablePublishingInAPIM() {
            return enablePublishingInAPIM;
        }

        public void setEnablePublishingInAPIM(Boolean enablePublishingInAPIM) {
            this.enablePublishingInAPIM = enablePublishingInAPIM;
        }

        private String environmentName;
        private String publishingEndpoint;
        private String tokenEndpoint;

        private Boolean isOpenAPIImportEnabled;
        private Boolean enablePublishingInAPIM = false;

        private String authUserName;
        private String authPassword;

        // Client Registration related config
        private String clientRegistrationEndpoint;
        private String callBackURL;
        private String clientName;
        private String tokenScope;
        private String clientOwner;
        private String grantType;
        private String isSaasApp;

    }
}
