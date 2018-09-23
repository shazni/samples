/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cep.test.service;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 0.1-SNAPSHOT
 */
@Path("/ceppublish")
public class CEPTestService {

    private Map<String, StockEvent> stockQuotes = new HashMap<>();

    @GET
    @Path("/")
    public String get() {
        System.out.println("GET invoked");
        return "Hello from WSO2 MSF4J";
    }

    @GET
    @Path("/{symbol}")
    @RequestInterceptor(RequestLoggerInterceptor.class)
    @Produces({"application/json", "text/xml"})
    public Response get(@PathParam("symbol") String symbol) {
        StockEvent stock = stockQuotes.get(symbol);

        for(Map.Entry<String,StockEvent> entry : stockQuotes.entrySet()) {
            System.out.println("Found " + entry.getKey() + " of closing price = " + entry.getValue().getClosePrice());
        }

        return stock == null ?
                Response.status(Response.Status.NOT_FOUND).entity("{\"result\":\"Symbol not found = " + symbol + "\"}").build() :
                Response.status(Response.Status.OK).entity(stock).build();
    }

    @POST
    @Path("/event")
    @Consumes({"plain/text", "application/json", "*/*"})
    public Response post(String eventData) {
        System.out.println("Event Data " + eventData);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String currentDateTime = formatter.format(date);
        String logString = currentDateTime + " - Event date = " + eventData + "\n";
        logToFile(logString);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/event/stock")
    @Consumes("application/json")
    public Response post(StockEvent stockEventData) {
        String symbol = stockEventData.getSymbol();
        stockQuotes.put(symbol, stockEventData);
        return Response.status(Response.Status.OK).
                entity("{\"result\":\"Updated the stock with symbol = " + stockEventData.getSymbol() + "\"}").build();
    }


    @PUT
    @Path("/")
    public void put() {
        System.out.println("PUT invoked");
    }

    @DELETE
    @Path("/")
    public void delete() {
        System.out.println("DELETE invoked");
    }

    private void logToFile(String logString) {
        File file = new File("evendata.log");
        FileWriter writer;
        try {
            writer = new FileWriter(file, true);
            PrintWriter printer = new PrintWriter(writer);
            printer.append(logString);
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
