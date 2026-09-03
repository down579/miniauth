package com.example.miniauth.service;

import com.example.miniauth.domain.Role;
import com.example.miniauth.domain.User;
import com.example.miniauth.dto.admin.AdminUserResponse;
import com.example.miniauth.repository.RoleRepository;
import com.example.miniauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(AdminUserResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse get(String publicId) {
        return AdminUserResponse.from(findUser(publicId));
    }

    @Transactional
    public AdminUserResponse unlock(String publicId) {
        User user = findUser(publicId);
        user.unlock();
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse changeStatus(String publicId, boolean enabled) {
        User user = findUser(publicId);
        if (enabled) {
            user.enable();
        } else {
            user.disable();
        }
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse addRole(String publicId, String roleCode) {
        User user = findUser(publicId);
        user.addRole(findRole(roleCode));
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse removeRole(String publicId, String roleCode) {
        User user = findUser(publicId);
        user.removeRole(findRole(roleCode));
        return AdminUserResponse.from(user);
    }

    private User findUser(String publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() ->
                        new NoSuchElementException("사용자를 찾을 수 없습니다."));
    }

    private Role findRole(String roleCode) {
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() ->
                        new NoSuchElementException("역할을 찾을 수 없습니다."));
    }
}
