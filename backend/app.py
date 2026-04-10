from datetime import timedelta

from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_jwt_extended import (
    JWTManager,
    create_access_token,
    get_jwt_identity,
    jwt_required
)

app = Flask(__name__)

# Configuración
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///site.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# JWT
app.config["JWT_SECRET_KEY"] = "cambia_esto_por_una_clave_mas_segura"
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=1)

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
jwt = JWTManager(app)


# Modelo de Usuario
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(20), unique=True, nullable=False)
    password = db.Column(db.String(60), nullable=False)

    def __repr__(self):
        return f"User('{self.username}')"


# Modelo de Nota
class Note(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(100), nullable=False)
    content = db.Column(db.Text, nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "content": self.content,
            "user_id": self.user_id
        }


@app.route('/')
def hello():
    return jsonify({"message": "API Funcionando con JWT"})


# REGISTRO
@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()

    if not data:
        return jsonify({"message": "No se enviaron datos"}), 400

    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"message": "Usuario y contraseña son obligatorios"}), 400

    if User.query.filter_by(username=username).first():
        return jsonify({"message": "El usuario ya existe"}), 400

    if len(password) < 6:
        return jsonify({"message": "La contraseña debe tener al menos 6 caracteres"}), 400

    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')

    new_user = User(username=username, password=hashed_password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": "Usuario creado exitosamente"}), 201


# LOGIN CON JWT
@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()

    if not data:
        return jsonify({"status": "error", "message": "No se enviaron datos"}), 400

    username = data.get('username')
    password = data.get('password')

    user = User.query.filter_by(username=username).first()

    if user and bcrypt.check_password_hash(user.password, password):
        access_token = create_access_token(
            identity=str(user.id),
            additional_claims={"username": user.username}
        )

        return jsonify({
            "status": "success",
            "message": "Login exitoso",
            "user_id": user.id,
            "username": user.username,
            "access_token": access_token
        }), 200

    return jsonify({
        "status": "error",
        "message": "Credenciales inválidas"
    }), 401


# CREAR nota (protegido)
@app.route('/notes', methods=['POST'])
@jwt_required()
def create_note():
    data = request.get_json()

    if not data:
        return jsonify({"message": "No se enviaron datos"}), 400

    title = data.get('title')
    content = data.get('content')

    if not title or not content:
        return jsonify({"message": "title y content son obligatorios"}), 400

    current_user_id = int(get_jwt_identity())

    note = Note(title=title, content=content, user_id=current_user_id)
    db.session.add(note)
    db.session.commit()

    return jsonify({
        "message": "Nota creada exitosamente",
        "note": note.to_dict()
    }), 201


# LEER notas del usuario autenticado (protegido)
@app.route('/notes', methods=['GET'])
@jwt_required()
def get_notes():
    current_user_id = int(get_jwt_identity())
    notes = Note.query.filter_by(user_id=current_user_id).all()
    return jsonify([note.to_dict() for note in notes]), 200


# ACTUALIZAR nota (protegido)
@app.route('/notes/<int:note_id>', methods=['PUT'])
@jwt_required()
def update_note(note_id):
    current_user_id = int(get_jwt_identity())

    note = Note.query.filter_by(id=note_id, user_id=current_user_id).first()
    if not note:
        return jsonify({"message": "Nota no encontrada"}), 404

    data = request.get_json()
    if not data:
        return jsonify({"message": "No se enviaron datos"}), 400

    title = data.get('title')
    content = data.get('content')

    if not title or not content:
        return jsonify({"message": "title y content son obligatorios"}), 400

    note.title = title
    note.content = content
    db.session.commit()

    return jsonify({
        "message": "Nota actualizada exitosamente",
        "note": note.to_dict()
    }), 200


# ELIMINAR nota (protegido)
@app.route('/notes/<int:note_id>', methods=['DELETE'])
@jwt_required()
def delete_note(note_id):
    current_user_id = int(get_jwt_identity())

    note = Note.query.filter_by(id=note_id, user_id=current_user_id).first()
    if not note:
        return jsonify({"message": "Nota no encontrada"}), 404

    db.session.delete(note)
    db.session.commit()

    return jsonify({"message": "Nota eliminada exitosamente"}), 200


if __name__ == '__main__':
    with app.app_context():
        db.create_all()

    app.run(host='0.0.0.0', port=5000, debug=True)