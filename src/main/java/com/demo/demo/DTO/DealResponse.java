package com.demo.demo.DTO;

import java.time.LocalTime;

public record DealResponse(
    String restaurantObjectId,
    String restaurantName,
    String restaurantAddress1,
    String restaurantSuburb,
    String restaurantOpen,
    String restaurantClose,
    String dealObjeactId,
    String discount,
    boolean dineIn,
    boolean lightning,
    int qtyLeft
) {}
