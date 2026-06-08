package com.decade.nexa.common;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;

public class DatasetImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
            importingClassMetadata.getAnnotationAttributes(ComponentTest.class.getName())
        );
        
        if (attributes != null && attributes.containsKey("datasets")) {
            Class<?>[] datasets = attributes.getClassArray("datasets");
            return Arrays.stream(datasets)
                .map(Class::getName)
                .toArray(String[]::new);
        }
        
        return new String[0];
    }
}
