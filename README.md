# Enterprise Expense Reimbursement Management System 💳

A production-grade Android application designed for modern corporate environments to manage hierarchical expense claim workflows. Built with **Clean Architecture** and **Material 3**, this system simulates a real-world SaaS product like ADP or SAP Concur.

## 🚀 Key Features

### 👥 Role-Based Access Control (RBAC)
*   **Employees**: Submit expense claims with receipt uploads, track reimbursement status in real-time, and view transaction history.
*   **Managers**: Review pending team claims, approve/reject with detailed remarks, and monitor team spending.
*   **Admins**: Access organization-wide financial analytics, budget utilization insights, and export comprehensive **PDF audit reports**.

### 🛠️ Core Capabilities
*   **Real-time Synchronization**: Powered by Firestore Snapshots for instant UI updates.
*   **Receipt Management**: Secure image hosting via Firebase Storage.
*   **Business Analytics**: Dynamic dashboard featuring budget utilization bars and status distribution.
*   **Smart Notifications**: Automated FCM & local alerts for claim status updates (Approved/Rejected).
*   **PDF Engine**: Native generation of professional financial reports for administrative auditing.

---

## 🏗️ Architecture (Interview-Ready)

This project follows **Clean Architecture** principles to ensure the code is testable, maintainable, and scalable.

### Layers:
1.  **Presentation (Jetpack Compose + MVVM)**: Unidirectional Data Flow (UDF) using `StateFlow` to ensure predictable UI states.
2.  **Domain**: Pure Kotlin layer containing the business logic, models, and repository interfaces (Abstracted from frameworks).
3.  **Data**: Implementation of repositories, Firebase services (Auth, Firestore, Storage), and DTO mapping.

### Technical Highlights:
*   **Dependency Injection**: Hilt for modularity and effortless testing.
*   **Reactive Streams**: Kotlin Coroutines & Flow for asynchronous data handling.
*   **Security**: Server-side validation via strict Firestore Security Rules.
*   **Modern UI**: Material 3 components, dynamic theming, and responsive layouts.

---

## 🛠️ Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Backend** | Firebase (Auth, Firestore, Storage, Cloud Messaging) |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Hilt |
| **Image Loading** | Coil |
| **Async/Reactive** | Coroutines & Kotlin Flow |
| **Reporting** | Native Android PDF Document API |

---

## 📸 Database Schema (Firestore)

*   **`users/`**: `{uid, name, email, role, organizationId}`
*   **`expenses/`**: `{id, employeeId, employeeName, amount, category, status, receiptUrl, managerName, createdAt}`

---

## 🚦 Getting Started

1.  **Firebase Setup**:
    *   Enable **Email/Password Auth**, **Firestore**, and **Storage** in the Firebase Console.
    *   Add your `google-services.json` to the `/app` directory.
2.  **Security Rules**:
    *   Deploy the provided `firestore.rules` and `storage.rules` to your Firebase project.
3.  **SHA Fingerprints**:
    *   Run `./gradlew signingReport` and add SHA-1/SHA-256 keys to Firebase Settings to enable Play Integrity/reCAPTCHA.
4.  **Build**:
    *   Sync Gradle and run the `:app` module.

---

## 📈 Scalability & Future Improvements
*   **Modularization**: Convert packages into feature-based Gradle modules (`:feature:auth`, `:feature:dashboard`).
*   **Unit Testing**: Implement Mockito/JUnit for Domain and Data layers.
*   **CI/CD**: Integrate GitHub Actions for automated linting and APK distribution.
*   **Offline Mode**: Enable Firestore persistence for offline claim drafting.

---

## 👨‍💻 Interview Talk Points
*   **Why Clean Architecture?** Decouples business logic from the UI/Framework, allowing us to swap Firebase for a REST API without touching the UI.
*   **Why Flow over LiveData?** Flow provides better support for complex operators and is cleaner for non-Android-specific layers (Domain).
*   **Why StateFlow?** Ensures the UI always has a "current state" and handles configuration changes (like rotation) automatically.
