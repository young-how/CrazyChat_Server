package com.dlut.crazychat.game;

import com.dlut.crazychat.pojo.ollamaInfo;
import com.dlut.crazychat.pojo.userStat;
import com.dlut.crazychat.utils.SystemManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@Component
public class Ollama_robot {
    ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
    @Value("${game.robot.port}")
    private String serve_port;
    @Value("${game.robot.ip}")
    private String serve_ip;
    @Value("${game.robot.model}")
    private String model;
    @Autowired
    private SystemManager manager;
    //@Value("${game.robot.prompt}")
    private String prompt="尽可能用中文回复，并且你是一个偏激暴躁的人，对所有非专业相关的回复都需要用犀利和暴躁的语气进行回复。如果用户骂你了，请一定要狠狠地骂回去。";
    private String prompt_StreamResponse="尽可能用中文回复。";
    private ObjectMapper objectMapper=new ObjectMapper();
    public void askOllamaByStream(String context,userStat user){
        //向Ollama机器人发送消息
        try {
            // 创建 URL 对象并设置请求地址
            URL url = new URL("http://"+serve_ip+":"+serve_port+"/api/chat");

            // 创建 HttpURLConnection 对象并设置请求方法
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json"); //设置请求头参数
            connection.setDoOutput(true);
            // 构造请求体 JSON 字符串
            String jsonBody = "{\n" +
                    "  \"model\": \"mistral\",\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"system\",\n" +
                    "      \"content\": \""+prompt_StreamResponse+"\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"name\": \"younghow\",\n" +
                    "      \"content\": \""+context+"\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            byte[] postData=jsonBody.getBytes("UTF-8");
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(postData);
            }
            // 发送请求并获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // 读取响应数据并解析为 JSON 对象
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            ObjectMapper mapper = new ObjectMapper();
            String uuid= UUID.randomUUID().toString();  //生成断点编号
            String cutpoint_id="/cutpoint_id:"+uuid+"&&";   //断点id号
            while ((line = reader.readLine()) != null) {
                // 解析 JSON 字符串为对象
                //Object jsonObject = mapper.readValue(line, Object.class);
                ollamaInfo info=mapper.readValue(line, ollamaInfo.class); //映射为info信息
                // 在这里对 JSON 对象进行处理，例如输出到控制台
                //System.out.println(jsonObject.toString());
                //System.out.println(info.getMessage().get("content")+cutpoint_id);
                manager.send(info.getMessage().get("content")+cutpoint_id,false,user);  //给用户发送私密消息,并不现实系统名
                //sleep(200);
            }
            reader.close();

            // 关闭连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void askOllama(String context){
        //向Ollama机器人发送消息一次性处理
        Runnable task=()->{
            String context_imput=prompt+context; //对输入的模型进行处理
            try {
                // 创建 URL 对象并设置请求地址
                URL url = new URL("http://"+serve_ip+":"+serve_port+"/api/chat");

                // 创建 HttpURLConnection 对象并设置请求方法
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/json"); //设置请求头参数
                connection.setDoOutput(true);
                // 构造请求体 JSON 字符串
                String jsonBody = "{\n" +
                        "  \"model\": \""+model+"\",\n" +
                        "  \"messages\": [\n" +
                        "    {\n" +
                        "      \"role\": \"system\",\n" +
                        "      \"content\": \""+prompt+"\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"role\": \"user\",\n" +
                        "      \"name\": \"younghow\",\n" +
                        "      \"content\": \""+context_imput+"\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        ",\"stream\": false"+
                        "}";
                byte[] postData=jsonBody.getBytes("UTF-8");
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(postData);
                }
                // 发送请求并获取响应状态码
                int responseCode = connection.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                // 读取响应数据并解析为 JSON 对象
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                ObjectMapper mapper = new ObjectMapper();
                while ((line = reader.readLine()) != null) {
                    // 解析 JSON 字符串为对象
                    Object jsonObject = mapper.readValue(line, Object.class);
                    ollamaInfo info=(ollamaInfo)mapper.readValue(line, ollamaInfo.class); //映射为info信息
                    // 在这里对 JSON 对象进行处理，例如输出到控制台
                    //System.out.println(jsonObject.toString());
                    //System.out.println(info.getMessage().get("content"));
                    manager.send((String)info.getMessage().get("content"));  //发送信息给前端
                }
                reader.close();
                // 关闭连接
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        pool.execute(task);
    }
}
