# FaceAttend - AI Attendance Tracker

## 1. Overview
FaceAttend is a modern Android application built to track employee attendance using on-device facial recognition and GPS geolocation. It features a dual-role system where administrators can manage staff and enroll their faces, while staff members can seamlessly clock in by snapping a selfie that is verified instantly against their enrolled biometric profile.

## 2. Architecture & Tech Stack
The app is built entirely natively using modern Android development standards:
* **Kotlin**: The primary programming language, utilizing Coroutines and Flows for asynchronous data streams.
* **Jetpack Compose**: Used exclusively for a declarative, reactive UI system.
* **MVVM Architecture**: Ensures a clean separation of concerns. UI state is hoisted in ViewModels and exposed as `StateFlow`s.
* **Room Database**: Provides robust local SQLite persistence for Users, Staff, and Attendance records, complete with a Repository layer.
* **CameraX**: Powers the camera experience, automatically binding to the Compose lifecycle to prevent memory leaks and handle rotation gracefully.
* **Google ML Kit**: Used for rapid, real-time face detection in the camera stream to ensure exactly one face is in the frame before allowing capture.
* **LiteRT (MobileFaceNet)**: An on-device TensorFlow Lite model (using `org.tensorflow.lite.Interpreter`) used to extract a 192-dimensional numerical embedding representing the unique features of a captured face.

## 3. How to Run
1. Clone the repository to your local machine.
2. Open the project in the latest stable release of **Android Studio**.
3. Let Gradle sync and resolve dependencies.
4. Build and run the app via the IDE, or use the terminal:
   * **To build an APK**: `./gradlew assembleDebug`
   * **To install on a connected device**: `./gradlew installDebug`
5. *Note: Because the app heavily relies on the camera, it is highly recommended to run this on a **physical Android device**, or an emulator configured with a passthrough webcam.*

## 4. Demo Credentials
The local database is automatically seeded on the first launch. You can log in using the following credentials:
* **Admin View**: Username: `admin` / Password: `admin123`
* **Staff View**: Username: `<EmployeeID>` (e.g., as assigned by admin) / Password: `1234`

## 5. Face Recognition Approach & Limitations
The app relies on a two-step biometric verification pipeline:
1. **Face Detection**: ML Kit validates that exactly one face is in the frame and crops the image strictly to the facial bounding box.
2. **Embedding Extraction**: The cropped image is normalized and passed through the `mobilefacenet.tflite` model via LiteRT, which outputs a 192-dimensional vector.
3. **Matching**: When marking attendance, the app calculates the **Cosine Similarity** between the newly captured selfie's embedding and the staff member's enrolled embedding. We use a heuristic threshold of **`0.75f`**.

**Limitations of this approach:**
* It is not robust to extremely poor lighting, drastic angle changes, or identical twins.
* It only supports single-face enrollment (a single frontal selfie).
* **Production Recommendations**: In a real-world enterprise app, this logic should be shifted to a secure backend cloud API to prevent client-side tampering. Additionally, rigorous **liveness detection** (e.g., blink detection, smile tracking, or depth sensing) is critical to prevent spoofing via printed photographs or screens. Enrollment should also capture multiple angles of the face to improve the matching confidence threshold.

## 6. Assumptions Made
Since the initial requirements left certain implementation details open, the following architectural and product assumptions were made:
* **Authentication**: Login is a basic local implementation. An admin account is seeded on the first app launch. When the admin adds a new staff member, a staff account is automatically created using their Employee ID as the username and `1234` as the default password.
* **Enrollment**: A staff member's face is enrolled using a single snapshot. The FloatArray embedding is encoded into a comma-separated String to be saved easily into the Room database.
* **Attendance Frequency**: Employees are allowed to mark attendance multiple times a day (to account for clocking out for lunch, shift changes, etc.). However, a 60-second throttle is enforced to prevent spamming check-ins.
* **Permissions**: Location is requested for attendance but allowed to fail gracefully. If the user denies GPS access, the app will record the attendance at `(0.0, 0.0)` rather than crashing.

## 7. Known Limitations & Future Improvements
If given more time, the following features would be added:
* **Cloud Syncing**: Moving from a local-only Room database to a Firebase Firestore backend to allow real-time syncing across multiple administrative devices.
* **Reverse Geocoding Optimization**: The "All Attendance" admin view currently caches reverse-geocoded addresses in memory. For thousands of records, this should be moved to a robust offline-first pagination system or handled server-side.
* **Liveness Detection**: Adding basic spoof-prevention by tracking eye contours or head movement vectors using ML Kit prior to allowing the capture button to be pressed.
