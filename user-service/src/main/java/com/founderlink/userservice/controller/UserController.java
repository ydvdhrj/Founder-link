package com.founderlink.userservice.controller;

import com.founderlink.userservice.dto.CreateProfileRequest;
import com.founderlink.userservice.dto.ProfileResponse;
import com.founderlink.userservice.dto.UpdateProfileRequest;
import com.founderlink.userservice.security.JwtPrincipal;
import com.founderlink.userservice.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final ProfileService profileService;

    public UserController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** Paginated directory (public). */
    @GetMapping
    public Page<ProfileResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return profileService.list(pageable);
    }

    /** Profile by auth user id (public). */
    @GetMapping("/{userId}")
    public ProfileResponse get(@PathVariable Long userId) {
        return profileService.getByUserId(userId);
    }

    /** Lookup profile by email and return userId for messaging receiver selection. */
    @GetMapping("/lookup")
    public ProfileResponse lookupByEmail(@RequestParam String email) {
        return profileService.getByEmail(email);
    }

    /** Compact user id directory for cross-service references (e.g., messaging receiverId). */
    @GetMapping("/ids")
    public List<Long> userIds(@RequestParam(defaultValue = "50") int limit) {
        return profileService.listUserIds(limit);
    }

    /** Create profile for the authenticated user (userId comes from JWT). */
    @PostMapping
    public ResponseEntity<ProfileResponse> create(
            @Valid @RequestBody CreateProfileRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        ProfileResponse body = profileService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /** Update profile; only owner or ROLE_ADMIN. Path id is auth user id. */
    @PutMapping("/{userId}")
    public ProfileResponse update(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return profileService.update(userId, request, principal);
    }
}
