package com.decade.nexa.documents.unit;

import com.decade.nexa.documents.application.GraphManagement;
import com.decade.nexa.documents.application.ports.out.KnowledgeEngineGraph;
import com.decade.nexa.documents.application.ports.out.LogRepository;
import com.decade.nexa.documents.domain.IndexLog;
import com.decade.nexa.documents.domain.LogStatus;
import com.decade.nexa.documents.domain.events.IndexCompleted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
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
    GraphManagement graphManagement;


    @Test
    void givenLogExistHaventRunningYet_whenIndexing_thenInvokeGraphIndexAndWaitUntilFinished() {
        LocalDate today = LocalDate.now();
        IndexLog log = spy(new IndexLog(today));

        when(logs.findByIndexDate(eq(today))).thenReturn(Optional.of(log));
        when(graph.isFinished(any()))
            .thenReturn(false)
            .thenReturn(true);
        runAsync(() ->
        {
            try {
                graphManagement.prepare(today);
                graphManagement.index(today);
                graphManagement.check(today);
                graphManagement.deadline(today);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        await().atMost(Duration.ofSeconds(3))
            .untilAsserted(() -> {
                verify(graph).index(eq(log.getRequestId()));
                verify(log).markAsRunning();
                verify(log).markAsCompleted();

                verify(logs, times(2)).save(eq(log));
                verify(publisher).publishEvent(eq(new IndexCompleted(today)));
            });
    }

    @Captor
    ArgumentCaptor<IndexLog> logCaptor;

    @Test
    void givenLogDoesNotExistYet_whenIndexing_thenInvokeGraphIndexAndWaitUntilFinished() {
        LocalDate today = LocalDate.now();

        when(logs.findByIndexDate(eq(today))).thenReturn(Optional.empty());
        when(graph.isFinished(any()))
            .thenReturn(false)
            .thenReturn(true);
        runAsync(() ->
        {
            try {
                graphManagement.prepare(today);
                graphManagement.index(today);
                graphManagement.check(today);
                graphManagement.deadline(today);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        await().atMost(Duration.ofSeconds(3))
            .untilAsserted(() -> {
                verify(logs, times(2)).save(logCaptor.capture());
                IndexLog log = logCaptor.getValue();
                assertThat(log).extracting(IndexLog::getIndexDate).isEqualTo(today);
                assertThat(log).extracting(IndexLog::getStatus).isEqualTo(LogStatus.COMPLETED);
                verify(graph, times(2)).isFinished(eq(log.getRequestId()));
                verify(publisher).publishEvent(eq(new IndexCompleted(today)));
            });
    }
}
