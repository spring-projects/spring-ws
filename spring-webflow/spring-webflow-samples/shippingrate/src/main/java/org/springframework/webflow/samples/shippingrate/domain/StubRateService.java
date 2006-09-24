package org.springframework.webflow.samples.shippingrate.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class StubRateService implements RateService {

	public Map getCountries() {
		Map countries = new HashMap();
		countries.put("US", "United States");
		countries.put("CA", "Canada");
		return countries;
	}

	public Map getPackageTypes() {
		Map packageTypes = new HashMap();
		packageTypes.put("1", "Letter Envelope");
		packageTypes.put("2", "Express Box");
		packageTypes.put("3", "Tube");
		return packageTypes;
	}

	public Rate getRate(RateCriteria criteria) {
		return new Rate(new BigDecimal("1.39"));
	}
}
