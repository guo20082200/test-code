package com.goaerion.testcode.http.dto.assistant;

/**
 * 智能助手响应结果
 */
public record AssistantResponse(String date, String msg, int code, AssistantData data, int status) {
}
