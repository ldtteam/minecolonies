package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_WATER;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class of the concrete mason building.
 */
public class BuildingConcreteMixer extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String CONCRETE_MIXER = "concretemixer";

    /**
     * How deep the water can max be to place concrete in it.
     */
    private static final int WATER_DEPTH_SUPPORT = 5;

    /**
     * Water position list.
     */
    private final Map<Integer, List<BlockPos>> waterPos = new HashMap<>();

    /**
     * The minimum found water level
     */
    private int minWaterLevel = WATER_DEPTH_SUPPORT;

    /**
     * Instantiates a new concrete mason building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingConcreteMixer(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        if (!blockState.getFluidState().isEmpty() && (blockState.isAir() || blockState.getBlock() == Blocks.WATER))
        {
            if (blockState.getFluidState().getType() == Fluids.FLOWING_WATER && blockState.getFluidState().getAmount() <= WATER_DEPTH_SUPPORT)
            {
                final List<BlockPos> fluidPos = waterPos.getOrDefault(blockState.getFluidState().getAmount(), new ArrayList<>());
                if (!fluidPos.contains(pos))
                {
                    fluidPos.add(pos);
                }
                waterPos.put(blockState.getFluidState().getAmount(), fluidPos);
                minWaterLevel = Math.min(minWaterLevel, blockState.getFluidState().getAmount());
            }
        }

        super.registerBlockPosition(blockState, pos, world);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = super.serializeNBT(provider);

        @NotNull final ListTag waterMap = new ListTag();
        for (@NotNull final Map.Entry<Integer, List<BlockPos>> entry : waterPos.entrySet())
        {
            final CompoundTag waterCompound = new CompoundTag();

            waterCompound.putInt(TAG_LEVEL, entry.getKey());

            @NotNull final ListTag waterList = new ListTag();
            for (@NotNull final BlockPos pos : entry.getValue())
            {
                waterList.add(NBTUtils.writeBlockPos(pos));
            }
            waterCompound.put(TAG_WATER, waterList);
            waterMap.add(waterCompound);
        }
        compound.put(TAG_WATER, waterMap);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);

        waterPos.clear();
        final ListTag waterMapList = compound.getList(TAG_WATER, Tag.TAG_COMPOUND);
        for (int i = 0; i < waterMapList.size(); ++i)
        {
            final CompoundTag waterCompound = waterMapList.getCompound(i);
            final int level = waterCompound.getInt(TAG_LEVEL);
            minWaterLevel = Math.min(minWaterLevel, level);

            final ListTag waterTagList = waterCompound.getList(TAG_WATER, Tag.TAG_INT_ARRAY);
            final List<BlockPos> water = new ArrayList<>();
            for (int j = 0; j < waterTagList.size(); ++j)
            {
                final BlockPos waterPos = NBTUtils.readBlockPos( waterTagList.getCompound(j));
                if (!water.contains(waterPos))
                {
                    water.add(waterPos);
                }
            }
            waterPos.put(level, water);
        }
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CONCRETE_MIXER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    /**
     * Check if there are open positions to mine.
     *
     * @return the open position if so.
     */
    @Nullable
    public BlockPos getBlockToMine()
    {
        for (int i = 1; i <= minWaterLevel; i++)
        {
            for (final BlockPos pos : waterPos.getOrDefault(i, Collections.emptyList()))
            {
                if (colony.getWorld().getBlockState(pos).is(ModTags.concreteBlocks))
                {
                    return pos;
                }
            }
        }
        
        return null;
    }

    /**
     * Check if there are open positions to place.
     *
     * @return the open position if so.
     */
    @Nullable
    public BlockPos getBlockToPlace()
    {
        for (int i = 1; i <= minWaterLevel; i++)
        {
            for (final BlockPos pos : waterPos.getOrDefault(i, Collections.emptyList()))
            {
                final BlockState state = colony.getWorld().getBlockState(pos);
                if (!state.getFluidState().isEmpty() && state.getBlock() == Blocks.WATER)
                {
                    return pos;
                }
            }
        }
 
        return null;
    }

    /**
     * Get how much of an itemStack we already placed in the world.
     *
     * @param primaryOutput the block to check for.
     * @return the total count.
     */
    public int outputBlockCountInWorld(final ItemStack primaryOutput)
    {
        int count = 0;
        if (primaryOutput.getItem() instanceof BlockItem)
        {
            for (int i = 1; i <= minWaterLevel; i++)
            {
                for (final BlockPos pos : waterPos.getOrDefault(i, Collections.emptyList()))
                {
                    if (((BlockItem) primaryOutput.getItem()).getBlock() == colony.getWorld().getBlockState(pos).getBlock())
                    {
                        count++;
                    }
                }
            }
        }

        return count;
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

        @Override
        public boolean canRecipeBeAdded(@NotNull final IToken<?> token)
        {
            return false;
        }
    }
}
