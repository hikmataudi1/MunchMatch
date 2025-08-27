# Munch Match 🍔📊💬📞

**Munch Match** is a modern **polling app** that allows users to create and participate in polls, chat in real-time, and make in-app voice calls using **Twilio**. Built with **Spring Boot**, **MySQL**, and **Supabase**, it delivers a seamless experience for voting, chatting, and calling.

---

## Table of Contents

- [Features](#features)  
- [Technologies](#technologies)  
- [Architecture](#architecture)  
- [Setup & Installation](#setup--installation)  
- [Configuration](#configuration)  
- [Security Considerations](#security-considerations)  
- [Contributing](#contributing)  
- [License](#license)  

---

## Features

### Polling
- Create polls with multiple options and real time updates, each containing:
  - Title & description  
  - Image  
  - Categories  
- **Voter details** tracked per option (ID, name, email, profile image).  
- Polls can be:
  - **Scheduled** for a specific date/time  
  - **Private** (restricted access) or public  
- Users can **filter choices** while voting or viewing polls.

### Chat
- Real-time messaging with **Twilio Chat Services**.  
- Messages include author, attributes, timestamps, and message IDs.  
- **Automatic moderation** filters messages for bad language.  
- Supports webhooks: `onMessageAdded`, `onMessageSend`.  
- Persistent chat history for all participants.  

### Voice Calls
- In-app voice calling via **Twilio Voice API** and access tokens.  
- Call other users directly within the app.  
- Online/offline user status via WebSocket.  
- Supports TwiML apps for advanced call routing and handling.  

---

## Technologies

- **Backend:** Spring Boot, Java 17  
- **Database:** MySQL  
- **Storage:** Supabase for images & media  
- **Authentication:** JWT tokens  
- **Real-Time Communication:** Twilio (Chat & Voice), WebSocket  
- **Frontend (Optional):** Android SDK or Web client  

---

## Architecture

1. **Spring Boot Backend**
   - REST APIs for polls, users, chat, and calls  
   - JWT authentication & authorization  
   - Twilio integration for chat and calls  

2. **Database Layer**
   - MySQL stores polls, options, votes, users, and chat history  

3. **Media Storage**
   - Supabase bucket stores images and assets  

4. **Real-Time Layer**
   - WebSocket for user presence and notifications  
   - Twilio Chat & Voice for messaging and calls  

---

## Setup & Installation

### 1. Clone the project
```bash
git clone https://github.com/your-username/munch-match.git
cd munch-match


### 2. Configure Environment Variables

Create a `.env` file in the project root (this file should be ignored by Git) and add your credentials:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/restoapp
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword
APP_JWT_SECRET=supersecretjwt
APP_JWT_EXPIRATION_MILLISECONDS=604800000

TWILIO_ACCOUNTSID=ACxxxxxxxxxxxxxxxx
TWILIO_APIKEYSID=SKxxxxxxxxxxxxxxxx
TWILIO_APIKEYSECRET=yourapikeysecret
TWILIO_TWIMLAPPSID=APxxxxxxxxxxxxxxxx
TWILIO_TOKENTTLSECONDS=86400
TWILIO_SERVICESID=ISxxxxxxxxxxxxxxxx
TWILIO_AUTHTOKEN=yourauthtoken
TWILIO_CHATSERVICESID=ISxxxxxxxxxxxxxxxx

SUPABASE_URL=https://yourproject.supabase.co
SUPABASE_APIKEY=your-supabase-key
SUPABASE_BUCKET=your-bucket-name


### 3. Run the Application

Start the Spring Boot backend:

```bash
./mvnw spring-boot:run

