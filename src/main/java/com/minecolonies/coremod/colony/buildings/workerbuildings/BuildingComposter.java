package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.client.gui.WindowHutComposter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.jobs.JobComposter;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.citizen.composter.EntityAIWorkComposter.COMPOSTABLE_LIST;

public class BuildingComposter extends AbstractBuildingWorker
{
    /**
     * Description of the job for this building
     */
    private static final String COMPOSTER = "composter";

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
    public BuildingComposter(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put((stack) -> this.hasModule(ItemListModule.class) && this.getModuleMatching(ItemListModule.class, m -> m.getId().equals(COMPOSTABLE_LIST)).map(m -> m.isItemInList(new ItemStorage(stack))).orElse(false), new Tuple<>(Integer.MAX_VALUE, true));
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
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobComposter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COMPOSTER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Stamina;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Athletics;
    }

    @NotNull
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
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT compostBinTagList = compound.getList(TAG_BARRELS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < compostBinTagList.size(); ++i)
        {
            barrels.add(NBTUtil.readBlockPos(compostBinTagList.getCompound(i).getCompound(TAG_POS)));
        }
        if (compound.keySet().contains(TAG_DIRT))
        {
            retrieveDirtFromCompostBin = compound.getBoolean(TAG_DIRT);
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT compostBinTagList = new ListNBT();
        for (@NotNull final BlockPos entry : barrels)
        {
            @NotNull final CompoundNBT compostBinCompound = new CompoundNBT();
            compostBinCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            compostBinTagList.add(compostBinCompound);
        }
        compound.put(TAG_BARRELS, compostBinTagList);
        compound.putBoolean(TAG_DIRT, retrieveDirtFromCompostBin);

        return compound;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(retrieveDirtFromCompostBin);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.composter;
    }

    /**
     * If the composter should retrieve dirt and not compost from the compost bin.
     *
     * @return true if so.
     */
    public boolean shouldRetrieveDirtFromCompostBin()
    {
        return retrieveDirtFromCompostBin;
    }

    /**
     * Set if the composter should retrieve dirt and not compost from the compost bin.
     *
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
    public static class View extends AbstractBuildingWorker.View
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
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
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
    }
}
