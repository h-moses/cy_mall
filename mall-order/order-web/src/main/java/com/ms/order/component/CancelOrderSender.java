package com.ms.order.component;

import com.ms.order.config.RabbitmqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CancelOrderSender {

    @Resource
    private AmqpTemplate amqpTemplate;

    public void sendCancelMessage(String orderNo, final Integer delayTime) {
        amqpTemplate.convertAndSend(RabbitmqConfig.CANCEL_EXCHANGE_NAME, RabbitmqConfig.CANCEL_ROUTING_KEY, orderNo, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(delayTime * 1000);
                return message;
            }
        });
        log.info("RabbitMQ 已经发送定时取消的消息，订单编号为：{}", orderNo);
    }
}
