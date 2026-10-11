import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

public class AreaConfigForm extends JFrame {
    private JTable tableKhuVuc;
    private DefaultTableModel tableModel;
    private JTextField txtMaKhuVuc, txtTenKhuVuc, txtSucChua;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;

    public AreaConfigForm() {
        setTitle("Cấu hình Khu vực (Dành cho Admin)");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Phần trên: Nhập liệu ---
        JPanel pnlInput = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông tin Khu vực"));

        pnlInput.add(new JLabel("Mã khu vực:"));
        txtMaKhuVuc = new JTextField();
        pnlInput.add(txtMaKhuVuc);

        pnlInput.add(new JLabel("Tên khu vực:"));
        txtTenKhuVuc = new JTextField();
        pnlInput.add(txtTenKhuVuc);

        pnlInput.add(new JLabel("Sức chứa tối đa:"));
        txtSucChua = new JTextField();
        pnlInput.add(txtSucChua);

        add(pnlInput, BorderLayout.NORTH);

        // --- Phần giữa: Bảng hiển thị dữ liệu ---
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Mã Khu Vực");
        tableModel.addColumn("Tên Khu Vực");
        tableModel.addColumn("Sức Chứa Tối Đa");
        tableModel.addColumn("Số Xe Đang Đỗ");

        tableKhuVuc = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableKhuVuc);
        add(scrollPane, BorderLayout.CENTER);

        // Bắt sự kiện Click chuột vào bảng
        tableKhuVuc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableKhuVuc.getSelectedRow();
                if (row >= 0) {
                    txtMaKhuVuc.setText(tableModel.getValueAt(row, 0).toString());
                    txtMaKhuVuc.setEnabled(false); // Khóa Mã khu vực lại, không cho sửa khóa chính
                    txtTenKhuVuc.setText(tableModel.getValueAt(row, 1).toString());
                    txtSucChua.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });

        // --- Phần dưới: Các nút chức năng ---
        JPanel pnlButtons = new JPanel();
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới Form");
        
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        add(pnlButtons, BorderLayout.SOUTH);

        // Bắt sự kiện cho các nút
        btnThem.addActionListener(e -> themKhuVuc());
        btnSua.addActionListener(e -> suaKhuVuc());
        btnXoa.addActionListener(e -> xoaKhuVuc());
        btnLamMoi.addActionListener(e -> clearForm());

        loadDataToTable();
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0); 
        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "SELECT * FROM KhuVuc";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("MaKhuVuc"));
                row.add(rs.getString("TenKhuVuc"));
                row.add(rs.getInt("SucChuaToiDa"));
                row.add(rs.getInt("SoXeDangDo"));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtMaKhuVuc.setText("");
        txtMaKhuVuc.setEnabled(true); // Mở khóa lại ô Mã
        txtTenKhuVuc.setText("");
        txtSucChua.setText("");
        tableKhuVuc.clearSelection();
    }

    private void themKhuVuc() {
        String ma = txtMaKhuVuc.getText();
        String ten = txtTenKhuVuc.getText();
        String sucChua = txtSucChua.getText();

        if (ma.isEmpty() || ten.isEmpty() || sucChua.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "INSERT INTO KhuVuc (MaKhuVuc, TenKhuVuc, SucChuaToiDa, SoXeDangDo) VALUES (?, ?, ?, 0)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ma);
            stmt.setString(2, ten);
            stmt.setInt(3, Integer.parseInt(sucChua));
            
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm khu vực thành công!");
            loadDataToTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm dữ liệu (Có thể trùng Mã): " + ex.getMessage());
        }
    }

    private void suaKhuVuc() {
        String ma = txtMaKhuVuc.getText();
        String ten = txtTenKhuVuc.getText();
        String sucChua = txtSucChua.getText();

        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khu vực cần sửa từ bảng!");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "UPDATE KhuVuc SET TenKhuVuc = ?, SucChuaToiDa = ? WHERE MaKhuVuc = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ten);
            stmt.setInt(2, Integer.parseInt(sucChua));
            stmt.setString(3, ma);
            
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataToTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi sửa dữ liệu: " + ex.getMessage());
        }
    }

    private void xoaKhuVuc() {
        String ma = txtMaKhuVuc.getText();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khu vực cần xóa từ bảng!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khu vực " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                String sql = "DELETE FROM KhuVuc WHERE MaKhuVuc = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, ma);
                
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDataToTable();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AreaConfigForm().setVisible(true);
        });
    }
}