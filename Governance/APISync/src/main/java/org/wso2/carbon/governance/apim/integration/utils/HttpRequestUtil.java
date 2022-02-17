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

import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpRequestUtil {
    public static HttpResponse sendRequest(URL endpoint, String postBody, Map<String, String> headers, String method)
            throws RegistryException {
        try {
            URLConnection connection = endpoint.openConnection();

            if(connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                return sendHttpsRequest((HttpsURLConnection) connection, postBody, headers, method);
            } else {
                return sendHttpRequest((HttpURLConnection) connection, postBody, headers, method);
            }
        } catch (IOException e) {
            throw new RegistryException("An error occurred while invoking " + endpoint + e.getMessage(), e);
        }
    }

    private static HttpResponse sendHttpRequest(HttpURLConnection urlConnection, String postBody,
                                                 Map<String, String> headers, String method) throws RegistryException {
        int responseCode = 0;

        try {
            try {
                urlConnection.setRequestMethod(method);
            } catch (ProtocolException e) {
                throw new RegistryException("Http URL doesn't support POST", e);
            }

            for (Map.Entry<String, String> e : headers.entrySet()) {
                urlConnection.setRequestProperty(e.getKey(), e.getValue());
            }

            urlConnection.setDoOutput(true);

            if(Constants.HTTP_METHOD_DELETE.equals(method)) {
                responseCode = urlConnection.getResponseCode();
                return new HttpResponse("", responseCode, null);
            }

            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setAllowUserInteraction(false);

            OutputStream out = urlConnection.getOutputStream();

            try {
                Writer writer = new OutputStreamWriter(out, "UTF-8");
                writer.write(postBody);
                writer.close();
            } catch (IOException e) {
                throw new RegistryException("IOException while posting data " + e.getMessage(), e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader rd = null;
            responseCode = urlConnection.getResponseCode();

            try {
                rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.defaultCharset()));
                String line;

                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } catch (FileNotFoundException ignored) {
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }

            Iterator<String> itr = urlConnection.getHeaderFields().keySet().iterator();
            Map<String, String> responseHeaders = new HashMap<>();

            while (itr.hasNext()) {
                String key = itr.next();
                if (key != null) {
                    responseHeaders.put(key, urlConnection.getHeaderField(key));
                }
            }
            return new HttpResponse(sb.toString(), urlConnection.getResponseCode(), responseHeaders);
        } catch (IOException e) {
            if(responseCode == 401) {
                return new HttpResponse("", responseCode, null);
            }
            throw new RegistryException("An error occurred while invoking " + e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static HttpResponse sendHttpsRequest(HttpsURLConnection urlConnection, String postBody,
                                                 Map<String, String> headers, String method) throws RegistryException {
        int responseCode = 0;

        try {
            try {
                urlConnection.setRequestMethod(method);
            } catch (ProtocolException e) {
                throw new RegistryException("Http URL doesn't support POST", e);
            }

            for (Map.Entry<String, String> e : headers.entrySet()) {
                urlConnection.setRequestProperty(e.getKey(), e.getValue());
            }

            urlConnection.setDoOutput(true);

            if(Constants.HTTP_METHOD_DELETE.equals(method)) {
                responseCode = urlConnection.getResponseCode();
                return new HttpResponse("", responseCode, null);
            }

            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setAllowUserInteraction(false);


            OutputStream out = urlConnection.getOutputStream();

            try {
                Writer writer = new OutputStreamWriter(out, "UTF-8");
                writer.write(postBody);
                writer.close();
            } catch (IOException e) {
                throw new RegistryException("IOException while posting data " + e.getMessage(), e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader rd = null;
            responseCode = urlConnection.getResponseCode();

            try {
                rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.defaultCharset()));
                String line;

                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } catch (FileNotFoundException ignored) {
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }

            Iterator<String> itr = urlConnection.getHeaderFields().keySet().iterator();
            Map<String, String> responseHeaders = new HashMap<>();

            while (itr.hasNext()) {
                String key = itr.next();
                if (key != null) {
                    responseHeaders.put(key, urlConnection.getHeaderField(key));
                }
            }
            return new HttpResponse(sb.toString(), urlConnection.getResponseCode(), responseHeaders);
        } catch (IOException e) {
            if(responseCode == 401) {
                return new HttpResponse("", responseCode, null);
            }
            throw new RegistryException("An error occurred while invoking " + e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
