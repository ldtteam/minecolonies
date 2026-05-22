package com.minecolonies.core.colony.jobs;

import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.entity.ai.workers.guard.EntityAICavalry;
import com.minecolonies.core.util.AttributeModifierUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.SHIELD_USAGE;
import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;
import static com.minecolonies.api.util.constant.GuardConstants.CAVALRY_HP_BONUS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;

import java.util.UUID;

/**
 * The Cavalry job class.
 */
public class JobCavalry extends AbstractJobGuard<JobCavalry>
{
    public static final float MOUNT_DAMAGE_SPLIT = .20f;
    public static final int DININGHALL_HORSE_PARKING_RANGE = 40;
    private static final String TAG_MOUNT = "mount";

    /**
     * The UUID of the mount.
     */
    protected UUID myMount = null;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobCavalry(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public EntityAICavalry generateGuardAI()
    {
        return new EntityAICavalry(this);
    }

    /**
     * Fired when level increases.
     */
    @Override
    public void onLevelUp()
    {
        // Bonus Health for cavalry matches knights (gets reset upon Firing)
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = getCitizen().getEntity().get();

            // +1 Heart every 2 level
            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME,
                getCitizen().getCitizenSkillHandler().getLevel(Skill.Stamina) + CAVALRY_HP_BONUS,
                AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }

    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.KNIGHT_GUARD_ID;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        if (myMount != null)
        {
            compound.putUUID(TAG_MOUNT, myMount);
        }

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        myMount = compound.contains(TAG_MOUNT) ? compound.getUUID(TAG_MOUNT) : null;
    }

    /**
     * Whether the citizen will ignore damage from the given source.
     * Units will ignore explosion and projectile damage if they have a shield in their offhand and the SHIELD_USAGE research is enabled.
     *
     * @param damageSource the source of the damage
     * @return true if the citizen will ignore the damage, false otherwise
     */
    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        boolean applicableDamageSource = damageSource.is(DamageTypeTags.IS_EXPLOSION) || damageSource.is(DamageTypeTags.IS_PROJECTILE);

        if(applicableDamageSource && this.getColony().getResearchManager().getResearchEffects().getEffectStrength(SHIELD_USAGE) > 0
                && InventoryUtils.findFirstSlotInItemHandlerWith(this.getCitizen().getInventory(), Items.SHIELD) != -1)
        {
            if (!this.getCitizen().getEntity().isPresent())
            {
                return true;
            }
            final AbstractEntityCitizen worker = this.getCitizen().getEntity().get();
            CitizenItemUtils.setHeldItem(worker, InteractionHand.OFF_HAND, InventoryUtils.findFirstSlotInItemHandlerWith(this.getCitizen().getInventory(), Items.SHIELD));
            worker.startUsingItem(InteractionHand.OFF_HAND);

            // Apply the colony Flag to the shield
            ItemStack shieldStack = worker.getInventoryCitizen().getHeldItem(InteractionHand.OFF_HAND);
            CompoundTag nbt = shieldStack.getOrCreateTagElement("BlockEntityTag");
            nbt.put(TAG_BANNER_PATTERNS, worker.getCitizenColonyHandler().getColonyOrRegister().getColonyFlag());

            worker.decreaseSaturationForContinuousAction();
            return true;
        }
        return super.ignoresDamage(damageSource);
    }

    /**
     * If the cavalry unity is missing a mount.
     *
     * @return true if so.
     */
    public boolean isMissingMount()
    {
        return myMount == null;
    }

    /**
     * Sets the mount UUID.
     *
     * @param mountUUID the mount UUID.
     */
    public void setMount(final UUID mountUUID)
    {
        this.myMount = mountUUID;
    }

    /**
     * Return the current mount for the cavalry job.
     *
     * @return
     */
    public UUID getMount()
    {
        return this.myMount;
    }

    /**
     * The fraction of damage that is applied to the mount instead of the rider.
     * This is used to calculate the damage to apply to the mount when the rider is attacked.
     * @return the fraction of damage to apply to the mount.
     */
    public float getMountDamageSplit()
    {
        return MOUNT_DAMAGE_SPLIT;
    }

    /**
     * Gets the weapon type that the AI will look for when checking if it can attack.
     *
     * @return the weapon type.
     */
    public static EquipmentTypeEntry getWeaponType()
    {
        return ModEquipmentTypes.spear.get();
    }
}
