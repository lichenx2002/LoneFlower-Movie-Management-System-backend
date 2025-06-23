-- ========================
-- 1. 管理员表
-- 功能：存储影院管理人员的账号信息
-- ========================
CREATE TABLE admin (
                       admin_id INT PRIMARY KEY AUTO_INCREMENT,      -- 管理员ID（自增主键）
                       username VARCHAR(50) NOT NULL UNIQUE,        -- 登录用户名（不可重复）
                       password VARCHAR(255) NOT NULL,              -- 加密后的密码（建议使用bcrypt）
                       name VARCHAR(100),                           -- 管理员真实姓名（可选）
                       last_login DATETIME                          -- 最后登录时间（用于审计）
);

-- ========================
-- 2. 用户表
-- 功能：存储注册用户信息
-- ========================
CREATE TABLE user (
                      user_id INT PRIMARY KEY AUTO_INCREMENT,      -- 用户ID（自增主键）
                      username VARCHAR(50) NOT NULL UNIQUE,        -- 用户名（用于登录）
                      password VARCHAR(255) NOT NULL,              -- 加密密码
                      email VARCHAR(100) UNIQUE,                   -- 邮箱（用于找回密码）
                      phone VARCHAR(20),                           -- 手机号（可选）
                      balance DECIMAL(10, 2) DEFAULT 0.00,         -- 账户余额（默认0元）
                      reg_time DATETIME DEFAULT CURRENT_TIMESTAMP  -- 注册时间（自动记录）
);

-- ========================
-- 3. 电影表
-- 功能：存储电影基本信息
-- ========================
CREATE TABLE movie (
    <u>movie_id INT PRIMARY KEY AUTO_INCREMENT,     -- 电影ID（自增主键）</u>
     title VARCHAR(100) NOT NULL,                 -- 电影名称（必填）
     english_title VARCHAR(100),                  -- 电影英文名称（新增）
     director VARCHAR(100),                      -- 导演（可选）
     genres VARCHAR(100),                        -- 电影类型（逗号分隔，新增）
     actors VARCHAR(255),                        -- 主演列表（逗号分隔，如"张三,李四"）
     duration INT NOT NULL,                      -- 片长（分钟，用于计算结束时间）
     release_date DATE,                          -- 上映日期（用于新片推荐）
     release_location VARCHAR(100),              -- 上映地址/地区（新增）
     poster_url VARCHAR(255),                    -- 海报URL地址（前端显示用）
     trailer_url VARCHAR(255),                   -- 预告视频URL（新增）
     description TEXT,                           -- 剧情简介（长文本）
    <u>avg_rating DECIMAL(3,2) DEFAULT 0.00,       -- 平均评分（1-5分，根据评论计算）</u>
    box_office DECIMAL(15,2) DEFAULT 0.00,      -- 票房（单位：元，新增）
    want_to_watch BOOLEAN DEFAULT FALSE         -- 是否想看（新增）
    );

-- ========================
-- 4. 影厅表
-- 功能：存储放映厅的物理信息
-- ========================
CREATE TABLE hall (
    <u>hall_id INT PRIMARY KEY AUTO_INCREMENT,      -- 影厅ID（自增主键）</u>
     name VARCHAR(50) NOT NULL,                  -- 影厅名称（如"1号IMAX厅"）
     type ENUM('LARGE', 'MEDIUM', 'SMALL', 'LOVERS') NOT NULL, -- 影厅类型
     row_count INT NOT NULL,                     -- 总行数（如10排）
     col_count INT NOT NULL,                     -- 总列数（如20座/排）
     row_labels VARCHAR(26) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' -- 自定义排号字母（可跳过I等字母）
);


-- ========================
-- 5. 座位模板表（核心）
-- 功能：存储每个影厅的物理座位布局
-- 设计说明：与影厅是多对一关系，避免重复存储相同影厅的座位信息
-- ========================
CREATE TABLE seat_template (
    <u>template_id INT PRIMARY KEY AUTO_INCREMENT,  -- 座位模板ID（自增主键）</u>
     hall_id INT NOT NULL,                       -- 所属影厅ID
     row_label CHAR(1) NOT NULL,                 -- 排字母（A-Z）
     col_num INT NOT NULL,                       -- 座位号（1-99）
     seat_type ENUM('NORMAL', 'LOVER_LEFT', 'LOVER_RIGHT', 'VIP') DEFAULT 'NORMAL', -- 座位类型
     FOREIGN KEY (hall_id) REFERENCES hall(hall_id) ON DELETE CASCADE, -- 级联删除
     UNIQUE KEY (hall_id, row_label, col_num)    -- 防止重复座位
);

