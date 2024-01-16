package com.minecolonies.core.entity.citizen;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Adaptation of CombatTracker to properly handle citizen death messages.
 */
public class CitizenCombatTracker extends CombatTracker
{
    public CitizenCombatTracker(EntityCitizen citizen)
    {
        super(citizen);
    }

    @Override
    @NotNull
    public Component getDeathMessage()
    {
        final IJob<?> job = citizen().getCitizenJobHandler().getColonyJob();
        Component nameComponent;
        if (job != null)
        {
            nameComponent = Component.translatable(
              TranslationConstants.WORKER_DESC,
              Component.translatable(job.getJobRegistryEntry().getTranslationKey()),
              citizen().getCitizenData().getName());
        }
        else
        {
            nameComponent = Component.translatable(
              TranslationConstants.COLONIST_DESC,
              citizen().getCitizenData().getName());
        }
        //adapted from supermethod
        CombatEntry lastEntry = getLastEntry();
        if (lastEntry == null)
        {
            return Component.translatable("death.attack.generic", nameComponent);
        }
        else
        {
            CombatEntry fall = getMostSignificantFall();
            Component attacker = lastEntry.getAttackerName();
            Entity entity = lastEntry.getSource().getEntity();
            Component result;
            if (fall != null && lastEntry.getSource() == DamageSource.FALL)
            {
                Component fallAttacker = fall.getAttackerName();
                if (fall.getSource() != DamageSource.FALL && fall.getSource() != DamageSource.OUT_OF_WORLD)
                {
                    if (fallAttacker != null && !fallAttacker.equals(attacker))
                    {
                        Entity fallEntity = fall.getSource().getEntity();
                        ItemStack stack = fallEntity instanceof LivingEntity ? ((LivingEntity) fallEntity).getMainHandItem() : ItemStack.EMPTY;
                        if (!stack.isEmpty() && stack.hasCustomHoverName())
                        {
                            result = Component.translatable("death.fell.assist.item", nameComponent, fallAttacker, stack.getDisplayName());
                        }
                        else
                        {
                            result = Component.translatable("death.fell.assist", nameComponent, fallAttacker);
                        }
                    }
                    else if (attacker != null)
                    {
                        ItemStack stack = entity instanceof LivingEntity ? ((LivingEntity) entity).getMainHandItem() : ItemStack.EMPTY;
                        if (!stack.isEmpty() && stack.hasCustomHoverName())
                        {
                            result = Component.translatable("death.fell.finish.item", nameComponent, attacker, stack.getDisplayName());
                        }
                        else
                        {
                            result = Component.translatable("death.fell.finish", nameComponent, attacker);
                        }
                    }
                    else
                    {
                        result = Component.translatable("death.fell.killer", nameComponent);
                    }
                }
                else
                {
                    result = Component.translatable("death.fell.accident." + (fall.getLocation() == null ? "generic" : fall.getLocation()), nameComponent);
                }
            }
            else
            {
                //adapted from DamageSource#getLocalizedDeathMessage
                LivingEntity killer = citizen().getKillCredit();
                String s = "death.attack." + lastEntry.getSource().msgId;
                result = killer != null
                           ? Component.translatable(killer instanceof Player ? s + ".player" : s, nameComponent, killer.getDisplayName())
                           : Component.translatable(s, nameComponent);
            }
            return result;
        }
    }

    /**
     * Helper method to cast the superclass's mob field to EntityCitizen.
     */
    private EntityCitizen citizen()
    {
        return (EntityCitizen) getMob();
    }
}
