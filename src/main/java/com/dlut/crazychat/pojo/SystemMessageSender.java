package com.dlut.crazychat.pojo;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Aspect
@Component
public class SystemMessageSender {
    @Value("${SystemMessage.ip}")
    private String ip;
    @Value("${SystemMessage.port}")
    private String port;
    @Value("${SystemMessage.topic_name}")
    private String topic_name;
    @Value("${SystemMessage.userName}")
    private String userName;  //用户名
    private userStat preview_user;  //执行操作前的用户状态
    ExecutorService executor = Executors.newFixedThreadPool(5);
    private KafkaProducer<String, String> kafkaProducer; //生产者
    @PostConstruct
    public void init(){
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ip+":"+port);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProducer = new KafkaProducer<>(properties);
    }
    public void send(String info){
        Runnable send_task=()->{
            ProducerRecord<String, String> record = new ProducerRecord<>(topic_name,  userName+": "+info);
            kafkaProducer.send(record);
        };
        executor.execute(send_task);
    }
    @Before("execution(* com.dlut.crazychat.service.userService.*(..)) && args(user,..)")
    public void systemEvents_before(userStat user){
        //系统事件.监听service操作前的用户状态，是一个加强方法，用于增强service中的方法
        preview_user=user;
        System.out.println("得到输入状态");

    }
    @AfterReturning(value="execution(* com.dlut.crazychat.service.userService.*(..))",returning ="user")
    public void systemEvents_after(userStat user){
        //系统事件.监听service操作后的用户状态，是一个加强方法，用于增强service中的方法
        //升级事件
        if(preview_user.getLevel()!=user.getLevel()){
            send("恭喜用户:"+user.getName()+" 升级至: "+user.getLevel()+" !!!");
        }
    }
}
