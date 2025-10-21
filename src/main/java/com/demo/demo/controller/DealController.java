package com.demo.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.demo.DTO.DealResponse;
import com.demo.demo.DTO.PeakWindowResponse;
import com.demo.demo.service.DealService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Data
@RestController
@Tag(name = "Deal Controller", description = "APIs for retrieving deal information")
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;
    
    @GetMapping("")
    public ResponseEntity<List<DealResponse>> getDeals(@RequestParam(required = false) String timeOfDay) {
        return ResponseEntity.ok(dealService.getDealsActiveAt(timeOfDay));
    }
    
    @GetMapping("/peak-window")
    public ResponseEntity<PeakWindowResponse> getPeakWindow() {
        return ResponseEntity.ok(dealService.getPeakDealWindow());
    }
    
}
