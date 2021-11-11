package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.coremod.entity.pathfinding.Node;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
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
      final World world,
      @NotNull final BlockPos start, final int range)
    {
        super(world, searchingEntity.blockPosition(), start, range, searchingEntity);

        this.searchingEntity = searchingEntity;
        this.lookTarget = lookTarget;
    }

    @Override
    protected double computeHeuristic(final BlockPos pos)
    {
        return searchingEntity.blockPosition().distManhattan(pos);
    }

    @Override
    protected boolean isAtDestination(final Node n)
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
    protected double getNodeResultScore(@NotNull final Node n)
    {
        //  For Result Score lower is better
        return start.distManhattan(n.pos);
    }

    private boolean canSeeTargetFromPos(final BlockPos pos)
    {
        Vector3d vec3d = new Vector3d(pos.getX(), pos.getY() + entity.get().getEyeHeight(), pos.getZ());
        Vector3d vec3d1 = new Vector3d(lookTarget.getX(), lookTarget.getEyeY(), lookTarget.getZ());
        return this.world.clip(new RayTraceContext(vec3d, vec3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity.get())).getType() == RayTraceResult.Type.MISS;
    }
}
