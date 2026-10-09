package com.goaerion.testcode.dto.centertask;

import java.util.List;
import java.util.Map;

/**
 * 个人指挥中心任务实体（对应 dto/centertask/a.json），手写建造者模式
 */
public record CenterTask(
                Long id,
                Integer tenantId,
                String mainTitle,
                String subTitle,
                String taskType,
                String taskSource,
                String processStatus,
                String taskFounderId,
                String taskFounderName,
                String taskAcceptorId,
                String taskAcceptorName,
                String taskArriveTime,
                String taskPlanFinishTime,
                String taskRealFinishTime,
                List<String> markInformation,
                String businessId,
                List<KeyValueInfo> labelInfo,
                List<KeyValueInfo> visibleInformation,
                ButtonInformation buttonInformation,
                Map<String, String> exchangeInformation) {

        public static Builder builder() {
                return new Builder();
        }

        public static class Builder {
                private Long id;
                private Integer tenantId;
                private String mainTitle;
                private String subTitle;
                private String taskType;
                private String taskSource;
                private String processStatus;
                private String taskFounderId;
                private String taskFounderName;
                private String taskAcceptorId;
                private String taskAcceptorName;
                private String taskArriveTime;
                private String taskPlanFinishTime;
                private String taskRealFinishTime;
                private List<String> markInformation;
                private String businessId;
                private List<KeyValueInfo> labelInfo;
                private List<KeyValueInfo> visibleInformation;
                private ButtonInformation buttonInformation;
                private Map<String, String> exchangeInformation;

                /** 任务id（响应体返回） */
                public Builder id(Long id) {
                        this.id = id;
                        return this;
                }

                /** 租户id，必填 */
                public Builder tenantId(Integer tenantId) {
                        this.tenantId = tenantId;
                        return this;
                }

                /** 主标题，必填 */
                public Builder mainTitle(String mainTitle) {
                        this.mainTitle = mainTitle;
                        return this;
                }

                /** 副标题 */
                public Builder subTitle(String subTitle) {
                        this.subTitle = subTitle;
                        return this;
                }

                /** 任务类型，必填 */
                public Builder taskType(String taskType) {
                        this.taskType = taskType;
                        return this;
                }

                /** 任务来源 */
                public Builder taskSource(String taskSource) {
                        this.taskSource = taskSource;
                        return this;
                }

                /** 任务状态，必填 */
                public Builder processStatus(String processStatus) {
                        this.processStatus = processStatus;
                        return this;
                }

                /** 发起人id，必填 */
                public Builder taskFounderId(String taskFounderId) {
                        this.taskFounderId = taskFounderId;
                        return this;
                }

                /** 发起人姓名 */
                public Builder taskFounderName(String taskFounderName) {
                        this.taskFounderName = taskFounderName;
                        return this;
                }

                /** 接收人id，必填 */
                public Builder taskAcceptorId(String taskAcceptorId) {
                        this.taskAcceptorId = taskAcceptorId;
                        return this;
                }

                /** 接收人姓名 */
                public Builder taskAcceptorName(String taskAcceptorName) {
                        this.taskAcceptorName = taskAcceptorName;
                        return this;
                }

                /** 任务到达时间 */
                public Builder taskArriveTime(String taskArriveTime) {
                        this.taskArriveTime = taskArriveTime;
                        return this;
                }

                /** 任务计划完成时间 */
                public Builder taskPlanFinishTime(String taskPlanFinishTime) {
                        this.taskPlanFinishTime = taskPlanFinishTime;
                        return this;
                }

                /** 任务实际完成时间 */
                public Builder taskRealFinishTime(String taskRealFinishTime) {
                        this.taskRealFinishTime = taskRealFinishTime;
                        return this;
                }

                /** 标记，如：催办、转办 */
                public Builder markInformation(List<String> markInformation) {
                        this.markInformation = markInformation;
                        return this;
                }

                /** 任务来源方的唯一标识 */
                public Builder businessId(String businessId) {
                        this.businessId = businessId;
                        return this;
                }

                /** 标签配置，可选 */
                public Builder labelInfo(List<KeyValueInfo> labelInfo) {
                        this.labelInfo = labelInfo;
                        return this;
                }

                /** 可见字段，key-参数名，value-值 */
                public Builder visibleInformation(List<KeyValueInfo> visibleInformation) {
                        this.visibleInformation = visibleInformation;
                        return this;
                }

                /** 按钮列表 */
                public Builder buttonInformation(ButtonInformation buttonInformation) {
                        this.buttonInformation = buttonInformation;
                        return this;
                }

                /** 数据交换字段，自定义 key-value */
                public Builder exchangeInformation(Map<String, String> exchangeInformation) {
                        this.exchangeInformation = exchangeInformation;
                        return this;
                }

                public CenterTask build() {
                        return new CenterTask(id, tenantId, mainTitle, subTitle, taskType, taskSource,
                                        processStatus, taskFounderId, taskFounderName, taskAcceptorId, taskAcceptorName,
                                        taskArriveTime, taskPlanFinishTime, taskRealFinishTime, markInformation,
                                        businessId, labelInfo, visibleInformation, buttonInformation,
                                        exchangeInformation);
                }
        }
}
