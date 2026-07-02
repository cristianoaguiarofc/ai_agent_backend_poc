package com.example.ai_agent.tools;

import com.example.ai_agent.models.CreditAnalysisStage;
import com.example.ai_agent.services.CreditAnalysisSessionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class FormCollectionTools {

    private final String sessionId;
    private final CreditAnalysisSessionService sessionService;

    public FormCollectionTools(String sessionId, CreditAnalysisSessionService sessionService) {
        this.sessionId = sessionId;
        this.sessionService = sessionService;
    }

    @Tool(description = "Salva o valor total solicitado pelo cliente em reais.")
    public String saveTotalAmount(BigDecimal totalAmount) {
        var form = sessionService.getForm(sessionId).withTotalAmount(totalAmount);
        sessionService.saveForm(sessionId, form);
        return "Valor total de R$ " + totalAmount + " registrado.";
    }

    @Tool(description = "Salva o prazo do financiamento em meses.")
    public String saveTermMonths(Integer termMonths) {
        var form = sessionService.getForm(sessionId).withTermMonths(termMonths);
        sessionService.saveForm(sessionId, form);
        return "Prazo de " + termMonths + " meses registrado.";
    }

    @Tool(description = "Salva a renda mensal do cliente em reais.")
    public String saveMonthlyIncome(BigDecimal monthlyIncome) {
        var form = sessionService.getForm(sessionId).withMonthlyIncome(monthlyIncome);
        sessionService.saveForm(sessionId, form);
        return "Renda mensal de R$ " + monthlyIncome + " registrada.";
    }

    @Tool(description = "Salva o CPF do cliente. Aceita com ou sem formatação.")
    public String saveCpf(String cpf) {
        String digits = cpf.replaceAll("[^\\d]", "");
        var form = sessionService.getForm(sessionId).withCpf(digits);
        sessionService.saveForm(sessionId, form);
        sessionService.advanceStage(sessionId, CreditAnalysisStage.FETCH_CPF_SCORE);
        return "CPF registrado.";
    }
}