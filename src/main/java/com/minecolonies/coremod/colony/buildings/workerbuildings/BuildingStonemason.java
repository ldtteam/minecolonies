package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobStonemason;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the stonemason building.
 */
public class BuildingStonemason extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String STONEMASON = "Stonemason";

    /**
     * The min percentage something has to have out of stone to be craftable by this worker.
     */
    private static final double MIN_PERCENTAGE_TO_CRAFT = 0.75;

    /**
     * Instantiates a new stonemason building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingStonemason(final Colony c, final BlockPos l)
    {
        super(c, l);
    }


    @NotNull
    @Override
    public String getSchematicName()
    {
        return STONEMASON;
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
        return new JobStonemason(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return STONEMASON;
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

        double amountOfValidBlocks = 0;
        double blocks = 0;


        if (storage.getPrimaryOutput().getItem() instanceof ItemBlock)
        {
            final Block block = ((ItemBlock) storage.getPrimaryOutput().getItem()).getBlock();
            if (block == Blocks.STONEBRICK
                  || block == Blocks.STONE_BRICK_STAIRS
                  || block == Blocks.STONE_SLAB
                  || block == Blocks.STONE_SLAB2
                  || block == Blocks.STONE
                  || block == Blocks.SANDSTONE
                  || block == Blocks.RED_SANDSTONE)
            {
                return true;
            }
            else if (block == Blocks.END_BRICKS)
            {
                return false;
            }
        }
        else if (storage.getPrimaryOutput().getItem() == Items.FLOWER_POT)
        {
            return true;
        }

        for(final ItemStack stack : storage.getInput())
        {
            if(!ItemStackUtils.isEmpty(stack))
            {
                blocks++;
                if (stack.getItem() instanceof ItemBlock)
                {
                    final Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (block == Blocks.STONEBRICK || block == Blocks.STONE_BRICK_STAIRS || block == Blocks.STONE_SLAB || block == Blocks.STONE_SLAB2)
                    {
                        amountOfValidBlocks++;
                        continue;
                    }
                }

                for(final int id: OreDictionary.getOreIDs(stack))
                {
                    final String name = OreDictionary.getOreName(id);
                    if(name.contains("stone"))
                    {
                        amountOfValidBlocks++;
                        break;
                    }
                    else if(name.contains("stick") || name.contains("wood") || name.toLowerCase(Locale.US).contains("redstone") || name.contains("string") || name.contains("gunpowder"))
                    {
                        return false;
                    }
                }
            }
        }

        return amountOfValidBlocks > 0 && amountOfValidBlocks/blocks > MIN_PERCENTAGE_TO_CRAFT;
    }

    /**
     * Crafter building View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {

        /**
         * Instantiate the stonemason view.
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
            return new WindowHutWorkerPlaceholder<>(this, STONEMASON);
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
