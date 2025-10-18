package com.demo.demo.DTO;

public record DealResponse(
    String restaurantObjectId,
    String restaurantName,
    String restaurantAddress1,
    String restaurantSuburb,
    String restaurantOpen,
    String restaurantClose,
    String dealObjeactId,
    String discount,
    String dineIn,
    String lightning,
    String qtyLeft
) {}
