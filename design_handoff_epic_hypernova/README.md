# Handoff: Epic Hypernova Score Tracker: Omega Protocol

Anotador de puntos para juegos de mesa. App móvil (Android), interfaz en español.

## Overview

App local (sin cuentas, sin backend) para llevar el puntaje de partidas de juegos de mesa y de cartas. Dos familias de juegos:

- **Genéricos por manos**: basas, chinchón, generala, escoba. N jugadores, se juegan muchas manos, se suman puntos, gana el mayor o el menor según configuración.
- **Específicos**: cada uno con su propia interfaz y mecánica — Truco (porotos, 30 puntos, buenas/malas, partidos), Magic: The Gathering (vidas, veneno, energía, commander a 4), y en el menú ya figuran Pokémon TCG, Yu-Gi-Oh! y Digimon TCG (pantallas aún no diseñadas).

Además hay una sección de **jugadores locales del dispositivo** (CRUD de nombres + color) que alimenta el armado de partidas genéricas, y un **historial** de partidas terminadas.

## About the Design Files

Los archivos `.dc.html` de este bundle son **referencias de diseño hechas en HTML**: prototipos que muestran la apariencia y el comportamiento buscado, no código de producción para copiar. La tarea es **recrear estos diseños en el entorno del codebase destino** (React Native, Flutter, Kotlin/Compose, SwiftUI, etc.) usando sus patrones y librerías establecidas. Si todavía no hay codebase, elegir el framework más apropiado (para una app Android sencilla y local: Kotlin + Jetpack Compose, o React Native si se quiere multiplataforma) e implementar ahí.

Los diseños están montados dentro de un marco de dispositivo Android de 412×892 px (viewport lógico de la pantalla: 412 × 813 útiles entre status bar y barra de gestos). Todas las medidas del documento están en px lógicos a ese ancho.

## Fidelity

**High-fidelity.** Colores, tipografías, tamaños, espaciados y estados finales están definidos. Recrear la UI fielmente con las librerías del codebase. Los datos que se ven (nombres, puntajes, historial) son de ejemplo.

## Screens / Views

### 1. Main menu — variante A "lista" (id 2a en el archivo)
- **Purpose**: elegir qué anotar; retomar la partida en curso.
- **Layout**: columna vertical. Header con logo (`logo.png`, ancho 254px, alto automático) a la izquierda y botón de ajustes circular 40×40 a la derecha (padding 22/20/14). Debajo: tarjeta "En curso", secciones "Genéricos", "Guardados", "Juegos específicos", y barra de tabs fija abajo. Padding lateral 20px en todas las secciones; separación entre filas 8px.
- **Components**:
  - *Tarjeta "En curso"*: radius 20, padding 18, fondo `linear-gradient(160deg, #21406F 0%, #16294F 100%)`, borde `1px solid rgba(47,211,240,0.45)`. Label "EN CURSO" 10px / letter-spacing 0.2em / uppercase / `#2FD3F0`; título "Chinchón" Cinzel 700 23px uppercase letter-spacing 0.03em; subtítulo 13px `#A9BCE0` ("Mano 7 · Vale va ganando con 38", el número en `#55E6A5` 600); botón circular 48×48 `#2FD3F0` con "▸" `#0B1730`.
  - *Fila de juego* (genéricos y específicos): fondo `rgba(255,255,255,0.045)`, borde `1px solid rgba(255,255,255,0.10)`, `box-shadow: inset 0 1px 0 rgba(255,255,255,0.10)`, radius 16, padding 14, gap 14. Cuadro de ícono 42×42 radius 13 (fondo según juego, ver tokens); título 15.5px 600; subtítulo 12.5px `#8497BC`; chevron "›" 16px `#556792`.
  - *Configuraciones guardadas*: fila horizontal de tarjetas min-width 170, radius 16, borde `1px dashed rgba(47,211,240,0.35)`, padding 12/14. Título 14px 600, detalle 12px `#8497BC` ("4 jugadores · 100 pts · menor").
  - *Section labels*: 10px, letter-spacing 0.2em, uppercase, `#6C7EA3`.
  - *Tab bar*: 3 columnas iguales, `position: sticky; bottom: 0`, fondo `#0A1327`, borde superior `1px solid rgba(255,255,255,0.09)`, padding 9/0/11. Ítem activo `#2FD3F0` con label 11px 600; inactivos `#7387AF` 11px. Glifos ♠ / ◷ / ☺ a 17px.
