# app/utils/validators.py
import re
from datetime import datetime


def validate_email(email):
    if not email or not isinstance(email, str):
        return False, "이메일을 입력해주세요."
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    if not re.match(pattern, email):
        return False, "유효하지 않은 이메일 형식입니다."
    return True, "유효한 이메일입니다."


def validate_phone(phone):
    if not phone or not isinstance(phone, str):
        return False, "전화번호를 입력해주세요."
    pattern = r'^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$'
    if not re.match(pattern, phone):
        return False, "유효하지 않은 전화번호 형식입니다. (예: 010-1234-5678)"
    return True, "유효한 전화번호입니다."


def validate_password(password):
    if not password or not isinstance(password, str):
        return False, "비밀번호를 입력해주세요."
    if len(password) < 8:
        return False, "비밀번호는 8자 이상이어야 합니다."
    if len(password) > 128:
        return False, "비밀번호는 128자를 초과할 수 없습니다."
    if not re.search(r'[A-Za-z]', password):
        return False, "비밀번호에 영문자가 포함되어야 합니다."
    if not re.search(r'\d', password):
        return False, "비밀번호에 숫자가 포함되어야 합니다."
    if not re.search(r'[!@#$%^&*(),.?":{}|<>]', password):
        return True, "비밀번호가 유효합니다. (특수문자를 포함하면 더 안전합니다.)"
    return True, "강력한 비밀번호입니다."


def validate_date_format(date_string, format_string='%Y-%m-%d'):
    try:
        datetime.strptime(date_string, format_string)
        return True, "유효한 날짜입니다."
    except ValueError:
        return False, f"날짜 형식이 올바르지 않습니다. ({format_string})"


def validate_integer_range(value, min_val=None, max_val=None, field_name="값"):
    try:
        int_value = int(value)
    except (TypeError, ValueError):
        return False, f"{field_name}은(는) 숫자여야 합니다."
    if min_val is not None and int_value < min_val:
        return False, f"{field_name}은(는) {min_val} 이상이어야 합니다."
    if max_val is not None and int_value > max_val:
        return False, f"{field_name}은(는) {max_val} 이하여야 합니다."
    return True, f"{field_name}이(가) 유효합니다."


def sanitize_string(text, max_length=None):
    if not isinstance(text, str):
        return ""
    text = re.sub(r'<[^>]+>', '', text)
    dangerous_patterns = [
        r'javascript:',
        r'vbscript:',
        r'onload=',
        r'onerror=',
        r'onclick='
    ]
    for pattern in dangerous_patterns:
        text = re.sub(pattern, '', text, flags=re.IGNORECASE)
    if max_length:
        text = text[:max_length]
    return text.strip()
