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
- Anbindung der Domainklassen (Spieler, Zug, Partie etc.)
- Navigation zwischen Bildschirmen
- Verbindung zum STOMP/WebSocket Server bereits vollständig implementiert

### 🔧 In Arbeit:
- Spielbildschirm mit Schachbrett-UI
- Grobes Redesign von ausgewählten Screens
- Vorbereitung auf Anbindung zur Spiellogik

### 📋 Geplant:
- erstes Theme dunkel & hell anwenden
- Spielverlauf & Timeranzeige
- Weitere Themes (falls noch Zeit bleibt)
- visuelle Highlights

### Weggelassen:
- Google Integration (stattdessen auf eigenes System umgestellt)
- Animationen der Züge
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

---

## 🤝 Mitarbeit & Konventionen

- Bitte keine Spiellogik ins Frontend integrieren – das Frontend ist zustandsarm.
- Design: Kaffeehaus-Ästhetik bevorzugt ☕😉
- UI-Komponenten modular halten (z. B. Schachbrett als eigene `Composable`)
- Benennungskonventionen: `CamelCase` für Klassen und Methoden, `snake_case` für Ressourcen

---

## 🔗 Backend & weitere Repositories

> Das Backend ist separat entwickelt und enthält sämtliche Spiellogik.
> Es handelt sich dabei um einen Java Springboot Server

---

**Viel Spaß beim Coden – und immer schön wach bleiben mit ChessPresso!**
