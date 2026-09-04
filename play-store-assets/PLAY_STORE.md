# Publicación en Google Play — Epic Hypernova Score Tracker

## Datos de la app
- **applicationId**: `com.epichypernova.scoretracker`
- **versionName / versionCode**: `1.0.0` / `1`
- **minSdk / targetSdk**: 24 / 35
- **Bundle a subir**: `EpicHypernova-release.aab` (raíz del proyecto)
- **Categoría sugerida**: Juegos → Casual / Herramientas de juego
- **Clasificación de contenido**: Para todos (sin violencia, sin compras, sin anuncios)
- **Anuncios**: No
- **Compras dentro de la app**: No

## Firma
- **Recomendado: activar Play App Signing** (Play gestiona la clave de firma final; vos subís con
  la clave de subida). La clave de subida es `epic-hypernova-release.keystore`
  (alias `hypernova`). **Guardá el archivo y la contraseña** (`keystore.properties`, git-ignored).
- SHA-256 de la clave de subida:
  `C0:24:12:5A:CD:85:DA:53:B6:4A:7F:E1:F2:44:0B:FC:EF:11:C3:B5:A2:64:98:04:4A:89:2D:7F:5B:92:CA:5A`
- Si perdés la clave de subida, con Play App Signing se puede resetear desde la Play Console.

## Assets incluidos
- `play_icon_512.png` — ícono de ficha 512×512.
- `feature_graphic_1024x500.png` — gráfico destacado 1024×500.
- **Faltan capturas** (2–8, teléfono): sacarlas del dispositivo una vez instalada (menú, tabla de
  puntos, Truco, Magic). Mínimo 2, formato PNG/JPG, lado corto ≥ 320 px.

## Ficha — Español

**Título (30):** Epic Hypernova Score Tracker

**Descripción corta (80):**
Anotador de puntos para juegos de mesa y cartas. Local, sin cuentas, en tu teléfono.

**Descripción larga:**
Epic Hypernova Score Tracker es un anotador de puntos rápido y prolijo para tus partidas de juegos
de mesa y de cartas. Todo se guarda en tu teléfono: sin cuentas, sin conexión y sin anuncios.

• Juegos genéricos por manos: basas, chinchón, generala, escoba. Elegí jugadores y reglas
  (puntaje objetivo, gana el mayor o el menor, manos fijas, apuestas) y cargá cada mano con un
  teclado rápido.
• Truco: anotá con porotos (malas y buenas), a 30, con marcador de partidos, deshacer y nuevo
  partido.
• Magic: The Gathering: contador de vidas 1 vs 1 y commander a 4, con veneno, energía y daño de
  comandante; el teléfono se apoya en la mesa con la mitad rival dada vuelta.
• Jugadores del dispositivo: guardá nombres y colores; los frecuentes aparecen primero.
• Historial de partidas terminadas.
• Español e inglés.

Próximamente: Pokémon TCG, Yu-Gi-Oh! y Digimon TCG.

## Ficha — English

**Title (30):** Epic Hypernova Score Tracker

**Short description (80):**
A score tracker for board and card games. Local, no accounts, right on your phone.

**Full description:**
Epic Hypernova Score Tracker is a fast, tidy score keeper for your board and card game nights.
Everything is stored on your phone: no accounts, no connection and no ads.

• Generic hand-based games: tricks, rummy, generala, escoba. Pick players and rules (target score,
  highest or lowest wins, fixed hands, bids) and enter each hand with a quick keypad.
• Truco: score with beans (bad and good rows), to 30, with a match counter, undo and new match.
• Magic: The Gathering: 1v1 and 4-player commander life counters with poison, energy and commander
  damage; rest the phone on the table with the opponent's half rotated.
• Device players: save names and colors; frequent players show first.
• History of finished games.
• Spanish and English.

Coming soon: Pokémon TCG, Yu-Gi-Oh! and Digimon TCG.

## Política de privacidad (texto listo para publicar)
Ver `PRIVACY.md`. Google Play exige una URL pública de política de privacidad: publicá ese texto
en cualquier página (por ej. GitHub Pages, Notion público o un Gist) y pegá la URL en la ficha.

## Data safety (formulario de Play)
- ¿Recopila o comparte datos del usuario? **No.**
- Todos los datos (jugadores, partidas, historial) se guardan **solo en el dispositivo** con
  DataStore local. No hay red, analítica ni terceros.

## Pasos para publicar
1. Crear la app en Play Console (idioma por defecto: Español o English).
2. Completar ficha con los textos de arriba + ícono 512 + feature graphic + capturas.
3. Content rating, Data safety (todo "No"), Target audience.
4. Producción → Crear release → subir `EpicHypernova-release.aab`.
5. Activar Play App Signing cuando lo ofrezca.
6. Enviar a revisión.
