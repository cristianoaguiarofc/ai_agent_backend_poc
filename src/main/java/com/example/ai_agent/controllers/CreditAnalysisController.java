package com.example.ai_agent.controllers;

import com.example.ai_agent.models.CreditAnalysisForm;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import com.example.ai_agent.useCases.CreditAnalysisOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class CreditAnalysisController {

    @Autowired
    private CreditAnalysisOrchestrator orchestrator;

    @Autowired
    private CreditAnalysisSessionService sessionService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam(value = "command") final String command,
            @RequestParam(value = "sessionId", required = false) String sessionId
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }
        return this.orchestrator.execute(sessionId, command);
    }

    @PostMapping("/session")
    public Map<String, String> createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionService.initSession(sessionId);
        return Map.of("sessionId", sessionId);
    }

    @GetMapping("/form/{sessionId}")
    public CreditAnalysisForm getForm(@PathVariable String sessionId) {
        return sessionService.getForm(sessionId);
    }
}
