package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobSawmill;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the sawmill building.
 */
public class BuildingSawmill extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String SAWMILL = "Sawmill";

    /**
     * The min percentage something has to have out of wood to be craftable by this worker.
     */
    private static final double MIN_PERCENTAGE_TO_CRAFT = 0.75;

    /**
     * Instantiates a new sawmill building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSawmill(final Colony c, final BlockPos l)
    {
        super(c, l);
    }


    @NotNull
    @Override
    public String getSchematicName()
    {
        return SAWMILL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobSawmill(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return SAWMILL;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
        if(!super.canRecipeBeAdded(token))
        {
            return false;
        }

        final IRecipeStorage storage = ColonyManager.getRecipeManager().getRecipes().get(token);
        if(storage == null)
        {
            return false;
        }

        int amountOfValidBlocks = 0;
        int blocks = 0;
        for(final ItemStack stack : storage.getInput())
        {
            if(!ItemStackUtils.isEmpty(stack))
            {
                for(final int id: OreDictionary.getOreIDs(stack))
                {
                    final String name = OreDictionary.getOreName(id);
                    if(name.contains("Wood"))
                    {
                        amountOfValidBlocks++;
                    }
                    else if(name.contains("ingot") || name.contains("stone") || name.contains("redstone") || name.contains("string"))
                    {
                        return false;
                    }
                }
                blocks++;
            }
        }

        final Item item = storage.getPrimaryOutput().getItem();
        if (item instanceof ItemBlock && (((ItemBlock) item).getBlock() instanceof BlockShingle || ((ItemBlock) item).getBlock() instanceof BlockShingleSlab))
        {
            return true;
        }

        return amountOfValidBlocks > 0 && blocks/amountOfValidBlocks > MIN_PERCENTAGE_TO_CRAFT;
    }

    /**
     * Sawmill View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {

        /**
         * Instantiate the sawmill view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, SAWMILL);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.ENDURANCE;
        }
    }
}
