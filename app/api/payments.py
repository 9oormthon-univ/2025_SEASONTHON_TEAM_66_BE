from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.models.facility import Facility

payments_bp = Blueprint('payments', __name__)

def calculate_monthly_payments(base_price, months):
    """
    단순 월비용 계산:
    - 월비용 = facility.monthly_cost (할인/시즌 없음)
    - 총액   = 월비용 * months
    """
    base = float(base_price)
    payments = []
    for i in range(months):
        payments.append({
            "month_index": i + 1,
            "amount": round(base, 0)  # 원 단위 반올림
        })
    return payments


@payments_bp.route('/calculate', methods=['POST'])
@jwt_required()
def calculate_payment():
    """
    단순 견적 계산 (기본 1개월)
    Request JSON:
      - facility_id (int, required)
      - membership_months (int, optional, default 1, 1~24)
    Response:
      - facility_id, facility_name, months
      - monthly_payments: [{month_index, amount}, ...]
      - total_amount
      - average_monthly_amount
    """
    try:
        _ = get_jwt_identity()  # 로그인 검증 목적
        data = request.get_json() or {}

        # 입력 검증
        if not data.get('facility_id'):
            return jsonify({"success": False, "error": "시설 ID가 필요합니다."}), 400

        months = int(data.get('membership_months', 1))
        if not (1 <= months <= 24):
            return jsonify({"success": False, "error": "개월 수는 1~24 사이여야 합니다."}), 400

        # 시설 조회
        facility = Facility.query.get(data['facility_id'])
        if not facility:
            return jsonify({"success": False, "error": "시설을 찾을 수 없습니다."}), 404

        # 계산
        monthly_payments = calculate_monthly_payments(facility.monthly_cost, months)
        total_amount = float(sum(p['amount'] for p in monthly_payments))
        average_monthly = round(total_amount / months, 0)

        return jsonify({
            "success": True,
            "data": {
                "facility_id": facility.facility_id,
                "facility_name": facility.name,
                "months": months,
                "monthly_payments": monthly_payments,
                "total_amount": total_amount,
                "average_monthly_amount": average_monthly
            }
        }), 200

    except Exception as e:
        return jsonify({"success": False, "error": "결제 계산에 실패했습니다.", "details": str(e)}), 500


@payments_bp.route('/compare', methods=['POST'])
@jwt_required()
def compare_payments():
    """
    두 시설 총액 비교 (단순 월비용 x 개월수)
    Request JSON:
      - facility_a_id (int, required)
      - facility_b_id (int, required)
      - membership_months (int, optional, default 1, 1~24)
    Response:
      - facility_a: {facility_id, facility_name, monthly_payments, total_amount}
      - facility_b: {facility_id, facility_name, monthly_payments, total_amount}
      - comparison: {total_difference, cheaper_facility}
    """
    try        :
        _ = get_jwt_identity()
        data = request.get_json() or {}

        # 입력 검증
        required = ['facility_a_id', 'facility_b_id']
        for f in required:
            if not data.get(f):
                return jsonify({"success": False, "error": f"{f}가 필요합니다."}), 400

        months = int(data.get('membership_months', 1))
        if not (1 <= months <= 24):
            return jsonify({"success": False, "error": "개월 수는 1~24 사이여야 합니다."}), 400

        # 시설 조회
        fa = Facility.query.get(data['facility_a_id'])
        fb = Facility.query.get(data['facility_b_id'])
        if not fa or not fb:
            return jsonify({"success": False, "error": "시설을 찾을 수 없습니다."}), 404

        # 계산
        payments_a = calculate_monthly_payments(fa.monthly_cost, months)
        payments_b = calculate_monthly_payments(fb.monthly_cost, months)
        total_a = float(sum(p['amount'] for p in payments_a))
        total_b = float(sum(p['amount'] for p in payments_b))

        cheaper = fa.name if total_a < total_b else fb.name

        return jsonify({
            "success": True,
            "data": {
                "facility_a": {
                    "facility_id": fa.facility_id,
                    "facility_name": fa.name,
                    "monthly_payments": payments_a,
                    "total_amount": total_a
                },
                "facility_b": {
                    "facility_id": fb.facility_id,
                    "facility_name": fb.name,
                    "monthly_payments": payments_b,
                    "total_amount": total_b
                },
                "comparison": {
                    "total_difference": float(total_a - total_b),
                    "cheaper_facility": cheaper
                }
            }
        }), 200

    except Exception as e:
        return jsonify({"success": False, "error": "비교 계산에 실패했습니다.", "details": str(e)}), 500
