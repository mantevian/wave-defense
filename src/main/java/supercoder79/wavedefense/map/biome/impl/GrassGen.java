package supercoder79.wavedefense.map.biome.impl;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import supercoder79.wavedefense.util.RandomCollection;
import xyz.nucleoid.substrate.gen.MapGen;

public final class GrassGen implements MapGen {
    public static final GrassGen INSTANCE = new GrassGen(new RandomCollection<BlockState>()
            .add(32, Blocks.GRASS.getDefaultState())
            .add(1, Blocks.DANDELION.getDefaultState())
            .add(1, Blocks.POPPY.getDefaultState()), 16, 8, 4);
    private final RandomCollection<BlockState> states;
    private final int count;
    private final int horizontalSpread;
    private final int verticalSpread;

    public GrassGen(RandomCollection<BlockState> states, int count, int horizontalSpread, int verticalSpread) {
        this.states = states;
        this.count = count;
        this.horizontalSpread = horizontalSpread;
        this.verticalSpread = verticalSpread;
    }

    @Override
    public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
        BlockState state = this.states.next();

        for (int i = 0; i < this.count; i++) {
            int aX = random.nextInt(this.horizontalSpread) - random.nextInt(this.horizontalSpread);
            int aY = random.nextInt(this.verticalSpread) - random.nextInt(this.verticalSpread);
            int aZ = random.nextInt(this.horizontalSpread) - random.nextInt(this.horizontalSpread);
            BlockPos local = pos.add(aX, aY, aZ);

            if (world.getBlockState(local.down()) == Blocks.GRASS_BLOCK.getDefaultState() && world.getBlockState(local).isAir()) {
                world.setBlockState(local, state, 3);
            }
        }
    }
}
