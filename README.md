[![English](https://img.shields.io/badge/English-EN-blue)](README.md)
[![Estonia](https://img.shields.io/badge/Eesti-ET-green)](README.et.md)

# 🌿 Plantastic

## Your Personal Plant Care Assistant

![Plantastic Hero](https://github.com/user-attachments/assets/eee07940-7fe3-458b-89a5-156a44e7bff1)

Plantastic is a comprehensive Android mobile application designed to help users manage their houseplants effortlessly. Whether you're a plant parent just starting your green journey or an experienced gardener, Plantastic provides intelligent reminders, detailed care information, and offline access to keep your plants thriving.

---

## ✨ Features

### Core Features
- **🪴 My Plants Library**
  - Add and manage your personal plant collection
  - Track plant species, location, and health status
  - Store plant-specific care instructions
  - Monitor watering frequency and schedule

- **🔔 Smart Watering Reminders**
  - Intelligent push notifications for watering schedules
  - Customizable reminder times and intervals
  - Snooze functionality for flexible scheduling
  - One-tap marking when plants are watered

- **📚 Comprehensive Plant Encyclopedia**
  - Database of popular houseplant species
  - Detailed care information including:
    - Watering frequency and volume
    - Light requirements (low, medium, bright, direct)
    - Humidity preferences
    - Temperature ranges
    - Fertilization schedules
    - Common issues and solutions

- **📋 Care History & Logging**
  - Track all watering and care activities
  - View historical care data for each plant
  - Monitor plant health trends over time

- **📱 Offline-First Architecture**
  - Full functionality without internet connection
  - Local data synchronization
  - Seamless offline-to-online transitions

---

## 🛠️ Technology Stack

### Architecture & Core
- **Language:** Java
- **API Level:** 24 - 36 (Android 7.0 and above)
- **Build System:** Gradle with version catalog

### Libraries & Dependencies
- **UI Framework:** Android AppCompat, Material Design 3
- **Data Persistence:** Room Database (v2.8.4)
- **Networking:** Retrofit2 (v2.9.0) with Gson converter
- **Image Loading:** Glide (v4.16.0)
- **Background Tasks:** WorkManager (v2.8.1)
- **UI Components:** RecyclerView, ConstraintLayout
- **Testing:** JUnit, Espresso

### External APIs
- **Perenual API:** Plant database and information

---

## 📋 Requirements

### System Requirements
- **Minimum Android Version:** Android 7.0 (API Level 24)
- **Target Android Version:** Android 15 (API Level 36)
- **RAM:** 2GB minimum
- **Storage:** 100MB available space

### Development Requirements
- Android Studio (latest version recommended)
- JDK 11 or higher
- Gradle 8.x
- Android SDK 36

---

## 🚀 Installation & Setup

### Prerequisites
1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/plantastic.git
   cd Plantastic
   ```

2. Install Android Studio and required SDKs

### Configuration

#### 1. API Key Setup
The app requires a Perenual API key for the plant database:

1. Get your API key from [Perenual](https://perenual.com/api)
2. Create or edit `local.properties` in the project root:
   ```properties
   PERENUAL_API_KEY=your_api_key_here
   ```

#### 2. Build Configuration
1. Ensure `gradle/libs.versions.toml` is properly configured
2. Sync Gradle in Android Studio: `File` → `Sync Now`

### Building

#### Debug Build
```bash
./gradlew assembleDebug
```

#### Release Build
```bash
./gradlew assembleRelease
```

#### Run on Device/Emulator
```bash
./gradlew installDebug
```

---

## 📖 Usage

### Adding Your First Plant

1. Open the app and navigate to "My Plants"
2. Tap the **+** button to add a new plant
3. Select plant species from the encyclopedia
4. Set your preferred watering frequency
5. Place your plant location
6. Confirm and start receiving reminders

### Setting Watering Reminders

1. Go to your plant's detail view
2. Tap "Edit" or the watering schedule option
3. Choose your preferred reminder frequency
4. Select notification time
5. Enable smart reminders

### Using the Plant Encyclopedia

1. Navigate to the "Plant Encyclopedia" tab
2. Browse or search for plant species
3. View comprehensive care information
4. Check light, watering, and temperature needs
5. Save favorite plants to your collection

### Managing Notifications

- **Snooze:** Tap the snooze button to delay reminder by 1 hour
- **Mark as Watered:** Confirm watering to reset the notification timer
- **Customize:** Settings → Notifications to adjust preferences

---

## 🏗️ Project Structure

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
└── README.md

```

---

## 🔐 Permissions

The app requests the following Android permissions:

- **INTERNET:** Required for API calls to fetch plant data
- **POST_NOTIFICATIONS:** Required for watering reminders (Android 13+)

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

---

## 🐛 Known Issues & Limitations

- Offline mode syncs when connection is restored
- Plant encyclopedia requires initial network connection
- Some advanced features may require API subscription

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards
- Follow Android best practices
- Use meaningful variable and method names
- Write comments for complex logic
- Test your changes thoroughly

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 📞 Support & Contact

- **Issues:** Report bugs by creating an issue on GitHub
- **Discussions:** Join community discussions for feature requests
- **Email:** [your-email@example.com]

---

## 🎯 Roadmap

### Planned Features
- [ ] Plant health scoring and analytics
- [ ] Community forum for plant tips
- [ ] Fertilizer and pest management tracking
- [ ] Multi-user household support
- [ ] Photo gallery for plant growth tracking
- [ ] Social sharing of plant achievements
- [ ] AI-powered plant disease detection

---

## 📚 Resources

- [Android Documentation](https://developer.android.com/)
- [Material Design](https://material.io/design)
- [Perenual API Documentation](https://perenual.com/docs/api)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)

---

## 🌟 Acknowledgments

- Hero image by Freddie Marriage on Unsplash
- Perenual for comprehensive plant database
- Android community for amazing libraries

Made with 🌱 for plant lovers everywhere.
