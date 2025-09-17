import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FTPClientGUI {
    private JFrame frame;
    private JTextField serverIPField;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JButton connectButton, uploadButton, downloadButton, deleteButton, listButton, exitButton;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private static final String CLIENT_DIR = "client_files/";

    public FTPClientGUI() {
        new File(CLIENT_DIR).mkdir();
        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("FTP Client");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel serverLabel = new JLabel("Server IP:");
        serverLabel.setBounds(20, 20, 100, 25);
        frame.add(serverLabel);

        serverIPField = new JTextField("127.0.0.1");
        serverIPField.setBounds(100, 20, 150, 25);
        frame.add(serverIPField);

        connectButton = new JButton("Connect");
        connectButton.setBounds(270, 20, 100, 25);
        frame.add(connectButton);

        listButton = new JButton("List Files");
        listButton.setBounds(400, 20, 120, 25);
        listButton.setEnabled(false);
        frame.add(listButton);

        uploadButton = new JButton("Upload");
        uploadButton.setBounds(20, 60, 100, 25);
        uploadButton.setEnabled(false);
        frame.add(uploadButton);

        downloadButton = new JButton("Download");
        downloadButton.setBounds(130, 60, 120, 25);
        downloadButton.setEnabled(false);
        frame.add(downloadButton);

        deleteButton = new JButton("Delete");
        deleteButton.setBounds(260, 60, 100, 25);
        deleteButton.setEnabled(false);
        frame.add(deleteButton);

        progressBar = new JProgressBar();
        progressBar.setBounds(20, 100, 530, 25);
        progressBar.setStringPainted(true);
        frame.add(progressBar);

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        JScrollPane fileScrollPane = new JScrollPane(fileList);
        fileScrollPane.setBounds(20, 140, 530, 200);
        frame.add(fileScrollPane);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBounds(20, 350, 530, 80);
        frame.add(logScrollPane);

        exitButton = new JButton("Exit");
        exitButton.setBounds(250, 440, 100, 30);
        frame.add(exitButton);

        connectButton.addActionListener(e -> connectToServer());
        listButton.addActionListener(e -> listFiles());
        uploadButton.addActionListener(e -> uploadFiles());
        downloadButton.addActionListener(e -> downloadFiles());
        deleteButton.addActionListener(e -> deleteFile());
        exitButton.addActionListener(e -> exitApplication());

        frame.setVisible(true);
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverIPField.getText().trim(), 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            log("Connected to server.");
            listButton.setEnabled(true);
            uploadButton.setEnabled(true);
            downloadButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } catch (IOException e) {
            showError("Connection failed.");
        }
    }

    private void listFiles() {
        try {
            out.println("list");
            fileListModel.clear();
            String response;
            while (!(response = in.readLine()).equals("END_OF_LIST")) {
                fileListModel.addElement(response);
            }
            log("File list updated.");
        } catch (IOException e) {
            showError("Error listing files. Server might be down.");
        }
    }
    
    private void uploadFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                for (File file : fileChooser.getSelectedFiles()) {
                    int attempts = 3; // Allow 3 retries
                    boolean uploadSuccessful = false;
    
                    while (attempts-- > 0 && !uploadSuccessful) {
                        uploadSuccessful = uploadFile(file);
                        if (!uploadSuccessful && attempts > 0) {
                            log("Retrying upload: " + file.getName());
                        }
                    }
    
                    if (!uploadSuccessful) {
                        log("Skipping file after 3 failed attempts: " + file.getName());
                    }
                }
            }).start();
        }
    }
    
    

    private boolean uploadFile(File file) {
        try {
            out.println("upload " + file.getName());
            dataOut.writeLong(file.length());
    
            FileInputStream fileIn = new FileInputStream(file);
            byte[] buffer = new byte[131072];
            int bytesRead;
            long uploadedBytes = 0;
    
            while ((bytesRead = fileIn.read(buffer)) > 0) {
                dataOut.write(buffer, 0, bytesRead);
                uploadedBytes += bytesRead;
                updateProgress(uploadedBytes, file.length());
            }
            
            fileIn.close();
            dataOut.flush(); // Ensure all bytes are sent
    
            // Ensure server response is received
            String response = in.readLine();
            if ("UPLOAD_SUCCESS".equals(response)) {
                log("Uploaded: " + file.getName());
                return true;
            } else {
                log("Upload failed: " + file.getName());
                return false;
            }
        } catch (IOException e) {
            log("Error uploading file: " + file.getName() + " - " + e.getMessage());
        }
        return false;
    }
    
    
    
    private void downloadFiles() {
        java.util.List<String> selectedFiles = fileList.getSelectedValuesList();
        if (selectedFiles.isEmpty()) {
            showError("Select files to download.");
            return;
        }

        new Thread(() -> {
            for (String fileName : selectedFiles) {
                downloadFile(fileName);
            }
        }).start();
    }

    private void downloadFile(String fileName) {
        try {
            out.println("download " + fileName);
            long fileSize = dataIn.readLong();
            if (fileSize == -1) {
                showError("File not found: " + fileName);
                return;
            }

            FileOutputStream fileOut = new FileOutputStream(CLIENT_DIR + fileName);
            byte[] buffer = new byte[131072];
            int bytesRead;
            long downloadedBytes = 0;

            while (downloadedBytes < fileSize && (bytesRead = dataIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                downloadedBytes += bytesRead;
                updateProgress(downloadedBytes, fileSize);
            }

            fileOut.close();
            log("Downloaded: " + fileName);
        } catch (IOException e) {
            showError("Download failed: " + fileName);
        }
    }

    private void deleteFile() {
        String fileName = fileList.getSelectedValue();
        if (fileName == null) {
            showError("Select a file to delete.");
            return;
        }

        out.println("delete " + fileName);
        log("Deleted file: " + fileName);
        listFiles();
    }

    private void updateProgress(long current, long total) {
        SwingUtilities.invokeLater(() -> progressBar.setValue((int) ((current * 100) / total)));
    }

    private void exitApplication() {
        try {
            if (out != null) out.println("exit");
            if (socket != null) socket.close();
        } catch (IOException e) {
            log("Error closing connection.");
        }
        System.exit(0);
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        new FTPClientGUI();
    }
}

