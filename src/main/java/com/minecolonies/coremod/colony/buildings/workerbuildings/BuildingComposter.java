package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowHutComposter;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobComposter;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BuildingComposter extends AbstractFilterableListBuilding
{
    /**
     * Description of the job for this building
     */
    private static final String COMPOSTER = "Composter";

    /**
     * Maximum building level
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Tag to store the barrel position.
     */
    private static final String TAG_POS = "pos";

    /**
     * Tag to store the barrel list.
     */
    private static final String TAG_BARRELS = "barrels";

    /**
     * Tag to store the if dirt should be retrieved.
     */
    private static final String TAG_DIRT = "dirt";

    /**
     * If the composter should retrieve dirt from his compost bin.
     */
    private boolean retrieveDirtFromCompostBin = false;

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> barrels = new ArrayList<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingComposter(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
        keepX.put((stack) -> isAllowedItem("compostables", new ItemStorage(stack)), new Tuple<>(Integer.MAX_VALUE, true));
    }

    /**
     * Return a list of barrels assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getBarrels()
    {
        return ImmutableList.copyOf(barrels);
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobComposter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COMPOSTER;
    }

    @Override
    public String getSchematicName()
    {
        return COMPOSTER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block == ModBlocks.blockBarrel && !barrels.contains(pos))
        {
            barrels.add(pos);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList compostBinTagList = new NBTTagList();
        for (@NotNull final BlockPos entry : barrels)
        {
            @NotNull final NBTTagCompound compostBinCompound = new NBTTagCompound();
            compostBinCompound.setTag(TAG_POS, NBTUtil.createPosTag(entry));
            compostBinTagList.appendTag(compostBinCompound);
        }
        compound.setTag(TAG_BARRELS, compostBinTagList);
        compound.setBoolean(TAG_DIRT, retrieveDirtFromCompostBin);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList compostBinTagList = compound.getTagList(TAG_BARRELS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < compostBinTagList.tagCount(); ++i)
        {
            barrels.add(NBTUtil.getPosFromTag(compostBinTagList.getCompoundTagAt(i).getCompoundTag(TAG_POS)));
        }
        if (compound.hasKey(TAG_DIRT))
        {
            retrieveDirtFromCompostBin = compound.getBoolean(TAG_DIRT);
        }
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(retrieveDirtFromCompostBin);
    }

    /**
     * If the composter should retrieve dirt and not compost from the compost bin.
     * @return true if so.
     */
    public boolean shouldRetrieveDirtFromCompostBin()
    {
        return retrieveDirtFromCompostBin;
    }

    /**
     * Set if the composter should retrieve dirt and not compost from the compost bin.
     * @param shouldRetrieveDirt whether or not to retrieve dirt..
     */
    public void setShouldRetrieveDirtFromCompostBin(final boolean shouldRetrieveDirt)
    {
        this.retrieveDirtFromCompostBin = shouldRetrieveDirt;
        markDirty();
    }

    /**
     * The client side representation of the building.
     */
    public static class View extends AbstractFilterableListsView
    {
        /**
         * If the composter should retrieve dirt from his compost bin.
         */
        public boolean retrieveDirtFromCompostBin = false;

        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            retrieveDirtFromCompostBin = buf.readBoolean();
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutComposter(this);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.DEXTERITY;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }
}
