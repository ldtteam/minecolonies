package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesNamedGrave;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.tileentities.TileEntityNamedGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.WindowHutGraveyard;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobUndertaker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.TAG_STRING;
import static com.minecolonies.api.util.constant.SchematicTagConstants.GRAVE;
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
    private static final String HUNDERTAKER = "undertaker";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * NBTTag to store the grave.
     */
    private static final String TAG_GRAVES = "graves";

    /**
     * NBTTag to store grave data.
     */
    private static final String TAG_GRAVE_DATA = "gravedata";

    /**
     * NBTTag to store the grave BlockPos.
     */
    private static final String TAG_GRAVES_BLOCKPOS = "gravesPos";

    /**
     * The last field tag.
     */
    private static final String TAG_CURRENT_GRAVE = "currentGRAVE";

    /**
     * The tag to store the list of resting citizen in this graveyard
     */
    private static final String TAG_RIP_CITIZEN_LIST = "ripCitizenList";

      /**
     * The list of the graves the undertaker need to collect.
     */
    private final Set<BlockPos> pendingGraves = new HashSet<>();

    /**
     * The list of resting citizen in this graveyard.
     */
    private final List<String> restingCitizen = new ArrayList<>();

    /**
     * The grave the undertaker is currently collecting.
     */
    @Nullable
    private BlockPos currentGrave;

    /**
     * The data of the last grave dug by the undertaker.
     */
    @Nullable
    private GraveData lastGraveData;

    /**
     * Whether we did init tags
     */
    private boolean initTags = false;

    /**
     * Grave positions
     */
    private List<Tuple<BlockPos, Direction>> visualGravePositions;

    /**
     * The graveyard random.
     */
    private Random random = new Random();

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
     * Returns list of graves that undertaker need to dig.
     *
     * @return a list of graves objects.
     */
    @NotNull
    public List<BlockPos> getPendingGraves()
    {
        return new ArrayList<>(pendingGraves);
    }

    /**
     * Assigns a grave list to the pending grave list.
     *
     * @param grave the grave to add.
     */
    public void addGrave(final BlockPos grave)
    {
        final TileEntity tileEntity = getColony().getWorld().getTileEntity(grave);
        if (tileEntity instanceof TileEntityGrave)
        {
            pendingGraves.add(grave);
            this.markDirty();
        }
    }

    /**
     * Clear the current grave the undertaker is currently working on.
     */
    public void ClearCurrentGrave()
    {
        this.currentGrave = null;
    }

    /**
     * Retrieves a random grave to work on for the undertaker.
     *
     * @return a field to work on.
     */
    @Nullable
    public BlockPos getGraveToWorkOn()
    {
        if(currentGrave != null)
        {
            final TileEntity tileEntity = getColony().getWorld().getTileEntity(currentGrave);
            if(tileEntity == null)
            {
                pendingGraves.remove(currentGrave);
                currentGrave = null;
            }
            else
            {
                return currentGrave;
            }
        }

        final List<BlockPos> graves = new ArrayList<>(pendingGraves);
        for (@NotNull final BlockPos grave : graves)
        {
            final TileEntity tileEntity = getColony().getWorld().getTileEntity(grave);
            if(tileEntity == null)
            {
                pendingGraves.remove(grave);
            }

            if (tileEntity instanceof TileEntityGrave)
            {
                currentGrave = grave;
                return currentGrave;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public IJob<?> createJob(@Nullable final ICitizenData citizen)
    {
        return new JobUndertaker(citizen);
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

        restingCitizen.clear();
        if (compound.keySet().contains(TAG_RIP_CITIZEN_LIST))
        {
            final ListNBT ripCitizen = compound.getList(TAG_RIP_CITIZEN_LIST, TAG_STRING);
            for (int i = 0; i < ripCitizen.size(); i++)
            {
                final String citizenName = ripCitizen.getString(i);
                restingCitizen.add(citizenName);
            }
        }

        if (compound.keySet().contains(TAG_GRAVE_DATA))
        {
            lastGraveData = new GraveData();
            lastGraveData.read(compound.getCompound(TAG_GRAVE_DATA));
        }
        else lastGraveData = null;
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

        @NotNull final ListNBT ripCitizen = new ListNBT();
        for (@NotNull final String citizenName : restingCitizen)
        {
            ripCitizen.add(StringNBT.valueOf(citizenName));
        }
        compound.put(TAG_RIP_CITIZEN_LIST, ripCitizen);

        if(lastGraveData != null)
        {
            compound.put(TAG_GRAVE_DATA, lastGraveData.write());
        }

        return compound;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return HUNDERTAKER;
}

    @NotNull
    @Override
    public Skill getPrimarySkill() { return Skill.Strength; }

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
        initTags = false;
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

        final List<BlockPos> graves = new ArrayList<>(pendingGraves);
        final List<BlockPos> cleanList = new ArrayList<>();

        for (@NotNull final BlockPos grave : graves)
        {
            if (colony.getWorld().isAreaLoaded(grave, 1))
            {
                final TileEntity tileEntity = getColony().getWorld().getTileEntity(grave);
                if (tileEntity instanceof TileEntityGrave)
                {
                    cleanList.add(grave);
                }
            }
        }

        for (final BlockPos pos : pendingGraves)
        {
            if (!cleanList.contains(pos))
            {
                Log.getLogger().warn("Pending Grave not considered because not loaded: " + pos.toString());
            }
        }

        //pending grave list
        buf.writeInt(cleanList.size());
        for (@NotNull final BlockPos grave : cleanList)
        {
            buf.writeBlockPos(grave);
        }

        //resting citizen list
        buf.writeInt(restingCitizen.size());
        for (@NotNull final String citizenName : restingCitizen)
        {
            buf.writeString(citizenName);
        }
        buf.writeInt(restingCitizen.size());
    }

    /**
     * Add a citizen to the list of resting citizen in this graveyard
     */
    public void buryCitizenHere(final Tuple<BlockPos, Direction> positionAndDirection)
    {
        if(lastGraveData != null && !restingCitizen.contains(lastGraveData.getCitizenName()))
        {
            //TODO: For now the facing is random - we could read it from the blueprint placeholder light block facing
            Direction facing = Direction.getRandomDirection(random);
            if(facing == Direction.UP || facing == Direction.DOWN)
            {
                facing = Direction.NORTH; //prevent a bug that will no exist after previous TODO TG done
            }
            getColony().getWorld().setBlockState(positionAndDirection.getA(), ModBlocks.blockNamedGrave.getDefaultState().with(AbstractBlockMinecoloniesNamedGrave.FACING, facing));

            TileEntity tileEntity = getColony().getWorld().getTileEntity(positionAndDirection.getA());
            if (tileEntity instanceof TileEntityNamedGrave)
            {
                final String firstName = StringUtils.split(lastGraveData.getCitizenName())[0];
                final String lastName = lastGraveData.getCitizenName().replaceFirst(firstName,"");

                final ArrayList<String> lines = new ArrayList<>();
                lines.add(firstName);
                lines.add(lastName);
                if (lastGraveData.getCitizenJobName() != null) { lines.add(lastGraveData.getCitizenJobName()); }
                ((TileEntityNamedGrave) tileEntity).setTextLines(lines);
            }

            restingCitizen.add(lastGraveData.getCitizenName());
            markDirty();
        }
    }

    public void setLastGraveData(final GraveData graveData)
    {
        this.lastGraveData = graveData;
        markDirty();
    }

    public GraveData getLastGraveData()
    {
        return this.lastGraveData;
    }

    /**
     * Reads the tag positions
     */
    public void initTagPositions()
    {
        if (initTags)
        {
            return;
        }

        if (getTileEntity() != null)
        {
            initTags = true;
            visualGravePositions = new ArrayList<>();
            for (final Map.Entry<BlockPos, List<String>> entry : getTileEntity().getWorldTagPosMap().entrySet())
            {
                if (entry.getValue().get(0).startsWith(GRAVE))
                {
                    final Direction direction = Direction.NORTH;
                    visualGravePositions.add(new Tuple<>(entry.getKey(), direction));
                }
            }
        }
    }

    /**
     * Has the graveyard space for new graves
     * @return
     */
    public boolean hasSpaceForNewGraves()
    {
        return getRandomFreeVisualGravePos() != null;
    }

    /**
     * Return a random free visual grave position
     */
    public Tuple<BlockPos, Direction> getRandomFreeVisualGravePos()
    {
        initTagPositions();

        if (visualGravePositions.isEmpty())
        {
            return null;
        }

        final List<Tuple<BlockPos, Direction>> availablePos = new ArrayList<Tuple<BlockPos, Direction>>();
        for(final Tuple<BlockPos, Direction> tuple : visualGravePositions)
        {
            if(getColony().getWorld().getBlockState(tuple.getA()).isAir())
            {
                availablePos.add(tuple);
            }
        }

        if (availablePos.isEmpty())
        {
            return null;
        }

        Collections.shuffle(availablePos);
        return availablePos.get(0);
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
         * Contains a view object of all the restingCitizen in the colony.
         */
        @NotNull
        private List<String> restingCitizen = new ArrayList<>();

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
            super.deserialize(buf);

            graves = new ArrayList<>();
            final int size = buf.readInt();
            for (int i = 1; i <= size; i++)
            {
                @NotNull final BlockPos pos = buf.readBlockPos();
                graves.add(pos);
            }

            restingCitizen = new ArrayList<>();
            final int sizeRIP = buf.readInt();
            for (int i = 1; i <= sizeRIP; i++)
            {
                restingCitizen.add(buf.readString());
            }
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
         * Getter of the restingCitizen list.
         *
         * @return an unmodifiable List.
         */
        @NotNull
        public List<String> getRestingCitizen()
        {
            return Collections.unmodifiableList(restingCitizen);
        }
    }
}
