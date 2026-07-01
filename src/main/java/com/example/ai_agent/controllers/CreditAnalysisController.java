package com.example.ai_agent.controllers;

import com.example.ai_agent.useCases.CreditAnalysisUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class CreditAnalysisController {

    @Autowired
    private CreditAnalysisUseCase creditAnalysisUseCase;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> creditAnalyst(@RequestParam(value = "command") final String command) {
        return this.creditAnalysisUseCase.execute(command);
    }
}