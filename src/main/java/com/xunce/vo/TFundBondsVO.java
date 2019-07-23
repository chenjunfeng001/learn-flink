package com.xunce.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/6/24
 * 债券产品维度表
 */
@Data
public class TFundBondsVO implements Serializable {
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
     * 债券类资产
     */
    private BigDecimal enBondsCash;

}
