package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.datalistener.model.StudyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BOOKCASES;

/**
 * Creates a new building for the Library.
 */
public class BuildingLibrary extends AbstractBuilding
{
    /**
     * Description of the block used to set this block.
     */
    private static final String LIBRARY_HUT_NAME = "library";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> bookCases = new ArrayList<>();

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingLibrary(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(StudyItem::isStudyItem, new Tuple<>(64, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return LIBRARY_HUT_NAME;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);
        final ListTag furnaceTagList = compound.getList(TAG_BOOKCASES, Tag.TAG_INT_ARRAY);
        for (int i = 0; i < furnaceTagList.size(); ++i)
        {
            bookCases.add(NBTUtils.readBlockPos(furnaceTagList.get(i)));
        }
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = super.serializeNBT(provider);
        @NotNull final ListTag bookcaseTagList = new ListTag();
        for (@NotNull final BlockPos entry : bookCases)
        {
            bookcaseTagList.add(NBTUtils.writeBlockPos(entry));
        }
        compound.put(TAG_BOOKCASES, bookcaseTagList);

        return compound;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block.defaultBlockState().is(Tags.Blocks.BOOKSHELVES))
        {
            bookCases.add(pos);
        }
    }

    /**
     * Returns a random bookshelf from the list.
     *
     * @return the position of it.
     */
    public BlockPos getRandomBookShelf()
    {
        if (bookCases.isEmpty())
        {
            return getPosition();
        }
        final BlockPos returnPos = bookCases.get(MathUtils.RANDOM.nextInt(bookCases.size()));
        if (colony.getWorld().getBlockState(returnPos).is(Tags.Blocks.BOOKSHELVES))
        {
            return returnPos;
        }
        bookCases.remove(returnPos);
        return getPosition();
    }
}
