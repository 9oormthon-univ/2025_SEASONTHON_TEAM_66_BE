package com.goormthon.careroad.nhis;

import java.util.List;

public interface NhisClient {
    List<NhisFacilityPayload> fetchFacilities(int page, int pageSize);
}
