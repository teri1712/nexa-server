package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.application.ports.out.FaqClusterer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SidecarFaqClusterer implements FaqClusterer {

    final FaqSideCar sideCar;


    @Override
    public void cluster(Long requestId, LocalDate today) {
        sideCar.cluster(requestId, today);
    }

    @Override
    public boolean isFinish(Long requestId) {
        Map<String, String> body = sideCar.progress(requestId);
        String status = body.get("status");
        return switch (status) {
            case "COMPLETED" -> {
                log.debug("Python clustering is COMPLETED for {}. Checking if synthesis is needed...", requestId);
                yield true;
            }
            case "ERROR", "IN_COMPLETED" -> throw new RuntimeException("Clustering job failed on Python side for %s: %s".formatted(requestId, body.get("error_message")));
            default -> false;
        };
    }

}