- **Copy exacta**: secciones "Genéricos" / "Guardados" (con acción "Editar" 12px 600 `#2FD3F0`) / "Juegos específicos". Filas: "Manos y puntos — Basas, chinchón, generala, escoba"; "Contador simple — Sumar y restar, sin manos"; "Truco — Porotos, 30 puntos, partidos"; "Magic: The Gathering — Vidas, veneno, commander"; "Pokémon TCG — Premios, marcadores de daño"; "Yu-Gi-Oh! — Life points 8000"; "Digimon TCG — Memoria, seguridad". Tabs: Juegos / Historial / Jugadores.

### 2. Main menu — variante B "mosaico" (2b)
Misma información en grilla. Logo 196px. Buscador: alto 46, radius 999, fondo `rgba(255,255,255,0.05)`, borde `1px solid rgba(255,255,255,0.10)`, placeholder "Buscar un juego" 14px `#7387AF` con "⌕" 15px. Banner de retomar: radius 16, borde `1px solid rgba(47,211,240,0.45)`, fondo `rgba(47,211,240,0.08)`, punto 8×8 `#55E6A5`, texto "Retomar Chinchón · mano 7" 14px 600 + "Sofi, Nacho, Vale, Tomi" 12px `#A9BCE0`, acción "Abrir" 12px 700 `#2FD3F0`. Grilla 2 columnas gap 10: tiles genéricos 136px de alto (el primero fondo `#2FD3F0` con texto `#0B1730`), tiles específicos 126px con cuadro de ícono 36×36 radius 11, y un tile final "Pedir un juego" con borde `1px dashed rgba(255,255,255,0.18)`.
**Nota**: 2a y 2b exceden los 813px útiles (~1030px de contenido); el scroll es intencional y la tab bar queda fija.

### 3. Genérico — armar partida (2c)
- **Purpose**: elegir jugadores y reglas antes de empezar; opcionalmente guardar la configuración.
- **Layout**: header con "‹" + título "Manos y puntos" (Cinzel 700 20px uppercase letter-spacing 0.04em) y sobretítulo "NUEVA PARTIDA" 11px letter-spacing 0.16em `#2FD3F0`, borde inferior `1px solid rgba(255,255,255,0.09)`. Luego: chips de jugadores, bloque "Reglas", bloque "Guardar", y CTA fijo abajo.
- **Components**:
  - *Chips de jugador*: radius 999, fondo `rgba(255,255,255,0.05)`, borde `1px solid rgba(255,255,255,0.11)`, padding 6/13/6/6, avatar 27×27 circular con color del jugador y inicial Cinzel 700 12px sobre `#0B1730`, nombre 14px 500, "✕" 13px `#6C7EA3`. Chip "＋ Agregar" con borde `1px dashed rgba(47,211,240,0.45)` y texto `#2FD3F0` 14px 600. Contador a la derecha del label: "4 de 6".
  - *Puntaje objetivo*: tarjeta estándar (radius 16) con stepper: dos botones circulares 34×34 borde `1px solid rgba(255,255,255,0.16)` y el valor en Orbitron 700 20px `#2FD3F0`, min-width 52, tabular-nums.
  - *Gana*: segmented de 2 opciones dentro de contenedor radius 999 fondo `rgba(0,0,0,0.28)` padding 4; opción activa fondo `#2FD3F0` texto `#0B1730` 13.5px 700; inactiva `#A9BCE0`.
  - *Toggles* ("Cantidad de manos fija — 8 manos y cierra sola", "Apuestas declaradas — Pedir basas antes de cada mano", "Guardar esta configuración"): pista 48×28 radius 999, fondo `#2FD3F0` encendido / `rgba(255,255,255,0.12)` apagado, perilla 22×22 `#0B1730`.
  - *Nombre de configuración*: campo con borde inferior `1.5px solid #2FD3F0`, valor 15px, contador "18/32" 12px `#6C7EA3`.
  - *CTA "Empezar partida"*: sticky bottom, alto 56, degradado `linear-gradient(180deg, #6BE4FA 0%, #2FD3F0 55%, #14A8CC 100%)`, texto `#04121C` Cinzel 900 16px uppercase letter-spacing 0.14em, glow `0 0 24px rgba(47,211,240,0.35)`, esquinas en chaflán `clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%, 0 14px)`.

