CREATE TABLE IF NOT EXISTS backup_file (
    id BIGINT NOT NULL AUTO_INCREMENT,
    file_name VARCHAR(255),
    encrypted_content LONGTEXT NOT NULL,
    checksum VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id)
);
