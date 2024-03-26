package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Citizen expedition members.
 */
public final class ExpeditionCitizenMember implements IExpeditionMember<ICitizenData>
{
    /**
     * Nbt tag constants.
     */
    private static final String TAG_MAX_HEALTH = "maxHealth";
    private static final String TAG_HEALTH     = "health";
    private static final String TAG_DAMAGE     = "damage";

    /**
     * The id of the citizen.
     */
    private final int id;

    /**
     * The name of the citizen.
     */
    private final String name;

    /**
     * The max health for this member.
     */
    private final float maxHealth;

    /**
     * The damage for this member.
     */
    private final float damage;

    /**
     * The current health for this member.
     */
    private float health;

    /**
     * Default constructor for deserialization.
     */
    public ExpeditionCitizenMember(final CompoundTag compound)
    {
        this.id = compound.getInt(TAG_ID);
        this.name = compound.getString(TAG_NAME);
        this.maxHealth = compound.getFloat(TAG_MAX_HEALTH);
        this.health = compound.getFloat(TAG_HEALTH);
        this.damage = compound.getFloat(TAG_DAMAGE);
    }

    /**
     * Default constructor.
     *
     * @param citizenData the citizen to create the expedition member for.
     */
    public ExpeditionCitizenMember(final ICitizenData citizenData)
    {
        this.id = citizenData.getId();
        this.name = citizenData.getName();
        this.maxHealth = citizenData.getEntity().orElseThrow().getMaxHealth();
        this.health = this.maxHealth;
        this.damage = 0f;//InventoryUtils.getFirstWeapon(citizenData.getInventory());
    }

    /**
     * Default constructor.
     *
     * @param citizenDataView the citizen to create the expedition member for.
     */
    public ExpeditionCitizenMember(final ICitizenDataView citizenDataView)
    {
        this.id = citizenDataView.getId();
        this.name = citizenDataView.getName();
        this.maxHealth = (float) citizenDataView.getMaxHealth();
        this.health = (float) citizenDataView.getHealth();
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
    public float getAttackDamage()
    {
        return 0;
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
    public ICitizenData resolveCivilianData(final IColony colony)
    {
        return colony.getCitizenManager().getCivilian(this.id);
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
        final ICitizenData citizenData = resolveCivilianData(colony);
        if (citizenData != null)
        {
            colony.getCitizenManager().removeCivilian(citizenData);
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

        final ExpeditionCitizenMember that = (ExpeditionCitizenMember) o;

        return id == that.id;
    }
}