### 4. Genérico — tabla + carga de mano (2d)
- **Purpose**: ver el acumulado por jugador y cargar los puntos de la mano actual.
- **Layout**: header compacto ("Chinchón" 16px 600 + meta "Mano 7 · a 100 · gana el menor" 12px `#8497BC`, "⋮" a la derecha). Grilla de 2 columnas `34px 1fr` para alinear la etiqueta de mano con las columnas de jugadores (`repeat(N, 1fr)`, gap 6). Bloques: totales, barras de progreso, filas de manos, nota, y hoja de carga anclada abajo.
- **Components**:
  - *Totales*: nombre 11.5px en el color del jugador (letter-spacing 0.04em, elipsis), total Orbitron 700 25px; el mejor total en `#55E6A5`, el resto `#EAF1FF`.
  - *Barra*: alto 4, radius 999, pista `rgba(255,255,255,0.10)`, relleno del color del jugador. **Semántica**: el ancho es `(objetivo − total) / objetivo` — en "gana el menor", más lleno = más cerca de ganar.
  - *Fila de mano*: fondo `rgba(255,255,255,0.035)`, borde `1px solid rgba(255,255,255,0.07)`, radius 12, padding 8/10. Etiqueta "M1…M6" 11px `#6C7EA3` letter-spacing 0.08em; celda con puntos 16px 600 tabular-nums y, si las apuestas están activas, "pedía N" 10px `#5E7099` debajo.
  - *Nota*: 11.5px `#8497BC` centrada — "Vale va ganando con 36 · la partida cierra cuando alguien llega a 100".
  - *Hoja de carga (bottom sheet)*: anclada al fondo, fondo `#142547`, borde superior `1px solid rgba(47,211,240,0.35)`, radius 22 arriba, sombra `0 -18px 40px rgba(0,0,0,0.45)`, padding 12/16/18, gap 12; grabber 38×4. Título "Mano 7" 15px 600 y hint "apuesta / puntos" 11.5px. Una columna por jugador: nombre 11.5px en su color, casilla de apuesta 34px de alto radius 10 fondo `rgba(255,255,255,0.07)`, casilla de puntos 50px de alto radius 12 borde `1.5px solid #2FD3F0` fondo `rgba(47,211,240,0.10)` con el valor en Orbitron 700 19px `#CFF6FF`. Teclado rápido: 5 teclas (1, 2, 3, 5, ⌫) de 42px radius 11 fondo `rgba(255,255,255,0.07)`. Acciones: "Cancelar" (94×50, borde `1px solid rgba(255,255,255,0.16)`) y "Guardar mano" (chaflán 12px, mismo degradado cian, Cinzel 900 14px uppercase letter-spacing 0.12em).
  - *Reserva de espacio*: el contenido deja 330px libres arriba de la hoja para que nada quede tapado.

