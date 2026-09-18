# Ficha de Google Play — Epic Hypernova Score Tracker

Actualizado: 2026-09-17 (v1.2.0). Copiar/pegar cada bloque en Play Console → **Crecimiento → Ficha
de Play Store → Ficha principal**. Los límites de caracteres están indicados; todos los textos de
abajo los cumplen.

## Datos técnicos de la app
- **applicationId**: `epic.score.tracker`
- **versionName / versionCode**: `1.2.1` / `5` (prueba cerrada; 1–4 ya usados en Play)
- **minSdk / targetSdk**: 24 / 36
- **Bundle**: `EpicHypernova-v1.2.1-vc5.aab` (raíz del proyecto)
- **Categoría**: Aplicación → **Entretenimiento** (no "Juego": es una herramienta para jugar en mesa)
- **Anuncios**: **Sí** — Google AdMob (banner, intersticial cada varias partidas, y recompensado
  opcional para desbloquear avatares)
- **Compras dentro de la app**: No
- **Política de privacidad**: https://tourmaline-crepe-24a9b4.netlify.app/ (fuente: `docs/privacy-policy.html`)
- **Correo de contacto**: nuestra.caja.app@gmail.com

## Firma
- Play App Signing activado; clave de subida `epic-hypernova-release.keystore` (alias `hypernova`),
  contraseñas en `keystore.properties` (git-ignored). **Guardar backup del keystore.**
- SHA-256 clave de subida:
  `C0:24:12:5A:CD:85:DA:53:B6:4A:7F:E1:F2:44:0B:FC:EF:11:C3:B5:A2:64:98:04:4A:89:2D:7F:5B:92:CA:5A`

## Assets gráficos
- `play_icon_512.png` — ícono 512×512 (obligatorio).
- `feature_graphic_1024x500.png` — gráfico destacado 1024×500 (obligatorio).
- **Capturas de teléfono: FALTAN.** Mínimo 2, máximo 8, PNG/JPG, relación 16:9 o 9:16, lado corto
  ≥ 320 px, lado largo ≤ 3840 px. Sacarlas en el teléfono (volumen abajo + power) con la app en
  español y subirlas en este orden:
  1. Menú de juegos (la grilla con los 22 juegos)
  2. Setup de jugadores con avatares y colores
  3. Truco (porotos)
  4. Magic commander a 4 (con la mitad rival dada vuelta)
  5. Manos y puntos: tabla de una partida en curso
  6. Ficha de D&D
  7. Uno con la calculadora de cartas
  8. Historial
- Opcional: las mismas capturas en inglés para la ficha en-US.

---

## Ficha — Español (idioma predeterminado: es-419 o es-ES)

**Nombre de la app (máx. 30):**
```
Epic Hypernova Score Tracker
```

**Descripción breve (máx. 80):**
```
Anotador de puntos para 22 juegos de mesa y cartas. Sin cuentas, todo local.
```

**Descripción completa (máx. 4000):**
```
Epic Hypernova es el anotador de puntos para tus noches de juegos de mesa y cartas. Elegís el juego, cargás a los jugadores y la app se encarga de contar: reglas, límites, quién va ganando y quién ganó. Todo se guarda en tu teléfono, sin cuentas ni registro.

22 JUEGOS CON SU PROPIO MARCADOR

Cartas y mesa
• Truco: porotos en malas y buenas, a 30, marcador de partidos y deshacer.
• Chinchón: gana el menor, a 100.
• Escoba de 15: cartas, oros, velo, setenta y escobas.
• Mus: piedras, amarrakos, juegos y vacas.
• Burako: canastas, a 2000.
• Generala: planilla completa con servidas.
• Uno: puntos a 500 con calculadora de cartas.
• Póker: timer de ciegas.
• Dardos 501: cuenta regresiva a 0.
• Bowling: 10 frames con strikes y spares.

Juegos de cartas coleccionables
• Magic: The Gathering: vidas, veneno, energía y daño de comandante. Modo 1 vs 1 y commander a 4 con la mitad rival dada vuelta para apoyar el teléfono en la mesa.
• Pokémon TCG: premios y marcadores de daño.
• Yu-Gi-Oh!: life points desde 8000.
• Digimon TCG: memoria y seguridad.
• Disney Lorcana: lore a 20.
• One Piece Card Game: vidas y DON!!.
• Star Wars Unlimited: vida de la base e iniciativa.

Rol y miniaturas
• Dungeons & Dragons: ficha del grupo con vida, CA e iniciativa, más notas de campaña.
• Warhammer 40k: puntos de victoria y CP por ronda.

Genéricos
• Manos y puntos: para basas, chinchón, generala, escoba o cualquier juego por manos. Puntaje objetivo, gana el mayor o el menor, manos fijas y apuestas.
• Contador simple: sumar y restar sin manos, para lo que sea.

PENSADO PARA LA MESA
• Jugadores guardados con nombre, color y avatar; los frecuentes aparecen primero.
• Teclado rápido para cargar cada mano sin perder tiempo.
• Deshacer, nueva partida y marcador de partidos ganados.
• Historial de partidas terminadas.
• Sonidos configurables.
• Español e inglés.

TUS DATOS SE QUEDAN EN TU TELÉFONO
No hay cuentas, servidores ni analítica. Los jugadores, partidas e historial se guardan solo en tu dispositivo. La app muestra anuncios de Google AdMob para mantenerse gratis; podés desbloquear avatares extra mirando un anuncio si querés.

Política de privacidad: https://tourmaline-crepe-24a9b4.netlify.app/
```

