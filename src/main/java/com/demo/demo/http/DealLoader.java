package com.demo.demo.http;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.demo.demo.config.UrlProperties;
import com.demo.demo.entity.NormalisedDeal;
import com.demo.demo.utils.TimeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@Component
public class DealLoader {
    
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final UrlProperties urlProperties;
    
    public List<NormalisedDeal> loadAll() throws JsonMappingException, JsonProcessingException {
        String url = urlProperties.getUrl();
        String body = restClient.get().uri(url).retrieve().body(String.class);
        
        JsonNode root = objectMapper.readTree(body);
        List<NormalisedDeal> deals = new ArrayList<>();
        JsonNode restaurantNode = root.get("restaurants");
        if (Objects.nonNull(restaurantNode)) {
            for (JsonNode restaurant : restaurantNode) {
                deals.addAll(parseRestaurant(restaurant));
            }
        }
        return deals;
    }
    
    private String text(JsonNode dealNode, String key) {
        return dealNode.has(key) ? dealNode.get(key).asText() : null;
    }
    
    private List<NormalisedDeal> parseRestaurant(JsonNode dealNode) {
        
        JsonNode deals = dealNode.get("deals");
        if (deals.size() == 0) {
            return List.of();
        }
        
        String restaurantObjectId = text(dealNode, "objectId");
        String restaurantName = text(dealNode, "name");
        String restaurantAddress1 = text(dealNode, "address1");
        String restarantSuburb = text(dealNode, "suburb");
        String rOpenRaw = text(dealNode, "open");
        String rCloseRaw = text(dealNode, "close");

        LocalTime restaurantOpen = TimeUtils.parseTime(rOpenRaw);
        LocalTime restaurantClose = TimeUtils.parseTime(rCloseRaw);
        
        List<NormalisedDeal> normalisedDeals = new ArrayList<>();
        
        for (JsonNode deal : deals) {
            String dealObjectId = text(deal, "objectId");
            String discount = text(deal, "discount");
            
            String startRaw = text(deal, "start");
            String endRaw = text(deal, "end");
            
            String openRaw = text(deal, "open");
            String closeRaw = text(deal, "close");
            
            LocalTime start = Objects.nonNull(startRaw) ? TimeUtils.parseTime(startRaw) : TimeUtils.parseTime(openRaw);
            // Ensure deal start is not before restaurant open
            start = start.isBefore(restaurantOpen) ? restaurantOpen : start;
            
            LocalTime end = Objects.nonNull(endRaw) ? TimeUtils.parseTime(endRaw) : TimeUtils.parseTime(closeRaw);
            // Ensure deal end is not after restaurant close
            end = end.isAfter(restaurantClose) ? restaurantClose : end;
            
            boolean dineIn = deal.has("dineIn") && deal.get("dineIn").asBoolean();
            boolean lightning = deal.has("lightning") && deal.get("lightning").asBoolean();
            int qtyLeft = deal.has("qtyLeft") ? deal.get("qtyLeft").asInt() : 0;
            
            NormalisedDeal normalisedDeal = new NormalisedDeal(
                restaurantObjectId,
                restaurantName,
                restaurantAddress1,
                restarantSuburb,
                start,
                end,
                dealObjectId,
                discount,
                dineIn,
                lightning,
                qtyLeft,
                restaurantOpen,
                restaurantClose
            );
            
            normalisedDeals.add(normalisedDeal);
        }
        
        return normalisedDeals;
        
    }
    
}
