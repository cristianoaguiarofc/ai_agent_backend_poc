package com.example.ai_agent.services;

import com.example.ai_agent.models.CreditAnalysisForm;
import com.example.ai_agent.models.CreditAnalysisStage;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class CreditAnalysisSessionService {

    private final ConcurrentHashMap<String, CreditAnalysisForm> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CreditAnalysisStage> stages = new ConcurrentHashMap<>();

    public CreditAnalysisForm getForm(final String sessionId) {
        return sessions.getOrDefault(sessionId, CreditAnalysisForm.empty());
    }

    public void saveForm(final String sessionId, final CreditAnalysisForm form) {
        sessions.put(sessionId, form);
    }

    public void initSession(final String sessionId) {
        sessions.putIfAbsent(sessionId, CreditAnalysisForm.empty());
        stages.putIfAbsent(sessionId, CreditAnalysisStage.COLLECT_DATA);
    }

    public CreditAnalysisStage getStage(final String sessionId) {
        return stages.getOrDefault(sessionId, CreditAnalysisStage.COLLECT_DATA);
    }

    public void advanceStage(final String sessionId, final CreditAnalysisStage stage) {
        stages.put(sessionId, stage);
    }
}
