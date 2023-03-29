package com.ms.order.component;

import com.ms.common.enums.ServiceResultEnum;
import com.ms.order.config.RabbitmqConfig;
import com.ms.order.service.MallOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@RabbitListener(queues = RabbitmqConfig.CANCEL_QUEUE_NAME)
@Component
@Slf4j
public class CancelOrderListener {

    @Resource
    private MallOrderService orderService;

    @RabbitHandler
    public void cancelOrder(String orderNo) {
        log.info("RabbitMQ监听器已经收到订单编号为{}的消息，正在进行取消...", orderNo);
        String res = orderService.cancelTimeoutOrder(orderNo);
        if (res.equals(ServiceResultEnum.ORDER_SUCCESS_CANCEL.getResult())) {
            log.info("RabbitMQ：编号为{}的订单已经完成取消！", orderNo);
        } else {
            log.info("RabbitMQ：编号为{}的订单不存在，请联系管理员！", orderNo);
        }
    }
}
