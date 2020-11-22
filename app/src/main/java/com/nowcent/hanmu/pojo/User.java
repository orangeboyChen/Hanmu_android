package com.nowcent.hanmu.pojo;


import lombok.Data;

@Data
public class User {
    private String userName;
    private String schoolName;
    private double minSpeed;
    private double maxSpeed;
    private String iemiCode;
    private String token;
    private String runId;
    private long costTime;
    private int distance;
    private long step;
}
