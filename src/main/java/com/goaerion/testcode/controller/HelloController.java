package com.goaerion.testcode.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goaerion.testcode.dto.assistant.AssistantResponse;
import com.goaerion.testcode.dto.assistant.Expert;
import com.goaerion.testcode.dto.assistant.SendToAssistantRequest;
import com.goaerion.testcode.dto.centertask.Button;
import com.goaerion.testcode.dto.centertask.ButtonAction;
import com.goaerion.testcode.dto.centertask.ButtonInformation;
import com.goaerion.testcode.dto.centertask.CenterTask;
import com.goaerion.testcode.dto.centertask.CenterTaskResponse;
import com.goaerion.testcode.dto.centertask.KeyValueInfo;
import com.goaerion.testcode.dto.centertask.RedirectInfo;
import com.goaerion.testcode.service.HttpServiceFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/test")
public class HelloController {

        private final HttpServiceFactory httpServiceFactory;

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

        public HelloController(HttpServiceFactory httpServiceFactory) {
                this.httpServiceFactory = httpServiceFactory;
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

                // 2. 构建 CenterTask的请求体
                CenterTask body = CenterTask.builder()
                                .tenantId(1)
                                .mainTitle("测试任务")
                                .subTitle("测试任务副标题")
                                .taskType("test")
                                .taskSource("test")
                                .processStatus("test")
                                .taskFounderId(currentUserId)
                                .taskFounderName("测试用户")
                                .taskAcceptorId(currentUserId)
                                .taskAcceptorName("测试用户")
                                .taskArriveTime("2023-01-01 00:00:00")
                                .taskPlanFinishTime("2023-01-01 00:00:00")
                                .taskRealFinishTime("2023-01-01 00:00:00")
                                .markInformation(List.of("测试标记"))
                                .businessId("123456")
                                // 标签配置，可选
                                .labelInfo(List.of(
                                                new KeyValueInfo("优先级", "P0"),
                                                new KeyValueInfo("事业群", "123")))
                                // 可见字段，key-参数名，value-值
                                .visibleInformation(List.of(
                                                new KeyValueInfo("资产类型", "硬件"),
                                                new KeyValueInfo("资产编号", "zf0515")))
                                // 按钮列表
                                .buttonInformation(buildButtonInformation())
                                // 数据交换字段，自定义 key-value
                                .exchangeInformation(Map.of(
                                                "lessseeId", "",
                                                "otherId", "",
                                                "planId", "",
                                                "applicationId", "",
                                                "dataSource", ""))
                                .build();

                // 3. 通过工厂获取实现，发送请求并处理响应结果
                return httpServiceFactory.getHttpService()
                                .post(centerTaskSendUrl, body, headers, CenterTaskResponse.class);
        }

        /**
         * 构建按钮列表，pc / mobile / innerNet 共用同一组按钮（处理、查看）
         */
        private ButtonInformation buildButtonInformation() {
                Map<String, String> params = Map.of(
                                "instanceId", "1937455889927225344",
                                "workGroupId", "1937442495629627394",
                                "workItemId", "520");
                RedirectInfo redirect = new RedirectInfo(
                                true, params, "/brain-instance/#/task-detail", "http", "");
                ButtonAction action = new ButtonAction("redirect", redirect);

                Button processButton = new Button("处理", true, action);
                Button viewButton = new Button("查看", true, action);

                List<Button> buttons = List.of(processButton, viewButton);
                return new ButtonInformation(buttons, buttons, buttons);
        }
}
