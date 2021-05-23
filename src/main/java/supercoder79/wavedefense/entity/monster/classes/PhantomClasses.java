package supercoder79.wavedefense.entity.monster.classes;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.text.LiteralText;
import supercoder79.wavedefense.entity.MonsterModifier;

import java.util.Random;

public class PhantomClasses {
    public static final PhantomClass DEFAULT = new PhantomClass() {
        @Override
        public void apply(MobEntity entity, MonsterModifier mod, Random random, int waveOrdinal) {

        }
        @Override
        public int ironCount() {
            return 4;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public int monsterScore() {
            return 3;
        }

        @Override
        public int xp() {
            return 2;
        }

        @Override
        public String name() {
            return "Phantom";
        }
    };

    public static final PhantomClass LARGE = new PhantomClass() {
        @Override
        public void apply(MobEntity entity, MonsterModifier mod, Random random, int waveOrdinal) {

        }

        @Override
        public int ironCount() {
            return 12;
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public int monsterScore() {
            return 5;
        }

        @Override
        public int xp() {
            return 4;
        }

        @Override
        public String name() {
            return "Nightmare";
        }
    };
}
