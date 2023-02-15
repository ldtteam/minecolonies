package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.modules.MinimumStockModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

public class BuildingNetherWorker extends AbstractBuilding
{

    /**
     * Settings
     */
    public static final ISettingKey<BoolSetting>  CLOSE_PORTAL = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "closeportal"));

    /**
     * 
     * Constant name for the Netherworker building
     */
    private static final String NETHER_WORKER = "netherworker";

    /**
     * How many trips we've run this period
     */
    private static final String TAG_CURRENT_TRIPS = "current_trips";

    /**
     * Which day in the period is it? 
     */
    private static final String TAG_CURRENT_DAY = "current_day";

    /**
     * How many trips we can make per period by default
     */
    private static final int MAX_PER_PERIOD = 1;

    /**
     * How many days are in a period by default
     */
    private static final int PERIOD_DAYS = 3;

    /**
     * Exclusion list id.
     */
    public static final String FOOD_EXCLUSION_LIST = "food";


    /**
     * Which day we're at in the current period
     */
    private int currentPeriodDay = 0;

    /**
     * How many trips we've done in the current period
     */
    private int currentTrips = 0;

    /**
     * ServerTime for the last 'day' snapshot, to track days when doDaylightCycle is not happening.
     */
    private long snapTime;

    public BuildingNetherWorker(@NotNull IColony colony, BlockPos pos)
    {
        super(colony, pos);

        keepX.put(this::isAllowedFood, new Tuple<>(STACKSIZE, true));

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SWORD, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));

        keepX.put(itemStack -> itemStack.getItem() instanceof FlintAndSteelItem, new Tuple<>(1, true));

        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof final ArmorItem armor
                && armor.getSlot() == EquipmentSlot.HEAD, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof final ArmorItem armor
                && armor.getSlot() == EquipmentSlot.CHEST, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof final ArmorItem armor
                && armor.getSlot() == EquipmentSlot.LEGS, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof final ArmorItem armor
                && armor.getSlot() == EquipmentSlot.FEET, new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return NETHER_WORKER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    /**
     * Should the portal be closed on return? 
     */
    public boolean shallClosePortalOnReturn()
    {
        return getSetting(CLOSE_PORTAL).getValue();
    }

    @Override
    public void onWakeUp()
    {
        super.onWakeUp();
        snapTime = colony.getWorld().getDayTime();
        if(this.currentPeriodDay < getPeriodDays())
        {
            this.currentPeriodDay++;
        }
        else
        {
            this.currentPeriodDay = 0;
            this.currentTrips = 0;
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if(compound.contains(TAG_CURRENT_TRIPS))
        {
            this.currentTrips = compound.getInt(TAG_CURRENT_TRIPS);
        }

        if(compound.contains(TAG_CURRENT_DAY))
        {
            this.currentPeriodDay = compound.getInt(TAG_CURRENT_DAY);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        compound.putInt(TAG_CURRENT_TRIPS, this.currentTrips);
        compound.putInt(TAG_CURRENT_DAY, this.currentPeriodDay);

        return compound;
    }

    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory, final JobEntry jobEntry)
    {
        if (stack.isEmpty())
        {
            return 0;
        }

        if (inventory && getFirstModuleOccurance(MinimumStockModule.class).isStocked(stack))
        {
            return stack.getCount();
        }

        // Check for materials needed to go to the Nether: 
        IRecipeStorage rs = getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class).getFirstRecipe(ItemStack::isEmpty);
        if(rs != null)
        {
            final ItemStorage kept = new ItemStorage(stack);
            boolean containsItem = rs.getInput().contains(kept);
            int keptCount = localAlreadyKept.stream().filter(storage -> storage.equals(kept)).mapToInt(ItemStorage::getAmount).sum();
            if(containsItem  && (keptCount < STACKSIZE || !inventory))
            {
                if (localAlreadyKept.contains(kept))
                {
                    kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
                }
                localAlreadyKept.add(kept);
                return 0;
            }
        }

        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory, jobEntry);
    }


    /**
     * Return whether the given stack is allowed food
     * @param stack the stack
     * @return true if so
     */
    public boolean isAllowedFood(ItemStack stack)
    {
        ItemListModule listModule = this.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST));
        return ISFOOD.test(stack) && !listModule.isItemInList(new ItemStorage(stack)) && !ItemStackUtils.ISCOOKABLE.test(stack);
    }

    /**
     * Check to see if it's valid to do a trip by checking how many done in this current period
     * @return true if the worker can go to the nether
     */
    public boolean isReadyForTrip()
    {
        if(snapTime == 0)
        {
            snapTime = colony.getWorld().getDayTime();
        }
        if(Math.abs(colony.getWorld().getDayTime() - snapTime) >= 24000)
        {
            //Make sure we're incrementing if day/night cycle isn't running. 
            this.currentPeriodDay++;
        }
        return this.currentTrips < getMaxPerPeriod();
    }

    /**
     * Let the building know we're doing a trip
     */
    public void recordTrip()
    {
        this.currentTrips++;
    }

    /**
     * Get the tagged location that the worker should walk to in the portal. 
     * This should be a 'air block' in the portal that can directly be checked to see if the portal is open
     * @return the block above the tag, null if not available
     */
    public BlockPos getPortalLocation()
    {
        BlockPos portalLocation = getFirstLocationFromTag("portal");
        if(portalLocation != null) {
            return portalLocation.above();
        }
        return null;
    }

    /**
     * Get the tagged location where the worker can hide while "away" in the nether
     * 
     * @return the tagged location, null if not available.
     */
    public BlockPos getVaultLocation()
    {
        return getFirstLocationFromTag("vault");
    }

    /**
     * Get the max per period, potentially modified by research
     * @return
     */
    public static int getMaxPerPeriod()
    {
        return MAX_PER_PERIOD;
    }

    /**
     * Get how many days are in a period, potentially modified by research.
     * @return
     */
    public static int getPeriodDays()
    {
        return PERIOD_DAYS;
    }

    /**
     * On initial construction or reset request, excludes the tagged food by default.
     *
     * @param listModule The food exclusion module.
     */
    public static void onResetFoodExclusionList(final ItemListModule listModule)
    {
        listModule.clearItems();
        for (final Item item : ForgeRegistries.ITEMS.tags().getTag(ModTags.excludedFood))
        {
            listModule.addItem(new ItemStorage(new ItemStack(item)));
        }
    }

    @Override
    public void onPlacement()
    {
        super.onPlacement();
        final Level world = colony.getWorld();
        if(WorldUtil.isNetherType(world))
        {
            final Block block = world.getBlockState(this.getPosition()).getBlock();
            block.destroy(world, getPosition(), world.getBlockState(getPosition()));
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Custom
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
    }
}
