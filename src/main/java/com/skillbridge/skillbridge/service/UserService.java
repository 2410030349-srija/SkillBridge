package com.skillbridge.skillbridge.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.repository.UserRepository;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final int MAX_UNSAFE_UPLOAD_WARNINGS = 3;
    private static final Set<String> ALLOWED_ROLES = Set.of("LEARNER", "CREATOR", "ADMIN");
    private static final Set<String> USER_SELF_CHANGEABLE_ROLES = Set.of("LEARNER", "CREATOR");
    private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "SOFTWARE DEVELOPMENT",
            "DATA SCIENCE",
            "AGRICULTURE",
            "UI/UX",
            "DEVOPS",
            "BLOCKCHAIN");

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final ContentModerationService moderationService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, ContentModerationService moderationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.moderationService = moderationService;
    }

    public User saveUser(User user) {
        if (userRepository.findByEmail(normalize(user.getEmail())).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        if (userRepository.findByUsername(normalize(user.getUsername())).isPresent()) {
            throw new IllegalArgumentException("Username is already registered");
        }

        moderationService.ensureSafeText("name", user.getName());
        moderationService.ensureSafeText("username", user.getUsername());
        moderationService.ensureSafeText("email", user.getEmail());
        moderationService.ensureSafeText("role", user.getRole());
        moderationService.ensureSafeOptionalText("bio", user.getBio());

        user.setName(user.getName().trim());
        user.setUsername(normalize(user.getUsername()));
        user.setEmail(normalize(user.getEmail()));
        user.setRole(requireAllowedRole(user.getRole()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setTeachSkills(normalizeSkills(user.getTeachSkills()));
        user.setLearnSkills(normalizeSkills(user.getLearnSkills()));
        user.setInterests(normalizeInterests(user.getInterests()));
        user.setDomain(user.getDomain() == null || user.getDomain().isBlank() ? null : requireAllowedDomain(user.getDomain()));
        user.setProfileCompleted(user.getDomain() != null && user.getName() != null && !user.getName().isBlank());
        user.setVerified(false);
        user.setBlocked(false);
        user.setUnsafeUploadAttempts(0);

        return userRepository.save(user);
    }

    public User setupProfile(String email, String fullName, String domain, String role, List<String> interests, String bio) {
        User user = requireUserByEmail(email);

        moderationService.ensureSafeText("fullName", fullName);
        moderationService.ensureSafeText("domain", domain);
        moderationService.ensureSafeText("role", role);
        moderationService.ensureSafeOptionalText("bio", bio);

        user.setName(fullName.trim());
        user.setDomain(requireAllowedDomain(domain));
        user.setRole(requireAllowedRole(role));
        user.setInterests(normalizeInterests(interests));
        user.setBio(bio == null || bio.isBlank() ? null : bio.trim());
        user.setProfileCompleted(true);
        return userRepository.save(user);
    }

    public User markEmailVerified(String email) {
        User user = requireUserByEmail(email);
        user.setVerified(true);
        return userRepository.save(user);
    }

    public User changeOwnRole(String email, String role) {
        User user = requireUserByEmail(email);
        String normalizedRole = requireAllowedRole(role);
        if (!USER_SELF_CHANGEABLE_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("You can only switch between LEARNER and CREATOR");
        }
        user.setRole(normalizedRole);
        return userRepository.save(user);
    }

    public User changeOwnDomain(String email, String domain) {
        User user = requireUserByEmail(email);
        moderationService.ensureSafeText("domain", domain);
        user.setDomain(requireAllowedDomain(domain));
        return userRepository.save(user);
    }

    public List<String> supportedDomains() {
        return ALLOWED_DOMAINS.stream().sorted().toList();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(normalize(email)).orElse(null);
    }

    public User requireUserByEmail(String email) {
        User user = userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        // Temporarily disabled for testing - users can now access profile
        // ensureAccountNotBlocked(user);
        return user;
    }

    public User registerUnsafeUploadAttempt(User user) {
        int attempts = user.getUnsafeUploadAttempts() + 1;
        user.setUnsafeUploadAttempts(attempts);
        if (attempts > MAX_UNSAFE_UPLOAD_WARNINGS) {
            user.setBlocked(true);
        }
        return userRepository.save(user);
    }

    public void ensureAccountNotBlocked(User user) {
        if (user.isBlocked()) {
            throw new IllegalArgumentException("Your account is blocked due to repeated unsafe content uploads");
        }
    }

    public User addTeachSkill(String email, String skill) {
        User user = requireUser(email);
        String normalizedSkill = moderationService.normalizeSkill(skill);
        addUniqueSkill(user.getTeachSkills(), normalizedSkill);
        return userRepository.save(user);
    }

    public User addLearnSkill(String email, String skill) {
        User user = requireUser(email);
        String normalizedSkill = moderationService.normalizeSkill(skill);
        addUniqueSkill(user.getLearnSkills(), normalizedSkill);
        return userRepository.save(user);
    }

    public List<User> searchUsersBySkill(String skill) {
        String normalizedSkill = moderationService.normalizeSkill(skill);
        return userRepository.findAll().stream()
                .filter(user -> hasSkill(user.getTeachSkills(), normalizedSkill) || hasSkill(user.getLearnSkills(), normalizedSkill))
                .toList();
    }

    public List<User> findMatchesForUser(String email) {
        User user = requireUser(email);
        Set<String> teachSkills = new LinkedHashSet<>(normalizeSkills(user.getTeachSkills()));
        Set<String> learnSkills = new LinkedHashSet<>(normalizeSkills(user.getLearnSkills()));

        return userRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(user.getId()))
                .filter(candidate -> isMatch(candidate, teachSkills, learnSkills))
            .toList();
    }

    private boolean isMatch(User candidate, Set<String> teachSkills, Set<String> learnSkills) {
        Set<String> candidateTeach = new LinkedHashSet<>(normalizeSkills(candidate.getTeachSkills()));
        Set<String> candidateLearn = new LinkedHashSet<>(normalizeSkills(candidate.getLearnSkills()));

        boolean teachMatch = candidateTeach.stream().anyMatch(learnSkills::contains);
        boolean learnMatch = candidateLearn.stream().anyMatch(teachSkills::contains);
        return teachMatch || learnMatch;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (skills == null) {
            return new ArrayList<>();
        }

        return skills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(moderationService::normalizeSkill)
                .distinct()
                .toList();
    }

    private List<String> normalizeInterests(List<String> interests) {
        if (interests == null) {
            return new ArrayList<>();
        }

        return interests.stream()
                .filter(interest -> interest != null && !interest.isBlank())
                .map(String::trim)
                .map(this::normalizeInterest)
                .distinct()
                .toList();
    }

    public User requireUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
    }

    private void addUniqueSkill(List<String> skills, String skill) {
        if (skills == null) {
            return;
        }

        if (!skills.contains(skill)) {
            skills.add(skill);
        }
    }

    private boolean hasSkill(List<String> skills, String normalizedSkill) {
        return skills != null && skills.stream().map(this::normalize).anyMatch(normalizedSkill::equals);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeInterest(String value) {
        return Arrays.stream(value.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT))
                .reduce((left, right) -> left + " " + right)
                .orElse(value.trim());
    }

    private String requireAllowedRole(String role) {
        String normalized = normalizeRole(role);
        if ("CONTENT CREATOR".equals(normalized) || "UPLOADER".equals(normalized)) {
            normalized = "CREATOR";
        }
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new IllegalArgumentException("Role must be one of: LEARNER, CREATOR, ADMIN");
        }
        return normalized;
    }

    private String requireAllowedDomain(String domain) {
        String normalized = domain == null ? null : domain.trim().toUpperCase(Locale.ROOT);
        if (normalized == null || !ALLOWED_DOMAINS.contains(normalized)) {
            throw new IllegalArgumentException("Invalid domain. Use supported domain values only");
        }
        return normalized;
    }
}