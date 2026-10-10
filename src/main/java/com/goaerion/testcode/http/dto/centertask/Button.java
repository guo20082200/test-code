package com.goaerion.testcode.http.dto.centertask;

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
        private String buttonName;
        private Boolean highLight;
        private ButtonAction action;

        public Builder buttonName(String buttonName) {
            this.buttonName = buttonName;
            return this;
        }

        public Builder highLight(Boolean highLight) {
            this.highLight = highLight;
            return this;
        }

        public Builder action(ButtonAction action) {
            this.action = action;
            return this;
        }

        public Button build() {
            return new Button(buttonName, highLight, action);
        }
    }
}