### 5. Truco — porotos 30/30 (2e)
- **Purpose**: anotar con la lógica de porotos: malas (1–15) y buenas (16–30), por bando.
- **Layout**: header ("Truco" + "A 30 · partidos 1 – 0", el 1 en `#2FD3F0` 600). Cuerpo en grilla `1fr 1px 1fr` con separador `rgba(255,255,255,0.10)`. Cada lado: label de bando, puntaje grande, bloque Buenas, línea divisoria, bloque Malas, y botonera al pie.
- **Components**:
  - *Bandos*: "NOSOTROS" 10px letter-spacing 0.2em `#2FD3F0`, número Orbitron 700 60px `#CFF6FF`; "ELLOS" `#E24BD6`, número `#FFD9F6`.
  - *Porotos*: grupos de 5 palitos = 4 barras verticales de 3×26 radius 2 color `#EAF1FF` con gap 4, más la 5.ª cruzada (`position: absolute; left 0; top 11px; width 30; height 3; transform: rotate(-24deg)`). Contenedores de min-height 96, wrap, gap 12, centrados. Labels "BUENAS"/"MALAS" 9.5px letter-spacing 0.18em `#6C7EA3`. Divisoria: `1px solid rgba(47,211,240,0.55)` del lado Nosotros y `rgba(226,75,214,0.55)` del lado Ellos.
  - *Botonera*: +1 / +2 / +3 de 48px radius 13 (Nosotros `#2FD3F0` sobre `#0B1730`; Ellos `#E24BD6` sobre `#240426`), 17px 700; abajo "− Quitar" 40px con borde `1px solid rgba(255,255,255,0.16)`.
  - *Pie*: "Deshacer" y "Nuevo partido", ambos 48px radius 999 (el segundo con fondo `rgba(255,255,255,0.07)`), sobre `#0A1327`.
  - Estado mostrado: Nosotros 18 (3 grupos en malas + 3 palitos en buenas), Ellos 12 (2 grupos + 2 palitos, buenas vacías con "—" `#465C86`).

### 6. Magic — 1v1 sobre la mesa (2f)
- **Purpose**: contador de vidas para dos jugadores con el teléfono apoyado entre ambos.
- **Layout**: dos mitades de igual alto; la superior con `transform: rotate(180deg)` para el rival. Entre ellas, una barra de 11/16 con bordes `1px solid rgba(47,211,240,0.28)` arriba y abajo.
- **Components**: nombre 11px letter-spacing 0.2em en el color del jugador; vida en Orbitron 800 88px `#FFFFFF` tabular-nums; botones − / ＋ circulares 58×58 borde `1px solid rgba(255,255,255,0.18)` (área táctil real recomendada ≥ 64). Chips de contadores radius 999 padding 7/14 13px sobre `rgba(255,255,255,0.08)`: "☠ 3" (veneno, glifo `#55E6A5`), "⚡ 0" (energía, glifo `#2FD3F0`), y "＋ contador" con borde `1px dashed rgba(255,255,255,0.20)`. Fondos por mitad: rival `radial-gradient(120% 90% at 50% 100%, #2A2354 0%, #171B3D 60%, #121533 100%)`; jugador `radial-gradient(120% 90% at 50% 100%, #123A48 0%, #10283A 60%, #0E2033 100%)`. Barra central: "VIDAS 20" 11px letter-spacing 0.14em `#6C7EA3`, "⟳ Reiniciar" (borde) y "⚄ Sortear" (fondo `#2FD3F0`, texto `#0B1730` 700), ambos 36px radius 999.

### 7. Magic — commander 4 jugadores (2g)
Grilla 2×2 gap 2; los dos cuadrantes de arriba rotados 180°. Cada cuadrante: nombre 10.5px letter-spacing 0.18em en su color, vida Orbitron 700 62px, chips "☠ n" (veneno) y "⚔ n" (daño de comandante) 11.5px sobre `rgba(255,255,255,0.09)`. Fondos radiales orientados a la esquina externa (violeta `#2A2354`, rosa `#4A2338`, cian `#123A48`). Jugador eliminado: fondo plano `#131A2E`, número `#3E4A6B` y leyenda "ELIMINADO" 10.5px letter-spacing 0.16em `#2FD3F0`. Pie igual al de 1v1 con "COMMANDER · 40".

