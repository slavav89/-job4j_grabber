package ru.job4j.grabber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement
                     = connection.prepareStatement("INSERT INTO post(name, text, link, created) "
                        + "VALUES (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM post")) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(newPost(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Post findById(int id) {
        Post result = null;
        try (PreparedStatement statement
                     = connection.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result = newPost(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Post newPost(ResultSet rs) throws SQLException {
        return new Post(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("text"),
                rs.getString("link"),
                rs.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}