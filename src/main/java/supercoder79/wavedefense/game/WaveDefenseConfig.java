package supercoder79.wavedefense.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public final class WaveDefenseConfig {
	public static final Codec<WaveDefenseConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
			PathConfig.CODEC.fieldOf("path").forGetter(config -> config.pathConfig),
			Codec.INT.fieldOf("spawn_radius").forGetter(config -> config.spawnRadius),
			Codec.INT.fieldOf("ticks_per_block").forGetter(config -> config.ticksPerBlock)
	).apply(instance, WaveDefenseConfig::new));

	public final PlayerConfig playerConfig;
	public final PathConfig pathConfig;
	public final int spawnRadius;
	public final int ticksPerBlock;

	public WaveDefenseConfig(PlayerConfig playerConfig, PathConfig pathConfig, int spawnRadius, int ticksPerBlock) {
		this.playerConfig = playerConfig;
		this.pathConfig = pathConfig;
		this.spawnRadius = spawnRadius;
		this.ticksPerBlock = ticksPerBlock;
	}

	public static final class PathConfig {
		public static final Codec<PathConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("path_length").forGetter(config -> config.pathLength),
				Codec.INT.fieldOf("path_segment_length").forGetter(config -> config.pathSegmentLength),
				Codec.DOUBLE.fieldOf("path_width").forGetter(config -> config.pathWidth)
		).apply(instance, PathConfig::new));

		public final int pathLength;
		public final int pathSegmentLength;
		public final double pathWidth;

		public PathConfig(int pathLength, int pathSegmentLength, double pathWidth) {
			this.pathLength = pathLength;
			this.pathSegmentLength = pathSegmentLength;
			this.pathWidth = pathWidth;
		}
	}
}