### 8. Jugadores del dispositivo (2h)
- **Purpose**: CRUD de los nombres locales que se usan en las partidas genéricas.
- **Layout**: sobretítulo "GUARDADOS EN ESTE TELÉFONO" 10px letter-spacing 0.2em `#2FD3F0`; título "JUGADORES" Cinzel 700 29px uppercase letter-spacing 0.04em; botón "＋ Agregar jugador" (alto 50, radius 16, borde `1px dashed rgba(47,211,240,0.45)`, texto `#2FD3F0` 15px 600); lista de tarjetas; tab bar fija con "Jugadores" activo.
- **Components**: tarjeta estándar radius 16 padding 12/14 gap 14, avatar circular 42×42 del color del jugador con inicial Cinzel 700 20px sobre `#0B1730`, nombre 15.5px 600, dato secundario 12px `#8497BC` ("24 partidas"), acción "✎" 15px `#6C7EA3`.

### 9. Editar jugador (2i)
Pantalla con teclado visible. Header: "✕", título "Editar jugador" 16px 600, acción "Guardar" 14px 700 `#2FD3F0`. Avatar 78×78 del color elegido con inicial Cinzel 700 34px. Campo "NOMBRE" (label 10px letter-spacing 0.2em `#6C7EA3`) con valor 19px y borde inferior `1.5px solid #2FD3F0` + caret 1.5×22. Selector de color: 6 círculos de 40px (`#2FD3F0`, `#55E6A5`, `#FF6FA8`, `#A18AF5`, `#3B7BF7`, `#F27BA9`); el activo con `box-shadow: 0 0 0 2px #0C1730, 0 0 0 4px #2FD3F0`. Toggle "Jugador frecuente — Aparece primero al armar partida" (apagado). Acción destructiva "Eliminar jugador" 14.5px 600 `#FF6FA8` con nota "Las partidas jugadas se conservan" 12px `#8497BC`.

### 10. Historial (2j)
Sobretítulo "28 PARTIDAS TERMINADAS" `#2FD3F0`, título "HISTORIAL" Cinzel 700 29px uppercase. Filtros en fila: chips radius 999 padding 7/14 13px — activo fondo `#2FD3F0` texto `#0B1730` 700, resto borde `1px solid rgba(255,255,255,0.14)` texto `#B9C8E8` ("Todas", "Truco", "Chinchón", "Magic"). Label de grupo "ESTA SEMANA". Tarjetas: cuadro 40×40 radius 12 del color del juego con glifo Cinzel 700 19px (♠ ♣ M ♦), juego 15px 600 + momento 11.5px `#6C7EA3`, detalle 12.5px `#8497BC` con elipsis, y a la derecha "GANÓ" 9.5px letter-spacing 0.14em sobre el nombre 14px 600 `#55E6A5`.

## Interactions & Behavior

