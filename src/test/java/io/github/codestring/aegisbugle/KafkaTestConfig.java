package io.github.codestring.aegisbugle;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapper;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapperImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class KafkaTestConfig {
    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Bean
    public AlertMapper alertMapper() {
        return new AlertMapperImpl();
    }
}
