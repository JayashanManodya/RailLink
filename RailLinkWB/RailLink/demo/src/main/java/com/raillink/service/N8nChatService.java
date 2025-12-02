package com.raillink.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raillink.dto.ChatMessageRequest;
import com.raillink.dto.ChatResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxies chat requests coming from the web UI to the configured n8n workflow and normalises the response.
 */
@Service
public class N8nChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(N8nChatService.class);

    private final RestTemplate restTemplate;

    @Value("${n8n.chatbot.url:}")
    private String chatWebhookUrl;

    @Value("${n8n.chatbot.enabled:true}")
    private boolean chatEnabled;

    @Value("${n8n.chatbot.api-key:}")
    private String apiKey;

    @Value("${n8n.chatbot.offline-message:Our digital assistant is offline right now. Please try again later.}")
    private String offlineMessage;

    private final ObjectMapper objectMapper;

    public N8nChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public ChatResponse sendMessage(ChatMessageRequest message, Map<String, Object> userContext) {
        String sessionId = StringUtils.hasText(message.getSessionId())
            ? message.getSessionId()
            : UUID.randomUUID().toString();

        if (!chatEnabled || !StringUtils.hasText(chatWebhookUrl)) {
            return ChatResponse.offline(sessionId, offlineMessage);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("message", message.getMessage());
        payload.put("history", message.getHistory());
        payload.put("metadata", message.getMetadata() == null ? Map.of() : message.getMetadata());
        payload.put("context", userContext == null ? Map.of() : userContext);
        payload.put("source", "RailLink-Web");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.set("X-N8N-API-KEY", apiKey);
        }

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            // Accept either a JSON object or an array of objects from n8n
            ResponseEntity<Object> response = restTemplate.postForEntity(chatWebhookUrl, entity, Object.class);
            return adaptResponse(response.getBody(), sessionId);
        } catch (RestClientException ex) {
            LOGGER.error("Unable to reach n8n chat webhook", ex);
            return ChatResponse.error(
                sessionId,
                "I couldn't reach the assistant workflow just now. Please try again in a moment."
            );
        }
    }

    /**
     * Normalise the raw response from n8n.
     * Supports either a single JSON object or an array with the first element being the payload.
     */
    private ChatResponse adaptResponse(Object rawBody, String sessionId) {
        if (rawBody == null) {
            return ChatResponse.error(sessionId, "The assistant returned an empty response.");
        }

        if (rawBody instanceof Map<?, ?> mapBody) {
            return adaptResponseFromMap(castToMap(mapBody), sessionId);
        }

        if (rawBody instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                return adaptResponseFromMap(castToMap(firstMap), sessionId);
            }
        }

        if (rawBody instanceof String json) {
            try {
                Object parsed = objectMapper.readValue(json, Object.class);
                return adaptResponse(parsed, sessionId);
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to parse n8n chat webhook JSON string response", e);
            }
        }

        LOGGER.warn("Unexpected response type from n8n chat webhook: {}", rawBody.getClass());
        return ChatResponse.error(sessionId, "The assistant returned data in an unexpected format.");
    }

    private ChatResponse adaptResponseFromMap(Map<String, Object> rawBody, String sessionId) {
        if (rawBody == null || rawBody.isEmpty()) {
            return ChatResponse.error(sessionId, "The assistant returned an empty response.");
        }

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setSessionId(sessionId);
        chatResponse.setSuccess(true);
        chatResponse.setAgentStatus(Objects.toString(rawBody.getOrDefault("agentStatus", "online"), "online"));
        chatResponse.setReply(resolveReply(rawBody));
        chatResponse.setSuggestions(parseSuggestions(rawBody.get("suggestions")));
        chatResponse.setData(extractData(rawBody));

        return chatResponse;
    }

    private String resolveReply(Map<String, Object> rawBody) {
        Object reply = rawBody.get("reply");
        if (reply instanceof String text && StringUtils.hasText(text)) {
            return text;
        }

        Object message = rawBody.get("message");
        if (message instanceof String fallback && StringUtils.hasText(fallback)) {
            return fallback;
        }

        Object data = rawBody.get("data");
        if (data instanceof Map<?,?> dataMap) {
            Object nestedReply = dataMap.get("reply");
            if (nestedReply instanceof String nestedText && StringUtils.hasText(nestedText)) {
                return nestedText;
            }
        }

        return "I'm not sure how to answer that yet, but a human agent can assist shortly.";
    }

    private List<ChatResponse.Suggestion> parseSuggestions(Object rawSuggestions) {
        if (!(rawSuggestions instanceof List<?> rawList) || rawList.isEmpty()) {
            return ChatResponse.Suggestion.emptyList();
        }

        List<ChatResponse.Suggestion> suggestions = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof String text && StringUtils.hasText(text)) {
                suggestions.add(ChatResponse.Suggestion.of(text));
            } else if (item instanceof Map<?, ?> map) {
                Object labelRaw = firstNonNull(map.get("label"), map.get("title"));
                Object valueRaw = firstNonNull(map.get("value"), map.get("payload"), labelRaw);
                String label = Objects.toString(labelRaw, "");
                String value = Objects.toString(valueRaw, label);
                if (StringUtils.hasText(label)) {
                    suggestions.add(new ChatResponse.Suggestion(label, value));
                }
            }
        }

        return suggestions.isEmpty() ? ChatResponse.Suggestion.emptyList() : suggestions;
    }

    private Map<String, Object> extractData(Map<String, Object> rawBody) {
        Object data = rawBody.get("data");
        if (data instanceof Map<?, ?> map) {
            return castToMap(map);
        }

        // As a fallback, provide the raw response so the UI can still expose metadata for debugging.
        return castToMap(rawBody);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Map<?, ?> source) {
        Map<String, Object> casted = new HashMap<>();
        source.forEach((key, value) -> casted.put(Objects.toString(key), value));
        return casted;
    }

    private Object firstNonNull(Object... candidates) {
        if (candidates == null) {
            return null;
        }
        for (Object value : candidates) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}

