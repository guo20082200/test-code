package com.goaerion.testcode.dto.centertask;

/**
 * 按钮
 */
public record Button(
        /** 按钮名称，如：处理、查看 */
        String buttonName,
        /** 是否高亮 */
        Boolean highLight,
        /** 按钮动作 */
        ButtonAction action) {
}
