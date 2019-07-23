package com.xunce.stream;



import com.xunce.utils.DateUtils;
import com.xunce.utils.JSONUtil;
import com.xunce.utils.KafkaHelper;
import com.xunce.utils.SnowflakeUUID;
import com.xunce.vo.TFundBondsVO;
import com.xunce.vo.TFundCashInfoVO;
import com.xunce.vo.TFundSharesVO;


import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

/**
 * 模拟生成产品维度表数据
 * @author junfeng.chen@xuncetech.com
 * @date 2019/6/29
 */
public class KafkaProducer {
    public static void main(String[] args) throws InterruptedException {

        int i = 0;
        while (true){
            TFundSharesVO tFundSharesVO = new TFundSharesVO();
            TFundBondsVO tFundBondsVO = new TFundBondsVO();

            TFundCashInfoVO tFundCashInfoVO = new TFundCashInfoVO();


            Random random = new Random();

            Integer fundId = random.nextInt(10);
            Integer day = DateUtils.getRemainSecondsOneDay(new Date());
            long uuid = SnowflakeUUID.getInstance().uuid();


            tFundBondsVO.setLFundId(fundId);
            tFundBondsVO.setLDate(day);
            tFundBondsVO.setBatchNumber(uuid);
            tFundBondsVO.setEnBondsCash(new BigDecimal(18+ i));

            tFundSharesVO.setLFundId(fundId);
            tFundSharesVO.setLDate(day);
            tFundSharesVO.setBatchNumber(uuid);
            tFundSharesVO.setEnSharesCash(new BigDecimal(2+i));


            tFundCashInfoVO.setLFundId(fundId);
            tFundCashInfoVO.setLDate(day);
            tFundCashInfoVO.setBatchNumber(uuid);
            tFundCashInfoVO.setEnOtherProfit(new BigDecimal(23+i ));
            tFundCashInfoVO.setEnOtherExpense(new BigDecimal(10+i));

            String tFundSharesVOStr = JSONUtil.getBeanToJson(tFundSharesVO);
            String tFundBondsVOStr = JSONUtil.getBeanToJson(tFundBondsVO);
            String tFundCashInfoVOStr = JSONUtil.getBeanToJson(tFundCashInfoVO);


            // 1的股票产品维度表
            KafkaHelper.saveData("xc-producer01", "1", tFundSharesVOStr);
            System.out.println("tFundSharesVOStr ： "+ tFundSharesVOStr);
            // 2写入债券产品维度表
            KafkaHelper.saveData("xc-producer01", "2",tFundBondsVOStr );
            System.out.println("tFundBondsVOStr ： "+ tFundBondsVOStr);
            // 3资产信息产品维度表
            KafkaHelper.saveData("xc-producer01", "3", tFundCashInfoVOStr);
            System.out.println("tFundCashInfoVOStr ： "+ tFundCashInfoVOStr);


            Thread.sleep(10000);
            i++;

        }


    }
}
