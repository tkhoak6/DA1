import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginForm() {
        // Thiết lập giao diện Form Đăng Nhập
        setTitle("Đăng nhập Hệ thống Bãi Xe");
        setSize(400, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị ra giữa màn hình
        setLayout(new BorderLayout());

        // Tiêu đề
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Panel chứa các ô nhập liệu
        JPanel pnlCenter = new JPanel(new GridLayout(2, 2, 10, 20));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        
        pnlCenter.add(new JLabel("Tên đăng nhập:"));
        txtUsername = new JTextField();
        pnlCenter.add(txtUsername);

        pnlCenter.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        pnlCenter.add(txtPassword);

        add(pnlCenter, BorderLayout.CENTER);

        // Panel chứa nút bấm
        JPanel pnlBottom = new JPanel();
        btnLogin = new JButton("Đăng nhập");
        pnlBottom.add(btnLogin);
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(pnlBottom, BorderLayout.SOUTH);

        // Bắt sự kiện khi click nút Đăng nhập
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xuLyDangNhap();
            }
        });
    }

    private void xuLyDangNhap() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        // Kiểm tra xem có để trống không
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
            return;
        }

        // Kết nối và truy vấn CSDL
        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "SELECT * FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Đăng nhập thành công -> Lấy Role và Tên
                int role = rs.getInt("MaRole");
                String tenNV = rs.getString("HoTen");
                
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!\nXin chào " + tenNV);

                // PHÂN QUYỀN MÀN HÌNH Ở ĐÂY
                if (role == 1) {
                    // Role 1 là Admin -> Mở Form cấu hình khu vực
                    AreaConfigForm adminForm = new AreaConfigForm();
                    adminForm.setVisible(true);
                } else if (role == 3) {
                    // Role 3 là Nhân viên -> Mở Form của nhân viên
                    // Chú ý: Cần đảm bảo file StaffForm.java của bạn không bị lỗi mới bỏ comment dòng dưới nhé
                    // StaffForm staffForm = new StaffForm();
                    // staffForm.setVisible(true);
                    JOptionPane.showMessageDialog(this, "Tính năng trạm gác nhân viên đang xây dựng...");
                }
                
                this.dispose(); // Đóng form đăng nhập hiện tại
            } else {
                // Sai tài khoản hoặc mật khẩu
                JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}