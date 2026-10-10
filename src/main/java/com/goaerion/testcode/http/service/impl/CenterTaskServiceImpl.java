package com.goaerion.testcode.http.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.goaerion.testcode.http.dto.centertask.Button;
import com.goaerion.testcode.http.dto.centertask.ButtonAction;
import com.goaerion.testcode.http.dto.centertask.ButtonConfig;
import com.goaerion.testcode.http.dto.centertask.ButtonInformation;
import com.goaerion.testcode.http.dto.centertask.CenterTask;
import com.goaerion.testcode.http.dto.centertask.KeyValueInfo;
import com.goaerion.testcode.http.dto.centertask.RedirectInfo;
import com.goaerion.testcode.http.service.CenterTaskService;

/**
 * 任务中心服务实现：负责任务请求体及按钮相关结构的构建
 */
@Service
public class CenterTaskServiceImpl implements CenterTaskService {

    @Override
    public CenterTask buildCenterTask(String currentUserId) {
        return CenterTask.builder()
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
    }

    @Override
    public ButtonInformation buildButtonInformation() {
        // 共用配置模板，仅按钮名不同
        ButtonConfig template = buildButtonConfig();

        List<Button> buttons = List.of(
                buildButton(template.withButtonName("处理")),
                buildButton(template.withButtonName("查看")));
        return new ButtonInformation(buttons, buttons, buttons);
    }

    @Override
    public ButtonConfig buildButtonConfig() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("currentuserid", "1937455889927225344");
        params.put("environment", "1937442495629627394");
        params.put("Authorization", "520");

        return buildButtonConfig("立即处理", params, "/brain-instance/#/task-detail", "http", "192.168.1.100:8080");
    }

    @Override
    public ButtonConfig buildButtonConfig(
            String buttonName, Map<String, String> params, String path, String protocol, String ip) {
        return buildButtonConfig(buttonName, true, "redirect", params, true, path, protocol, ip);
    }

    @Override
    public ButtonConfig buildButtonConfig(String buttonName, Boolean highLight, String actionType,
            Map<String, String> params,
            Boolean ifToken, String path, String protocol, String ip) {
        return ButtonConfig.builder().buttonName(buttonName)
                .highLight(highLight)
                .actionType(actionType)
                .params(params)
                .ifToken(ifToken)
                .path(path)
                .protocol(protocol)
                .ip(ip)
                .build();
    }

    @Override
    public Button buildButton(ButtonConfig config) {

        RedirectInfo redirect = RedirectInfo.builder()
                .ifToken(config.ifToken())
                .params(config.params())
                .path(config.path())
                .protocol(config.protocol())
                .ip(config.ip())
                .build();
        ButtonAction action = ButtonAction.builder()
                .type(config.actionType())
                .redirect(redirect)
                .build();

        return Button.builder()
                .buttonName(config.buttonName())
                .highLight(config.highLight())
                .action(action)
                .build();
    }
}
