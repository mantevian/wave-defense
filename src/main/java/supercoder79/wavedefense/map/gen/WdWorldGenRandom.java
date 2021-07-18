package supercoder79.wavedefense.map.gen;

import net.minecraft.world.gen.WorldGenRandom;

import java.util.Random;

public class WdWorldGenRandom implements WorldGenRandom {
    private Random random;

    public WdWorldGenRandom(long seed) {
        random = new Random(seed);
    }

    public WdWorldGenRandom() {
        random = new Random();
    }

    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public int nextInt() {
        return random.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return random.nextGaussian();
    }

    @Override
    public void skip(int count) {
        WorldGenRandom.super.skip(count);
    }
}
