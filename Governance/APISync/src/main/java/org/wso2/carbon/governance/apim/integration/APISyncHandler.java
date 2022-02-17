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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.CarbonConfigurationContextFactory;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.apim.integration.utils.Constants;
import org.wso2.carbon.registry.api.Tag;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.resource.services.utils.AddRolePermissionUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLStreamException;
import java.util.*;

@SuppressWarnings("unused")
public class APISyncHandler extends Handler {
    private static final Log log = LogFactory.getLog(APISyncHandler.class);

    @Override
    public void put(RequestContext requestContext) throws RegistryException {
        try {
            if (!CommonUtil.isUpdateLockAvailable()) {
                return;
            }

            CommonUtil.acquireUpdateLock();

            if(!APIMConfigurations.getInstance().isEnableAPIMSync()) {
                return;
            }

            Resource apiResource = requestContext.getResource();
            if (apiResource == null) {
                throw new RegistryException("The resource is not available");            }

            String apiUUID = apiResource.getUUID();

            Registry userRegistry = RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(CarbonContext.getThreadLocalCarbonContext().getUsername(),
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());

            GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, Constants.API_KEY);
            GenericArtifact apiGenericArtifact = artifactManager.getGenericArtifact(apiUUID);

            Object contentObj = apiResource.getContent();

            String content;
            if(contentObj instanceof String) {
                content = (String) contentObj;
            } else {
                byte[] contentBytes = (byte[]) contentObj;
                content = new String(contentBytes);
            }

            GenericArtifact apiNewGenericArtifact;
            try {
                apiNewGenericArtifact = artifactManager.newGovernanceArtifact(AXIOMUtil.stringToOM(content));
            } catch (XMLStreamException e) {
                throw new RegistryException("An error occurred while creating the temporary artifact from the resource content ", e);
            }

            APIMRestClient apimRestClient = new APIMRestClient();
            // Only the first is taken. When multiple environment is supported this has to get looped
            APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig = APIMConfigurations.getInstance().getApiEnvironmentConfigList().get(0);

            if(apiGenericArtifact != null) {
                String associatedAPIMUUID = apiResource.getProperty(Constants.API_ID_PROPERTY);
                APIArtifact apiArtifact = apimRestClient.createAPIArtifact(apiNewGenericArtifact);

                setTags(userRegistry, requestContext, apiArtifact);

                setThumbnailURL(apiNewGenericArtifact, apiUUID, userRegistry, apiArtifact);

                if (associatedAPIMUUID == null) {
                    String apiId = apimRestClient.createAPIInAPIM(apiArtifact, apiEnvironmentConfig);

                    if(apiId != null) {
                        apiResource.addProperty(Constants.API_ID_PROPERTY, apiId);
                        apiResource.addProperty(Constants.ADD_IN_PROGRESS_PROPERTY, Constants.TRUE);

                        if(log.isDebugEnabled()) {
                            log.debug("Created API uuid = " + apiId);
                        }
                        handleDocuments(apiArtifact.getDocuments(), apiResource, apimRestClient, apiId, apiEnvironmentConfig);
                    }
                } else {
                    if(apiResource.getProperty(Constants.ADD_IN_PROGRESS_PROPERTY) == null) {
                        if(log.isDebugEnabled()) {
                            log.debug("Updating the api of UUID = " + associatedAPIMUUID);
                        }
                        apimRestClient.updateAPIInAPIM(apiArtifact, associatedAPIMUUID, apiEnvironmentConfig);

                        handleDocuments(apiArtifact.getDocuments(), apiResource, apimRestClient, associatedAPIMUUID, apiEnvironmentConfig);
                    } else {
                        if(log.isDebugEnabled()) {
                            log.debug("Removing the property in progress");
                        }
                        apiResource.removeProperty(Constants.ADD_IN_PROGRESS_PROPERTY);
                    }
                }
            } else {
                if(apiNewGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_UNLIMITED).equals(Constants.FALSE) &&
                        apiNewGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_BRONZE).equals(Constants.FALSE) &&
                        apiNewGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_SILVER).equals(Constants.FALSE) &&
                        apiNewGenericArtifact.getAttribute(Constants.TIER_AVAILABILITY_GOLD).equals(Constants.FALSE)) {
                    throw new RegistryException("At least one throttling tier needs to be selected");
                }

                String context = apiNewGenericArtifact.getAttribute(Constants.OVERVIEW_CONTEXT);
                String contextAlternative;
                if(context.startsWith("/")) {
                    contextAlternative = context.substring(1);
                    context = context.replace("/", "\\/");
                } else {
                    contextAlternative = "\\/" + context;
                }

                GenericArtifact[] artifactsWithSameContext = artifactManager.findGovernanceArtifacts("overview:context=" + context);
                GenericArtifact[] artifactsWithSameAlternateContext = artifactManager.findGovernanceArtifacts("overview:context=" + contextAlternative);

                if(artifactsWithSameContext.length > 0 || artifactsWithSameAlternateContext.length > 0) {
                    if(log.isDebugEnabled()) {
                        log.debug("Seems like there are APIs with same contexts. Checking whether it's of the same name to determine if it's a copy");
                    }
                    String name = apiNewGenericArtifact.getAttribute(Constants.OVERVIEW_NAME);
                    GenericArtifact[] artifactsWithSameContextSameName = artifactManager.findGovernanceArtifacts("overview:context=" + context + "&overview:name=" + name);
                    GenericArtifact[] artifactsWithSameAlternateContextSameName = artifactManager.findGovernanceArtifacts("overview:context=" + contextAlternative + "&overview:name=" + name);

                    if(artifactsWithSameContextSameName.length > 0 || artifactsWithSameAlternateContextSameName.length > 0) {
                        if(log.isDebugEnabled()) {
                            log.debug("Looks like there's an API with same name and same context. Therefore copying it in the API Manager");
                        }
                        String aCopyingApiId = null;
                        if(artifactsWithSameContextSameName.length > 0) {
                            if(userRegistry.resourceExists(artifactsWithSameContextSameName[0].getPath())) {
                                aCopyingApiId = userRegistry.get(artifactsWithSameContextSameName[0].getPath()).getProperty(Constants.API_ID_PROPERTY);
                            }
                        } else {
                            if(userRegistry.resourceExists(artifactsWithSameAlternateContextSameName[0].getPath())) {
                                aCopyingApiId = userRegistry.get(artifactsWithSameAlternateContextSameName[0].getPath()).getProperty(Constants.API_ID_PROPERTY);
                            }
                        }

                        if(aCopyingApiId != null) {
                            String newCopyApiUUID = apimRestClient.copyAPI(aCopyingApiId, apiNewGenericArtifact.getAttribute(Constants.OVERVIEW_VERSION), apiEnvironmentConfig);
                            apiResource.addProperty(Constants.API_ID_PROPERTY, newCopyApiUUID);

                            if(log.isDebugEnabled()) {
                                log.debug("Copied API uuid = " + newCopyApiUUID);
                            }

                            apiResource.addProperty(Constants.ADD_IN_PROGRESS_PROPERTY, Constants.TRUE);
                        } else {
                            if(log.isDebugEnabled()) {
                                log.debug("A copying API id could not be found. Nevertheless trying to create the API without copying");
                            }
                        }
                    } else {
                        throw new RegistryException("Can't add API with same context " + context);
                    }
                }
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        if(!APIMConfigurations.getInstance().isEnableAPIMSync()) {
            return;
        }

        try {
            if (!CommonUtil.isUpdateLockAvailable()) {
                return;
            }

            CommonUtil.acquireUpdateLock();

            // Only the first is taken. When multiple environment is supported this has to get looped
            APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig = APIMConfigurations.getInstance().getApiEnvironmentConfigList().get(0);

            Resource apiResource = requestContext.getResource();
            if (apiResource == null) {
                throw new RegistryException("The resource is not available");
            }

            String associatedAPIMUUID = apiResource.getProperty(Constants.API_ID_PROPERTY);

            if(associatedAPIMUUID != null) {
                APIMRestClient apimRestClient = new APIMRestClient();
                apimRestClient.deleteAPI(associatedAPIMUUID, apiEnvironmentConfig);
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void setTags(Registry userRegistry, RequestContext requestContext, APIArtifact apiArtifact) throws RegistryException {
        String resourcePath = requestContext.getResourcePath().getPath();
        if(resourcePath.startsWith(Constants.GOVERNANCE_PATH)) {
            resourcePath = resourcePath.substring(Constants.GOVERNANCE_PATH.length());
        }
        Tag[] tags = userRegistry.getTags(resourcePath);
        List<String> listTags = new ArrayList<>();
        if(tags != null && tags.length > 0) {
            for(Tag tag : tags) {
                listTags.add(tag.getTagName());
            }
        }
        apiArtifact.setApiTags(listTags);
    }

    private void handleDocuments(List<APIArtifact.Document> documents, Resource apiResource,
                                 APIMRestClient apimRestClient, String associatedAPIMUUID, APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig) {
        if(documents != null && documents.size() >= 0) {
            Set<APIArtifact.Document> uniqueDocs = new HashSet<>(documents);

            ArrayList<String> docNames = new ArrayList<>();

            for(APIArtifact.Document document : uniqueDocs) {
                String docName = document.getDocumentName();
                docNames.add(docName);
                if(apiResource.getProperty(Constants.DOCUMENT_ID_PREFIX + docName) != null) {
                    if(log.isDebugEnabled()) {
                        log.debug("property exist for the doc of name " + docName + ", therefore updating the doc");
                    }
                    apimRestClient.updateDocument(document, associatedAPIMUUID, apiResource.getProperty(Constants.DOCUMENT_ID_PREFIX + docName), apiEnvironmentConfig);
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("property doesn't exist for the doc of name " + docName + ", therefore adding the doc");
                    }
                    String documentId = apimRestClient.addNewDocument(document, associatedAPIMUUID, apiEnvironmentConfig);

                    // When environment specific doc needs to be added, need a qualification for the environment
                    apiResource.addProperty(Constants.DOCUMENT_ID_PREFIX + docName, documentId);
                }
            }

            Properties properties = apiResource.getProperties();
            Enumeration e = properties.propertyNames();

            // Delete deleted docs
            while(e.hasMoreElements()) {
                String key = (String) e.nextElement();
                if(log.isDebugEnabled()) {
                    log.debug("Checking whether to Delete the doc with key = " + key);
                }

                if(key.startsWith(Constants.DOCUMENT_ID_PREFIX) && !docNames.contains(key.substring(Constants.DOCUMENT_ID_PREFIX.length()))) {
                    if(log.isDebugEnabled()) {
                        log.debug("Deleting the doc with the key = " + key.substring(Constants.DOCUMENT_ID_PREFIX.length()));
                    }
                    apimRestClient.deleteDocument(associatedAPIMUUID, apiResource.getProperty(key), apiEnvironmentConfig);
                    apiResource.removeProperty(key);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setThumbnailURL(GenericArtifact apiNewGenericArtifact, String apiUUID, Registry userRegistry, APIArtifact apiArtifact) throws RegistryException {
        if(apiNewGenericArtifact.getAttribute(Constants.MANAGE_THUMBNAIL) != null) {
            String thumbnailPath = "/store/asset_resources/" + Constants.API_KEY + "/" + apiUUID + "/manage_thumbnail";

            if(userRegistry.resourceExists(thumbnailPath)) {
                if(log.isDebugEnabled()) {
                    log.debug("Thumbnail Resource exist in " + thumbnailPath);
                }

                String host = null;
                try {
                    host = NetworkUtils.getLocalHostname();
                } catch (Exception e) {
                    log.error("An error occurred while constructing the permalink for the given path: " + thumbnailPath, e);
                }

                String thumbnailFullPath = Constants.GOVERNANCE_PATH + thumbnailPath;

                UserRegistry systemRegistry = RegistryCoreServiceComponent.getRegistryService().getSystemRegistry();
                try {
                    AddRolePermissionUtil.addRolePermission(systemRegistry, thumbnailFullPath, Constants.SYSTEM_ANONYMOUS_ROLE, "2", "1");
                } catch (Exception e) {
                    log.error("An error occurred while setting anonymous permission to API thumbnail resource");
                }

                String webContext = ServerConfiguration.getInstance().getFirstProperty(Constants.WEB_CONTEXT_ROOT_PROPERTY);
                if (webContext == null || webContext.equals("/")) {
                    webContext = "";
                }

                int port = -1;
                if(APIMConfigurations.getInstance().isPublishingRegistryLoadBalanced()) {
                    port = Integer.parseInt(APIMConfigurations.getInstance().getLoadBalancerPort());
                } else {
                    port = CarbonUtils.getTransportProxyPort(CarbonConfigurationContextFactory.getConfigurationContext(), Constants.HTTPS_ENDPOINT);
                    if (port == -1) {
                        port = CarbonUtils.getTransportPort(CarbonConfigurationContextFactory.getConfigurationContext(), Constants.HTTPS_ENDPOINT);
                    }
                }

                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

                String permanentLink = Constants.HTTPS_ENDPOINT + "://" + host + ":" + port + webContext +
                        ((tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
                                "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain :
                                "") + "/registry/resource" + org.wso2.carbon.registry.app.Utils.encodeRegistryPath(Constants.GOVERNANCE_PATH + thumbnailPath);
                apiArtifact.setPermanentLink(permanentLink);
            } else {
                log.warn("Thumbnail Resource does not exist in " + thumbnailPath);
            }
        }
    }
}
