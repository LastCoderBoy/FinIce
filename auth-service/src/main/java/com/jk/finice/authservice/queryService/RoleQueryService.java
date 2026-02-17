package com.jk.finice.authservice.queryService;

import com.jk.finice.authservice.entity.Role;
import com.jk.finice.authservice.enums.RoleName;
import com.jk.finice.authservice.repository.RoleRepository;
import com.jk.finice.commonlibrary.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleQueryService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Role findByName(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Role not found with name: {}", roleName);
                    return new ResourceNotFoundException("Role not found with name: " + roleName);
                });
    }

    @Transactional
    public Role getOrCreateDefaultRole() {
        return roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> {
                    log.warn("[AUTH-SERVICE] ROLE_USER not found, creating new one");
                    Role newRole = new Role(RoleName.ROLE_USER);
                    return roleRepository.save(newRole);
                });
    }
}
