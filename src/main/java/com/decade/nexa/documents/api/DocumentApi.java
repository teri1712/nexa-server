package com.decade.nexa.documents.api;

import java.util.Map;
import java.util.Set;

public interface DocumentApi {

    Map<String, DocInfo> find(Set<String> ids);

}
