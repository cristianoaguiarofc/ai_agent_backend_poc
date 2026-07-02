package com.example.ai_agent.useCases;

import com.example.ai_agent.services.CreditAnalysisSessionService;
import com.example.ai_agent.tools.FormCollectionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class CollectCreditDataUseCase {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente de análise de crédito. Sua missão é coletar as seguintes informações do usuário, uma de cada vez, de forma amigável e conversacional:

            1. Valor total solicitado (em reais)
            2. Prazo em meses
            3. Renda mensal (em reais)
            4. CPF

            Regras:
            - Colete apenas uma informação por vez, na ordem acima.
            - Após receber cada informação, use imediatamente a ferramenta correspondente para salvá-la antes de continuar.
            - Confirme o valor salvo e solicite o próximo campo.
            - Aceite valores em linguagem natural (ex: "5 mil", "R$ 5.000", "5000").
            - Aceite o CPF com ou sem formatação.
            - Se o usuário informar um valor inválido ou ambíguo, peça para repetir de forma clara.
            - Quando todos os dados estiverem coletados, apresente um resumo e informe que irá consultar o score de crédito.
            - Após coletar todos os dados corretamente muda o status para seguir para a próxima etapa
            - Seja sempre educado e objetivo.
            """;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private CreditAnalysisSessionService sessionService;

    public Flux<String> execute(final String sessionId, final String command) {
        sessionService.initSession(sessionId);

        var tools = new FormCollectionTools(sessionId, sessionService);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(command)
                .tools(tools)
                .advisors(spec -> spec
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .param("chat_memory_conversation_id", sessionId))
                .stream()
                .content();
    }
}
