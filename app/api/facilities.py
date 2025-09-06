# from flask import Blueprint, request, jsonify
# from flask_jwt_extended import jwt_required, get_jwt_identity
# from app import db
# from app.models.facility import Facility

# facilities_bp = Blueprint('facilities', __name__)

# @facilities_bp.route('/list', methods=['GET'])
# def get_facilities():
#     """시설 목록 조회 (필터링 포함)"""
#     try:
#         # 쿼리 파라미터 받기
#         grade = request.args.get('grade')  
#         keyword = request.args.get('search')  
#         min_cost = request.args.get('min_cost')  
#         max_cost = request.args.get('max_cost') 
#         page = int(request.args.get('page', 1)) 
#         per_page = int(request.args.get('per_page', 10)) 
        
#         # 기본 쿼리
#         query = Facility.query
        
#         # 필터 적용
#         if grade:
#             query = query.filter(Facility.grade == grade)
        
#         if keyword:
#             query = query.filter(Facility.name.like(f'%{keyword}%'))
        
#         if min_cost:
#             query = query.filter(Facility.monthly_cost >= float(min_cost))
        
#         if max_cost:
#             query = query.filter(Facility.monthly_cost <= float(max_cost))
        
#         # 페이지네이션
#         facilities_paginated = query.paginate(
#             query,
#             page=page, 
#             per_page=per_page, 
#             error_out=False
#         )
        
#         # 결과 변환
#         facilities = [facility.to_dict() for facility in facilities_paginated.items]
        
#         return jsonify({
#             'success': True,
#             'data': facilities,
#             'pagination': {
#                 'page': page,
#                 'per_page': per_page,
#                 'total': facilities_paginated.total,
#                 'pages': facilities_paginated.pages
#             }
#         }), 200
        
#     except Exception as e:
#         return jsonify({
#             'success': False,
#             'error': '시설 목록을 가져오는데 실패했습니다.',
#             'details': str(e)
#         }), 500

# @facilities_bp.route('/<int:facility_id>', methods=['GET'])
# def get_facility_detail(facility_id):
#     """시설 상세 정보 조회"""
#     try:
#         facility = Facility.query.get_or_404(facility_id)
        
#         return jsonify({
#             'success': True,
#             'data': facility.to_dict()
#         }), 200
        
#     except Exception as e:
#         return jsonify({
#             'success': False,
#             'error': '시설 정보를 가져오는데 실패했습니다.'
#         }), 404

# @facilities_bp.route('/grades', methods=['GET'])
# def get_facilities_by_grade():
#     """등급별 시설 개수 통계"""
#     try:
#         grade_stats = db.session.query(
#             Facility.grade, 
#             db.func.count(Facility.facility_id).label('count')
#         ).group_by(Facility.grade).all()
        
#         stats = {}
#         for grade, count in grade_stats:
#             stats[f'grade_{grade}'] = count
        
#         return jsonify({
#             'success': True,
#             'data': stats
#         }), 200
        
#     except Exception as e:
#         return jsonify({
#             'success': False,
#             'error': '통계를 가져오는데 실패했습니다.'
#         }), 500

# @facilities_bp.route('/search', methods=['GET'])
# def search_facilities():
#     """시설 통합 검색"""
#     try:
#         keyword = request.args.get('q', '')
        
#         if not keyword:
#             return jsonify({
#                 'success': False,
#                 'error': '검색어를 입력해주세요.'
#             }), 400
        
#         # 이름과 주소에서 검색
#         facilities = Facility.query.filter(
#             db.or_(
#                 Facility.name.like(f'%{keyword}%'),
#                 Facility.address.like(f'%{keyword}%')
#             )
#         ).limit(20).all()
        
#         results = [facility.to_dict() for facility in facilities]
        
#         return jsonify({
#             'success': True,
#             'data': results,
#             'count': len(results)
#         }), 200
        
#     except Exception as e:
#         return jsonify({
#             'success': False,
#             'error': '검색에 실패했습니다.'
#         }), 500

# @facilities_bp.route('/nearby', methods=['GET'])
# @jwt_required()
# def get_nearby_facilities():
#     """근처 시설 조회 (추후 GPS 기능 확장용)"""
#     try:
#         # 현재는 모든 시설 반환 (추후 GPS 연동)
#         facilities = Facility.query.limit(10).all()
#         results = [facility.to_dict() for facility in facilities]
        
#         return jsonify({
#             'success': True,
#             'data': results,
#             'message': '근처 시설 목록입니다.'
#         }), 200
        
#     except Exception as e:
#         return jsonify({
#             'success': False,
#             'error': '근처 시설을 가져오는데 실패했습니다.'
#         }), 500



# # from app.service.services import NHISService
# # from flask_jwt_extended import jwt_required, get_jwt_identity

