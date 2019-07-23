package com.xunce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 回购产品维度表
 * @author junfeng.chen@xuncetech.com
 * @date 2019/7/3
 */
@Data
public class TFundHgregisterVO implements Serializable {
    private Long BATCH_NUMBER;
    /**
     * 持仓日期
     */
    private Integer L_DATE;
    /**
     * 产品序号
     */
    private Integer L_FUND_ID;

    /**
     * 正回购资产
     */
    private BigDecimal EN_RZ_HGREGISTER_CASH = BigDecimal.ZERO;

    /**
     * 逆回购资产
     */
    private BigDecimal EN_RQ_HGREGISTER_CASH = BigDecimal.ZERO;

}
