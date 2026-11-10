# 🐄 Gai Khamar (গাই খামার) — Dairy & Cattle Farm Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![API](https://img.shields.io/badge/API-24%2B%20(Android%207.0%2B)-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-32%20%2F%20Compile%2033-orange.svg?style=flat)](https://developer.android.com/about/versions/13)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20RTDB-FFCA28.svg?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Room](https://img.shields.io/badge/Room%20DB-Version%205-4285F4.svg?style=flat&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Language](https://img.shields.io/badge/Language-Java-ED8B00.svg?style=flat&logo=java&logoColor=white)](https://www.java.com/)
[![Localization](https://img.shields.io/badge/Localization-English%20%7C%20বাংলা-blue.svg?style=flat)]()

**Gai Khamar (গাই খামার)** is a comprehensive, offline-first native Android application designed for commercial dairy and beef cattle farms in Bangladesh and South Asia. The app combines offline SQLite/Room data resilience with cloud-synchronized operations, live collaborative boards, hardware biometric authentication, native camera picture storage, external REST API weather analytics, and core Android system integrations.

---

## 📑 Feature & Architecture Matrix (Direct Code Links)

Every feature in the application maps directly to its corresponding Java class and layout resource file:

### 1. 🏛️ Architecture & Navigation

| Feature | Implementation Description | Java Class Link | XML Layout Link |
| :--- | :--- | :--- | :--- |
| **Multi-Activity Routing** | 22 dedicated modular activities managing lifecycle and transitions. | [`MainActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/MainActivity.java)<br>[`MoreActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/MoreActivity.java) | [`activity_main.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_main.xml)<br>[`activity_more.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_more.xml) |
| **Manifest Declarations** | Complete activity, service, provider, receiver, and permission registry. | — | [`AndroidManifest.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/AndroidManifest.xml) |
| **Explicit Intents** | Type-safe internal navigation with extras (`EXTRA_ID`, `EXTRA_KIND`). | [`MoreActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/MoreActivity.java#L95)<br>[`AnimalDetailActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/AnimalDetailActivity.java#L386) | [`activity_animal_detail.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_animal_detail.xml) |
| **Implicit Intents (Dialer & SMS)** | Dialing veterinary doctors (`ACTION_DIAL`) and sending SMS messages (`ACTION_SENDTO`). | [`ContactsActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/ContactsActivity.java#L101-L103) | [`activity_list.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_list.xml) |
| **Implicit Intents (Sharing & Settings)** | Exporting PDF/CSV reports via `ACTION_SEND` and opening app settings via `ACTION_APPLICATION_DETAILS_SETTINGS`. | [`ShareHelper.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/ShareHelper.java#L17)<br>[`ContactPickerActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/ContactPickerActivity.java#L41) | [`file_paths.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/xml/file_paths.xml) |
| **View Binding** | Compile-time null-safe and type-safe view references across the app. | [`MainActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/MainActivity.java#L53)<br>[`AnimalPhotoActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/AnimalPhotoActivity.java#L54) | [`build.gradle`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/build.gradle#L29) |
| **Event Handling & Forms** | Dynamic form validation, password toggles, text watchers, and Activity Result contracts. | [`FormDialog.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/FormDialog.java#L91-L142)<br>[`WorkersActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/WorkersActivity.java#L125) | [`item_row.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/item_row.xml) |

---

### 2. 🎨 UI & Lists

| Feature | Implementation Description | Java Class Link | XML Layout Link |
| :--- | :--- | :--- | :--- |
| **Layout Diversity** | Optimized combination of RelativeLayout, LinearLayout, ScrollView, and GridLayout. | — | [`activity_list.xml` (RelativeLayout)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_list.xml)<br>[`activity_main.xml` (GridLayout)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_main.xml#L208)<br>[`activity_animal_detail.xml` (ScrollView)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_animal_detail.xml) |
| **Generic List Adapter** | High-performance reusable `RowAdapter` with `DiffUtil.ItemCallback` and static `ViewHolder`. | [`RowAdapter.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/RowAdapter.java)<br>[`Row.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/Row.java) | [`item_row.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/item_row.xml) |
| **Entity-Specific Lists** | Custom adapters for cattle cards, milk yields, feed distribution, and batches. | [`AnimalAdapter.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/AnimalAdapter.java)<br>[`MilkAdapter.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/MilkAdapter.java)<br>[`FeedLogAdapter.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/FeedLogAdapter.java) | [`item_animal_beef.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/item_animal_beef.xml)<br>[`item_animal_dairy.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/item_animal_dairy.xml)<br>[`item_milk_record.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/item_milk_record.xml) |
| **Loans & Installments** | Tracks loans, balances, due dates, payments, and expenses. | [`LoansActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/LoansActivity.java) | [`activity_list.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_list.xml) |
| **Workers & Attendance** | Staff directory, daily attendance recording, and wage calculations. | [`WorkersActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/WorkersActivity.java) | [`activity_list.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_list.xml) |

---

### 3. ⚙️ Android Core Components

| Feature | Implementation Description | Java Class Link | XML / Manifest Link |
| :--- | :--- | :--- | :--- |
| **System Broadcast Receiver** | Dynamic broadcast receiver listening to `ACTION_AIRPLANE_MODE_CHANGED`, `ACTION_BATTERY_LOW`, and `ACTION_BATTERY_OKAY`. | [`SystemEventsReceiver.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/system/SystemEventsReceiver.java)<br>[`MainActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/MainActivity.java#L76-L80) | [`activity_main.xml` (`tvSystemBanner`)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_main.xml#L56) |
| **Boot Receiver** | Reschedules daily farm alerts when the device completes rebooting (`ACTION_BOOT_COMPLETED`). | [`BootReceiver.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/system/BootReceiver.java) | [`AndroidManifest.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/AndroidManifest.xml#L60) |
| **Custom Content Provider** | Exposes farm summary and animal records to outside systems via URIs (`content://com.gaikhamar.app.provider/`). | [`FarmProvider.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/FarmProvider.java) | [`AndroidManifest.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/AndroidManifest.xml#L59) |
| **Device Contacts Access** | Queries Android system's `ContactsContract.CommonDataKinds.Phone` using `ContentResolver`. | [`ContactPickerActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/ContactPickerActivity.java#L95-L125) | [`AndroidManifest.xml` (`READ_CONTACTS`)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/AndroidManifest.xml#L9) |
| **Foreground Service** | Persistent foreground service updating active status bar tray notifications with pending farm alerts. | [`FarmMonitorService.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/system/FarmMonitorService.java)<br>[`NotificationHelper.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/system/NotificationHelper.java) | [`AndroidManifest.xml` (`FOREGROUND_SERVICE`)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/AndroidManifest.xml#L7) |
| **Background Alarms** | Daily scheduled reminders via `AlarmManager` wakeups and local notifications. | [`AlarmScheduler.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/system/AlarmScheduler.java)<br>[`ReminderReceiver.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/system/ReminderReceiver.java) | [`AndroidManifest.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/AndroidManifest.xml#L57) |
| **Runtime Permissions** | Granular permission handling for Android 13+ Notifications (`POST_NOTIFICATIONS`), Camera, and Contacts. | [`MainActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/MainActivity.java#L41)<br>[`AnimalPhotoActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/AnimalPhotoActivity.java#L47) | [`AndroidManifest.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/AndroidManifest.xml#L5-L11) |

---

### 4. 🌐 Networking, APIs & Firebase Integration

| Feature | Implementation Description | Java Class Link | Config / Layout Link |
| :--- | :--- | :--- | :--- |
| **REST API & HTTP Client** | Native `HttpURLConnection` making asynchronous GET requests to Open-Meteo REST API with timeout policies. | [`WeatherHelper.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/WeatherHelper.java#L35-L84) | [`activity_main.xml` (`tvWeather`)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_main.xml#L76) |
| **JSON Parsing & THI Heat Stress** | Parses nested JSON response (`current_weather`, `relativehumidity_2m`, `precipitation`) and calculates dairy heat stress index. | [`WeatherHelper.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/WeatherHelper.java#L59-L96) | [`strings.xml` (Heat stress strings)](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/values/strings.xml#L427-L432) |
| **Firebase Authentication** | Phone-backed email authentication (`<phone>@gaikhamar.app`) managing silent cloud sign-in and account creation. | [`CloudAuth.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/cloud/CloudAuth.java)<br>[`LoginActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/LoginActivity.java#L46) | [`google-services.json`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/google-services.json) |
| **Firebase Cloud Firestore** | Two-way cloud backup and full disaster recovery of all 18 database tables with batched commits. | [`CloudSync.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/cloud/CloudSync.java#L64-L200)<br>[`NetworkMonitor.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/cloud/NetworkMonitor.java) | [`activity_more.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_more.xml) |
| **Firebase Realtime Database** | Live shared collaborative boards (`farms/{uid}/prices` & `tasks`) syncing instant socket updates across devices. | [`BoardActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/BoardActivity.java#L55-L87) | [`activity_list.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_list.xml) |
| **Camera & Media Storage** | Hardware camera capture via `ActivityResultContracts.TakePicture()`, private file persistence, and secure content URI resolution. | [`AnimalPhotoActivity.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/ui/AnimalPhotoActivity.java)<br>[`PhotoHelper.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/PhotoHelper.java) | [`activity_animal_photo.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_animal_photo.xml)<br>[`file_paths.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/xml/file_paths.xml) |
| **Biometric Security** | Hardware fingerprint and biometric authentication prompt (`BiometricPrompt`). | [`BiometricHelper.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/BiometricHelper.java)<br>[`AppPrefs.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/AppPrefs.java#L16) | [`activity_more.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/layout/activity_more.xml) |

---

### 5. 📱 Layout Responsiveness & Localization

| Feature | Implementation Description | Configuration / Resource Links |
| :--- | :--- | :--- |
| **Orientation Qualifiers** | Adapts grid columns between portrait (2 columns) and landscape (4 columns). | Default: [`values/integers.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/values/integers.xml)<br>Landscape: [`values-land/integers.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/values-land/integers.xml) |
| **Tablet Support (sw600dp)** | Adaptive UI scaling for 7-inch portrait and 10-inch landscape tablets. | 7-inch Tablet: [`values-sw600dp/integers.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/values-sw600dp/integers.xml)<br>10-inch Landscape: [`values-sw600dp-land/integers.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/values-sw600dp-land/integers.xml) |
| **Full Dual Localization** | Dynamic language switching between English and Bengali (`bn-rBD`) with custom context wrapper. | English: [`values/strings.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/values/strings.xml)<br>Bengali: [`values-bn-rBD/strings.xml`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/res/values-bn-rBD/strings.xml)<br>Engine: [`LocaleHelper.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/util/LocaleHelper.java) |

---

## 🗄️ Database Architecture (Room Version 5)

The app stores data locally using Android Room with 18 relational entities, asynchronous DAOs, and automated destructive fallback migrations:

* **Entities:** [`User`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/User.java), [`Animal`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/Animal.java), [`Batch`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/Batch.java), [`MilkRecord`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/MilkRecord.java), [`FeedType`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/FeedType.java), [`FeedLog`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/FeedLog.java), [`Vaccination`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/Vaccination.java), [`HealthRecord`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/HealthRecord.java), [`FinanceRecord`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/FinanceRecord.java), [`Customer`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/Customer.java), [`LedgerEntry`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/LedgerEntry.java), [`Loan`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/Loan.java), [`Worker`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/Worker.java), [`Attendance`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/entity/Attendance.java).
* **Database Class:** [`AppDatabase.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/AppDatabase.java)
* **Repositories:** [`Repository.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/Repository.java), [`ExtraRepository.java`](https://github.com/ziggyzu/-Gai-Khamar-/blob/master/app/src/main/java/com/gaikhamar/app/data/ExtraRepository.java)

---

## 🛠️ Build & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ziggyzu/-Gai-Khamar-.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio $\rightarrow$ **File > Open** $\rightarrow$ select the `-Gai-Khamar-` folder.
   - Let Gradle sync dependencies.
3. **Run on an Emulator or Device:**
   - Select an emulator or physical device running **Android 7.0 (API 24) or higher** (recommended: API 33).
   - Click the green **Run (▶)** button or press `Shift + F10`.

---

## 📄 License
This project is developed for educational and farm management automation purposes.
