
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

package org.springframework.ws.samples.airline.client.jws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.ws.samples.airline.client.jws.BookFlightRequest.Passengers;


/**
 * <p>Java class for BookFlightRequest element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="BookFlightRequest">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;all>
 *           &lt;element name="flightNumber" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}FlightNumber"/>
 *           &lt;element name="departureTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *           &lt;element name="passengers">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;choice maxOccurs="9">
 *                     &lt;element name="passenger" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}Name"/>
 *                     &lt;element name="username" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}FrequentFlyerUsername"/>
 *                   &lt;/choice>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
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
@XmlRootElement(name = "BookFlightRequest")
public class BookFlightRequest {

    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected String flightNumber;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected XMLGregorianCalendar departureTime;
    @XmlElement(namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true)
    protected Passengers passengers;

    /**
     * Gets the value of the flightNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlightNumber() {
        return flightNumber;
    }

    /**
     * Sets the value of the flightNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlightNumber(String value) {
        this.flightNumber = value;
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
     * Gets the value of the passengers property.
     * 
     * @return
     *     possible object is
     *     {@link Passengers }
     *     
     */
    public Passengers getPassengers() {
        return passengers;
    }

    /**
     * Sets the value of the passengers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Passengers }
     *     
     */
    public void setPassengers(Passengers value) {
        this.passengers = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice maxOccurs="9">
     *         &lt;element name="passenger" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}Name"/>
     *         &lt;element name="username" type="{http://www.springframework.org/spring-ws/samples/airline/schemas}FrequentFlyerUsername"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "passengerOrUsername"
    })
    public static class Passengers {

        @XmlElements({
            @XmlElement(name = "passenger", namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true, type = Name.class),
            @XmlElement(name = "username", namespace = "http://www.springframework.org/spring-ws/samples/airline/schemas", required = true, type = String.class)
        })
        protected List<Object> passengerOrUsername;

        /**
         * Gets the value of the passengerOrUsername property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the passengerOrUsername property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPassengerOrUsername().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Name }
         * {@link String }
         * 
         * 
         */
        public List<Object> getPassengerOrUsername() {
            if (passengerOrUsername == null) {
                passengerOrUsername = new ArrayList<Object>();
            }
            return this.passengerOrUsername;
        }

    }

}
