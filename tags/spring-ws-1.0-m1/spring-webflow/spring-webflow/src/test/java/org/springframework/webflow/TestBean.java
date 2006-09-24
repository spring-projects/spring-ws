package org.springframework.webflow;

import java.io.Serializable;

public class TestBean implements Serializable {
	private int amount = 0;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public boolean equals(Object o) {
		if (!(o instanceof TestBean)) {
			return false;
		}
		return amount == ((TestBean)o).amount;
	}

	public int hashCode() {
		return amount * 29;
	}

	public String toString() {
		return "[TestBean amount = " + amount + "]";
	}
}