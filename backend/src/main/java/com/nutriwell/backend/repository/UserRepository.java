package com.nutriwell.backend.repository;

import com.nutriwell.backend.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByUsernameOrEmail(String username, String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Users WHERE username = ? OR email = ?",
                Integer.class,
                username,
                email
        );
        return count != null && count > 0;
    }

    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password_hash FROM Users WHERE username = ?",
                (rs, rowNum) -> new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                ),
                username
        ).stream().findFirst();
    }

    public User save(String username, String email, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Users (username, email, password_hash) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return new User(key == null ? null : key.intValue(), username, email, passwordHash);
    }
}
