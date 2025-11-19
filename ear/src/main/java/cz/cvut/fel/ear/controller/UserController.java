package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.dto.UserLoginDTO;
import cz.cvut.fel.ear.dto.UserRegistrationDTO;
import cz.cvut.fel.ear.security.JwtService;
import cz.cvut.fel.ear.security.UserDetailsImpl;
import cz.cvut.fel.ear.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody UserLoginDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("id", userDetails.getId());
            response.put("username", userDetails.getUsername());
            response.put("email", userDetails.getEmail());
            response.put("roles", userDetails.getAuthorities());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/signup/user")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }

    @PostMapping("/signup/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerAdmin(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Admin registered successfully!");
    }
}