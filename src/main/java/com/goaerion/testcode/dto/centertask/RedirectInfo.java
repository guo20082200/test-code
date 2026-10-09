package com.goaerion.testcode.dto.centertask;

import java.util.Map;

/**
 * 跳转配置
 */
public record RedirectInfo(
        /** 需要带 C 大脑 token 时传 true */
        Boolean ifToken,
        /** URL 问号（?）之后拼接的参数 */
        Map<String, String> params,
        /** 问号之前的相对路径，如：/brain-build/#/app-execute/app-forms-detail */
        String path,
        /** 请求方式：http/https */
        String protocol,
        /** 当前服务 ip */
        String ip) {
}
