# 📂 Java Swing FTP Application

A simple yet powerful **File Transfer Protocol (FTP)** application built with **Java Swing** ✨<br>
It includes two applications:

* 🖥 **Server (`FTPServerGUI`)** – manages file storage & client requests.
* 👤 **Client (`FTPClientGUI`)** – provides a graphical interface to upload, download, list, and delete files.

---

## 🌟 Features

- ✅ **Server GUI** – Start/stop server & view logs.
- ✅ **Client GUI** – Clean interface for file operations.
- 📜 **File Listing** – See all server files in one click.
- ⬆️ **File Upload** – Send files to the server.
- ⬇️ **File Download** – Save files locally.
- 🗑 **File Deletion** – Remove unwanted files from the server.
- 📊 **Progress Bar** – Visualize upload & download progress.
- 🔄 **Retry Mechanism** – Auto-retry failed uploads (up to 3 times).

---

## ⚡ Getting Started

### 🔧 Prerequisites

* ☕ **Java Development Kit (JDK)** 8 or higher installed

---

### 🚀 Running the Server

1. Open **Terminal / Command Prompt**.
2. Navigate to the folder containing `FTPServerGUI.java`.
3. Compile:
   ```bash
   javac FTPServerGUI.java
   ```
4. Run:
   ```bash
   java FTPServerGUI
   ```
5. 🎉 A GUI window will appear. Click **Start Server** to begin (default port: `5000`).

   * A 📁 `server_files/` directory is created automatically for file storage.

---

### 🚀 Running the Client

1. Open a **new Terminal / Command Prompt**.
2. Navigate to the folder containing `FTPClientGUI.java`.
3. Compile:
   ```bash
   javac FTPClientGUI.java
   ```
4. Run:
   ```bash
   java FTPClientGUI
   ```
5. Enter the **Server IP** (default: `127.0.0.1` for local).
6. Click **Connect** ⚡

   * A 📁 `client_files/` directory is created for downloads.

---

## 📸 Screenshots
- 🖥 Server GUI

- 👤 Client GUI

---

## 🖥 Usage

### 🔹 Server (FTPServerGUI)

* ▶️ **Start Server** – Accepts client connections.
* ⏹ **Stop Server** – Disconnects all clients & shuts down.
* ❌ **Exit** – Close server app.
* 📝 **Log Area** – Shows real-time activity (connections, file transfers, errors).

### 🔹 Client (FTPClientGUI)

* 🔗 **Connect** – Connect to server.
* 📂 **List Files** – Display all files on the server.
* ⬆️ **Upload** – Select and send files to the server.
* ⬇️ **Download** – Save selected file(s) locally.
* 🗑 **Delete** – Remove file(s) from server.
* ❌ **Exit** – Close client & disconnect.

---

## 📁 Project Structure

```
📦 Java-Swing-FTP
 ┣ 📜 FTPServerGUI.java   # Server logic + GUI
 ┣ 📜 FTPClientGUI.java   # Client logic + GUI
 ┣ 📂 server_files/       # Server-side stored files
 ┣ 📂 client_files/       # Client-side downloaded files
```

---

## ⚙️ Implementation Details

* 🌐 **Networking** – Uses `ServerSocket` & `Socket` (TCP/IP).
* 🧵 **Multithreading** – Each client runs in its own thread.
* 🔄 **Data Streams** –

  * `DataInputStream` / `DataOutputStream` → File data
  * `BufferedReader` / `PrintWriter` → Commands
* 🎨 **GUI** – Built with **Java Swing** + event listeners.
* 🛡 **Error Handling** – `try-catch` blocks with friendly logs & dialogs.

---

## 🎯 Final Notes

This project is perfect for learning **Java networking, multithreading, and Swing GUI development** 🚀
Whether you want to **transfer files locally** or just **experiment with client-server systems**, this project has you covered 💡.

💾 Happy Coding & File Sharing! ⚡

---
