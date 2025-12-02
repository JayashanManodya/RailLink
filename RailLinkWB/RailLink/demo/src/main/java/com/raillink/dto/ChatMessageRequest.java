package com.raillink.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Payload sent from the UI chat widget to the backend before the message is proxied to n8n.
 */
public class ChatMessageRequest {

    private String sessionId;
    private String message;
    private List<ChatTurn> history = new ArrayList<>();
    private Map<String, Object> metadata;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatTurn> getHistory() {
        if (history == null) {
            history = new ArrayList<>();
        }
        return history;
    }

    public void setHistory(List<ChatTurn> history) {
        this.history = history;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Represents a single entry of the running conversation that is forwarded to the n8n workflow.
     */
    public static class ChatTurn {
        private String role; // "user" | "assistant"
        private String content;
        private String timestamp;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}

