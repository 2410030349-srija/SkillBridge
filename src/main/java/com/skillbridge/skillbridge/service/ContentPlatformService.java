package com.skillbridge.skillbridge.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.skillbridge.dto.ContentFeedbackRequest;
import com.skillbridge.skillbridge.dto.ContentUpsertRequest;
import com.skillbridge.skillbridge.dto.ContentView;
import com.skillbridge.skillbridge.dto.UserFeedbackView;
import com.skillbridge.skillbridge.exception.ModerationViolationException;
import com.skillbridge.skillbridge.model.Content;
import com.skillbridge.skillbridge.model.ContentBookmark;
import com.skillbridge.skillbridge.model.ContentFeedback;
import com.skillbridge.skillbridge.model.ContentReaction;
import com.skillbridge.skillbridge.model.ReactionType;
import com.skillbridge.skillbridge.model.SearchHistory;
import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.repository.ContentBookmarkRepository;
import com.skillbridge.skillbridge.repository.ContentFeedbackRepository;
import com.skillbridge.skillbridge.repository.ContentReactionRepository;
import com.skillbridge.skillbridge.repository.ContentRepository;
import com.skillbridge.skillbridge.repository.SearchHistoryRepository;

@Service
public class ContentPlatformService {

    private final ContentRepository contentRepository;
    private final ContentFeedbackRepository feedbackRepository;
    private final ContentReactionRepository reactionRepository;
    private final ContentBookmarkRepository bookmarkRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserService userService;
    private final ContentModerationService moderationService;

    public ContentPlatformService(
            ContentRepository contentRepository,
            ContentFeedbackRepository feedbackRepository,
            ContentReactionRepository reactionRepository,
            ContentBookmarkRepository bookmarkRepository,
            SearchHistoryRepository searchHistoryRepository,
            UserService userService,
            ContentModerationService moderationService) {
        this.contentRepository = contentRepository;
        this.feedbackRepository = feedbackRepository;
        this.reactionRepository = reactionRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.userService = userService;
        this.moderationService = moderationService;
    }

    public ContentView createContent(String email, ContentUpsertRequest request) {
        User user = userService.requireUserByEmail(email);
        requireCreatorOrAdmin(user);

        if ("CREATOR".equals(user.getRole()) && !user.isVerified()) {
            throw new IllegalArgumentException("Email verification is required before uploading content");
        }

        Content content = new Content();
        applyContentRequest(content, request, user);
        content.setUploadedBy(user);
        // Auto-approve for admins and verified creators, unless content fails moderation
        content.setVerified("ADMIN".equals(user.getRole()) || user.isVerified());

        return toView(contentRepository.save(content));
    }

    public ContentView updateOwnContent(String email, Long contentId, ContentUpsertRequest request) {
        User user = userService.requireUserByEmail(email);
        userService.ensureAccountNotBlocked(user);
        Content content = requireContent(contentId);

        if (!isAdmin(user) && !content.getUploadedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only edit your own content");
        }

        applyContentRequest(content, request, user);
        content.setVerified(isAdmin(user) || user.isVerified());

        return toView(contentRepository.save(content));
    }

    @Transactional
    public void deleteOwnContent(String email, Long contentId) {
        User user = userService.requireUserByEmail(email);
        userService.ensureAccountNotBlocked(user);
        Content content = requireContent(contentId);

        System.out.println("[DELETE-DEBUG] contentId=" + contentId + ", ownerId=" + (content.getUploadedBy() != null ? content.getUploadedBy().getId() : "null") + ", currentUserId=" + user.getId() + ", isAdmin=" + isAdmin(user));

        if (!isAdmin(user) && !content.getUploadedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own content (contentId=" + contentId + ", ownerId=" + content.getUploadedBy().getId() + ", currentUserId=" + user.getId() + ")");
        }

        feedbackRepository.deleteByContentId(content.getId());
        reactionRepository.deleteByContentId(content.getId());
        bookmarkRepository.deleteByContentId(content.getId());
        contentRepository.delete(content);
    }

    public ContentView getContentDetails(String email, Long contentId) {
        User user = userService.requireUserByEmail(email);
        Content content = requireContent(contentId);

        if (!content.isVerified() && !isAdmin(user) && !content.getUploadedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Content is not approved yet");
        }

        content.setViews(content.getViews() + 1);
        return toView(contentRepository.save(content));
    }

