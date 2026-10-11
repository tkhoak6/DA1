import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    // Thông tin kết nối mặc định của XAMPP
    // Cấu hình UTF-8 để không bị lỗi font tiếng Việt khi lưu vào Database
    private static final String URL = "jdbc:mysql://localhost:3306/quanly_baixe?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP mặc định mật khẩu rỗng

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Khai báo sử dụng Driver của MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Thực hiện kết nối
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối CSDL quanly_baixe thành công!");
            
        } catch (ClassNotFoundException e) {
            System.out.println("Lỗi: Không tìm thấy thư viện MySQL JDBC Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Lỗi: Không thể kết nối tới MySQL. Hãy kiểm tra lại XAMPP!");
            e.printStackTrace();
        }
        return conn;
    }
    
    // Hàm main để bạn chạy test thử độc lập file này
    public static void main(String[] args) {
        getConnection();
    }
}