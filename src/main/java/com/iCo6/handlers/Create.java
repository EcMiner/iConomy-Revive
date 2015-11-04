package com.iCo6.handlers;

import com.iCo6.command.Handler;
import com.iCo6.command.Parser.Argument;
import com.iCo6.command.exceptions.InvalidUsage;
import com.iCo6.iConomy;
import com.iCo6.system.Accounts;
import com.iCo6.system.events.AccountCreation;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.UUID;

public class Create extends Handler {

    private Accounts Accounts = new Accounts();

    public Create(iConomy plugin) {
        super(plugin, plugin.Template);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if (!hasPermissions(sender, "create"))
            throw new InvalidUsage("You do not have permission to do that.");

        String name = arguments.get("name").getStringValue();
        String tag = template.color(Template.Node.TAG_MONEY);

        if (name.equals("0"))
            throw new InvalidUsage("Missing <white>name<rose>: /money create <name>");

        UUID uuid = Bukkit.getPlayer(name) != null ? Bukkit.getPlayer(name).getUniqueId() : (Bukkit.getOfflinePlayer(name) != null ? Bukkit.getOfflinePlayer(name).getUniqueId() : null);
        if (uuid != null) {
            String pName = Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName();

            if (Accounts.exists(uuid)) {
                template.set(Template.Node.ERROR_EXISTS);
                Messaging.send(sender, tag + template.parse());
                return false;
            }

            if (!Accounts.create(uuid)) {
                template.set(Template.Node.ERROR_CREATE);
                template.add("name", pName);
                Messaging.send(sender, tag + template.parse());
                return false;
            }
            AccountCreation event = new AccountCreation(uuid);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                template.set(Template.Node.ACCOUNTS_CREATE);
                template.add("name", pName);
                Messaging.send(sender, tag + template.parse());
            }
        } else {
            template.set(Template.Node.ERROR_PLAYER);
            template.add("name", name);
            Messaging.send(sender, tag + template.parse());
            return false;
        }
        return false;
    }
}
