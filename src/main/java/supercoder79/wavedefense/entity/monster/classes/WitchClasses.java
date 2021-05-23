package supercoder79.wavedefense.entity.monster.classes;

import net.minecraft.entity.mob.MobEntity;
import supercoder79.wavedefense.entity.MonsterModifier;

import java.util.Random;

public class WitchClasses {
    public static final MonsterClass DEFAULT = new MonsterClass() {
        @Override
        public void apply(MobEntity entity, MonsterModifier mod, Random random, int waveOrdinal) {

        }

        @Override
        public int emeraldCount() {
            return 2 + new Random().nextInt(2);
        }

        @Override
        public int monsterScore() {
            return 10;
        }

        @Override
        public int xp() {
            return 3;
        }

        @Override
        public String name() {
            return "Witch";
        }

        @Override
        public double maxHealth() {
            return 36;
        }
    };
}
