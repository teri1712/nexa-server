package com.decade.nexa.documents.unit;

import com.decade.nexa.documents.application.LogManagement;
import com.decade.nexa.documents.application.ports.out.KnowledgeEngineGraph;
import com.decade.nexa.documents.application.ports.out.LogRepository;
import com.decade.nexa.documents.domain.IndexLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GraphManagementTest {

    @Mock
    KnowledgeEngineGraph graph;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    LogRepository logs;

    @InjectMocks
    LogManagement graphManagement;


    @Test
    void givenLogExistHaventRunningYet_whenIndexing_thenLogMustBeInRunningState() {
        LocalDate today = LocalDate.now();
        IndexLog log = spy(new IndexLog(today));

        when(logs.findByIndexDate(eq(today)))
            .thenReturn(Optional.of(log));

        graphManagement.index(today);


        verify(log).markAsRunning();
        verify(logs, times(1)).save(eq(log));
    }


    @Test
    void givenRunning_whenCheckWithFinishStatus_thenLogMustBeCompleted() {
        LocalDate today = LocalDate.now();

        when(graph.isFinished(any()))
            .thenReturn(false)
            .thenReturn(true);

        var log = new IndexLog(today);
        when(logs.findByIndexDate(eq(today)))
            .thenReturn(Optional.of(log));

        graphManagement.check(today);
        graphManagement.check(today);

        verify(graph, times(2)).isFinished(eq(log.getRequestId()));
        verify(logs, times(1)).save(eq(log));

    }
}
