package com.example.ai_agent.useCases;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class CreditAnalysisUseCase {

    @Autowired
    private ChatClient chatClient;

    public Flux<String> execute(final String command) {
        return this.chatClient
                .prompt(command)
                .stream()
                .content();
    }
}