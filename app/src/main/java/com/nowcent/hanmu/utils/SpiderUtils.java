package com.nowcent.hanmu.utils;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcent.hanmu.pojo.User;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.SSLContext;

import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.conn.ssl.NoopHostnameVerifier;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.ssl.SSLContexts;
import cz.msebera.android.httpclient.ssl.TrustStrategy;
import cz.msebera.android.httpclient.util.EntityUtils;

public class SpiderUtils {

    private static SSLContext sslContext;

    static {
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        return true;
                    }
                }).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    private static CloseableHttpClient client = HttpClients.custom().setSslcontext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();


    private static final String ENCRYPT_KEY = "xfvdmyirsg";
    private static final String LOGIN_URL = "https://client4.aipao.me/api/token/QM_Users/LoginSchool?IMEICode=";

    private static final String INFO_URL_PREFIX = "http://client3.aipao.me/api/";
    private static final String INFO_URL_SUFFIX = "/QM_Users/GS";

    private static final String RUN_ID_PREFIX = "http://client3.aipao.me/api/";
    private static final String RUN_ID_SUFFIX = "/QM_Runs/SRS?S1=40.62828&S2=120.79108&S3=";

    /**
     * 登录
     * @param imeiCode 需要的imei号
     * @return JSON
     */
    public static JSONObject login(String imeiCode) throws IOException {
        HttpGet request = new HttpGet(LOGIN_URL + imeiCode);
        CloseableHttpResponse response = client.execute(request);
        return JSON.parseObject(EntityUtils.toString(response.getEntity()));
    }

    public static JSONObject getUserInfo(String token) throws IOException {
        HttpGet httpGet = new HttpGet(INFO_URL_PREFIX + token + INFO_URL_SUFFIX);
        CloseableHttpResponse response = client.execute(httpGet);
        return JSON.parseObject(EntityUtils.toString(response.getEntity()));
    }

    public static String getRunId(String token, int distance) throws IOException {
        HttpGet httpGet = new HttpGet(RUN_ID_PREFIX + token + RUN_ID_SUFFIX + distance);
        CloseableHttpResponse response = client.execute(httpGet);
        JSONObject json = JSON.parseObject(EntityUtils.toString(response.getEntity()));
        if(json.getBoolean("Success")){
            return json.getJSONObject("Data").getString("RunId");
        }
        else{
            return null;
        }
    }

    public static JSONObject postFinishRunning(User user) throws IOException {
        Random r = new Random();
        double postSpeed = (r.nextInt((int) (user.getMaxSpeed() * 100 + 30 - user.getMinSpeed() * 100 + 50)) + user.getMinSpeed() * 100 + 30) / 100;
        Log.e("speed", String.valueOf(postSpeed));
        double postDistance = user.getDistance() + r.nextInt(5);
        int postCostTime = (int) (postDistance / postSpeed);
        int postStep = r.nextInt(2222 - 1555) + 1555;

        HttpGet httpGet = new HttpGet("http://client3.aipao.me/api/" +
                user.getToken() +
                "/QM_Runs/ES?" +
                "S1=" +
                user.getRunId() +
                "&S4=" +
                encryptNumber(postCostTime) +
                "&S5=" +
                encryptNumber((int) postDistance) +
                "&S6=A0A2A1A3A0&S7=1&S8=xfvdmyirsg&S9=" +
                encryptNumber(postStep));

        CloseableHttpResponse response = client.execute(httpGet);
        return JSON.parseObject(EntityUtils.toString(response.getEntity()));
    }

    private static String encryptNumber(int number){
        char[] numberChars = String.valueOf(number).toCharArray();
        StringBuilder stringBuilder = new StringBuilder();

        for (char numberChar : numberChars) {
            stringBuilder.append(ENCRYPT_KEY.charAt(numberChar - '0'));
        }

        return stringBuilder.toString();
    }


}
