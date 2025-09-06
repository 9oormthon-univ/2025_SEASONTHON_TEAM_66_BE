import os
from dotenv import load_dotenv
from urllib.parse import quote

load_dotenv()

class Config:
    # 데이터베이스 설정
    SQLALCHEMY_DATABASE_URI = (
        f"mysql+pymysql://{os.getenv('DB_USER')}:"
        f"{os.getenv('DB_PASSWORD')}@{os.getenv('DB_HOST')}/"
        f"{os.getenv('DB_NAME')}"
    )
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    
    # JWT 설정
    JWT_SECRET_KEY = os.getenv('JWT_SECRET_KEY', '1234')
    JWT_ACCESS_TOKEN_EXPIRES = 300 #3600  # 1시간

    JWT_IDENTITY_CLAIM = 'sub'  
    JWT_DECODE_LEEWAY = 10     

    LTC_API_BASE_URL = os.getenv("LTC_API_BASE_URL", "https://apis.data.go.kr/B550928")
    LTC_API_SERVICE_KEY = os.getenv("LTC_API_SERVICE_KEY")
    LTC_API_TIMEOUT = int(os.getenv("LTC_API_TIMEOUT", "10"))
