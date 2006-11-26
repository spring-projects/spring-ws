
package org.springframework.ws.samples.airline.client.jws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.springframework.ws.samples.airline.client.jws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetFrequentFlyerMileageResponse_QNAME = new QName("http://www.springframework.org/spring-ws/samples/airline/schemas", "GetFrequentFlyerMileageResponse");
    private final static QName _BookFlightResponse_QNAME = new QName("http://www.springframework.org/spring-ws/samples/airline/schemas", "BookFlightResponse");
    private final static QName _GetFrequentFlyerMileageRequest_QNAME = new QName("http://www.springframework.org/spring-ws/samples/airline/schemas", "GetFrequentFlyerMileageRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.springframework.ws.samples.airline.client.jws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link org.springframework.ws.samples.airline.client.jws.Ticket.Passengers }
     * 
     */
    public org.springframework.ws.samples.airline.client.jws.Ticket.Passengers createTicketPassengers() {
        return new org.springframework.ws.samples.airline.client.jws.Ticket.Passengers();
    }

    /**
     * Create an instance of {@link Flight }
     * 
     */
    public Flight createFlight() {
        return new Flight();
    }

    /**
     * Create an instance of {@link Airport }
     * 
     */
    public Airport createAirport() {
        return new Airport();
    }

    /**
     * Create an instance of {@link GetFlightsRequest }
     * 
     */
    public GetFlightsRequest createGetFlightsRequest() {
        return new GetFlightsRequest();
    }

    /**
     * Create an instance of {@link GetFlightsResponse }
     * 
     */
    public GetFlightsResponse createGetFlightsResponse() {
        return new GetFlightsResponse();
    }

    /**
     * Create an instance of {@link org.springframework.ws.samples.airline.client.jws.BookFlightRequest.Passengers }
     * 
     */
    public org.springframework.ws.samples.airline.client.jws.BookFlightRequest.Passengers createBookFlightRequestPassengers() {
        return new org.springframework.ws.samples.airline.client.jws.BookFlightRequest.Passengers();
    }

    /**
     * Create an instance of {@link BookFlightRequest }
     * 
     */
    public BookFlightRequest createBookFlightRequest() {
        return new BookFlightRequest();
    }

    /**
     * Create an instance of {@link Name }
     * 
     */
    public Name createName() {
        return new Name();
    }

    /**
     * Create an instance of {@link Ticket }
     * 
     */
    public Ticket createTicket() {
        return new Ticket();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", name = "GetFrequentFlyerMileageResponse")
    public JAXBElement<Integer> createGetFrequentFlyerMileageResponse(Integer value) {
        return new JAXBElement<Integer>(_GetFrequentFlyerMileageResponse_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ticket }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", name = "BookFlightResponse")
    public JAXBElement<Ticket> createBookFlightResponse(Ticket value) {
        return new JAXBElement<Ticket>(_BookFlightResponse_QNAME, Ticket.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", name = "GetFrequentFlyerMileageRequest")
    public JAXBElement<Object> createGetFrequentFlyerMileageRequest(Object value) {
        return new JAXBElement<Object>(_GetFrequentFlyerMileageRequest_QNAME, Object.class, null, value);
    }

}
