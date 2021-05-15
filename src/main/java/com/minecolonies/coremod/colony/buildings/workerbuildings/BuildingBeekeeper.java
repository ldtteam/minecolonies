package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.util.ResourceLocation;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.huts.WindowHutBeekeeperModule;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobBeekeeper;
import com.minecolonies.coremod.network.messages.server.colony.building.beekeeper.BeekeeperSetHarvestHoneycombsMessage;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

public class BuildingBeekeeper extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String BEEKEEPER = "beekeeper";

    /**
     * List of hives.
     */
    private Set<BlockPos> hives = new HashSet<>();

    /**
     * Whether the beekeeper should harvest honeycombs or honey bottles
     */
    private BeekeeperSetHarvestHoneycombsMessage.HarvestType harvestHoneycombs = BeekeeperSetHarvestHoneycombsMessage.HarvestType.HONEYCOMBS;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingBeekeeper(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(stack -> Items.SHEARS == stack.getItem(), new Tuple<>(1, true));
        keepX.put(stack -> Items.GLASS_BOTTLE == stack.getItem(), new Tuple<>(4, true));
        keepX.put(stack -> ItemTags.FLOWERS.contains(stack.getItem()), new Tuple<>(STACKSIZE,true));
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobBeekeeper(citizen);
    }

    /**
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return BEEKEEPER;
    }

    /**
     * Primary skill getter.
     *
     * @return the primary skill.
     */
    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Dexterity;
    }

    /**
     * Secondary skill getter.
     *
     * @return the secondary skill.
     */
    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Adaptability;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.beekeeper;
    }

    /**
     * Children must return the name of their structure.
     *
     * @return StructureProxy name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BEEKEEPER;
    }

    /**
     * Children must return their max building level.
     *
     * @return Max building level.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        NBTUtils.streamCompound(compound.getList(NbtTagConstants.TAG_HIVES, Constants.NBT.TAG_COMPOUND))
          .map(NBTUtil::readBlockPos)
          .forEach(this.hives::add);
        this.harvestHoneycombs = BeekeeperSetHarvestHoneycombsMessage.HarvestType.values()[compound.getInt(NbtTagConstants.TAG_HARVEST_HONEYCOMBS)];
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = super.serializeNBT();
        nbt.put(NbtTagConstants.TAG_HIVES, this.hives.stream().map(NBTUtil::writeBlockPos).collect(NBTUtils.toListNBT()));
        nbt.putInt(NbtTagConstants.TAG_HARVEST_HONEYCOMBS, this.harvestHoneycombs.ordinal());
        return nbt;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeEnumValue(harvestHoneycombs);
    }

    /**
     * Get the hives/nests positions that belong to this beekeper
     *
     * @return te set of positions of hives/nests that belong to this beekeeper
     */
    public Set<BlockPos> getHives()
    {
        return Collections.unmodifiableSet(new HashSet<>(hives));
    }

    /**
     * Remove a hive/nest position from this beekeper
     *
     * @param pos the position to remove
     */
    public void removeHive(final BlockPos pos)
    {
        hives.remove(pos);
    }

    /**
     * Add a hive/nest position to this beekeper
     *
     * @param pos the position to add
     */
    public void addHive(final BlockPos pos)
    {
        hives.add(pos);
    }

    /**
     * Set the hives/nests positions that belong to this beekeeper
     *
     * @param hives the new set of positions of hives/nests that belong to this beekeeper
     */
    public void setHives(final Set<BlockPos> hives)
    {
        this.hives = hives;
    }

    /**
     * Set whether the beekeeper should harvest honeycombs
     *
     * @param harvestHoneycombs what materials the beekeeper should harvest
     */
    public void setHarvestHoneycombs(final BeekeeperSetHarvestHoneycombsMessage.HarvestType harvestHoneycombs)
    {
        this.harvestHoneycombs = harvestHoneycombs;
    }

    /**
     * Get what materials the beekeeper should harvest.
     *
     * @return honeycomb, honey bottle, or both
     */
    public BeekeeperSetHarvestHoneycombsMessage.HarvestType getHarvestTypes()
    {
        return harvestHoneycombs;
    }

    /**
     * Get the maximum amount of hives that can belong to this beekeeper
     *
     * @return the number of maximum hives that can belong to this beekeeper
     */
    public int getMaximumHives()
    {
        return (int) Math.pow(2, getBuildingLevel() - 1);
    }

    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Harvest honeycombs or not.
         */
        private BeekeeperSetHarvestHoneycombsMessage.HarvestType harvestHoneycombs = BeekeeperSetHarvestHoneycombsMessage.HarvestType.HONEYCOMBS;

        /**
         * Creates a building view.
         *
         * @param c ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        @Nullable
        @Override
        public Window getWindow()
        {
            return new WindowHutBeekeeperModule(this);
        }

        public BeekeeperSetHarvestHoneycombsMessage.HarvestType isHarvestHoneycombs()
        {
            return harvestHoneycombs;
        }

        public void setHarvestHoneycombs(final BeekeeperSetHarvestHoneycombsMessage.HarvestType harvestHoneycombs)
        {
            Network.getNetwork().sendToServer(new BeekeeperSetHarvestHoneycombsMessage(this, harvestHoneycombs));
            this.harvestHoneycombs = harvestHoneycombs;
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            harvestHoneycombs = buf.readEnumValue(BeekeeperSetHarvestHoneycombsMessage.HarvestType.class);
        }
    }
}
