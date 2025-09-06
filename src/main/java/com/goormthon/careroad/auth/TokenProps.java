package com.goormthon.careroad.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class TokenProps {
    private String privateKeyPem;
    private String publicKeyPem;
    private String issuer;
    private int accessMin;
    private int refreshDays;
    private String kid;

    // getters/setters or record-style 생성자도 가능
    public String privateKeyPem(){return privateKeyPem;}
    public String publicKeyPem(){return publicKeyPem;}
    public String issuer(){return issuer;}
    public int accessMin(){return accessMin;}
    public int refreshDays(){return refreshDays;}
    public String kid(){return kid;}
    public void setPrivateKeyPem(String v){this.privateKeyPem=v;}
    public void setPublicKeyPem(String v){this.publicKeyPem=v;}
    public void setIssuer(String v){this.issuer=v;}
    public void setAccessMin(int v){this.accessMin=v;}
    public void setRefreshDays(int v){this.refreshDays=v;}
    public void setKid(String v){this.kid=v;}
}
