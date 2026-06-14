package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.LogManagement;
import com.decade.nexa.documents.dto.IndexLogResponse;
import com.decade.nexa.documents.dto.IndexMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDate;

@RestController
@RequestMapping("/index-logs")
@RequiredArgsConstructor
public class IndexController {

    final LogManagement management;
    final IndexMapper mapper;

    @GetMapping
    public Page<IndexLogResponse> list(Pageable pageable) {
        return management.list(pageable).map(mapper::map);
    }

    @GetMapping("/{date}")
    public IndexLogResponse find(@PathVariable LocalDate date) {
        return mapper.map(management.find(date));
    }


    @PostMapping(value = "/trigger")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void trigger() {
        LocalDate today = LocalDate.now();
        management.prepare(today);
        management.index(today);
        Flux.interval(Duration.ofSeconds(1))
            .startWith(0L)
            .takeUntil(i -> management.check(today))
            .doOnComplete(() -> management.deadline(today))
            .subscribe();
    }

}
