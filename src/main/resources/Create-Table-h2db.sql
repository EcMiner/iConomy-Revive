CREATE TABLE %1s (
    id INT auto_increment PRIMARY KEY,
    uuid VARCHAR(36) UNIQUE,
    balance DECIMAL (64, 2),
    status INT(2) DEFAULT 0
);
