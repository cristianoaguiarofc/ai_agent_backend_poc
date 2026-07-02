package com.example.ai_agent.models;

import java.math.BigDecimal;

public record CreditAnalysisForm(
        BigDecimal totalAmount,
        Integer termMonths,
        BigDecimal monthlyIncome,
        String cpf,
        Integer score
) {
    public boolean isComplete() {
        return totalAmount != null && termMonths != null && monthlyIncome != null && cpf != null;
    }

    public CreditAnalysisForm withTotalAmount(BigDecimal value) {
        return new CreditAnalysisForm(value, termMonths, monthlyIncome, cpf, score);
    }

    public CreditAnalysisForm withTermMonths(Integer value) {
        return new CreditAnalysisForm(totalAmount, value, monthlyIncome, cpf, score);
    }

    public CreditAnalysisForm withMonthlyIncome(BigDecimal value) {
        return new CreditAnalysisForm(totalAmount, termMonths, value, cpf, score);
    }

    public CreditAnalysisForm withCpf(String value) {
        return new CreditAnalysisForm(totalAmount, termMonths, monthlyIncome, value, score);
    }

    public CreditAnalysisForm withScore(Integer value) {
        return new CreditAnalysisForm(totalAmount, termMonths, monthlyIncome, cpf, value);
    }

    public static CreditAnalysisForm empty() {
        return new CreditAnalysisForm(null, null, null, null, null);
    }
}
