/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.samples.stockquote.client.jaxws;

import java.net.URL;
import javax.xml.namespace.QName;

/**
 * Simple client that calls the <code>StockQuote</code> operations using JAX-WS.
 *
 * @author Arjen Poutsma
 */
public class Main {

    public static void main(String[] args) throws Exception {
        StockService service;
        if (args.length == 0) {
            service = new StockService();
        }
        else {
            QName serviceName = new QName("http://www.springframework.org/spring-ws/samples/stockquote", "StockSoap11");
            service = new StockService(new URL(args[0]), serviceName);
        }
        Stock stock = service.getStockSoap11();
        StockQuoteRequest request = new StockQuoteRequest();
        request.getSymbol().add("FABRIKAM");
        request.getSymbol().add("CONTOSO");

        System.out.format("Requesting quotes for %s%n", request.getSymbol());

        StockQuoteResponse response = stock.stockQuote(request);

        System.out.format("Got %d results%n", response.getStockQuote().size());
        for (StockQuote quote : response.getStockQuote()) {
            System.out.println();
            System.out.println("Symbol: " + quote.getSymbol());
            System.out.println("\tName:\t\t\t" + quote.getName());
            System.out.println("\tLast Price:\t\t" + quote.getLast());
            System.out.println("\tPrevious Change:\t" + quote.getChange() + "%");
        }
    }

}
