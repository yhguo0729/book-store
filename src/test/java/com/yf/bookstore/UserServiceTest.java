package com.yf.bookstore;

import com.yf.bookstore.model.user.User;
import com.yf.bookstore.repository.UserRepository;
import com.yf.bookstore.service.UserService;
import com.yf.bookstore.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("password");
        user.setRole("USER");
    }

    @Test
    public void register_UserAlreadyExists_ThrowsException() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> userService.register(user));

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void register_UserDoesNotExist_SavesUser() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.register(user);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void login_ValidCredentials_ReturnsToken() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);

        String token = userService.login(user.getUsername(), user.getPassword());

        assertNotNull(token);
        assertTrue(token.length() > 0);
        verify(redisTemplate, times(1)).opsForValue().set(anyString(), anyString(), anyInt(), any(TimeUnit.class));
    }

    @Test
    public void login_InvalidCredentials_ThrowsException() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.login(user.getUsername(), user.getPassword()));

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(redisTemplate, never()).opsForValue().set(anyString(), anyString(), anyInt(), any(TimeUnit.class));
    }

    @Test
    public void getUserInfo_UserAuthenticated_ReturnsUserInfo() {
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setUsername("authenticatedUser");

        when(userRepository.findByUsername("authenticatedUser")).thenReturn(authenticatedUser);

        User userInfo = userService.getUserInfo();

        assertNotNull(userInfo);
        assertEquals("authenticatedUser", userInfo.getUsername());
        verify(userRepository, times(1)).findByUsername("authenticatedUser");
    }

    @Test
    public void getUserInfo_UserNotAuthenticated_ThrowsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.getUserInfo());

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    public void loadUserByUsername_UserExists_ReturnsUserDetails() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        verify(userRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    public void loadUserByUsername_UserDoesNotExist_ThrowsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(anyString()));

        verify(userRepository, times(1)).findByUsername(anyString());
    }
}

