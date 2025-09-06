from app import db
from datetime import datetime

class Facility(db.Model):
    __tablename__ = 'facilities'
    
    facility_id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    address = db.Column(db.String(500))
    phone = db.Column(db.String(20))
    grade = db.Column(db.Enum('1', '2', '3', '4', '5'))
    capacity = db.Column(db.Integer)
    daily_cost = db.Column(db.DECIMAL(10, 2))
    monthly_cost = db.Column(db.DECIMAL(10, 2))

    #long_term_code = db.Column(db.String(20), unique=True, index=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 관계 설정 (후기와 연결)
    reviews = db.relationship('Review', backref='facility', lazy=True, cascade='all, delete-orphan')
    
    def to_dict(self):
        """시설 정보를 딕셔너리로 변환"""
        return {
            'facility_id': self.facility_id,
            'name': self.name,
            'address': self.address,
            'phone': self.phone,
            'grade': self.grade,
            'capacity': self.capacity,
            'daily_cost': float(self.daily_cost) if self.daily_cost else None,
            'monthly_cost': float(self.monthly_cost) if self.monthly_cost else None,
            #"long_term_code": self.long_term_code,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
    
    @staticmethod
    def get_by_grade(grade):
        """등급별 시설 조회"""
        return Facility.query.filter_by(grade=grade).all()
    
    @staticmethod
    def search_by_name(keyword):
        """이름으로 시설 검색"""
        return Facility.query.filter(Facility.name.like(f'%{keyword}%')).all()
    
    @staticmethod
    def get_by_price_range(min_cost=None, max_cost=None):
        """가격 범위로 시설 검색"""
        query = Facility.query
        if min_cost:
            query = query.filter(Facility.monthly_cost >= min_cost)
        if max_cost:
            query = query.filter(Facility.monthly_cost <= max_cost)
        return query.all()