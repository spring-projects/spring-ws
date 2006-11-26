
package org.springframework.ws.samples.airline.client.jws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Flight complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Flight">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="number" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}FlightNumber"/>
 *         &lt;element name="departureTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="from" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}Airport"/>
 *         &lt;element name="arrivalTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="to" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}Airport"/>
 *         &lt;element name="serviceClass" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}ServiceClass"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Flight", propOrder = {
    "number",
    "departureTime",
    "from",
    "arrivalTime",
    "to",
    "serviceClass"
})
public class Flight {

    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected String number;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected XMLGregorianCalendar departureTime;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected Airport from;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected XMLGregorianCalendar arrivalTime;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected Airport to;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected ServiceClass serviceClass;

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumber(String value) {
        this.number = value;
    }

    /**
     * Gets the value of the departureTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDepartureTime() {
        return departureTime;
    }

    /**
     * Sets the value of the departureTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDepartureTime(XMLGregorianCalendar value) {
        this.departureTime = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link Airport }
     *     
     */
    public Airport getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link Airport }
     *     
     */
    public void setFrom(Airport value) {
        this.from = value;
    }

    /**
     * Gets the value of the arrivalTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Sets the value of the arrivalTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setArrivalTime(XMLGregorianCalendar value) {
        this.arrivalTime = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link Airport }
     *     
     */
    public Airport getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link Airport }
     *     
     */
    public void setTo(Airport value) {
        this.to = value;
    }

    /**
     * Gets the value of the serviceClass property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceClass }
     *     
     */
    public ServiceClass getServiceClass() {
        return serviceClass;
    }

    /**
     * Sets the value of the serviceClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceClass }
     *     
     */
    public void setServiceClass(ServiceClass value) {
        this.serviceClass = value;
    }

}
