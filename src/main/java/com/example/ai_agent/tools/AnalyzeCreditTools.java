package com.example.ai_agent.tools;

import com.example.ai_agent.models.CreditAnalysisStage;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import org.springframework.ai.tool.annotation.Tool;

public class AnalyzeCreditTools {

    private final String sessionId;
    private final CreditAnalysisSessionService sessionService;

    public AnalyzeCreditTools(String sessionId, CreditAnalysisSessionService sessionService) {
        this.sessionId = sessionId;
        this.sessionService = sessionService;
    }

    @Tool(description = "Avança para a próxima etapa.", name = "changeStep")
    public String changeStep() {
        this.sessionService.advanceStage(sessionId, CreditAnalysisStage.ANALYZE);
        return "Análise feita com sucesso. Prosseguindo para a próxima etapa.";
    }
}
