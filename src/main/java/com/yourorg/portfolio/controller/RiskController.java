package com.yourorg.portfolio.controller;

import com.yourorg.portfolio.dto.RiskAnalysisDTO;
import com.yourorg.portfolio.service.RiskAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskController {
    
    private final RiskAnalysisService riskAnalysisService;
    
    @Autowired
    public RiskController(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }
    
    @GetMapping("/analyze/{userId}")
    public ResponseEntity<RiskAnalysisDTO> analyzeRisk(@PathVariable Long userId) {
        RiskAnalysisDTO analysis = riskAnalysisService.analyzeRisk(userId);
        return ResponseEntity.ok(analysis);
    }
}

