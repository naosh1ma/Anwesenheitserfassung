package com.art.erfassung.tests;

import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StatusService;
import com.art.erfassung.service.StudentenService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockConfig {

    @Bean
    public StudentenService studentenService() {
        return Mockito.mock(StudentenService.class);
    }

    @Bean
    public ErfassungService erfassungService() {
        return Mockito.mock(ErfassungService.class);
    }

    @Bean
    public StatusService statusService() {
        return Mockito.mock(StatusService.class);
    }

    @Bean
    public GruppeService gruppeService() {
        return Mockito.mock(GruppeService.class);
    }
}
