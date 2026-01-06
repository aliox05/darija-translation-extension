# Darija Translation Extension

A Chrome extension with a Java backend that translates text into Moroccan Darija.

##  Structure
- `frontend/` → Chrome extension (manifest, popup, sidebar, JS, PHP)
- `backend/` → Jakarta REST API (JWT auth, translation logic)

##  Setup

### Frontend
1. Open Chrome → Extensions → Enable Developer Mode
2. Load unpacked → select `frontend/`

### Backend
```bash
cd backend
mvn clean install
mvn jakarta:run
