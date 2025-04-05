from flask import Flask, jsonify, request, Response
import json
import os

app = Flask(__name__)
app.response_class = Response  # Для лучшей буферизации

# Загрузка данных из JSON файлов
def load_data():
    try:
        with open('specialties.json', 'r', encoding='utf-8') as f:
            specialties = json.load(f)
        with open('persons.json', 'r', encoding='utf-8') as f:
            persons = json.load(f)
        return specialties, persons
    except FileNotFoundError:
        print("Ошибка: JSON файлы не найдены.")
        return [], []  # Возвращаем пустые списки, чтобы избежать ошибок на сервере


specialties_data, persons_data = load_data()

@app.route('/specialties')
def get_specialties():
    page = int(request.args.get('page', 1))
    per_page = int(request.args.get('per_page', 100))

    start = (page - 1) * per_page
    end = start + per_page

    paginated_specialties = specialties_data[start:end]
    total_specialties = len(specialties_data)
    total_pages = (total_specialties + per_page - 1) // per_page

    response = {
        "specialties": paginated_specialties,
        "currentPage": page,
        "totalPages": total_pages,
        "totalItems": total_specialties
    }

    return jsonify(response)



@app.route('/persons/specialty/<int:specialty_id>')
def persons_by_specialty(specialty_id):
    # Проверка наличия данных, чтобы избежать 500 ошибки, если файлы не найдены
    if not persons_data or not specialties_data:
        return jsonify({"error": "Ошибка загрузки данных"}), 500

    if not any(s["id"] == specialty_id for s in specialties_data):
        return jsonify({"error": "Специальность не найдена"}), 404

    filtered_persons = [p for p in persons_data if p.get('specialtyId') == specialty_id]

    result = []
    for person in filtered_persons:
        result.append({
            "id": person["id"],
            "firstName": person["firstName"],
            "lastName": person["lastName"],
            "year": person["year"],
            "yearOfAdmission": person["yearOfAdmission"],
            "specialtyTitle": next((s["title"] for s in specialties_data if s["id"] == person["specialtyId"]), "")
        })

    return jsonify(result)

if __name__ == '__main__':
    if not specialties_data:
        print("Внимание: Сервер запущен с пустыми данными!")
    app.run(host='0.0.0.0', port=8000, debug=True)