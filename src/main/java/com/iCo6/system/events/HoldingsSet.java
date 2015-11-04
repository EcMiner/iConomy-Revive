package com.iCo6.system.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class HoldingsSet extends Event {
    private final UUID account;
    private double balance;

    public HoldingsSet(UUID account, double balance) {
        this.account = account;
        this.balance = balance;
    }

    public UUID getAccount() {
        return account;
    }

    public double getBalance() {
        return balance;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
