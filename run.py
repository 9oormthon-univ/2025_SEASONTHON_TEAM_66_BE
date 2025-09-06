from app import create_app, db

app = create_app()

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
        print("Database tables created/checked!")
    
    print("Starting Flask server...")
    print("API endpoints:")
    print("- POST /api/auth/register (회원가입)")
    print("- POST /api/auth/login (로그인)")
    
    app.run(debug=True, host='0.0.0.0', port=5000)