package com.demo.demo.repository.Impl;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.demo.demo.entity.NormalisedDeal;
import com.demo.demo.http.DealLoader;
import com.demo.demo.repository.DealsRepository;
import com.demo.demo.utils.TimeUtils;

import lombok.Data;

@Data
@Repository
public class CachedHttpDealsRepository implements DealsRepository{

    private final DealLoader dealLoader;

    @Override
    public List<NormalisedDeal> findAllActiveDeals(String timeOfDay) {
        try {
            LocalTime queryTime = TimeUtils.parseTime(timeOfDay);
            
            if (Objects.isNull(queryTime)) {
                return dealLoader.loadAll();
            }
            
            return dealLoader.loadAll().stream()
                    .filter(deal -> {
                        LocalTime dealStart = Objects.nonNull(deal.start()) ? deal.start() : deal.restaurantOpen();
                        LocalTime dealEnd = Objects.nonNull(deal.end()) ? deal.end() : deal.restaurantClose();
                        if (dealStart == null || dealEnd == null) {
                            return false;
                        }
                        return !queryTime.isBefore(dealStart) && !queryTime.isAfter(dealEnd);
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load deals", e);
        }
    }

}
