package com.minecolonies.core.entity.ai.workers.guard;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.jobs.JobCavalry;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;

import net.minecraft.resources.ResourceLocation;
import static com.minecolonies.api.util.constant.GuardConstants.CAVALRY_DAMAGE_MULTIPLIER;
import static com.minecolonies.api.util.constant.GuardConstants.CAVALRY_RANGE_MULTIPLIER;

public class CavalryCombatAI extends KnightCombatAI
{
    /**
     * Combat icon
     */
    private final static VisibleCitizenStatus CAVALRY_COMBAT_ICON =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/cavalry_combat.png"), "com.minecolonies.gui.visiblestatus.cavalry_combat");


    public CavalryCombatAI(final EntityCitizen owner, final ITickRateStateMachine<?> stateMachine, final AbstractEntityAIGuard<?, ?> parentAI)
    {
        super(owner, stateMachine, parentAI);
    }

    @Override
    protected double getAttackDamage()
    {
        // TODO: Allow this to improve through research
        return super.getAttackDamage() * CAVALRY_DAMAGE_MULTIPLIER;
    }

    /**
     * Gets the weapon type that the AI will look for when checking if it can attack.
     *
     * @return the weapon type.
     */
    @Override
    public EquipmentTypeEntry getWeaponType()
    {
        return JobCavalry.getWeaponType();
    }


    /**
     * Get the attack distance for cavalry units.
     * 
     * @return the attack distance, increased by {@link #CAVALRY_RANGE_MULTIPLIER}.
     */
    protected double getAttackDistance()
    {
        return super.getAttackDistance() * CAVALRY_RANGE_MULTIPLIER;
    }

    /**
     * Get the icon to display when in combat.
     *
     * @return the icon.
     */
    @Override
    protected VisibleCitizenStatus getCombatStatus()
    {
        return CAVALRY_COMBAT_ICON;
    }

}