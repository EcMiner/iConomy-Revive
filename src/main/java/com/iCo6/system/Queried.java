package com.iCo6.system;

import com.iCo6.Constants;
import com.iCo6.IO.InventoryDB;
import com.iCo6.IO.mini.Arguments;
import com.iCo6.IO.mini.Mini;
import com.iCo6.iConomy;
import com.iCo6.util.Thrun;
import com.iCo6.util.org.apache.commons.dbutils.DbUtils;
import com.iCo6.util.org.apache.commons.dbutils.QueryRunner;
import com.iCo6.util.org.apache.commons.dbutils.ResultSetHandler;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Controls all account actions as well as SQL queries.
 *
 * @author Nijikokun
 */
public class Queried {
    static Mini database;
    static InventoryDB inventory;

    static ResultSetHandler<String> returnName = new ResultSetHandler<String>() {
        public String handle(ResultSet rs) throws SQLException {
            if (rs.next())
                return rs.getString("name");

            return null;
        }
    };

    static ResultSetHandler<List<UUID>> returnList = new ResultSetHandler<List<UUID>>() {
        private List<UUID> accounts;

        public List<UUID> handle(ResultSet rs) throws SQLException {
            accounts = new ArrayList<UUID>();

            while (rs.next())
                accounts.add(UUID.fromString(rs.getString("uuid")));

            return accounts;
        }
    };


    static ResultSetHandler<Boolean> returnBoolean = new ResultSetHandler<Boolean>() {
        public Boolean handle(ResultSet rs) throws SQLException {
            return rs.next();
        }
    };

    static ResultSetHandler<Double> returnBalance = new ResultSetHandler<Double>() {
        public Double handle(ResultSet rs) throws SQLException {
            if (rs.next()) return rs.getDouble("balance");
            return null;
        }
    };

    static ResultSetHandler<Integer> returnStatus = new ResultSetHandler<Integer>() {
        public Integer handle(ResultSet rs) throws SQLException {
            if (rs.next()) return rs.getInt("status");
            return null;
        }
    };

    static boolean useOrbDB() {
        if (!iConomy.Database.getType().toString().equalsIgnoreCase("orbdb"))
            return false;

        if (database == null)
            database = iConomy.Database.getDatabase();

        return true;
    }

    static boolean useMiniDB() {
        if (!iConomy.Database.getType().toString().equalsIgnoreCase("minidb"))
            return false;

        if (database == null)
            database = iConomy.Database.getDatabase();

        return true;
    }

    static boolean useInventoryDB() {
        if (!iConomy.Database.getType().toString().equalsIgnoreCase("inventorydb"))
            return false;

        if (inventory == null)
            inventory = iConomy.Database.getInventoryDatabase();

        if (database == null)
            database = iConomy.Database.getDatabase();

        return true;
    }

    static List<UUID> accountList() {
        List<UUID> accounts = new ArrayList<UUID>();

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            if (useInventoryDB())
                accounts.addAll(inventory.getAllPlayers());

            if (useOrbDB())
                for (Player p : iConomy.Server.getOnlinePlayers())
                    accounts.add(p.getUniqueId());

            for (String key : database.getIndices().keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    accounts.add(uuid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return accounts;
        }

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                accounts = run.query(c, "SELECT uuid FROM " + t, returnList);
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }

        return accounts;
    }

