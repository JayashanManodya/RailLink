package com.raillink.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Response returned to the UI chat widget after the message is processed (or when a fallback is used).
 */
public class ChatResponse {

    private String sessionId;
    private String reply;
    private boolean success;
    private String error;
    private String agentStatus;
    private List<Suggestion> suggestions = new ArrayList<>();
    private Map<String, Object> data;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(String agentStatus) {
        this.agentStatus = agentStatus;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public static ChatResponse offline(String sessionId, String message) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setReply(message);
        response.setSuccess(false);
        response.setAgentStatus("offline");
        return response;
    }

    public static ChatResponse error(String sessionId, String message) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setReply(message);
        response.setError(message);
        response.setSuccess(false);
        response.setAgentStatus("degraded");
        return response;
    }

    public record Suggestion(String label, String value) {

        public static Suggestion of(String text) {
            return new Suggestion(text, text);
        }

        public static List<Suggestion> emptyList() {
            return Collections.emptyList();
        }
    }
}

