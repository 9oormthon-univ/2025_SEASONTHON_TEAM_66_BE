package com.goormthon.careroad.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@RestController
public class JwksController {
    @Value("${app.jwt.public-key-pem}")
    private String publicKeyPem;
    @Value("${app.jwt.kid}")
    private String kid;

    private final ResourceLoader loader;

    public JwksController(ResourceLoader loader){ this.loader = loader; }

    @GetMapping(value="/oauth/jwks.json", produces=MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() throws Exception {
        String pem = Files.readString(loader.getResource(publicKeyPem).getFile().toPath(), StandardCharsets.UTF_8);
        String body = pem.replace("-----BEGIN PUBLIC KEY-----","").replace("-----END PUBLIC KEY-----","").replaceAll("\\s","");
        byte[] der = Base64.getDecoder().decode(body);
        RSAPublicKey pk = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));

        String n = Base64.getUrlEncoder().withoutPadding().encodeToString(pk.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding().encodeToString(pk.getPublicExponent().toByteArray());

        Map<String, String> jwk = Map.of(
                "kty","RSA","kid",kid,"alg","RS256","use","sig",
                "n", trimLeadingZeroUrl(n), "e", e
        );
        return Map.of("keys", new Object[]{ jwk });
    }

    // modulus 앞 0x00 바이트 제거 보정
    private static String trimLeadingZeroUrl(String urlBase64) {
        byte[] b = Base64.getUrlDecoder().decode(urlBase64);
        if (b.length>0 && b[0]==0x00) {
            byte[] t = new byte[b.length-1];
            System.arraycopy(b,1,t,0,t.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(t);
        }
        return urlBase64;
    }
}
