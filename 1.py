import cv2
import os
import csv
from datetime import datetime

# === Initialisation ===
print("=== Capture des visages ===")
print("Appuyez sur 'q' pour quitter")

# Création du dossier pour stocker les visages
BASE_DIR = "visages"
if not os.path.exists(BASE_DIR):
    os.makedirs(BASE_DIR)

# Nom du fichier CSV pour stocker les infos des membres
CSV_FILE = "membres.csv"

# Vérifier si le fichier CSV existe, sinon le créer avec les en-têtes
if not os.path.isfile(CSV_FILE):
    with open(CSV_FILE, mode="w", newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(["Matricule", "Nom Complet", "Rôle", "Département", "Date d'Inscription"])

# === Saisie des informations utilisateur ===
nom = input("Nom complet : ").strip()
matricule = input("ID unique (matricule) : ").strip()
role = input("Rôle/Fonction : ").strip()
departement = input("Département/Classe/Service : ").strip()

# Vérification si le matricule existe déjà dans le CSV
existe_deja = False
with open(CSV_FILE, mode="r", encoding='utf-8') as f:
    reader = csv.reader(f)
    next(reader)  # ignorer l'en-tête
    for row in reader:
        if matricule == row[0]:
            existe_deja = True
            break

if existe_deja:
    print(f"[ERREUR] Le matricule {matricule} existe déjà dans la base !")
    exit()

# === Création d'un dossier pour l'utilisateur ===
user_dir = os.path.join(BASE_DIR, matricule)
if not os.path.exists(user_dir):
    os.makedirs(user_dir)

# === Capture de la vidéo ===
cap = cv2.VideoCapture(0)
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")

count = 0
while True:
    ret, frame = cap.read()
    if not ret:
        print("Erreur lors de l'accès à la caméra.")
        break

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.2, minNeighbors=5, minSize=(100, 100))

    for (x, y, w, h) in faces:
        count += 1
        # Dessiner un rectangle autour du visage
        cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)

        # Sauvegarder l'image
        face_filename = os.path.join(user_dir, f"{matricule}_{count}.jpg")
        cv2.imwrite(face_filename, gray[y:y + h, x:x + w])

        cv2.putText(frame, f"Image {count}", (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

    cv2.imshow("Capture des visages", frame)

    # Quitter avec 'q' ou après 30 images
    if cv2.waitKey(1) & 0xFF == ord('q') or count >= 30:
        break

cap.release()
cv2.destroyAllWindows()

# === Enregistrer les informations dans le CSV ===
with open(CSV_FILE, mode="a", newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow([matricule, nom, role, departement, datetime.now().strftime("%Y-%m-%d %H:%M:%S")])

print(f"\n[SUCCÈS] Capture terminée pour {nom} ({matricule}) !")
print(f"Les images ont été enregistrées dans : {user_dir}")
print(f"Les informations ont été ajoutées à {CSV_FILE}")
