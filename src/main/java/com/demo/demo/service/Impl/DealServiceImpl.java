package com.demo.demo.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.demo.demo.DTO.DealResponse;
import com.demo.demo.repository.DealsRepository;
import com.demo.demo.service.DealService;

import lombok.Data;

@Data
@Service
public class DealServiceImpl implements DealService{

    private final DealsRepository dealsRepository;

    @Override
    public List<DealResponse> getDealsActiveAt(String timeOfDay) {
        
        return dealsRepository.findAllActiveDeals(timeOfDay).stream()
                .map(deal -> new DealResponse(
                        deal.restaurantObjectId(),
                        deal.restaurantName(),
                        deal.restaurantAddress1(),
                        deal.restaurantSuburb(),
                        deal.restaurantOpen(),
                        deal.restaurantClose(),
                        deal.dealObjectId(),
                        deal.discount(),
                        deal.dineIn(),
                        deal.lightning(),
                        deal.qtyLeft()
                ))
                .toList();
        
    }

}
