package com.decade.nexa.faq.application;

import com.decade.nexa.faq.adapters.FaqSideCar;
import com.decade.nexa.faq.application.ports.out.FaqClusterer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
public class FaqClustererImpl implements FaqClusterer {

    final FaqSideCar sideCar;
    final String faqSideCarUrl;


    public FaqClustererImpl(FaqSideCar sideCar, @Value("${faq.sidecar.url}") String faqSideCarUrl) {
        this.sideCar = sideCar;
        this.faqSideCarUrl = faqSideCarUrl;
    }


    @Override
    public void cluster(LocalDate today) {
        sideCar.cluster(today);
    }

    @Override
    public boolean isFinish(LocalDate date) {
        Map<String, String> body = sideCar.status();
        String status = body.get("status");
        String dateStr = body.get("process_date");

        if ("COMPLETED".equals(status) && dateStr != null) {
            LocalDate processedDate = LocalDate.parse(dateStr);

            if (processedDate.equals(date)) {
                log.debug("Python clustering is COMPLETED for {}. Checking if synthesis is needed...", date);
                return true;
            }
        } else if ("ERROR".equals(status)) {
            log.error("Clustering job failed on Python side for {}: {}", dateStr, body.get("error_message"));
            return true;
        }
        return false;
    }

}
