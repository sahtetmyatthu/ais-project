package com.mdt.aisproject.service;

import com.mdt.aisproject.config.CognitoSecretHash;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${spring.security.oauth2.client.registration.cognito.userPoolId}")
    private String userPoolId;

    @Value("${spring.security.oauth2.client.registration.cognito.clientId}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.cognito.clientSecret}")
    private String clientSecret;

    public void signUp( String username,String password) {
        try {
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(username)
                    .password(password)
                    .secretHash(CognitoSecretHash.calculateSecretHash(username,clientId, clientSecret))
                    .build();

            cognitoClient.signUp(request);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void adminConfirmSignup(String username, Map<String, String> clientMetadata) {
        try{

            AdminConfirmSignUpRequest request = AdminConfirmSignUpRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .clientMetadata(clientMetadata)
                    .build();

            cognitoClient.adminConfirmSignUp(request);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, String> login(String email, String password) {
        Map<String, String> authParams = new LinkedHashMap<>();
        authParams.put("USERNAME", email);
        authParams.put("PASSWORD", password);

        // Calculate secret hash and add it to parameters
        String secretHash = CognitoSecretHash.calculateSecretHash(email, clientId, clientSecret);
        authParams.put("SECRET_HASH", secretHash);

        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authParameters(authParams)
                .build();

        AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("idToken", authResponse.authenticationResult().idToken());
        tokens.put("accessToken", authResponse.authenticationResult().accessToken());
        tokens.put("refreshToken", authResponse.authenticationResult().refreshToken());
        tokens.put("message", "Successfully login");


        return tokens;
    }
}
