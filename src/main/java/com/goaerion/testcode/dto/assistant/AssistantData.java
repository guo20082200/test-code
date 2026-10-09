package com.goaerion.testcode.dto.assistant;

/**
 * 智能助手响应数据
 */
public record AssistantData(String instantTaskId, String scheduleId, String status,
        String content, String errorMessage) {
}
