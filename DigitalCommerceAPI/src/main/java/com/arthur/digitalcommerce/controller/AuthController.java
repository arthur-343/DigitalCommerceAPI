package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.security.request.LoginRequest;
import com.arthur.digitalcommerce.security.request.SignupRequest;
import com.arthur.digitalcommerce.security.response.UserInfoResponse;
import com.arthur.digitalcommerce.security.response.MessageResponse;
import com.arthur.digitalcommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return authService.registerUser(signUpRequest);
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {
        return authService.getCurrentUserName(authentication);
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails(Authentication authentication) {
        return authService.getUserDetails(authentication);
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> signoutUser() {
        return authService.signoutUser();
    }
}
