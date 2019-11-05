package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobStonemason;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.Tags;
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
    private static final String STONEMASON = "stonemason";

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
    public BuildingStonemason(final IColony c, final BlockPos l)
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


        if (storage.getPrimaryOutput().getItem() instanceof BlockItem)
        {
            final Item item = storage.getPrimaryOutput().getItem();
            if (item.isIn(Tags.Items.STONE) ||
                    item.isIn(Tags.Items.COBBLESTONE) ||
                    item.isIn(ItemTags.STONE_BRICKS))
            {
                return true;
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
                if (stack.getItem() instanceof BlockItem)
                {
                    final Block block = ((BlockItem) stack.getItem()).getBlock();
                    if (block.isIn(Tags.Blocks.STONE) ||
                            block.isIn(Tags.Blocks.COBBLESTONE) ||
                            block.isIn(BlockTags.STONE_BRICKS) ||
                            block.asItem().getRegistryName().getPath().contains("smooth_stone") ||
                            block.asItem().getRegistryName().getPath().contains("sandstone_slab") )
                    {
                        amountOfValidBlocks++;
                        continue;
                    }
                }

                for (final ResourceLocation tag : stack.getItem().getTags())
                {
                    if(tag.getPath().contains("stone"))
                    {
                        amountOfValidBlocks++;
                        break;
                    }
                    else if(tag.getPath().contains("stick") || tag.getPath().contains("wood") || tag.getPath().toLowerCase(Locale.US).contains("redstone") || tag.getPath().contains("string") || tag.getPath().contains("gunpowder"))
                    {
                        return false;
                    }
                }
            }
        }

        return amountOfValidBlocks > 0 && amountOfValidBlocks/blocks > MIN_PERCENTAGE_TO_CRAFT;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.stoneMason;
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
