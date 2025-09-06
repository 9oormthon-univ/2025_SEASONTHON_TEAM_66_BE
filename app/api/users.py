from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.security import check_password_hash, generate_password_hash
from app import db
from app.models.user import User
from datetime import datetime
import re
from app.utils import sanitize_string

users_bp = Blueprint('users', __name__)

def validate_password(password):
    """비밀번호 유효성 검사"""
    if len(password) < 8:
        return False, "비밀번호는 8자 이상이어야 합니다."
    if not re.search(r"[A-Za-z]", password):
        return False, "비밀번호에 영문자가 포함되어야 합니다."
    if not re.search(r"\d", password):
        return False, "비밀번호에 숫자가 포함되어야 합니다."
    return True, "유효한 비밀번호입니다."

def validate_phone(phone):
    """전화번호 유효성 검사"""
    phone_pattern = re.compile(r'^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$')
    return phone_pattern.match(phone) is not None

def validate_email(email):
    """이메일 유효성 검사"""
    email_pattern = re.compile(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$')
    return email_pattern.match(email) is not None

@users_bp.route('/profile', methods=['GET'])
@jwt_required()
def get_user_profile():
    """사용자 프로필 조회"""
    try:
        user_id = get_jwt_identity()
        user = User.query.get(user_id)
        
        if not user:
            return jsonify({
                'success': False,
                'error': '사용자를 찾을 수 없습니다.'
            }), 404
        
        return jsonify({
            'success': True,
            'data': {
                'user_id': user.user_id,
                'username': user.username,
                'email': user.email,
                'phone': user.phone,
                'role': user.role,
                'created_at': user.created_at.isoformat() if user.created_at else None,
                'updated_at': user.updated_at.isoformat() if user.updated_at else None
            }
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '프로필 조회에 실패했습니다.',
            'details': str(e)
        }), 500

@users_bp.route('/profile', methods=['PUT'])
@jwt_required()
def update_user_profile():
    """사용자 프로필 수정"""
    try:
        user_id = get_jwt_identity()
        user = User.query.get(user_id)
        
        if not user:
            return jsonify({
                'success': False,
                'error': '사용자를 찾을 수 없습니다.'
            }), 404
        
        data = request.get_json()
        updated_fields = []
        
        # 이메일 수정
        if 'email' in data:
            #new_email = data['email'].strip()
            new_email = sanitize_string(data['email'].strip(), max_length=100)
            if not validate_email(new_email):
                return jsonify({
                    'success': False,
                    'error': '유효하지 않은 이메일 형식입니다.'
                }), 400
            
            # 이메일 중복 체크 
            existing_user = User.query.filter(
                User.email == new_email,
                User.user_id != user_id
            ).first()
            
            if existing_user:
                return jsonify({
                    'success': False,
                    'error': '이미 사용 중인 이메일입니다.'
                }), 400
            
            user.email = new_email
            updated_fields.append('email')
        
        # 전화번호 수정
        if 'phone' in data:
            #new_phone = data['phone'].strip()
            new_phone = sanitize_string(data['phone'].strip(), max_length=20)
            if not validate_phone(new_phone):
                return jsonify({
                    'success': False,
                    'error': '유효하지 않은 전화번호 형식입니다.'
                }), 400
            
            user.phone = new_phone
            updated_fields.append('phone')
        
        # 사용자명 수정
        if 'username' in data:
            #new_username = data['username'].strip()
            new_username = sanitize_string(data['username'].strip(), max_length=50)
            if len(new_username) < 2:
                return jsonify({
                    'success': False,
                    'error': '사용자명은 2자 이상이어야 합니다.'
                }), 400
            
            # 사용자명 중복 체크
            existing_user = User.query.filter(
                User.username == new_username,
                User.user_id != user_id
            ).first()
            
            if existing_user:
                return jsonify({
                    'success': False,
                    'error': '이미 사용 중인 사용자명입니다.'
                }), 400
            
            user.username = new_username
            updated_fields.append('username')
        
        # 수정된 필드가 없는 경우
        if not updated_fields:
            return jsonify({
                'success': False,
                'error': '수정할 정보를 입력해주세요.'
            }), 400
        
        user.updated_at = datetime.utcnow()
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': '프로필이 성공적으로 수정되었습니다.',
            'updated_fields': updated_fields,
            'data': {
                'user_id': user.user_id,
                'username': user.username,
                'email': user.email,
                'phone': user.phone,
                'updated_at': user.updated_at.isoformat()
            }
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({
            'success': False,
            'error': '프로필 수정에 실패했습니다.',
            'details': str(e)
        }), 500

@users_bp.route('/change-password', methods=['PUT'])
@jwt_required()
def change_password():
    """비밀번호 변경"""
    try:
        user_id = get_jwt_identity()
        user = User.query.get(user_id)
        
        if not user:
            return jsonify({
                'success': False,
                'error': '사용자를 찾을 수 없습니다.'
            }), 404
        
        data = request.get_json()
        
        # 입력 검증
        if not data.get('current_password') or not data.get('new_password'):
            return jsonify({
                'success': False,
                'error': '현재 비밀번호와 새 비밀번호를 모두 입력해주세요.'
            }), 400
        
        # 현재 비밀번호 확인
        if not check_password_hash(user.password_hash, data['current_password']):
            return jsonify({
                'success': False,
                'error': '현재 비밀번호가 올바르지 않습니다.'
            }), 400
        
        # 새 비밀번호 유효성
        is_valid, message = validate_password(data['new_password'])
        if not is_valid:
            return jsonify({
                'success': False,
                'error': message
            }), 400
        
        # 새 비밀번호가 현재 비밀번호와 같은지
        if check_password_hash(user.password_hash, data['new_password']):
            return jsonify({
                'success': False,
                'error': '새 비밀번호는 현재 비밀번호와 달라야 합니다.'
            }), 400
        
        # 비밀번호 업데이트
        user.password_hash = generate_password_hash(data['new_password'])
        user.updated_at = datetime.utcnow()
        
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': '비밀번호가 성공적으로 변경되었습니다.'
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({
            'success': False,
            'error': '비밀번호 변경에 실패했습니다.',
            'details': str(e)
        }), 500

@users_bp.route('/delete-account', methods=['DELETE'])
@jwt_required()
def delete_account():
    """계정 삭제"""
    try:
        user_id = get_jwt_identity()
        user = User.query.get(user_id)
        
        if not user:
            return jsonify({
                'success': False,
                'error': '사용자를 찾을 수 없습니다.'
            }), 404
        
        data = request.get_json()
        
        # 비밀번호 확인
        if not data.get('password'):
            return jsonify({
                'success': False,
                'error': '비밀번호를 입력해주세요.'
            }), 400
        
        if not check_password_hash(user.password_hash, data['password']):
            return jsonify({
                'success': False,
                'error': '비밀번호가 올바르지 않습니다.'
            }), 400
        
        # 사용자 삭제
        db.session.delete(user)
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': '계정이 성공적으로 삭제되었습니다.'
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({
            'success': False,
            'error': '계정 삭제에 실패했습니다.',
            'details': str(e)
        }), 500

@users_bp.route('/check-username', methods=['POST'])
def check_username_availability():
    """사용자명 중복 확인"""
    try:
        data = request.get_json()
        
        if not data.get('username'):
            return jsonify({
                'success': False,
                'error': '사용자명을 입력해주세요.'
            }), 400
        
        #username = data['username'].strip()
        username = sanitize_string(data['username'].strip(), max_length=50)
        
        if len(username) < 2:
            return jsonify({
                'success': False,
                'error': '사용자명은 2자 이상이어야 합니다.'
            }), 400
        
        existing_user = User.query.filter_by(username=username).first()
        
        return jsonify({
            'success': True,
            'available': existing_user is None,
            'message': '사용 가능한 사용자명입니다.' if not existing_user else '이미 사용 중인 사용자명입니다.'
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '사용자명 확인에 실패했습니다.',
            'details': str(e)
        }), 500

@users_bp.route('/check-email', methods=['POST'])
def check_email_availability():
    """이메일 중복 확인"""
    try:
        data = request.get_json()
        
        if not data.get('email'):
            return jsonify({
                'success': False,
                'error': '이메일을 입력해주세요.'
            }), 400
        
        #email = data['email'].strip()
        email = sanitize_string(data['email'].strip(), max_length=100)
        
        if not validate_email(email):
            return jsonify({
                'success': False,
                'error': '유효하지 않은 이메일 형식입니다.'
            }), 400
        
        existing_user = User.query.filter_by(email=email).first()
        
        return jsonify({
            'success': True,
            'available': existing_user is None,
            'message': '사용 가능한 이메일입니다.' if not existing_user else '이미 사용 중인 이메일입니다.'
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '이메일 확인에 실패했습니다.',
            'details': str(e)
        }), 500

@users_bp.route('/stats', methods=['GET'])
@jwt_required()
def get_user_stats():
    """사용자 활동 통계 (후기 수, 가입일 등)"""
    try:
        user_id = get_jwt_identity()
        user = User.query.get(user_id)
        
        if not user:
            return jsonify({
                'success': False,
                'error': '사용자를 찾을 수 없습니다.'
            }), 404
        
        # 후기 수 계산
        from app.models.review import Review
        review_count = Review.query.filter_by(user_id=user_id).count()
        
        # 가입 후 일수
        days_since_joined = (datetime.utcnow() - user.created_at).days if user.created_at else 0
        
        return jsonify({
            'success': True,
            'data': {
                'review_count': review_count,
                'days_since_joined': days_since_joined,
                'joined_date': user.created_at.isoformat() if user.created_at else None,
                'last_activity': user.updated_at.isoformat() if user.updated_at else None
            }
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': True,  
            'data': {
                'review_count': 0,
                'days_since_joined': 0,
                'joined_date': user.created_at.isoformat() if user.created_at else None,
                'last_activity': user.updated_at.isoformat() if user.updated_at else None
            },
            'note': '일부 통계는 관련 기능 구현 후 제공됩니다.'
        }), 200