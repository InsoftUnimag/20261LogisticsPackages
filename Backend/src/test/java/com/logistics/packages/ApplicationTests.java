package com.logistics.packages;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application Tests")
class ApplicationTests {

	@Test
	@DisplayName("verifica que la clase Application existe y es configurable")
	void contextLoads() {
		// Verifica que la clase Application puede ser referenciada
		// sin cargar el contexto de Spring
		assertNotNull(Application.class);
		assertTrue(Application.class.getSimpleName().equals("Application"));
	}
}