import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffForm extends JFrame {
    // Component Nhận Xe
    private JTextField txtBienSoNhan;
    private JComboBox<String> cbxKhuVuc;
    private JButton btnNhanXe;

    // Component Trả Xe
    private JTextField txtBienSoTra;
    private JButton btnTimXe, btnTraXe;
    private JLabel lblThongTinXe;

    private int maGiaoDichHienTai = -1; // Lưu mã giao dịch khi tìm thấy xe
    private String maKhuVucHienTai = "";

    public StaffForm() {
        setTitle("Trạm Gác - Phần mềm Quản lý Bãi Xe (Nhân viên)");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2, 10, 10)); // Chia đôi màn hình trái/phải

        // ================= PHẦN TRÁI: NHẬN XE =================
        JPanel pnlNhanXe = new JPanel(new BorderLayout());
        pnlNhanXe.setBorder(BorderFactory.createTitledBorder("THAO TÁC NHẬN XE VÀO"));

        JPanel pnlNhanXeInput = new JPanel(new GridLayout(3, 2, 10, 20));
        pnlNhanXeInput.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        pnlNhanXeInput.add(new JLabel("Biển số xe:"));
        txtBienSoNhan = new JTextField();
        pnlNhanXeInput.add(txtBienSoNhan);

        pnlNhanXeInput.add(new JLabel("Khu vực đỗ:"));
        cbxKhuVuc = new JComboBox<>();
        loadKhuVucIntoComboBox(); // Load danh sách khu từ DB
        pnlNhanXeInput.add(cbxKhuVuc);

        pnlNhanXe.add(pnlNhanXeInput, BorderLayout.CENTER);

        JPanel pnlNhanXeBtn = new JPanel();
        btnNhanXe = new JButton("GHI NHẬN XE VÀO");
        btnNhanXe.setFont(new Font("Arial", Font.BOLD, 14));
        btnNhanXe.setBackground(new Color(144, 238, 144)); // Màu xanh lá nhạt
        pnlNhanXeBtn.add(btnNhanXe);
        pnlNhanXe.add(pnlNhanXeBtn, BorderLayout.SOUTH);

        // ================= PHẦN PHẢI: TRẢ XE =================
        JPanel pnlTraXe = new JPanel(new BorderLayout());
        pnlTraXe.setBorder(BorderFactory.createTitledBorder("THAO TÁC TRẢ XE RA"));

        JPanel pnlTraXeInput = new JPanel(new GridLayout(4, 1, 10, 10));
        pnlTraXeInput.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlTimKiem = new JPanel(new BorderLayout(10, 0));
        pnlTimKiem.add(new JLabel("Biển số xe: "), BorderLayout.WEST);
        txtBienSoTra = new JTextField();
        pnlTimKiem.add(txtBienSoTra, BorderLayout.CENTER);
        btnTimXe = new JButton("Tìm");
        pnlTimKiem.add(btnTimXe, BorderLayout.EAST);
        pnlTraXeInput.add(pnlTimKiem);

        lblThongTinXe = new JLabel("<html>Vui lòng nhập biển số để tìm...<br><br><br></html>");
        lblThongTinXe.setForeground(Color.BLUE);
        pnlTraXeInput.add(lblThongTinXe);

        pnlTraXe.add(pnlTraXeInput, BorderLayout.CENTER);

        JPanel pnlTraXeBtn = new JPanel();
        btnTraXe = new JButton("THANH TOÁN & TRẢ XE");
        btnTraXe.setFont(new Font("Arial", Font.BOLD, 14));
        btnTraXe.setBackground(new Color(255, 182, 193)); // Màu hồng nhạt
        btnTraXe.setEnabled(false); // Ẩn nút thanh toán đi cho đến khi tìm thấy xe
        pnlTraXeBtn.add(btnTraXe);
        pnlTraXe.add(pnlTraXeBtn, BorderLayout.SOUTH);

        // Add 2 nửa vào Form chính
        add(pnlNhanXe);
        add(pnlTraXe);

        // Gắn sự kiện cho các nút bấm
        btnNhanXe.addActionListener(e -> xuLyNhanXe());
        btnTimXe.addActionListener(e -> timXeTrongBai());
        btnTraXe.addActionListener(e -> xuLyTraXe());
    }

    // Hàm lấy danh sách khu vực từ DB để đưa vào Menu thả xuống
    private void loadKhuVucIntoComboBox() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT MaKhuVuc, TenKhuVuc FROM KhuVuc");
            while (rs.next()) {
                cbxKhuVuc.addItem(rs.getString("MaKhuVuc") + " - " + rs.getString("TenKhuVuc"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void xuLyNhanXe() {
        String bienSo = txtBienSoNhan.getText().trim();
        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa nhập biển số xe!");
            return;
        }

        String selectedKhu = cbxKhuVuc.getSelectedItem().toString();
        String maKhu = selectedKhu.split("-")[0].trim(); // Tách lấy Mã khu vực (K1, K2...)

        try (Connection conn = DatabaseHelper.getConnection()) {
            // 1. Kiểm tra xem bãi còn chỗ không
            PreparedStatement stmtCheck = conn
                    .prepareStatement("SELECT SucChuaToiDa, SoXeDangDo FROM KhuVuc WHERE MaKhuVuc = ?");
            stmtCheck.setString(1, maKhu);
            ResultSet rsCheck = stmtCheck.executeQuery();
            if (rsCheck.next()) {
                if (rsCheck.getInt("SoXeDangDo") >= rsCheck.getInt("SucChuaToiDa")) {
                    JOptionPane.showMessageDialog(this, "Khu vực này đã đầy!");
                    return;
                }
            }

            // 2. Insert vào LichSuGiaoDich
            PreparedStatement stmtInsert = conn.prepareStatement(
                    "INSERT INTO LichSuGiaoDich (BienSoXe, MaKhuVuc, ThoiGianVao, TrangThai) VALUES (?, ?, NOW(), 'DangDo')");
            stmtInsert.setString(1, bienSo);
            stmtInsert.setString(2, maKhu);
            stmtInsert.executeUpdate();

            // 3. Update tăng số xe đang đỗ trong bảng KhuVuc
            PreparedStatement stmtUpdate = conn
                    .prepareStatement("UPDATE KhuVuc SET SoXeDangDo = SoXeDangDo + 1 WHERE MaKhuVuc = ?");
            stmtUpdate.setString(1, maKhu);
            stmtUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Nhận xe thành công!");
            txtBienSoNhan.setText(""); // Xóa trắng ô nhập liệu

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nhận xe: " + ex.getMessage());
        }
    }

    private void timXeTrongBai() {
        String bienSo = txtBienSoTra.getText().trim();
        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập biển số để tìm!");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM LichSuGiaoDich WHERE BienSoXe = ? AND TrangThai = 'DangDo'");
            stmt.setString(1, bienSo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                maGiaoDichHienTai = rs.getInt("MaGiaoDich");
                maKhuVucHienTai = rs.getString("MaKhuVuc");
                String thoiGianVao = rs.getString("ThoiGianVao");

                lblThongTinXe.setText("<html>Mã GD: " + maGiaoDichHienTai + "<br>Khu vực: " + maKhuVucHienTai
                        + "<br>Giờ vào: " + thoiGianVao + "</html>");
                btnTraXe.setEnabled(true); // Cho phép bấm nút thanh toán
            } else {
                lblThongTinXe.setText("Không tìm thấy xe này trong bãi!");
                btnTraXe.setEnabled(false);
                maGiaoDichHienTai = -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void xuLyTraXe() {
        if (maGiaoDichHienTai == -1)
            return;

        // Code tính tiền tạm thời rất đơn giản: Xe Ô tô (K2) thu 20.000, Xe máy thu
        // 5.000
        int tienThu = maKhuVucHienTai.equals("K2") ? 20000 : 5000;

        try (Connection conn = DatabaseHelper.getConnection()) {
            // 1. Update LichSuGiaoDich: Chuyển trạng thái, cập nhật giờ ra và tiền
            PreparedStatement stmtUpdate = conn.prepareStatement(
                    "UPDATE LichSuGiaoDich SET ThoiGianRa = NOW(), TienThu = ?, TrangThai = 'DaTra' WHERE MaGiaoDich = ?");
            stmtUpdate.setInt(1, tienThu);
            stmtUpdate.setInt(2, maGiaoDichHienTai);
            stmtUpdate.executeUpdate();

            // 2. Giảm số lượng xe đang đỗ trong bảng KhuVuc
            PreparedStatement stmtKhuVuc = conn.prepareStatement(
                    "UPDATE KhuVuc SET SoXeDangDo = SoXeDangDo - 1 WHERE MaKhuVuc = ?");
            stmtKhuVuc.setString(1, maKhuVucHienTai);
            stmtKhuVuc.executeUpdate();

            JOptionPane.showMessageDialog(this, "Trả xe thành công!\nThu tiền: " + tienThu + " VNĐ");

            // Reset form lại như ban đầu
            lblThongTinXe.setText("<html>Vui lòng nhập biển số để tìm...<br><br><br></html>");
            txtBienSoTra.setText("");
            btnTraXe.setEnabled(false);
            maGiaoDichHienTai = -1;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi trả xe: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StaffForm().setVisible(true);
        });
    }
}