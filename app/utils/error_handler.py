# app/utils/error_handler.py
from flask import jsonify, request
from functools import wraps
import logging
import traceback
from datetime import datetime

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class APIError(Exception):
    """사용자 정의 API 에러 클래스"""
    def __init__(self, message, status_code=400, error_code=None):
        super().__init__(message)
        self.message = message
        self.status_code = status_code
        self.error_code = error_code

def standardize_error_response(error_message, status_code=400, error_code=None, details=None):
    """표준화된 에러 응답 형식"""
    response = {
        'success': False,
        'error': error_message,
        'timestamp': datetime.utcnow().isoformat()
    }
    
    if error_code:
        response['error_code'] = error_code
    
    if details:
        response['details'] = details
        
    return jsonify(response), status_code

def log_api_request():
    """API 요청 로깅"""
    logger.info(f"API Request: {request.method} {request.path} from {request.remote_addr}")
    if request.get_json():
        # 비밀번호 등 민감한 정보 제외
        data = request.get_json().copy()
        if 'password' in data:
            data['password'] = '***'
        if 'current_password' in data:
            data['current_password'] = '***'
        if 'new_password' in data:
            data['new_password'] = '***'
        logger.info(f"Request Data: {data}")

def handle_api_errors(f):
    """API 에러 처리 데코레이터"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            log_api_request()
            return f(*args, **kwargs)
        
        except APIError as e:
            logger.error(f"API Error: {e.message}")
            return standardize_error_response(
                error_message=e.message,
                status_code=e.status_code,
                error_code=e.error_code
            )
        
        except ValueError as e:
            logger.error(f"Validation Error: {str(e)}")
            return standardize_error_response(
                error_message="입력값이 올바르지 않습니다.",
                status_code=400,
                details=str(e)
            )
        
        except Exception as e:
            logger.error(f"Unexpected Error: {str(e)}\n{traceback.format_exc()}")
            return standardize_error_response(
                error_message="서버 내부 오류가 발생했습니다.",
                status_code=500,
                details=str(e) if logger.level == logging.DEBUG else None
            )
    
    return decorated_function

def validate_json_input(required_fields=None, optional_fields=None):
    """JSON 입력 검증 데코레이터"""
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            data = request.get_json()
            
            if not data:
                raise APIError("JSON 데이터가 필요합니다.", 400)
            
            # 필수 필드 검증
            if required_fields:
                missing_fields = [field for field in required_fields if field not in data or data[field] is None]
                if missing_fields:
                    raise APIError(f"필수 필드가 누락되었습니다: {', '.join(missing_fields)}", 400)
            
            # 허용되지 않은 필드 검증
            if optional_fields is not None:
                allowed_fields = set(required_fields or []) | set(optional_fields or [])
                invalid_fields = set(data.keys()) - allowed_fields
                if invalid_fields:
                    raise APIError(f"허용되지 않은 필드입니다: {', '.join(invalid_fields)}", 400)
            
            return f(*args, **kwargs)
        return decorated_function
    return decorator










from flask import jsonify
import logging
from datetime import datetime
from flask_jwt_extended.exceptions import NoAuthorizationError
from jwt.exceptions import ExpiredSignatureError   # ✅ 여기로 교체

logger = logging.getLogger(__name__)

class APIError(Exception):
    def __init__(self, message, status_code=400, error_code=None):
        super().__init__(message)
        self.message = message
        self.status_code = status_code
        self.error_code = error_code

def standardize_error_response(error_message, status_code=400, error_code=None, details=None):
    response = {
        'success': False,
        'error': error_message,
        'timestamp': datetime.utcnow().isoformat()
    }
    if error_code:
        response['error_code'] = error_code
    if details:
        response['details'] = details
    return jsonify(response), status_code

def register_error_handlers(app):
    @app.errorhandler(APIError)
    def handle_api_error(error):
        logger.error(f"APIError: {error.message}")
        return standardize_error_response(error.message, error.status_code, error.error_code)

    # JWT 인증 관련 에러 처리
    @app.errorhandler(NoAuthorizationError)
    def handle_missing_jwt(e):
        return standardize_error_response("인증 토큰이 필요합니다.", 401)

    @app.errorhandler(ExpiredSignatureError)
    def handle_expired_jwt(e):
        return standardize_error_response("인증 토큰이 만료되었습니다.", 401)

    # 모든 나머지 예외 처리
    @app.errorhandler(Exception)
    def handle_unexpected_error(error):
        logger.error(f"Unhandled Exception: {str(error)}", exc_info=True)
        return standardize_error_response("서버 내부 오류가 발생했습니다.", 500)
