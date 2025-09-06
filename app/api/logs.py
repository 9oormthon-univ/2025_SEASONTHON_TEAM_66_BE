

# app/api/logs.py
from flask import Blueprint, jsonify, request
from sqlalchemy import text
from app import db

logs_bp = Blueprint("logs", __name__)

@logs_bp.route("/seed-one", methods=["POST"])
def seed_one():
    """
    logs 테이블에 더미 한 줄 INSERT (동작 확인용)
    """
    sql = text("""
        INSERT INTO logs (endpoint, method, status_code, response_time, details)
        VALUES (:endpoint, :method, :status_code, :response_time, :details)
    """)
    params = {
        "endpoint": "/demo",
        "method": "GET",
        "status_code": 200,
        "response_time": 12.3,
        "details": "manual seed"
    }
    with db.engine.begin() as conn:
        conn.execute(sql, params)
    return jsonify({"ok": True})

@logs_bp.route("/recent", methods=["GET"])
def recent():
    """
    최근 로그 조회
    """
    limit = int(request.args.get("limit", 10))
    rows = []
    with db.engine.begin() as conn:
        rs = conn.execute(
            text("""
                SELECT log_id, request_time, endpoint, method, status_code, response_time
                FROM logs
                ORDER BY log_id DESC
                LIMIT :limit
            """),
            {"limit": limit}
        )
        for r in rs.mappings():
            rows.append(dict(r))
    return jsonify(rows), 200
