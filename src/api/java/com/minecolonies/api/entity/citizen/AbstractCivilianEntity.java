package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.entity.pathfinding.IStuckHandlerEntity;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.sounds.EventType.INTERACTION;
import static com.minecolonies.api.util.SoundUtils.playSoundAtCivilian;

public abstract class AbstractCivilianEntity extends AgeableEntity implements INPC, IStuckHandlerEntity
{

    /**
     * Whether this entity can be stuck for stuckhandling
     */
    private boolean canBeStuck = true;

    protected AbstractCivilianEntity(final EntityType<? extends AgeableEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }

    /**
     * Setter for the citizen data.
     *
     * @param data the data to set.
     */
    public abstract void setCivilianData(@Nullable ICivilianData data);

    /**
     * Setter for the citizen data.
     *
     * @return civilian data
     */
    public abstract ICivilianData getCivilianData();

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    public abstract void markDirty();

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    public abstract int getCivilianID();

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    public abstract void setCitizenId(int id);

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
    public void push(@NotNull final Entity entityIn)
    {
        if (entityIn instanceof ServerPlayerEntity)
        {
            onPlayerCollide((PlayerEntity) entityIn);
        }
        super.push(entityIn);
    }

    /**
     * On player collision action
     *
     * @param player
     */
    public void onPlayerCollide(final PlayerEntity player)
    {
        if (getNavigation().getPath() != null)
        {
            getNavigation().stop();
            getLookControl().setLookAt(player.position().add(0, player.eyeHeight, 0));

            playSoundAtCivilian(level, blockPosition(), INTERACTION, getCivilianData());
        }
    }

    /**
     * Prevent citizens and visitors from travelling to other dimensions through portals.
     */
    @Nullable
    @Override
    public Entity changeDimension(@NotNull final ServerWorld serverWorld, @NotNull final ITeleporter teleporter)
    {
        return null;
    }
}
