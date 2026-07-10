package com.example.ai_agent.controllers;

import com.example.ai_agent.models.CreditAnalysisForm;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import com.example.ai_agent.useCases.CollectCreditDataUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class CreditAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(CreditAnalysisController.class);

    private final CollectCreditDataUseCase collectCreditDataUseCase;

    private final CreditAnalysisSessionService sessionService;

    public CreditAnalysisController(
            CollectCreditDataUseCase collectCreditDataUseCase,
            CreditAnalysisSessionService sessionService
    ) {
        this.collectCreditDataUseCase = collectCreditDataUseCase;
        this.sessionService = sessionService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam(value = "command") final String command,
            @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }
        log.info("Starting chat stream for session {}", sessionId);
        return this.collectCreditDataUseCase.execute(sessionId, command);
    }

    @GetMapping("/form/{sessionId}")
    public CreditAnalysisForm getForm(@PathVariable String sessionId) {
        return sessionService.getForm(sessionId);
    }
}