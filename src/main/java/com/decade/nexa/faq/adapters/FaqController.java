package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.FaqResponse;
import com.decade.nexa.faq.application.FaqClusterManagement;
import com.decade.nexa.faq.application.query.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FaqController {
    final FaqService faqService;
    final FaqClusterManagement management;

    @Operation(summary = "Get today FAQ list")
    @GetMapping("/faqs")
    public List<FaqResponse> faqs() {
        return faqService.list();
    }

    @PostMapping("/faqs/cluster")
    public Flux<String> cluster() {
        LocalDate today = LocalDate.now();
        return Flux.interval(Duration.ofSeconds(1))
            .map(i -> {
                if (i == 0) {
                    management.prepare(today);
                    return "Preparing cluster...\n";
                } else if (i == 1) {
                    management.cluster(today);
                    return "Clustering Started\n";
                }
                return "Clustering is in progress at " + (i - 1) + "th seconds.\n";
            })
            .takeUntil(i -> management.check(today))
            .doOnComplete(() -> management.deadline(today));
    }
}
