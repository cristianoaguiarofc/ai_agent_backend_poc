package com.example.ai_agent.tools;

import com.example.ai_agent.models.CreditAnalysisStage;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import org.springframework.ai.tool.annotation.Tool;

public class FetchCpfScoreTools {

    private final String sessionId;
    private final CreditAnalysisSessionService sessionService;

    public FetchCpfScoreTools(String sessionId, CreditAnalysisSessionService sessionService) {
        this.sessionId = sessionId;
        this.sessionService = sessionService;
    }

    @Tool(description = "Avança para a próxima etapa.", name = "changeStep")
    public String changeStep() {
        this.sessionService.advanceStage(sessionId, CreditAnalysisStage.ANALYZE);
        return "Score coletado com sucesso. Prosseguindo para a próxima etapa.";
    }

}
