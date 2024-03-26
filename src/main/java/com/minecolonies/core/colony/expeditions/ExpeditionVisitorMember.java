package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Visitor expedition members.
 */
public final class ExpeditionVisitorMember implements IExpeditionMember<IVisitorData>
{
    /**
     * Nbt tag constants.
     */
    private static final String TAG_MAX_HEALTH = "maxHealth";
    private static final String TAG_HEALTH     = "health";

    /**
     * The id of the visitor.
     */
    private final int id;

    /**
     * The name of the visitor.
     */
    private final String name;

    /**
     * The max health for this member.
     */
    private final float maxHealth;

    /**
     * The current health for this member.
     */
    private float health;

    /**
     * Default constructor for deserialization.
     */
    public ExpeditionVisitorMember(final CompoundTag compound)
    {
        this.id = compound.getInt(TAG_ID);
        this.name = compound.getString(TAG_NAME);
        this.maxHealth = compound.getFloat(TAG_MAX_HEALTH);
        this.health = compound.getFloat(TAG_HEALTH);
    }

    /**
     * Default constructor.
     *
     * @param visitorDataView the visitor to create the expedition member for.
     */
    public ExpeditionVisitorMember(final IVisitorViewData visitorDataView)
    {
        this.id = visitorDataView.getId();
        this.name = visitorDataView.getName();
        this.maxHealth = (float) visitorDataView.getMaxHealth();
        this.health = (float) visitorDataView.getHealth();
    }

    @Override
    public int getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public float getHealth()
    {
        return health;
    }

    @Override
    public float getMaxHealth()
    {
        return maxHealth;
    }

    @Override
    public void heal(final IColony colony, final float amount)
    {
        this.health = Mth.clamp(this.health + amount, 0, this.maxHealth);
    }

    @Override
    public void hurt(final IColony colony, final DamageSource damageSource, final float amount)
    {
        final ICitizenData citizenData = resolveCivilianData(colony);

        float finalDamage = amount;
        if (citizenData != null)
        {
            finalDamage = IExpeditionMember.handleDamageReduction(citizenData, damageSource, finalDamage);
        }

        this.health = Mth.clamp(this.health - finalDamage, 0, this.maxHealth);
    }

    @Override
    public boolean isDead()
    {
        return this.health <= 0;
    }

    @Override
    @Nullable
    public IVisitorData resolveCivilianData(final IColony colony)
    {
        return colony.getVisitorManager().getCivilian(this.id);
    }

    @Override
    public void write(final CompoundTag compound)
    {
        compound.putInt(TAG_ID, this.id);
        compound.putString(TAG_NAME, this.name);
        compound.putFloat(TAG_MAX_HEALTH, this.maxHealth);
        compound.putFloat(TAG_HEALTH, this.health);
    }

    @Override
    public void removeFromColony(final IColony colony)
    {
        final IVisitorData visitorData = resolveCivilianData(colony);
        if (visitorData != null)
        {
            colony.getVisitorManager().removeCivilian(visitorData);
        }
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ExpeditionVisitorMember that = (ExpeditionVisitorMember) o;

        return id == that.id;
    }
}