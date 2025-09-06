from app import db
from datetime import datetime

class Review(db.Model):
    __tablename__ = 'reviews'
    
    review_id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    facility_id = db.Column(db.Integer, db.ForeignKey('facilities.facility_id'), nullable=False)
    content = db.Column(db.Text, nullable=False)
    photo_url = db.Column(db.String(500))
    rating = db.Column(db.Integer, nullable=False)  # 1-5점
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 관계 설정
    #user = db.relationship('User', backref='reviews', lazy=True)
    user = db.relationship('User', lazy=True)
    
    
    def to_dict(self, include_user=True, include_facility=False):
        """후기 정보를 딕셔너리로 변환"""
        result = {
            'review_id': self.review_id,
            'user_id': self.user_id,
            'facility_id': self.facility_id,
            'content': self.content,
            'photo_url': self.photo_url,
            'rating': self.rating,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
        
        # 사용자 정보
        if include_user and self.user:
            result['user'] = {
                'username': self.user.username,
                'user_id': self.user.user_id
            }
        
        # 시설 정보
        if include_facility and self.facility:
            result['facility'] = {
                'name': self.facility.name,
                'facility_id': self.facility.facility_id
            }
        
        return result
    
    @staticmethod
    def get_by_facility(facility_id, limit=None):
        """특정 시설의 후기 조회"""
        query = Review.query.filter_by(facility_id=facility_id).order_by(Review.created_at.desc())
        if limit:
            query = query.limit(limit)
        return query.all()
    
    @staticmethod
    def get_by_user(user_id, limit=None):
        """특정 사용자의 후기 조회"""
        query = Review.query.filter_by(user_id=user_id).order_by(Review.created_at.desc())
        if limit:
            query = query.limit(limit)
        return query.all()
    
    @staticmethod
    def get_average_rating(facility_id):
        """시설의 평균 평점 계산"""
        result = db.session.query(
            db.func.avg(Review.rating).label('avg_rating'),
            db.func.count(Review.review_id).label('review_count')
        ).filter_by(facility_id=facility_id).first()
        
        return {
            'average_rating': round(float(result.avg_rating), 1) if result.avg_rating else 0,
            'review_count': result.review_count or 0
        }