package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.FaqResponse;
import com.decade.nexa.faq.application.FAQManagement;
import com.decade.nexa.faq.application.query.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/faqs")
public class FaqController {
    final FaqService faqService;
    final FAQManagement faqManagement;

    @Operation(summary = "Get today FAQ list")
    @GetMapping
    List<FaqResponse> faqs() {
        return faqService.list();
    }

    @PostMapping("/trigger")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void trigger() {
        faqManagement.cluster();
    }

}
