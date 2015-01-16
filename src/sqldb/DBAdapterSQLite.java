package sqldb;

import java.sql.*;

/**
 * Created by v1ar on 15.01.15.
 */
public class DBAdapterSQLite extends DBAdapter {

    public Connection connection;
    public final Statement statement;

    public DBAdapterSQLite(String serv) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(serv);
        statement = connection.createStatement();
    }

    @Override
    public int insert(String str) {
        synchronized (statement) {
            try {
                statement.execute(str);
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 1;
            }
        }
    }

    @Override
    public int delete(String str) {
        synchronized (statement) {
            try {
                statement.execute(str);
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 1;
            }
        }
    }

    @Override
    public ResultSet select(String str) {
        ResultSet resSet = null;
        synchronized (statement) {
            try {
                resSet = statement.executeQuery(str);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resSet;
    }

    @Override
    public int update(String str) {
        synchronized (statement) {
            try {
                statement.execute(str);
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 1;
            }
        }
    }
}
