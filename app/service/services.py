import os
import requests
from typing import Any, Dict, List, Optional, Tuple

from app import db
from app.models.facility import Facility


class NHISServiceError(Exception):
    pass


def _env(key: str, default: Optional[str] = None) -> str:
    v = os.getenv(key, default)
    if v is None:
        raise NHISServiceError(f"Missing environment variable: {key}")
    return v


class NHISService:
    """
    공공데이터포털 - 장기요양기관 '목록/검색' API 동기화 서비스
    - BASE: LTC_API_BASE (예: https://apis.data.go.kr/B550928/searchLtcInsttService01)
    - METHOD: LTC_API_METHOD (기본: getBillGreentInsttSearchList01)
    - KEY: LTC_API_KEY
    """

    BASE = _env("LTC_API_BASE")                 
    METHOD = _env("LTC_API_METHOD", "getBillGreentInsttSearchList01")
    KEY = _env("LTC_API_KEY")
    TIMEOUT = int(_env("LTC_API_TIMEOUT", "10"))
    ROWS = int(_env("LTC_API_DEFAULT_NUM_OF_ROWS", "100"))

    @staticmethod
    def _get(page: int, rows: int, siDoCd=None, siGunGuCd=None, serviceKind=None, adminNm=None) -> Dict[str, Any]:
        """
        목록 API 호출 (JSON)
        """
        url = f"{NHISService.BASE}/{NHISService.METHOD}"
        params = {
            "serviceKey": NHISService.KEY,
            "_type": "json",
            "pageNo": page,
            "numOfRows": rows,
            "siDoCd": siDoCd,
            "siGunGuCd": siGunGuCd,
            "serviceKind": serviceKind,
            "adminNm": adminNm,
        }
        # 빈 값 제거
        params = {k: v for k, v in params.items() if v not in (None, "", "null")}
        r = requests.get(url, params=params, timeout=NHISService.TIMEOUT)
        r.raise_for_status()
        return r.json()

    @staticmethod
    def _extract_items(payload: dict) -> Tuple[List[Dict[str, Any]], int]:
        """
        response.body.items.item -> list | dict | ""(빈 문자열)
        totalCount -> int
        """
        body = (payload or {}).get("response", {}).get("body", {}) or {}
        total = body.get("totalCount") or 0
        try:
            total = int(total)
        except Exception:
            total = 0

        items = body.get("items", "")
        if items == "" or items is None:
            return [], total

        # items: {"item": [...] } or {"item": {...}}
        if isinstance(items, dict):
            items = items.get("item", [])
        if isinstance(items, dict):
            items = [items]
        if not isinstance(items, list):
            items = []

        return items, total

    @staticmethod
    def _join_phone(item: Dict[str, Any]) -> Optional[str]:
        parts = []
        for k in ("locTelNo_1", "locTelNo_2", "locTelNo_3"):
            v = item.get(k)
            if v is not None and str(v).strip() != "":
                parts.append(str(v).strip())
        return "-".join(parts) if parts else None

    @staticmethod
    def _upsert_one(item: Dict[str, Any]) -> bool:
        """
        facilities 테이블에 한 건 업서트
        - 스키마: name, address, phone 만 사용 (외부코드 컬럼 없다고 가정)
        - 키 충돌 없이 'name' 기준으로 찾고 갱신
        """
        name = (str(item.get("adminNm")).strip() if item.get("adminNm") is not None else None)
        if not name:
            return False

        phone = NHISService._join_phone(item)
        address = None 

        fac = Facility.query.filter_by(name=name).first()
        if not fac:
            fac = Facility(name=name, address=address, phone=phone)
            db.session.add(fac)
        else:
            # 누락값 보충
            if not fac.address and address:
                fac.address = address
            if (not fac.phone) and phone:
                fac.phone = phone

        return True

    @staticmethod
    def sync_facilities(siDoCd=None, siGunGuCd=None, serviceKind=None, adminNm=None, max_pages: Optional[int] = None) -> Dict[str, Any]:
        """
        공공데이터 → DB 동기화 (페이지 전체 순회)
        - 필터 파라미터는 None이면 전체
        - max_pages 지정 시 그 페이지만큼만 수집
        """
        page = 1
        rows = NHISService.ROWS
        total_updated = 0
        total_seen = 0
        total_count_reported = None

        while True:
            payload = NHISService._get(
                page=page,
                rows=rows,
                siDoCd=siDoCd,
                siGunGuCd=siGunGuCd,
                serviceKind=serviceKind,
                adminNm=adminNm,
            )
            items, total = NHISService._extract_items(payload)
            if total_count_reported is None:
                total_count_reported = total

            if not items:
                break

            for it in items:
                total_seen += 1
                if NHISService._upsert_one(it):
                    total_updated += 1

            # 페이지 종료 판정
            if total and page * rows >= total:
                break
            page += 1
            if max_pages is not None and page > max_pages:
                break

        db.session.commit()
        return {
            "requested_filters": {
                "siDoCd": siDoCd, "siGunGuCd": siGunGuCd, "serviceKind": serviceKind, "adminNm": adminNm
            },
            "reported_totalCount": total_count_reported or 0,
            "seen_items": total_seen,
            "upserted": total_updated,
            "page_size": rows
        }
