package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.entity.pathfinding.IStuckHandlerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Special abstract minecolonies mob that overrides laggy vanilla behaviour.
 */
public abstract class AbstractFastMinecoloniesEntity extends PathfinderMob implements IStuckHandlerEntity
{
    /**
     * Whether this entity can be stuck for stuckhandling
     */
    private boolean canBeStuck = true;

    /**
     * Create a new instance.
     * @param type from type.
     * @param worldIn the world.
     */
    protected AbstractFastMinecoloniesEntity(final EntityType<? extends PathfinderMob> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public boolean canBeLeashed(Player player)
    {
        return false;
    }

    @Override
    public boolean canBeStuck()
    {
        return canBeStuck;
    }

    /**
     * Sets whether the entity currently can be stuck
     *
     * @param canBeStuck
     */
    public void setCanBeStuck(final boolean canBeStuck)
    {
        this.canBeStuck = canBeStuck;
    }

    @Override
    public boolean checkBedExists()
    {
        return false;
    }

    @Override
    protected void removeFrost()
    {

    }

    @Override
    protected void tryAddFrost()
    {

    }

    @Override
    public void onInsideBubbleColumn(boolean down)
    {

    }

    @Override
    protected int decreaseAirSupply(final int supply)
    {
        return supply - 1;
    }

    @Override
    protected int increaseAirSupply(final int supply)
    {
        return supply + 1;
    }

    @Override
    protected void onChangedBlock(final BlockPos pos)
    {
        // This just tries to apply soulspeed or frostwalker
    }

    /**
     * Prevent citizens and visitors from travelling to other dimensions through portals.
     */
    @Nullable
    @Override
    public Entity changeDimension(@NotNull final ServerLevel serverWorld, @NotNull final ITeleporter teleporter)
    {
        return null;
    }
}
