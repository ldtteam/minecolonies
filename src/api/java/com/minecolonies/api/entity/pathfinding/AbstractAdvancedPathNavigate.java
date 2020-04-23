package com.minecolonies.api.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;

public abstract class AbstractAdvancedPathNavigate extends GroundPathNavigator
{
    //  Parent class private members
    protected final MobEntity ourEntity;
    @Nullable
    protected       BlockPos     destination;
    protected       double       walkSpeed = 1.0D;
    @Nullable
    protected       BlockPos     originalDestination;
    @Nullable
    protected       Future<Path> calculationFuture;

    /**
     * The navigators node costs
     */
    private PathingOptions pathingOptions = new PathingOptions();

    public AbstractAdvancedPathNavigate(
      final MobEntity entityLiving,
      final World worldIn)
    {
        super(entityLiving, worldIn);
        this.ourEntity = entity;
    }

    /**
     * Get the destination from the path.
     *
     * @return the destination position.
     */
    public BlockPos getDestination()
    {
        return destination;
    }

    public abstract PathResult moveAwayFromXYZ(final BlockPos currentPosition, final double range, final double speed);

    public abstract PathResult moveToXYZ(final double x, final double y, final double z, final double speed);

    public abstract WaterPathResult moveToWater(final int searchRange, final double v, final List<BlockPos> ponds);

    public abstract PathResult moveAwayFromLivingEntity(final Entity target, final double distance, final double combatMovementSpeed);

    public abstract boolean tryMoveToBlockPos(final BlockPos position, final double speed);

    public abstract TreePathResult moveToTree(final BlockPos startRestriction, final BlockPos endRestriction, final double speed, final List<ItemStorage> treesToCut, final IColony colony);

    public abstract TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> treesToCut, final IColony colony);

    public abstract PathResult moveToLivingEntity(@NotNull final Entity e, final double speed);

    /**
     * Get the pathing options
     *
     * @return the pathing options.
     */
    public PathingOptions getPathingOptions()
    {
        return pathingOptions;
    }
}
