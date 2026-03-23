package com.founderlink.userservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.founderlink.userservice.dto.CreateProfileRequest;
import com.founderlink.userservice.dto.ProfileResponse;
import com.founderlink.userservice.dto.UpdateProfileRequest;
import com.founderlink.userservice.entity.Profile;
import com.founderlink.userservice.repo.ProfileRepository;
import com.founderlink.userservice.security.JwtPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public ProfileService(ProfileRepository profileRepository, ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProfileResponse create(CreateProfileRequest request, JwtPrincipal principal) {
        Long uid = principal.getUserId();
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (profileRepository.existsByUserId(uid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists for this user");
        }

        Profile p = new Profile();
        p.setUserId(uid);
        p.setName(request.getName().trim());
        p.setEmail(request.getEmail().trim().toLowerCase());
        p.setSkills(trimToNull(request.getSkills()));
        p.setExperience(trimToNull(request.getExperience()));
        p.setBio(trimToNull(request.getBio()));
        p.setPortfolioLinks(linksToJson(request.getPortfolioLinks()));

        return toResponse(profileRepository.save(p));
    }

    @Transactional
    public ProfileResponse update(Long userId, UpdateProfileRequest request, JwtPrincipal principal) {
        ensureCanEdit(userId, principal);

        Profile p = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (request.getName() != null) {
            p.setName(request.getName().trim());
        }
        if (request.getEmail() != null) {
            p.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getSkills() != null) {
            p.setSkills(trimToNull(request.getSkills()));
        }
        if (request.getExperience() != null) {
            p.setExperience(trimToNull(request.getExperience()));
        }
        if (request.getBio() != null) {
            p.setBio(trimToNull(request.getBio()));
        }
        if (request.getPortfolioLinks() != null) {
            p.setPortfolioLinks(linksToJson(request.getPortfolioLinks()));
        }

        return toResponse(profileRepository.save(p));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByUserId(Long userId) {
        Profile p = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<ProfileResponse> list(Pageable pageable) {
        return profileRepository.findAllByOrderByNameAsc(pageable).map(this::toResponse);
    }

    private void ensureCanEdit(Long userId, JwtPrincipal principal) {
        Long uid = principal.getUserId();
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (uid.equals(userId)) {
            return;
        }
        if (principal.hasRole("ROLE_ADMIN")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own profile");
    }

    private ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getId(),
                p.getUserId(),
                p.getName(),
                p.getEmail(),
                p.getSkills(),
                p.getExperience(),
                p.getBio(),
                linksFromJson(p.getPortfolioLinks()),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }

    private String linksToJson(List<String> links) {
        if (links == null || links.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(links);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid portfolio links");
        }
    }

    private List<String> linksFromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
