import sqlite3
import json

# Подключение к вашей базе данных
conn = sqlite3.connect('C:/Users/Diana/OneDrive/Desktop/mobile/3LR/person_db')
cursor = conn.cursor()

# Экспорт специальностей
cursor.execute("SELECT * FROM specialty")
specialties = [{"id": row[0], "title": row[1]} for row in cursor.fetchall()]

# Экспорт персон
cursor.execute("""
    SELECT p.*, s.title 
    FROM person p 
    JOIN specialty s ON p.specialty_id = s._id
""")
persons = [{
    "id": row[0],
    "firstName": row[1],
    "lastName": row[2],
    "year": row[3],
    "yearOfAdmission": row[4],
    "specialtyId": row[5],
    "specialtyTitle": row[6]
} for row in cursor.fetchall()]

# Сохранение в JSON файлы
with open('specialties.json', 'w', encoding='utf-8') as f:
    json.dump(specialties, f, ensure_ascii=False, indent=2)

with open('persons.json', 'w', encoding='utf-8') as f:
    json.dump(persons, f, ensure_ascii=False, indent=2)

conn.close()