- **Navegación**: menú → (genérico) armar partida → tabla; menú → (específico) marcador propio. Tabs inferiores entre Juegos / Historial / Jugadores. "‹" y "✕" vuelven a la pantalla anterior. La tarjeta "En curso" abre la última partida sin pasar por el armado.
- **Partida genérica**: al guardar una mano, se suman los puntos de cada jugador al total, se agrega una fila y avanza el contador de mano. Si hay "manos fijas" y se alcanzó el número, la partida se cierra y pasa al historial. Si alguien alcanza el puntaje objetivo, también se cierra: gana el menor o el mayor según la regla elegida. Empates: mostrar ambos como ganadores.
- **Hoja de carga**: se abre desde un FAB/acción "Cargar mano"; teclado propio (1, 2, 3, 5, ⌫) más entrada libre; una columna por jugador con foco secuencial; "Cancelar" descarta, "Guardar mano" confirma. Debe poder editarse una mano ya cargada tocando su fila.
- **Truco**: +1/+2/+3 por bando; los puntos llenan primero malas (hasta 15) y luego buenas; al llegar a 30 se gana el partido y se suma al marcador de partidos. "− Quitar" resta 1; "Deshacer" revierte la última acción; "Nuevo partido" resetea porotos manteniendo el marcador de partidos.
- **Magic**: ± sobre vidas con repetición al mantener presionado (recomendado: incremento acelerado); contadores de veneno y energía; "＋ contador" agrega tipos; a 0 vidas (o 10 de veneno) el jugador queda "ELIMINADO" — el cuadrante se apaga pero sigue visible. "Sortear" elige quién empieza; "Reiniciar" vuelve a vidas iniciales (20 en 1v1, 40 en commander). La mitad/los cuadrantes superiores están rotados 180°.
- **Jugadores**: agregar, editar (nombre, color, "frecuente"), eliminar con confirmación; borrar un jugador conserva sus partidas en el historial. Los jugadores frecuentes aparecen primero al armar partida.
- **Configuraciones guardadas**: al activar "Guardar esta configuración" se persiste jugadores + reglas con un nombre; desde el menú se inicia una partida nueva con esa configuración en un toque.
- **Táctil**: ningún control por debajo de 44px de alto efectivo; los botones ± de Magic son de 58px.
- **Persistencia**: todo es local al dispositivo (no hay cuentas ni sincronización). La partida en curso debe sobrevivir el cierre de la app.
- **Feedback**: sin animaciones elaboradas. Transiciones de pantalla nativas; la hoja de carga entra desde abajo (~200ms, ease-out); pulsación con leve oscurecimiento/escala.

## State Management

- `users[]`: { id, name, color, favorite, gamesPlayed } — jugadores locales.
- `savedConfigs[]`: { id, name, gameType, playerIds[], targetScore, lowWins, fixedHands|null, bidsEnabled }.
- `currentGame`: { id, gameType, playerIds[], rules, rounds[] } donde `rounds[] = { index, cells: [{ playerId, points, bid|null }] }`; derivados: totales por jugador, líder, mano actual, terminada/no.
- `trucoMatch`: { us: { points, gamesWon }, them: { points, gamesWon }, history[] } para deshacer.
- `magicGame`: { mode: '1v1' | 'commander', players: [{ playerId, life, poison, energy, commanderDamage, eliminated }], startingLife }.
- `history[]`: partidas terminadas { id, gameType, playerIds[], winnerId(s), summary, finishedAt }.
- Sin data fetching: todo se lee y escribe en almacenamiento local del dispositivo.

## Design Tokens

**Colores — base**
- Fondo app: `#0C1730`; degradado de menú `radial-gradient(130% 70% at 50% -10%, #1B3768 0%, #0C1730 58%, #0A1327 100%)`
- Fondo profundo / barras: `#0A1327`; superficie elevada (bottom sheet): `#142547`
- Superficie de tarjeta: `rgba(255,255,255,0.045)`; fila secundaria: `rgba(255,255,255,0.035)`; relleno de control: `rgba(255,255,255,0.07)`
- Bordes: `rgba(255,255,255,0.10)` (tarjeta), `rgba(255,255,255,0.07)` (fila), `rgba(255,255,255,0.16)` (botón secundario), `rgba(255,255,255,0.09)` (divisor)
- Bisel: `inset 0 1px 0 rgba(255,255,255,0.10)`

**Colores — texto**
- Primario `#EAF1FF`; secundario `#B9C8E8`; terciario `#8497BC`; apagado `#6C7EA3`; muy apagado `#556792`; sobre acento `#0B1730` / `#04121C`

**Colores — acentos (tomados del logo)**
- Cian primario `#2FD3F0`; degradado de CTA `linear-gradient(180deg, #6BE4FA, #2FD3F0 55%, #14A8CC)`; glow `0 0 24px rgba(47,211,240,0.35)`; cian claro (números) `#CFF6FF`
- Magenta `#E24BD6` (bando "Ellos", acentos calientes) y `#FFD9F6` para sus números
- Menta `#55E6A5` (líder, ganador, veneno)
- Colores de jugador: `#2FD3F0`, `#A18AF5`, `#55E6A5`, `#FF6FA8`, `#3B7BF7`, `#F27BA9`
- Colores de juego: Magic `#A18AF5`, Pokémon `#55E6A5`, Yu-Gi-Oh! `#FF6FA8`, Digimon `#3B7BF7`, Truco/genéricos `#2FD3F0`

