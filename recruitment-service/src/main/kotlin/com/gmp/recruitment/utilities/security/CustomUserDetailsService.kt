package com.gmp.recruitment.utilities.security

import com.gmp.recruitment.exceptions.NotFoundException
import com.gmp.recruitment.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): AuthenticatedUser =
        userRepository.findByUsernameIgnoreCase(username)
            .map {
                AuthenticatedUser(
                    id = it.id,
                    login = it.username,
                    passwordHash = it.passwordHash,
                    fullName = it.fullName,
                    role = it.role,
                    status = it.status,
                )
            }
            .orElseThrow { UsernameNotFoundException("User not found") }

    fun loadById(id: String): AuthenticatedUser =
        userRepository.findById(java.util.UUID.fromString(id))
            .map {
                AuthenticatedUser(
                    id = it.id,
                    login = it.username,
                    passwordHash = it.passwordHash,
                    fullName = it.fullName,
                    role = it.role,
                    status = it.status,
                )
            }
            .orElseThrow { NotFoundException("User not found") }
}
