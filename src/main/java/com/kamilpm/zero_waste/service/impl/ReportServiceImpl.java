package com.kamilpm.zero_waste.service.impl;

import com.kamilpm.zero_waste.repository.BlogRepository;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.repository.ReportRepository;
import com.kamilpm.zero_waste.repository.ReviewRepository;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.Report;
import com.kamilpm.zero_waste.domain.entity.ReportSubjectType;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.ReportRequest;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.ReportService;

import jakarta.persistence.EntityNotFoundException;
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

  @Override
  @Transactional
  public void createReport(ReportRequest reportRequest) {
    User user = authService.getRequiredAuthenticatedUser();
    validateSubjectExists(user.getId(), reportRequest.subjectType(), reportRequest.subjectId());

    // if (reportRepository.existsByReporter_IdAndSubjectId(user.getId(),
    // reportRequest.subjectId())) {
    // throw new ForbiddenException(
    // "You have already made a report on this " +
    // reportRequest.subjectType().toString().toLowerCase());
    // }

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

  private void validateSubjectExists(UUID userId, ReportSubjectType type, UUID subjectId) {

    boolean isValid = switch (type) {
      case USER -> userRepository.existsByIdAndIdNot(subjectId, userId);
      case BLOG -> blogRepository.existsByIdAndAuthor_IdNot(subjectId, userId);
      case ITEM -> itemRepository.existsByIdAndOwner_IdNot(subjectId, userId);
      case REVIEW -> reviewRepository.existsByIdAndReviewer_IdNotAndReviewee_Id(subjectId, userId, userId);
      default -> false;
    };

    if (!isValid) {
      throw new EntityNotFoundException("Subject doesn't exists or can not report yourself");

    }

  }

}
