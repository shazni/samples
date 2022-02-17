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

package org.wso2.carbon.governance.apim.integration.utils;

public final class Constants {
    public static final String OVERVIEW_NAME = "overview_name";
    public static final String OVERVIEW_VERSION = "overview_version";
    public static final String OVERVIEW_DESCRIPTION = "overview_description";
    public static final String OVERVIEW_PROVIDER = "overview_provider";
    public static final String OVERVIEW_CONTEXT = "overview_context";

    public static final String IMPLEMENT_ENDPOINT_TYPE = "implement_endpointType";
    public static final String IMPLEMENT_ENDPOINT_URL = "implement_endpointURL";
    public static final String IMPLEMENT_SANDBOX_URL = "implement_sandboxURL";
    public static final String IMPLEMENT_ENABLE_MESSAGE_MEDIATION_FLOW = "implement_enableMessageMediationFlow";
    public static final String IMPLEMENT_IN_FLOW = "implement_inFlow";
    public static final String IMPLEMENT_OUT_FLOW = "implement_outFlow";
    public static final String IMPLEMENT_FAULT_FLOW = "implement_faultFlow";

    public static final String MANAGE_MAKE_THIS_DEFAULT_VERSION = "manage_makeThisTheDefaultVersion";
    public static final String MANAGE_ENABLE_HARD_THROTTLING_LIMIT = "manage_enableHardThrottlingLimits";
    public static final String MANAGE_RESPONSE_CACHING = "manage_responseCaching";
    public static final String MANAGE_VISIBILITY = "manage_visibility";
    public static final String MANAGE_THUMBNAIL = "manage_thumbnail";
    public static final String MANAGE_VISIBLE_ROLES = "manage_visibleRoles";
    public static final String MANAGE_VISIBLE_TENANTS = "manage_visibleTenants";
    public static final String MANAGE_IS_LATEST = "manage_isLatest";
    public static final String MANAGE_ENABLE_HTTP = "manage_enableHTTP";

    @SuppressWarnings("unused")
    public static final String MANAGE_ENABLE_HTTPS = "manage_enableHTTPS";

    public static final String TIER_AVAILABILITY_UNLIMITED = "tierAvailability_unlimited";
    public static final String TIER_AVAILABILITY_GOLD = "tierAvailability_gold";
    public static final String TIER_AVAILABILITY_SILVER = "tierAvailability_silver";
    public static final String TIER_AVAILABILITY_BRONZE = "tierAvailability_bronze";

    public static final String BUSINESS_INFO_TECHNICAL_OWNER = "businessInformation_technicalOwner";
    public static final String BUSINESS_INFO_TECHNICAL_OWNER_EMAIL = "businessInformation_technicalOwnerEmail";
    public static final String BUSINESS_INFO_BUSINESS_OWNER = "businessInformation_businessOwner";
    public static final String BUSINESS_INFO_BUSINESS_OWNER_EMAIL = "businessInformation_businessOwnerEmail";

    public static final String API_DEFINITION_METHOD = "apiDefinition_Method";
    public static final String API_DEFINITION_URL_PATTERN = "apiDefinition_URL";
    public static final String API_DEFINITION_AUTH_TYPE = "apiDefinition_authType";
    public static final String API_DEFINITION_THROTTLING = "apiDefinition_Throttling";
    public static final String API_DEFINITION_SCOPE_NAME = "apiDefinition_scopeName";

    public static final String DOCUMENT_NAME = "documents_Name";
    public static final String DOCUMENT_SUMMARY = "documents_Summary";
    public static final String DOCUMENT_TYPE = "documents_Type";
    public static final String DOCUMENT_URL = "documents_sourceURL";

    public static final String SCOPES_SCOPE_KEY = "scopes_scopeKey";
    public static final String SCOPES_SCOPE_NAME = "scopes_scopeName";
    public static final String SCOPES_ROLES = "scopes_Roles";
    public static final String SCOPES_DESCRIPTION = "scopes_Description";

    public static final String API_KEY = "api";

    public static final String HTTP_ENDPOINT_KEYWORD = "HTTP Endpoint";
    public static final String ADDRESS_ENDPOINT_KEYWORD = "Address Endpoint";
    public static final String FAILOVER_ENDPOINT_KEYWORD = "Failover Endpoint";
    public static final String LOAD_BALANCED_ENDPOINT_KEYWORD = "Loadbalanced Endpoint";
    public static final String DEFAULT_ENDPOINT_KEYWORD = "Default Endpoint";

    public static final String HTTP_ENDPOINT = "http";
    public static final String HTTPS_ENDPOINT = "https";
    public static final String ADDRESS_ENDPOINT = "address";
    public static final String FAILOVER_ENDPOINT = "failover";
    public static final String LOAD_BALANCED_ENDPOINT = "loadbalanced";
    public static final String DEFAULT_ENDPOINT = "default";

    public static final String SWAGGER_VERSION = "2.0";

    public static final String SECRET_KEY_LOCATION = "/trunk/apis/secretKey";

    public static final int CACHE_TIMEOUT = 300;

    public static final String PUBLISH = "Publish";
    public static final String DEPLOY_AS_A_PROTOTYPE = "Deploy+as+a+Prototype";
    public static final String DEMOTE_TO_CREATED = "Demote+to+Created";
    public static final String DEMOTE_TO_PROTOTYPED = "Demote+to+Prototyped";
    public static final String BLOCK = "Block";
    public static final String DEPRECATE = "Deprecate";
    public static final String REPUBLISH = "Re-Publish";
    public static final String RETIRE = "Retire";

    public static final String DOC_TYPE_HOW_TO = "HOWTO";
    public static final String DOC_TYPE_SAMPLE_SDK = "SAMPLES";
    public static final String DOC_TYPE_PUBLIC_FORUM = "PUBLIC_FORUM";
    public static final String DOC_TYPE_SUPPORT_FORUM = "SUPPORT_FORUM";

    public static final String API_MANAGER_CONFIG_XML = "api-integration.xml";
    public static final String GOVERNANCE_PATH = "/_system/governance";

    public static final String DOCUMENT_ID_PREFIX = "documentID_";
    public static final String API_ID_PROPERTY = "API_ID";
    public static final String ADD_IN_PROGRESS_PROPERTY = "addInProgress";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String SYSTEM_ANONYMOUS_ROLE = "system/wso2.anonymous.role";
    public static final String WEB_CONTEXT_ROOT_PROPERTY = "WebContextRoot";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";

    public static final String API_DOCUMENT_VISIBILITY = "visibility";
    public static final String API_DOCUMENT_SOURCE_TYPE = "sourceType";
    public static final String API_DOCUMENT_SOURCE_URL = "sourceUrl";
    public static final String API_DOCUMENT_OTHER_TYPE_NAME = "otherTypeName";
    public static final String API_DOCUMENT_SUMMARY = "summary";
    public static final String API_DOCUMENT_NAME = "name";
    public static final String API_DOCUMENT_TYPE = "type";
}
