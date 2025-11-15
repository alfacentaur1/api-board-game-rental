package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.dto.RegisteredUserCreateDTO;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /// NOTE - modify those dtos pls then implements spring security :) thx
    //endpoint for non-admin user registration
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisteredUserCreateDTO userCreationDTO) {
        //TODO implement user registration
        return null;
    }

    //endpoint for admin user creation
    @PostMapping("/admins")
    public ResponseEntity<?> createAdminUser(@RequestBody RegisteredUserCreateDTO userCreationDTO) {
        //TODO implement admin user creation
        return null;
    }
}
