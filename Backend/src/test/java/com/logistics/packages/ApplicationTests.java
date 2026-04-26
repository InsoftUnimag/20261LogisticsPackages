package com.logistics.packages;

import com.logistics.packages.application.repository.CoverageService;
import com.logistics.packages.application.repository.ZonaAlmacenajeRepository;
import com.logistics.packages.application.usecase.RegistrarAdmisionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class ApplicationTests {

    @MockBean
    private RegistrarAdmisionUseCase registrarAdmisionUseCase;

	@Test
	void contextLoads() {
	}
}