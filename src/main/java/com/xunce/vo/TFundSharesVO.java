package com.xunce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/6/24
 * 股票产品维度表
 */
@Data
public class TFundSharesVO implements Serializable {
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
     * 股票类资产
     */
    private BigDecimal enSharesCash;
}
