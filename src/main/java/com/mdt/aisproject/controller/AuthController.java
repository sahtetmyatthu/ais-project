package com.mdt.aisproject.controller;

import com.mdt.aisproject.dto.LoginRequest;
import com.mdt.aisproject.dto.SignUpRequestBody;
import com.mdt.aisproject.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequestBody request){
        try{
            authService.signUp(request.getEmail(), request.getPassword());
            return ResponseEntity.ok("User signed up, awaiting confirmation");
        } catch(Exception e){
            return ResponseEntity.status(500).body("Sign up failed"+ e.getMessage());
        }
    }


    @PostMapping("/confirm-signup")
    public ResponseEntity<String> confirmSignUp(@RequestParam String username, @RequestBody(required = false) Map<String, String> clientMetadata){
        try{
            authService.adminConfirmSignup(username, clientMetadata);
            return ResponseEntity.ok("User confirmed successfully");
        }catch(Exception e){
            return ResponseEntity.status(500).body("Confirmation error"+ e.getMessage());
        }
    }
}
