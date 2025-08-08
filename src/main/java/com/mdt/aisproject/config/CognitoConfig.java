package com.mdt.aisproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoConfig {

    @Value("${spring.security.oauth2.client.registration.cognito.region}")
    private String region;

    @Bean
    public CognitoIdentityProviderClient cognitoClient(){
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
    }
}
