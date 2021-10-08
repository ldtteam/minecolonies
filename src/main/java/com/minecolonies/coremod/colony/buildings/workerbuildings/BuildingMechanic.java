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
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobMechanic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the mechanic building.
 */
public class BuildingMechanic extends AbstractBuildingWorker implements IBuildingPublicCrafter
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
    public IJob<?> createJob(final ICitizenData citizen)
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

    /**
     * Mechanic View.
     */
    public static class View extends AbstractBuildingWorkerView
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
        public BOWindow getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, MECHANIC);
        }
    }

    /**
     * Mechanic crafting module.
     */
    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobMechanic(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, MECHANIC);
            if (isRecipeAllowed.isPresent()) { return isRecipeAllowed.get(); }

            final Item item = recipe.getPrimaryOutput().getItem();
            if (item instanceof MinecartItem
                    || (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof HopperBlock))
            {
                return true;
            }
            return false;
        }
    }

    public static class DOCraftingModule extends AbstractCraftingBuildingModule.Custom
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobMechanic(null));
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
                    if (!ItemStackUtils.isEmpty(ingredientStack)
                          && !ModTags.crafterIngredient.get(BuildingFletcher.FLETCHER).contains(ingredientStack.getItem())
                          && !ModTags.crafterIngredient.get(BuildingSawmill.SAWMILL).contains(ingredientStack.getItem())
                          && !ModTags.crafterIngredient.get(BuildingStonemason.STONEMASON).contains(ingredientStack.getItem())
                          && !ModTags.crafterIngredient.get(BuildingGlassblower.GLASS_BLOWER).contains(ingredientStack.getItem()))
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
