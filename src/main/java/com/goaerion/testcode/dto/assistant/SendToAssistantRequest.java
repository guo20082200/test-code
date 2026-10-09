package com.goaerion.testcode.dto.assistant;

/**
 * 发送智能助手请求体（手写建造者模式）
 */
public record SendToAssistantRequest(String name, String prompt, String mode, String model,
        boolean iqs, Expert expert) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String prompt;
        private String mode;
        private String model;
        private boolean iqs;
        private Expert expert;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder iqs(boolean iqs) {
            this.iqs = iqs;
            return this;
        }

        public Builder expert(Expert expert) {
            this.expert = expert;
            return this;
        }

        public SendToAssistantRequest build() {
            return new SendToAssistantRequest(name, prompt, mode, model, iqs, expert);
        }
    }
}
