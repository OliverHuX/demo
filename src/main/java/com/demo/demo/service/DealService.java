package com.demo.demo.service;

import java.util.List;

import com.demo.demo.DTO.DealResponse;

public interface DealService {
    List<DealResponse> getDealsActiveAt(String timeOfDay);
}
