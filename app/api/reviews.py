from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app import db
from app.models.review import Review
from app.models.facility import Facility
from app.models.user import User
from app.utils import sanitize_string

reviews_bp = Blueprint('reviews', __name__)

@reviews_bp.route('', methods=['POST'])
@jwt_required()
def create_review():
    """후기 작성"""
    try:
        user_id = get_jwt_identity()
        data = request.get_json()
        
        # 입력 검증
        if not data.get('facility_id'):
            return jsonify({'success': False, 'error': '시설 ID가 필요합니다.'}), 400
        
        if not data.get('content'):
            return jsonify({'success': False, 'error': '후기 내용이 필요합니다.'}), 400
        
        if not data.get('rating') or not (1 <= int(data.get('rating')) <= 5):
            return jsonify({'success': False, 'error': '평점은 1-5 사이의 값이어야 합니다.'}), 400
        
        # 시설 존재 여부
        facility = Facility.query.get(data['facility_id'])
        if not facility:
            return jsonify({'success': False, 'error': '존재하지 않는 시설입니다.'}), 404
        
        # 중복 후기 확인 (빼도될듯)
        existing_review = Review.query.filter_by(
            user_id=user_id, 
            facility_id=data['facility_id']
        ).first()
        
        if existing_review:
            return jsonify({
                'success': False, 
                'error': '이미 이 시설에 후기를 작성하셨습니다.'
            }), 409
        
        # 새 후기 생성
        review = Review(
            user_id=user_id,
            facility_id=data['facility_id'],
            content=sanitize_string(data['content'], max_length=1000),
            rating=int(data['rating']),
            photo_url=sanitize_string(data.get('photo_url', ''), max_length=500)
        )
        
        db.session.add(review)
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': '후기가 성공적으로 등록되었습니다.',
            'data': review.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({
            'success': False,
            'error': '후기 등록에 실패했습니다.',
            'details': str(e)
        }), 500

@reviews_bp.route('', methods=['GET'])
def get_reviews():
    """후기 목록 조회"""
    try:
        facility_id = request.args.get('facility_id')
        user_id = request.args.get('user_id')
        page = int(request.args.get('page', 1))
        per_page = int(request.args.get('per_page', 10))
        
        # 기본 쿼리
        query = Review.query
        
        # 필터 적용
        if facility_id:
            query = query.filter_by(facility_id=facility_id)
        
        if user_id:
            query = query.filter_by(user_id=user_id)
        
        # 최신순 정렬
        query = query.order_by(Review.created_at.desc())
        
        # 페이지네이션
        reviews_paginated = query.paginate(
            page=page, 
            per_page=per_page, 
            error_out=False
        )
        
        # 결과 변환
        reviews = [review.to_dict(include_user=True) for review in reviews_paginated.items]
        
        return jsonify({
            'success': True,
            'data': reviews,
            'pagination': {
                'page': page,
                'per_page': per_page,
                'total': reviews_paginated.total,
                'pages': reviews_paginated.pages
            }
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '후기 목록을 가져오는데 실패했습니다.',
            'details': str(e)
        }), 500

@reviews_bp.route('/<int:review_id>', methods=['GET'])
def get_review_detail(review_id):
    """후기 상세 조회"""
    try:
        review = Review.query.get_or_404(review_id)
        
        return jsonify({
            'success': True,
            'data': review.to_dict(include_user=True, include_facility=True)
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '후기를 찾을 수 없습니다.'
        }), 404

@reviews_bp.route('/<int:review_id>', methods=['PUT'])
@jwt_required()
def update_review(review_id):
    """후기 수정"""
    try:
        user_id = get_jwt_identity()
        review = Review.query.get_or_404(review_id)
        
        # 작성자 확인
        if review.user_id != user_id:
            return jsonify({
                'success': False,
                'error': '본인이 작성한 후기만 수정할 수 있습니다.'
            }), 403
        
        data = request.get_json()
        
        # 수정할 필드 업데이트
        if 'content' in data:
            #review.content = data['content']
            review.content = sanitize_string(data['content'], max_length=500)
            
        
        if 'rating' in data:
            if not (1 <= int(data['rating']) <= 5):
                return jsonify({
                    'success': False,
                    'error': '평점은 1-5 사이의 값이어야 합니다.'
                }), 400
            review.rating = int(data['rating'])
        
        if 'photo_url' in data:
            #review.photo_url = data['photo_url']
            review.photo_url = sanitize_string(data['photo_url'], max_length=500)
        
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': '후기가 성공적으로 수정되었습니다.',
            'data': review.to_dict()
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({
            'success': False,
            'error': '후기 수정에 실패했습니다.',
            'details': str(e)
        }), 500

@reviews_bp.route('/<int:review_id>', methods=['DELETE'])
@jwt_required()
def delete_review(review_id):
    """후기 삭제"""
    try:
        user_id = get_jwt_identity()
        review = Review.query.get_or_404(review_id)
        
        # 작성자 확인
        if review.user_id != user_id:
            return jsonify({
                'success': False,
                'error': '본인이 작성한 후기만 삭제할 수 있습니다.'
            }), 403
        
        db.session.delete(review)
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': '후기가 성공적으로 삭제되었습니다.'
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({
            'success': False,
            'error': '후기 삭제에 실패했습니다.'
        }), 500

@reviews_bp.route('/facility/<int:facility_id>/stats', methods=['GET'])
def get_facility_review_stats(facility_id):
    """시설 후기 통계 (평점, 후기 수)"""
    try:
        stats = Review.get_average_rating(facility_id)
        
        return jsonify({
            'success': True,
            'data': stats
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '통계를 가져오는데 실패했습니다.'
        }), 500