package com.decade.nexa.faq.adapters;

import com.decade.nexa.faq.application.FaqClusterManagement;
import com.decade.nexa.faq.dto.ClusterLogResponse;
import com.decade.nexa.faq.dto.ClusterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDate;

@RestController
@RequestMapping("/cluster-logs")
@RequiredArgsConstructor
public class ClusterController {

    final FaqClusterManagement management;
    final ClusterMapper mapper;

    @GetMapping
    public Page<ClusterLogResponse> list(Pageable pageable) {
        return management.list(pageable).map(mapper::map);
    }

    @GetMapping("/{date}")
    public ClusterLogResponse find(@PathVariable LocalDate date) {
        return mapper.map(management.find(date));
    }


    @PostMapping(value = "/trigger", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streaming() {
        LocalDate today = LocalDate.now();
        return Flux.interval(Duration.ofSeconds(1))
            .map(i -> {
                if (i == 0) {
                    management.prepare(today);
                    return "Preparing for clustering\n";
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
