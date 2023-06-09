package com.example.rocketmq.batch;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.List;
/**
 * 批量消息示例
 * 批量发送消息能显著提高传递小消息的性能。限制是这些批量消息应该有相同的topic，
 * 相同的waitStoreMsgOK，而且不能是延时消息。此外，这一批消息的总大小不应超过4MB。
 */
public class BatchProducer {

    public static void main(String[] args) throws MQClientException {

        DefaultMQProducer producer = new DefaultMQProducer("batch_producer");
        producer.start();

        String topic = "BatchTest";
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(topic, "TagA", "OrderID001", "Hello world 0".getBytes()));
        messages.add(new Message(topic, "TagA", "OrderID002", "Hello world 1".getBytes()));
        messages.add(new Message(topic, "TagA", "OrderID003", "Hello world 2".getBytes()));
        try {
            SendResult sendResult = producer.send(messages);
            System.out.printf("%s%n", sendResult);
        } catch (Exception e) {
            e.printStackTrace();
            //处理error
        }

        producer.shutdown();
    }

}
