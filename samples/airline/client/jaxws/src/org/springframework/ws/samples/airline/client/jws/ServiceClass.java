
package org.springframework.ws.samples.airline.client.jws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for ServiceClass.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ServiceClass">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *     &lt;enumeration value="economy"/>
 *     &lt;enumeration value="business"/>
 *     &lt;enumeration value="first"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum ServiceClass {

    @XmlEnumValue("business")
    BUSINESS("business"),
    @XmlEnumValue("economy")
    ECONOMY("economy"),
    @XmlEnumValue("first")
    FIRST("first");
    private final String value;

    ServiceClass(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ServiceClass fromValue(String v) {
        for (ServiceClass c: ServiceClass.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
