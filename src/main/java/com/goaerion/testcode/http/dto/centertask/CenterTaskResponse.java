package com.goaerion.testcode.http.dto.centertask;

/**
 * 任务中心新增任务响应体（对应 doc/任务中心新增响应体.json）
 */
public record CenterTaskResponse(
        /** 请求是否成功 */
        Boolean success,
        /** 响应码，如：200 */
        Integer code,
        /** 响应消息 */
        String msg,
        /** 任务详情 */
        CenterTask data) {
}
