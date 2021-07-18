package supercoder79.wavedefense.entity;

import net.minecraft.util.math.Vec3d;
import supercoder79.wavedefense.entity.monster.classes.MonsterClass;
import supercoder79.wavedefense.game.WdActive;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.HashMap;

public interface WaveEntity {
    default int ironCount() {
        return this.getMonsterClass().ironCount() + this.getMod().ironBonus;
    }
    default int goldCount() {
        return this.getMonsterClass().goldCount();
    }
    default int emeraldCount() {
        return this.getMonsterClass().emeraldCount();
    }
    default int xp() {
        return this.getMonsterClass().xp();
    }

    default int monsterScore() {
        return this.getMonsterClass().monsterScore();
    }

    MonsterClass getMonsterClass();

    void setMod(MonsterModifier monsterModifier);

    MonsterModifier getMod();

    Vec3d pos = Vec3d.ZERO;

    WdActive getGame();

    default boolean showHealth() {
        return false;
    }

    HashMap<PlayerRef, Integer> contributedPlayers = new HashMap<>();

    default void addContributedPlayer(PlayerRef ref, int i) {
        contributedPlayers.merge(ref, i, Integer::sum);
    }

    default int totalContribution() {
        int accumulatedContribution = 0;
        for (int i : contributedPlayers.values()) {
            accumulatedContribution += i;
        }
        return accumulatedContribution;
    }

    default int getContribution(PlayerRef ref) {
        if (contributedPlayers.get(ref) == null) {
            return 0;
        }
        return contributedPlayers.get(ref);
    }
}
