// src/main/java/com/blockycraft/blockybounty/database/BountyDatabaseManager.java

package com.blockycraft.blockybounty.database;

import com.blockycraft.blockybounty.data.Bounty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BountyDatabaseManager {

    private Connection connection;

    public BountyDatabaseManager(String dbPath) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS bounties (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "setter_uuid TEXT NOT NULL," +
                "setter_name TEXT NOT NULL," +
                "target_uuid TEXT NOT NULL," +
                "target_name TEXT NOT NULL," +
                "amount INTEGER NOT NULL," +
                "created_at INTEGER NOT NULL" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBounty(Bounty bounty) {
        String sql = "INSERT INTO bounties (setter_uuid, setter_name, target_uuid, target_name, amount, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bounty.getSetterUUID());
            pstmt.setString(2, bounty.getSetterName());
            pstmt.setString(3, bounty.getTargetUUID());
            pstmt.setString(4, bounty.getTargetName());
            pstmt.setInt(5, bounty.getAmount());
            pstmt.setLong(6, bounty.getCreatedAt());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBounty(String setterUUID, String targetUUID) {
        String sql = "DELETE FROM bounties WHERE setter_uuid = ? AND target_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, setterUUID);
            pstmt.setString(2, targetUUID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Corrigido: remove todas as bounties para o target, sem se importar se setter está online!
    public void removeAllBountiesForPlayer(String targetUUID) {
        String sql = "DELETE FROM bounties WHERE target_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetUUID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Busca todas as bounties por alvo (mesmo se setter estiver offline!)
    public List<Bounty> getBountiesForTarget(String targetUUID) {
        List<Bounty> result = new ArrayList<Bounty>();
        String sql = "SELECT * FROM bounties WHERE target_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, targetUUID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(extractBounty(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Bounty> getBountiesBySetter(String setterUUID) {
        List<Bounty> result = new ArrayList<Bounty>();
        String sql = "SELECT * FROM bounties WHERE setter_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, setterUUID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(extractBounty(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Bounty> getAllBounties() {
        List<Bounty> result = new ArrayList<Bounty>();
        String sql = "SELECT * FROM bounties";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(extractBounty(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Bounty extractBounty(ResultSet rs) throws SQLException {
        return new Bounty(
                rs.getString("setter_uuid"),
                rs.getString("setter_name"),
                rs.getString("target_uuid"),
                rs.getString("target_name"),
                rs.getInt("amount"),
                rs.getLong("created_at")
        );
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
