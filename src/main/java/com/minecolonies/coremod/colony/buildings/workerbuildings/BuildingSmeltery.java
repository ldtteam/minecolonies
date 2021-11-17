package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.Suppression.MAGIC_NUMBERS_SHOULD_NOT_BE_USED;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the smeltery building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingSmeltery extends AbstractBuilding
{
    /**
     * The smelter string.
     */
    private static final String SMELTERY_DESC = "smeltery";

    /**
     * Max building level of the smeltery.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Amount of swords and armor to keep at the worker.
     */
    private static final int STUFF_TO_KEEP = 10;

    /**
     * Instantiates a new smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSmeltery(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(IColonyManager.getInstance().getCompatibilityManager()::isOre, new Tuple<>(Integer.MAX_VALUE, true));
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack)
                             && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof DiggerItem || stack.getItem() instanceof ArmorItem)
          , new Tuple<>(STUFF_TO_KEEP, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SMELTERY_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @SuppressWarnings(MAGIC_NUMBERS_SHOULD_NOT_BE_USED)
    public int ingotMultiplier(final int skillLevel, final Random random)
    {
        switch (getBuildingLevel())
        {
            case 1:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel / 2) == 0 ? DOUBLE : 1;
            case 2:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel) == 0 ? DOUBLE : 1;
            case 3:
                return 2;
            case 4:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel / 2) == 0 ? TRIPLE : DOUBLE;
            case 5:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel) == 0 ? TRIPLE : DOUBLE;
            default:
                return 1;
        }
    }

    /**
     * Smelter building View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiate the smeltery view.
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
        public BOWindow getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, "smelter");
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public SmeltingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            // all "recipes" are handled by the AI, and queried via the job
            return false;
        }

        @Override
        public boolean isVisible()
        {
            return false;
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly()
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly());

            final ICompatibilityManager compatibility = IColonyManager.getInstance().getCompatibilityManager();
            for (final ItemStack stack : compatibility.getListOfAllItems())
            {
                if (ItemStackUtils.IS_SMELTABLE.and(compatibility::isOre).test(stack))
                {
                    final ItemStack output = FurnaceRecipes.getInstance().getSmeltingResult(stack);
                    recipes.add(createSmeltingRecipe(new ItemStorage(stack), output, Blocks.FURNACE));
                }
            }
            return recipes;
        }

        private static IGenericRecipe createSmeltingRecipe(final ItemStorage input, final ItemStack output, final Block intermediate)
        {
            return GenericRecipe.of(StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    Collections.singletonList(input),
                    1,
                    output,
                    intermediate));
        }
    }
}
