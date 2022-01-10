package com.minecolonies.coremod.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIKnight;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
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
    public static final String DESC = "com.minecolonies.coremod.job.knight";

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

    /**
     * Gets the {@link IModelType} to use for our knight.
     *
     * @return The model to use.
     */
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.KNIGHT_GUARD_ID;
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
            worker.getCitizenItemHandler().setHeldItem(InteractionHand.OFF_HAND, InventoryUtils.findFirstSlotInItemHandlerWith(this.getCitizen().getInventory(), Items.SHIELD));
            worker.startUsingItem(InteractionHand.OFF_HAND);

            // Apply the colony Flag to the shield
            ItemStack shieldStack = worker.getInventoryCitizen().getHeldItem(InteractionHand.OFF_HAND);
            CompoundTag nbt = shieldStack.getOrCreateTagElement("BlockEntityTag");
            nbt.put(TAG_BANNER_PATTERNS, worker.getCitizenColonyHandler().getColony().getColonyFlag());

            worker.decreaseSaturationForContinuousAction();
            return true;
        }
        return super.ignoresDamage(damageSource);
    }
}
