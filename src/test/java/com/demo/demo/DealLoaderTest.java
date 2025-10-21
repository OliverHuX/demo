package com.demo.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.client.RestClient;

import com.demo.demo.config.UrlProperties;
import com.demo.demo.entity.NormalisedDeal;
import com.demo.demo.http.DealLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class DealLoaderTest {

    private MockWebServer mockWebServer;
    private DealLoader dealLoader;
    
    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        UrlProperties props = new UrlProperties();
        props.setUrl(mockWebServer.url("/challenge.json").toString());
        dealLoader = new DealLoader(RestClient.create(), new ObjectMapper(), props);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }
    
    @Test
    void loadAll_NormalizesDealTimes_AndParsesFields() throws JsonMappingException, JsonProcessingException {
        // Given: restaurant open 15:00–21:00
        // - D1 explicitly 14:00–22:00 → should clamp to 15:00–21:00
        // - D2 has no times → falls back to restaurant window 15:00–21:00
        // - D3 with start=16:00 only, no end → clamps end to 21:00
        // - d/l/qty parsing validated
        String payload = """
        {
          "restaurants": [
            {
              "objectId": "r1",
              "name": "eatclub",
              "address1": "fake Street",
              "suburb": "Sydney",
              "open": "3:00pm",
              "close": "9:00pm",
              "deals": [
                {
                  "objectId": "D-A",
                  "discount": "50",
                  "dineIn": "false",
                  "lightning": "true",
                  "open": "2:00pm",
                  "close": "10:00pm",
                  "qtyLeft": "5"
                },
                {
                  "objectId": "D-B",
                  "discount": "40",
                  "dineIn": "true",
                  "lightning": "false",
                  "qtyLeft": "4"
                },
                {
                  "objectId": "D-C",
                  "discount": "30",
                  "dineIn": "true",
                  "lightning": "true",
                  "start": "4:00pm"
                }
              ]
            },
            {
              "objectId": "r2",
              "name": "empty restaurant",
              "address1": "nowhere",
              "suburb": "nowhere",
              "open": "10:00",
              "close": "12:00",
              "deals": []
            }
          ]
        }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(payload));

        List<NormalisedDeal> got = dealLoader.loadAll();

        assertNotNull(got);
        assertEquals(3, got.size(), "Should flatten and return all deals from r1; r2 has none");

        // Shared restaurant info
        for (NormalisedDeal nd : got) {
            assertEquals("r1", nd.restaurantObjectId());
            assertEquals("eatclub", nd.restaurantName());
            assertEquals("fake Street", nd.restaurantAddress1());
            assertEquals("Sydney", nd.restaurantSuburb());
            assertEquals(LocalTime.of(15, 0), nd.restaurantOpen());
            assertEquals(LocalTime.of(21, 0), nd.restaurantClose());
            assertTrue(nd.start().isBefore(nd.end()), "Start should be before end after normalization");
        }

        // Deal A: explicit 14:00–22:00 but clamped to 15:00–21:00
        NormalisedDeal a = got.stream().filter(d -> d.dealObjectId().equals("D-A")).findFirst().orElseThrow();
        assertEquals(LocalTime.of(15, 0), a.start());
        assertEquals(LocalTime.of(21, 0), a.end());
        assertEquals("50", a.discount());
        assertFalse(a.dineIn());
        assertTrue(a.lightning());
        assertEquals(5, a.qtyLeft());

        // Deal B: no times → defaults to restaurant window 15:00–21:00
        NormalisedDeal b = got.stream().filter(d -> d.dealObjectId().equals("D-B")).findFirst().orElseThrow();
        assertEquals(LocalTime.of(15, 0), b.start());
        assertEquals(LocalTime.of(21, 0), b.end());
        assertEquals("40", b.discount());
        assertTrue(b.dineIn());
        assertFalse(b.lightning());
        assertEquals(4, b.qtyLeft());

        // Deal C: start=16:00, no end → end clamps to restaurantClose (21:00)
        NormalisedDeal c = got.stream().filter(d -> d.dealObjectId().equals("D-C")).findFirst().orElseThrow();
        assertEquals(LocalTime.of(16, 0), c.start());
        assertEquals(LocalTime.of(21, 0), c.end());
        assertEquals("30", c.discount());
        assertTrue(c.dineIn());
        assertTrue(c.lightning());
        assertEquals(0, c.qtyLeft(), "Missing qtyLeft should default to 0");
    }

    @Test
    void loadAll_EmptyRestaurantsArray_ReturnsEmptyList() throws Exception {
        String payload = """
        { "restaurants": [] }
        """;
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(payload));

        List<NormalisedDeal> got = dealLoader.loadAll();
        assertNotNull(got);
        assertTrue(got.isEmpty(), "No restaurants → no deals");
    }
}
