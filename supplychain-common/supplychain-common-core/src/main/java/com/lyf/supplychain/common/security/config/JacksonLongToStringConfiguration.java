package com.lyf.supplychain.common.security.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON serialization rules shared by all services.
 */
@Configuration
public class JacksonLongToStringConfiguration {

    /**
     * Serialize Snowflake-style IDs as strings so browser clients do not lose
     * precision when parsing 19-digit Java Long values.
     */
    @Bean
    public static Jackson2ObjectMapperBuilderCustomizer longToStringJacksonCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
}
