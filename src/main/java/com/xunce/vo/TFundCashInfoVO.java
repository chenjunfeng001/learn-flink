package com.xunce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/6/24
 * 资产信息产品维度表
 */
@Data
public class TFundCashInfoVO implements Serializable {
    /**
     * 批次
     */
    private Long batchNumber;
    /**
     * 持仓日期
     */
    private Integer lDate;
    /**
     * 产品序号
     */
    private Integer lFundId;

    /**
     * 总应付
     */
    private BigDecimal enOtherExpense;

    /**
     * 总应收
     */
    private BigDecimal enOtherProfit;

}
