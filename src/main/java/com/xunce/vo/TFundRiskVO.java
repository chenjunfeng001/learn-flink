package com.xunce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author ：junfeng.chen@xuncetech.com
 * @date ：2019/6/24
 * 产品规则汇总表
 */
@Data
public class TFundRiskVO implements Serializable {

    private Long batchNumber;

    private Integer markNumber = 0;
    /**
     * 持仓日期
     */
    private Integer lDate;
    /**
     * 产品序号
     */
    private Integer lFundId;
    /**
     * 总资产
     */
    private BigDecimal totalAsset = BigDecimal.ZERO;
    /**
     * 净资产
     */
    private BigDecimal netAsset = BigDecimal.ZERO;

}
