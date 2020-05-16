package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobMechanic;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the mechanic building.
 */
public class BuildingMechanic extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String MECHANIC = "mechanic";

    /**
     * Instantiates a new mechanic building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingMechanic(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return MECHANIC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobMechanic(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return MECHANIC;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Knowledge;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Agility;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
        if(!super.canRecipeBeAdded(token))
        {
            return false;
        }

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if(storage == null)
        {
            return false;
        }


        if (storage.getPrimaryOutput().getItem().getRegistryName().getPath().contains("ice")
              || ItemTags.RAILS.contains(storage.getPrimaryOutput().getItem())
              ||  storage.getPrimaryOutput().getItem() instanceof MinecartItem
              || storage.getPrimaryOutput().getItem() == Items.JACK_O_LANTERN
              || (storage.getPrimaryOutput().getItem() instanceof BlockItem && ((BlockItem) storage.getPrimaryOutput().getItem()).getBlock() instanceof HopperBlock)
              || Tags.Items.STORAGE_BLOCKS.contains(storage.getPrimaryOutput().getItem())
              || storage.getPrimaryOutput().getItem() == Items.ENCHANTING_TABLE
              || storage.getPrimaryOutput().getItem() == Items.LANTERN)
        {
            return true;
        }

        boolean hasValidItem = false;

        for(final ItemStack stack : storage.getInput())
        {
            if (Tags.Items.DUSTS_REDSTONE.contains(stack.getItem()) || Tags.Items.ORES_REDSTONE.contains(stack.getItem()) || Tags.Items.STORAGE_BLOCKS_REDSTONE.contains(stack.getItem()))
            {
                hasValidItem = true;
            }
        }

        return hasValidItem;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.mechanic;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Mechanic", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * Mechanic View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {

        /**
         * Instantiate the mechanic view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, MECHANIC);
        }
    }
}