-- ========================
-- 6. 场次表（核心）
-- 功能：存储电影的排期信息
-- 设计说明：关联电影和影厅，包含价格策略
-- ========================
CREATE TABLE schedule (
                          schedule_id INT PRIMARY KEY AUTO_INCREMENT,  -- 场次ID（自增主键）
                          movie_id INT NOT NULL,                      -- 放映电影ID
                          hall_id INT NOT NULL,                       -- 使用影厅ID
                          start_time DATETIME NOT NULL,               -- 开始时间（精确到分钟）
                          end_time DATETIME NOT NULL,                 -- 结束时间（根据电影时长自动计算）
                          base_price DECIMAL(8,2) NOT NULL,           -- 基础票价（如45.00）
                          vip_price DECIMAL(8,2),                     -- VIP座位加价（如+10元）
                          lover_price DECIMAL(8,2),                   -- 情侣座价格（双人总价）
                          FOREIGN KEY (movie_id) REFERENCES movie(movie_id) ON DELETE CASCADE,
                          FOREIGN KEY (hall_id) REFERENCES hall(hall_id) ON DELETE CASCADE
);

-- ========================
-- 7. 场次座位状态表（最核心）
-- 功能：实现动态选座功能
-- 设计说明：每个场次+座位组合都有一条记录，状态实时变化
-- ========================
CREATE TABLE schedule_seat (
                               ss_id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 状态记录ID（自增主键）
                               schedule_id INT NOT NULL,                   -- 关联场次ID
                               template_id INT NOT NULL,                   -- 关联座位模板ID（非直接关联座位）
                               status ENUM('AVAILABLE', 'OCCUPIED', 'LOCKED') DEFAULT 'AVAILABLE', -- 三种状态
                               lock_time DATETIME,                         -- 锁定时间（选座后15分钟未支付自动释放）
                               user_id INT,                                -- 锁定用户ID（临时占用）
                               FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id) ON DELETE CASCADE,
                               FOREIGN KEY (template_id) REFERENCES seat_template(template_id) ON DELETE CASCADE,
                               FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

-- ========================
-- 8. 订单表（核心）
-- 功能：存储用户的购票订单
-- ========================
CREATE TABLE orders (
                        order_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 订单ID（自增主键）
                        user_id INT NOT NULL,                       -- 下单用户ID
                        schedule_id INT NOT NULL,                   -- 关联场次ID
                        total_amount DECIMAL(10,2) NOT NULL,        -- 订单总金额（可能含优惠）
                        order_time DATETIME DEFAULT CURRENT_TIMESTAMP, -- 下单时间（精确到秒）
                        status ENUM('UNPAID', 'PAID', 'CANCELED', 'REFUNDED') DEFAULT 'UNPAID', -- 订单状态
                        payment_id VARCHAR(100),                    -- 第三方支付ID（如支付宝交易号）
                        FOREIGN KEY (user_id) REFERENCES user(user_id),
                        FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id)
);

-- ========================
-- 9. 订单座位关联表
-- 功能：记录订单包含的具体座位
-- 设计说明：解决订单与座位的多对多关系
-- ========================
CREATE TABLE order_seat (
                            order_id BIGINT NOT NULL,                   -- 关联订单ID
                            ss_id BIGINT NOT NULL,                      -- 关联场次座位状态ID
                            actual_price DECIMAL(8,2) NOT NULL,         -- 实际售价（可能使用优惠券）
                            PRIMARY KEY (order_id, ss_id),              -- 联合主键
                            FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                            FOREIGN KEY (ss_id) REFERENCES schedule_seat(ss_id)
);

-- ========================
-- 10. 评论表
-- 功能：存储用户对电影的评分和评价
-- ========================
CREATE TABLE comment (
                         comment_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 评论ID（自增主键）
                         user_id INT NOT NULL,                        -- 评论用户ID
                         movie_id INT NOT NULL,                       -- 关联电影ID
                         content TEXT NOT NULL,                       -- 评论内容（前端做敏感词过滤）
                         rating TINYINT CHECK (rating BETWEEN 1 AND 5), -- 评分（1-5星）
                         comment_time DATETIME DEFAULT CURRENT_TIMESTAMP, -- 评论时间
                         FOREIGN KEY (user_id) REFERENCES user(user_id),
                         FOREIGN KEY (movie_id) REFERENCES movie(movie_id)
);

-- ========================
-- 11. 新增影院表
-- 功能：存储用户对电影的评分和评价
-- ========================
CREATE TABLE IF NOT EXISTS cinema (
                                      cinema_id INT PRIMARY KEY AUTO_INCREMENT,
                                      name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    description TEXT
    );

INSERT INTO cinema (cinema_id, name, address, phone, description)
VALUES (1, '默认影院', '默认地址', '000-00000000', '系统初始化影院')
    ON DUPLICATE KEY UPDATE name=name;

-- 3. 修改hall表，增加cinema_id字段，所有影厅默认绑定cinema_id=1
ALTER TABLE hall ADD COLUMN cinema_id INT NOT NULL DEFAULT 1;

-- 4. 增加外键约束
ALTER TABLE hall ADD CONSTRAINT fk_hall_cinema FOREIGN KEY (cinema_id) REFERENCES cinema(cinema_id) ON DELETE CASCADE;