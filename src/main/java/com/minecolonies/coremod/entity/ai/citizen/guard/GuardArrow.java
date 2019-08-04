package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.achievements.ModAchievements;
import net.minecraft.entity.LivingEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

/**
 * Arrow class for arrows shot by guards.
 */
public class GuardArrow extends EntityTippedArrow
{
    private static final String  TAG_COLONY = "colony";
    private              IColony colony;

    /**
     * Constructor for forge.
     *
     * @param worldin the world this is in.
     */
    public GuardArrow(final World worldin)
    {
        super(worldin);
    }

    /**
     * Create a new Arrow.
     *
     * @param worldIn the world this is shot in.
     * @param shooter the guard shooting
     */
    public GuardArrow(final World worldIn, final AbstractEntityCitizen shooter)
    {
        super(worldIn, shooter);
        this.colony = shooter.getCitizenColonyHandler().getColony();
    }

    @Override
    public void writeEntityToNBT(final CompoundNBT compound)
    {
        super.writeEntityToNBT(compound);
        compound.putInt(TAG_COLONY, colony.getID());
    }

    @Override
    public void readEntityFromNBT(final CompoundNBT compound)
    {
        super.readEntityFromNBT(compound);
        final int colonyID = compound.getInt(TAG_COLONY);
        colony = IColonyManager.getInstance().getColonyByWorld(colonyID, world);
    }

    @Override
    protected void arrowHit(final LivingEntityBase targetEntity)
    {
        super.arrowHit(targetEntity);
        if (targetEntity.getHealth() <= 0.0F)
        {
            if (targetEntity instanceof PlayerEntity)
            {
                final PlayerEntity player = (PlayerEntity) targetEntity;
                if (colony.getPermissions().isColonyMember(player))
                {
                    this.colony.getStatsManager().triggerAchievement(ModAchievements.achievementPlayerDeathGuard);
                }
            }
            colony.getStatsManager().incrementStatistic("mobs");
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GuardArrow))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final GuardArrow that = (GuardArrow) o;
        return colony == null ? (that.colony != null) : colony.equals(that.colony);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (colony != null ? colony.hashCode() : 0);
        return result;
    }
}
