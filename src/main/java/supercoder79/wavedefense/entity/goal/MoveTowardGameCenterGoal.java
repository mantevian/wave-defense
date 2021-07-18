package supercoder79.wavedefense.entity.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import supercoder79.wavedefense.entity.WaveEntity;
import supercoder79.wavedefense.game.WdActive;

import java.util.EnumSet;

public final class MoveTowardGameCenterGoal<T extends PathAwareEntity & WaveEntity> extends MoveToTargetPosGoal {
    private final T entity;

    public MoveTowardGameCenterGoal(T entity) {
        super(entity, 1.0, 64);
        this.entity = entity;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (entity.getNavigation().isIdle()) {
            WdActive game = entity.getGame();
            double distance2 = entity.squaredDistanceTo(game.guide.getCenterPos());
            return distance2 > game.config.spawnRadius * game.config.spawnRadius;
        }
        return false;
    }

    @Override
    protected BlockPos getTargetPos() {
        WdActive game = entity.getGame();
        Vec3d center = game.guide.getCenterPos();
        return new BlockPos.Mutable(center.x, center.y, center.z);
    }

    @Override
    public void start() {
        WdActive game = entity.getGame();
        Vec3d center = game.guide.getCenterPos();

        if (center != null) {
            entity.getNavigation().startMovingTo(center.x, center.y, center.z, 1.0);
        }
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }
}
