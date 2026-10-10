package com.goaerion.testcode.http.dto.centertask;

/**
 * 按钮动作
 */
public record ButtonAction(
        /** 动作类型，如：redirect */
        String type,
        /** 跳转动作配置 */
        RedirectInfo redirect) {

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
        private String type;
        private RedirectInfo redirect;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder redirect(RedirectInfo redirect) {
            this.redirect = redirect;
            return this;
        }

        public ButtonAction build() {
            return new ButtonAction(type, redirect);
        }
    }
}
