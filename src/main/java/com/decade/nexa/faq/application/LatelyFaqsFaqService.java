package com.decade.nexa.faq.application;

import com.decade.nexa.faq.FaqResponse;
import com.decade.nexa.faq.application.ports.out.FAQRepository;
import com.decade.nexa.faq.application.query.FaqService;
import com.decade.nexa.faq.domain.FAQ;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
public class LatelyFaqsFaqService implements FaqService {
    final FAQRepository faqs;

    @Override
    public List<FaqResponse> list() {
        LocalDate today = LocalDate.now();
        List<FAQ> todays = faqs.findByCreatedAt(today);
        if (!todays.isEmpty()) {
            return todays.stream().map(new Function<FAQ, FaqResponse>() {
                @Override
                public FaqResponse apply(FAQ faq) {
                    return new FaqResponse(faq.getQuestion());
                }
            }).toList();
        }
        LocalDate yesterday = today.minusDays(1);
        return faqs.findByCreatedAt(yesterday).stream().map(new Function<FAQ, FaqResponse>() {
            @Override
            public FaqResponse apply(FAQ faq) {
                return new FaqResponse(faq.getQuestion());
            }
        }).toList();
    }
}
