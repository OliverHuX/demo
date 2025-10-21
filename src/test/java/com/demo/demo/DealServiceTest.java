package com.demo.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.demo.demo.DTO.DealResponse;
import com.demo.demo.DTO.PeakWindowResponse;
import com.demo.demo.entity.NormalisedDeal;
import com.demo.demo.repository.DealsRepository;
import com.demo.demo.service.DealService;
import com.demo.demo.service.Impl.DealServiceImpl;

@ExtendWith(MockitoExtension.class)
public class DealServiceTest {
    
    @Mock
    DealsRepository dealsRepository;
    
    private DealService dealService;
    
    @BeforeEach
    void setUp() {
        dealService = new DealServiceImpl(dealsRepository);
    }
    
    @Test
    void testGetDealsActiveAt() {
        // Arrange
        LocalTime rOpen  = LocalTime.of(15, 0); // 15:00
        LocalTime rClose = LocalTime.of(21, 0); // 21:00

        NormalisedDeal d1 = new NormalisedDeal("r1", "eatclub", "addr1", "sydney",
                LocalTime.of(15, 0), LocalTime.of(21, 0),
                "d1", "10", true, false, 5,
                rOpen, rClose);

        when(dealsRepository.findAllActiveDeals("18:00")).thenReturn(List.of(d1));

        // Act
        List<DealResponse> got = dealService.getDealsActiveAt("18:00");

        // Assert
        assertEquals(1, got.size());
        DealResponse dr = got.get(0);
        assertEquals("r1", dr.restaurantObjectId());
        assertEquals("eatclub", dr.restaurantName());
        assertEquals("addr1", dr.restaurantAddress1());
        assertEquals("sydney", dr.restaurantSuburb());
        assertEquals("03:00 pm", dr.restaurantOpen());
        assertEquals("09:00 pm", dr.restaurantClose());
        assertEquals("d1", dr.dealObjeactId());
        assertEquals("10", dr.discount());
        assertTrue(dr.dineIn());
        assertFalse(dr.lightning());
        assertEquals(5, dr.qtyLeft());

        verify(dealsRepository).findAllActiveDeals("18:00");
        verifyNoMoreInteractions(dealsRepository);
    }
    
    @Test
    void testGetPeakDealWindow_NoDeals() {
    
        when(dealsRepository.findAllActiveDeals(null)).thenReturn(List.of());

        PeakWindowResponse window = dealService.getPeakDealWindow();

        assertEquals(null, window.start());
        assertEquals(null, window.end());

        verify(dealsRepository).findAllActiveDeals(null);
        verifyNoMoreInteractions(dealsRepository);
    }
    
    @Test
    void testGetPeakDealWindow_WithDeals() {
            
        LocalTime rOpen  = LocalTime.of(15, 0); // 15:00
        LocalTime rClose = LocalTime.of(21, 0); // 21:00

        NormalisedDeal d1 = new NormalisedDeal("r1", "eatclub", "addr1", "sydney",
                LocalTime.of(15, 0), LocalTime.of(21, 0),
                "d1", "10", true, false, 5,
                rOpen, rClose);

        NormalisedDeal d2 = new NormalisedDeal("r2", "eatclub2", "addr2", "sydney",
                LocalTime.of(16, 0), LocalTime.of(20, 0),
                "d2", "20", true, false, 3,
                rOpen, rClose);

        when(dealsRepository.findAllActiveDeals(null)).thenReturn(List.of(d1, d2));

        PeakWindowResponse window = dealService.getPeakDealWindow();

        assertEquals("04:00 pm", window.start());
        assertEquals("08:00 pm", window.end());

        verify(dealsRepository).findAllActiveDeals(null);
        verifyNoMoreInteractions(dealsRepository);
    }
}
