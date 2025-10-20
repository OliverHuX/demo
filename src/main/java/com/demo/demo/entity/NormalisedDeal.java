package com.demo.demo.entity;

import java.time.LocalTime;

public record NormalisedDeal(
    String restaurantObjectId,
    String restaurantName,
    String restaurantAddress1,
    String restaurantSuburb,
    LocalTime start,
    LocalTime end,
    String dealObjectId,
    String discount,
    boolean dineIn,
    boolean lightning,
    int qtyLeft,
    LocalTime restaurantOpen,
    LocalTime restaurantClose
) {}
