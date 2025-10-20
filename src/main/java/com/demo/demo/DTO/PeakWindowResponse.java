package com.demo.demo.DTO;

import java.time.LocalTime;

public record PeakWindowResponse(
    String start,
    String end
) {}
