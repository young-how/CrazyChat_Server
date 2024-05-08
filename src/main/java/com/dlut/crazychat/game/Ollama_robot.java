package com.dlut.crazychat.game;

import com.dlut.crazychat.pojo.ollamaInfo;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    @Value("${game.robot.prompt}")
    private String prompt;
    private ObjectMapper objectMapper=new ObjectMapper();
    public void askOllamaByStream(String context){
        //向Ollama机器人发送消息
        try {
            // 创建 URL 对象并设置请求地址
            URL url = new URL("http://10.7.8.7:11434/api/chat");

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
            while ((line = reader.readLine()) != null) {
                // 解析 JSON 字符串为对象
                Object jsonObject = mapper.readValue(line, Object.class);
                ollamaInfo info=(ollamaInfo)mapper.readValue(line, ollamaInfo.class); //映射为info信息
                // 在这里对 JSON 对象进行处理，例如输出到控制台
                //System.out.println(jsonObject.toString());
                System.out.println(info.getMessage().get("content"));
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
                        "  \"model\": \"mistral\",\n" +
                        "  \"messages\": [\n" +
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
