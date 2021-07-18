package supercoder79.wavedefense.map;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import supercoder79.wavedefense.game.WdConfig;
import supercoder79.wavedefense.map.gen.WdChunkGenerator;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import supercoder79.wavedefense.map.gen.WdPath;

public final class WdMap {
	public final WdPath path;
	public final WdConfig config;

	public final DoubleList waveStarts;

	public WdMap(WdPath path, WdConfig config, DoubleList waveStarts) {
		this.path = path;
		this.config = config;
		this.waveStarts = waveStarts;
	}

	public ChunkGenerator chunkGenerator(MinecraftServer server) {
		return new WdChunkGenerator(server, config, this);
	}
}
