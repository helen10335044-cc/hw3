-- MySQL 8.0.33
DROP DATABASE IF EXISTS booking_system2;
CREATE DATABASE booking_system2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE booking_system2;

CREATE TABLE customer (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_no VARCHAR(30) NOT NULL UNIQUE,
  customer_name VARCHAR(80) NOT NULL,
  phone VARCHAR(30),
  address VARCHAR(200),
  username VARCHAR(60) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 增加 role 欄位做權限管理（你需求寫「管理員登入」必須能辨識 ADMIN/STAFF）
CREATE TABLE employee (
  id INT AUTO_INCREMENT PRIMARY KEY,
  employee_no VARCHAR(30) NOT NULL UNIQUE,
  employee_name VARCHAR(80) NOT NULL,
  phone VARCHAR(30),
  address VARCHAR(200),
  username VARCHAR(60) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'STAFF',  -- ADMIN / STAFF
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE product (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_code VARCHAR(60) NOT NULL UNIQUE,
  product_name VARCHAR(120) NOT NULL,
  price_per_night DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL DEFAULT 2,
  image_path VARCHAR(200),
  description VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ORDER 是保留字，所以表名用 orders
CREATE TABLE orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(40) NOT NULL UNIQUE,
  customer_id INT NOT NULL,
  check_in_date DATE NOT NULL,
  check_out_date DATE NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PAID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_orders_customer(customer_id),
  INDEX idx_orders_date(check_in_date, check_out_date),
  CHECK (check_in_date < check_out_date)
) ENGINE=InnoDB;

CREATE TABLE order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  unit_price_per_night DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES product(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_items_order(order_id),
  INDEX idx_items_product(product_id)
) ENGINE=InnoDB;

INSERT INTO employee(employee_no, employee_name, phone, address, username, password, role) VALUES
('E-ADMIN-001', 'System Admin', '0900-000-000', 'HQ', 'admin', 'admin123', 'ADMIN'),
('E-STAFF-001', 'Front Desk', '0900-111-111', 'Branch', 'staff', 'staff123', 'STAFF');

INSERT INTO customer(customer_no, customer_name, phone, address, username, password) VALUES
('C-0001', 'Demo Customer', '0912-345-678', 'Taipei', 'demo', 'demo123');

INSERT INTO product(product_code, product_name, price_per_night, stock, image_path, description) VALUES
('CLASSIC_KING_BED', '經典一大床', 3200, 2, '/images/classic_king_bed.jpg', 'Classic king bed room'),
('CLASSIC_TWIN_BEDS', '經典兩小床', 3200, 2, '/images/classic_twin_beds.jpg', 'Classic twin beds room'),
('TRIPLE_TWIN_SUITE', '三人雙床套房', 4200, 2, '/images/triple_twin_suite.jpg', 'Triple twin suite'),
('QUAD_TWIN_SUITE', '四人雙床套房', 5200, 2, '/images/quad_twin_suite.jpg', 'Quad twin suite'),
('SIX_TRIPLE_BED_SUITE', '六人三床套房', 6800, 2, '/images/six_triple_bed_suite.jpg', 'Six-person three-bed suite'),
('GARDEN_CABIN_DOUBLE_SUITE', '花園木屋兩人套房', 4500, 2, '/images/garden_cabin_double_suite.jpg', 'Garden cabin for two'),
('GARDEN_CABIN_QUAD_SUITE', '花園木屋四人套房', 5800, 2, '/images/garden_cabin_quad_suite.jpg', 'Garden cabin for four'),
('FOREST_CABIN_DOUBLE_SUITE', '森林木屋兩人套房', 4700, 2, '/images/forest_cabin_double_suite.jpg', 'Forest cabin for two'),
('FOREST_CABIN_QUAD_SUITE', '森林木屋四人套房', 6000, 2, '/images/forest_cabin_quad_suite.jpg', 'Forest cabin for four'),
('JAPANESE_CABIN_DOUBLE_SUITE', '日式木屋兩人套房', 4900, 2, '/images/japanese_cabin_double_suite.jpg', 'Japanese cabin for two'),
('JAPANESE_CABIN_QUAD_SUITE', '日式木屋四人套房', 6300, 2, '/images/japanese_cabin_quad_suite.jpg', 'Japanese cabin for four');
