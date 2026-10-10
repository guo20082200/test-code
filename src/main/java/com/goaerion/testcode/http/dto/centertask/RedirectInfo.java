package com.goaerion.testcode.http.dto.centertask;

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

    /**
     * 建造者模式入口
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 建造者：链式设置字段，build() 产出不可变 record 实例
     */
    public static class Builder {
        private Boolean ifToken;
        private Map<String, String> params;
        private String path;
        private String protocol;
        private String ip;

        public Builder ifToken(Boolean ifToken) {
            this.ifToken = ifToken;
            return this;
        }

        public Builder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public RedirectInfo build() {
            return new RedirectInfo(ifToken, params, path, protocol, ip);
        }
    }
}
