package com.dlut.crazychat.utils;

import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.service.gameService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Aspect
@Data
@Component
public class SystemManager extends Thread{
    @Value("${SystemMessage.ip}")
    private String ip;
    @Value("${SystemMessage.port}")
    private String port;
    @Value("${SystemMessage.topic_name}")
    private String topic_name;
    @Value("${SystemMessage.command_Line}")
    private String command_line;  //指令队列
    @Value("${SystemMessage.groupId}")
    private String groupId;  //指令队列
    @Value("${SystemMessage.userName}")
    private String userName;  //用户名
    private userStat preview_user;  //执行操作前的用户状态
    ExecutorService executor = Executors.newFixedThreadPool(5);
    private KafkaProducer<String, String> kafkaProducer; //生产者
    private KafkaConsumer<String, String> command_listener; //消费者，监听命令队列
    @Autowired
    private gameService gameservice;
    @PostConstruct
    public void init(){
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ip+":"+port);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProducer = new KafkaProducer<>(properties);  //构造生产者
        Properties config_listener = new Properties();
        config_listener.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ip+":"+port);
        config_listener.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config_listener.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config_listener.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        command_listener = new KafkaConsumer<>(config_listener);  //构造消费者
        command_listener.subscribe(Arrays.asList(command_line));  //监听命令对垒
        start();  //监听命令
    }
    public void send(String info){
        Runnable send_task=()->{
            ProducerRecord<String, String> record = new ProducerRecord<>(topic_name,  userName+": "+info+"\n");
            kafkaProducer.send(record);
        };
        executor.execute(send_task);
    }
    public void send(String info,boolean showName){
        //showName表示是否展示系统的名称
        Runnable send_task=()->{
            ProducerRecord<String, String> record;
            if(showName==true){
                record = new ProducerRecord<>(topic_name,  userName+": "+info+"\n");
            }
            else{
                 record = new ProducerRecord<>(topic_name,  info);
            }
            kafkaProducer.send(record);
        };
        executor.execute(send_task);
    }
    public void send(String info,boolean showName,userStat user){
        //showName表示是否展示系统的名称,进行私密发送
        Runnable send_task=()->{
            ProducerRecord<String, String> record;
            if(showName==true){
                record = new ProducerRecord<>(topic_name,  "/toUser:"+user.getId()+"@@"+info+"&&");
            }
            else{
                record = new ProducerRecord<>(topic_name,  "/toUser:"+user.getId()+"@@"+info+"&&");
            }
            kafkaProducer.send(record);
        };
        executor.execute(send_task);
    }
    /*
     * @param info: 发送的私密信息
    	 * @param user: 目标用户
      * @return void
     * @author younghow
     * @description 系统单独给目标用户发送消息
     * @date younghow younghow
     */
    public void send(String info,userStat user){
        Runnable send_task=()->{
            ProducerRecord<String, String> record = new ProducerRecord<>(topic_name,  "/toUser:"+user.getId()+"@@系统私密消息: "+info+"\n&&");
            kafkaProducer.send(record);
        };
        executor.execute(send_task);
    }
    public void send(String info,String userid){
        Runnable send_task=()->{
            String order="/toUser:"+userid+"@@系统私密消息: "+info+"&&";
            ProducerRecord<String, String> record = new ProducerRecord<>(topic_name,  order);
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
    @Override
    public void run(){
        String info;
        while (true) {
            ConsumerRecords<String, String> consumerRecords = command_listener.poll(Duration.ofSeconds(2));
            for (ConsumerRecord<String, String> record : consumerRecords) {
                try {
                    info=record.value(); //获取命令
                    String[] parts = info.split(":");  //命令分割
                    String user_id=parts[0];
                    String command=parts[1];
                    String result=gameservice.process(user_id,command);
                    send(result);
                    Thread.sleep(20); // 线程休眠1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    continue;
                }
            }
        }
    }
}
