CREATE TABLE IF NOT EXISTS token
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    token_type ENUM('BEARER') NOT NULL,
    revoked BOOLEAN NOT NULL,
    expired BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL,
    created_date TIMESTAMP(6) NOT NULL ,
    last_modified_date TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES _user(id)
);