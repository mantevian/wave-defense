package supercoder79.wavedefense.game;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

import supercoder79.wavedefense.entity.ZombieClass;
import supercoder79.wavedefense.entity.ZombieClasses;
import supercoder79.wavedefense.entity.ZombieModifier;
import supercoder79.wavedefense.entity.WaveDrownedEntity;
import supercoder79.wavedefense.entity.WaveZombieEntity;

import java.util.Random;

public final class WdWaveSpawner {
    // Magic values for finding faraway players
    // sqrt2/2 works better with larger numbers... perhaps we need a smarter way of calculating these?
    private static final double SQRT3_2 = Math.sqrt(3) / 2.0;
    private static final double SQRT2_2 = Math.sqrt(2) / 2.0;
    private static final long SPAWN_TICKS = 20 * 5;

    private final WdActive game;
    private final WdWave wave;

    private final long startTime;
    private int spawnedZombies;

    WdWaveSpawner(WdActive game, WdWave wave) {
        this.game = game;
        this.wave = wave;

        this.startTime = game.space.getWorld().getTime();
    }

    public boolean tick(long time) {
        long timeSinceStart = time - startTime;
        int targetZombies = Math.min((int) (timeSinceStart * wave.totalZombies / SPAWN_TICKS), wave.totalZombies);

        if (targetZombies > spawnedZombies) {
            ServerWorld world = game.space.getWorld();
            Vec3d centerPos = game.guide.getCenterPos();
            Random random = new Random();

            WeightedList<Position> validCenters = new WeightedList<>();
            validCenters.add(centerPos, this.game.getParticipants().size() * 100);

            for (ServerPlayerEntity participant : this.game.getParticipants()) {
                BlockPos pos = participant.getBlockPos();
                double aX = pos.getX() - centerPos.getX();
                double aZ = pos.getZ() - centerPos.getZ();
                double dist = (aX * aX) + (aZ * aZ);

                double threshold = game.config.spawnRadius * SQRT2_2;

                if (dist * dist >= threshold * threshold) {
                    validCenters.add(participant.getPos(), (int) (getDistWeight(dist - threshold) * 100));
                }
            }

            for (int i = spawnedZombies; i < targetZombies; i++) {
                Position chosenPos = validCenters.pickRandom(random);

                // Spawn zombies closer to faraway players
                // TODO: some randomization here
                double distance = chosenPos == centerPos ? game.config.spawnRadius : 8;

                double theta = random.nextDouble() * 2 * Math.PI;
                int x = (int) (chosenPos.getX() + (Math.cos(theta) * distance));
                int z = (int) (chosenPos.getZ() + (Math.sin(theta) * distance));

                BlockPos pos = WdSpawnLogic.findSurfaceAt(x, z, 12, world);
                if (spawnZombie(world, pos)) {
                    wave.onZombieAdded();
                }
            }

            spawnedZombies = targetZombies;
        }

        return spawnedZombies >= wave.totalZombies;
    }

    private boolean spawnZombie(ServerWorld world, BlockPos pos) {
        ZombieClass zombieClass = getZombieClass(world.getRandom(), wave.ordinal);
        ZombieModifier mod = getZombieModifier(world.getRandom()); //TODO: scale based on ordinal

        MobEntity zombie;
        if (world.containsFluid(new Box(pos).expand(1.0))) {
            zombie = new WaveDrownedEntity(world, game, mod, ZombieClasses.DROWNED);
        } else {
            zombie = new WaveZombieEntity(world, game, mod, zombieClass);
        }

        zombie.refreshPositionAndAngles(pos, 0, 0);
        zombie.setPersistent();

        return world.spawnEntity(zombie);
    }

    private ZombieClass getZombieClass(Random random, int waveOrdinal) {
        if (waveOrdinal > 5 && random.nextInt((int) (500.0 / (waveOrdinal - 5))) == 0) {
            return ZombieClasses.KNIGHT;
        }

        return ZombieClasses.DEFAULT;
    }

    private ZombieModifier getZombieModifier(Random random) {
        int r = random.nextInt(50);

        if (r <= 1) { // 4% chance of withering
            return ZombieModifier.WITHER;
        } else if (r <= 5) { // 8% chance of poison
            return ZombieModifier.POISON;
        } else if (r <= 10) { // 10% chance of weakness
            return ZombieModifier.WEAKNESS;
        }

        return ZombieModifier.NORMAL;
    }

    // Weights go from 0.083ish to 0.5
    // 0.5\left(\frac{-1}{\left(x+1.2\right)}+1\right)
    private static double getDistWeight(double distance) {
        return 0.5 * ((-1.0 / (distance + 1.2)) + 1.0);
    }
}
