package com.example.ai_agent.useCases;

import com.example.ai_agent.models.CreditAnalysisForm;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FetchCpfScoreUseCase {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            Você é um assistente de análise de crédito. Os dados do cliente já foram coletados:

            - Valor solicitado: R$ %s
            - Prazo: %d meses
            - Renda mensal: R$ %s
            - CPF: %s

            Use a ferramenta `get_score` com o CPF acima para consultar o score de crédito.
            Após obter o score, informe o resultado ao usuário de forma clara: mencione o número do score na resposta.
            Não invente scores — use sempre a ferramenta.
            """;

    private static final Pattern SCORE_PATTERN = Pattern.compile("\\b([0-9]{1,4})\\b");

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private CreditAnalysisSessionService sessionService;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    public Flux<String> execute(final String sessionId) {
        CreditAnalysisForm form = sessionService.getForm(sessionId);

        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(
                form.totalAmount(),
                form.termMonths(),
                form.monthlyIncome(),
                form.cpf()
        );

        StringBuilder accumulated = new StringBuilder();

        return chatClient.prompt()
                .system(systemPrompt)
                .user("Consulte o score do CPF " + form.cpf() + " agora.")
                .tools(toolCallbackProvider)
                .advisors(spec -> spec
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .param("chat_memory_conversation_id", sessionId))
                .stream()
                .content()
                .doOnNext(accumulated::append)
                .doOnComplete(() -> persistScore(sessionId, accumulated.toString()));
    }

    private void persistScore(String sessionId, String response) {
        Matcher matcher = SCORE_PATTERN.matcher(response);
        while (matcher.find()) {
            int candidate = Integer.parseInt(matcher.group(1));
            if (candidate >= 0 && candidate <= 1000) {
                sessionService.saveForm(sessionId, sessionService.getForm(sessionId).withScore(candidate));
                return;
            }
        }
    }
}
