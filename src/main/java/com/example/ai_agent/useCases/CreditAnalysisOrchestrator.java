package com.example.ai_agent.useCases;

import com.example.ai_agent.guardrails.PromptInjectionGuardrail;
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

    @Autowired
    private PromptInjectionGuardrail guardrail;

    public Flux<String> execute(final String sessionId, final String command) {
        CreditAnalysisStage stage = sessionService.getStage(sessionId);

        // Apenas o estágio COLLECT_DATA recebe input direto do usuário.
        // Os demais são fluxos internos e não precisam ser validados novamente.
        if (stage != CreditAnalysisStage.COLLECT_DATA) {
            return routeToStage(sessionId, command, stage);
        }

        // Valida o input do usuário antes de passar para o agente
        return Flux.from(
                guardrail.validate(command)
                        .flatMapMany(result -> {
                            if (result.blocked()) {
                                return Flux.just("⚠️ " + result.reason());
                            }
                            return routeToStage(sessionId, command, sessionService.getStage(sessionId));
                        })
        );
    }

    private Flux<String> routeToStage(final String sessionId, final String command, final CreditAnalysisStage stage) {
        return switch (stage) {
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
