package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobSawmill;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the sawmill building.
 */
public class BuildingSawmill extends AbstractBuildingWorker implements IBuildingPublicCrafter
{
    /**
     * Description string of the building.
     */
    protected static final String SAWMILL = "sawmill";

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
    public BuildingSawmill(final IColony c, final BlockPos l)
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
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobSawmill(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return SAWMILL;
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

    /**
     * Sawmill View.
     */
    public static class View extends AbstractBuildingWorkerView
    {

        /**
         * Instantiate the sawmill view.
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
            return new WindowHutWorkerModulePlaceholder<>(this, SAWMILL);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobSawmill(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, SAWMILL);
            if (isRecipeAllowed.isPresent()) return isRecipeAllowed.get();

            double amountOfValidBlocks = 0;
            double blocks = 0;
            for (final List<ItemStack> stacks : recipe.getInputs())
            {
                // just check the first alternative for now
                if (stacks.isEmpty()) continue;
                final ItemStack stack = stacks.get(0);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    if (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS))
                    {
                        amountOfValidBlocks += stack.getCount();
                        continue;
                    }
                    for (final ResourceLocation tag : stack.getItem().getTags())
                    {
                        if (tag.getPath().contains("wood"))
                        {
                            amountOfValidBlocks += stack.getCount();
                            break;
                        }
                    }
                    blocks += stack.getCount();
                }
            }

            return amountOfValidBlocks > 0 && amountOfValidBlocks / blocks > MIN_PERCENTAGE_TO_CRAFT;
        }
    }

    public static class DOCraftingModule extends AbstractCraftingBuildingModule.Custom
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobSawmill(null));
        }

        @Override
        public boolean isRecipeCompatible(final @NotNull IGenericRecipe recipe)
        {
            final ItemStack stack = recipe.getPrimaryOutput().copy();
            if (stack.getItem().getRegistryName().getNamespace().equals("domum_ornamentum"))
            {
                final CompoundTag dataNbt = stack.getOrCreateTagElement("textureData");
                final MaterialTextureData textureData = MaterialTextureData.deserializeFromNBT(dataNbt);
                for (final Block block : textureData.getTexturedComponents().values())
                {
                    final ItemStack ingredientStack = new ItemStack(block);
                    if (!ItemStackUtils.isEmpty(ingredientStack) && (ItemTags.PLANKS.contains(ingredientStack.getItem()) || ItemTags.LOGS.contains(ingredientStack.getItem())))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canLearnCraftingRecipes() { return true; }

        @Override
        public boolean canLearnFurnaceRecipes() { return false; }

        @Override
        public boolean canLearnLargeRecipes() { return true; }
    }
}
