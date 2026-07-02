package com.example.ai_agent.useCases;

import com.example.ai_agent.models.CreditAnalysisStage;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class CreditAnalysisOrchestrator {

    @Autowired
    private CreditAnalysisSessionService sessionService;

    @Autowired
    private CollectCreditDataUseCase collectCreditDataUseCase;

    @Autowired
    private FetchCpfScoreUseCase fetchCpfScoreUseCase;

    @Autowired
    private AnalyzeCreditUseCase analyzeCreditUseCase;

    public Flux<String> execute(final String sessionId, final String command) {
        return switch (sessionService.getStage(sessionId)) {
            case COLLECT_DATA    -> onCollectData(sessionId, command);
            case FETCH_CPF_SCORE -> onFetchCpfScore(sessionId, command);
            case ANALYZE         -> onAnalyze(sessionId, command);
            case COMPLETE        -> onComplete();
        };
    }

    private Flux<String> onCollectData(final String sessionId, final String command) {
        return collectCreditDataUseCase.execute(sessionId, command)
                .concatWith(Flux.defer(() -> {
                    if (!CreditAnalysisStage.COLLECT_DATA.equals(sessionService.getStage(sessionId))) {
                        return execute(sessionId, command);
                    }
                    return Flux.empty();
                }));
    }

    private Flux<String> onFetchCpfScore(final String sessionId, final String command) {
        return fetchCpfScoreUseCase.execute(sessionId)
                .concatWith(Flux.defer(() -> {
                    if (!CreditAnalysisStage.FETCH_CPF_SCORE.equals(sessionService.getStage(sessionId))) {
                        return execute(sessionId, command);
                    }
                    return Flux.empty();
                }));
    }

    private Flux<String> onAnalyze(final String sessionId, final String command) {
        return analyzeCreditUseCase.execute(sessionId)
                .concatWith(Flux.defer(() -> {
                    if (!CreditAnalysisStage.ANALYZE.equals(sessionService.getStage(sessionId))) {
                        return execute(sessionId, command);
                    }
                    return Flux.empty();
                }));
    }

    private Flux<String> onComplete() {
        return Flux.just("A análise de crédito já foi concluída. Inicie uma nova sessão para uma nova solicitação.");
    }
}
