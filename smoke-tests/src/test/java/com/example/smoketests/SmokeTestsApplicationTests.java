package com.example.smoketests;

import org.junit.jupiter.api.Test;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * Smoke tests to bootstrap Spring module components to verify they are functional.
 */
class SmokeTestsApplicationTests {

	@Test
	void createWebServiceTemplate() {
		WebServiceTemplate template = new WebServiceTemplate();
	}
}
