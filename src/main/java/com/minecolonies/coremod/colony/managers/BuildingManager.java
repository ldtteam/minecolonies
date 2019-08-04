package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.managers.interfaces.IBuildingManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.registry.BuildingRegistry;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.network.messages.ColonyViewBuildingViewMessage;
import com.minecolonies.coremod.network.messages.ColonyViewRemoveBuildingMessage;
import com.minecolonies.coremod.tileentities.TileEntityScarecrow;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class BuildingManager implements IBuildingManager
{
    /**
     * List of building in the colony.
     */
    @NotNull
    private final Map<BlockPos, IBuilding> buildings = new HashMap<>();

    /**
     * List of fields of the colony.
     */
    private final List<BlockPos> fields = new ArrayList<>();

    /**
     * The warehouse building position. Initially null.
     */
    private IWareHouse wareHouse = null;

    /**
     * The townhall of the colony.
     */
    @Nullable
    private ITownHall townHall;

    /**
     * Variable to check if the buildings needs to be synched.
     */
    private boolean isBuildingsDirty = false;

    /**
     * Variable to check if the fields needs to be synched.
     */
    private boolean isFieldsDirty    = false;

    /**
     * Counter for world ticks.
     */
    private int tickCounter = 0;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Creates the BuildingManager for a colony.
     * @param colony the colony.
     */
    public BuildingManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        //  Buildings
        final ListNBT buildingTagList = compound.getList(TAG_BUILDINGS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.size(); ++i)
        {
            final CompoundNBT buildingCompound = buildingTagList.getCompound(i);
            @Nullable final IBuilding b = BuildingRegistry.createFromNBT(colony, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
            }
        }

        if(compound.keySet().contains(TAG_NEW_FIELDS))
        {
            // Fields before Buildings, because the Farmer needs them.
            final ListNBT fieldTagList = compound.getList(TAG_NEW_FIELDS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < fieldTagList.size(); ++i)
            {
                addField(BlockPosUtil.readFromNBT(fieldTagList.getCompound(i), TAG_POS));
            }
        }
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        //  Buildings
        @NotNull final ListNBT buildingTagList = new ListNBT();
        for (@NotNull final IBuilding b : buildings.values())
        {
            @NotNull final CompoundNBT buildingCompound = b.serializeNBT();
            buildingTagList.add(buildingCompound);
        }
        compound.put(TAG_BUILDINGS, buildingTagList);

        // Fields
        @NotNull final ListNBT fieldTagList = new ListNBT();
        for (@NotNull final BlockPos pos : fields)
        {
            @NotNull final CompoundNBT fieldCompound = new CompoundNBT();
            BlockPosUtil.writeToNBT(fieldCompound, TAG_POS, pos);
            fieldTagList.add(fieldCompound);
        }
        compound.put(TAG_NEW_FIELDS, fieldTagList);
    }

    @Override
    public void tick(final TickEvent.ServerTickEvent event)
    {
        for (@NotNull final IBuilding b : buildings.values())
        {
            b.onServerTick(event);
        }
    }

    @Override
    public void clearDirty()
    {
        isBuildingsDirty = false;
        buildings.values().forEach(IBuilding::clearDirty);
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
        for (@NotNull final IBuilding building : buildings.values())
        {
            if (event.world.isBlockLoaded(building.getPosition()))
            {
                if (tickCounter == 20)
                {
                    building.secondsWorldTick(event);
                }

                building.onWorldTick(event);
            }
        }

        if (tickCounter == 20)
        {
            tickCounter = 0;
        }
        tickCounter++;
    }

    @Override
    public void markBuildingsDirty()
    {
        isBuildingsDirty = true;
    }

    @Override
    public void cleanUpBuildings(@NotNull final TickEvent.WorldTickEvent event)
    {
        @Nullable final List<IBuilding> removedBuildings = new ArrayList<>();

        //Need this list, we may enter here while we add a building in the real world.
        final List<IBuilding> tempBuildings = new ArrayList<>(buildings.values());

        for (@NotNull final IBuilding building : tempBuildings)
        {
            final BlockPos loc = building.getPosition();
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
                final TileEntityScarecrow scarecrow = (TileEntityScarecrow) event.world.getTileEntity(pos);
                if (scarecrow == null)
                {
                    removeField(pos);
                }
            }
        }

        removedBuildings.forEach(IBuilding::destroy);
    }

    /**
     * Get building in Colony by ID.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @return AbstractBuilding belonging to the given ID.
     */
    @Override
    public IBuilding getBuilding(final BlockPos buildingId)
    {
        if (buildingId != null)
        {
            return buildings.get(buildingId);
        }
        return null;
    }

    @Override
    public Map<BlockPos, IBuilding> getBuildings()
    {
        return Collections.unmodifiableMap(buildings);
    }

    @Override
    public ITownHall getTownHall()
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
    public void addNewField(final AbstractScarescrowTileEntity tileEntity, final BlockPos pos, final World world)
    {
        addField(pos);
        tileEntity.calculateSize(world, pos.down());
        markFieldsDirty();
    }

    @Override
    public <B extends IBuilding> B getBuilding(final BlockPos buildingId, @NotNull final Class<B> type)
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
    public TileEntityScarecrow getFreeField(final int owner, final World world)
    {
        for (@NotNull final BlockPos pos : fields)
        {
            final TileEntity field = world.getTileEntity(pos);
            if (field instanceof TileEntityScarecrow && !((TileEntityScarecrow) field).isTaken())
            {
                return (TileEntityScarecrow) field;
            }
        }
        return null;
    }

    @Override
    public IBuilding addNewBuilding(@NotNull final AbstractTileEntityColonyBuilding tileEntity, final World world)
    {
        tileEntity.setColony(colony);
        if (!buildings.containsKey(tileEntity.getPosition()))
        {
            @Nullable final IBuilding building = BuildingRegistry.create(colony, tileEntity);
            if (building != null)
            {
                addBuilding(building);
                tileEntity.setBuilding(building);

                Log.getLogger().info(String.format("Colony %d - new AbstractBuilding for %s at %s",
                        colony.getID(),
                        tileEntity.getBlockType().getClass(),
                        tileEntity.getPosition()));
                if (tileEntity.isMirrored())
                {
                    building.invertMirror();
                }
                if (!tileEntity.getStyle().isEmpty())
                {
                    building.setStyle(tileEntity.getStyle());
                }
                else
                {
                    building.setStyle(colony.getStyle());
                }

                if (world != null && !(building instanceof PostBox))
                {
                    building.onPlacement();

                    building.setRotation(BlockUtils.getRotationFromFacing(world.getBlockState(building.getPosition()).getValue(AbstractBlockHut.FACING)));
                    final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, 1);
                    final Structure wrapper = new Structure(world, workOrder.getStructureName(), new PlacementSettings());
                    final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
                      = ColonyUtils.calculateCorners(building.getPosition(),
                      world,
                      wrapper,
                      workOrder.getRotation(world),
                      workOrder.isMirrored());

                    building.setCorners(corners.getA().getA(), corners.getA().getB(), corners.getB().getA(), corners.getB().getB());
                    building.setHeight(wrapper.getHeight());

                    ConstructionTapeHelper.placeConstructionTape(building.getPosition(), corners, world);
                }

                ConstructionTapeHelper.placeConstructionTape(building.getPosition(), building.getCorners(), world);
                colony.getRequestManager().onProviderAddedToColony(building);
            }
            else
            {
                Log.getLogger().error(String.format("Colony %d unable to create AbstractBuilding for %s at %s",
                        colony.getID(),
                        tileEntity.getBlockType().getClass(),
                        tileEntity.getPosition()));
            }

            colony.getCitizenManager().calculateMaxCitizens();
            return building;
        }
        return null;
    }

    @Override
    public void removeBuilding(@NotNull final IBuilding building, final Set<EntityPlayerMP> subscribers)
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
        for (@NotNull final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            citizen.onRemoveBuilding(building);
        }

        colony.getCitizenManager().calculateMaxCitizens();
    }

    @Override
    public void removeField(final BlockPos pos)
    {
        this.markFieldsDirty();
        fields.remove(pos);
        colony.markDirty();
    }

    @Override
    public BlockPos getBestRestaurant(final AbstractEntityCitizen citizen)
    {
        double distance = Double.MAX_VALUE;
        BlockPos goodCook = null;
        for (final IBuilding building : citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values())
        {
            if (building instanceof BuildingCook && building.getBuildingLevel() > 0)
            {
                final double localDistance = building.getPosition().distanceSq(citizen.getPosition());
                if (localDistance < distance)
                {
                    distance = localDistance;
                    goodCook = building.getPosition();
                }
            }
        }
        return goodCook;
    }

    @Override
    public void setTownHall(@Nullable final ITownHall building)
    {
        this.townHall = building;
    }

    @Override
    public void setWareHouse(@Nullable final IWareHouse building)
    {
        this.wareHouse = building;
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
    private void addBuilding(@NotNull final IBuilding building)
    {
        buildings.put(building.getID(), building);
        building.markDirty();

        //  Limit 1 town hall
        if (building instanceof BuildingTownHall && townHall == null)
        {
            townHall = (ITownHall) building;
        }

        if (building instanceof BuildingWareHouse && wareHouse == null)
        {
            wareHouse = (IWareHouse) building;
        }
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
            for (@NotNull final IBuilding building : buildings.values())
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
            for (final IBuilding building : buildings.values())
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
        colony.markDirty();
    }
}
