import cv2
import os
import numpy as np
import pickle

# === Répertoires et fichiers ===
BASE_DIR = "visages"  # dossier contenant les sous-dossiers par matricule
MODEL_FILE = "modele_lbph.yml"
LABELS_FILE = "labels.pkl"

# === Créer le recognizer LBPH ===
recognizer = cv2.face.LBPHFaceRecognizer_create()

faces = []
labels = []
label_ids = {}
current_id = 0

# === Parcourir les images pour l'entraînement ===
for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.lower().endswith(".jpg") or file.lower().endswith(".png"):
            path = os.path.join(root, file)
            label = os.path.basename(root)  # le nom du dossier = matricule

            if label not in label_ids:
                label_ids[label] = current_id
                current_id += 1
            id_ = label_ids[label]

            image = cv2.imread(path, cv2.IMREAD_GRAYSCALE)
            if image is None:
                continue

            faces.append(image)
            labels.append(id_)

if len(faces) == 0:
    print("[ERREUR] Aucune image trouvée dans le dossier 'visages/'.")
    exit()

# === Entraînement du modèle ===
recognizer.train(faces, np.array(labels))
recognizer.save(MODEL_FILE)
print(f"[SUCCÈS] Modèle entraîné et sauvegardé dans {MODEL_FILE}")

# === Sauvegarde des labels séparément ===
with open(LABELS_FILE, "wb") as f:
    pickle.dump(label_ids, f)
print(f"[SUCCÈS] Labels enregistrés dans {LABELS_FILE}")
