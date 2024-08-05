package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHEARS, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack ->  itemStack.getItem() == Items.NETHER_WART, new Tuple<>(16, false));
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
    public void registerBlockPosition(@NotNull final BlockState block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.getBlock() == Blocks.SOUL_SAND)
        {
            soulsand.add(pos);
        }
        else if (block.is(BlockTags.LEAVES))
        {
            leaves.add(pos);
        }
        else if (block.getBlock() == Blocks.BREWING_STAND)
        {
            brewingStands.add(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        final ListTag sandPos = compound.getList(TAG_PLANTGROUND, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < sandPos.size(); ++i)
        {
            soulsand.add(NBTUtils.readBlockPos(sandPos.getCompound(i), TAG_POS));
        }

        final ListTag leavesPos = compound.getList(TAG_LEAVES, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < leavesPos.size(); ++i)
        {
            leaves.add(NBTUtils.readBlockPos(leavesPos.getCompound(i), TAG_POS));
        }

        final ListTag brewingStandPos = compound.getList(TAG_BREWING_STAND, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < brewingStandPos.size(); ++i)
        {
            brewingStands.add(NBTUtils.readBlockPos(brewingStandPos.getCompound(i), TAG_POS));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        @NotNull final ListTag sandCompoundList = new ListTag();
        for (@NotNull final BlockPos entry : soulsand)
        {
            @NotNull final CompoundTag sandCompound = new CompoundTag();
            sandCompound.put(TAG_POS, NBTUtils.writeBlockPos(entry));
            sandCompoundList.add(sandCompound);
        }
        compound.put(TAG_PLANTGROUND, sandCompoundList);

        @NotNull final ListTag leavesCompoundList = new ListTag();
        for (@NotNull final BlockPos entry : leaves)
        {
            @NotNull final CompoundTag leaveCompound = new CompoundTag();
            leaveCompound.put(TAG_POS, NBTUtils.writeBlockPos(entry));
            leavesCompoundList.add(leaveCompound);
        }
        compound.put(TAG_LEAVES, leavesCompoundList);

        @NotNull final ListTag brewingStandCompoundList = new ListTag();
        for (@NotNull final BlockPos entry : brewingStands)
        {
            @NotNull final CompoundTag brewingStandCompound = new CompoundTag();
            brewingStandCompound.put(TAG_POS, NBTUtils.writeBlockPos(entry));
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

    /**
     * Remove soil position.
     * @param pos the position of it.
     */
    public void removeSoilPosition(final BlockPos pos)
    {
        soulsand.remove(pos);
    }

    /**
     * Remove leaf position.
     * @param pos the position of it.
     */
    public void removeLeafPosition(final BlockPos pos)
    {
        leaves.remove(pos);
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
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return Collections.emptySet();
        }

        @Override
        public @NotNull List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull final Level world)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));

            // growing mistletoe
            recipes.add(new GenericRecipe(null, new ItemStack(ModItems.mistletoe),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    1, Blocks.OAK_LEAVES,
                    null, ToolType.SHEARS, Collections.emptyList(), -1));

            // growing netherwart
            recipes.add(new GenericRecipe(null, new ItemStack(Items.NETHER_WART, 4),
                    Collections.emptyList(),
                    Collections.singletonList(Collections.singletonList(new ItemStack(Items.NETHER_WART))),
                    1, Blocks.SOUL_SAND,
                    null, ToolType.NONE, Collections.emptyList(), -1));

            return recipes;
        }
    }
}
