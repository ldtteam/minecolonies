package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.nbt.CompoundTag;
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
    private static final String TAG_MAX_HEALTH  = "maxHealth";
    private static final String TAG_HEALTH      = "health";
    private static final String TAG_INVENTORY   = "inventory";
    private static final String TAG_WEAPON_TYPE = "weapon_type";

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
     * The inventory of the citizen.
     */
    private final InventoryCitizen inventory;

    /**
     * Get the weapon type that the member should be using.
     */
    private final EquipmentTypeEntry weaponType;

    /**
     * The current health for this member.
     */
    private float health;

    /**
     * The slot that the weapon was detected in.
     */
    private Integer cachedWeaponSlot;

    /**
     * Default constructor for deserialization.
     */
    public ExpeditionCitizenMember(final CompoundTag compound)
    {
        this.id = compound.getInt(TAG_ID);
        this.name = compound.getString(TAG_NAME);
        this.maxHealth = compound.getFloat(TAG_MAX_HEALTH);
        this.inventory = new InventoryCitizen(this.name, true);
        this.inventory.read(compound.getCompound(TAG_INVENTORY));
        this.weaponType = ModEquipmentTypes.getRegistry().getValue(EquipmentTypeEntry.parseResourceLocation(compound.getString(TAG_WEAPON_TYPE)));
        this.health = compound.getFloat(TAG_HEALTH);
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
        this.inventory = citizenData.getInventory();
        this.weaponType = IGuardTypeDataManager.getInstance().getFrom(citizenData.getJob().getJobRegistryEntry().getKey()).getPrimaryWeapon();
        this.health = this.maxHealth;
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
    public void setHealth(final float health)
    {
        this.health = health;
    }

    @Override
    public float getMaxHealth()
    {
        return maxHealth;
    }

    @Override
    public boolean isDead()
    {
        return this.health <= 0;
    }

    @Override
    public ItemStack getPrimaryWeapon()
    {
        if (cachedWeaponSlot == null)
        {
            cachedWeaponSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(inventory, weaponType, 0, 5);
        }

        return inventory.getStackInSlot(cachedWeaponSlot);
    }

    @Override
    public InventoryCitizen getInventory()
    {
        return inventory;
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
        final CompoundTag inventoryTag = new CompoundTag();
        inventory.write(inventoryTag);
        compound.put(TAG_INVENTORY, inventoryTag);
        compound.putString(TAG_WEAPON_TYPE, weaponType.getRegistryName().toString());
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
