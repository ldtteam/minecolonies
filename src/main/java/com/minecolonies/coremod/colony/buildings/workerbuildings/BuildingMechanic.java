package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_MECHANIC;

/**
 * Class of the mechanic building.
 */
public class BuildingMechanic extends AbstractBuilding
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

    /**
     * Mechanic crafting module.
     */
    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_MECHANIC)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_MECHANIC);
            if (isRecipeAllowed.isPresent()) { return isRecipeAllowed.get(); }

            final Item item = recipe.getPrimaryOutput().getItem();
            return item instanceof MinecartItem
                     || (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof HopperBlock);
        }
    }

    public static class DOCraftingModule extends AbstractCraftingBuildingModule.Custom
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public DOCraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
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
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return Set.of(ModCraftingTypes.SMALL_CRAFTING, ModCraftingTypes.LARGE_CRAFTING);
        }
    }
}
