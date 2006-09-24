package org.springframework.webflow.samples.birthdate;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import org.springframework.validation.BindException;

public class BirthdateValidatorTests extends TestCase {
	public void testValidateCardForm() throws Exception {
		BirthDateValidator validator = new BirthDateValidator();
		BirthDate birthDate = new BirthDate();
		birthDate.setName("Keith");
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		birthDate.setSendCard(true);
		birthDate.setDate(format.parse("29/12/1977"));
		BindException errors = new BindException(birthDate, "birthDate");
		validator.validateCardForm(birthDate, errors);
		assertEquals(1, errors.getAllErrors().size());
	}
}
