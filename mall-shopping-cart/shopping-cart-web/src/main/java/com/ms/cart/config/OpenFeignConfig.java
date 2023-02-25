package com.ms.cart.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenFeignConfig {

    @Bean
    public Logger.Level openFeignLogLevel() {
        return Logger.Level.FULL;
    }
}
