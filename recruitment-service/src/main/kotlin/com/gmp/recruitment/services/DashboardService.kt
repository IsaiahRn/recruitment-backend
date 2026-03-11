package com.gmp.recruitment.services

import com.gmp.recruitment.models.dto.dashboard.DailySubmissionPoint
import com.gmp.recruitment.models.dto.dashboard.DashboardOverviewResponse
import com.gmp.recruitment.models.dto.dashboard.DashboardSummaryResponse
import com.gmp.recruitment.models.dto.dashboard.DecisionTrendPoint
import com.gmp.recruitment.models.dto.dashboard.RecentSubmissionPoint
import com.gmp.recruitment.models.dto.dashboard.ReviewerWorkloadPoint
import com.gmp.recruitment.models.dto.dashboard.StatusBreakdownPoint
import com.gmp.recruitment.models.dto.dashboard.UserRoleBreakdownPoint
import com.gmp.recruitment.models.enums.ApplicationStatus
import com.gmp.recruitment.repositories.ApplicantProfileRepository
import com.gmp.recruitment.repositories.ApplicationRepository
import com.gmp.recruitment.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class DashboardService(
  private val applicantProfileRepository: ApplicantProfileRepository,
  private val applicationRepository: ApplicationRepository,
  private val userRepository: UserRepository,
) {
  fun summary(): DashboardSummaryResponse =
    DashboardSummaryResponse(
      totalApplicants = applicantProfileRepository.count(),
      submitted = applicationRepository.countByStatus(ApplicationStatus.SUBMITTED),
      underReview = applicationRepository.countByStatus(ApplicationStatus.UNDER_REVIEW),
      approved = applicationRepository.countByStatus(ApplicationStatus.APPROVED),
      rejected = applicationRepository.countByStatus(ApplicationStatus.REJECTED),
      pendingVerification = applicantProfileRepository.countByNidaVerifiedFalseOrNesaVerifiedFalse(),
      totalUsers = userRepository.count(),
    )

  fun overview(): DashboardOverviewResponse =
    DashboardOverviewResponse(
      summary = summary(),
      statusBreakdown = applicationRepository.fetchStatusBreakdown().map {
        StatusBreakdownPoint(
          status = it.getStatus(),
          count = it.getCount(),
        )
      },
      dailySubmissions = applicationRepository.fetchDailySubmissions().map {
        DailySubmissionPoint(
          day = it.getDay(),
          count = it.getCount(),
        )
      },
      decisionTrend = applicationRepository.fetchDecisionTrend().map {
        DecisionTrendPoint(
          day = it.getDay(),
          approved = it.getApproved(),
          rejected = it.getRejected(),
        )
      },
      userRoleBreakdown = userRepository.fetchRoleBreakdown().map {
        UserRoleBreakdownPoint(
          role = it.getRole(),
          count = it.getCount(),
        )
      },
      reviewerWorkload = applicationRepository.fetchReviewerWorkload().map {
        ReviewerWorkloadPoint(
          reviewerName = it.getReviewerName(),
          reviewedCount = it.getReviewedCount(),
        )
      },
      recentSubmissions = applicationRepository.fetchRecentSubmissions().map {
        RecentSubmissionPoint(
          applicationId = it.getApplicationId(),
          applicationNumber = it.getApplicationNumber(),
          fullName = it.getFullName(),
          status = it.getStatus(),
          submittedAt = it.getSubmittedAt(),
        )
      },
    )
}
