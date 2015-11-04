package com.iCo6.system;

import com.iCo6.Constants;

import java.util.List;
import java.util.UUID;

public class Accounts {
    public Accounts() {
    }

    public boolean exists(UUID uuid) {
        return Queried.hasAccount(uuid);
    }

    public Account get(UUID uuid) {
        if (!Queried.hasAccount(uuid))
            this.create(uuid);

        return new Account(uuid);
    }

    public List<Account> getTopAccounts(int amount) {
        return Queried.topAccounts(amount);
    }

    public boolean create(UUID uuid) {
        return create(uuid, Constants.Nodes.Balance.getDouble());
    }

    public boolean create(UUID uuid, Double balance) {
        return create(uuid, balance, 0);
    }

    public boolean create(UUID uuid, Double balance, Integer status) {
        if (!Queried.hasAccount(uuid)) {
            return Queried.createAccount(uuid, balance, status);
        }
        return false;
    }

    public boolean remove(UUID... uuids) {
        Boolean success = false;

        for (UUID uuid : uuids)
            if (Queried.hasAccount(uuid)) {
                success = Queried.removeAccount(uuid);
            }


        return success;
    }

    public void purge() {
        Queried.purgeDatabase();
    }

    public void empty() {
        Queried.emptyDatabase();
    }
}