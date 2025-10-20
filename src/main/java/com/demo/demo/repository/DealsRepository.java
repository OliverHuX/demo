package com.demo.demo.repository;

import java.util.List;

import com.demo.demo.entity.NormalisedDeal;

public interface DealsRepository {
    List<NormalisedDeal> findAllActiveDeals(String timeOfDay);
}