# # @facilities_bp.route('/sync-public', methods=['POST'])
# # @jwt_required()
# # def sync_public_facilities():
# #     """공공데이터 API 연동 (관리자 전용)"""
# #     user_id = get_jwt_identity()
# #     # 관리자만 가능하도록 체크 (role이 'admin'인지 확인)
# #     from app.models.user import User
# #     user = User.query.get(user_id)
# #     if not user or user.role != 'admin':
# #         return jsonify({"success": False, "error": "관리자만 실행 가능합니다."}), 403

# #     try:
# #         NHISService.sync_facilities()
# #         return jsonify({"success": True, "message": "공공 데이터와 동기화 완료"}), 200
# #     except Exception as e:
# #         return jsonify({"success": False, "error": str(e)}), 500






from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app import db
from app.models.facility import Facility

facilities_bp = Blueprint('facilities', __name__)

@facilities_bp.route('/list', methods=['GET'])
def get_facilities():
    """시설 목록 조회 (필터링 포함)"""
    try:
        # 쿼리 파라미터 받기
        grade = request.args.get('grade')  # ?grade=1
        keyword = request.args.get('search')  # ?search=서울
        min_cost = request.args.get('min_cost')  # ?min_cost=1000000
        max_cost = request.args.get('max_cost')  # ?max_cost=1500000
        page = int(request.args.get('page', 1))  # ?page=1
        per_page = int(request.args.get('per_page', 10))  # ?per_page=10
        
        # 기본 쿼리
        query = Facility.query
        
        # 필터 적용
        if grade:
            query = query.filter(Facility.grade == grade)
        
        if keyword:
            query = query.filter(Facility.name.like(f'%{keyword}%'))
        
        if min_cost:
            query = query.filter(Facility.monthly_cost >= float(min_cost))
        
        if max_cost:
            query = query.filter(Facility.monthly_cost <= float(max_cost))
        
        # 페이지네이션
        facilities_paginated = query.paginate(
            page=page, 
            per_page=per_page, 
            error_out=False
        )
        
        # 결과 변환
        facilities = [facility.to_dict() for facility in facilities_paginated.items]
        
        return jsonify({
            'success': True,
            'data': facilities,
            'pagination': {
                'page': page,
                'per_page': per_page,
                'total': facilities_paginated.total,
                'pages': facilities_paginated.pages
            }
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '시설 목록을 가져오는데 실패했습니다.',
            'details': str(e)
        }), 500

@facilities_bp.route('/<int:facility_id>', methods=['GET'])
def get_facility_detail(facility_id):
    """시설 상세 정보 조회"""
    try:
        facility = Facility.query.get_or_404(facility_id)
        
        return jsonify({
            'success': True,
            'data': facility.to_dict()
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '시설 정보를 가져오는데 실패했습니다.'
        }), 404

@facilities_bp.route('/grades', methods=['GET'])
def get_facilities_by_grade():
    """등급별 시설 개수 통계"""
    try:
        grade_stats = db.session.query(
            Facility.grade, 
            db.func.count(Facility.facility_id).label('count')
        ).group_by(Facility.grade).all()
        
        stats = {}
        for grade, count in grade_stats:
            stats[f'grade_{grade}'] = count
        
        return jsonify({
            'success': True,
            'data': stats
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '통계를 가져오는데 실패했습니다.'
        }), 500

@facilities_bp.route('/search', methods=['GET'])
def search_facilities():
    """시설 통합 검색"""
    try:
        keyword = request.args.get('q', '')
        
        if not keyword:
            return jsonify({
                'success': False,
                'error': '검색어를 입력해주세요.'
            }), 400
        
        # 이름과 주소에서 검색
        facilities = Facility.query.filter(
            db.or_(
                Facility.name.like(f'%{keyword}%'),
                Facility.address.like(f'%{keyword}%')
            )
        ).limit(20).all()
        
        results = [facility.to_dict() for facility in facilities]
        
        return jsonify({
            'success': True,
            'data': results,
            'count': len(results)
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '검색에 실패했습니다.'
        }), 500

@facilities_bp.route('/nearby', methods=['GET'])
@jwt_required()
def get_nearby_facilities():
    """근처 시설 조회 (추후 GPS 기능 확장용)"""
    try:
        # 현재는 모든 시설 반환 (추후 GPS 연동)
        facilities = Facility.query.limit(10).all()
        results = [facility.to_dict() for facility in facilities]
        
        return jsonify({
            'success': True,
            'data': results,
            'message': '근처 시설 목록입니다.'
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': '근처 시설을 가져오는데 실패했습니다.'
        }), 500



from app.service.services import NHISService
from flask_jwt_extended import jwt_required, get_jwt_identity

@facilities_bp.route('/sync-public', methods=['POST'])
@jwt_required()
def sync_public_facilities():
    """공공데이터 API 연동 (관리자 전용)"""
    user_id = get_jwt_identity()
    # 관리자만 가능
    from app.models.user import User
    user = User.query.get(user_id)
    if not user or user.role != 'admin':
        return jsonify({"success": False, "error": "관리자만 실행 가능합니다."}), 403

    try:
        result = NHISService.sync_facilities()  
        return jsonify({"success": True, "message": "공공 데이터와 동기화 완료", "data": result}), 200
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500