from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
from app import db
from app.models.user import User

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    
    # 입력 검증
    if not data.get('username') or not data.get('email') or not data.get('password'):
        return jsonify({'error': '필수 정보가 누락되었습니다.'}), 400
    
    # 중복 확인
    if User.query.filter_by(username=data['username']).first():
        return jsonify({'error': '이미 존재하는 사용자명입니다.'}), 409
    
    if User.query.filter_by(email=data['email']).first():
        return jsonify({'error': '이미 존재하는 이메일입니다.'}), 409
    
    # 새 사용자 생성
    user = User(
        username=data['username'],
        email=data['email'],
        phone=data.get('phone')
    )
    user.set_password(data['password'])
    
    db.session.add(user)
    db.session.commit()
    
    return jsonify({
        'message': '회원가입이 완료되었습니다.',
        'user_id': user.user_id
    }), 201

@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    
    if not data.get('username') or not data.get('password'):
        return jsonify({'error': '사용자명과 비밀번호를 입력해주세요.'}), 400
    
    user = User.query.filter_by(username=data['username']).first()
    
    if user and user.check_password(data['password']):
        access_token = create_access_token(identity=str(user.user_id))
        return jsonify({
            'access_token': access_token,
            'user': {
                'user_id': user.user_id,
                'username': user.username,
                'email': user.email,
                'role': user.role
            }
        }), 200
    
    return jsonify({'error': '잘못된 사용자명 또는 비밀번호입니다.'}), 401