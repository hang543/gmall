package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;


import org.junit.Before;
import org.junit.Test;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
public class JwtTest {
    private static final String pubKeyPath = "I:\\rsa\\rsa.pub";

    private static final String priKeyPath = "I:\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234##@@@dsqui");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 2);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        //String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzAxMjEyODZ9.GioCiqMt_ZcN6_RAuDBcOzcHQ5WdqdhA9QYu-2IqCQqnAef1VyXczEInj1Ef1xo7AvcjxnkIMuZK48OoczUy1iqtPQPDchUzTl03b8h_J3xMBaxOAaKSwMpm20DH25VrTgBExUafyxHwxfOa-PVHW0Kk41KrWDncayzXbZ_lYLoa9Cuvacr8eAFz-ckriIiZ9bRzFkhX-wYHSHFlym2IJRjBRhFtpkN5GLAVsmsdm-yD4eiJXqioWspqXiBSdROsjrTRiFe511yujR0y2ngL9OnZ1QH6bHDQ2WmhPTrswKjjy-HWIxk1FQ7uXtSpPa5diymmPVTWA0clys7R1MK9oQ";
       // String token="eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1ODY2ODI5MTh9.vjpWKTzxZPq8P2LqskoNeEc0V10dNtHWngBaSHDHbnjtIIt_2AFdd8FSxHf0fhgW-_VOp1PWzCz2aNFekRtsLfKTz5Hw9eHDPoNPOWFRC8QtxBcTyUT2mQE57SWGIvwzvuYwRaZb1auffr89l58I5F3s9mvoDIVekIMKVlv-nUDB0xVR8UyaT07r9IS-D5UHfXfdX4ooqir6URmWR58WXyrk2pTaar_CI_9biVasPRA9NOTcZmuLGFUw1CL1-B0asFBM4cs47BoxaJEvqR4c6TDAAQd9AhhI7nl4zHXnlKgL3oPe0W1oCnyAGUAVw2rdmKQWPi8Kf-t0APWi5ZPi-w";
       String token="eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1ODY2ODMxNTN9.PMPoTuF0HS4ZYa-THeyqGa5bUcKhXlXRsKglTiA50m4Tq87Ubv8HvCdQnYeKSEMSO5U7xYeIACVpXhnoXHC7FWuro6gw8mzmvZstfVzj2H6ii1QLOHqZobYhN4PvRnvn6HqGfztE4fNx_21ZV72_GGtqwr19nAFRLSRet9mFv8_N9Xf8t5MkHdZ2MSTRrlVyYKwXQb8LIa1ignK0a0P_q5lObUKOzxmFJ1AXPlKfSEB--FXcuC3I6OjPlqcXEKFU_PGVS7mfhluldABZPmqlrnTODZK51x-wMjfJ7PvKSB_e4gX2htr6nfDzA97bk_x60FW8oM66DZYaj1vxw7C2vw";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
