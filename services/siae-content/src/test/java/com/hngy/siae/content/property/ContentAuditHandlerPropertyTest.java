package com.hngy.siae.content.property;

import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.entity.Statistics;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.StatisticsService;
import com.hngy.siae.content.strategy.audit.AuditHandler;
import com.hngy.siae.content.strategy.audit.AuditHandlerContext;
import com.hngy.siae.content.strategy.audit.impl.ContentAuditHandler;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Property-Based Tests for ContentAuditHandler
 * 
 * **Feature: content-service-refactor, Property 1 & 2: Content Audit Status Consistency**
 * **Validates: Requirements 2.2, 2.3**
 * 
 * This test uses mocking to isolate the ContentAuditHandler logic from database dependencies.
 * The properties verify that:
 * - Property 1: When content is approved, status changes to PUBLISHED and statistics are created
 * - Property 2: When content is rejected, status changes to DRAFT
 * 
 * @author Kiro
 */
public class ContentAuditHandlerPropertyTest {

    private ContentService contentService;
    private StatisticsService statisticsService;
    private ContentAuditHandler contentAuditHandler;
    private AuditHandlerContext auditHandlerContext;

    /**
     * Set up mocks before each property test
     */
    private void setupMocks(Content content, boolean updateSuccess) {
        contentService = Mockito.mock(ContentService.class);
        statisticsService = Mockito.mock(StatisticsService.class);
        
        // Mock getById to return the content
        when(contentService.getById(content.getId())).thenReturn(content);
        
        // Mock updateStatus to return the specified success value
        when(contentService.updateStatus(eq(content.getId()), any(ContentStatusEnum.class)))
            .thenReturn(updateSuccess);
        
        // Create the handler with mocked dependencies
        contentAuditHandler = new ContentAuditHandler(contentService, statisticsService);
        
        // Create the context with the handler
        auditHandlerContext = new AuditHandlerContext(List.of(contentAuditHandler));
    }

    /**
     * **Feature: content-service-refactor, Property 1: 内容审核通过后状态一致性**
     * **Validates: Requirements 2.2**
     * 
     * For any pending content, when audit is approved:
     * - The handler should call updateStatus with PUBLISHED status
     * - If updateStatus succeeds, statistics should be created
     * - The handler should return true
     */
    @Property(tries = 100)
    void contentApprovalShouldSetPublishedStatusAndCreateStatistics(
            @ForAll("pendingContents") Content pendingContent) {
        
        // Given: A content in PENDING status exists and update will succeed
        setupMocks(pendingContent, true);
        
        // When: The content audit handler approves the content
        AuditHandler handler = auditHandlerContext.getHandler(TypeEnum.CONTENT);
        boolean success = handler.onApproved(pendingContent.getId());
        
        // Then: The operation should succeed
        assertThat(success).isTrue();
        
        // And: updateStatus should be called with PUBLISHED status
        verify(contentService).updateStatus(pendingContent.getId(), ContentStatusEnum.PUBLISHED);
        
        // And: Statistics should be created for this content
        verify(statisticsService).addContentStatistics(pendingContent.getId());
    }

    /**
     * **Feature: content-service-refactor, Property 1 (edge case): 审核通过但更新失败时不创建统计**
     * **Validates: Requirements 2.2**
     * 
     * For any pending content, when audit approval fails due to optimistic lock:
     * - The handler should return false
     * - Statistics should NOT be created
     */
    @Property(tries = 100)
    void contentApprovalFailureShouldNotCreateStatistics(
            @ForAll("pendingContents") Content pendingContent) {
        
        // Given: A content in PENDING status exists but update will fail (optimistic lock)
        setupMocks(pendingContent, false);
        
        // When: The content audit handler tries to approve the content
        AuditHandler handler = auditHandlerContext.getHandler(TypeEnum.CONTENT);
        boolean success = handler.onApproved(pendingContent.getId());
        
        // Then: The operation should fail
        assertThat(success).isFalse();
        
        // And: updateStatus should be called with PUBLISHED status
        verify(contentService).updateStatus(pendingContent.getId(), ContentStatusEnum.PUBLISHED);
        
        // And: Statistics should NOT be created
        verify(statisticsService, never()).addContentStatistics(any());
    }

    /**
     * **Feature: content-service-refactor, Property 2: 内容审核拒绝后状态一致性**
     * **Validates: Requirements 2.3**
     * 
     * For any pending content, when audit is rejected:
     * - The handler should call updateStatus with DRAFT status
     * - The handler should return true if update succeeds
     */
    @Property(tries = 100)
    void contentRejectionShouldSetDraftStatus(
            @ForAll("pendingContents") Content pendingContent,
            @ForAll("rejectionReasons") String reason) {
        
        // Given: A content in PENDING status exists and update will succeed
        setupMocks(pendingContent, true);
        
        // When: The content audit handler rejects the content
        AuditHandler handler = auditHandlerContext.getHandler(TypeEnum.CONTENT);
        boolean success = handler.onRejected(pendingContent.getId(), reason);
        
        // Then: The operation should succeed
        assertThat(success).isTrue();
        
        // And: updateStatus should be called with DRAFT status
        verify(contentService).updateStatus(pendingContent.getId(), ContentStatusEnum.DRAFT);
    }

