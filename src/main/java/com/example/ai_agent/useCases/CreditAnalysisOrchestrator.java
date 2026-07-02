package com.example.ai_agent.useCases;

import com.example.ai_agent.models.CreditAnalysisStage;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return Mono.fromCallable(() -> sessionService.getStage(sessionId))
                .flatMapMany(initialStage -> {
                    if (CreditAnalysisStage.COMPLETE.equals(initialStage)) {
                        return this.onComplete();
                    }

                    // Dispara a cadeia recursiva/dinâmica de passos
                    return processStage(sessionId, command);
                });
    }

    // Método auxiliar que avalia o estado atual e decide se continua ou para
    private Flux<String> processStage(final String sessionId, final String command) {
        switch (sessionService.getStage(sessionId)) {
            case COLLECT_DATA:
                return this.onCollectData(sessionId, command);

            case FETCH_CPF_SCORE:
                return this.onFetchCpfScore(sessionId, command);

            case ANALYZE:
                return this.onAnalyze(sessionId, command);

            default:
                // Se chegou em COMPLETE ou outro estado interativo, encerra o fluxo
                return Flux.empty();
        }
    }

    private Flux<String> onCollectData(final String sessionId, final String command) {
        return collectCreditDataUseCase.execute(sessionId, command);
    }

    private Flux<String> onFetchCpfScore(final String sessionId, final String command) {
        return fetchCpfScoreUseCase.execute(sessionId);
    }

    private Flux<String> onAnalyze(final String sessionId, final String command) {
        return analyzeCreditUseCase.execute(sessionId);
    }

    private Flux<String> onComplete() {
        return Flux.just("A análise de crédito já foi concluída. Inicie uma nova sessão para uma nova solicitação.");
    }
}
