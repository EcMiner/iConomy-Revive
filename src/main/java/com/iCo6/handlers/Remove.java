package com.iCo6.handlers;

import com.iCo6.command.Handler;
import com.iCo6.command.Parser.Argument;
import com.iCo6.command.exceptions.InvalidUsage;
import com.iCo6.iConomy;
import com.iCo6.system.Accounts;
import com.iCo6.system.events.AccountRemoval;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.UUID;

public class Remove extends Handler {
    private Accounts Accounts = new Accounts();

    public Remove(iConomy plugin) {
        super(plugin, plugin.Template);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if (!hasPermissions(sender, "remove"))
            throw new InvalidUsage("You do not have permission to do that.");

        String name = arguments.get("name").getStringValue();
        String tag = template.color(Template.Node.TAG_MONEY);

        if (name.equals("0"))
            throw new InvalidUsage("Missing <white>name<rose>: /money remove <name>");

        UUID uuid = Bukkit.getPlayer(name) != null ? Bukkit.getPlayer(name).getUniqueId() : (Bukkit.getOfflinePlayer(name) != null ? Bukkit.getOfflinePlayer(name).getUniqueId() : null);
        if (uuid != null) {
            String pName = Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName();
            if (!Accounts.exists(uuid)) {
                template.set(Template.Node.ERROR_ACCOUNT);
                template.add("name", pName);
                Messaging.send(sender, tag + template.parse());
                return false;
            }

            if (!Accounts.remove(uuid)) {
                template.set(Template.Node.ERROR_CREATE);
                template.add("name", pName);
                Messaging.send(sender, tag + template.parse());
                return false;
            }
            AccountRemoval event = new AccountRemoval(uuid);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                template.set(Template.Node.ACCOUNTS_REMOVE);
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
