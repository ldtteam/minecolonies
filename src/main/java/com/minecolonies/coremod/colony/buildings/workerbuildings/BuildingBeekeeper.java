package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutBeekeeper;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobBeekeeper;
import com.minecolonies.coremod.network.messages.server.colony.building.beekeeper.BeekeeperSetHarvestHoneycombsMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

public class BuildingBeekeeper extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String BEEKEEPER = "beekeeper";

    private static final String TAG_HIVES              = "hives";
    private static final String TAG_HARVEST_HONEYCOMBS = "harvest_honeycombs";

    /**
     * List of hives.
     */
    private Set<BlockPos> hives             = new HashSet<>();
    private boolean       harvestHoneycombs = true;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingBeekeeper(@NotNull IColony c, BlockPos l)
    {
        super(c, l);
        keepX.put(stack -> Items.SHEARS == stack.getItem(), new Tuple<>(1, true));
        keepX.put(stack -> Items.GLASS_BOTTLE == stack.getItem(), new Tuple<>(4, true));
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    @Override
    public IJob createJob(ICitizenData citizen)
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
        return Skill.Stamina; //TODO
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
        return Skill.Focus; //TODO
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
    public void deserializeNBT(CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        NBTUtils.streamCompound(compound.getList(TAG_HIVES, Constants.NBT.TAG_COMPOUND))
          .map(NBTUtil::readBlockPos)
          .forEach(this.hives::add);
        this.harvestHoneycombs = compound.getBoolean(TAG_HARVEST_HONEYCOMBS);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = super.serializeNBT();
        nbt.put(TAG_HIVES, this.hives.stream().map(NBTUtil::writeBlockPos).collect(NBTUtils.toListNBT()));
        nbt.putBoolean(TAG_HARVEST_HONEYCOMBS, this.harvestHoneycombs);
        return nbt;
    }

    @Override
    public boolean canRecipeBeAdded(IToken token)
    {
        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if (storage == null)
        {
            return false;
        }

        return storage.getInput()
                 .stream()
                 .map(ItemStack::getItem)
                 .anyMatch(item -> item == Items.HONEYCOMB || item == Items.HONEY_BOTTLE) ||
                storage.getPrimaryOutput().getItem() == Items.HONEY_BOTTLE ||
                storage.getPrimaryOutput().getItem() == Items.HONEYCOMB;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(harvestHoneycombs);
    }

    public Set<BlockPos> getHives()
    {
        return hives;
    }

    public void setHives(Set<BlockPos> hives)
    {
        this.hives = hives;
    }

    public void setHarvestHoneycombs(final boolean harvestHoneycombs)
    {
        this.harvestHoneycombs = harvestHoneycombs;
    }

    public boolean shouldHarvestHoneycombs()
    {
        return harvestHoneycombs;
    }

    public int getMaximumHives()
    {
        return (int) Math.floor(Math.pow(2, getBuildingLevel()-1));
    }

    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Harvest honeycombs or not.
         */
        private boolean harvestHoneycombs = true;

        /**
         * Creates a building view.
         *
         * @param c ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(IColonyView c, @NotNull BlockPos l)
        {
            super(c, l);
        }

        @Nullable
        @Override
        public Window getWindow()
        {
            return new WindowHutBeekeeper(this);
        }

        public void setHarvestHoneycombs(final boolean harvestHoneycombs)
        {
            Network.getNetwork().sendToServer(new BeekeeperSetHarvestHoneycombsMessage(this, harvestHoneycombs));
            this.harvestHoneycombs = harvestHoneycombs;
        }

        public boolean isHarvestHoneycombs()
        {
            return harvestHoneycombs;
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            harvestHoneycombs = buf.readBoolean();
        }
    }
}
