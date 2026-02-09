package com.JK.FinIce.authservice.config.security;

import com.JK.FinIce.authservice.entity.User;
import com.JK.FinIce.authservice.entity.UserPrincipal;
import com.JK.FinIce.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + usernameOrEmail));

        // Reset the failed login attempts if the account locked time is expired
        if(user.getAccountLocked()){
            if(user.getAccountLockedUntil() != null && LocalDateTime.now().isAfter(user.getAccountLockedUntil())){
                user.resetFailedLoginAttempts();
                userRepository.save(user);
            }
        }

        return UserPrincipal.create(user);
    }
}
