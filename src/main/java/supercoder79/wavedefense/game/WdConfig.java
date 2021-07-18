package supercoder79.wavedefense.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

import java.util.ArrayList;
import java.util.List;

public final class WdConfig {
    public static final Codec<WdConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            Path.CODEC.fieldOf("path").forGetter(config -> config.path),
            Codec.INT.fieldOf("spawn_radius").forGetter(config -> config.spawnRadius),
            Codec.DOUBLE.fieldOf("min_wave_spacing").forGetter(config -> config.minWaveSpacing),
            Codec.DOUBLE.fieldOf("max_wave_spacing").forGetter(config -> config.maxWaveSpacing),
            MonsterSpawns.CODEC.fieldOf("monster_spawns").forGetter(config -> config.monsterSpawns),
            MonsterSpawnChoices.CODEC.fieldOf("monster_spawn_choices").forGetter(config -> config.monsterSpawnChoices),
            Codec.list(Codec.list(ShopEntry.CODEC)).fieldOf("shop").forGetter(config -> config.shop)
    ).apply(instance, (playerConfig1, path1, spawnRadius1, minWaveSpacing1, maxWaveSpacing1, monsterSpawns1, monsterSpawnChoices1, shop1) -> new WdConfig(playerConfig1, path1, monsterSpawns1, monsterSpawnChoices1, shop1, spawnRadius1, minWaveSpacing1, maxWaveSpacing1)));

    public final PlayerConfig playerConfig;
    public final Path path;
    public final MonsterSpawns monsterSpawns;
    public final MonsterSpawnChoices monsterSpawnChoices;
    public final List<List<ShopEntry>> shop;
    public final int spawnRadius;
    public final double minWaveSpacing;
    public final double maxWaveSpacing;

    public WdConfig(PlayerConfig playerConfig, Path path, MonsterSpawns monsterSpawns, MonsterSpawnChoices monsterSpawnChoices, List<List<ShopEntry>> shop, int spawnRadius, double minWaveSpacing, double maxWaveSpacing) {
        this.playerConfig = playerConfig;
        this.path = path;
        this.monsterSpawns = monsterSpawns;
        this.monsterSpawnChoices = monsterSpawnChoices;
        this.shop = shop;
        this.spawnRadius = spawnRadius;
        this.minWaveSpacing = minWaveSpacing;
        this.maxWaveSpacing = maxWaveSpacing;
    }

    public static final class Path {
        public static final Codec<Path> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("length").forGetter(config -> config.length),
                Codec.INT.fieldOf("segment_length").forGetter(config -> config.segmentLength),
                Codec.DOUBLE.fieldOf("radius").forGetter(config -> config.radius)
        ).apply(instance, Path::new));

        public final int length;
        public final int segmentLength;
        public final double radius;

        public Path(int length, int segmentLength, double radius) {
            this.length = length;
            this.segmentLength = segmentLength;
            this.radius = radius;
        }
    }

    public static final class MonsterSpawns {
        public static final Codec<MonsterSpawns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("post_wave_5_scale").forGetter(config -> config.postWaveFiveScale),
                Codec.DOUBLE.fieldOf("base_group_size_scale").forGetter(config -> config.baseGroupSizeScale),
                Codec.DOUBLE.fieldOf("base_index_scale").forGetter(config -> config.baseIndexScale),
                Codec.DOUBLE.fieldOf("index_scale").forGetter(config -> config.indexScale),
                Codec.DOUBLE.fieldOf("group_size_scale").forGetter(config -> config.groupSizeScale),
                Codec.DOUBLE.fieldOf("upper_group_size_scale").forGetter(config -> config.upperGroupSizeScale),
                Codec.DOUBLE.fieldOf("upper_index_scale").forGetter(config -> config.upperIndexScale)
        ).apply(instance, MonsterSpawns::new));

        public final double postWaveFiveScale;
        public final double baseGroupSizeScale;
        public final double baseIndexScale;
        public final double indexScale;
        public final double groupSizeScale;
        public final double upperGroupSizeScale;
        public final double upperIndexScale;

        public MonsterSpawns(double postWaveFiveScale, double baseGroupSizeScale, double baseIndexScale, double indexScale, double groupSizeScale, double upperGroupSizeScale, double upperIndexScale) {
            this.postWaveFiveScale = postWaveFiveScale;
            this.baseGroupSizeScale = baseGroupSizeScale;
            this.baseIndexScale = baseIndexScale;
            this.indexScale = indexScale;
            this.groupSizeScale = groupSizeScale;
            this.upperGroupSizeScale = upperGroupSizeScale;
            this.upperIndexScale = upperIndexScale;
        }
    }

    public static final class MonsterSpawnChoices {
        public static final Codec<MonsterSpawnChoices> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("zombie").forGetter(config -> config.zombie),
                Codec.DOUBLE.fieldOf("skeleton").forGetter(config -> config.skeleton),
                Codec.DOUBLE.fieldOf("phantom").forGetter(config -> config.phantom),
                Codec.DOUBLE.fieldOf("witch").forGetter(config -> config.witch),
                Codec.DOUBLE.fieldOf("cave_spider").forGetter(config -> config.caveSpider)
        ).apply(instance, MonsterSpawnChoices::new));

        public final double zombie;
        public final double skeleton;
        public final double phantom;
        public final double witch;
        public final double caveSpider;

        public MonsterSpawnChoices(double zombie, double skeleton, double phantom, double witch, double caveSpider) {
            this.zombie = zombie;
            this.skeleton = skeleton;
            this.phantom = phantom;
            this.witch = witch;
            this.caveSpider = caveSpider;
        }
    }

    public static class ShopEntry {
        public static final Codec<ShopEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("name", "").forGetter(config -> config.name),
                Codec.STRING.optionalFieldOf("description", "").forGetter(config -> config.description),
                ShopEntryCost.CODEC.optionalFieldOf("cost", new ShopEntryCost("none", "iron", -1, -1, -1, new ArrayList<>())).forGetter(config -> config.cost),
                ShopItem.CODEC.optionalFieldOf("item", new ShopItem("air", 0)).forGetter(config -> config.item),
                ShopEnchantment.CODEC.optionalFieldOf("enchantment", new ShopEnchantment("none", "none", 0)).forGetter(config -> config.enchantment),
                Codec.STRING.optionalFieldOf("upgrade", "").forGetter(config -> config.itemToUpgrade),
                Codec.STRING.optionalFieldOf("display", "light_gray_stained_glass_pane").forGetter(config -> config.display),
                Codec.STRING.optionalFieldOf("potion", "").forGetter(config -> config.potion)
        ).apply(instance, ShopEntry::new));

        public final String name;
        public final String description;
        public final ShopEntryCost cost;
        public final ShopItem item;
        public final ShopEnchantment enchantment;
        public final String itemToUpgrade;
        public final String display;
        public final String potion;

        public ShopEntry(String name, String description, ShopEntryCost cost, ShopItem item, ShopEnchantment enchantment, String itemToUpgrade, String display, String potion) {
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.item = item;
            this.enchantment = enchantment;
            this.itemToUpgrade = itemToUpgrade;
            this.display = display;
            this.potion = potion;
        }
    }

    public static class ShopEntryCost {
        public static final Codec<ShopEntryCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(config -> config.type),
                Codec.STRING.optionalFieldOf("currency", "iron").forGetter(config -> config.currency),
                Codec.INT.optionalFieldOf("cost", -1).forGetter(config -> config.cost),
                Codec.INT.optionalFieldOf("base", 0).forGetter(config -> config.base),
                Codec.DOUBLE.optionalFieldOf("scale", 0d).forGetter(config -> config.scale),
                Codec.list(ShopSubEntryCost.CODEC).optionalFieldOf("levels", new ArrayList<>()).forGetter(config -> config.levels)
        ).apply(instance, ShopEntryCost::new));

        public final String type;
        public final String currency;
        public final int cost;
        public final int base;
        public final double scale;
        public final List<ShopSubEntryCost> levels;

        public ShopEntryCost(String type, String currency, int cost, int base, double scale, List<ShopSubEntryCost> levels) {
            this.cost = cost;
            this.type = type;
            this.currency = currency;
            this.base = base;
            this.scale = scale;
            this.levels = levels;
        }
    }

    public static class ShopSubEntryCost {
        public static final Codec<ShopSubEntryCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("currency").forGetter(config -> config.currency),
                Codec.INT.optionalFieldOf("cost", -1).forGetter(config -> config.cost)
        ).apply(instance, ShopSubEntryCost::new));

        public final String currency;
        public final int cost;

        public ShopSubEntryCost(String currency, int cost) {
            this.cost = cost;
            this.currency = currency;
        }
    }

    public static class ShopEnchantment {
        public static final Codec<ShopEnchantment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("target").forGetter(config -> config.target),
                Codec.STRING.fieldOf("enchantment").forGetter(config -> config.enchantment),
                Codec.INT.optionalFieldOf("limit", -1).forGetter(config -> config.limit)
        ).apply(instance, ShopEnchantment::new));

        public final String target;
        public final String enchantment;
        public final int limit;

        public ShopEnchantment(String target, String enchantment, int limit) {
            this.target = target;
            this.enchantment = enchantment;
            this.limit = limit;
        }
    }

    public static class ShopItem {
        public static final Codec<ShopItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(config -> config.item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(config -> config.count)
        ).apply(instance, ShopItem::new));

        public final String item;
        public final int count;

        public ShopItem(String item, int count) {

            this.item = item;
            this.count = count;
        }
    }
}
