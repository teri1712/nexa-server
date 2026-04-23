package com.decade.nexa.documents.application.ports.out;

import reactor.core.publisher.Flux;

public interface SuggestService {
      Flux<String> suggest(String query);
}
