package supercoder79.wavedefense.game;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.core.jmx.Server;
import supercoder79.wavedefense.entity.WaveEntity;
import supercoder79.wavedefense.map.WdMap;
import supercoder79.wavedefense.util.ASCIIProgressBar;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.*;

public final class WdActive {
    public final GameSpace space;
    public final WdMap map;
    public final WdConfig config;
    private final MutablePlayerSet participants;
    private final WdSpawnLogic spawnLogic;
    public final WdWaveManager waveManager;
    public final HashMap<PlayerRef, WdPlayer> players = new HashMap<>();
    private final Set<BlockPos> openedChests = new HashSet<>();
    public final WdBar bar;

    public final WdGuide guide;

    private long gameCloseTick = Long.MAX_VALUE;

    public final int groupSize;

    public int averageGroupSize;

    private WdActive(GameSpace space, WdMap map, WdConfig config, MutablePlayerSet participants, GlobalWidgets widgets) {
        this.space = space;
        this.map = map;
        this.config = config;
        this.participants = participants;

        this.spawnLogic = new WdSpawnLogic(this.space, config);
        this.waveManager = new WdWaveManager(this);
        this.bar = WdBar.create(widgets);

        this.guide = new WdGuide(this);

        this.groupSize = participants.size();
        this.averageGroupSize = groupSize;
    }