**Tipografía**
- Títulos, nombre de la app y monogramas: **Cinzel** 700/900, uppercase, letter-spacing 0.03–0.14em. Escala: 29px (título de pantalla), 23px (título de tarjeta), 20px (título de header), 16/14px (CTA).
- Puntajes y números: **Orbitron** 700/800, `font-variant-numeric: tabular-nums`. Escala: 88px (vida 1v1), 62px (commander / porotos), 25px (totales de tabla), 20px (stepper), 19px (celda de carga).
- Interfaz y copy: **Space Grotesk** 400/500/600/700. Escala: 15.5px (título de fila), 14px (control), 13px (chip), 12.5px (subtítulo), 11.5px (dato), 10px (label de sección, letter-spacing 0.2em, uppercase).

**Espaciado**: escala 4 / 6 / 8 / 10 / 12 / 14 / 16 / 18 / 20 / 22 / 24 px. Padding lateral de pantalla 20px (16px en la tabla). Gap entre filas 8px, entre secciones 18–24px.

**Radios**: 999 (pastillas, avatares), 20 (tarjeta destacada / tile), 16 (tarjeta), 13/12/11 (íconos y teclas), 10 (casilla). Chaflán de CTA: `clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%, 0 14px)` (12px en el CTA chico).

**Sombras**: bottom sheet `0 -18px 40px rgba(0,0,0,0.45)`; glow de CTA cian `0 0 24px rgba(47,211,240,0.35)`.

**Geometría de pantalla**: 412×892 con status bar de 40 y barra de gestos de 24 → 828 útiles (813 medidos en el prototipo).

## Assets

- `logo.png` (1993×789, PNG con transparencia) — logotipo "Epic Hypernova Score Tracker: Omega Protocol" provisto por el usuario. Se usa como título en ambos menús (254px y 196px de ancho). Conviene exportar versiones @1x/@2x/@3x y una variante recortada solo con "HYPERNOVA" para headers chicos.
- Íconos: en el prototipo son glifos Unicode (♠ ♣ ♦ ＋ ✕ ✎ ⚙ ⌕ ‹ › ⋮ ☠ ⚡ ⚔ ⟳ ⚄ ◷ ☺). En producción reemplazar por el set de íconos del codebase; los símbolos de palo pueden quedar como tipografía.
- Fuentes: Cinzel, Orbitron y Space Grotesk (Google Fonts, licencia OFL) — empaquetarlas en la app.

## Files

- `Points Scorer v2.dc.html` — diseño vigente y única referencia visual: las 10 pantallas descritas arriba, en un canvas horizontal con badges 2a…2j. Cada pantalla vive dentro de un marco Android de 412×892.
- `Points Scorer.dc.html` — primera versión (paleta clara papel/terracota), **obsoleta**; sirve solo para ver la evolución del layout.
- `android-frame.jsx` — marco de dispositivo usado por los prototipos (status bar, barra de gestos, teclado). No es parte del producto.
- `logo.png` — logotipo.
- Datos de ejemplo y lógica de derivación (totales, líder, porcentaje de barra) están en la clase `Component` al final de `Points Scorer v2.dc.html`.

## Pendiente / decisiones abiertas

- Pokémon TCG, Yu-Gi-Oh! y Digimon TCG figuran en el menú pero **no tienen marcador diseñado** todavía.
- La interfaz está en español; se pidió soporte español/inglés — falta la pantalla de ajustes con el selector de idioma (y traducir las cadenas).
- No hay pantalla de ajustes ni de fin de partida (resumen/ganador) diseñadas.
