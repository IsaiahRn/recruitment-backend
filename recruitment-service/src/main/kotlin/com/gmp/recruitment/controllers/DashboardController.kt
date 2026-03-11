package com.gmp.recruitment.controllers

import com.gmp.recruitment.models.dto.dashboard.DashboardOverviewResponse
import com.gmp.recruitment.models.dto.dashboard.DashboardSummaryResponse
import com.gmp.recruitment.services.DashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController(
    private val dashboardService: DashboardService,
) {
    @GetMapping("/summary")
    fun summary(): DashboardSummaryResponse = dashboardService.summary()

    @GetMapping("/overview")
    fun overview(): DashboardOverviewResponse = dashboardService.overview()
}