    public List<ContentView> searchContent(String email, String keyword) {
        User user = userService.requireUserByEmail(email);
        String cleanKeyword = normalizeKeyword(keyword);
        moderationService.ensureSafeText("keyword", cleanKeyword);

        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setKeyword(cleanKeyword);
        searchHistoryRepository.save(history);

        String lowered = cleanKeyword.toLowerCase(Locale.ROOT);
        Set<String> queryTerms = collectQueryTerms(lowered);
        return listVisibleContents(user).stream()
            .filter(content -> matchesQuery(content, queryTerms, lowered))
        .sorted(Comparator.comparingDouble((Content content) -> searchScore(content, queryTerms, lowered)).reversed())
                .map(this::toView)
                .toList();
    }

    public List<ContentView> personalizedFeed(String email) {
        User user = userService.requireUserByEmail(email);
        List<Content> visible = listVisibleContents(user);

        if (visible.isEmpty()) {
            return List.of();
        }

        List<SearchHistory> history = searchHistoryRepository.findTop20ByUserIdOrderBySearchedAtDesc(user.getId());
        Set<String> keywords = history.stream()
                .map(SearchHistory::getKeyword)
                .map(this::normalizeKeyword)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> likedTags = reactionRepository.findByUserId(user.getId()).stream()
                .filter(reaction -> reaction.getReactionType() == ReactionType.LIKE)
                .map(ContentReaction::getContent)
                .flatMap(content -> content.getTags().stream())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        boolean hasActivity = !keywords.isEmpty() || !likedTags.isEmpty();
        List<Content> seed = hasActivity
                ? visible
                : visible.stream().filter(content -> domainMatches(content, user.getDomain())).toList();

        if (seed.isEmpty()) {
            seed = visible;
        }

        Map<Long, Double> scoreById = seed.stream().collect(Collectors.toMap(Content::getId,
                content -> personalizedScore(content, user.getDomain(), keywords, likedTags),
                (left, right) -> left));

        return seed.stream()
                .sorted(Comparator.comparingDouble((Content content) -> scoreById.getOrDefault(content.getId(), 0.0)).reversed())
                .map(this::toView)
                .limit(20)
                .toList();
    }

    public ContentView addOrUpdateFeedback(String email, Long contentId, ContentFeedbackRequest request) {
        User user = userService.requireUserByEmail(email);
        Content content = requireContent(contentId);

        if (!content.isVerified()) {
            throw new IllegalArgumentException("Feedback is allowed only for approved content");
        }

        moderationService.ensureSafeText("feedback", request.comment());

        ContentFeedback feedback = feedbackRepository.findByUserIdAndContentId(user.getId(), contentId)
                .orElseGet(ContentFeedback::new);
        feedback.setUser(user);
        feedback.setContent(content);
        feedback.setRating(request.rating());
        feedback.setComment(request.comment().trim());
        feedbackRepository.save(feedback);

        return toView(content);
    }

    public ContentView react(String email, Long contentId, String reactionType) {
        User user = userService.requireUserByEmail(email);
        Content content = requireVisibleContentForUser(user, contentId);

        ReactionType parsedReaction;
        try {
            parsedReaction = ReactionType.valueOf(reactionType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Reaction type must be LIKE or DISLIKE");
        }

        ContentReaction reaction = reactionRepository.findByUserIdAndContentId(user.getId(), contentId)
                .orElseGet(ContentReaction::new);
        reaction.setUser(user);
        reaction.setContent(content);
        reaction.setReactionType(parsedReaction);
        reactionRepository.save(reaction);

        return toView(content);
    }

    public void bookmark(String email, Long contentId) {
        User user = userService.requireUserByEmail(email);
        Content content = requireVisibleContentForUser(user, contentId);

        if (bookmarkRepository.existsByUserIdAndContentId(user.getId(), contentId)) {
            return;
        }

        ContentBookmark bookmark = new ContentBookmark();
        bookmark.setUser(user);
        bookmark.setContent(content);
        bookmarkRepository.save(bookmark);
    }

    public void removeBookmark(String email, Long contentId) {
        User user = userService.requireUserByEmail(email);
        bookmarkRepository.deleteByUserIdAndContentId(user.getId(), contentId);
    }

    public List<ContentView> myBookmarks(String email) {
        User user = userService.requireUserByEmail(email);
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ContentBookmark::getContent)
                .map(this::toView)
                .toList();
    }

