package com.gmp.recruitment.repositories

import com.gmp.recruitment.models.entities.UserEntity
import com.gmp.recruitment.models.enums.UserStatus
import com.gmp.recruitment.repositories.projections.UserRoleCountProjection
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByUsernameIgnoreCase(username: String): Optional<UserEntity>
    fun existsByUsernameIgnoreCase(username: String): Boolean
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun countByStatus(status: UserStatus): Long

    @Query(
        value = """
        SELECT role AS role, COUNT(*) AS count
        FROM users
        GROUP BY role
        ORDER BY role
        """,
        nativeQuery = true
    )
    fun fetchRoleBreakdown(): List<UserRoleCountProjection>
}
