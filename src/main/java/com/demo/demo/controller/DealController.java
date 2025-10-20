package com.demo.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.demo.DTO.DealResponse;
import com.demo.demo.service.DealService;

import lombok.Data;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Data
@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;
    
    @GetMapping("")
    public ResponseEntity<List<DealResponse>> getDeals(@RequestParam String timeOfDay) {
        return ResponseEntity.ok(dealService.getDealsActiveAt(timeOfDay));
    }
    
    
}
