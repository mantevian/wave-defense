package supercoder79.wavedefense.game;

import java.util.HashMap;

public class WdPlayer {
    private final HashMap<String, Integer> mobKills;
    private final HashMap<String, Integer> mobAssists;
    public int openedShopPage;
    public int totalXPLevels;
    public int upgradePoints;

    public WdPlayer() {
        mobKills = new HashMap<>();
        mobAssists = new HashMap<>();
        openedShopPage = 0;
        totalXPLevels = 0;
        upgradePoints = 0;
    }

    public void addMobKill(String name) {
        mobKills.merge(name, 1, Integer::sum);
    }

    public void addMobAssist(String name) {
        mobAssists.merge(name, 1, Integer::sum);
    }

    public String mobKillsToString() {
        StringBuilder s = new StringBuilder();
        if (mobKills.size() > 0) {
            s.append("Mob kills:");
        } else {
            s.append("No mob kills");
        }
        for (String k : mobKills.keySet()) {
            s.append("\n").append(k).append(": ").append(mobKills.get(k));
        }
        return s.toString();
    }

    public String mobAssistsToString() {
        StringBuilder s = new StringBuilder();
        if (mobAssists.size() > 0) {
            s.append("Mob kill assists:");
        } else {
            s.append("No mob kill assists");
        }
        for (String k : mobAssists.keySet()) {
            s.append("\n").append(k).append(": ").append(mobAssists.get(k));
        }
        return s.toString();
    }
}
