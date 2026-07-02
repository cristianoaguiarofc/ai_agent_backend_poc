package com.example.ai_agent.useCases;

import com.example.ai_agent.models.CreditAnalysisForm;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class AnalyzeCreditUseCase {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            Você é um analista de crédito experiente. Com base nos dados abaixo, emita uma análise de crédito completa e objetiva.

            Dados do cliente:
            - Valor solicitado: R$ %s
            - Prazo: %d meses
            - Renda mensal: R$ %s
            - CPF: %s
            - Score de crédito: %d / 1000

            Critérios para sua análise:
            - Score acima de 700: perfil excelente
            - Score entre 500 e 699: perfil moderado, pode exigir condições
            - Score abaixo de 500: perfil de risco elevado

            Calcule a parcela estimada (valor / prazo) e verifique se ela compromete mais de 30%% da renda mensal.
            Com base nisso, emita um dos três vereditos: APROVADO, APROVADO COM CONDIÇÕES ou REPROVADO.
            Explique brevemente o raciocínio da decisão ao cliente de forma clara e respeitosa.
            """;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private CreditAnalysisSessionService sessionService;

    public Flux<String> execute(final String sessionId) {
        CreditAnalysisForm form = sessionService.getForm(sessionId);

        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(
                form.totalAmount(),
                form.termMonths(),
                form.monthlyIncome(),
                form.cpf(),
                form.score() != null ? form.score() : 0
        );

        return chatClient.prompt()
                .system(systemPrompt)
                .user("Realize a análise de crédito com os dados fornecidos.")
                .advisors(spec -> spec
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .param("chat_memory_conversation_id", sessionId))
                .stream()
                .content();
    }
}