    public List<ContentView> myUploadedContent(String email) {
        User user = userService.requireUserByEmail(email);
        return contentRepository.findByUploadedByIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toView)
                .toList();
    }

    public List<UserFeedbackView> myFeedback(String email) {
        User user = userService.requireUserByEmail(email);
        return feedbackRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(UserFeedbackView::from)
                .toList();
    }

    public List<ContentView> pendingContentForAdmin(String email) {
        User admin = userService.requireUserByEmail(email);
        requireAdmin(admin);

        return contentRepository.findByVerifiedFalseOrderByCreatedAtDesc().stream()
                .map(this::toView)
                .toList();
    }

    public ContentView approveContent(String email, Long contentId) {
        User admin = userService.requireUserByEmail(email);
        requireAdmin(admin);

        Content content = requireContent(contentId);
        content.setVerified(true);
        return toView(contentRepository.save(content));
    }

    public void rejectContent(String email, Long contentId) {
        User admin = userService.requireUserByEmail(email);
        requireAdmin(admin);

        Content content = requireContent(contentId);
        contentRepository.delete(content);
    }

    private void applyContentRequest(Content content, ContentUpsertRequest request, User actor) {
        try {
            moderationService.ensureSafeText("title", request.title());
            moderationService.ensureSafeText("description", request.description());
            moderationService.ensureSafeText("domain", request.domain());
            moderationService.ensureSafeOptionalUrl("resourceLink", request.resourceLink());
            moderationService.ensureSafeOptionalText("resourceFile", request.resourceFile());
        } catch (ModerationViolationException ex) {
            handleUnsafeUploadAttempt(actor, ex);
            throw ex;
        }

        content.setTitle(request.title().trim());
        content.setDescription(request.description().trim());
        content.setDomain(request.domain().trim().toUpperCase(Locale.ROOT));
        content.setTags(normalizeTags(request.tags()));
        content.setResourceLink(blankToNull(request.resourceLink()));
        content.setResourceFile(blankToNull(request.resourceFile()));
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }

        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                .map(String::toUpperCase)
                .distinct()
                .limit(15)
                .toList();
    }

    private List<Content> listVisibleContents(User user) {
        if (isAdmin(user)) {
            return contentRepository.findAll();
        }

        List<Content> approved = contentRepository.findByVerifiedTrueOrderByCreatedAtDesc();
        if ("LEARNER".equals(user.getRole())) {
            return approved;
        }

        List<Content> mine = contentRepository.findByUploadedByIdOrderByCreatedAtDesc(user.getId());
        Map<Long, Content> combined = approved.stream().collect(Collectors.toMap(Content::getId, Function.identity()));
        mine.forEach(content -> combined.put(content.getId(), content));
        return combined.values().stream().toList();
    }

    private Content requireVisibleContentForUser(User user, Long contentId) {
        Content content = requireContent(contentId);
        if (!content.isVerified() && !isAdmin(user) && !content.getUploadedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Content is not approved yet");
        }
        return content;
    }

    private Content requireContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
    }

    private boolean matchesQuery(Content content, Set<String> queryTerms, String queryPhrase) {
        String contentText = buildSearchableText(content);
        Set<String> contentTokens = tokenize(contentText);
        Set<String> atomicTerms = queryTerms.stream().filter(term -> !term.contains(" ")).collect(Collectors.toCollection(LinkedHashSet::new));
        boolean phraseMatch = queryPhrase.contains(" ") && contentText.contains(queryPhrase);

        return phraseMatch || atomicTerms.stream().allMatch(term -> tokenMatches(term, contentTokens));
    }

    private double searchScore(Content content, Set<String> queryTerms, String queryPhrase) {
        String contentText = buildSearchableText(content);
        Set<String> contentTokens = tokenize(contentText);
        Set<String> atomicTerms = queryTerms.stream().filter(term -> !term.contains(" ")).collect(Collectors.toCollection(LinkedHashSet::new));

        long overlap = atomicTerms.stream()
            .filter(term -> tokenMatches(term, contentTokens))
                .count();

        if (queryPhrase.contains(" ") && contentText.contains(queryPhrase)) {
            overlap += 1;
        }

        return (overlap * 20.0) + trendingScore(content);
    }

    private Set<String> collectQueryTerms(String query) {
        Set<String> terms = new LinkedHashSet<>();
        String normalized = normalizeSearchToken(query);
        if (normalized.isBlank()) {
            return terms;
        }

        terms.add(normalized);
        for (String token : normalized.split("\\s+")) {
            if (!token.isBlank()) {
                terms.add(token);
            }
        }
        return terms;
    }

    private String buildSearchableText(Content content) {
        String joinedTags = content.getTags() == null ? "" : String.join(" ", content.getTags());
        return normalizeSearchToken(
                content.getTitle() + " "
                        + content.getDescription() + " "
                        + content.getDomain() + " "
                        + joinedTags);
    }

    private Set<String> tokenize(String normalizedText) {
        Set<String> tokens = new LinkedHashSet<>();
        if (normalizedText == null || normalizedText.isBlank()) {
            return tokens;
        }
        for (String token : normalizedText.split("\\s+")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private boolean tokenMatches(String term, Set<String> contentTokens) {
        if (term == null || term.isBlank()) {
            return false;
        }

        // Keep short terms strict to avoid false positives such as "ai" in "blockchain".
        if (term.length() <= 2) {
            return contentTokens.contains(term);
        }

        return contentTokens.stream().anyMatch(token -> token.equals(term) || token.startsWith(term));
    }

    private String normalizeSearchToken(String token) {
        return token == null ? "" : token.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private boolean domainMatches(Content content, String userDomain) {
        return userDomain != null && content.getDomain().equalsIgnoreCase(userDomain);
    }

    private double personalizedScore(Content content, String userDomain, Set<String> keywords, Set<String> likedTags) {
        double score = trendingScore(content);

        if (domainMatches(content, userDomain)) {
            score += 8.0;
        }

        String searchable = (content.getTitle() + " " + content.getDescription() + " " + content.getDomain()).toLowerCase(Locale.ROOT);
        long keywordHits = keywords.stream().filter(searchable::contains).count();
        score += keywordHits * 3.0;

        long tagHits = content.getTags().stream()
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .filter(likedTags::contains)
                .count();
        score += tagHits * 2.5;

        return score;
    }

    private double trendingScore(Content content) {
        long likes = reactionRepository.countByContentIdAndReactionType(content.getId(), ReactionType.LIKE);
        double avgRating = averageRating(content.getId());
        long daysOld = Math.max(0, Duration.between(content.getCreatedAt(), LocalDateTime.now()).toDays());
        double recencyBoost = Math.max(0, 10 - daysOld);
        return (content.getViews() * 0.2) + (likes * 2.0) + (avgRating * 2.5) + recencyBoost;
    }

    private double averageRating(Long contentId) {
        return feedbackRepository.findByContentId(contentId).stream()
                .mapToInt(ContentFeedback::getRating)
                .average()
                .orElse(0.0);
    }

    private void requireCreatorOrAdmin(User user) {
        userService.ensureAccountNotBlocked(user);
        if (!"CREATOR".equals(user.getRole()) && !isAdmin(user)) {
            throw new IllegalArgumentException("Only creators or admins can upload content");
        }
    }

    private void handleUnsafeUploadAttempt(User actor, ModerationViolationException originalError) {
        if (actor == null || isAdmin(actor) || !"CREATOR".equals(actor.getRole())) {
            return;
        }

        User updated = userService.registerUnsafeUploadAttempt(actor);
        int attempts = updated.getUnsafeUploadAttempts();
        if (updated.isBlocked()) {
            throw new IllegalArgumentException("Account blocked: you attempted to upload unsafe content more than 3 times");
        }

        throw new IllegalArgumentException("Warning: unsafe content rejected (attempt " + attempts + "/3 before block)");
    }

    private void requireAdmin(User user) {
        if (!isAdmin(user)) {
            throw new IllegalArgumentException("Admin access required");
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword is required");
        }
        return keyword.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ContentView toView(Content content) {
        long likes = reactionRepository.countByContentIdAndReactionType(content.getId(), ReactionType.LIKE);
        long dislikes = reactionRepository.countByContentIdAndReactionType(content.getId(), ReactionType.DISLIKE);

        return new ContentView(
                content.getId(),
                content.getTitle(),
                content.getDescription(),
                content.getDomain(),
                content.getTags(),
                content.getResourceLink(),
                content.getResourceFile(),
                content.getUploadedBy().getUsername(),
                content.isVerified(),
                content.getViews(),
                likes,
                dislikes,
                averageRating(content.getId()),
                content.getCreatedAt());
    }
}
