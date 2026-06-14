package com.decade.nexa.documents.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.DocumentDataset;
import com.decade.nexa.common.OpenAiDataset;
import com.decade.nexa.common.RedisDataset;
import com.decade.nexa.files.apis.FileApi;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@ComponentTest(datasets = {DocumentDataset.class, RedisDataset.class, GraphDataset.class, OpenAiDataset.class})
public class DocumentModuleIntegrationTest {

    @MockitoSpyBean
    FileApi fileApi;

}