    /**
     * **Feature: content-service-refactor, Property 2 (edge case): 审核拒绝但更新失败**
     * **Validates: Requirements 2.3**
     * 
     * For any pending content, when audit rejection fails due to optimistic lock:
     * - The handler should return false
     */
    @Property(tries = 100)
    void contentRejectionFailureShouldReturnFalse(
            @ForAll("pendingContents") Content pendingContent,
            @ForAll("rejectionReasons") String reason) {
        
        // Given: A content in PENDING status exists but update will fail (optimistic lock)
        setupMocks(pendingContent, false);
        
        // When: The content audit handler tries to reject the content
        AuditHandler handler = auditHandlerContext.getHandler(TypeEnum.CONTENT);
        boolean success = handler.onRejected(pendingContent.getId(), reason);
        
        // Then: The operation should fail
        assertThat(success).isFalse();
        
        // And: updateStatus should be called with DRAFT status
        verify(contentService).updateStatus(pendingContent.getId(), ContentStatusEnum.DRAFT);
    }

    /**
     * **Feature: content-service-refactor, Property: Handler type correctness**
     * **Validates: Requirements 2.1**
     * 
     * The ContentAuditHandler should be annotated with @AuditType(TypeEnum.CONTENT)
     * and should be retrievable from the context using TypeEnum.CONTENT
     */
    @Property(tries = 10)
    void handlerTypeShouldBeContent() {
        // Given: A content audit handler registered in the context
        setupMocks(createSampleContent(1L), true);
        
        // When: Getting the handler from context using CONTENT type
        AuditHandler handler = auditHandlerContext.getHandler(TypeEnum.CONTENT);
        
        // Then: The handler should be the ContentAuditHandler instance
        assertThat(handler).isInstanceOf(ContentAuditHandler.class);
    }

    /**
     * **Feature: content-service-refactor, Property: getCurrentStatus correctness**
     * **Validates: Requirements 2.1**
     * 
     * For any content, getCurrentStatus should return the content's current status code
     */
    @Property(tries = 100)
    void getCurrentStatusShouldReturnContentStatus(
            @ForAll("pendingContents") Content content) {
        
        // Given: A content exists
        setupMocks(content, true);
        
        // When: Getting the current status
        Integer status = contentAuditHandler.getCurrentStatus(content.getId());
        
        // Then: The status should match the content's status code
        assertThat(status).isEqualTo(content.getStatus().getCode());
    }

    private Content createSampleContent(Long id) {
        Content content = new Content();
        content.setId(id);
        content.setTitle("Test Content");
        content.setDescription("Test Description");
        content.setUploadedBy(1L);
        content.setType(ContentTypeEnum.ARTICLE);
        content.setStatus(ContentStatusEnum.PENDING);
        content.setCategoryId(1L);
        content.setVersion(0);
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        return content;
    }

    // Counter for generating unique IDs
    private static final AtomicLong idCounter = new AtomicLong(1);

    /**
     * Provides pending content instances for property testing
     */
    @Provide
    Arbitrary<Content> pendingContents() {
        Arbitrary<String> titles = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(50)
                .map(s -> "Test Content " + s);
        
        Arbitrary<String> descriptions = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(10)
                .ofMaxLength(100)
                .map(s -> "Description: " + s);
        
        Arbitrary<Long> userIds = Arbitraries.longs().between(1L, 10000L);
        
        Arbitrary<ContentTypeEnum> types = Arbitraries.of(ContentTypeEnum.values());
        
        return Combinators.combine(titles, descriptions, userIds, types)
                .as((title, description, userId, type) -> {
                    Content content = new Content();
                    content.setId(idCounter.getAndIncrement()); // Generate unique ID
                    content.setTitle(title);
                    content.setDescription(description);
                    content.setUploadedBy(userId);
                    content.setType(type);
                    content.setStatus(ContentStatusEnum.PENDING);
                    content.setCategoryId(1L); // Default category
                    content.setVersion(0);
                    content.setCreateTime(LocalDateTime.now());
                    content.setUpdateTime(LocalDateTime.now());
                    return content;
                });
    }

    /**
     * Provides rejection reasons for property testing
     */
    @Provide
    Arbitrary<String> rejectionReasons() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(200)
                .map(s -> "Rejection reason: " + s);
    }
}
