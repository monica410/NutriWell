-- Users are created through POST /api/auth/signup.
-- Do not seed plain-text passwords in this file.

-- add one test user for demo
-- the password is testCS160!
INSERT IGNORE INTO Users (username, email, password_hash) VALUES
    ('test', 'test@gmail.com', '$2a$10$FY0lmPSSIvXAcBruBGkAv.ipzxiXvH4Qzm3ritMIcNmjPdnmwm6XW');

