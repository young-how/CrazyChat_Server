package com.dlut.crazychat.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class rankList {
    private List<userStat> users=new ArrayList<>();
    public void addUsrRank(userStat user){
        users.add(user);
    }
}