    static List<Account> topAccounts(int amount) {
        Accounts Accounts = new Accounts();
        List<Account> accounts = new ArrayList<Account>();
        List<Account> finals = new ArrayList<Account>();
        List<UUID> total = new ArrayList<UUID>();

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            if (useInventoryDB())
                total.addAll(inventory.getAllPlayers());

            if (useOrbDB())
                for (Player p : iConomy.Server.getOnlinePlayers())
                    total.add(p.getUniqueId());

            for (String key : database.getIndices().keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    total.add(uuid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                QueryRunner run = new QueryRunner();
                Connection c = iConomy.Database.getConnection();

                try {
                    String t = Constants.Nodes.DatabaseTable.toString();
                    total = run.query(c, "SELECT uuid FROM " + t + " WHERE status <> 1 ORDER BY balance DESC LIMIT " + amount, returnList);
                } catch (SQLException ex) {
                    System.out.println("[iConomy] Error issueing SQL query: " + ex);
                } finally {
                    DbUtils.close(c);
                }
            } catch (SQLException ex) {
                System.out.println("[iConomy] Database Error: " + ex);
            }
        }

        for (Iterator<UUID> it = total.iterator(); it.hasNext(); ) {
            UUID uuid = it.next();
            if (useMiniDB() || useInventoryDB() || useOrbDB()) {
                accounts.add(Accounts.get(uuid));
            } else {
                finals.add(new Account(uuid));
            }
        }

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            Collections.sort(accounts, new MoneyComparator());

            if (amount > accounts.size())
                amount = accounts.size();

            for (int i = 0; i < amount; i++) {
                if (accounts.get(i).getStatus() == 1) {
                    i--;
                    continue;
                }

                finals.add(accounts.get(i));
            }
        }

