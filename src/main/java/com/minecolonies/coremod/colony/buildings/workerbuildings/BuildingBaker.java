package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Building for the bakery.
 */
public class BuildingBaker extends AbstractBuildingFurnaceUser implements IBuildingPublicCrafter
{
    /**
     * General bakery description key.
     */
    private static final String BAKER = "baker";

    /**
     * Max hut level of the bakery.
     */
    private static final int BAKER_HUT_MAX_LEVEL = 5;

    /**
     * Constructor for the bakery building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBaker(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Baker schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BAKER;
    }

    /**
     * Gets the max level of the bakery's hut.
     *
     * @return The max level of the bakery's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return BAKER_HUT_MAX_LEVEL;
    }

    @Override
    protected boolean keepFood()
    {
        return false;
    }

    /**
     * Create a Baker job.
     *
     * @param citizen the citizen to take the job.
     * @return The new Baker job.
     */
    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobBaker(citizen);
    }

    /**
     * The name of the bakery's job.
     *
     * @return The name of the bakery's job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return BAKER;
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
        return Skill.Dexterity;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * The client view for the bakery building.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * The client view constructor for the bakery building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return A blockui window.
         */
        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, BAKER);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        /**
         * Always try to keep at least 2 stacks of recipe inputs in the inventory and in the worker chest.
         */
        private static final int RECIPE_INPUT_HOLD = 128;

        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobBaker(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, BAKER);
            return isRecipeAllowed.orElse(false);
        }

        @Override
        public boolean canLearnCraftingRecipes()
        {
            if (building == null) return true;  // because it can learn at *some* level
            return building.getBuildingLevel() >= 3;
        }

        @Override
        public boolean addRecipe(final IToken<?> token)
        {
            final boolean recipeAdded = super.addRecipe(token);

            if(recipeAdded)
            {
                final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

                ItemStack smeltResult = FurnaceRecipes.getInstance().getSmeltingResult(storage.getPrimaryOutput());

                if(smeltResult != null)
                {
                    final IRecipeStorage smeltingRecipe =  StandardFactoryController.getInstance().getNewInstance(
                      TypeConstants.RECIPE,
                      StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                      ImmutableList.of(new ItemStorage(storage.getPrimaryOutput().copy())),
                      1,
                      smeltResult,
                      Blocks.FURNACE);
                    addRecipeToList(IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(smeltingRecipe), false);
                }
            }

            return recipeAdded;
        }

        @Override
        public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
        {
            final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> map = super.getRequiredItemsAndAmount();
            for (final IToken<?> token : getRecipes())
            {
                final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
                for (final ItemStorage itemStorage : storage.getCleanedInput())
                {
                    final ItemStack stack = itemStorage.getItemStack();
                    map.put(stack::sameItem, new Tuple<>(RECIPE_INPUT_HOLD, false));
                }
            }

            return map;
        }
    }

    @Override
    public Skill getCraftSpeedSkill()
    {
        return getSecondarySkill();
    }
}
