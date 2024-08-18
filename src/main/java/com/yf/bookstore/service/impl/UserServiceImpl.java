package com.yf.bookstore.service.impl;

import com.yf.bookstore.model.user.User;
import com.yf.bookstore.repository.UserRepository;
import com.yf.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RedisTemplate<String, String> redisTemplate, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     *
     * @param user The user object to be registered.
     * @return The newly registered user object.
     * @throws IllegalArgumentException If the username already exists.
     */
    @Override
    public User register(User user) {
        // Check if the user already exists
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new IllegalArgumentException("User already exists.");
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the new user
        return userRepository.save(user);
    }

    /**
     * Authenticates the user and generates a token.
     *
     * @param username The username.
     * @param password The password.
     * @return The authentication token.
     * @throws IllegalArgumentException If the credentials are invalid.
     */
    @Override
    public String login(String username, String password) {
        // Find the user by username
        User user = userRepository.findByUsername(username);

        // Verify the password
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        // Generate a unique token
        String token = UUID.randomUUID().toString();

        // Store the token in Redis with an expiration time
        redisTemplate.opsForValue().set("token:" + token, user.getId().toString(), 1, TimeUnit.HOURS);

        return token;
    }

    /**
     * Retrieves the current user's information.
     *
     * @return The current user object.
     * @throws IllegalArgumentException If the user cannot be found.
     */
    @Override
    public User getUserInfo() {
        if(SecurityContextHolder.getContext().getAuthentication() == null){
            throw new IllegalArgumentException("User not login");
        }
        // Retrieve the user information by Name
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userRepository.findByUsername(userName);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found with ID: " + userName);
        }
        return existingUser;
    }

    /**
     * Loads user details by username for authentication.
     *
     * @param s The username.
     * @return The user details.
     * @throws UsernameNotFoundException If the user does not exist.
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User existingUser = userRepository.findByUsername(s);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not exists.");
        }
        // Convert role string to GrantedAuthority object
        GrantedAuthority authority = new SimpleGrantedAuthority(existingUser.getRole());

        // Create UserDetails instance
        return new org.springframework.security.core.userdetails.User(
                existingUser.getUsername(),
                existingUser.getPassword(),
                Collections.singletonList(authority));
    }
}