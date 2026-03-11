package com.gmp.recruitment.repositories

import com.gmp.recruitment.models.entities.ApplicationEntity
import com.gmp.recruitment.models.enums.ApplicationStatus
import com.gmp.recruitment.repositories.projections.*
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ApplicationRepository : JpaRepository<ApplicationEntity, UUID> {
    fun findByProfileUserId(userId: UUID): Optional<ApplicationEntity>

    fun countByStatus(status: ApplicationStatus): Long

    @Query(
        value = """
        WITH latest AS (
            SELECT
                a.id AS applicationId,
                a.application_number AS applicationNumber,
                CONCAT(p.first_name, ' ', p.last_name) AS fullName,
                a.status AS status,
                a.submitted_at AS submittedAt
            FROM applications a
            INNER JOIN applicant_profiles p ON p.id = a.profile_id
            WHERE a.submitted_at IS NOT NULL
            ORDER BY a.submitted_at DESC
            LIMIT 10
        )
        SELECT *
        FROM latest
        ORDER BY lower(fullName) ASC
        """,
        nativeQuery = true
    )
    fun findLatestTenAlphabetically(): List<HrApplicantRowProjection>

    @Query(
        value = """
        SELECT status AS status, COUNT(*) AS count
        FROM applications
        WHERE submitted_at IS NOT NULL
        GROUP BY status
        ORDER BY status
        """,
        nativeQuery = true
    )
    fun fetchStatusBreakdown(): List<StatusCountProjection>

    @Query(
        value = """
        SELECT CAST(submitted_at AS date) AS day, COUNT(*) AS count
        FROM applications
        WHERE submitted_at IS NOT NULL
          AND submitted_at >= CURRENT_DATE - INTERVAL '13 days'
        GROUP BY CAST(submitted_at AS date)
        ORDER BY day ASC
        """,
        nativeQuery = true
    )
    fun fetchDailySubmissions(): List<DailySubmissionProjection>

    @Query(
        value = """
        SELECT
            CAST(reviewed_at AS date) AS day,
            SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approved,
            SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected
        FROM applications
        WHERE reviewed_at IS NOT NULL
          AND reviewed_at >= CURRENT_DATE - INTERVAL '13 days'
        GROUP BY CAST(reviewed_at AS date)
        ORDER BY day ASC
        """,
        nativeQuery = true
    )
    fun fetchDecisionTrend(): List<DecisionTrendProjection>

    @Query(
        value = """
        SELECT
            u.full_name AS reviewerName,
            COUNT(a.id) AS reviewedCount
        FROM applications a
        INNER JOIN users u ON u.id = a.reviewed_by_user_id
        WHERE a.reviewed_by_user_id IS NOT NULL
        GROUP BY u.full_name
        ORDER BY reviewedCount DESC, reviewerName ASC
        """,
        nativeQuery = true
    )
    fun fetchReviewerWorkload(): List<ReviewerWorkloadProjection>

    @Query(
        value = """
        SELECT
            a.id AS applicationId,
            a.application_number AS applicationNumber,
            CONCAT(p.first_name, ' ', p.last_name) AS fullName,
            a.status AS status,
            a.submitted_at AS submittedAt
        FROM applications a
        INNER JOIN applicant_profiles p ON p.id = a.profile_id
        WHERE a.submitted_at IS NOT NULL
        ORDER BY a.submitted_at DESC
        LIMIT 10
        """,
        nativeQuery = true
    )
    fun fetchRecentSubmissions(): List<RecentSubmissionProjection>
}
