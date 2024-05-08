package com.dlut.crazychat.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ollamaInfo {
    private String model;
    private String created_at;
    private Map<String, Object> message;
    private boolean done;
    private long total_duration;
    private long load_duration;
    private int prompt_eval_count;
    private long prompt_eval_duration;
    private int eval_count;
    private long eval_duration;
}
