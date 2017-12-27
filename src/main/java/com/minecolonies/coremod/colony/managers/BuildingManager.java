package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.network.messages.ColonyViewBuildingViewMessage;
import com.minecolonies.coremod.network.messages.ColonyViewRemoveBuildingMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDINGS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NEW_FIELDS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

public class BuildingManager implements IBuildingManager
{
    /**
     * List of building in the colony.
     */
    @NotNull
    private final Map<BlockPos, AbstractBuilding> buildings = new HashMap<>();

    /**
     * List of fields of the colony.
     */
    private final List<BlockPos> fields = new ArrayList<>();

    /**
     * The warehouse building position. Initially null.
     */
    private BuildingWareHouse wareHouse = null;

    /**
     * The townhall of the colony.
     */
    @Nullable
    private BuildingTownHall townHall;

    /**
     * Variable to check if the buildings needs to be synched.
     */
    private boolean isBuildingsDirty = false;

    /**
     * Variable to check if the fields needs to be synched.
     */
    private boolean isFieldsDirty    = false;

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound, final Colony colony)
    {
        //  Buildings
        final NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.tagCount(); ++i)
        {
            final NBTTagCompound buildingCompound = buildingTagList.getCompoundTagAt(i);
            @Nullable final AbstractBuilding b = AbstractBuilding.createFromNBT(colony, buildingCompound);
            if (b != null)
            {
                addBuilding(b, colony);
            }
        }

        if(compound.hasKey(TAG_NEW_FIELDS))
        {
            // Fields before Buildings, because the Farmer needs them.
            final NBTTagList fieldTagList = compound.getTagList(TAG_NEW_FIELDS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < fieldTagList.tagCount(); ++i)
            {
                addField(BlockPosUtil.readFromNBT(fieldTagList.getCompoundTagAt(i), TAG_POS));
            }
        }

    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        //  Buildings
        @NotNull final NBTTagList buildingTagList = new NBTTagList();
        for (@NotNull final AbstractBuilding b : buildings.values())
        {
            @NotNull final NBTTagCompound buildingCompound = new NBTTagCompound();
            b.writeToNBT(buildingCompound);
            buildingTagList.appendTag(buildingCompound);
        }
        compound.setTag(TAG_BUILDINGS, buildingTagList);

        // Fields
        @NotNull final NBTTagList fieldTagList = new NBTTagList();
        for (@NotNull final BlockPos pos : fields)
        {
            @NotNull final NBTTagCompound fieldCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(fieldCompound, TAG_POS, pos);
            fieldTagList.appendTag(fieldCompound);
        }
        compound.setTag(TAG_NEW_FIELDS, fieldTagList);
    }

    @Override
    public void tick(final TickEvent.ServerTickEvent event)
    {
        for (@NotNull final AbstractBuilding b : buildings.values())
        {
            b.onServerTick(event);
        }
    }

    @Override
    public void clearDirty()
    {
        isBuildingsDirty = false;
        buildings.values().forEach(AbstractBuilding::clearDirty);
    }

    @Override
    public void sendPackets(final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers, final Set<EntityPlayerMP> subscribers)
    {
        sendBuildingPackets(oldSubscribers, hasNewSubscribers, subscribers);
        sendFieldPackets(hasNewSubscribers, subscribers);
        isBuildingsDirty = false;
        isFieldsDirty    = false;
    }

    @Override
    public void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        //  Tick Buildings
        for (@NotNull final AbstractBuilding building : buildings.values())
        {
            building.onWorldTick(event);
        }
    }

    @Override
    public void markBuildingsDirty()
    {
        isBuildingsDirty = true;
    }

    @Override
    public void cleanUpBuildings(@NotNull final TickEvent.WorldTickEvent event)
    {
        @Nullable final List<AbstractBuilding> removedBuildings = new ArrayList<>();

        //Need this list, we may enter he while we add a building in the real world.
        final List<AbstractBuilding> tempBuildings = new ArrayList<>(buildings.values());

        for (@NotNull final AbstractBuilding building : tempBuildings)
        {
            final BlockPos loc = building.getLocation();
            if (event.world.isBlockLoaded(loc) && !building.isMatchingBlock(event.world.getBlockState(loc).getBlock()))
            {
                //  Sanity cleanup
                removedBuildings.add(building);
            }
        }

        @NotNull final ArrayList<BlockPos> tempFields = new ArrayList<>(fields);

        for (@NotNull final BlockPos pos : tempFields)
        {
            if (event.world.isBlockLoaded(pos))
            {
                final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) event.world.getTileEntity(pos);
                if (scarecrow == null)
                {
                    fields.remove(pos);
                }
            }
        }

        removedBuildings.forEach(AbstractBuilding::destroy);
    }

    /**
     * Get building in Colony by ID.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @return AbstractBuilding belonging to the given ID.
     */
    @Override
    public AbstractBuilding getBuilding(final BlockPos buildingId)
    {
        if (buildingId != null)
        {
            return buildings.get(buildingId);
        }
        return null;
    }

    @Override
    public Map<BlockPos, AbstractBuilding> getBuildings()
    {
        return Collections.unmodifiableMap(buildings);
    }

    @Override
    public BuildingTownHall getTownHall()
    {
        return townHall;
    }

    @Override
    public boolean hasWarehouse()
    {
        return wareHouse != null;
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    @Override
    public List<BlockPos> getFields()
    {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public ScarecrowTileEntity getFreeField(final int owner, final World world)
    {
        for (@NotNull final BlockPos pos : fields)
        {
            final TileEntity field = world.getTileEntity(pos);
            if (field instanceof ScarecrowTileEntity && !((ScarecrowTileEntity) field).isTaken())
            {
                ((ScarecrowTileEntity) field).setTaken(true);
                ((ScarecrowTileEntity) field).setOwner(owner);
                markFieldsDirty();
                return (ScarecrowTileEntity) field;
            }
        }
        return null;
    }

    @Override
    public <B extends AbstractBuilding> B getBuilding(final BlockPos buildingId, @NotNull final Class<B> type)
    {
        try
        {
            return type.cast(buildings.get(buildingId));
        }
        catch (final ClassCastException e)
        {
            Log.getLogger().warn("getBuilding called with wrong type: ", e);
            return null;
        }
    }

    @Override
    public void addNewField(final ScarecrowTileEntity tileEntity, final BlockPos pos, final World world)
    {
        addField(pos);
        tileEntity.calculateSize(world, pos);
        markFieldsDirty();
    }

    @Override
    public AbstractBuilding addNewBuilding(@NotNull final TileEntityColonyBuilding tileEntity, final Colony colony, final World world)
    {
        tileEntity.setColony(colony);
        if (!buildings.containsKey(tileEntity.getPosition()))
        {
            @Nullable final AbstractBuilding building = AbstractBuilding.create(colony, tileEntity);
            if (building != null)
            {
                addBuilding(building, colony);
                tileEntity.setBuilding(building);

                Log.getLogger().info(String.format("Colony %d - new AbstractBuilding for %s at %s",
                        colony.getID(),
                        tileEntity.getBlockType().getClass(),
                        tileEntity.getPosition()));
                if (tileEntity.isMirrored())
                {
                    building.setMirror();
                }
                if (!tileEntity.getStyle().isEmpty())
                {
                    building.setStyle(tileEntity.getStyle());
                }
                else
                {
                    building.setStyle(colony.getStyle());
                }
                ConstructionTapeHelper.placeConstructionTape(building.getLocation(), building.getCorners(), world);
            }
            else
            {
                Log.getLogger().error(String.format("Colony %d unable to create AbstractBuilding for %s at %s",
                        colony.getID(),
                        tileEntity.getBlockType().getClass(),
                        tileEntity.getPosition()));
            }

            colony.getCitizenManager().calculateMaxCitizens(colony);
            ColonyManager.markDirty();
            return building;
        }
        return null;
    }

    @Override
    public void removeBuilding(@NotNull final AbstractBuilding building, final Set<EntityPlayerMP> subscribers, final Colony colony)
    {
        if (buildings.remove(building.getID()) != null)
        {
            for (final EntityPlayerMP player : subscribers)
            {
                MineColonies.getNetwork().sendTo(new ColonyViewRemoveBuildingMessage(colony, building.getID()), player);
            }

            Log.getLogger().info(String.format("Colony %d - removed AbstractBuilding %s of type %s",
                    colony.getID(),
                    building.getID(),
                    building.getSchematicName()));
        }

        if (building instanceof BuildingTownHall)
        {
            townHall = null;
        }
        else if (building instanceof BuildingWareHouse)
        {
            wareHouse = null;
        }

        colony.getRequestManager().onProviderRemovedFromColony(building);

        //Allow Citizens to fix up any data that wasn't fixed up by the AbstractBuilding's own onDestroyed
        for (@NotNull final CitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            citizen.onRemoveBuilding(building);
        }

        colony.getCitizenManager().calculateMaxCitizens(colony);

        ColonyManager.markDirty();
    }

    @Override
    public void removeField(final BlockPos pos)
    {
        this.markFieldsDirty();
        fields.remove(pos);
    }

    @Override
    public BlockPos getBestRestaurant(final EntityCitizen citizen)
    {
        double distance = Double.MAX_VALUE;
        BlockPos goodCook = null;
        for (final AbstractBuilding building : citizen.getColony().getBuildingManager().getBuildings().values())
        {
            if (building instanceof BuildingCook && building.getBuildingLevel() > 0)
            {
                final double localDistance = building.getLocation().distanceSq(citizen.getPosition());
                if (localDistance < distance)
                {
                    distance = localDistance;
                    goodCook = building.getLocation();
                }
            }
        }
        return goodCook;
    }

    /**
     * Updates all subscribers of fields etc.
     */
    private void markFieldsDirty()
    {
        isFieldsDirty = true;
    }

    /**
     * Add a AbstractBuilding to the Colony.
     *
     * @param building AbstractBuilding to add to the colony.
     */
    private void addBuilding(@NotNull final AbstractBuilding building, final Colony colony)
    {
        buildings.put(building.getID(), building);
        building.markDirty();

        //  Limit 1 town hall
        if (building instanceof BuildingTownHall && townHall == null)
        {
            townHall = (BuildingTownHall) building;
        }

        if (building instanceof BuildingWareHouse && wareHouse == null)
        {
            wareHouse = (BuildingWareHouse) building;
        }

        colony.getRequestManager().onProviderAddedToColony(building);
    }

    /**
     * Sends packages to update the buildings.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendBuildingPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers, final Set<EntityPlayerMP> subscribers)
    {
        if (isBuildingsDirty || hasNewSubscribers)
        {
            for (@NotNull final AbstractBuilding building : buildings.values())
            {
                if (building.isDirty() || hasNewSubscribers)
                {
                    subscribers.stream()
                            .filter(player -> building.isDirty() || !oldSubscribers.contains(player))
                            .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewBuildingViewMessage(building), player));
                }
            }
        }
    }

    /**
     * Sends packages to update the fields.
     *
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendFieldPackets(final boolean hasNewSubscribers, final Set<EntityPlayerMP> subscribers)
    {
        if (isFieldsDirty || hasNewSubscribers)
        {
            for (final AbstractBuilding building : buildings.values())
            {
                if (building instanceof BuildingFarmer)
                {
                    subscribers.forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewBuildingViewMessage(building), player));
                }
            }
        }
    }

    /**
     * Add a Field to the Colony.
     *
     * @param pos Field position to add to the colony.
     */
    private void addField(@NotNull final BlockPos pos)
    {
        if(!fields.contains(pos))
        {
            fields.add(pos);
        }
    }
}
