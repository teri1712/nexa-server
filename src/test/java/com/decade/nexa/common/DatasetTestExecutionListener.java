package com.decade.nexa.common;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

public class DatasetTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        List<TestDataset> datasets = getDatasets(testContext);
        datasets.forEach(TestDataset::setup);
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        List<TestDataset> datasets = getDatasets(testContext);
        datasets.forEach(TestDataset::clean);
    }

    private List<TestDataset> getDatasets(TestContext testContext) {
        List<TestDataset> datasetBeans = new ArrayList<>();
        ComponentTest annotation = MergedAnnotations.from(testContext.getTestClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
            .get(ComponentTest.class)
            .synthesize();
        
        if (annotation != null) {
            ApplicationContext context = testContext.getApplicationContext();
            for (Class<? extends TestDataset> datasetClass : annotation.datasets()) {
                datasetBeans.add(context.getBean(datasetClass));
            }
        }
        return datasetBeans;
    }
}
