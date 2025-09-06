
from flask import Blueprint, jsonify
misc_bp = Blueprint('misc', __name__)

@misc_bp.route('/health', methods=['GET'])
def health():
    return jsonify({"ok": True}), 200


from app import db
from app.models.user import User
from app.models.facility import Facility
from app.models.review import Review

@misc_bp.route('/seed', methods=['POST'])
def seed():
    if not User.query.filter_by(username='demo').first():
        u = User(username='demo', email='demo@demo.dev'); u.set_password('demo1234!')
        db.session.add(u)
    if not Facility.query.filter_by(name='케어로드 ').first():
        f = Facility(name='케어로드 ', address='서울시 서대문구 1', phone='02-000-0000',
                     grade='2', capacity=80, daily_cost=50000, monthly_cost=1200000)
        db.session.add(f)
    db.session.commit()
    # 샘플 리뷰(중복 방지)
    demo_user = User.query.filter_by(username='demo').first()
    demo_fac  = Facility.query.filter_by(name='케어로드 ').first()
    if demo_user and demo_fac and not Review.query.filter_by(user_id=demo_user.user_id, facility_id=demo_fac.facility_id).first():
        db.session.add(Review(user_id=demo_user.user_id, facility_id=demo_fac.facility_id,
                              content='해커톤 데모', rating=5))
        db.session.commit()
    return jsonify({"ok": True, "message": "seed completed"}), 200


from flask_jwt_extended import create_access_token
@misc_bp.route('/demo-token', methods=['GET'])
def demo_token():
    user = User.query.filter_by(username='demo').first()
    if not user:
        return jsonify({"ok": False, "error": "Run /api/seed first"}), 400
    token = create_access_token(identity=str(user.user_id))
    return jsonify({"ok": True, "access_token": token}), 200
