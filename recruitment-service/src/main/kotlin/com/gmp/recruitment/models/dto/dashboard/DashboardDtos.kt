package com.gmp.recruitment.models.dto.dashboard

import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class DashboardSummaryResponse(
  val totalApplicants: Long,
  val submitted: Long,
  val underReview: Long,
  val approved: Long,
  val rejected: Long,
  val pendingVerification: Long,
  val totalUsers: Long,
) : Serializable

data class StatusBreakdownPoint(
  val status: String,
  val count: Long,
) : Serializable

data class DailySubmissionPoint(
  val day: LocalDate,
  val count: Long,
) : Serializable

data class DecisionTrendPoint(
  val day: LocalDate,
  val approved: Long,
  val rejected: Long,
) : Serializable

data class UserRoleBreakdownPoint(
  val role: String,
  val count: Long,
) : Serializable

data class ReviewerWorkloadPoint(
  val reviewerName: String,
  val reviewedCount: Long,
) : Serializable

data class RecentSubmissionPoint(
  val applicationId: UUID,
  val applicationNumber: String,
  val fullName: String,
  val status: String,
  val submittedAt: Instant,
) : Serializable

data class DashboardOverviewResponse(
  val summary: DashboardSummaryResponse,
  val statusBreakdown: List<StatusBreakdownPoint>,
  val dailySubmissions: List<DailySubmissionPoint>,
  val decisionTrend: List<DecisionTrendPoint>,
  val userRoleBreakdown: List<UserRoleBreakdownPoint>,
  val reviewerWorkload: List<ReviewerWorkloadPoint>,
  val recentSubmissions: List<RecentSubmissionPoint>,
) : Serializable
