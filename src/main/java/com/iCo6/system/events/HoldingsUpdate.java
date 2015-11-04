package com.iCo6.system.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class HoldingsUpdate extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final UUID account;
    private double balance;
    private double previous;
    private double amount;
    private boolean cancelled = false;

    public HoldingsUpdate(UUID account, double previous, double balance, double amount) {
        this.account = account;
        this.previous = previous;
        this.balance = balance;
        this.amount = amount;
    }

    public UUID getAccount() {
        return account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        this.balance = previous + amount;
    }

    public double getPrevious() {
        return previous;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
