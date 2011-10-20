/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.samples.stockquote.ws;

import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.ws.samples.stockquote.schema.StockQuote;
import org.springframework.ws.samples.stockquote.schema.StockQuoteRequest;
import org.springframework.ws.samples.stockquote.schema.StockQuoteResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.addressing.server.annotation.Action;
import org.springframework.ws.soap.addressing.server.annotation.Address;

@Endpoint
@Address("http://localhost:8080/StockService")
// optional
public class StockService {

    private DatatypeFactory datatypeFactory;

    public StockService() throws DatatypeConfigurationException {
        datatypeFactory = DatatypeFactory.newInstance();
    }

    @Action(value = "http://www.springframework.org/spring-ws/samples/stockquote/StockService/GetQuote",
            output = "http://www.springframework.org/spring-ws/samples/stockquote/StockService/Quotes")
    public StockQuoteResponse getStockQuotes(StockQuoteRequest request) {
        StockQuoteResponse response = new StockQuoteResponse();

        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
        for (String symbol : request.getSymbol()) {
            StockQuote quote = new StockQuote();
            quote.setSymbol(symbol);
            quote.setDate(now);
            if ("FABRIKAM".equals(symbol)) {
                quote.setName("Fabrikam, Inc.");
                quote.setLast(120.00);
                quote.setChange(5.5);
            }
            else {
                quote.setName("Contoso Corp.");
                quote.setLast(50.07);
                quote.setChange(1.15);
            }
            response.getStockQuote().add(quote);
        }

        return response;
    }

}