---

## Ficha — English (en-US)

**App name (max 30):**
```
Epic Hypernova Score Tracker
```

**Short description (max 80):**
```
Score keeper for 22 board & card games. No accounts, data stays on your phone.
```

**Full description (max 4000):**
```
Epic Hypernova is the score keeper for your board and card game nights. Pick the game, add the players and the app does the counting: rules, limits, who's ahead and who won. Everything is stored on your phone, with no accounts or sign-up.

22 GAMES, EACH WITH ITS OWN SCOREBOARD

Cards & tabletop
• Truco: beans on bad and good rows, to 30, match counter and undo.
• Chinchón: lowest wins, to 100.
• Escoba de 15: cards, golds, seven of golds, seventy and sweeps.
• Mus: stones, amarrakos, games and vacas.
• Burako: canastas, to 2000.
• Generala: full sheet with served rolls.
• Uno: points to 500 with a card calculator.
• Poker: blinds timer.
• Darts 501: countdown to 0.
• Bowling: 10 frames with strikes and spares.

Trading card games
• Magic: The Gathering: life, poison, energy and commander damage. 1v1 and 4-player commander with the opponent's half rotated so the phone rests on the table.
• Pokémon TCG: prize cards and damage counters.
• Yu-Gi-Oh!: life points from 8000.
• Digimon TCG: memory and security.
• Disney Lorcana: lore to 20.
• One Piece Card Game: life and DON!!.
• Star Wars Unlimited: base life and initiative.

RPG & miniatures
• Dungeons & Dragons: party sheet with HP, AC and initiative, plus campaign notes.
• Warhammer 40k: victory points and CP per round.

Generic
• Hands & points: for tricks, rummy, generala, escoba or any hand-based game. Target score, highest or lowest wins, fixed hands and bids.
• Simple counter: add and subtract with no hands, for anything.

BUILT FOR THE TABLE
• Saved players with name, color and avatar; frequent players show first.
• Quick keypad to enter each hand without slowing the game down.
• Undo, new game and won-matches counter.
• History of finished games.
• Adjustable sounds.
• Spanish and English.

YOUR DATA STAYS ON YOUR PHONE
No accounts, servers or analytics. Players, games and history are stored only on your device. The app shows Google AdMob ads to stay free; you can unlock extra avatars by watching an ad if you want.

Privacy policy: https://tourmaline-crepe-24a9b4.netlify.app/
```

---

## Política → Contenido de la app (respuestas)
| Sección | Respuesta |
|---|---|
| Política de privacidad | https://tourmaline-crepe-24a9b4.netlify.app/ |
| Anuncios | **Sí**, contiene anuncios |
| Acceso a la app | Todas las funciones disponibles sin acceso especial |
| Clasificación de contenido (cuestionario IARC) | Categoría *Utilidad, productividad, comunicación u otros*. Todo "No" (sin violencia, sexo, drogas, apuestas con dinero real, contenido generado por usuarios, ni compartir ubicación). Resultado esperado: **PEGI 3 / Everyone** |
| Público objetivo | **13 años o más** (no dirigida a niños) |
| Apps de noticias | No |
| Rastreo de contactos COVID | No |
| Seguridad de los datos | Ver abajo |
| Apps gubernamentales | No |
| Funciones financieras | No |
| Salud | No |

### Seguridad de los datos
- ¿Recopila o comparte datos? **Sí** (SDK de AdMob).
- ¿Cifrado en tránsito? **Sí**. ¿Mecanismo de borrado? **No** (no hay cuentas ni servidor).
- Tipos de datos (todos: recopilados **y** compartidos, obligatorios, finalidad **Publicidad o marketing**):
  - Identificadores del dispositivo u otros → *ID del dispositivo u otros identificadores*
  - Actividad en la app → *Interacciones con la app*
  - Rendimiento de la app → *Datos de diagnóstico*
- Todo lo demás: No. Los nombres de jugadores no se declaran porque nunca salen del dispositivo.

## Pendiente antes de producción (no bloquea la prueba cerrada)
1. IDs reales de AdMob en `admob.properties` (hoy la release usa IDs de test).
2. Publicar el mensaje GDPR en AdMob → Privacidad y mensajes, con la URL de la política.
3. Capturas de pantalla (sí bloquean: Play las exige para cualquier track, incluida la prueba cerrada).
