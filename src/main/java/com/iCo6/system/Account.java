package com.iCo6.system;

import com.iCo6.iConomy;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Account {
    public UUID uuid;

    public Account(UUID uuid) {
        this.uuid = uuid;
    }

    public Account(UUID uuid, Boolean create) {
        this.uuid = uuid;
    }

    public void showHoldings(boolean console) {
        if (console)
            return;

        Player player = iConomy.Server.getPlayer(uuid);
        if (iConomy.Server.getPlayer(uuid) == null)
            return;

        String tag = iConomy.Template.color(Template.Node.TAG_MONEY);

        Template template = iConomy.Template;
        template.set(Template.Node.PERSONAL_BALANCE);
        template.add("balance", getHoldings().getBalance());

        Messaging.send(player, tag + template.parse());
    }

    public Holdings getHoldings() {
        return new Holdings(this.uuid);
    }

    public Integer getStatus() {
        return Queried.getStatus(this.uuid);
    }

    public void setStatus(int status) {
        Queried.setStatus(this.uuid, status);
    }

    public boolean remove() {
        return Queried.removeAccount(this.uuid);
    }

    @Override
    public String toString() {
        String tag = iConomy.Template.raw(Template.Node.TAG_MONEY);

        Template template = iConomy.Template;
        template.set(Template.Node.PLAYER_BALANCE);
        template.add("uuid", uuid);
        template.add("balance", getHoldings().getBalance());

        return tag + template.parseRaw();
    }
}
