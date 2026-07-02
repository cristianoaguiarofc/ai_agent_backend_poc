package com.example.ai_agent.guardrails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Orquestra os guardrails na ordem:
 * 1. StaticGuardrail  — rápido, síncrono, sem custo de LLM.
 * 2. SemanticGuardrail — assíncrono, usa LLM classificador isolado.
 *
 * Se qualquer camada bloquear, a cadeia é interrompida imediatamente.
 */
@Component
public class PromptInjectionGuardrail {

    private static final Logger log = LoggerFactory.getLogger(PromptInjectionGuardrail.class);

    @Autowired
    private StaticGuardrail staticGuardrail;

    @Autowired
    private SemanticGuardrail semanticGuardrail;

    /**
     * Verifica o input do usuário em todas as camadas.
     * Retorna um {@link GuardrailResult} com blocked=false se o input for seguro,
     * ou blocked=true com a razão se for bloqueado.
     */
    public Mono<GuardrailResult> validate(String userInput) {
        // Camada 1: verificação estática (sync → wrapped em Mono)
        GuardrailResult staticResult = staticGuardrail.check(userInput);
        if (staticResult.blocked()) {
            log.warn("[PromptInjectionGuardrail] Bloqueado pela camada estática.");
            return Mono.just(staticResult);
        }

        // Camada 2: verificação semântica (async)
        return semanticGuardrail.check(userInput)
                .doOnNext(result -> {
                    if (result.blocked()) {
                        log.warn("[PromptInjectionGuardrail] Bloqueado pela camada semântica.");
                    }
                });
    }
}
