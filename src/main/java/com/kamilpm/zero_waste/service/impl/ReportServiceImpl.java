package com.kamilpm.zero_waste.service.impl;

import com.kamilpm.zero_waste.repository.BlogRepository;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.repository.RefreshTokenRepository;
import com.kamilpm.zero_waste.repository.ReportRepository;
import com.kamilpm.zero_waste.repository.ReviewRepository;
import com.kamilpm.zero_waste.repository.UserBanRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.dto.ReportDto;
import com.kamilpm.zero_waste.domain.entity.Blog;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.ModerationStatus;
import com.kamilpm.zero_waste.domain.entity.Report;
import com.kamilpm.zero_waste.domain.entity.ReportStatus;
import com.kamilpm.zero_waste.domain.entity.ReportSubjectType;
import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserBan;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.domain.mapper.ReportMapper;
import com.kamilpm.zero_waste.domain.request.ReportRequest;
import com.kamilpm.zero_waste.domain.request.ResolveReportRequest;
import com.kamilpm.zero_waste.exception.BadRequestException;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.ReportService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
  private final ReviewRepository reviewRepository;
  private final BlogRepository blogRepository;
  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final AuthService authService;
  private final ReportRepository reportRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final ReportMapper reportMapper;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserBanRepository userBanRepository;

  @Override
  @Transactional
  public void createReport(ReportRequest reportRequest) {
    User user = authService.getRequiredAuthenticatedUser();
    validateSubjectExists(user, reportRequest.subjectType(), reportRequest.subjectId());

    if (reportRepository.existsByReporter_IdAndSubjectId(user.getId(),
        reportRequest.subjectId())) {
      throw new ForbiddenException(
          "You have already made a report on this " +
              reportRequest.subjectType().toString().toLowerCase());
    }

    Report report = Report.builder()
        .reporter(user)
        .subjectType(reportRequest.subjectType())
        .subjectId(reportRequest.subjectId())
        .reason(reportRequest.reason())
        .comment(reportRequest.comment())
        .build();

    Report savedReport = reportRepository.save(report);
    simpMessagingTemplate.convertAndSend("/topic/reports", savedReport);
  }

  private void validateSubjectExists(User user, ReportSubjectType type, UUID subjectId) {
    switch (type) {
      case USER -> userExists(subjectId, user);
      case BLOG -> blogExists(subjectId, user);
      case ITEM -> itemExists(subjectId, user);
      case REVIEW -> reviewExists(subjectId, user);
    }
    ;
  }

  private void reviewExists(UUID subjectId, User user) {
    Review review = reviewRepository.findById(subjectId)
        .orElseThrow(() -> new EntityNotFoundException("Review not found"));

    if (Objects.equals(review.getReviewer().getId(), user.getId()))
      throw new ForbiddenException("You can not report yourself");

    if (!Objects.equals(review.getReviewee().getId(), user.getId()))
      throw new ForbiddenException("You can not report review not received");

    if (Objects.equals(review.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden review");

  }

  private void itemExists(UUID subjectId, User user) {
    Item item = itemRepository.findById(subjectId).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (Objects.equals(item.getOwner().getId(), user.getId()))
      throw new ForbiddenException("You can not report yourself");

    if (Objects.equals(item.getOwner().getRole(), UserRole.DEMO))
      throw new ForbiddenException("Unable to interact with demo users");

    if (Objects.equals(item.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden item");

    if (!Objects.equals(item.getState(), ItemState.AVAILABLE))
      throw new ForbiddenException("Only available items can be reported");

  }

  private void userExists(UUID subjectId, User user) {
    User reportedUser = userRepository.findById(subjectId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    if (Objects.equals(reportedUser.getId(), user.getId()))
      throw new ForbiddenException("You can not report yourself");

    if (Objects.equals(reportedUser.getRole(), UserRole.DEMO))
      throw new ForbiddenException("Unable to interact with demo users");

    if (reportedUser.isBanActive())
      throw new ForbiddenException("Unable to report banned user");

  }

  private void blogExists(UUID subjectId, User user) {
    Blog blog = blogRepository.findById(subjectId).orElseThrow(() -> new EntityNotFoundException("Blog not found"));

    if (Objects.equals(blog.getAuthor().getId(), user.getId()))
      throw new ForbiddenException("You can not report yourself");

    if (Objects.equals(blog.getAuthor().getRole(), UserRole.DEMO))
      throw new ForbiddenException("Unable to interact with demo users");

    if (Objects.equals(blog.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden blog");

  }

  @Override
  public List<ReportDto> getReports() {
    return reportRepository.findAllByOrderByCreatedAtDesc().stream().map(reportMapper::toDto).toList();
  }

  @Override
  @Transactional
  public void rejectReport(UUID reportId, String adminNote) {
    User admin = authService.getRequiredAuthenticatedUser();

    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("Report not found"));

    if (report.getStatus() != ReportStatus.PENDING) {
      throw new ConflictException("Report has already been processed");
    }

    if (Objects.equals(report.getReporter().getId(), admin.getId())) {
      throw new ForbiddenException("You cannot moderate your own reports");
    }

    validateAdminCanModerate(admin.getId(), report);
    report.setStatus(ReportStatus.REJECTED);
    report.setResolvedBy(admin);
    report.setResolvedAt(Instant.now());
    report.setAdminNote(adminNote);

    reportRepository.save(report);

  }

  private void validateAdminCanModerate(UUID userId, Report report) {
    boolean isOwn = switch (report.getSubjectType()) {
      case USER -> Objects.equals(userId, report.getSubjectId());
      case ITEM -> itemRepository.existsByIdAndOwner_Id(report.getSubjectId(), userId);
      case BLOG -> blogRepository.existsByIdAndAuthor_Id(report.getSubjectId(), userId);
      case REVIEW -> reviewRepository.existsByIdAndReviewer_Id(
          report.getSubjectId(),
          userId)
          ||
          reviewRepository.existsByIdAndReviewee_Id(
              report.getSubjectId(),
              userId);
    };

    if (isOwn) {
      throw new ForbiddenException("You cannot moderate a report against yourself");
    }

  }

  @Override
  @Transactional
  public void resolveReport(UUID reportId, ResolveReportRequest resolveRequest) {

    User admin = authService.getRequiredAuthenticatedUser();

    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("Report not found"));

    if (report.getStatus() != ReportStatus.PENDING) {
      throw new ConflictException("Report has already been processed");
    }

    if (Objects.equals(report.getReporter().getId(), admin.getId())) {
      throw new ForbiddenException("You cannot moderate your own reports");
    }

    validateAdminCanModerate(admin.getId(), report);

    switch (resolveRequest.action()) {
      case REMOVE -> removeReportedEntity(report);
      case HIDE -> hideReportedEntity(admin, report);
      case BAN -> banReportedUser(admin, report, resolveRequest);
      default -> throw new BadRequestException("Action not supported");
    }
    ;

    report.setStatus(ReportStatus.RESOLVED);
    report.setResolvedBy(admin);
    report.setResolvedAt(Instant.now());
    report.setAdminNote(resolveRequest.adminNote());

    reportRepository.save(report);
    rejectAllBySubjectId(report.getSubjectId());
  }

  private void removeReportedEntity(Report report) {
    switch (report.getSubjectType()) {
      case ITEM -> itemRepository.deleteById(report.getSubjectId());
      case BLOG -> blogRepository.deleteById(report.getSubjectId());
      case REVIEW -> reviewRepository.deleteById(report.getSubjectId());
      default -> throw new BadRequestException("Users cannot be removed using REMOVE action");
    }
    ;
  }

  private void banReportedUser(User admin, Report report, ResolveReportRequest resolveRequest) {
    if (!Objects.equals(report.getSubjectType(), ReportSubjectType.USER)) {

      throw new BadRequestException("BAN action can only be used for users");
    }

    User userToBan = userRepository.findById(report.getSubjectId())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    if (userToBan.isBanActive() && userToBan.getBannedUntil() == null) {
      throw new ConflictException("User already banned");
    }

    userToBan.setBanActive(true);
    userToBan.setBannedUntil(null);

    UserBan userBan = UserBan.builder()
        .user(userToBan)
        .reason(resolveRequest.adminNote())
        .createdAt(Instant.now())
        .bannedBy(admin)
        .expiresAt(null)
        .build();
    userRepository.save(userToBan);
    userBanRepository.save(userBan);

    refreshTokenRepository.revokeAllByUserId(userToBan.getId());

    simpMessagingTemplate.convertAndSendToUser(userToBan.getEmail(),
        "/queue/ban", Map.of("message", "You have been banned"));
  }

  private void hideReportedEntity(User admin, Report report) {

    switch (report.getSubjectType()) {
      case ITEM -> hideItem(admin, report);
      case BLOG -> hideBlog(admin, report);
      case REVIEW -> hideReview(admin, report);
      default -> throw new BadRequestException("Users cannot be hidden using HIDE action");
    }
    ;
  }

  private void hideItem(User admin, Report report) {
    Item item = itemRepository.findByIdAndModerationStatus(report.getSubjectId(), ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
    item.setModeratedAt(Instant.now());
    item.setModeratedBy(admin);
    item.setModerationStatus(ModerationStatus.HIDDEN);
    itemRepository.save(item);
  }

  private void hideBlog(User admin, Report report) {

    Blog blog = blogRepository.findByIdAndModerationStatus(report.getSubjectId(), ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Blog not found"));
    blog.setModeratedAt(Instant.now());
    blog.setModeratedBy(admin);
    blog.setModerationStatus(ModerationStatus.HIDDEN);
    blogRepository.save(blog);

  }

  private void hideReview(User admin, Report report) {

    Review review = reviewRepository.findByIdAndModerationStatus(report.getSubjectId(), ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    review.setModeratedAt(Instant.now());
    review.setModeratedBy(admin);
    review.setModerationStatus(ModerationStatus.HIDDEN);
    reviewRepository.save(review);

  }

  @Override
  public void rejectAllBySubjectId(UUID subjectId) {
    List<Report> reports = reportRepository.findBySubjectIdAndStatus(subjectId, ReportStatus.PENDING);
    for (Report report : reports) {
      report.setAdminNote("Content was deleted by its owner.");
      report.setResolvedAt(Instant.now());
      report.setStatus(ReportStatus.RESOLVED);
    }
    reportRepository.saveAll(reports);
  }
}
