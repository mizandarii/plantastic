[![English](https://img.shields.io/badge/English-EN-blue)](README.md)
[![Estonia](https://img.shields.io/badge/Eesti-ET-green)](README.et.md)

# 🌿 Plantastic

## Sinu isiklik taimede hooldusassistent

![Plantastic pealkiri](https://github.com/user-attachments/assets/eee07940-7fe3-458b-89a5-156a44e7bff1)

Plantastic on terviklik Android-i mobiilirakendus, mis aitab kasutajatel hallata oma toataimi vaevata. Olenemata sellest, kas oled alles alustanud oma rohelist reisi või sul on juba rohkem kogemusi, pakub Plantastic intelligentseid meeldetuletusi, üksikasjalikku hoolduse teavet ja võrguühenduseta juurdepääsu, et hoida teie taimed elujõul.

---

## ✨ Funktsioonid

### Peamised funktsioonid
- **🪴 Minu taimede teek**
  - Lisa ja halda oma isiklikku taimekollektsiooni
  - Jälgi taimedelt liike, asukohta ja tervist
  - Salvesta taimekohtased hoolduse juhised
  - Monitoori kastmise sagedust ja graafikut

- **🔔 Nutikad kastmise meeldetuletused**
  - Intelligentsed push-teatised kastmise graafiku kohta
  - Kohandatavad meeldetuletuse ajad ja intervallid
  - Edasi lükkamise funktsioon paindlikule ajaplaneeringule
  - Ühe kliki tõugetus kui taimed on kastutud

- **📚 Taimedega seotud entsüklopeedia**
  - Populaarsete tupetaimede andmebaas
  - Üksikasjalik hoolduse teave, sealhulgas:
    - Kastmissagedus ja -maht
    - Valgusvajadused (nõrk, keskmine, hele, otsene)
    - Niiskuse eelistused
    - Temperatuurivahemikud
    - Väetiste graafikud
    - Tavaline küsimused ja lahendused

- **📋 Hoolduse ajalugu ja logimine**
  - Jälgi kõiki kastmise ja hoolduse tegevusi
  - Vaata iga taime ajaloolist hoolduse andmeid
  - Jälgi taimete tervisega seotud trende aja jooksul

- **📱 Võrguühenduseta-esmane arhitektuur**
  - Täielik funktsioon ilma internetiühenduseta
  - Kohalik andmete sünkroonimine
  - Sujuv ülekanne võrguühenduseta muudelt

---

## 🛠️ Tehniline kihistus

### Arhitektuur ja tuumik
- **Keel:** Java
- **API tase:** 24 - 36 (Android 7.0 ja uuem)
- **Ehitussüsteem:** Gradle versioon kataloogiga

### Teegid ja sõltuvused
- **UI raamistik:** Android AppCompat, Material Design 3
- **Andmete püsivus:** Room Database (v2.8.4)
- **Võrgundus:** Retrofit2 (v2.9.0) koos Gson muunduriga
- **Kujutiste laadimine:** Glide (v4.16.0)
- **Taustülesanded:** WorkManager (v2.8.1)
- **UI komponendid:** RecyclerView, ConstraintLayout
- **Testimine:** JUnit, Espresso

### Välised API-d
- **Perenual API:** Taimete andmebaas ja teave

---

## 📋 Nõuded

### Süsteemise nõuded
- **Minimaalne Android versioon:** Android 7.0 (API tase 24)
- **Sihtversioon Android:** Android 15 (API tase 36)
- **RAM:** Minimaalne 2GB
- **Salvestusruum:** 100MB vaba ruumi

### Arenduse nõuded
- Android Studio (uusim versioon on soovitatav)
- JDK 11 või uuem
- Gradle 8.x
- Android SDK 36

---

## 🚀 Paigaldamine ja seadistus

### Eeltingimused
1. Kloonige hoidla
   ```bash
   git clone https://github.com/yourusername/plantastic.git
   cd Plantastic
   ```

2. Installige Android Studio ja nõutavad SDK-d

### Seadistus

#### 1. API-võtme seadistus
Rakendus nõuab Perenual API-võtit taimete andmebaasile:

1. Hangi oma API-võti aadressilt [Perenual](https://perenual.com/api)
2. Loo või redigeeri `local.properties` projekti juurkaustast:
   ```properties
   PERENUAL_API_KEY=your_api_key_here
   ```

#### 2. Ehitus konfiguratsioon
1. Veenduge, et `gradle/libs.versions.toml` on õigesti konfigureeritud
2. Sünkroonige Gradle Android Studios: `File` → `Sync Now`

### Ehitamine

#### Silumise versioon
```bash
./gradlew assembleDebug
```

#### Väljalaskmise versioon
```bash
./gradlew assembleRelease
```

#### Käivita seadmel/emulaatoril
```bash
./gradlew installDebug
```

---

## 📖 Kasutamine

### Esimese taime lisamine

1. Avage rakendus ja navigeerige "Minu taimed" menüüsse
2. Puudutage nuppu **+** uue taime lisamiseks
3. Valige taimedelt liik entsüklopeedist
4. Määrake oma eelistatud kastmissagedus
5. Asetage oma taime asukoht
6. Kinnitage ja alustage meeldetuletuste saamist

### Kastmise meeldetuletuste seadistamine

1. Minge oma taime üksikasjade vaatusse
2. Puudutage "Redigeeri" või kastmise graafiku valikut
3. Valige eelistatud meeldetuletuse sagedus
4. Valige teatise aeg
5. Lubake nutikaid meeldetuletusi

### Taimede entsüklopeedias kasutamine

1. Navigeerige sakile "Taimede entsüklopeedia"
2. Sirvige või otsige taimedelt liike
3. Vaata terviklikku hoolduse teavet
4. Kontrollige valguse, kastmise ja temperatuuri vajadusi
5. Salvesta lemmikud taimed oma kollektsiooni

### Teatiste haldamine

- **Edasi lükka:** Puudutage edasi lükkamise nuppu meeldetuletus edasi lükkamiseks 1 tunni võrra
- **Märgi kastuna:** Kinnitage kastmine teatise ajaanduri lähtestamiseks
- **Kohanda:** Seaded → Teatised kohtade teada asukohtade muutmiseks

---

## 🏗️ Projekti struktuur

```
Plantastic/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/plantastic/
│   │   │   │   ├── MainActivity
│   │   │   │   ├── adapters/
│   │   │   │   ├── database/
│   │   │   │   ├── models/
│   │   │   │   ├── notifications/
│   │   │   │   ├── api/
│   │   │   │   └── utils/
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   └── values/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle
├── gradle/libs.versions.toml
├── build.gradle
├── settings.gradle
└── README.et.md

```

---

## 🔐 Load

Rakendus taotleb järgmisi Android-i lube:

- **INTERNET:** API-kutsete jaoks taimete andmete hankimiseks
- **POST_NOTIFICATIONS:** Kastmise meeldetuletusteks (Android 13+)

---

## 🧪 Testimine

### Avaldamise testid
```bash
./gradlew test
```

### Instrumentatsiooni testid
```bash
./gradlew connectedAndroidTest
```

---

## 🐛 Teadaolevad probleemid ja piirangud

- Võrguühenduseta režiim sünkroonimine, kui ühendus on taastatud
- Taimete entsüklopeedia nõuab esialgset võrguühendust
- Mõned täiustatud funktsioonid nõivad API-lepingut

---

## 🤝 Panustamine

Panustamine on teretulnud! Panustamiseks:

1. Fork hoidla
2. Loo funktsiooni haru (`git checkout -b feature/amazing-feature`)
3. Tegage kohustusega oma muudatused (`git commit -m 'Add amazing feature'`)
4. Tõuge harule (`git push origin feature/amazing-feature`)
5. Avage Pull Request

### Koodi standardid
- Järgige Android-i parimaid praktikaid
- Kasutage tähenduslikke muutuja ja meetodi nimesid
- Kirjutage kommentaarid keerulise loogika jaoks
- Testige oma muudatusi hoolikalt

---

## 📄 Litsents

See projekt on litsenseeritud MIT litsentsi alusel - üksikasjaid saate vaadata LICENSE failist.

---

## 📞 Tugi ja kontakt

- **Probleemid:** Teatage vigadest GitHub-is küsimus

Avades
- **Arutlused:** Liituge kogukonna aruteluga funktsioonide teavituste jaoks
- **E-post:** [teie-e-posti@näide.com]

---

## 🎯 Teekond

### Kavandatud funktsioonid
- [ ] Taimete tervisepunktsus ja analüütika
- [ ] Kommuuni foorum taimede näpunäidete jaoks
- [ ] Väetiste ja kahjurite juhtimise jälgimine
- [ ] Mitme kasutaja kodumajapidamise tugi
- [ ] Fotogalerii taimete kasvule
- [ ] Ühiskondliku jagamise taimete saavutuste jaoks
- [ ] AI-toestatud taimete haiguse avastamine

---

## 📚 Ressursid

- [Android dokumentatsioon](https://developer.android.com/)
- [Materjalse konstruktsioon](https://material.io/design)
- [Perenual API dokumentatsioon](https://perenual.com/docs/api)
- [Ruumi andmebaasi juhend](https://developer.android.com/training/data-storage/room)

---

## 🌟 Tunnustused

- Pealkiri pealt Freddie Marriage Unsplash
- Perenual taimedega seotud andmebaasi jaoks
- Android-i kogukonnale suurepärase teegi eest

Tehtud koos 🌱 taimede armastajatele üle kogu maailma.
