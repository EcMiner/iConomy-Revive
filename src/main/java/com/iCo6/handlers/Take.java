package com.iCo6.handlers;

import com.iCo6.command.Handler;
import com.iCo6.command.Parser.Argument;
import com.iCo6.command.exceptions.InvalidUsage;
import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
import com.iCo6.system.events.HoldingsUpdate;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.UUID;

public class Take extends Handler {
    private Accounts Accounts = new Accounts();

    public Take(iConomy plugin) {
        super(plugin, plugin.Template);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if (!hasPermissions(sender, "take"))
            throw new InvalidUsage("You do not have permission to do that.");

        String name = arguments.get("name").getStringValue();
        String tag = template.color(Template.Node.TAG_MONEY);
        Double amount;

        if (name.equals("0"))
            throw new InvalidUsage("Missing name parameter: /money take <name> <amount>");

        if (arguments.get("amount").getStringValue().equals("empty"))
            throw new InvalidUsage("Missing amount parameter: /money take <name> <amount>");

        try {
            amount = arguments.get("amount").getDoubleValue();
        } catch (NumberFormatException e) {
            throw new InvalidUsage("Invalid amount parameter, must be double.");
        }

        if (Double.isInfinite(amount) || Double.isNaN(amount))
            throw new InvalidUsage("Invalid amount parameter, must be double.");

        UUID uuid = Bukkit.getPlayer(name) != null ? Bukkit.getPlayer(name).getUniqueId() : (Bukkit.getOfflinePlayer(name) != null ? Bukkit.getOfflinePlayer(name).getUniqueId() : null);
        if (uuid != null) {
            String pName = Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName();

            if (!Accounts.exists(uuid)) {
                template.set(Template.Node.ERROR_ACCOUNT);
                template.add("name", pName);

                Messaging.send(sender, tag + template.parse());
                return false;
            }

            Account account = new Account(uuid);

            HoldingsUpdate event = new HoldingsUpdate(uuid, account.getHoldings().getBalance(), account.getHoldings().getBalance() - amount, amount);
            Bukkit.getServer().getPluginManager().callEvent(event);

            account.getHoldings().subtract(amount);

            template.set(Template.Node.PLAYER_DEBIT);
            template.add("name", pName);
            template.add("amount", iConomy.format(amount));

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
