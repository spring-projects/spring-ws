package org.springframework.webflow.samples.shippingrate.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class Rate implements Serializable {

	private BigDecimal value;

	public Rate(BigDecimal value) {
		this.value = value;
	}

	public double getDoubleValue() {
		return value.doubleValue();
	}

	public boolean equals(Object o) {
		if (!(o instanceof Rate)) {
			return false;
		}
		return value.equals(((Rate)o).value);
	}

	public int hashCode() {
		return value.hashCode();
	}

	public String toString() {
		return value.toString();
	}
}
