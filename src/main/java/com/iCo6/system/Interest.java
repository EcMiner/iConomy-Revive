package com.iCo6.system;

import com.iCo6.Constants;
import com.iCo6.iConomy;
import com.iCo6.util.Template;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

public class Interest extends TimerTask {
    Template Template = null;

    public Interest(String directory) {
        Template = new Template(directory, "Messages.yml");
    }

    @Override
    public void run() {
        Accounts Accounts = new Accounts();
        DecimalFormat DecimalFormat = new DecimalFormat("#.##");
        List<UUID> players = new ArrayList<UUID>();
        LinkedHashMap<UUID, HashMap<String, Object>> queries = new LinkedHashMap<UUID, HashMap<String, Object>>();

        if (Constants.Nodes.InterestOnline.getBoolean()) {
            for (Player p : iConomy.Server.getOnlinePlayers()) {
                players.add(p.getUniqueId());
            }
        } else {
            players.addAll(Queried.accountList());
        }

        double cutoff = Constants.Nodes.InterestCutoff.getDouble(),
                percentage = Constants.Nodes.InterestPercentage.getDouble(),
                min = Constants.Nodes.InterestMin.getDouble(),
                max = Constants.Nodes.InterestMax.getDouble(),
                amount = 0.0;

        String table = Constants.Nodes.DatabaseTable.toString();
        String query = "UPDATE " + table + " SET balance = ? WHERE uuid = ?";

        if (percentage == 0.0) {
            try {
                if (min != max)
                    amount = Double.valueOf(DecimalFormat.format(Math.random() * (max - min) + (min)));
                else {
                    amount = max;
                }
            } catch (NumberFormatException e) {
                amount = max;
            }
        }

        for (UUID uuid : players) {
            if (!Accounts.exists(uuid))
                continue;

            Account account = new Account(uuid);
            Double balance = account.getHoldings().getBalance();

            if (cutoff > 0.0)
                if (balance >= cutoff)
                    continue;
                else if (cutoff < 0.0)
                    if (balance <= cutoff)
                        continue;

            LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

            if (percentage != 0.0)
                amount = Double.valueOf(DecimalFormat.format((percentage * balance) / 100));

            data.put("original", balance);
            data.put("balance", (balance + amount));
            queries.put(uuid, data);
        }

        if (queries.isEmpty())
            return;

        Queried.doInterest(query, queries);
    }
}
