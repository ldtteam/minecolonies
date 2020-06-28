package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutEnchanter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobEnchanter;
import com.minecolonies.coremod.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The enchanter building.
 */
public class BuildingEnchanter extends AbstractBuildingWorker
{
    /**
     * Enchanter.
     */
    private static final String ENCHANTER = "enchanter";

    /**
     * Maximum building level
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * List of buildings the enchanter gathers experience from.
     */
    private Map<BlockPos, Boolean> buildingToGatherFrom = new HashMap<>();

    /**
     * The random variable.
     */
    private Random random = new Random();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingEnchanter(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put((stack) -> stack.getItem() == ModItems.ancientTome, new Tuple<>(STACKSIZE, true));
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobEnchanter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return ENCHANTER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Mana;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Knowledge;
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
        buildingToGatherFrom.put(blockPos, false);
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
    public void onWakeUp()
    {
        final Set<BlockPos> keys = new HashSet<>(buildingToGatherFrom.keySet());
        buildingToGatherFrom.clear();
        keys.forEach(k -> buildingToGatherFrom.put(k, false));
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        buildingToGatherFrom.clear();
        NBTUtils.streamCompound(compound.getList(TAG_GATHER_LIST, Constants.NBT.TAG_COMPOUND))
                                      .map(this::deserializeListElement)
                                      .forEach(t -> buildingToGatherFrom.put(t.getA(), t.getB()));
    }

    /**
     * Helper to deserialize a list element from nbt.
     * @param nbtTagCompound the compound to deserialize from.
     * @return the resulting blockPos/boolean tuple.
     */
    private Tuple<BlockPos, Boolean> deserializeListElement(final CompoundNBT nbtTagCompound)
    {
        final BlockPos pos = BlockPosUtil.read(nbtTagCompound, TAG_POS);
        final boolean gatheredAlready = nbtTagCompound.getBoolean(TAG_GATHERED_ALREADY);
        return new Tuple<>(pos, gatheredAlready);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        compound.put(TAG_GATHER_LIST, buildingToGatherFrom.entrySet().stream().map(this::serializeListElement).collect(NBTUtils.toListNBT()));
        return compound;
    }

    private CompoundNBT serializeListElement(final Map.Entry<BlockPos, Boolean> entry)
    {
        final CompoundNBT compound = new CompoundNBT();
        BlockPosUtil.write(compound,TAG_POS, entry.getKey());
        compound.putBoolean(TAG_GATHERED_ALREADY, entry.getValue());
        return compound;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(buildingToGatherFrom.size());
        for (final BlockPos pos : buildingToGatherFrom.keySet())
        {
            buf.writeBlockPos(pos);
        }
    }

    /**
     * Return the set of the buildings to gather from.
     * @return a copy of th eset.
     */
    public Set<BlockPos> getBuildingsToGatherFrom()
    {
        return new HashSet<>(buildingToGatherFrom.keySet());
    }

    /**
     * Get a random worker building id to gather xp from.
     * @return the unique pos id of it.
     */
    @Nullable
    public BlockPos getRandomBuildingToDrainFrom()
    {
        final List<BlockPos> buildings = buildingToGatherFrom.entrySet().stream().filter(k -> !k.getValue()).map(Map.Entry::getKey).collect(Collectors.toList());
        if (buildings.isEmpty())
        {
            return null;
        }
        return buildings.get(random.nextInt(buildings.size()));
    }

    /**
     * Set the building as gathered.
     * @param pos the pos of the building.
     */
    public void setAsGathered(final BlockPos pos)
    {
        buildingToGatherFrom.put(pos, true);
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
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            final int size = buf.readInt();
            buildingToGatherFrom.clear();
            for (int i = 0; i < size; i++)
            {
                buildingToGatherFrom.add(buf.readBlockPos());
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

        /**
         * Add a new worker to gather xp from.
         * @param blockPos the pos of the building.
         */
        public void addWorker(final BlockPos blockPos)
        {
            buildingToGatherFrom.add(blockPos);
            Network.getNetwork().sendToServer(new EnchanterWorkerSetMessage(this, blockPos, true));
        }

        /**
         * Remove a worker to stop gathering from.
         * @param blockPos the pos of that worker.
         */
        public void removeWorker(final BlockPos blockPos)
        {
            buildingToGatherFrom.remove(blockPos);
            Network.getNetwork().sendToServer(new EnchanterWorkerSetMessage(this, blockPos, false));
        }
    }
}
