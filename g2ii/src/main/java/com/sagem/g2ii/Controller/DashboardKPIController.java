package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.KPIInterventionDTO;
import com.sagem.g2ii.DTOs.KPIInventoryDTO;
import com.sagem.g2ii.DTOs.KPIPerformanceDTO;
import com.sagem.g2ii.Service.DashboardKPIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Ajustez selon la configuration CORS de votre passerelle Angular
public class DashboardKPIController {

    private final DashboardKPIService dashboardKPIService;

    @GetMapping("/kpi/interventions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIEN')")
    public ResponseEntity<KPIInterventionDTO> getInterventionKPIs() {
        return ResponseEntity.ok(dashboardKPIService.getInterventionKPIs());
    }

    @GetMapping("/kpi/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Restreint à la direction/managers
    public ResponseEntity<KPIInventoryDTO> getInventoryKPIs() {
        return ResponseEntity.ok(dashboardKPIService.getInventoryKPIs());
    }

    @GetMapping("/kpi/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<KPIPerformanceDTO> getPerformanceKPIs() {
        return ResponseEntity.ok(dashboardKPIService.getPerformanceKPIs());
    }
}