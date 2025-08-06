# ♟️ ChessPresso – Frontend

Willkommen bei **ChessPresso**, einer stilvollen Schach-App mit Kaffeethema!  
Dieses Repository enthält das **Frontend** der App, das in **Kotlin mit Jetpack Compose** entwickelt wird.

## ☕ Projektüberblick

**ChessPresso** kombiniert klassische Schachfunktionalität mit einem modernen UI-Ansatz – inspiriert vom Flair eines gemütlichen Kaffeehauses.  
Das Frontend ist bewusst schlank gehalten und übernimmt primär die Darstellung und die Kommunikation mit dem Server. Die Spiellogik liegt vollständig im Backend.

---

## 📱 Aktueller Status

### ✅ Bereits implementiert:
- **HomeScreen** mit Grunddesign im „Espresso“-Theme
- Projektstruktur und Basis-Setup + Inkludieren von Jetpack Compose etc.

### 🔧 In Arbeit:
- Anbindung der Domainklassen (Spieler, Zug, Partie etc.)
- Integration von Google (z. B. Google Sign-In oder Firebase)
- Navigation zwischen Bildschirmen
- Erste Verbindungspunkte zum Server (STOMP/WebSocket)

### 📋 Geplant:
- Spielbildschirm mit Schachbrett-UI
- Spielverlauf & Timeranzeige
- Weitere Themes
- Animierte Züge und visuelle Highlights

---

## 🌈 Themes

ChessPresso wird mit mehreren stilvollen Themes erscheinen – das Haupttheme ist **Espresso**:
- **Espresso Theme:** Dunkle Töne, warme Brauntöne, stilisiertes Logo in Form einer Espressotasse mit Schachfigur
- Weitere Themes sind geplant und über die Settings auswählbar

---

## 🛠️ Tech-Stack

- **Sprache:** Kotlin
- **UI-Framework:** Jetpack Compose
- **IDE:** Android Studio
- **Kommunikation:** 
- **Design-Prinzip:** Clean UI – alle Spiellogik kommt vom Server

---

## 🗂️ Projektstruktur (Auszug)

chesspresso-frontend/
├── ui/
│ ├── theme/
│ ├── screens/
│ └── components/
├── model/
│ └── domain/ // z. B. Spieler, Zug etc.
├── network/
│ └── connection/
├── MainActivity.kt
└── ...

yaml
Kopieren
Bearbeiten

---

## 🤝 Mitarbeit & Konventionen

- Bitte keine Spiellogik ins Frontend integrieren – das Frontend ist zustandsarm.
- Design: Kaffeehaus-Ästhetik bevorzugt ☕😉
- UI-Komponenten modular halten (z. B. Schachbrett als eigene `Composable`)
- Benennungskonventionen: `CamelCase` für Klassen und Methoden, `snake_case` für Ressourcen

---

## 📸 Vorschau

| HomeScreen (Espresso Theme) |
|-----------------------------|
| *(Bild folgt nach Fertigstellung)* |

---

## 🔗 Backend & weitere Repositories

> Das Backend ist separat entwickelt und enthält sämtliche Spiellogik.
> Es handelt sich dabei um einen Java Springboot Server

---

Du hast Ideen für weitere Themes oder UI-Komponenten?  
Erstelle gerne ein Issue oder öffne einen Pull Request!

---

**Viel Spaß beim Coden – und immer schön wach bleiben mit ChessPresso!**
