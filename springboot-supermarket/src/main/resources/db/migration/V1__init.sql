-- ============================================================
-- 邻里小超社区电商系统 · 15 张表
-- MySQL 8 / H2(MySQL 兼容模式) 均可执行；全部 IF NOT EXISTS 可重复启动
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    gender TINYINT DEFAULT 0,
    points INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    real_name VARCHAR(50),
    role VARCHAR(20),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    icon VARCHAR(255),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT,
    name VARCHAR(100),
    subtitle VARCHAR(200),
    code VARCHAR(50),
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    original_price DECIMAL(10,2) DEFAULT 0.00,
    stock INT DEFAULT 0,
    stock_alert INT DEFAULT 10,
    sales INT DEFAULT 0,
    unit VARCHAR(20),
    spec VARCHAR(100),
    image VARCHAR(255),
    images TEXT,
    description TEXT,
    nutrition TEXT,
    sort INT DEFAULT 0,
    is_hot TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT idx_product_category FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS cart (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT,
    quantity INT DEFAULT 1,
    selected TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS address (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    receiver VARCHAR(50),
    phone VARCHAR(20),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail VARCHAR(200),
    is_default TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    type TINYINT,
    discount DECIMAL(10,2) DEFAULT 0.00,
    threshold DECIMAL(10,2) DEFAULT 0.00,
    valid_type TINYINT DEFAULT 1,
    start_time DATETIME,
    end_time DATETIME,
    valid_days INT DEFAULT 30,
    scope TINYINT DEFAULT 1,
    total INT DEFAULT 0,
    issued INT DEFAULT 0,
    new_user_auto TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    coupon_id BIGINT,
    status TINYINT DEFAULT 0,
    get_time DATETIME,
    use_time DATETIME,
    order_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT idx_uc_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT idx_uc_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(id)
);

CREATE TABLE IF NOT EXISTS points_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    type TINYINT,
    points INT,
    balance INT,
    source VARCHAR(100),
    order_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) UNIQUE,
    user_id BIGINT,
    address_id BIGINT,
    receiver VARCHAR(50),
    phone VARCHAR(20),
    address_detail VARCHAR(255),
    goods_amount DECIMAL(10,2) DEFAULT 0.00,
    delivery_fee DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    pay_amount DECIMAL(10,2) DEFAULT 0.00,
    coupon_id BIGINT,
    user_coupon_id BIGINT,
    pay_type VARCHAR(20),
    status TINYINT DEFAULT 0,
    deliveryman_id BIGINT,
    pay_time DATETIME,
    delivery_time DATETIME,
    finish_time DATETIME,
    cancel_time DATETIME,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    product_id BIGINT,
    product_name VARCHAR(100),
    product_image VARCHAR(255),
    price DECIMAL(10,2) DEFAULT 0.00,
    quantity INT DEFAULT 1,
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT idx_item_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE IF NOT EXISTS review (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT,
    order_id BIGINT,
    rating INT DEFAULT 5,
    content TEXT,
    images TEXT,
    status TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS delivery_area (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50),
    type TINYINT,
    enabled TINYINT DEFAULT 1,
    hot TINYINT DEFAULT 0,
    sort INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS deliveryman (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS store_setting (
    id BIGINT NOT NULL PRIMARY KEY,
    store_name VARCHAR(100),
    slogan VARCHAR(200),
    notice VARCHAR(500),
    delivery_fee DECIMAL(10,2) DEFAULT 5.00,
    free_delivery_threshold DECIMAL(10,2) DEFAULT 59.00,
    service_phone VARCHAR(20),
    logo VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 门店设置默认行（固定 id=1），兼容 MySQL 与 H2
INSERT INTO store_setting (id, store_name, slogan, notice, delivery_fee, free_delivery_threshold, service_phone, logo)
SELECT 1, '邻里小超·社区优选', '新鲜到家，邻里共享', '本店支持当日达，满59元免配送费', 5.00, 59.00, '400-800-1234', ''
WHERE NOT EXISTS (SELECT 1 FROM store_setting WHERE id = 1);
