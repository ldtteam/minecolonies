package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIKnight;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.SHIELD_USAGE;
import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;
import static com.minecolonies.api.util.constant.GuardConstants.KNIGHT_HP_BONUS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;

/**
 * The Knight's job class
 *
 * @author Asherslab
 */
public class JobKnight extends AbstractJobGuard<JobKnight>
{
    /**
     * Desc of knight job.
     */
    public static final String DESC = "com.minecolonies.coremod.job.Knight";

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobKnight(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generates the {@link AbstractEntityAIGuard} job for our knight.
     *
     * @return The AI.
     */
    @Override
    public EntityAIKnight generateGuardAI()
    {
        return new EntityAIKnight(this);
    }

    /**
     * Custom Action on Levelup, increases Knight HP
     */
    @Override
    public void onLevelUp()
    {
        // Bonus Health for knights(gets reset upon Firing)
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = getCitizen().getEntity().get();

            // +1 Heart every 2 level
            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME,
                getCitizen().getCitizenSkillHandler().getLevel(Skill.Stamina) + KNIGHT_HP_BONUS,
                AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.knight;
    }

    /**
     * Gets the name of our knight.
     *
     * @return The name.
     */
    @Override
    public String getName()
    {
        return DESC;
    }

    /**
     * Gets the {@link BipedModelType} to use for our ranger.
     *
     * @return The model to use.
     */
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.KNIGHT_GUARD;
    }

    /**
     * Reduces explosion damage when Knights have a shield and have unlocked the research to use it.
     * @param damageSource   The source of damage.
     * @return If true, ignores this damage.
     */
    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        if(damageSource.isExplosion() && this.getColony().getResearchManager().getResearchEffects().getEffectStrength(SHIELD_USAGE) > 0
                && InventoryUtils.findFirstSlotInItemHandlerWith(this.getCitizen().getInventory(), Items.SHIELD) != -1)
        {
            if (!this.getCitizen().getEntity().isPresent())
            {
                return true;
            }
            final AbstractEntityCitizen worker = this.getCitizen().getEntity().get();
            worker.getCitizenItemHandler().setHeldItem(Hand.OFF_HAND, InventoryUtils.findFirstSlotInItemHandlerWith(this.getCitizen().getInventory(), Items.SHIELD));
            worker.startUsingItem(Hand.OFF_HAND);

            // Apply the colony Flag to the shield
            ItemStack shieldStack = worker.getInventoryCitizen().getHeldItem(Hand.OFF_HAND);
            CompoundNBT nbt = shieldStack.getOrCreateTagElement("BlockEntityTag");
            nbt.put(TAG_BANNER_PATTERNS, worker.getCitizenColonyHandler().getColony().getColonyFlag());

            worker.decreaseSaturationForContinuousAction();
            return true;
        }
        return super.ignoresDamage(damageSource);
    }
}
