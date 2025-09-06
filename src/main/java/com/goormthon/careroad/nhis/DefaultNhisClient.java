package com.goormthon.careroad.nhis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** 데모용: 실제 연동은 합의 필요 (엔드포인트/파라미터/파싱) */
@Component
@ConditionalOnProperty(prefix = "app.nhis", name = "enabled", havingValue = "true")
public class DefaultNhisClient implements NhisClient {

    private final RestClient http;
    private final NhisProperties props;

    public DefaultNhisClient(RestClient nhisRestClient, NhisProperties props) {
        this.http = nhisRestClient;
        this.props = props;
    }

    @Override
    public List<NhisFacilityPayload> fetchFacilities(int page, int pageSize) {
        // TODO: props.getApiKey() 를 헤더/쿼리에 붙여 실제 호출
        // return http.get().uri("/facilities?page={p}&size={s}&key={k}", page, pageSize, props.getApiKey())
        //         .retrieve().body(new ParameterizedTypeReference<List<NhisFacilityPayload>>(){});
        return List.of(); // 데모: 빈 목록
    }
}
