CREATE DATABASE IF NOT EXISTS QuanLyDoXe;
USE QuanLyDoXe;

CREATE TABLE KhuVuc (
    MaKhuVuc VARCHAR(20) PRIMARY KEY,
    TenKhuVuc VARCHAR(100) NOT NULL,
    SucChuaToiDa INT NOT NULL,
    SoXeDangDo INT DEFAULT 0
);

-- Thêm một vài dữ liệu mẫu
INSERT INTO KhuVuc (MaKhuVuc, TenKhuVuc, SucChuaToiDa, SoXeDangDo) 
VALUES ('KHU_A', 'Khu vực xe máy Tầng 1', 100, 0),
       ('KHU_B', 'Khu vực xe số', 50, 0),
       ('KHU_C', 'Khu vực ô tô', 20, 0);
