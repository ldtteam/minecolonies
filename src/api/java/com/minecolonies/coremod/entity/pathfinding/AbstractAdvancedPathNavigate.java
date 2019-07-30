package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;

public abstract class AbstractAdvancedPathNavigate extends PathNavigateGround
{
    //  Parent class private members
    protected final EntityLiving ourEntity;
    @Nullable
    protected       BlockPos     destination;
    protected       double       walkSpeed = 1.0D;
    @Nullable
    protected       BlockPos     originalDestination;
    @Nullable
    protected       Future<Path> future;

    public AbstractAdvancedPathNavigate(
      final EntityLiving entitylivingIn,
      final World worldIn,
      @NotNull final EntityLiving entity)
    {
        super(entitylivingIn, worldIn);
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

    public abstract PathResult moveAwayFromEntityLiving(final Entity target, final double distance, final double combatMovementSpeed);

    public abstract boolean tryMoveToBlockPos(final BlockPos position, final double speed);

    public abstract TreePathResult moveToTree(final int range, final double speed, final List<ItemStorage> treesToCut, final IColony colony);

    public abstract PathResult moveToEntityLiving(@NotNull final Entity e, final double speed);
}
