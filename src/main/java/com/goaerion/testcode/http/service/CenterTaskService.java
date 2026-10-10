package com.goaerion.testcode.http.service;

import com.goaerion.testcode.http.dto.centertask.Button;
import com.goaerion.testcode.http.dto.centertask.ButtonConfig;
import com.goaerion.testcode.http.dto.centertask.ButtonInformation;
import com.goaerion.testcode.http.dto.centertask.CenterTask;

import java.util.Map;

/**
 * 任务中心服务
 */
public interface CenterTaskService {

    /**
     * 构建任务中心新增任务的请求体（按钮等子结构由本服务组装）
     *
     * @param currentUserId 当前用户 id，作为任务创建人 / 接受人
     */
    CenterTask buildCenterTask(String currentUserId);

    /**
     * 构建按钮列表，pc / mobile / innerNet 共用同一组按钮（处理、查看）
     */
    ButtonInformation buildButtonInformation();

    /**
     * 构建按钮共用配置模板（使用默认参数值，委托给全参重载）
     */
    ButtonConfig buildButtonConfig();

    /**
     * 构建按钮共用配置模板（常用参数，其余取默认值）
     */
    ButtonConfig buildButtonConfig(String buttonName, Map<String, String> params,
            String path, String protocol, String ip);

    /**
     * 构建按钮共用配置模板（全参数）
     *
     * @param buttonName 按钮名称
     * @param highLight  是否高亮
     * @param actionType 动作类型，如：redirect
     * @param params     请求参数
     * @param ifToken    是否携带 C 大脑 token
     * @param path       问号之前的相对路径
     * @param protocol   请求方式：http/https
     * @param ip         当前服务 ip
     */
    ButtonConfig buildButtonConfig(String buttonName, Boolean highLight, String actionType,
            Map<String, String> params, Boolean ifToken, String path, String protocol, String ip);

    /**
     * 按参数对象构建单个按钮
     */
    Button buildButton(ButtonConfig config);
}
