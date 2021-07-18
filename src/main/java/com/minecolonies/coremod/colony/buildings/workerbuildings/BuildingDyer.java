package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobDyer;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the dyer building.
 */
public class BuildingDyer extends AbstractBuildingFurnaceUser implements IBuildingPublicCrafter
{
    /**
     * Description string of the building.
     */
    private static final String DYER = "dyer";
    private static final String DYER_SMELTING = "dyer_smelting";

    /**
     * Instantiates a new dyer building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingDyer(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return DYER;
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
        return new JobDyer(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return DYER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Creativity;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Dexterity;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.dyer;
    }

    @Override
    public Skill getCraftSpeedSkill()
    {
        return getSecondarySkill();
    }

    /**
     * Dyer View.
     */
    public static class View extends AbstractBuildingWorkerView
    {

        /**
         * Instantiate the dyer view.
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
            return new WindowHutWorkerModulePlaceholder<>(this, DYER);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobDyer(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, DYER).orElse(false);
        }
        
        @Override
        public IRecipeStorage getFirstRecipe(Predicate<ItemStack> stackPredicate)
        {
            IRecipeStorage recipe = super.getFirstRecipe(stackPredicate);

            if(recipe == null && stackPredicate.test(new ItemStack(Items.WHITE_WOOL)))
            {
                final HashMap<ItemStorage, Integer> inventoryCounts = new HashMap<>();

                if (!building.getColony().getBuildingManager().hasWarehouse())
                {
                    return null;
                }

                final List<ItemStorage> woolItems = ItemTags.WOOL.getValues().stream()
                                                      .filter(item -> !item.equals(Items.WHITE_WOOL))
                                                      .map(i -> new ItemStorage(new ItemStack(i))).collect(Collectors.toList());

                for(ItemStorage color : woolItems)
                {
                    for(IBuilding wareHouse: building.getColony().getBuildingManager().getWareHouses())
                    {
                        final int colorCount = InventoryUtils.getCountFromBuilding(wareHouse, color);
                        inventoryCounts.put(color, inventoryCounts.getOrDefault(color, 0) + colorCount);
                    }
                }

                ItemStorage woolToUse = inventoryCounts.entrySet().stream().min(java.util.Map.Entry.comparingByValue(Comparator.reverseOrder())).get().getKey();

                final IRecipeStorage tempRecipe = StandardFactoryController.getInstance().getNewInstance(
                  TypeConstants.RECIPE,
                  StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                  ImmutableList.of(woolToUse, new ItemStorage(new ItemStack(Items.WHITE_DYE, 1))),
                  1,
                  new ItemStack(Items.WHITE_WOOL, 1),
                  Blocks.AIR);

                final IToken<?> token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(tempRecipe);
                recipe = IColonyManager.getInstance().getRecipeManager().getRecipe(token);
            }
            return recipe;
        }

        @Override
        public boolean holdsRecipe(final IToken<?> token)
        {
            if (super.holdsRecipe(token))
            {
                return true;
            }

            return IColonyManager.getInstance().getRecipeManager().getRecipe(token).getPrimaryOutput().getItem() == Items.WHITE_WOOL;
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobDyer(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, DYER_SMELTING).orElse(false);
        }
    }
}
