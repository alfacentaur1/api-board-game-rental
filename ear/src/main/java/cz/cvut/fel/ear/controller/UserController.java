package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.dto.UserLoginDTO;
import cz.cvut.fel.ear.dto.UserRegistrationDTO;
import cz.cvut.fel.ear.security.JwtService;
import cz.cvut.fel.ear.security.UserDetailsImpl;
import cz.cvut.fel.ear.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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

    /**
     * Authenticates a user with username and password.
     *
     * @param loginRequest Data transfer object containing the username and password.
     * @return A ResponseEntity containing the JWT token and user details on success.
     */
    @Operation(summary = "User Sign In", description = "Authenticates a user with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticated", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or validation error occurred", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> authenticateUser(
            @Valid @RequestBody UserLoginDTO loginRequest
    ) {
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

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", jwt);
            loginData.put("id", userDetails.getId());
            loginData.put("username", userDetails.getUsername());
            loginData.put("email", userDetails.getEmail());
            loginData.put("roles", userDetails.getAuthorities());

            ResponseWrapper generator = new ResponseWrapper();
            generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "User");
            generator.addResponseData("user", loginData);

            return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);

        } catch (AuthenticationException e) {
            ResponseWrapper generator = new ResponseWrapper();
            generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.INVALID_AUTHORIZATION);

            return new ResponseEntity<>(generator.getResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Registers a new regular user account in the system.
     *
     * @param registrationDTO Data transfer object containing registration details.
     * @return A ResponseEntity indicating success.
     */
    @Operation(summary = "Register New User", description = "Registers a new regular user account in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/signup/user")
    public ResponseEntity<Map<String, Object>> registerUser(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerUser(registrationDTO);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, "User");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.CREATED);
    }

    /**
     * Registers a new administrator account in the system.
     *
     * @param registrationDTO Data transfer object containing registration details.
     * @return A ResponseEntity indicating success.
     */
    @Operation(summary = "Register New Admin", description = "Registers a new administrator account in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin successfully registered", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Admin already exists", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/signup/admin")
    public ResponseEntity<Map<String, Object>> registerAdmin(
            @Valid @RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerAdmin(registrationDTO);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, "Admin");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.CREATED);
    }
}