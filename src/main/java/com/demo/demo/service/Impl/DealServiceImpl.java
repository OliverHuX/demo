package com.demo.demo.service.Impl;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.demo.demo.DTO.DealResponse;
import com.demo.demo.DTO.PeakWindowResponse;
import com.demo.demo.entity.NormalisedDeal;
import com.demo.demo.repository.DealsRepository;
import com.demo.demo.service.DealService;
import com.demo.demo.utils.TimeUtils;

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
                        TimeUtils.formatTime(deal.restaurantOpen()),
                        TimeUtils.formatTime(deal.restaurantClose()),
                        deal.dealObjectId(),
                        deal.discount(),
                        deal.dineIn(),
                        deal.lightning(),
                        deal.qtyLeft()
                ))
                .toList();
        
    }


    /**
     * Get the peak deal window.
     * The algorithm uses a sweep line approach to find the time window with the maximum number of active deals.
     * @return the peak deal window
     */
    @Override
    public PeakWindowResponse getPeakDealWindow() {
        
        List<NormalisedDeal> deals = dealsRepository.findAllActiveDeals(null);
        
        if (Objects.isNull(deals) || deals.isEmpty()) {
            return new PeakWindowResponse(null, null);
        }
        
        TreeMap<LocalTime, Integer> timeCountMap = new TreeMap<>();
        LocalTime dealStart, dealEnd;
        // Build the time count map (sweep line/event)
        // Each start time increments the count, each end time decrements the count
        for (NormalisedDeal deal : deals) {
        
            dealStart = deal.start();
            dealEnd = deal.end();
            // If start/end is null, use restaurant open/close times
            if (dealStart == null) {
                dealStart = deal.restaurantOpen();
            }
            if (dealEnd == null) {
                dealEnd = deal.restaurantClose();
            }
            
            timeCountMap.merge(dealStart, 1, Integer::sum);
            timeCountMap.merge(dealEnd, -1, Integer::sum);
        }
        
        if (timeCountMap.isEmpty()) {
            return new PeakWindowResponse(null, null);
        }
        
        // current number of overlapping deals
        int overlapdeals = 0;
        // best number of overlapping deals found
        int best = -1;
        LocalTime peakStart = null;
        LocalTime peakEnd = null;
        LocalTime prevTime = null;
        
        for (Map.Entry<LocalTime, Integer> e : timeCountMap.entrySet()) {
            LocalTime currentTime = e.getKey();
            
            // Check if we have a previous time to form a window
            // If so, check if the current number of overlapping deals is the best found so far in the [prevTime, currentTime) window]
            if (Objects.nonNull(prevTime)) {
                if (overlapdeals > best) {
                    best = overlapdeals;
                    peakStart = prevTime;
                    peakEnd = currentTime;
                } else if (overlapdeals == best && best > 0) {
                    // Extend the peak end time if we have another window with the same number of overlapping deals
                    if (Objects.equals(peakEnd, prevTime)) {
                        peakEnd = currentTime;
                    }
                }
            }
            
            overlapdeals += e.getValue();
            prevTime = currentTime;
        }
        
        if (best <= 0 || peakStart == null || peakEnd == null || !peakEnd.isAfter(peakStart)) {
            return new PeakWindowResponse(null, null);
        }

        return new PeakWindowResponse(TimeUtils.formatTime(peakStart), TimeUtils.formatTime(peakEnd));
    }

}
