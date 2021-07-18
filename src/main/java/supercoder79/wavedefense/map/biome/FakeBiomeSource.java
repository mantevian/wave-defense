package supercoder79.wavedefense.map.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.RegistryLookupCodec;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import supercoder79.wavedefense.map.biome.impl.*;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import supercoder79.wavedefense.map.gen.WdWorldGenRandom;

public final class FakeBiomeSource extends BiomeSource {
	public static final Codec<FakeBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter(source -> source.biomeRegistry),
			Codec.LONG.fieldOf("seed").stable().forGetter(source -> source.seed))
			.apply(instance, instance.stable(FakeBiomeSource::new)));

	private final Registry<Biome> biomeRegistry;
	private final long seed;

	private final SimplexNoiseSampler temperatureNoise;
	private final SimplexNoiseSampler rainfallNoise;
	private final SimplexNoiseSampler roughnessNoise;

	public FakeBiomeSource(Registry<Biome> biomeRegistry, long seed) {
		super(ImmutableList.of());
		this.biomeRegistry = biomeRegistry;
		this.seed = seed;

		temperatureNoise = new SimplexNoiseSampler(new WdWorldGenRandom(seed + 79));
		rainfallNoise = new SimplexNoiseSampler(new WdWorldGenRandom(seed - 79));
		roughnessNoise = new SimplexNoiseSampler(new WdWorldGenRandom(seed));
	}

	@Override
	protected Codec<? extends BiomeSource> getCodec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long seed) {
		return new FakeBiomeSource(this.biomeRegistry, seed);
	}

	@Override
	public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
		return biomeRegistry.get(getRealBiome(biomeX << 2, biomeZ << 2).getFakingBiome());
	}

	public BiomeGen getRealBiome(int x, int z) {
		double temperature = (temperatureNoise.sample(x / 320.0, z / 320.0) + 1) / 2;
		temperature = temperature * 0.9 + (((roughnessNoise.sample(x / 72.0, z / 72.0) + 1) / 2) * 0.1);

		double rainfall = (rainfallNoise.sample(x / 320.0, z / 320.0) + 1) / 2;

		if (temperature > 0.85) {
			return DesertGen.INSTANCE;
		} else if (temperature > 0.575) {
			if (rainfall < 0.35) {
				return DesertGen.INSTANCE;
			} else if (rainfall < 0.5) {
				return ShrublandGen.INSTANCE;
			} else if (rainfall > 0.6) {
				return SwampGen.INSTANCE;
			} else {
				return PlainsGen.INSTANCE;
			}
		} else if (temperature > 0.4) {
			if (rainfall > 0.575) {
				return TaigaGen.INSTANCE;
			} else {
				return ForestGen.INSTANCE;
			}
		} else {
			return TundraGen.INSTANCE;
		}
	}
}
