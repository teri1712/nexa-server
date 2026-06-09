package com.decade.nexa.faq.application.ports.out;

import java.time.LocalDate;

public interface FaqClusterer {

    void cluster(Long requestId, LocalDate date);

    boolean isFinish(Long requestId);
}
