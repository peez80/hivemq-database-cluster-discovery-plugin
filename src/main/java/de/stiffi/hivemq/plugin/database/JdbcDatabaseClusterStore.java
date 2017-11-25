package de.stiffi.hivemq.plugin.database;

import com.hivemq.spi.callback.cluster.ClusterNodeAddress;
import de.stiffi.hivemq.plugin.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter Class to store Cluster information in a JDBC database. Automatically reads database connection parameters
 * from Plugin configuration.
 */
public class JdbcDatabaseClusterStore {

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Configuration config;

    @Inject
    public JdbcDatabaseClusterStore(Configuration config) {
        this.config = config;
    }

    public void addClusterNode(String clusterId, ClusterNodeAddress ownAddress) {
        String sql = "INSERT INTO nodes (clusterId, host, port, lastSeen) VALUES (?, ?, ?, ?)";
        try (Connection conn = getDbConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, clusterId);
            pstmt.setString(2, ownAddress.getHost());
            pstmt.setInt(3, ownAddress.getPort());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void remove(String clusterId) {
        String sql = "DELETE FROM nodes WHERE clusterId = ?";
        try (Connection conn = getDbConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, clusterId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    public List<ClusterNodeAddress> getNodeAddresses() {

        long inactiveTimeoutMs = config.getInactiveTimeoutMs();
        LocalDateTime minLastSeen = LocalDateTime.now().minus(inactiveTimeoutMs, ChronoUnit.MILLIS);

        String sql = "SELECT host, port FROM nodes WHERE lastSeen > ?";
        try (Connection conn = getDbConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(minLastSeen));

            ResultSet rs = pstmt.executeQuery();
            List<ClusterNodeAddress> nodeAddresses = new ArrayList<>();
            while (rs.next()) {
                ClusterNodeAddress address = new ClusterNodeAddress(rs.getString("host"), rs.getInt("port"));
                nodeAddresses.add(address);
            }
            return nodeAddresses;

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    public void updateLastSeen(String clusterId) {
        String sql = "UPDATE nodes SET lastSeen = ? WHERE clusterId = ?";
        try (Connection conn = getDbConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, clusterId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }


    private Connection getDbConnection() throws SQLException {
        String jdbcUrl = config.getJdbcUrl();
        String user = config.getJdbcUser();
        String password = config.getJdbcPassword();
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

}