        return finals;
    }

    static boolean createAccount(UUID uuid, Double balance, Integer status) {
        Boolean created = false;

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            if (hasAccount(uuid))
                return false;

            if (useOrbDB())
                if (iConomy.Server.getPlayer(uuid) != null)
                    return false;

            if (useInventoryDB())
                if (inventory.dataExists(uuid))
                    return false;

            Arguments Row = new Arguments(uuid);
            Row.setValue("balance", balance);
            Row.setValue("status", status);

            database.addIndex(Row.getKey(), Row);
            database.update();

            return true;
        }

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                Integer amount = run.update(c, "INSERT INTO " + t + "(uuid, balance, status) values (?, ?, ?)", uuid.toString(), balance, status);

                if (amount > 0)
                    created = true;
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }

        return false;
    }

    static boolean removeAccount(UUID uuid) {
        Boolean removed = false;

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            if (database.hasIndex(uuid)) {
                database.removeIndex(uuid);
                database.update();

                return true;
            }

            return false;
        }

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                Integer amount = run.update(c, "DELETE FROM " + t + " WHERE uuid=?", uuid.toString());

                if (amount > 0)
                    removed = true;
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }

        return removed;
    }

    static boolean hasAccount(UUID uuid) {
        Boolean exists = false;

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            if (useInventoryDB())
                if (inventory.dataExists(uuid))
                    return true;

            if (useOrbDB())
                if (iConomy.Server.getPlayer(uuid) != null)
                    return true;

            return database.hasIndex(uuid);
        }

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                exists = run.query(c, "SELECT id FROM " + t + " WHERE uuid=?", returnBoolean, uuid.toString());
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }

        return exists;
    }

    static double getBalance(UUID uuid) {
        Double balance = Constants.Nodes.Balance.getDouble();

        if (!hasAccount(uuid))
            return balance;

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            if (useInventoryDB())
                if (inventory.dataExists(uuid))
                    return inventory.getBalance(uuid);

            if (useOrbDB())
                if (iConomy.Server.getPlayer(uuid) != null)
                    return iConomy.Server.getPlayer(uuid).getTotalExperience();

            if (database.hasIndex(uuid))
                return database.getArguments(uuid).getDouble("balance");

            return balance;
        }

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                balance = run.query(c, "SELECT balance FROM " + t + " WHERE uuid=?", returnBalance, uuid.toString());
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }

        return balance;
    }

    static void setBalance(UUID uuid, double balance) {
        double original = 0.0, gain = 0.0, loss = 0.0;

        if (Constants.Nodes.Logging.getBoolean()) {
            original = getBalance(uuid);
            gain = balance - original;
            loss = original - balance;
        }

        if (!hasAccount(uuid)) {
            createAccount(uuid, balance, 0);

            if (Constants.Nodes.Logging.getBoolean()) {
                if (gain < 0.0) gain = 0.0;
                if (loss < 0.0) loss = 0.0;

                Transactions.insert(
                        new Transaction(
                                "setBalance", null, uuid
                        ).
                                setFromBalance(original).
                                setToBalance(balance).
                                setGain(gain).
                                setLoss(loss).
                                setSet(balance)
                );
            }

            return;
        }

        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            if (useInventoryDB())
                if (inventory.dataExists(uuid)) {
                    inventory.setBalance(uuid, balance);
                    return;
                }

            if (useOrbDB()) {
                Player gainer = iConomy.Server.getPlayer(uuid);

                if (gainer != null)
                    if (balance < gainer.getTotalExperience()) {
                        int amount = (int) (gainer.getTotalExperience() - balance);
                        for (int i = 0; i < amount; i++) {
                            if (gainer.getExp() > 0)
                                gainer.setExp(gainer.getExp() - 1);
                            else if (gainer.getTotalExperience() > 0)
                                gainer.setTotalExperience(gainer.getTotalExperience() - 1);
                            else
                                break;
                        }
                    } else {
                        int amount = (int) (balance - gainer.getTotalExperience());

                        for (int i = 0; i < amount; i++)
                            gainer.setExp(gainer.getExp() + 1);
                    }

                return;
            }

            if (database.hasIndex(uuid)) {
                database.setArgument(uuid.toString(), "balance", balance);
                database.update();
            }


            return;
        }

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                int update = run.update(c, "UPDATE " + t + " SET balance=? WHERE uuid=?", balance, uuid.toString());
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }
    }

    static void doInterest(final String query, LinkedHashMap<UUID, HashMap<String, Object>> queries) {
        final Object[][] parameters = new Object[queries.size()][2];

        int i = 0;
        for (UUID uuid : queries.keySet()) {
            double balance = (Double) queries.get(uuid).get("balance");
            double original = 0.0, gain = 0.0, loss = 0.0;

            if (Constants.Nodes.Logging.getBoolean()) {
                original = getBalance(uuid);
                gain = balance - original;
                loss = original - balance;
            }

            // We are using a query for MySQL
            if (!useInventoryDB() && !useMiniDB() && !useOrbDB()) {
                parameters[i][0] = balance;
                parameters[i][1] = uuid.toString();

                i++;
            } else if (useMiniDB()) {
                if (!hasAccount(uuid))
                    continue;

                database.setArgument(uuid.toString(), "balance", balance);
                database.update();
            } else if (useInventoryDB()) {
                if (inventory.dataExists(uuid))
                    inventory.setBalance(uuid, balance);
                else if (database.hasIndex(uuid)) {
                    database.setArgument(uuid.toString(), "balance", balance);
                    database.update();
                }
            } else if (useOrbDB()) {
                if (!hasAccount(uuid))
                    continue;

                Player gainer = iConomy.Server.getPlayer(uuid);

                if (gainer != null)
                    setBalance(uuid, balance);
            }

            if (Constants.Nodes.Logging.getBoolean()) {
                if (gain < 0.0) gain = 0.0;
                if (loss < 0.0) loss = 0.0;

                Transactions.insert(
                        new Transaction(
                                "Interest", null, uuid
                        ).
                                setFromBalance(original).
                                setToBalance(balance).
                                setGain(gain).
                                setLoss(loss).
                                setSet(balance)
                );
            }
        }

        if (!useInventoryDB() && !useMiniDB() && !useOrbDB())
            Thrun.init(new Runnable() {
                public void run() {
                    try {
                        QueryRunner run = new QueryRunner();
                        Connection c = iConomy.Database.getConnection();

                        try {
                            run.batch(c, query, parameters);
                        } catch (SQLException ex) {
                            System.out.println("[iConomy] Error with batching: " + ex);
                        } finally {
                            DbUtils.close(c);
                        }
                    } catch (SQLException ex) {
                        System.out.println("[iConomy] Database Error: " + ex);
                    }
                }
            });
    }

    public static void purgeDatabase() {
        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            for (String index : database.getIndices().keySet()) {
                if (database.getArguments(index).getDouble("balance") == Constants.Nodes.Balance.getDouble())
                    database.removeIndex(index);
            }

            database.update();

            if (useInventoryDB())
                for (Player p : iConomy.Server.getOnlinePlayers())
                    if (inventory.dataExists(p.getUniqueId()) && inventory.getBalance(p.getUniqueId()) == Constants.Nodes.Balance.getDouble())
                        inventory.setBalance(p.getUniqueId(), 0);

            if (useOrbDB())
                for (Player p : iConomy.Server.getOnlinePlayers())
                    if (p.getExp() == Constants.Nodes.Balance.getDouble())
                        p.setExp(0);

            return;
        }

        Thrun.init(new Runnable() {
            public void run() {
                try {
                    QueryRunner run = new QueryRunner();
                    Connection c = iConomy.Database.getConnection();

                    try {
                        String t = Constants.Nodes.DatabaseTable.toString();
                        Integer amount = run.update(c, "DELETE FROM " + t + " WHERE balance=?", Constants.Nodes.Balance.getDouble());
                    } catch (SQLException ex) {
                        System.out.println("[iConomy] Error issueing SQL query: " + ex);
                    } finally {
                        DbUtils.close(c);
                    }
                } catch (SQLException ex) {
                    System.out.println("[iConomy] Database Error: " + ex);
                }
            }
        });
    }

    static void emptyDatabase() {
        if (useMiniDB() || useInventoryDB() || useOrbDB()) {
            for (String index : database.getIndices().keySet())
                database.removeIndex(index);

            database.update();

            if (useInventoryDB())
                for (Player p : iConomy.Server.getOnlinePlayers())
                    if (inventory.dataExists(p.getUniqueId()))
                        inventory.setBalance(p.getUniqueId(), 0);

            if (useOrbDB())
                for (Player p : iConomy.Server.getOnlinePlayers())
                    p.setExp(0);

            return;
        }

        Thrun.init(new Runnable() {
            public void run() {
                try {
                    QueryRunner run = new QueryRunner();
                    Connection c = iConomy.Database.getConnection();

                    try {
                        String t = Constants.Nodes.DatabaseTable.toString();
                        Integer amount = run.update(c, "TRUNCATE TABLE " + t);
                    } catch (SQLException ex) {
                        System.out.println("[iConomy] Error issueing SQL query: " + ex);
                    } finally {
                        DbUtils.close(c);
                    }
                } catch (SQLException ex) {
                    System.out.println("[iConomy] Database Error: " + ex);
                }
            }
        });
    }

    static Integer getStatus(UUID uuid) {
        int status = 0;

        if (!hasAccount(uuid))
            return -1;

        if (useMiniDB())
            return database.getArguments(uuid).getInteger("status");

        if (useInventoryDB())
            return (inventory.dataExists(uuid)) ? 1 : (database.hasIndex(uuid)) ? database.getArguments(uuid).getInteger("status") : 0;

        if (useOrbDB())
            return (iConomy.Server.getPlayer(uuid) != null) ? 1 : (database.hasIndex(uuid)) ? database.getArguments(uuid).getInteger("status") : 0;

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                status = run.query(c, "SELECT status FROM " + t + " WHERE uuid=?", returnStatus, uuid.toString());
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }

        return status;
    }

    static void setStatus(UUID uuid, int status) {
        if (!hasAccount(uuid))
            return;

        if (useMiniDB()) {
            database.setArgument(uuid.toString(), "status", status);
            database.update();

            return;
        }

        if (useInventoryDB() || useOrbDB()) {
            if (database.hasIndex(uuid)) {
                database.setArgument(uuid.toString(), "status", status);
                database.update();
            }

            return;
        }

        try {
            QueryRunner run = new QueryRunner();
            Connection c = iConomy.Database.getConnection();

            try {
                String t = Constants.Nodes.DatabaseTable.toString();
                int update = run.update(c, "UPDATE " + t + " SET status=? WHERE username=?", status, uuid.toString());
            } catch (SQLException ex) {
                System.out.println("[iConomy] Error issueing SQL query: " + ex);
            } finally {
                DbUtils.close(c);
            }
        } catch (SQLException ex) {
            System.out.println("[iConomy] Database Error: " + ex);
        }
    }
}

class MoneyComparator implements Comparator<Account> {
    public int compare(Account a, Account b) {
        return (int) (b.getHoldings().getBalance() - a.getHoldings().getBalance());
    }
}