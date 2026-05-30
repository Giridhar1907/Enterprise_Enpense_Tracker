# Enterprise Expense Reimbursement Management System

A production-quality Android application built with modern standards for managing corporate expense claims.

## Tech Stack
- **Kotlin**: Primary programming language.
- **Jetpack Compose**: Declarative UI framework.
- **Clean Architecture**: Separation of concerns into Data, Domain, and Presentation layers.
- **MVVM**: Model-View-ViewModel pattern for stable state management.
- **Hilt**: Dependency injection for modularity and testability.
- **Firebase**:
    - **Authentication**: Secure user login and signup.
    - **Firestore**: Scalable NoSQL database for expense and user data.
    - **Storage**: Image storage for receipt uploads.
- **Kotlin Coroutines + Flow**: Asynchronous programming and reactive data streams.
- **Navigation Compose**: Type-safe navigation between screens.
- **Material 3**: Modern UI components and theming.

## Architecture Decisions & Tradeoffs

### 1. Clean Architecture
**Why**: Ensures that the business logic (Domain layer) is independent of the framework (Android/Firebase). This makes the app highly testable and maintainable.
**Tradeoff**: Increased boilerplate code initially (interfaces, use cases), but pays off as the project grows.

### 2. Repository Pattern
**Why**: Abstracts the data source. If we decide to migrate from Firebase to a REST API in the future, we only need to change the implementation in the Data layer; the rest of the app remains untouched.
**Tradeoff**: Requires mapping between Data Transfer Objects (DTOs) and Domain models.

### 3. State Management with StateFlow
**Why**: `StateFlow` is lifecycle-aware and integrates seamlessly with Jetpack Compose's `collectAsState`. It provides a "single source of truth" for the UI state.
**Tradeoff**: Requires careful management of event-driven states (like one-time navigation events).

## Scalability Discussion
- **Microservices Ready**: By abstracting data via repositories, the backend can be migrated to a microservices architecture without impacting the UI.
- **Module-based Scaling**: The current package structure can easily be converted into Gradle modules (e.g., `:feature:auth`, `:feature:expenses`) for faster build times and better team collaboration.
- **Offline Support**: Firestore provides built-in offline persistence, allowing the app to scale for users in low-connectivity environments.

## Future Improvements
- **Unit Testing**: Implement JUnit and Mockito for domain and repository layers.
- **UI Testing**: Use Compose Test Rule for automated UI validation.
- **Advanced Analytics**: Integrate Google Analytics for business insights.
- **Multi-tenancy**: Expand the `organizationId` logic to support isolated data for different companies.
