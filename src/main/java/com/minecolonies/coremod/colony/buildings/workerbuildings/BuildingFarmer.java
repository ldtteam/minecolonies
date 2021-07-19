package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.FarmerFieldModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class which handles the farmer building.
 */
public class BuildingFarmer extends AbstractBuildingWorker implements IBuildingPublicCrafter
{
    /**
     * The beekeeper mode.
     */
    public static final ISettingKey<BoolSetting> FERTILIZE = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "fertilize"));

    /**
     * Descriptive string of the profession.
     */
    private static final String FARMER = "farmer";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingFarmer(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public IJob<?> createJob(@Nullable final ICitizenData citizen)
    {
        for (FarmerFieldModule module : getModules(FarmerFieldModule.class))
        {
            if (citizen != null && !module.getFarmerFields().isEmpty())
            {
                for (@NotNull final BlockPos field : module.getFarmerFields())
                {
                    final TileEntity scareCrow = getColony().getWorld().getBlockEntity(field);
                    if (scareCrow instanceof ScarecrowTileEntity)
                    {
                        ((ScarecrowTileEntity) scareCrow).setOwner(citizen.getId());
                    }
                }
            }
        }
        return new JobFarmer(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return FARMER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Stamina;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Athletics;
    }

    @Override
    public boolean canBeGathered()
    {
        // Normal crafters are only gatherable when they have a task, i.e. while producing stuff.
        // BUT, the farmer both gathers and crafts things now, like the lumberjack
        return true;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory. When the inventory is full, everything get's dumped into the building chest. But you can use this
     * method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());
        for (FarmerFieldModule module : getModules(FarmerFieldModule.class))
        {
            for (final BlockPos field : module.getFarmerFields())
            {
                final TileEntity scareCrow = getColony().getWorld().getBlockEntity(field);
                if (scareCrow instanceof ScarecrowTileEntity && !ItemStackUtils.isEmpty(((ScarecrowTileEntity) scareCrow).getSeed()))
                {
                    final ItemStack seedStack = ((ScarecrowTileEntity) scareCrow).getSeed();
                    toKeep.put(seedStack::sameItem, new Tuple<>(64, true));
                }
            }
        }
        return toKeep;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.farmer;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return FARMER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
    }

    /**
     * Getter for request fertilizer
     */
    public boolean requestFertilizer()
    {
        return getSetting(FERTILIZE).getValue();
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        for (FarmerFieldModule module : getModules(FarmerFieldModule.class))
        {
            for (final BlockPos field : module.getFarmerFields())
            {
                final TileEntity scareCrow = getColony().getWorld().getBlockEntity(field);
                if (scareCrow instanceof ScarecrowTileEntity && !ItemStackUtils.isEmpty(((ScarecrowTileEntity) scareCrow).getSeed()))
                {
                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(((ScarecrowTileEntity) scareCrow).getSeed(), stack))
                    {
                        return false;
                    }
                }
            }
        }

        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * Provides a view of the farmer building class.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        @NotNull
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, FARMER);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobFarmer(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, FARMER).orElse(false);
        }
    }
}
