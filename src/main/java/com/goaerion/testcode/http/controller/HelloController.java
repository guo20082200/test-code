package com.goaerion.testcode.http.controller;

import com.goaerion.testcode.http.dto.assistant.AssistantResponse;
import com.goaerion.testcode.http.dto.assistant.Expert;
import com.goaerion.testcode.http.dto.assistant.SendToAssistantRequest;
import com.goaerion.testcode.http.dto.centertask.*;
import com.goaerion.testcode.http.dto.centertask.CenterTask;
import com.goaerion.testcode.http.dto.centertask.CenterTaskResponse;
import com.goaerion.testcode.http.service.CenterTaskService;
import com.goaerion.testcode.http.service.HttpServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
public class HelloController {

        private final HttpServiceFactory httpServiceFactory;

        private final CenterTaskService centerTaskService;

        /**
         * 智能助手接口地址（见 application.properties 的 assistant.send-url），属于业务配置
         */
        @Value("${assistant.send-url}")
        private String sendToAssistantUrl;

        /**
         * 任务中心新增任务接口地址（见 application.properties 的 center-task.send-url）
         */
        @Value("${center-task.send-url}")
        private String centerTaskSendUrl;

        public HelloController(HttpServiceFactory httpServiceFactory, CenterTaskService centerTaskService) {
                this.httpServiceFactory = httpServiceFactory;
                this.centerTaskService = centerTaskService;
        }

        @GetMapping("/hello")
        public AssistantResponse hello(@RequestHeader("currentuserid") String currentUserId,
                        @RequestHeader("environment") String environment,
                        @RequestHeader("Authorization") String authorization) {
                // 1. 写发送http请求的请求头
                Map<String, String> headers = new LinkedHashMap<>();
                headers.put("currentuserid", currentUserId);
                headers.put("environment", environment);
                headers.put("Authorization", authorization);

                // 2. 写发送http请求的请求体
                SendToAssistantRequest body = SendToAssistantRequest.builder()
                                .name("联网测试")
                                .prompt("今天上海的天气如何")
                                .mode("advanced-mode-agent")
                                .model("qwen3.6-27b")
                                .iqs(true)
                                .expert(Expert.builder()
                                                .expertCode("2090367357087125504")
                                                .expertName("商飞智能本体查询")
                                                .source("market")
                                                .build())
                                .build();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .post(sendToAssistantUrl, body, headers, AssistantResponse.class);
        }

        @GetMapping("/centerTask")
        public CenterTaskResponse centerTask(@RequestHeader("currentuserid") String currentUserId,
                                             @RequestHeader("environment") String environment,
                                             @RequestHeader("Authorization") String authorization) {
                // 1. 写发送http请求的请求头
                Map<String, String> headers = new LinkedHashMap<>();
                headers.put("currentuserid", currentUserId);
                headers.put("environment", environment);
                headers.put("Authorization", authorization);

                // 2. 构建CenterTask的请求体（结构组装委托给任务中心服务）
                CenterTask body = centerTaskService.buildCenterTask(currentUserId);

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .post(centerTaskSendUrl, body, headers, CenterTaskResponse.class);
        }
}
