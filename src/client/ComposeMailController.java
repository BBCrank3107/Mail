package client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ComposeMailController {
    @FXML
    private Label usernameLabel; // Label để hiển thị username
    @FXML
    private TextField addressField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;

    private String username; // Thêm biến để lưu username

    // Thiết lập username
    public void setUsername(String username) {
        this.username = username; // Gán giá trị username cho biến instance
        usernameLabel.setText("Logged in as: " + username);
    }

    // Xử lý sự kiện khi người dùng nhấn nút "Send"
    @FXML
    private void handleSend() {
        String address = addressField.getText();
        String title = titleField.getText();
        String content = contentField.getText();

        if (!address.isEmpty() && !title.isEmpty() && !content.isEmpty()) {
            // Lưu email thành file trong thư mục người dùng
            saveEmailToFile(username, address, title, content);
            
            // Đóng cửa sổ Compose sau khi gửi thành công
            Stage stage = (Stage) addressField.getScene().getWindow();
            stage.close();
        }
    }

    // Lưu email thành file trong thư mục của người dùng
    private void saveEmailToFile(String username, String address, String title, String content) {
        // Đường dẫn lưu email vào thư mục người dùng
        File userDir = new File("mails/" + address);
        if (!userDir.exists()) {
            userDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }

        File emailFile = new File(userDir, title + ".txt");
        try (FileWriter writer = new FileWriter(emailFile)) {
        	writer.write("From: " + username + "\n");
            writer.write("To: " + address + "\n");
            writer.write("Title: " + title + "\n");
            writer.write("Content: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
