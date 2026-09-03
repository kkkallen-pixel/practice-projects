DROP DATABASE IF EXISTS book_manager;
CREATE DATABASE book_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE book_manager;

DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS reader;

CREATE TABLE book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  isbn VARCHAR(32) NOT NULL UNIQUE COMMENT 'ISBN 编号',
  title VARCHAR(100) NOT NULL COMMENT '书名',
  author VARCHAR(50) NOT NULL COMMENT '作者',
  category VARCHAR(30) DEFAULT '' COMMENT '分类',
  total INT NOT NULL DEFAULT 1 COMMENT '馆藏总数',
  available INT NOT NULL DEFAULT 1 COMMENT '当前可借数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reader (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  card_no VARCHAR(30) NOT NULL UNIQUE COMMENT '读者证号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE borrow_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  reader_id BIGINT NOT NULL,
  borrow_date DATE NOT NULL,
  due_date DATE NOT NULL,
  return_date DATE NULL,
  CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book(id),
  CONSTRAINT fk_borrow_reader FOREIGN KEY (reader_id) REFERENCES reader(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO book (isbn, title, author, category, total, available) VALUES
('9787111213826', 'Java核心技术 卷I', 'Cay S. Horstmann', '编程', 3, 2),
('9787115428028', 'Python编程：从入门到实践', 'Eric Matthes', '编程', 2, 2),
('9787115471550', 'MySQL必知必会', 'Ben Forta', '数据库', 2, 2),
('9787302555575', 'Head First HTML与CSS', 'Elisabeth Robson', '前端', 1, 1);

INSERT INTO reader (name, phone, card_no) VALUES
('张三', '13800000001', 'R001'),
('李四', '13800000002', 'R002');

INSERT INTO borrow_record (book_id, reader_id, borrow_date, due_date, return_date) VALUES
(1, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), NULL);
