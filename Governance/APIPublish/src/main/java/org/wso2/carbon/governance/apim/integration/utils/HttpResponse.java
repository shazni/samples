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

import java.util.Map;

public class HttpResponse {
    private String data;
    private int responseCode;
    private String responseMessage;
    private Map<String, String> headers;

    public HttpResponse(String data, int responseCode, Map<String, String> headers) {
        this.data = data;
        this.responseCode = responseCode;
        this.headers = headers;
    }

    public String getData() {
        return data;
    }

    @SuppressWarnings("unused")
    public void setData(String data) {
        this.data = data;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @SuppressWarnings("unused")
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @SuppressWarnings("unused")
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getHeaders() {
        return headers;
    }

    @SuppressWarnings("unused")
    public String getResponseMessage() {
        return responseMessage;
    }

    @SuppressWarnings("unused")
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
