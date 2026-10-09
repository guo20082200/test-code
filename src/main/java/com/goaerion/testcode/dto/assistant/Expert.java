package com.goaerion.testcode.dto.assistant;

/**
 * 专家信息（手写建造者模式）
 */
public record Expert(String expertCode, String expertName, String source) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String expertCode;
        private String expertName;
        private String source;

        public Builder expertCode(String expertCode) {
            this.expertCode = expertCode;
            return this;
        }

        public Builder expertName(String expertName) {
            this.expertName = expertName;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Expert build() {
            return new Expert(expertCode, expertName, source);
        }
    }
}
