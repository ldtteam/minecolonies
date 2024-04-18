package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    private static final String TAG_MAX_HEALTH  = "maxHealth";
    private static final String TAG_HEALTH      = "health";
    private static final String TAG_ARMOR       = "armor";
    private static final String TAG_ARMOR_TYPE  = "type";
    private static final String TAG_ARMOR_STACK = "stack";
    private static final String TAG_WEAPON      = "weapon";

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
     * The armor pieces this member is wearing.
     */
    private final Map<EquipmentSlot, ItemStack> armor;

    /**
     * The primary weapon the member is carrying.
     */
    private ItemStack primaryWeapon;

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
        this.armor = new HashMap<>();
        final ListTag armorsCompound = compound.getList(TAG_ARMOR, Tag.TAG_COMPOUND);
        for (int i = 0; i < armorsCompound.size(); ++i)
        {
            final CompoundTag armorCompound = armorsCompound.getCompound(i);
            final EquipmentSlot equipmentSlot = EquipmentSlot.byName(armorCompound.getString(TAG_ARMOR_TYPE));
            final ItemStack itemStack = ItemStack.of(armorCompound.getCompound(TAG_ARMOR_STACK));
            this.armor.put(equipmentSlot, itemStack);
        }
        this.primaryWeapon = ItemStack.of(compound.getCompound(TAG_WEAPON));
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
        this.health = this.maxHealth;
        this.armor = new HashMap<>();
        this.armor.computeIfAbsent(EquipmentSlot.HEAD, visitorDataView.getInventory()::getArmorInSlot);
        this.armor.computeIfAbsent(EquipmentSlot.CHEST, visitorDataView.getInventory()::getArmorInSlot);
        this.armor.computeIfAbsent(EquipmentSlot.LEGS, visitorDataView.getInventory()::getArmorInSlot);
        this.armor.computeIfAbsent(EquipmentSlot.FEET, visitorDataView.getInventory()::getArmorInSlot);
        this.primaryWeapon = visitorDataView.getInventory().getHeldItem(InteractionHand.MAIN_HAND);
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
        return primaryWeapon;
    }

    @Override
    public void setPrimaryWeapon(final ItemStack itemStack)
    {
        this.primaryWeapon = itemStack;
    }

    @Override
    @NotNull
    public ItemStack getArmor(final EquipmentSlot slot)
    {
        return armor.get(slot);
    }

    @Override
    public void setArmor(final EquipmentSlot slot, final @NotNull ItemStack itemStack)
    {
        armor.put(slot, itemStack);
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
        final ListTag armorsCompound = new ListTag();
        for (final Entry<EquipmentSlot, ItemStack> armorEntry : armor.entrySet())
        {
            final CompoundTag armorCompound = new CompoundTag();
            armorCompound.putString(TAG_ARMOR_TYPE, armorEntry.getKey().getName());
            armorCompound.put(TAG_ARMOR_STACK, armorEntry.getValue().serializeNBT());
            armorsCompound.add(armorCompound);
        }
        compound.put(TAG_ARMOR, armorsCompound);
        compound.put(TAG_WEAPON, primaryWeapon.save(new CompoundTag()));
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