CREATE TABLE %1s (id INTEGER PRIMARY KEY,uuid varchar(36) NOT NULL,balance double(64,2) NOT NULL,status int(2) NOT NULL DEFAULT 0);
CREATE UNIQUE INDEX uuid ON iconomy (uuid);