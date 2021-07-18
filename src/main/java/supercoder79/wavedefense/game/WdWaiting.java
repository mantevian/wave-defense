package supercoder79.wavedefense.game;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import supercoder79.wavedefense.map.WdMap;
import supercoder79.wavedefense.map.WdMapGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class WdWaiting {
	private final GameSpace space;
	private final ServerWorld world;
	private final WdMap map;
	private final WdConfig config;

	private final WdSpawnLogic spawnLogic;

	private WdWaiting(GameSpace space, ServerWorld world, WdMap map, WdConfig config) {
		this.space = space;
		this.world = world;
		this.map = map;
		this.config = config;
		this.spawnLogic = new WdSpawnLogic(space, world, config);
	}

	public static GameOpenProcedure open(GameOpenContext<WdConfig> context) {
		WdMapGenerator generator = new WdMapGenerator();
		WdConfig config = context.config();

		WdMap map = generator.build(config);
		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
				.setGenerator(map.chunkGenerator(context.server()))
				.setTimeOfDay(18000)
				.setDifficulty(Difficulty.NORMAL);

		return context.openWithWorld(worldConfig, (game, world) -> {
			WdWaiting waiting = new WdWaiting(game.getGameSpace(), world, map, config);
			GameWaitingLobby.applyTo(game, config.playerConfig);

			game.deny(GameRuleType.CRAFTING);
			game.deny(GameRuleType.PORTALS);
			game.deny(GameRuleType.PVP);
			game.deny(GameRuleType.FALL_DAMAGE);
			game.deny(GameRuleType.HUNGER);
			game.deny(GameRuleType.THROW_ITEMS);
			game.deny(GameRuleType.INTERACTION);

			game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);

			game.listen(GamePlayerEvents.OFFER, waiting::offerPlayer);
			game.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> ActionResult.FAIL);
			game.listen(PlayerDeathEvent.EVENT, (player, source) -> ActionResult.FAIL);
		});
	}

	private GameResult requestStart() {
		WdActive.open(this.space, this.world, this.map, this.config);
		return GameResult.ok();
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		var spawn = WdSpawnLogic.findSurfaceAround(Vec3d.ZERO, world, this.config);
		if (spawn == null) {
			return offer.reject(new LiteralText("No spawn defined on map!"));
		}

		return offer.accept(this.world, Vec3d.ofCenter(spawn))
				.and(() -> {
					var player = offer.player();
					this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
					this.spawnLogic.spawnPlayer(player);
				});
	}
}
