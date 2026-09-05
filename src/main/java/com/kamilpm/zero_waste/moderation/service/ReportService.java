package com.kamilpm.zero_waste.moderation.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.blog.api.BlogReportApi;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.exception.BadRequestException;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.item.api.ItemReportApi;
import com.kamilpm.zero_waste.moderation.api.RejectReportEvent;
import com.kamilpm.zero_waste.moderation.dto.ReportDto;
import com.kamilpm.zero_waste.moderation.dto.ReportRequest;
import com.kamilpm.zero_waste.moderation.dto.ResolveReportRequest;
import com.kamilpm.zero_waste.moderation.entity.Report;
import com.kamilpm.zero_waste.moderation.entity.ReportStatus;
import com.kamilpm.zero_waste.moderation.entity.ReportSubjectType;
import com.kamilpm.zero_waste.moderation.mapper.ReportMapper;
import com.kamilpm.zero_waste.moderation.repository.ReportRepository;
import com.kamilpm.zero_waste.review.api.ReviewReportApi;
import com.kamilpm.zero_waste.user.api.UserReportApi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
  private final AuthApi authApi;
  private final ReportRepository reportRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final ReportMapper reportMapper;
  private final ReviewReportApi reviewReportApi;
  private final ItemReportApi itemReportApi;
  private final UserReportApi userReportApi;
  private final BlogReportApi blogReportApi;
  // private final RefreshTokenRepository refreshTokenRepository;
  // private final UserBanRepository userBanRepository;

  @Transactional
  public void createReport(ReportRequest reportRequest) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    validateSubjectExists(user, reportRequest.subjectType(), reportRequest.subjectId());

    if (reportRepository.existsByReporterIdAndSubjectId(user.id(),
        reportRequest.subjectId())) {
      throw new ForbiddenException(
          "You have already made a report on this " +
              reportRequest.subjectType().toString().toLowerCase());
    }

    Report report = Report.builder()
        .reporterId(user.id())
        .subjectType(reportRequest.subjectType())
        .subjectId(reportRequest.subjectId())
        .reason(reportRequest.reason())
        .comment(reportRequest.comment())
        .build();

    Report savedReport = reportRepository.save(report);
    simpMessagingTemplate.convertAndSend("/topic/reports", savedReport);
  }

  private void validateSubjectExists(AuthenticatedUser user, ReportSubjectType type, UUID subjectId) {
    switch (type) {
      case USER -> userReportApi.userExists(subjectId, user.id());
      case BLOG -> blogReportApi.blogExists(subjectId, user.id());
      case ITEM -> itemReportApi.itemExists(subjectId, user.id());
      case REVIEW -> reviewReportApi.reviewExists(subjectId, user.id());
    }
    ;
  }

  public List<ReportDto> getReports() {
    List<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc();

    Map<UUID, UserSummaryDto> usersById = userReportApi.getUsersByIds(
        reports.stream().flatMap(report -> List.of(report.getReporterId(), report.getResolvedBy()).stream())
            .collect(Collectors.toSet()));

    return reports.stream()
        .map((report) -> reportMapper.toDto(report,
            usersById.get(report.getReporterId()),
            report.getResolvedBy() == null ? null : usersById.get(report.getResolvedBy())))
        .toList();
  }

  @Transactional
  public void rejectReport(UUID reportId, String adminNote) {
    AuthenticatedUser admin = authApi.getRequiredAuthenticatedUser();

    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("Report not found"));

    if (report.getStatus() != ReportStatus.PENDING) {
      throw new ConflictException("Report has already been processed");
    }

    if (Objects.equals(report.getReporterId(), admin.id())) {
      throw new ForbiddenException("You cannot moderate your own reports");
    }

    validateAdminCanModerate(admin.id(), report);
    report.setStatus(ReportStatus.REJECTED);
    report.setResolvedBy(admin.id());
    report.setResolvedAt(Instant.now());
    report.setAdminNote(adminNote);

    reportRepository.save(report);

  }

  private void validateAdminCanModerate(UUID userId, Report report) {
    boolean isOwn = switch (report.getSubjectType()) {
      case USER -> Objects.equals(userId, report.getSubjectId());
      case ITEM -> itemReportApi.isItemOwner(report.getSubjectId(), userId);
      case BLOG -> blogReportApi.isBlogAuthor(report.getSubjectId(), userId);
      case REVIEW -> reviewReportApi.isReviewerOrReviewee(report.getSubjectId(), userId);
    };

    if (isOwn) {
      throw new ForbiddenException("You cannot moderate a report against yourself");
    }

  }

  @Transactional
  public void resolveReport(UUID reportId, ResolveReportRequest resolveRequest) {

    AuthenticatedUser admin = authApi.getRequiredAuthenticatedUser();

    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("Report not found"));

    if (report.getStatus() != ReportStatus.PENDING) {
      throw new ConflictException("Report has already been processed");
    }

    if (Objects.equals(report.getReporterId(), admin.id())) {
      throw new ForbiddenException("You cannot moderate your own reports");
    }

    validateAdminCanModerate(admin.id(), report);

    switch (resolveRequest.action()) {
      case REMOVE -> removeReportedEntity(report);
      case HIDE -> hideReportedEntity(admin.id(), report);
      case BAN -> banReportedUser(admin.id(), report, resolveRequest);
      default -> throw new BadRequestException("Action not supported");
    }
    ;

    report.setStatus(ReportStatus.RESOLVED);
    report.setResolvedBy(admin.id());
    report.setResolvedAt(Instant.now());
    report.setAdminNote(resolveRequest.adminNote());

    reportRepository.save(report);
    rejectAllBySubjectId(report.getSubjectId(), true);
  }

  private void removeReportedEntity(Report report) {
    switch (report.getSubjectType()) {
      case ITEM -> itemReportApi.deleteItemById(report.getSubjectId());
      case BLOG -> blogReportApi.deleteBlogById(report.getSubjectId());
      case REVIEW -> reviewReportApi.deleteReviewById(report.getSubjectId());
      default -> throw new BadRequestException("Users cannot be removed using REMOVE action");
    }
    ;
  }

  private void banReportedUser(UUID adminId, Report report, ResolveReportRequest resolveRequest) {
    if (!Objects.equals(report.getSubjectType(), ReportSubjectType.USER)) {

      throw new BadRequestException("BAN action can only be used for users");
    }

    userReportApi.banUser(adminId, report.getSubjectId(), resolveRequest.adminNote());
  }

  private void hideReportedEntity(UUID adminId, Report report) {

    switch (report.getSubjectType()) {
      case ITEM -> itemReportApi.hideItem(adminId, report.getSubjectId());
      case BLOG -> blogReportApi.hideBlog(adminId, report.getSubjectId());
      case REVIEW -> reviewReportApi.hideReview(adminId, report.getSubjectId());
      default -> throw new BadRequestException("Users cannot be hidden using HIDE action");
    }
    ;
  }

  public void rejectAllBySubjectId(UUID subjectId, boolean isAdmin) {
    List<Report> reports = reportRepository.findBySubjectIdAndStatus(subjectId, ReportStatus.PENDING);
    for (Report report : reports) {
      report.setAdminNote(isAdmin ? "Content was deleted by admin." : "Content was deleted by its owner.");
      report.setResolvedAt(Instant.now());
      report.setStatus(ReportStatus.RESOLVED);
    }
    reportRepository.saveAll(reports);
  }

  @ApplicationModuleListener
  void on(RejectReportEvent event) {
    rejectAllBySubjectId(event.subjectId(), event.isAdmin());
  }
}
