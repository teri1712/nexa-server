package com.decade.nexa.faq.adapters;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.time.LocalDate;
import java.util.Map;

public interface FaqSideCar {

    @PostExchange(value = "/cluster", contentType = "application/json")
    Map<String, String> cluster(@RequestParam
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @GetExchange("/status/{date}")
    Map<String, String> status(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
