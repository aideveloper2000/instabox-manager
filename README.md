# Instabox Manager - Android App

Android Java application to manage **Instabox** machines. List all machines, view live status, and perform all possible actions (start, stop, pause, restart, execute commands, logs, metrics, terminal, VNC, SSH) with a beautiful Material Design UI.

| | |
|---|---|
| **Language** | Java |
| **Min SDK** | 21 (Android 5.0) |
| **Target SDK** | 34 (Android 14) |
| **Compile SDK** | 34 |
| **Dependencies** | Retrofit 2.9, Gson, OkHttp 4.12, Material Components, RecyclerView |

## Features

### 🔑 API Key Management
- Add multiple Instabox API keys
- Keys are stored securely in Android SharedPreferences
- Switch between keys with one tap

### 📋 Machine List
- Fetch and display all machines from Instabox API
- Real-time status indicators with color coding:
  - 🟢 Running
  - 🔴 Stopped
  - 🟠 Paused/Suspended
  - 🟣 Creating
  - ⚫ Deleting
  - ⭐ Error

### ⚙️ All Machine Actions
- **Start** machine
- **Stop** machine
- **Restart** machine
- **Pause** / **Suspend** machine
- **Resume** machine
- **Wake** machine
- **Delete** machine
- **Fork** machine (clone)
- **Create Snapshot**
- **Restore Snapshot**
- **Run Command** (exec)
- **View Logs**
- **View Metrics**
- **Terminal** (SSH)
- **VNC** access

### 🎨 Material 3 UI
- Material Design 3 components, color roles, and type scale
- Light and dark themes
- Inline sync status in the API key and Machines widgets (no toasts)
- Status color indicators
- Card-based layouts with 48dp touch targets
- Snackbars for transient feedback

## Project Structure

```
instabox-manager/
├── app/
│   ├── src/main/
│   │   ├── java/com/instabox/manager/
│   │   │   ├── MainActivity.java           # API key management UI
│   │   │   ├── MachineListActivity.java    # Machine list with sync
│   │   │   ├── MachineDetailActivity.java  # Machine details + actions
│   │   │   ├── ApiService.java             # Retrofit API interface
│   │   │   ├── ApiClient.java              # OkHttp/Retrofit client
│   │   │   ├── Machine.java                # Machine model
│   │   │   ├── MachineAdapter.java         # RecyclerView adapter
│   │   │   ├── ApiKey.java                 # API key model
│   │   │   └── ApiKeyAdapter.java          # API key list adapter
│   │   ├── res/layout/
│   │   │   ├── activity_main.xml           # Main screen layout
│   │   │   ├── activity_machine_list.xml   # Machine list screen
│   │   │   ├── activity_machine_detail.xml # Machine detail screen
│   │   │   ├── item_machine.xml            # Machine list item
│   │   │   └── item_api_key.xml            # API key list item
│   │   ├── res/values/
│   │   │   ├── strings.xml                 # UI strings
│   │   │   ├── colors.xml                  # Colors (incl. status colors)
│   │   │   └── themes.xml                  # App theme
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle                            # Root build file
├── settings.gradle                           # Settings
├── gradle.properties                         # Gradle properties
├── gradlew / gradlew.bat                   # Gradle wrapper
└── README.md                               # This file
```

## Setup

### Prerequisites
- Android Studio (Arcticis or newer)
- JDK 17 or later
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0

### Import into Android Studio

1. **Clone the repository:**
   ```bash
   git clone https://github.com/aideveloper2000/instabox-manager.git
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select **File → Open**
   - Navigate to the cloned directory and open it

3. **Wait for Gradle sync** to complete (downloads dependencies)

4. **Build & Run:**
   - Select a device/emulator
   - Click the **Run** button (▶)

### Manual Build

```bash
cd instabox-manager
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

## Usage

1. **Add API Keys:**
   - Tap "Add API Key"
   - Enter your Instabox API key (starts with `ibk_`) and a name
   - Keys persist across app restarts

2. **Manage Machines:**
   - Tap the refresh icon to fetch all machines
   - View machines with live status indicators

3. **Manage a Machine:**
   - Tap any machine to view details
   - Use the action buttons (Start, Stop, Pause, etc.)
   - Execute commands via the command input

## API Endpoints Used

The app uses the following Instabox API endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/machines` | GET | List all machines |
| `/v1/machines/{id}` | GET | Get machine details |
| `/v1/machines/{id}/start` | POST | Start machine |
| `/v1/machines/{id}/stop` | POST | Stop machine |
| `/v1/machines/{id}/suspend` | POST | Pause machine |
| `/v1/machines/{id}/resume` | POST | Resume machine |
| `/v1/machines/{id}/wake` | POST | Wake machine |
| `/v1/machines/{id}/fork` | POST | Fork machine |
| `/v1/machines/{id}/exec` | POST | Execute command |
| `/v1/templates` | GET | List templates |
| `/v1/me` | GET | Get current user |
| `/v1/usage/machines` | GET | Get machine usage |

## Architecture

- **Networking:** Retrofit 2 + OkHttp 4 (with logging interceptor)
- **JSON Parsing:** Gson
- **UI Components:** Material Components, RecyclerView, CardView
- **Async Execution:** Android Executors, Retrofit Callbacks

## License

This project is licensed under the MIT License. See `LICENSE` file for details.

## Notes

- API keys are stored in plaintext in Android SharedPreferences. For production, use Android's **Keystore** system.
- The app does not currently support real SSH terminal or VNC connections (UI placeholders for future implementation).
- Command execution is currently a placeholder that displays the command (full implementation depends on Instabox API support).

## Contributing

Feel free to fork and submit pull requests. For major changes, please open an issue first.
