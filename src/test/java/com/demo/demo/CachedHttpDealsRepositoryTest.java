package com.demo.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.demo.demo.entity.NormalisedDeal;
import com.demo.demo.http.DealLoader;
import com.demo.demo.repository.Impl.CachedHttpDealsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@ExtendWith(MockitoExtension.class)
public class CachedHttpDealsRepositoryTest {
    
    @Mock
    DealLoader dealLoader;
    
    private CachedHttpDealsRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new CachedHttpDealsRepository(dealLoader);
    }
    
    
    // Restaurant 15:00–21:00; three deals:
    // D1: 15:00 - 21:00
    // D2: 16:00 - 20:00
    // D3: (no start/end) - falls back to restaurant hours
    
    @Test
    void testFindAllActiveDeals() throws JsonMappingException, JsonProcessingException {
        LocalTime rOpen = LocalTime.of(15, 0);
        LocalTime rClose = LocalTime.of(21, 0);
        
        NormalisedDeal d1 = new NormalisedDeal("r1", "eatclub", "addr1", "sydney",
                LocalTime.of(15, 0), LocalTime.of(21, 0),
                "d1", "10", true, false, 5,
                rOpen, rClose);
        
        NormalisedDeal d2 = new NormalisedDeal("r1", "eatclub", "addr1", "sydney",
                LocalTime.of(16, 0), LocalTime.of(20, 0),
                "d2", "20", true, false, 3,
                rOpen, rClose);
                
        when(dealLoader.loadAll()).thenReturn(List.of(d1, d2));
        
        List<NormalisedDeal> results = repository.findAllActiveDeals("16:00");
        assertEquals(2, results.size(), "16:00 should include d1 and d2");

        results = repository.findAllActiveDeals("9:00pm");
        assertEquals(1, results.size(), "21:00 should include d1 only");
        assertEquals("d1", results.get(0).dealObjectId());

        results = repository.findAllActiveDeals("14:59");
        assertTrue(results.isEmpty(), "Before restaurant open should include none");
    }
    
    
    // D3: (no start/end)
    @Test
    void testFindAllActiveDeals_dealWithoutTimesFallsBackToRestaurantWindow() throws JsonMappingException, JsonProcessingException {
        LocalTime rOpen = LocalTime.of(15, 0);
        LocalTime rClose = LocalTime.of(21, 0);
        
        NormalisedDeal d3 = new NormalisedDeal("r1", "eatclub", "addr1", "sydney",
                null, null,
                "d3", "15", true, false, 10,
                rOpen, rClose);
                
        when(dealLoader.loadAll()).thenReturn(List.of(d3));
        
        List<NormalisedDeal> results = repository.findAllActiveDeals("16:00");
        assertEquals(1, results.size(), "16:00 should include d3");

        results = repository.findAllActiveDeals("14:00");
        assertTrue(results.isEmpty(), "Before restaurant open should include none");

        results = repository.findAllActiveDeals("21:00");
        assertEquals(1, results.size(), "At restaurant close should include d3");
    }
    
    @Test
    void testInvalidTimeFormat() throws JsonMappingException, JsonProcessingException {
        
        try {
            repository.findAllActiveDeals("2");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to load deals"), "Should throw RuntimeException on invalid time");
        }
        
    }
    
}
