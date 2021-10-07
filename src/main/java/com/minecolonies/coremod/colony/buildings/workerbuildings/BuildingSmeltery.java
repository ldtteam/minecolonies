package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class BuildingSmeltery extends AbstractBuildingFurnaceUser
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

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobSmelter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return "smelter";
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Athletics;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Strength;
    }

    @SuppressWarnings(MAGIC_NUMBERS_SHOULD_NOT_BE_USED)
    public int ingotMultiplier(final int skillLevel, final Random random)
    {
        int fortuneLevel;
        switch (getBuildingLevel())
        {
            case 2: // Building Level 2 - Default: Fortune 0, Chance for Fortune 1
                fortuneLevel = random.nextInt(ONE_HUNDRED_PERCENT - skillLevel / 2) == 0 ? 1 : 0; break;
            case 3: // Building Level 3 - Default: Fortune 1, Chance for Fortune 2
                fortuneLevel = random.nextInt(ONE_HUNDRED_PERCENT - skillLevel) == 0 ? 2 : 1; break;
            case 4: // Building Level 4 - Default: Fortune 2, Chance for Fortune 3
                fortuneLevel = random.nextInt(ONE_HUNDRED_PERCENT - skillLevel / 2) == 0 ? 3 : 2; break;
            case 5: // Building Level 5 - Default: Fortune 3, Chance for Fortune 4
                fortuneLevel = random.nextInt(ONE_HUNDRED_PERCENT - skillLevel) == 0 ? 4 : 3; break;
            default: // Building Level 1 - Default: Fortune 0
                fortuneLevel = 0; break;
        }
        int multiplier = random.nextInt(fortuneLevel + 2) - 1;
        return multiplier > 0 ? 1 + multiplier : 1;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.smeltery;
    }

    /**
     * Smelter building View.
     */
    public static class View extends AbstractBuildingWorkerView
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
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobSmelter(null));
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
