package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.entity.pathfinding.IStuckHandlerEntity;
import com.minecolonies.api.sounds.SoundManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.sounds.EventType.GREETING;
import static com.minecolonies.api.sounds.EventType.SUCCESS;
import static com.minecolonies.api.util.SoundUtils.playSoundAtCitizenWith;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

public abstract class AbstractCivilianEntity extends AgeableMob implements Npc, IStuckHandlerEntity
{
    /**
     * Whether this entity can be stuck for stuckhandling
     */
    private boolean canBeStuck = true;

    /**
     * Time after which the next player collision is possible
     */
    protected long nextPlayerCollisionTime = 0;

    /**
     * Sound manager of the civilian.
     */
    private SoundManager soundManager;

    /**
     * Create a new instance.
     * @param type from type.
     * @param worldIn the world.
     */
    protected AbstractCivilianEntity(final EntityType<? extends AgeableMob> type, final Level worldIn)
    {
        super(type, worldIn);
        if (worldIn.isClientSide)
        {
            soundManager = new SoundManager((ClientLevel) worldIn);
        }
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
    public void tick()
    {
        super.tick();
        if (level.isClientSide)
        {
            soundManager.tick();
        }
    }

    @Override
    public void push(@NotNull final Entity entityIn)
    {
        if (entityIn instanceof ServerPlayer)
        {
            onPlayerCollide((Player) entityIn);
        }
        super.push(entityIn);
    }

    /**
     * On player collision action
     *
     * @param player
     */
    public void onPlayerCollide(final Player player)
    {
        if (player.level.getGameTime() > nextPlayerCollisionTime)
        {
            nextPlayerCollisionTime = player.level.getGameTime() + TICKS_SECOND * 15;
            getNavigation().stop();
            getLookControl().setLookAt(player);

            playSoundAtCitizenWith(level, blockPosition(), GREETING, getCivilianData());
        }
    }

    /**
     * Get the sound manager.
     * @return the sound manager.
     */
    public SoundManager getSoundManager()
    {
        return soundManager;
    }

    @Override
    protected void removeFrost()
    {

    }

    @Override
    protected void tryAddFrost()
    {

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
