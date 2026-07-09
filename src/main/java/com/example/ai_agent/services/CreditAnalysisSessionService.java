package com.example.ai_agent.services;

import com.example.ai_agent.models.CreditAnalysisForm;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class CreditAnalysisSessionService {

    private final ConcurrentHashMap<String, CreditAnalysisForm> sessions = new ConcurrentHashMap<>();

    public CreditAnalysisForm getForm(final String sessionId) {
        return sessions.getOrDefault(sessionId, CreditAnalysisForm.empty());
    }

    public void saveForm(final String sessionId, final CreditAnalysisForm form) {
        sessions.put(sessionId, form);
    }

    public void initSession(final String sessionId) {
        sessions.putIfAbsent(sessionId, CreditAnalysisForm.empty());
    }
}