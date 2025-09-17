import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class FTPServerGUI {
    private JFrame frame;
    private JTextArea logArea;
    private JButton startButton, stopButton, exitButton;
    private ServerSocket serverSocket;
    private boolean running = false;
    private static final String SERVER_DIR = "server_files/";

    public FTPServerGUI() {
        new File(SERVER_DIR).mkdir();
        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("FTP Server");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBounds(20, 20, 440, 250);
        frame.add(logScrollPane);

        startButton = new JButton("Start Server");
        startButton.setBounds(50, 290, 130, 30);
        frame.add(startButton);

        stopButton = new JButton("Stop Server");
        stopButton.setBounds(190, 290, 130, 30);
        stopButton.setEnabled(false);
        frame.add(stopButton);

        exitButton = new JButton("Exit");
        exitButton.setBounds(330, 290, 100, 30);
        frame.add(exitButton);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        exitButton.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);
                running = true;
                log("Server started on port 5000.");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();
                }
            } catch (IOException e) {
                log("Error: " + e.getMessage());
            }
        }).start();
    }

    private void stopServer() {
        try {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
            }
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            log("Server stopped.");
        } catch (IOException e) {
            log("Error stopping server.");
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
        
                log("Client connected: " + socket.getInetAddress());
        
                String command;
                while ((command = in.readLine()) != null) {
                    handleCommand(command);
                }
            } catch (IOException e) {
                log("Client disconnected unexpectedly.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Error closing socket: " + e.getMessage());
                }
            }
        }
        
        private void handleCommand(String command) throws IOException {
            if (command.startsWith("list")) {
                sendFileList();
            } else if (command.startsWith("upload")) {
                String fileName = command.substring(7);
                receiveFile(fileName);
            } else if (command.startsWith("download")) {
                String fileName = command.substring(9);
                sendFile(fileName);
            } else if (command.startsWith("delete")) {
                String fileName = command.substring(7);
                deleteFile(fileName);
            }
        }
        
        

        private void sendFileList() {
            File dir = new File(SERVER_DIR);
            String[] files = dir.list();
            if (files != null) {
                for (String file : files) {
                    out.println(file);
                }
            }
            out.println("END_OF_LIST");
            log("Sent file list.");
        }

        private void receiveFile(String fileName) {
            try {
                long fileSize = dataIn.readLong();
                if (fileSize < 0) {
                    log("Invalid file size received for: " + fileName);
                    return;
                }
        
                File file = new File(SERVER_DIR + fileName);
                FileOutputStream fileOut = new FileOutputStream(file);
                byte[] buffer = new byte[131072];
                int bytesRead;
                long receivedBytes = 0;
        
                while (receivedBytes < fileSize && (bytesRead = dataIn.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                    receivedBytes += bytesRead;
                }
        
                fileOut.close();
        
                if (receivedBytes == fileSize) {
                    out.println("UPLOAD_SUCCESS");
                    log("File uploaded successfully: " + fileName);
                } else {
                    out.println("UPLOAD_FAILED");
                    log("File upload incomplete: " + fileName);
                }
            } catch (IOException e) {
                out.println("UPLOAD_ERROR");
                log("Error receiving file: " + fileName + " - " + e.getMessage());
            }
        }        

        private void sendFile(String fileName) {
            File file = new File(SERVER_DIR + fileName);
            try {
                if (!file.exists()) {
                    dataOut.writeLong(-1);
                    return;
                }

                dataOut.writeLong(file.length());
                FileInputStream fileIn = new FileInputStream(file);
                byte[] buffer = new byte[65536];
                int bytesRead;

                while ((bytesRead = fileIn.read(buffer)) > 0) {
                    dataOut.write(buffer, 0, bytesRead);
                }

                fileIn.close();
                log("File sent: " + fileName);
            } catch (IOException e) {
                log("Error sending file: " + fileName);
            }
        }

        private void deleteFile(String fileName) {
            File file = new File(SERVER_DIR + fileName);
            if (file.exists() && file.delete()) {
                log("File deleted: " + fileName);
            } else {
                log("Failed to delete: " + fileName);
            }
        }
    }

    public static void main(String[] args) {
        new FTPServerGUI();
    }
}