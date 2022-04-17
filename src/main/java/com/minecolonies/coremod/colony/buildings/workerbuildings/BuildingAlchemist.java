package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PLANTGROUND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_PLANTATION;
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
     * List of sand blocks to grow onto.
     */
    private final List<BlockPos> soulsand = new ArrayList<>();
    //todo also need one for leaves.

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
        return compound;
    }

    /**
     * Get a list of all the available working positions.
     *
     * @return the list of positions.
     */
    public List<BlockPos> getAllSoilPositions()
    {
        return soulsand;
    }

    //todo list: We want the block, the job, the workermodel, the AI

    //todo we got two types of recipes. a) Custom (based on the mistletoe) and b) Brewing.

    //todo we want in here also the position of leaves.

    //todo add the custom recipe being unlocked by the druid research. + custom potion (stackable, useless for player) for the Druid.

    //todo craft on demand (RS), and harvest/plant netherwart randomly && harvest mistletoe randomly (small chance for mistletoe).

    //todo we need special fuel handling here.

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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_PLANTATION).combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_PLANTATION);
            return isRecipeAllowed.orElse(false);
        }
    }
}
