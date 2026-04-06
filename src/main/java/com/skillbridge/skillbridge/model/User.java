package com.skillbridge.skillbridge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @NotBlank
    @Size(min = 8, max = 120)
    private String password;

    @NotBlank
    @Size(max = 30)
    private String role;

    @Size(max = 500)
    private String bio;

    @Size(max = 40)
    private String domain;

    private boolean verified = false;

    private boolean blocked = false;

    private int unsafeUploadAttempts = 0;

    private boolean profileCompleted = false;

    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private List<String> interests = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_teach_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> teachSkills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_learn_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> learnSkills = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User() {
        // Required by JPA for entity instantiation.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getUnsafeUploadAttempts() {
        return unsafeUploadAttempts;
    }

    public void setUnsafeUploadAttempts(int unsafeUploadAttempts) {
        this.unsafeUploadAttempts = unsafeUploadAttempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getTeachSkills() {
        return teachSkills;
    }

    public void setTeachSkills(List<String> teachSkills) {
        this.teachSkills = teachSkills;
    }

    public List<String> getLearnSkills() {
        return learnSkills;
    }

    public void setLearnSkills(List<String> learnSkills) {
        this.learnSkills = learnSkills;
    }
}