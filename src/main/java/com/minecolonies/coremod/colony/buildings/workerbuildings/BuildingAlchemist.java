package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class of the alchemist building. Crafts potions and grows netherwart.
 */
public class BuildingAlchemist extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String ALCHEMIST = "alchemist";

    /**
     * List of soul sand blocks to grow onto.
     */
    private final List<BlockPos> soulsand = new ArrayList<>();

    /**
     * List of leave blocks to gather mistletoes from.
     */
    private final List<BlockPos> leaves = new ArrayList<>();

    /**
     * List of brewing stands.
     */
    private final List<BlockPos> brewingStands = new ArrayList<>();

    /**
     * Instantiates a new plantation building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingAlchemist(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return ALCHEMIST;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block == Blocks.SOUL_SAND)
        {
            soulsand.add(pos);
        }
        else if (block.is(BlockTags.LEAVES))
        {
            leaves.add(pos);
        }
        else if (block == Blocks.BREWING_STAND)
        {
            brewingStands.add(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT sandPos = compound.getList(TAG_PLANTGROUND, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < sandPos.size(); ++i)
        {
            soulsand.add(NBTUtil.readBlockPos(sandPos.getCompound(i).getCompound(TAG_POS)));
        }

        final ListNBT leavesPos = compound.getList(TAG_LEAVES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < leavesPos.size(); ++i)
        {
            leaves.add(NBTUtil.readBlockPos(leavesPos.getCompound(i).getCompound(TAG_POS)));
        }

        final ListNBT brewingStandPos = compound.getList(TAG_BREWING_STAND, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < brewingStandPos.size(); ++i)
        {
            brewingStands.add(NBTUtil.readBlockPos(brewingStandPos.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT sandCompoundList = new ListNBT();
        for (@NotNull final BlockPos entry : soulsand)
        {
            @NotNull final CompoundNBT sandCompound = new CompoundNBT();
            sandCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            sandCompoundList.add(sandCompound);
        }
        compound.put(TAG_PLANTGROUND, sandCompoundList);

        @NotNull final ListNBT leavesCompoundList = new ListNBT();
        for (@NotNull final BlockPos entry : leaves)
        {
            @NotNull final CompoundNBT leaveCompound = new CompoundNBT();
            leaveCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            leavesCompoundList.add(leaveCompound);
        }
        compound.put(TAG_LEAVES, leavesCompoundList);

        @NotNull final ListNBT brewingStandCompoundList = new ListNBT();
        for (@NotNull final BlockPos entry : brewingStands)
        {
            @NotNull final CompoundNBT brewingStandCompound = new CompoundNBT();
            brewingStandCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            brewingStandCompoundList.add(brewingStandCompound);
        }
        compound.put(TAG_BREWING_STAND, brewingStandCompoundList);

        return compound;
    }

    /**
     * Get a list of all the available working positions.
     *
     * @return copy of the list of positions.
     */
    public List<BlockPos> getAllSoilPositions()
    {
        return new ArrayList<>(soulsand);
    }

    /**
     * Get a list of all leave positions.
     *
     * @return copy of the list of positions.
     */
    public List<BlockPos> getAllLeavePositions()
    {
        return new ArrayList<>(leaves);
    }

    /**
     * Get a list of all brewing stand positions.
     *
     * @return copy of the list of positions.
     */
    public List<BlockPos> getAllBrewingStandPositions()
    {
        return new ArrayList<>(brewingStands);
    }

    /**
     * Remove a vanished brewing stand.
     * @param pos the position of it.
     */
    public void removeBrewingStand(final BlockPos pos)
    {
        brewingStands.remove(pos);
    }

    public static class BrewingModule extends AbstractCraftingBuildingModule.Brewing
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public BrewingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }

            return recipe.getPrimaryOutput().getItem() == Items.POTION;
        }
    }

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

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
                return false;

            return recipe.getPrimaryOutput().getItem() == ModItems.magicpotion;
        }

        @Override
        public Set<CrafingType> getSupportedRecipeTypes()
        {
            return Collections.emptySet();
        }
    }
}
