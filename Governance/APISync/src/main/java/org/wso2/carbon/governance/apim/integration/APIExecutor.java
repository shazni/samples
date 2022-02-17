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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.apim.integration.utils.Constants;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

@SuppressWarnings("unused")
public class APIExecutor implements Execution {
    private static final Log log = LogFactory.getLog(APIExecutor.class);

    @Override
    public void init(Map map) {
    }

    @Override
    public boolean execute(RequestContext requestContext, String currentState, String targetState) {
        if(APIMConfigurations.getInstance().isEnableAPIMSync()) {
            Resource apiResource = requestContext.getResource();

            String associatedAPIMUUID = apiResource.getProperty(Constants.API_ID_PROPERTY);

            APIMRestClient apimRestClient = new APIMRestClient();
            // Only the first is taken. When multiple environment is supported this has to get looped
            APIMConfigurations.APIMEnvironmentConfig apiEnvironmentConfig = APIMConfigurations.getInstance().getApiEnvironmentConfigList().get(0);

            if (associatedAPIMUUID != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Publishing the api of UUID = " + associatedAPIMUUID);
                }
                apiResource.addProperty(Constants.ADD_IN_PROGRESS_PROPERTY, Constants.TRUE);
                try {
                    apimRestClient.publishTheAPI(currentState, targetState, associatedAPIMUUID, apiEnvironmentConfig);
                } catch (RegistryException e) {
                    return false;
                }
            } else {
                // Due to some exceptional cases, API exists in the G-Reg but not have created in API-M
                // Trying to create the API at this stage

                Registry userRegistry;

                try {
                    userRegistry = RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(CarbonContext.getThreadLocalCarbonContext().getUsername(),
                            CarbonContext.getThreadLocalCarbonContext().getTenantId());
                    GenericArtifactManager artifactManager = new GenericArtifactManager(userRegistry, Constants.API_KEY);
                    GenericArtifact apiGenericArtifact = artifactManager.getGenericArtifact(apiResource.getId());

                    APIArtifact apiArtifact = apimRestClient.createAPIArtifact(apiGenericArtifact);
                    String apiId = apimRestClient.createAPIInAPIM(apiArtifact, apiEnvironmentConfig);

                    apiResource.addProperty(Constants.API_ID_PROPERTY, apiId);

                    apimRestClient.publishTheAPI(currentState, targetState, apiId, apiEnvironmentConfig);
                } catch (RegistryException e) {
                    log.error("An error occurred in the APIExecutor ", e);
                    return false;
                }
            }
        }

        return true;
    }
}
