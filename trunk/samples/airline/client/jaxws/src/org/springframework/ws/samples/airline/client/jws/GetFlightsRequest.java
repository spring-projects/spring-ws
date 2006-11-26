
package org.springframework.ws.samples.airline.client.jws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for GetFlightsRequest element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="GetFlightsRequest">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;all>
 *           &lt;element name="from" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}AirportCode"/>
 *           &lt;element name="to" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}AirportCode"/>
 *           &lt;element name="departureDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *           &lt;element name="serviceClass" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}ServiceClass" minOccurs="0"/>
 *         &lt;/all>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "GetFlightsRequest")
public class GetFlightsRequest {

    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected String from;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected String to;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected XMLGregorianCalendar departureDate;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas")
    protected ServiceClass serviceClass;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(String value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(String value) {
        this.to = value;
    }

    /**
     * Gets the value of the departureDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDepartureDate() {
        return departureDate;
    }

    /**
     * Sets the value of the departureDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDepartureDate(XMLGregorianCalendar value) {
        this.departureDate = value;
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
