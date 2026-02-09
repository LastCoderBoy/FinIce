package com.JK.FinIce.authservice.serviceTest;


import com.JK.FinIce.authservice.config.AuthCookiesManager;
import com.JK.FinIce.authservice.config.redis.RedisService;
import com.JK.FinIce.authservice.config.security.JwtProvider;
import com.JK.FinIce.authservice.dto.AuthResponse;
import com.JK.FinIce.authservice.dto.RegisterRequest;
import com.JK.FinIce.authservice.entity.RefreshToken;
import com.JK.FinIce.authservice.entity.User;
import com.JK.FinIce.authservice.queryService.RoleQueryService;
import com.JK.FinIce.authservice.repository.UserRepository;
import com.JK.FinIce.authservice.service.RefreshTokenService;
import com.JK.FinIce.authservice.service.impl.AuthenticationServiceImpl;
import com.JK.FinIce.authservice.utils.RefreshTokenUtils;
import com.JK.FinIce.authservice.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.IP_ADDRESS_HEADER;
import static com.JK.FinIce.commonlibrary.constants.AppConstants.USER_AGENT_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceUnitTest {

    private User userA;
    private RefreshToken freshRefreshToken;
    private final String clientIp = "127.0.0.1";
    private final String userAgent = "Test-Agent";
    private final String accessToken = "test-Access-Token-" + System.currentTimeMillis();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthCookiesManager cookiesManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RedisService redisService;
    @Mock
    private RoleQueryService roleQueryService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl serviceUnderTest;


    @BeforeEach
    void setUp() {
        userA = UserUtils.freshUser();
        freshRefreshToken = RefreshTokenUtils.buildRefreshToken(userA, clientIp, userAgent);
    }

    @Test
    void testRegister_Success(){
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("MTven")
                .email("Tven@gmail.com")
                .firstName("Mark")
                .lastName("Tven")
                .password("Mtven01")
                .phoneNumber("+48600000000")
                .build();

        List<String> userRoles = userA.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        // Stubbing
        when(request.getHeader(IP_ADDRESS_HEADER)).thenReturn(clientIp);
        when(request.getHeader(USER_AGENT_HEADER)).thenReturn(userAgent);
        when(userRepository.save(any(User.class))).thenReturn(userA);

        when(jwtProvider.generateAccessToken(userA.getUsername(), userA.getId(), userA.getEmail(), userRoles))
                .thenReturn(accessToken);

        when(refreshTokenService.createRefreshToken(userA, clientIp, userAgent)).thenReturn(freshRefreshToken);

        // Act
        AuthResponse actualResult = serviceUnderTest.register(registerRequest, response, request);

        // Assert
        Assertions.assertThat(actualResult.getAccessToken()).isEqualTo(accessToken);
        Assertions.assertThat(actualResult.getTokenType()).isEqualTo("Bearer");
        Assertions.assertThat(actualResult.getUser().getId()).isEqualTo(userA.getId());
        Assertions.assertThat(actualResult.getUser().getUsername()).isEqualTo(userA.getUsername());
        Assertions.assertThat(actualResult.getUser().getEmail()).isEqualTo(userA.getEmail());
        Assertions.assertThat(actualResult.getUser().getRoles()).isEqualTo(userRoles);
        Assertions.assertThat(actualResult.getUser().getFirstName()).isEqualTo(userA.getFirstName());

    }

}
