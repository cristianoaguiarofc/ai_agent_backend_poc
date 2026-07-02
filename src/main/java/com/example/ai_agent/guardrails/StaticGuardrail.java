package com.example.ai_agent.guardrails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Guardrail estático: verifica tamanho máximo de input e padrões
 * conhecidos de prompt injection por regex (sem custo de LLM).
 */
@Component
public class StaticGuardrail {

    private static final Logger log = LoggerFactory.getLogger(StaticGuardrail.class);

    private static final int MAX_INPUT_LENGTH = 1000;

    /**
     * Padrões de mensagens sempre permitidas (saudações, cortesias, agradecimentos).
     * Se qualquer um casar, o input é liberado imediatamente sem verificar injeções.
     */
    private static final List<Pattern> ALLOWLIST_PATTERNS = List.of(
            Pattern.compile("(?i)^(ol[aá]|oi|bom\\s+dia|boa\\s+tarde|boa\\s+noite|boas\\s+vindas)[!.,\\s]*$"),
            Pattern.compile("(?i)^(obrigad[oa]|muito\\s+obrigad[oa]|grat[oa]|valeu)[!.,\\s]*$"),
            Pattern.compile("(?i)^(tudo\\s+bem|tudo\\s+certo|tudo\\s+bom|como\\s+vai)[?!.,\\s]*$"),
            Pattern.compile("(?i)^(at[eé]\\s+mais|tchau|at[eé]\\s+logo|at[eé])[!.,\\s]*$"),
            Pattern.compile("(?i)^(por\\s+favor|pode\\s+ser|ok|okay|sim|n[ãa]o|certo|entendido)[!.,\\s]*$")
    );

    /**
     * Padrões que tentam subverter, redefinir ou escalar o system prompt.
     * A lista não é exaustiva, mas cobre as técnicas mais comuns.
     */
    private static final List<Pattern> INJECTION_PATTERNS = List.of(

            // Tentativas de redefinir/ignorar instruções originais
            // Tolera palavras extras entre o verbo e o alvo: "forget YOUR previous instructions"
            Pattern.compile("(?i)(ignore|forget|disregard|override)(\\s+\\w+){0,3}\\s+(previous|prior|above|original|system)\\s+(instructions?|prompts?|rules?|context|constraints?)", Pattern.CASE_INSENSITIVE),

            // Comandos para atuar como outro sistema
            // Tolera artigo "an" além de "a": "act like AN uncensored assistant"
            Pattern.compile("(?i)(pretend|act|behave|respond|play)\\s+(as|like|you\\s+are|you're)\\s+(an?\\s+)?(different|new|other|another|unrestricted|jailbroken|uncensored|evil|dan)", Pattern.CASE_INSENSITIVE),

            // Técnica DAN / jailbreak
            Pattern.compile("(?i)\\b(DAN|do\\s+anything\\s+now|jailbreak|jail\\s*break|developer\\s+mode|god\\s+mode)\\b", Pattern.CASE_INSENSITIVE),

            // Redefinição direta de papel (role)
            Pattern.compile("(?i)(you\\s+are\\s+(now|actually|really)\\s+|your\\s+(new\\s+)?(role|persona|name|instructions?|identity)\\s+(is|are|will\\s+be))", Pattern.CASE_INSENSITIVE),

            // Injeção via delimitadores ou markdown especial
            Pattern.compile("(?i)(</?(system|user|assistant|instruction|prompt|context)>|\\[SYSTEM\\]|\\[INST\\]|<<SYS>>|<\\|system\\|>)", Pattern.CASE_INSENSITIVE),

            // Tentativa de exfiltrar o system prompt
            // Tolera palavras extras: "show ME your system prompt", "what are your hidden guidelines"
            Pattern.compile("(?i)(show(\\s+me)?|print|reveal|repeat|output|display|tell\\s+me|what\\s+is|what\\s+are)\\s+(your\\s+)?((system|initial|hidden|original)\\s+)?(prompt|instructions?|guidelines?|context)", Pattern.CASE_INSENSITIVE),

            // Escaping de contexto via tokens de controle comuns
            Pattern.compile("(?i)(###\\s*(instruction|system|end))", Pattern.CASE_INSENSITIVE),

            // Injeção de novas tarefas fora do escopo de crédito
            Pattern.compile("(?i)(translate|summarize|write\\s+(a\\s+)?(poem|code|script|story|email)|generate\\s+(code|image|text)|hack|exploit|bypass|crack|sql\\s+injection)", Pattern.CASE_INSENSITIVE)
    );

    public GuardrailResult check(String input) {
        if (input == null || input.isBlank()) {
            return GuardrailResult.block("Entrada vazia não é permitida.");
        }

        if (input.length() > MAX_INPUT_LENGTH) {
            log.warn("[StaticGuardrail] Input excedeu o limite de {} caracteres.", MAX_INPUT_LENGTH);
            return GuardrailResult.block(
                    "Sua mensagem é muito longa. Por favor, seja mais conciso (máximo " + MAX_INPUT_LENGTH + " caracteres)."
            );
        }

        // Allowlist: saudações e cortesias são sempre liberadas sem verificar injeções
        for (Pattern allowed : ALLOWLIST_PATTERNS) {
            if (allowed.matcher(input.strip()).matches()) {
                return GuardrailResult.allow();
            }
        }

        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("[StaticGuardrail] Padrão suspeito detectado: {}", pattern.pattern());
                return GuardrailResult.block(
                        "Sua mensagem contém conteúdo não permitido. Por favor, forneça apenas as informações solicitadas."
                );
            }
        }

        return GuardrailResult.allow();
    }
}
