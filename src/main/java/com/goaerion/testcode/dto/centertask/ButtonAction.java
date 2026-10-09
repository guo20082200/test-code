package com.goaerion.testcode.dto.centertask;

/**
 * 按钮动作
 */
public record ButtonAction(
        /** 动作类型，如：redirect */
        String type,
        /** 跳转动作配置 */
        RedirectInfo redirect) {
}
