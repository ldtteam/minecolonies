package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.coremod.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Pathing job for moving into vision of the given entity
 */
public class PathJobCanSee extends AbstractPathJob
{
    /**
     * The entity to see
     */
    private final LivingEntity lookTarget;

    /**
     * The entity to move
     */
    private final LivingEntity searchingEntity;

    public PathJobCanSee(
      final LivingEntity searchingEntity,
      final LivingEntity lookTarget,
      final Level world,
      @NotNull final BlockPos pos, final int range)
    {
        super(world, searchingEntity.blockPosition(), pos, range, searchingEntity);

        this.searchingEntity = searchingEntity;
        this.lookTarget = lookTarget;
    }

    @Override
    protected double computeHeuristic(final BlockPos pos)
    {
        return searchingEntity.blockPosition().distManhattan(pos);
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        if (end.getY() - n.pos.getY() > 2)
        {
            return false;
        }

        return canSeeTargetFromPos(n.pos);
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double of the distance.
     */
    @Override
    protected double getNodeResultScore(@NotNull final MNode n)
    {
        //  For Result Score lower is better
        return start.distManhattan(n.pos);
    }

    private boolean canSeeTargetFromPos(final BlockPos pos)
    {
        Vec3 vec3d = new Vec3(pos.getX(), pos.getY() + entity.get().getEyeHeight(), pos.getZ());
        Vec3 vec3d1 = new Vec3(lookTarget.getX(), lookTarget.getEyeY(), lookTarget.getZ());
        return this.world.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity.get())).getType() == HitResult.Type.MISS;
    }
}
