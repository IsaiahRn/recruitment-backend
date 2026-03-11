package com.gmp.recruitment.utilities.security

import com.gmp.recruitment.models.enums.UserRole
import com.gmp.recruitment.models.enums.UserStatus
import java.util.UUID
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AuthenticatedUser(
    val id: UUID,
    private val login: String,
    private val passwordHash: String,
    val fullName: String,
    val role: UserRole,
    val status: UserStatus,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = login
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = status == UserStatus.ACTIVE
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = status == UserStatus.ACTIVE
}
