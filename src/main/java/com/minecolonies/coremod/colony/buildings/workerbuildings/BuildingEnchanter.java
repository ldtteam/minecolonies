package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowHutEnchanter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobEnchanter;
import com.minecolonies.coremod.network.messages.EnchanterWorkerSetMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * The enchanter building.
 */
public class BuildingEnchanter extends AbstractBuildingWorker
{
    /**
     * Nbt tag for the list of buildings to gather.
     */
    private static final String TAG_GATHER_LIST = "buildingstogather";

    /**
     * Enchanter.
     */
    private static final String ENCHANTER = "Enchanter";

    /**
     * Maximum building level
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * List of buildings the enchanter gathers experience from.
     */
    private Set<BlockPos> buildingToGatherFrom = new HashSet<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingEnchanter(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put((stack) -> stack.getItem() == ModItems.compost, new Tuple<>(STACKSIZE, true));
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobEnchanter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return ENCHANTER;
    }

    @Override
    public String getSchematicName()
    {
        return ENCHANTER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.enchanter;
    }

    /**
     * Add a new worker to gather xp from.
     * @param blockPos the pos of the building.
     */
    public void addWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.add(blockPos);
        markDirty();
    }

    /**
     * Remove a worker to stop gathering from.
     * @param blockPos the pos of that worker.
     */
    public void removeWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.remove(blockPos);
        markDirty();
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);
        buildingToGatherFrom.clear();
        buildingToGatherFrom.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_GATHER_LIST, Constants.NBT.TAG_COMPOUND))
                                      .map(comp -> BlockPosUtil.readFromNBT(comp, TAG_POS))
                                      .collect(Collectors.toList()));
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        compound.setTag(TAG_GATHER_LIST, buildingToGatherFrom.stream().map(pos -> BlockPosUtil.writeToNBT(new NBTTagCompound(),TAG_POS, pos)).collect(NBTUtils.toNBTTagList()));
        return compound;
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(buildingToGatherFrom.size());
        for (final BlockPos pos : buildingToGatherFrom)
        {
            BlockPosUtil.writeToByteBuf(buf, pos);
        }
    }

    /**
     * The client side representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * List of buildings the enchanter gathers experience from.
         */
        private List<BlockPos> buildingToGatherFrom = new ArrayList<>();

        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutEnchanter(this);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            final int size = buf.readInt();
            for (int i = 0; i < size; i++)
            {
                buildingToGatherFrom.add(BlockPosUtil.readFromByteBuf(buf));
            }
        }

        /**
         * Getter for the list.
         * @return the list.
         */
        public List<BlockPos> getBuildingsToGatherFrom()
        {
            return new ArrayList<>(buildingToGatherFrom);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.CHARISMA;
        }

        /**
         * Add a new worker to gather xp from.
         * @param blockPos the pos of the building.
         */
        public void addWorker(final BlockPos blockPos)
        {
            buildingToGatherFrom.add(blockPos);
            MineColonies.getNetwork().sendToServer(new EnchanterWorkerSetMessage(this, blockPos, true));
        }

        /**
         * Remove a worker to stop gathering from.
         * @param blockPos the pos of that worker.
         */
        public void removeWorker(final BlockPos blockPos)
        {
            buildingToGatherFrom.remove(blockPos);
            MineColonies.getNetwork().sendToServer(new EnchanterWorkerSetMessage(this, blockPos, false));
        }
    }
}
