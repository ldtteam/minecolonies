package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.WindowHutGraveyard;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.colony.jobs.JobGravedigger;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class which handles the graveyard building.
 */
public class BuildingGraveyard extends AbstractBuildingWorker
{
    /**
     * Descriptive string of the building.
     */
    private static final String GRAVEYARD = "graveyard";

    /**
     * Descriptive string of the profession.
     */
    private static final String GRAVEDIGGER = "gravedigger";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * NBTTag to store the grave.
     */
    private static final String TAG_GRAVES = "graves";

    /**
     * NBTTag to store the grave BlockPos.
     */
    private static final String TAG_GRAVES_BLOCKPOS = "gravesPos";

    /**
     * The last field tag.
     */
    private static final String TAG_CURRENT_GRAVE = "currentGRAVE";

      /**
     * The list of the graves the gravedigger need to collect.
     */
    private final Set<BlockPos> pendingGraves = new HashSet<>();

    /**
     * The field the gravedigger is currently collecting.
     */
    @Nullable
    private BlockPos currentGrave;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingGraveyard(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    /**
     * Returns list of graves of the gravedigger.
     *
     * @return a list of graves objects.
     */
    @NotNull
    public List<BlockPos> getPendingGraves()
    {
        return new ArrayList<>(pendingGraves);
    }

    /**
     * Checks if the gravedigger has any graves.
     *
     * @return true if he has none.
     */
    public boolean hasNoGraves()
    {
        return pendingGraves.isEmpty();
    }

    /**
     * Assigns a grave list to the pending grave list.
     *
     * @param grave the grave to add.
     */
    public void addFarmerFields(final BlockPos grave)
    {
        final TileEntity scareCrow = getColony().getWorld().getTileEntity(grave); //TODO TG change scareCrow TileEntity
        if (scareCrow instanceof ScarecrowTileEntity)
        {
            pendingGraves.add(grave);
            this.markDirty();
        }
    }

    /**
     * Getter of the current grave.
     *
     * @return a grave object.
     */
    @Nullable
    public BlockPos getCurrentGrave()
    {
        return currentGrave;
    }

    /**
     * Sets the grave the gravedigger is currently working on.
     *
     * @param currentGrave the grave to work on.
     */
    public void setCurrentField(@Nullable final BlockPos currentGrave)
    {
        this.currentGrave = currentGrave;
    }

    /**
     * Retrieves a random grave to work on for the gravedigger.
     *
     * @param world the world it is in.
     * @return a field to work on.
     */
    @Nullable
    public BlockPos getGraveToWorkOn(final World world) //TODO TG Gravedigger should work on the oldest grave first ? or closest?
    {
        final List<BlockPos> graves = new ArrayList<>(pendingGraves);
        Collections.shuffle(graves);

        for (@NotNull final BlockPos grave : graves)
        {
            final TileEntity tileEntity = getColony().getWorld().getTileEntity(grave);
            if (tileEntity instanceof TileEntityGrave)
            {
                currentGrave = grave;
                return grave;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public IJob<?> createJob(@Nullable final ICitizenData citizen)
    {
        if (citizen != null && !pendingGraves.isEmpty())
        {
            for (@NotNull final BlockPos field : pendingGraves)
            {
                final TileEntity scareCrow = getColony().getWorld().getTileEntity(field); //TODO TG change scareCrow TileEntity
                if (scareCrow instanceof ScarecrowTileEntity)
                {
                    ((ScarecrowTileEntity) scareCrow).setOwner(citizen.getId());
                }
            }
        }
        return new JobGravedigger(citizen);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT graveTagList = compound.getList(TAG_GRAVES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < graveTagList.size(); ++i)
        {
            final CompoundNBT graveCompound = graveTagList.getCompound(i);
            final BlockPos graveLocation = BlockPosUtil.read(graveCompound, TAG_GRAVES_BLOCKPOS);
            pendingGraves.add(graveLocation);
        }

        if (compound.keySet().contains(TAG_CURRENT_GRAVE))
        {
            final BlockPos pos = BlockPosUtil.read(compound, TAG_CURRENT_GRAVE);
            currentGrave = pos;
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT fieldTagList = new ListNBT();
        for (@NotNull final BlockPos f : pendingGraves)
        {
            @NotNull final CompoundNBT fieldCompound = new CompoundNBT();
            BlockPosUtil.write(fieldCompound, TAG_GRAVES_BLOCKPOS, f);
            fieldTagList.add(fieldCompound);
        }
        compound.put(TAG_GRAVES, fieldTagList);

        if (currentGrave != null)
        {
            BlockPosUtil.write(compound, TAG_CURRENT_GRAVE, currentGrave);
        }

        return compound;
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        for (@NotNull final BlockPos grave : pendingGraves)
        {
            final TileEntity tileentity = getColony().getWorld().getTileEntity(grave);
            if (tileentity instanceof TileEntityGrave)
            {
                //TODO TG
                // ((TileEntityGrave) tileentity).setTaken(false);
                //((TileEntityGrave) tileentity).setOwner(0);
                //getColony().getWorld()
                //        .notifyBlockUpdate(tileentity.getPos(),
                //                getColony().getWorld().getBlockState(tileentity.getPos()),
                //                getColony().getWorld().getBlockState(tileentity.getPos()),
                //                BLOCK_UPDATE_FLAG);

            }
        }
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return GRAVEDIGGER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Strength;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Mana;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.graveyard;
    }

    @Override
    public void onWakeUp()
    {
        resetGraves();
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return GRAVEYARD;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        //TODO TG
    }

    /**
     * Resets the graves to need work again.
     */
    public void resetGraves()
    {
        for (@NotNull final BlockPos grave : pendingGraves)
        {
            final TileEntity tileentity = getColony().getWorld().getTileEntity(grave);
            if (tileentity instanceof TileEntityGrave)
            {
                //TODO TG ((TileEntityGrave) tileentity).setNeedsWork(true);
            }
        }
    }

    /**
     * Synchronize graves list with colony.
     *
     * @param world the world the building is in.
     */
    public void syncWithColony(@NotNull final World world)
    {
        if (!pendingGraves.isEmpty())
        {
            @NotNull final ArrayList<BlockPos> tempGraves = new ArrayList<>(pendingGraves);

            for (@NotNull final BlockPos grave : tempGraves)
            {
                final TileEntity tileEntity = world.getTileEntity(grave);
                if (tileEntity instanceof TileEntityGrave)
                {
                    //TODO TG
                   /** getColony().getWorld()
                            .notifyBlockUpdate(tileEntity.getPos(),
                                    getColony().getWorld().getBlockState(tileEntity.getPos()),
                                    getColony().getWorld().getBlockState(tileEntity.getPos()),
                                    BLOCK_UPDATE_FLAG);
                    ((TileEntityGrave) tileEntity).setTaken(true);
                    ((TileEntityGrave) tileEntity).setOwner(getMainCitizen() != null? getMainCitizen().getId() : 0);
                    ((TileEntityGrave) tileEntity).setColony(colony);**/
                }
                else
                {
                    pendingGraves.remove(grave);
                    if (currentGrave != null && currentGrave.equals(grave))
                    {
                        currentGrave = null;
                    }
                }
            }
        }
    }

    /**
     * Method called to free a grave.
     *
     * @param position id of the grave.
     */
    public void freeField(final BlockPos position)
    {
        final TileEntity scarecrow = getColony().getWorld().getTileEntity(position); //TODO TG change scarecrow
        if (scarecrow instanceof ScarecrowTileEntity)
        {
            pendingGraves.remove(position);
            /*
            ((ScarecrowTileEntity) scarecrow).setTaken(false);

            ((ScarecrowTileEntity) scarecrow).setOwner(0);
            getColony().getWorld()
                    .notifyBlockUpdate(scarecrow.getPos(),
                            getColony().getWorld().getBlockState(scarecrow.getPos()),
                            getColony().getWorld().getBlockState(scarecrow.getPos()),
                            BLOCK_UPDATE_FLAG);

             */
        }
    }

    /**
     * Method called to assign a grave to the gravedigger.
     *
     * @param position id of the grave.
     */
    public void assignGrave(final BlockPos position)
    {
        final TileEntity tileEntity = getColony().getWorld().getTileEntity(position);
        if (tileEntity instanceof TileEntityGrave)
        {
            //TODO TG
            /**
            ((TileEntityGrave) tileEntity).setTaken(true);

            if (getMainCitizen() != null)
            {
                ((TileEntityGrave) tileEntity).setOwner(getMainCitizen().getId());
            }
             **/
            pendingGraves.add(position);
        }
    }

    /**
     * Provides a view of the farmer building class.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * Contains a view object of all the graves in the colony.
         */
        @NotNull
        private List<BlockPos> graves = new ArrayList<>();

        /**
         * The amount of graves the gravedigger owns.
         */
        private int amountOfGraves;

        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        @NotNull
        public Window getWindow()
        {
            return new WindowHutGraveyard(this);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            graves = new ArrayList<>();
            super.deserialize(buf);
            final int size = buf.readInt();
            for (int i = 1; i <= size; i++)
            {
                @NotNull final BlockPos pos = buf.readBlockPos();
                graves.add(pos);
            }
            amountOfGraves = buf.readInt();
        }

        /**
         * Getter of the graves list.
         *
         * @return an unmodifiable List.
         */
        @NotNull
        public List<BlockPos> getGraves()
        {
            return Collections.unmodifiableList(graves);
        }

        /**
         * Getter for amount of graves.
         *
         * @return the amount of graves.
         */
        public int getAmountOfGraves()
        {
            return amountOfGraves;
        }
    }
}
