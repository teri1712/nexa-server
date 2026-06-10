package com.decade.nexa.faq.unit;

import com.decade.nexa.faq.application.FaqClusterManagement;
import com.decade.nexa.faq.application.ports.out.ClusterLogRepository;
import com.decade.nexa.faq.application.ports.out.FaqClusterer;
import com.decade.nexa.faq.domain.ClusterLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaqClusterManagementTest {

    @Mock
    FaqClusterer clusterer;

    @Mock
    ClusterLogRepository logs;

    @InjectMocks
    FaqClusterManagement management;

    @Test
    void shouldPrepareLog() {
        LocalDate today = LocalDate.now();
        management.prepare(today);
        verify(logs).save(any(ClusterLog.class));
    }

    @Test
    void shouldTriggerClustering() {
        LocalDate today = LocalDate.now();
        ClusterLog log = new ClusterLog(today);
        // We can't easily mock the id because it's DB generated, 
        // but in this test we can just assume it's null or we can manually set it if there is a setter or via reflection if needed.
        // Actually, ClusterLog has a getRequestId() which returns id. 
        // For testing purposes, we might need to use a spy or just rely on the fact that it's a Long now.
        when(logs.findByClusterDate(today)).thenReturn(Optional.of(log));

        management.cluster(today);

        verify(clusterer).cluster(eq(log.getRequestId()), eq(today));
        verify(logs).save(log);
    }

    @Test
    void shouldCheckProgress() {
        LocalDate today = LocalDate.now();
        ClusterLog log = new ClusterLog(today);
        when(logs.findByClusterDate(today)).thenReturn(Optional.of(log));
        when(clusterer.isFinish(log.getRequestId())).thenReturn(true);

        management.check(today);

        verify(logs).save(log);
        // Note: markAsCompleted generates an event, but here we just verify save
    }
}
