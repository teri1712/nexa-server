package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.FaqResponse;
import com.decade.nexa.faq.application.query.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequiredArgsConstructor
public class FaqController {
    final FaqService faqService;

    @GetMapping("/faqs")
    public List<FaqResponse> faqs() {
        return faqService.list();
    }
}
