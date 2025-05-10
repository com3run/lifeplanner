Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
# 📆 LifePlanner

**LifePlanner** is a simple, elegant, and fully open-source life planning and goal-tracking app built using Kotlin Multiplatform with Jetpack Compose.

![screenshot](preview.png)

---

## 🚀 Features

- 📝 Create, edit, and delete goals
- 🧩 Organize goals by category (e.g., Health, Career)
- ⏳ Timeline filtering (Short, Mid, Long Term)
- 🔁 Swipe-to-reveal delete with confirmation
- ⛔ Undo delete with Snackbar
- 💡 Empty state prompts
- 📱 Runs on Android and iOS

---

## 📲 Download

| Platform | Link |
|---------|------|
| Android | [Google Play – coming soon](#) |
| iOS     | [App Store – coming soon](#) |

---

## 🧠 Tech Stack

- **Language:** Kotlin Multiplatform
- **UI:** Jetpack Compose Multiplatform
- **DB:** SQLDelight
- **DI:** Koin
- **Architecture:** Clean Architecture (DDD style)

---

## 🛠 How to Run

```bash
git clone https://github.com/kamranmammadov/lifeplanner.git
cd lifeplanner
./gradlew build
```

To run on Android:

```bash
./gradlew :androidApp:installDebug
```

---

## 📂 Project Structure

```
/composeApp        → Shared UI & logic
/iosApp            → iOS app entry point (SwiftUI if needed)
/shared            → Domain, use cases, SQLDelight, DI
```

---

## 🧑‍💻 Contributing

We love contributions!

1. Fork the repo
2. Create a feature branch
3. Commit your changes
4. Open a pull request 🚀

See [CONTRIBUTING.md](CONTRIBUTING.md) for full details.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

Developed by [Kamran Mammadov](https://github.com/kamranmammadov)