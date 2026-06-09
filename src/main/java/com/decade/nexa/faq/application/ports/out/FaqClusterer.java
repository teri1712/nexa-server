package com.decade.nexa.faq.application.ports.out;

import java.time.LocalDate;

public interface FaqClusterer {

    void cluster(LocalDate date);

    boolean isFinish(LocalDate date);
}
