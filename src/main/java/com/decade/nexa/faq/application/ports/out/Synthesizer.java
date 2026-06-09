package com.decade.nexa.faq.application.ports.out;

import com.decade.nexa.faq.domain.FAQ;

import java.util.List;

public interface Synthesizer {
    List<String> synthesize(List<FAQ> clusters);
}
