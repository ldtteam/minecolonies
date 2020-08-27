package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import org.jetbrains.annotations.NotNull;

/**
 * Block of the combat academy camp.
 */
public class BlockHutCombatAcademy extends AbstractBlockHut<BlockHutCombatAcademy>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutcombatacademy";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.combatAcademy;
    }

    @Override
    public void onItemCrafted(final IColony colony, final PlayerEntity player)
    {
        if (colony == null || player == null)
        {
            return;
        }
        if (colony.getResearchManager().getResearchEffects().getEffect("Combat Academy", UnlockBuildingResearchEffect.class) == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.crafted", "combat academy"));
        }
    }
}
