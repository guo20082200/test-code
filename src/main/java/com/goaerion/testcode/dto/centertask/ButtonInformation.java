package com.goaerion.testcode.dto.centertask;

import java.util.List;

/**
 * 按钮列表，按当前列表顺序排列，显隐问题不再由个人指挥中心控制
 */
public record ButtonInformation(
        /** PC 端按钮 */
        List<Button> pc,
        /** 移动端按钮 */
        List<Button> mobile,
        /** 内网按钮 */
        List<Button> innerNet) {
}
