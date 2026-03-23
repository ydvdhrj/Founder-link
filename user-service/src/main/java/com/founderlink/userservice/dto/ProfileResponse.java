package com.founderlink.userservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProfileResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String skills;
    private String experience;
    private String bio;
    private List<String> portfolioLinks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProfileResponse() {
    }

    public ProfileResponse(Long id, Long userId, String name, String email, String skills,
            String experience, String bio, List<String> portfolioLinks,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.experience = experience;
        this.bio = bio;
        this.portfolioLinks = portfolioLinks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getPortfolioLinks() {
        return portfolioLinks;
    }

    public void setPortfolioLinks(List<String> portfolioLinks) {
        this.portfolioLinks = portfolioLinks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
