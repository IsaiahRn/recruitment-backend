package com.gmp.recruitment.services

import com.gmp.recruitment.models.entities.UserEntity
import com.gmp.recruitment.models.enums.UserRole
import com.gmp.recruitment.repositories.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BootstrapDataSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (userRepository.count() > 0) return

        val seedPassword = passwordEncoder.encode("Password@123")
        userRepository.save(
            UserEntity(
                username = "admin",
                email = "admin@example.com",
                fullName = "System Administrator",
                passwordHash = seedPassword,
                role = UserRole.SUPER_ADMIN,
                otpEnabled = false,
            )
        )
        userRepository.save(
            UserEntity(
                username = "hr01",
                email = "hr01@example.com",
                fullName = "HR Officer",
                passwordHash = seedPassword,
                role = UserRole.HR,
                otpEnabled = false,
            )
        )
        userRepository.save(
            UserEntity(
                username = "applicant01",
                email = "applicant01@example.com",
                fullName = "Sample Applicant",
                passwordHash = seedPassword,
                role = UserRole.APPLICANT,
                otpEnabled = false,
            )
        )
    }
}
