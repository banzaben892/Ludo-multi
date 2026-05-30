# 🎲 Ludo Master Pro — Kotlin Native

> Jeu de Ludo multi-joueurs 100% natif Android  
> **Kotlin · Jetpack Compose · MVVM · Canvas · DataStore**  
> Build APK automatisé via **GitHub Actions — sans keystore, sans secret**

![Build](https://github.com/VOTRE_USERNAME/LudoMasterPro/actions/workflows/build-apk.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-purple?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2024.05-blue)
![minSdk](https://img.shields.io/badge/minSdk-24_(Android_7)-orange)
![License](https://img.shields.io/badge/License-MIT-green)

---

## ✨ Fonctionnalités

| Feature | Détail |
|---------|--------|
| 👥 Joueurs | 2 à 4, Humains & IA librement mixables |
| 🤖 IA | 3 niveaux : 😊 Facile · 🤖 Normal · 🧠 Expert |
| 🎨 Rendu | Canvas Compose natif — 60fps |
| 🎲 Dé animé | Rotation + scale + retour haptique |
| ✨ Pions pulsants | Halo doré animé sur les pions jouables |
| 🎬 Animation | Déplacement case par case fluide |
| 🏆 Podium | Barres animées (Spring) + stats complètes |
| 💾 Records | DataStore Preferences persistants |
| 📐 Responsive | Portrait & Paysage automatique |
| 🌙 Thème | Sombre exclusif (Material 3) |

---

## 🚀 Démarrage immédiat — Zéro configuration

### 1. Forker / cloner le repo

```bash
git clone https://github.com/VOTRE_USERNAME/LudoMasterPro.git
cd LudoMasterPro
```

### 2. Pousser sur GitHub

```bash
git add .
git commit -m "Initial commit"
git push origin main
```

**C'est tout.** Le workflow GitHub Actions se lance automatiquement.

---

## 🔐 Signature sans Keystore

Le workflow utilise une **clé RSA éphémère auto-générée** à chaque build :

```
openssl genrsa         →  Clé RSA 2048 bits (créée dans le runner)
openssl req -x509      →  Certificat auto-signé (10 000 jours)
openssl pkcs12         →  Conversion PKCS#12
zipalign               →  Alignement de l'APK
apksigner sign         →  Signature de l'APK
rm ephemeral.*         →  Suppression immédiate de la clé
```

> **Pourquoi ça marche ?**  
> Android accepte n'importe quelle signature valide pour l'installation en dehors du Play Store.  
> La clé éphémère est suffisante pour **distribuer, tester et installer** l'APK.  
> Elle disparaît à la fin du job — **aucun secret à stocker, aucun fichier keystore**.

> **Limite :** Pour publier sur le **Google Play Store**, il faudrait une clé stable.  
> Pour une distribution directe (APK), la clé éphémère est parfaite.

---

## 📦 Récupérer l'APK

### Après un push sur `main`

1. Aller dans **Actions** du repo GitHub
2. Cliquer sur le dernier workflow `Build Ludo Master Pro APK`
3. Scroll vers le bas → **Artifacts**
4. Télécharger `LudoMasterPro-vX.X.X-buildN.apk`

### Créer une Release officielle

```bash
git tag v1.0.0
git push origin v1.0.0
```

→ GitHub crée automatiquement une **Release** avec l'APK en téléchargement.

---

## 🏗️ Architecture

```
MVVM + Unidirectional Data Flow

UI Layer (Jetpack Compose)
│   MenuScreen · GameScreen · PodiumScreen
│   BoardCanvas · DiceView
│   collectAsStateWithLifecycle()
│
ViewModel Layer
│   GameViewModel (AndroidViewModel + Coroutines)
│   StateFlow<GameState>  ←→  DataStore (records)
│
Domain Layer (Kotlin pur, sans Android)
    LudoEngine.kt
    ├── LudoRules   (computeNewPos, applyMove, aiChoose…)
    ├── BoardConstants (chemin, couloirs, cases sûres…)
    └── Data classes (GameState, Player, Piece, HistoryEntry)
```

---

## 🗂️ Structure des fichiers

```
LudoMasterPro/
├── .github/
│   └── workflows/
│       └── build-apk.yml          ← CI/CD : Lint → Tests → Build → Sign → Release
├── app/
│   ├── build.gradle.kts           ← Gradle sans signingConfig (géré par CI)
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ludomasterpro/
│       │   ├── LudoApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── engine/
│       │   │   ├── LudoEngine.kt        ← Règles, IA, data classes
│       │   │   └── GameViewModel.kt     ← MVVM, StateFlow, DataStore
│       │   └── ui/
│       │       ├── theme/Theme.kt       ← Material3 dark
│       │       ├── components/
│       │       │   ├── BoardCanvas.kt   ← Plateau Canvas natif
│       │       │   └── DiceView.kt      ← Dé animé + haptique
│       │       └── screens/
│       │           ├── MenuScreen.kt
│       │           └── GameAndPodiumScreens.kt
│       └── res/
│           ├── values/  (strings, colors, themes)
│           ├── drawable/ (splash, icône vectorielle)
│           └── mipmap-*/
├── gradle/wrapper/gradle-wrapper.properties  ← Gradle 8.7
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🤖 Intelligence Artificielle

| Niveau | Stratégie |
|--------|-----------|
| 😊 Facile | Choix aléatoire parmi les coups valides |
| 🤖 Normal | Arrivée > Capture > Avancement > Sortie de base |
| 🧠 Expert | Idem + pénalité si la case cible est à portée d'un adversaire |

---

## 🎮 Règles du Ludo

1. **🎲 Lancer le dé** → résultat 1–6
2. **6** = sortir un pion de la base **et** rejouer
3. **♟️ Choisir** un pion surligné en doré pour le déplacer
4. **⭐ Cases étoile** = protection, pas de capture possible
5. **💥 Capture** = atterrir sur un adversaire → il retourne en base
6. **🏆 Victoire** = 4 pions au centre (entrée exacte requise)

---

## 📱 Compatibilité

| Paramètre | Valeur |
|-----------|--------|
| Android min | 7.0 Nougat (API 24) |
| Android cible | 15 (API 35) |
| Architecture | arm64-v8a, armeabi-v7a, x86_64 |
| Taille APK | ~6–8 MB (release minifié) |
| Signature | Clé éphémère RSA 2048 auto-générée |

---

## 📄 Licence

MIT © 2024 — Ludo Master Pro
