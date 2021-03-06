package com.iCo6.handlers;

import com.iCo6.command.Handler;
import com.iCo6.command.Parser.Argument;
import com.iCo6.command.exceptions.InvalidUsage;
import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
import com.iCo6.system.events.HoldingsSet;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.UUID;

public class Set extends Handler {
    private Accounts Accounts = new Accounts();

    public Set(iConomy plugin) {
        super(plugin, plugin.Template);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if (!hasPermissions(sender, "set"))
            throw new InvalidUsage("You do not have permission to do that.");

        String name = arguments.get("name").getStringValue();
        String tag = template.color(Template.Node.TAG_MONEY);
        Double amount;

        if (name.equals("0"))
            throw new InvalidUsage("Missing <white>name<rose>: /money set <name> <amount>");

        if (arguments.get("amount").getStringValue().equals("empty"))
            throw new InvalidUsage("Missing <white>amount<rose>: /money set <name> <amount>");

        try {
            amount = arguments.get("amount").getDoubleValue();
        } catch (NumberFormatException e) {
            throw new InvalidUsage("Invalid <white>amount<rose>, must be double.");
        }

        if (Double.isInfinite(amount) || Double.isNaN(amount))
            throw new InvalidUsage("Invalid <white>amount<rose>, must be double.");

        UUID uuid = Bukkit.getPlayer(name) != null ? Bukkit.getPlayer(name).getUniqueId() : (Bukkit.getOfflinePlayer(name) != null ? Bukkit.getOfflinePlayer(name).getUniqueId() : null);
        if (uuid != null) {
            String pName = Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName();

            if (!Accounts.exists(uuid)) {
                template.set(Template.Node.ERROR_ACCOUNT);
                template.add("name", name);

                Messaging.send(sender, tag + template.parse());
                return false;
            }
            HoldingsSet event = new HoldingsSet(uuid, amount);
            Bukkit.getServer().getPluginManager().callEvent(event);
            Account account = new Account(uuid);
            account.getHoldings().setBalance(amount);

            template.set(Template.Node.PLAYER_SET);
            template.add("name", pName);
            template.add("amount", account.getHoldings().toString());

            Messaging.send(sender, tag + template.parse());
        } else {
            template.set(Template.Node.ERROR_PLAYER);
            template.add("name", name);
            Messaging.send(sender, tag + template.parse());
            return false;
        }
        return false;
    }
}
