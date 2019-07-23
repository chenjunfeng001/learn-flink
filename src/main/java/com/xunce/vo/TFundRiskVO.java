package com.xunce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author ：junfeng.chen@xuncetech.com
 * @date ：2019/6/24
 * 产品规则表
 */
@Data
public class TFundRiskVO implements Serializable {

    private Long BATCH_NUMBER;

    private Integer MARK_NUMBER = 0;
    /**
     * 日期
     */
    private Integer L_DATE;
    /**
     * 产品序号
     */
    private Integer L_FUND_ID;
    /**
     * 总资产
     */
    private BigDecimal TOTAL_ASSET = BigDecimal.ZERO;
    /**
     * 净资产
     */
    private BigDecimal NET_ASSET = BigDecimal.ZERO;

}
