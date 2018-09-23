package org.cep.test.service;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;

import org.wso2.msf4j.interceptor.RequestInterceptor;

import java.nio.charset.StandardCharsets;

public class RequestLoggerInterceptor implements RequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggerInterceptor.class);

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        log.info("Logging HTTP request { HTTPMethod: {}, URI: {} }", request.getHttpMethod(), request.getUri());
        return true;
    }
}
