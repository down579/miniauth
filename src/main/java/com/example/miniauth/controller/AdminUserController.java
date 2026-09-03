package com.example.miniauth.controller;

import com.example.miniauth.dto.admin.AdminUserResponse;
import com.example.miniauth.dto.admin.RoleUpdateRequest;
import com.example.miniauth.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Page<AdminUserResponse> list(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return adminUserService.list(pageable);
    }

    @GetMapping("/{publicId}")
    public AdminUserResponse get(@PathVariable String publicId) {
        return adminUserService.get(publicId);
    }

    @PostMapping("/{publicId}/unlock")
    public AdminUserResponse unlock(@PathVariable String publicId) {
        return adminUserService.unlock(publicId);
    }

    @PostMapping("/{publicId}/enable")
    public AdminUserResponse enable(@PathVariable String publicId) {
        return adminUserService.changeStatus(publicId, true);
    }

    @PostMapping("/{publicId}/disable")
    public AdminUserResponse disable(@PathVariable String publicId) {
        return adminUserService.changeStatus(publicId, false);
    }

    @PostMapping("/{publicId}/roles")
    public AdminUserResponse addRole(
            @PathVariable String publicId,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        return adminUserService.addRole(publicId, request.roleCode());
    }

    @DeleteMapping("/{publicId}/roles/{roleCode}")
    public AdminUserResponse removeRole(
            @PathVariable String publicId,
            @PathVariable String roleCode
    ) {
        return adminUserService.removeRole(publicId, roleCode);
    }
}
