package com.finance.app.dashboard;

import com.finance.app.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.dashboard(userId)));
    }

    @GetMapping("/expenses-by-category")
    public ResponseEntity<ApiResponse<List<DashboardResponse.ExpenseByCategory>>> expensesByCategory(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate now = LocalDate.now();
        if (from == null) from = now.withDayOfMonth(1);
        if (to == null) to = now;
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.expensesByCategory(userId, from, to)));
    }

    @GetMapping("/monthly-evolution")
    public ResponseEntity<ApiResponse<List<DashboardResponse.MonthlyEvolution>>> monthlyEvolution(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate now = LocalDate.now();
        if (from == null) from = now.withDayOfYear(1);
        if (to == null) to = now;
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.monthlyEvolution(userId, from, to)));
    }
}
