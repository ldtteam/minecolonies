package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Pathing job for moving into vision of the given entity
 */
public class PathJobCanSee extends AbstractPathJob implements ISearchPathJob
{
    /**
     * The entity to see
     */
    private final LivingEntity lookTarget;

    /**
     * The position we want to search around from, usually guarding pos, so guide the heuristic there from the current entity position
     */
    private final BlockPos searchAroundPos;

    public PathJobCanSee(
      final Mob searchingEntity,
      final LivingEntity lookTarget,
      final Level world,
      @NotNull final BlockPos searchAroundPos, final int range)
    {
        super(world, PathfindingUtils.prepareStart(searchingEntity), range, new PathResult<PathJobCanSee>(), searchingEntity);

        this.searchAroundPos = searchAroundPos;
        this.lookTarget = lookTarget;
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.distManhattan(searchAroundPos.getX(), searchAroundPos.getY(), searchAroundPos.getZ(), x, y, z);
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        if (searchAroundPos.getY() - n.y > 2)
        {
            return false;
        }

        return canSeeTargetFromPos(tempWorldPos.set(n.x, n.y, n.z))
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                      == SurfaceType.WALKABLE;
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double of the distance.
     */
    @Override
    protected double getEndNodeScore(@NotNull final MNode n)
    {
        return BlockPosUtil.distManhattan(start, n.x, n.y, n.z);
    }

    private boolean canSeeTargetFromPos(final BlockPos pos)
    {
        Vec3 vec3d = new Vec3(pos.getX(), pos.getY() + entity.getEyeHeight(), pos.getZ());
        Vec3 vec3d1 = new Vec3(lookTarget.getX(), lookTarget.getEyeY(), lookTarget.getZ());
        return this.world.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    }
}
