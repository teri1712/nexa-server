package com.decade.nexa.documents.application.ports.in;

import com.decade.nexa.documents.dto.DocFilter;
import com.decade.nexa.documents.dto.DocPage;

public interface SearchService {
      DocPage search(DocFilter filter);
}