    public static void open(GameSpace world, WdMap map, WdConfig config) {
        world.openGame(game -> {
            GlobalWidgets widgets = new GlobalWidgets(game);
            WdActive active = new WdActive(world, map, config, (MutablePlayerSet) world.getPlayers().copy(), widgets);

            game.setRule(GameRule.CRAFTING, RuleResult.ALLOW);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.ALLOW);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);

            game.on(GameOpenListener.EVENT, active::open);
            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);
            game.on(UseItemListener.EVENT, active::onUseItem);

            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
            game.on(EntityDeathListener.EVENT, active::onEntityDeath);
            game.on(AttackEntityListener.EVENT, active::onEntityAttack);
            game.on(EntityHitListener.EVENT, active::onEntityHit);
            game.on(UseBlockListener.EVENT, active::onUseBlock);

        });
    }

    private void open() {
        for (ServerPlayerEntity player : this.participants) {
            this.spawnParticipant(player);
        }
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnSpectator(player);
    }

    private void removePlayer(ServerPlayerEntity player) {
        participants.remove(player);
    }

    private void tick() {
        ServerWorld world = this.space.getWorld();
        long time = world.getTime();

        if (time > gameCloseTick) {
            this.space.close();
            return;
        }

        this.guide.tick(time, waveManager.isActive());
        this.waveManager.tick(time, guide.getProgressBlocks());

        this.damageFarPlayers(guide.getCenterPos());

        this.bar.tick(waveManager.getActiveWave());

        // This is a horrifically cursed workaround for UseBlockListener not working. I'm sorry.
        if (time % 20 == 0) {
            for (ServerPlayerEntity player : this.participants) {
                BlockPos.Mutable mutable = player.getBlockPos().mutableCopy();

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        for (int y = 0; y <= 2; y++) {

                            BlockPos local = mutable.add(x, y, z);
                            if (this.space.getWorld().getBlockState(local).isOf(Blocks.CHEST)) {
                                if (!this.openedChests.contains(local)) {
                                    this.participants.forEach((participant) -> {
                                        participant.sendMessage(new LiteralText(player.getEntityName() + " has found a loot chest!"), false);

                                        if (new Random().nextInt(4) == 0) {
                                            participant.sendMessage(new LiteralText("You recieved 6 iron and 1 gold!"), false);
                                            participant.inventory.insertStack(new ItemStack(Items.IRON_INGOT, 6));
                                            participant.inventory.insertStack(new ItemStack(Items.GOLD_INGOT, 1));
                                        } else {
                                            participant.sendMessage(new LiteralText("You recieved 12 iron!"), false);
                                            participant.inventory.insertStack(new ItemStack(Items.IRON_INGOT, 12));
                                        }
                                    });

                                    // Change glowstone to obsidian
                                    world.setBlockState(local.down(), Blocks.OBSIDIAN.getDefaultState());

                                    this.openedChests.add(local);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof WaveEntity) {
                String prefix = ((WaveEntity) entity).getMod().prefix;
                MutableText name = new LiteralText((prefix + " " + ((WaveEntity) entity).getMonsterClass().name()));

                if (prefix.equals(""))
                    name = new LiteralText((((WaveEntity) entity).getMonsterClass().name()));

                if (((WaveEntity) entity).showHealth()) {
                    MutableText healthBar = ASCIIProgressBar.get(((MobEntity) entity).getHealth() / ((MobEntity) entity).getMaxHealth(), 7);

                    entity.setCustomName(name.append(" ").append(healthBar));
                } else entity.setCustomName(name);
            } else if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                WdPlayer wdPlayer = players.get(PlayerRef.of(player));
                if (player.experienceLevel > wdPlayer.totalXPLevels) {
                    wdPlayer.upgradePoints++;
                }

                wdPlayer.totalXPLevels = player.experienceLevel;
            }
        }
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() == Items.COMPASS) {
            player.openHandledScreen(WdItemShop.create(player, this));
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    private ActionResult onEntityDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof WaveEntity) {
            WaveEntity waveEntity = (WaveEntity) entity;
            WdWave activeWave = waveManager.getActiveWave();
            if (activeWave != null) {
                activeWave.onMonsterKilled(waveEntity.monsterScore());

                if (source.getAttacker() instanceof ServerPlayerEntity) {
                    ServerPlayerEntity player = (ServerPlayerEntity) source.getAttacker();

                    player.inventory.insertStack(new ItemStack(Items.IRON_INGOT, waveEntity.ironCount()));
                    player.inventory.insertStack(new ItemStack(Items.GOLD_INGOT, waveEntity.goldCount()));
                    player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    player.addExperience(waveEntity.xp());
                    if (waveEntity.ironCount() > 0) {
                        player.sendMessage(new LiteralText("+" + waveEntity.ironCount() + " iron for killing " + waveEntity.getMonsterClass().name()).styled(style -> style.withColor(TextColor.parse("gray"))), false);
                    }
                    if (waveEntity.goldCount() > 0) {
                        player.sendMessage(new LiteralText("+" + waveEntity.goldCount() + " gold for killing " + waveEntity.getMonsterClass().name()).styled(style -> style.withColor(TextColor.parse("gold"))), false);
                    }
                    players.get(PlayerRef.of(player)).addMobKill(waveEntity.getMonsterClass().name());

                    Random random = space.getWorld().random;

                    for (int i = 0; i < waveEntity.emeraldCount(); i++) {
                        double x = random.nextDouble();
                        double y = random.nextDouble();
                        double z = random.nextDouble();
                        ItemEntity itemEntity = new ItemEntity(space.getWorld(), entity.getX() + x, entity.getY() + y, entity.getZ() + z, new ItemStack(Items.EMERALD));
                        itemEntity.addVelocity(z * 0.3, 0.2, z * 0.3);
                        space.getWorld().spawnEntity(itemEntity);
                    }

                    for (ServerPlayerEntity p : space.getWorld().getPlayers()) {

                        if (waveEntity.getContribution(PlayerRef.of(p)) >= waveEntity.totalContribution() / 4
                                && !p.equals(player)) {
                            int assistedIron = (int) ((Math.floor(waveEntity.ironCount() / 4d) + waveEntity.goldCount() * 4) * (waveEntity.getContribution(PlayerRef.of(p)) / waveEntity.totalContribution()));
                            if (assistedIron > 0) {
                                p.inventory.insertStack(new ItemStack(Items.IRON_INGOT, assistedIron));
                                p.sendMessage(new LiteralText("+" + assistedIron + " iron for assisting a kill of " + waveEntity.getMonsterClass().name()).styled(style -> style.withColor(TextColor.parse("gray"))), false);
                                players.get(PlayerRef.of(player)).addMobAssist(waveEntity.getMonsterClass().name());
                            }
                        }
                    }
                }
            }

            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

    private ActionResult onEntityAttack(ServerPlayerEntity player, Hand hand, Entity entity, EntityHitResult result) {
        if (entity instanceof WaveEntity && result != null) {
            WaveEntity waveEntity = (WaveEntity) entity;
            LivingEntity newEntity = (LivingEntity) result.getEntity();

            WdWave activeWave = waveManager.getActiveWave();
            if (activeWave != null) {
                waveEntity.addContributedPlayer(PlayerRef.of(player), (int) (newEntity.getHealth() - ((LivingEntity) entity).getHealth()));
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onEntityHit(ProjectileEntity projectileEntity, EntityHitResult result) {
        if (projectileEntity.getOwner() instanceof PlayerEntity && result != null) {
            WaveEntity waveEntity = (WaveEntity) result.getEntity();

            WdWave activeWave = waveManager.getActiveWave();
            if (activeWave != null) {
                waveEntity.addContributedPlayer(PlayerRef.of((PlayerEntity) projectileEntity.getOwner()), (int) ((ArrowEntity) projectileEntity).getDamage());
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.eliminatePlayer(player);

        if (participants.isEmpty()) {
            // Display win results
            PlayerSet players = space.getPlayers();
            players.sendMessage(new LiteralText("All players died....").formatted(Formatting.DARK_RED));
            players.sendMessage(new LiteralText("You made it to wave " + waveManager.getWaveOrdinal() + ".").formatted(Formatting.DARK_RED));

            // Close game in 10 secs
            this.gameCloseTick = this.space.getWorld().getTime() + (10 * 20);
        }

        return ActionResult.FAIL;
    }

    // TODO: this doesn't work. The logic has been moved to tick() as a hacky workaround.
    private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (this.space.getWorld().getBlockState(hitResult.getBlockPos()).isOf(Blocks.CHEST)) {
            if (!this.openedChests.contains(hitResult.getBlockPos())) {
                for (ServerPlayerEntity participant : this.participants) {
                    participant.sendMessage(new LiteralText(player.getDisplayName() + " has found a loot chest!"), false);
                    participant.sendMessage(new LiteralText("You recieved 12 iron."), false);
                    participant.inventory.insertStack(new ItemStack(Items.IRON_INGOT, 12));
                    participant.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }

                this.openedChests.add(hitResult.getBlockPos());
            }

            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
        this.guide.onAddPlayer(player);

        player.inventory.insertStack(0,
                ItemStackBuilder.of(Items.IRON_SWORD)
                        .setUnbreakable()
                        .build()
        );

        player.inventory.insertStack(1,
                ItemStackBuilder.of(Items.BOW)
                        .setUnbreakable()
                        .build()
        );

        player.inventory.insertStack(2,
                ItemStackBuilder.of(Items.CROSSBOW)
                        .setUnbreakable()
                        .build()
        );

        player.inventory.insertStack(3,
                ItemStackBuilder.of(Items.COOKED_BEEF)
                        .setCount(8)
                        .build()
        );

        player.inventory.insertStack(4,
                ItemStackBuilder.of(Items.ARROW)
                        .setCount(8)
                        .build()
        );

        player.inventory.insertStack(8,
                ItemStackBuilder.of(Items.COMPASS)
                        .setName(new LiteralText("Item Shop"))
                        .build()
        );

        player.inventory.armor.set(3, ItemStackBuilder.of(Items.CHAINMAIL_HELMET).setUnbreakable().build());
        player.inventory.armor.set(2, ItemStackBuilder.of(Items.CHAINMAIL_CHESTPLATE).setUnbreakable().build());
        player.inventory.armor.set(1, ItemStackBuilder.of(Items.CHAINMAIL_LEGGINGS).setUnbreakable().build());
        player.inventory.armor.set(0, ItemStackBuilder.of(Items.CHAINMAIL_BOOTS).setUnbreakable().build());

        players.put(PlayerRef.of(player), new WdPlayer());
    }

    private void eliminatePlayer(ServerPlayerEntity player) {
        if (!participants.remove(player)) {
            return;
        }

        Text message = player.getDisplayName().shallowCopy().append(" succumbed to the monsters....")
                .formatted(Formatting.RED);

        PlayerSet players = this.space.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);

        this.spawnSpectator(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void damageFarPlayers(Vec3d centerPos) {
        int maxDistance = this.config.spawnRadius + 5;
        double maxDistance2 = maxDistance * maxDistance;

        List<ServerPlayerEntity> farPlayers = new ArrayList<>();

        for (ServerPlayerEntity player : participants) {
            double deltaX = player.getX() - centerPos.getX();
            double deltaZ = player.getZ() - centerPos.getZ();

            if (deltaX * deltaX + deltaZ * deltaZ > maxDistance2) {
                if (!player.isCreative() && !player.isSpectator()) {
                    farPlayers.add(player);
                }
            }
        }

        for (ServerPlayerEntity player : farPlayers) {
            LiteralText message = new LiteralText("You are too far away from your villager!");
            player.sendMessage(message.formatted(Formatting.RED), true);

            player.damage(DamageSource.OUT_OF_WORLD, 0.5F);
        }
    }

    public PlayerSet getParticipants() {
        return participants;
    }
}
