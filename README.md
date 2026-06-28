🍽️ Restaurant Manager App

An Android application for managing restaurant operations, built with Java and Firebase.


⚠️ This project is still under active development. Many features are being improved and new ones are planned. Contributions and feedback are welcome!




📱 Features (Current)


🔐 Authentication — Login and registration with Firebase Auth
🍕 Menu Management — Add, view, and manage menu items
📦 Order Tracking — Create and monitor orders in real time
🪑 Table Management — Add and manage restaurant tables
👥 Customer Management — Store and view customer information
📅 Reservations — Add and track reservations
👨‍💼 Staff Management — Manage staff members
🎁 Promotions — Create and display promotions
📊 Dashboard — Overview of restaurant activity with charts
👤 Profile — User profile management
🔔 Notifications — In-app notification system
📧 Email Service — Transactional email integration via Brevo (Sendinblue)



🛠️ Tech Stack

LayerTechnologyLanguageJavaIDEAndroid StudioAuthenticationFirebase AuthDatabaseFirebase FirestoreChartsMPAndroidChartEmailBrevo (Sendinblue) SMTP APIMin SDKAndroid 7.0 (API 24)


🚧 Work in Progress

This app is still under development. Here are some areas that still need improvement:


 Better error handling and input validation
 Improved UI/UX design and responsiveness
 Order status tracking (pending → preparing → served)
 Dashboard with more detailed analytics
 Search and filter functionality across all sections
 Dark mode support
 Multi-language support (Arabic, French, English)
 Unit and integration tests
 Performance optimization



🚀 Getting Started

Prerequisites


Android Studio (latest version recommended)
A Firebase project with Firestore and Authentication enabled
A Brevo (Sendinblue) account for email features


Setup


Clone the repository:


bashgit clone https://github.com/jihed0205/Restaurant_Manager.git


Open the project in Android Studio
Connect your own Firebase project:

Go to Firebase Console
Create a new project
Download google-services.json and place it in the /app folder
Enable Authentication and Firestore in the Firebase Console



Add your Brevo API key in EmailService.java:


javaprivate static final String API_KEY = "your_api_key_here";


Build and run the app on an emulator or physical device



📁 Project Structure

app/src/main/java/com/example/restaurantapp/
│
├── Activities          # Login, Register, Add screens
├── Fragments           # Dashboard, Menu, Orders, Tables...
├── Adapters            # RecyclerView adapters
└── Models              # Data classes (Order, MenuItem, Customer...)


👨‍💻 Author

Jihed Laouini

Student in computer science — ISAMM, Université de la Manouba

📧 jihedlouini@gmail.com

🔗 LinkedIn


