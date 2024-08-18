package com.yf.bookstore.controller;

import com.yf.bookstore.model.user.User;
import com.yf.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Inject UserService
    private final UserService userService;

    // Constructor injection
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * User registration interface
     *
     * @param user User information sent by the client
     * @return Returns the registration result to the client
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User registeredUser = userService.register(user);
        return ResponseEntity.ok(registeredUser);
    }

    /**
     * User login interface
     *
     * @param username User's account
     * @param password User's password
     * @return Returns the login result to the client, mainly the token
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        return ResponseEntity.ok(token);
    }

    /**
     * Get user information interface
     *
     * @return Returns the user's information
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserInfo() {
        // The @CurrentUser annotation will automatically inject the user ID here
        User userInfo = userService.getUserInfo();

        return ResponseEntity.ok(userInfo);
    }

}