package com.goaerion.testcode.http.dto.centertask;

import java.util.Map;

/**
 * 按钮构建参数（参数对象）：汇聚 buildButton 所需的全部配置
 */
public record ButtonConfig(
        /** 按钮名称，如：处理、查看 */
        String buttonName,
        /** 是否高亮 */
        Boolean highLight,
        /** 动作类型，如：redirect */
        String actionType,
        /** URL 问号（?）之后拼接的参数 */
        Map<String, String> params,
        /** 需要带 C 大脑 token 时传 true */
        Boolean ifToken,
        /** 问号之前的相对路径，如：/brain-instance/#/task-detail */
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
     * 派生一个仅替换按钮名称的新配置（其余字段复用当前值）
     */
    public ButtonConfig withButtonName(String buttonName) {
        return new ButtonConfig(buttonName, highLight, actionType, params, ifToken, path, protocol, ip);
    }

    /**
     * 建造者：链式设置字段，build() 产出不可变 record 实例
     */
    public static class Builder {
        private String buttonName;
        private Boolean highLight;
        private String actionType;
        private Map<String, String> params;
        private Boolean ifToken;
        private String path;
        private String protocol;
        private String ip;

        public Builder buttonName(String buttonName) {
            this.buttonName = buttonName;
            return this;
        }

        public Builder highLight(Boolean highLight) {
            this.highLight = highLight;
            return this;
        }

        public Builder actionType(String actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder ifToken(Boolean ifToken) {
            this.ifToken = ifToken;
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

        public ButtonConfig build() {
            return new ButtonConfig(buttonName, highLight, actionType, params, ifToken, path, protocol, ip);
        }
    }
}
