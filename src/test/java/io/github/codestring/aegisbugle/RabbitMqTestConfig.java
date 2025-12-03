package io.github.codestring.aegisbugle;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapper;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapperImpl;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


@TestConfiguration
public class RabbitMqTestConfig {

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

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
