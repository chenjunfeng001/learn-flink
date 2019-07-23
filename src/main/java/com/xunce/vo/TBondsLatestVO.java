package com.xunce.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/6/24
 * 债券产品维度表
 */
@Data
public class TBondsLatestVO {
    /**
     * 持仓日期
     */
    private Integer L_DATE;
    /**
     * 产品序号
     */
    private Integer L_FUND_ID;

    private String VC_INTER_CODE; //证券内码
    private Double L_CURRENT_AMOUNT ; //当前数量
    private Double L_BUY_AMOUNT ; //当日买入数量
    private Double L_SALE_AMOUNT; //当日卖出数量

    /**
     * 债券类资产
     */
    private BigDecimal EN_BONDS_CASH = BigDecimal.ZERO;

}
