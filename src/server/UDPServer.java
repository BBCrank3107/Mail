package server;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class UDPServer {
    private static final int PORT = 12345;
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String MAILS_DIR = "mails/";

    public static void main(String[] args) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(PORT);
        System.out.println("Server is running...");

        byte[] receiveData = new byte[1024];
        byte[] sendData;

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            String[] parts = clientMessage.split(" ");
            String command = parts[0];

            if ("REGISTER".equals(command)) {
                if (parts.length < 3) {
                    sendData = "INVALID_COMMAND".getBytes();
                } else {
                    String username = parts[1];
                    String password = parts[2];

                    if (registerAccount(username, password)) {
                        sendData = "REGISTER_SUCCESS".getBytes();
                    } else {
                        sendData = "REGISTER_FAILED".getBytes();
                    }
                }
            } else if ("LOGIN".equals(command)) {
                if (parts.length < 3) {
                    sendData = "INVALID_COMMAND".getBytes();
                } else {
                    String username = parts[1];
                    String password = parts[2];

                    if (checkLogin(username, password)) {
                        sendData = "LOGIN_SUCCESS".getBytes();
                    } else {
                        sendData = "LOGIN_FAILED".getBytes();
                    }
                }
            } else if ("SEND_EMAIL".equals(command)) {
                if (parts.length < 4) {
                    sendData = "INVALID_COMMAND".getBytes();
                } else {
                    String from = parts[1]; // username
                    String to = parts[2]; // address
                    String title = parts[3]; // title
                    StringBuilder contentBuilder = new StringBuilder();
                    for (int i = 4; i < parts.length; i++) {
                        contentBuilder.append(parts[i]).append(" ");
                    }
                    String content = contentBuilder.toString().trim();

                    saveEmailToFile(from, to, title, content);
                    sendData = "EMAIL_SENT".getBytes(); // Phản hồi cho client
                }
            }else if ("GET_EMAILS".equals(command)) {
                if (parts.length < 2) {
                    sendData = "INVALID_COMMAND".getBytes();
                } else {
                    String username = parts[1];
                    String emailList = getEmailList(username);
                    sendData = emailList.getBytes();
                }
            }else if ("READ_EMAIL".equals(command)) {
                if (parts.length < 3) {
                    sendData = "INVALID_COMMAND".getBytes();
                } else {
                    String username = parts[1];
                    String emailTitle = parts[2];

                    String emailContent = readEmailContent(username, emailTitle);
                    if (emailContent != null) {
                        sendData = emailContent.getBytes();
                    } else {
                        sendData = "EMAIL_NOT_FOUND".getBytes();
                    }
                }
            }else {
                sendData = "INVALID_COMMAND".getBytes();
            }

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);
        }
    }

    private static String readEmailContent(String username, String title) {
        String filePath = MAILS_DIR + username + "/" + title + ".txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            return contentBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static boolean registerAccount(String username, String password) throws IOException {
        File accountsFile = new File(ACCOUNTS_FILE);
        if (!accountsFile.exists()) {
            accountsFile.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(accountsFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] account = line.split(" ");
            if (account[0].equals(username)) {
                return false;
            }
        }
        reader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(accountsFile, true));
        writer.write(username + " " + password);
        writer.newLine();
        writer.close();

        Path userDir = Paths.get(MAILS_DIR + username);
        Files.createDirectories(userDir);
        return true;
    }

    private static boolean checkLogin(String username, String password) throws IOException {
        File accountsFile = new File(ACCOUNTS_FILE);
        if (!accountsFile.exists()) {
            return false;
        }

        BufferedReader reader = new BufferedReader(new FileReader(accountsFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] account = line.split(" ");
            if (account[0].equals(username) && account[1].equals(password)) {
                return true;
            }
        }
        reader.close();
        return false;
    }

    // Hàm lấy danh sách email của user dưới dạng chuỗi
    private static String getEmailList(String username) throws IOException {
        File userDir = new File(MAILS_DIR + username);
        if (!userDir.exists()) {
            return "NO_EMAILS";
        }

        File[] emailFiles = userDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (emailFiles == null || emailFiles.length == 0) {
            return "NO_EMAILS";
        }

        StringBuilder emailList = new StringBuilder();
        for (File email : emailFiles) {
            emailList.append(email.getName()).append(" ");
        }
        return emailList.toString().trim();
    }

    // Hàm gửi email
    private static void saveEmailToFile(String from, String to, String title, String content) {
        try {
            String userDir = MAILS_DIR + to; // Thư mục của người nhận
            File dir = new File(userDir);
            if (!dir.exists()) {
                dir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
            }
            FileWriter writer = new FileWriter(new File(dir, title + ".txt"));
            writer.write("From: " + from + "\n");
            writer.write("To: " + to + "\n");
            writer.write("Title: " + title + "\n");
            writer.write("Content: \n" + content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
