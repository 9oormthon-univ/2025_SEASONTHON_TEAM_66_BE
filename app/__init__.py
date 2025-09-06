from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
from app.config import Config
from flask_cors import CORS
from app.utils.error_handler import register_error_handlers


db = SQLAlchemy()
jwt = JWTManager()

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    # 확장 초기화
    CORS(app)
    db.init_app(app)
    jwt.init_app(app)

    register_error_handlers(app)
    
    # Blueprint 등록
    from app.api.auth import auth_bp
    from app.api.facilities import facilities_bp
    from app.api.reviews import reviews_bp
    from app.api.payments import payments_bp
    from app.api.users import users_bp
    from app.api.misc import misc_bp
    app.register_blueprint(misc_bp, url_prefix='/api')
    from app.api.logs import logs_bp
    app.register_blueprint(logs_bp, url_prefix="/api/logs")


    app.register_blueprint(auth_bp, url_prefix='/api/auth')
    app.register_blueprint(facilities_bp, url_prefix='/api/facilities') 
    app.register_blueprint(reviews_bp, url_prefix='/api/reviews')
    app.register_blueprint(payments_bp, url_prefix='/api/payments')
    app.register_blueprint(users_bp, url_prefix='/api/users')

    return app

