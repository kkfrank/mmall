package com.mmall.task;

import com.mmall.service.OrderService;
import com.mmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CloseOrderTask {
    private static Logger logger = LoggerFactory.getLogger(CloseOrderTask.class);

    @Autowired
    private OrderService orderService;

    @Scheduled(cron="0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrderTaskV1(){
        logger.info("关闭订单定时任务start");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));
        orderService.closeOrder(hour);
        logger.info("关闭订单定时任务end");
    }
}
