package com.example.ai_agent.guardrails;

/**
 * Resultado da verificação de guardrail.
 * blocked = true indica que a entrada deve ser rejeitada.
 */
public record GuardrailResult(boolean blocked, String reason) {

    public static GuardrailResult allow() {
        return new GuardrailResult(false, null);
    }

    public static GuardrailResult block(String reason) {
        return new GuardrailResult(true, reason);
    }
}
