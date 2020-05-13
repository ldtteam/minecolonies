package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.managers.interfaces.IBuildingManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.huts.BlockHutTavern;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.blocks.huts.BlockHutWareHouse;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewBuildingViewMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewRemoveBuildingMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

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
    private final List<IWareHouse> wareHouses = new ArrayList<>();

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
    private boolean isFieldsDirty = false;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Creates the BuildingManager for a colony.
     *
     * @param colony the colony.
     */
    public BuildingManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        buildings.clear();
        //  Buildings
        final ListNBT buildingTagList = compound.getList(TAG_BUILDINGS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.size(); ++i)
        {
            final CompoundNBT buildingCompound = buildingTagList.getCompound(i);
            @Nullable final IBuilding b = IBuildingDataManager.getInstance().createFrom(colony, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
            }
        }

        if (compound.keySet().contains(TAG_NEW_FIELDS))
        {
            // Fields before Buildings, because the Farmer needs them.
            final ListNBT fieldTagList = compound.getList(TAG_NEW_FIELDS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < fieldTagList.size(); ++i)
            {
                addField(BlockPosUtil.read(fieldTagList.getCompound(i), TAG_POS));
            }
        }
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
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
            BlockPosUtil.write(fieldCompound, TAG_POS, pos);
            fieldTagList.add(fieldCompound);
        }
        compound.put(TAG_NEW_FIELDS, fieldTagList);
    }

    @Override
    public void clearDirty()
    {
        isBuildingsDirty = false;
        buildings.values().forEach(IBuilding::clearDirty);
    }

    @Override
    public void sendPackets(final Set<ServerPlayerEntity> closeSubscribers, final Set<ServerPlayerEntity> newSubscribers)
    {
        sendBuildingPackets(closeSubscribers, newSubscribers);
        sendFieldPackets(closeSubscribers, newSubscribers);
        isBuildingsDirty = false;
        isFieldsDirty = false;
    }

    /**
     * Ticks all buildings when this building manager receives a tick.
     *
     * @param colony the colony which is being ticked.
     */
    @Override
    public void onColonyTick(final IColony colony)
    {
        //  Tick Buildings
        for (@NotNull final IBuilding building : buildings.values())
        {
            if (colony.getWorld().isBlockPresent(building.getPosition()))
            {
                building.onColonyTick(colony);
            }
        }
    }

    @Override
    public void markBuildingsDirty()
    {
        isBuildingsDirty = true;
    }

    @Override
    public void cleanUpBuildings(@NotNull final IColony colony)
    {
        @Nullable final List<IBuilding> removedBuildings = new ArrayList<>();

        //Need this list, we may enter here while we add a building in the real world.
        final List<IBuilding> tempBuildings = new ArrayList<>(buildings.values());

        for (@NotNull final IBuilding building : tempBuildings)
        {
            final BlockPos loc = building.getPosition();
            if (colony.getWorld().isBlockPresent(loc) && !building.isMatchingBlock(colony.getWorld().getBlockState(loc).getBlock()))
            {
                //  Sanity cleanup
                removedBuildings.add(building);
            }
        }

        @NotNull final ArrayList<BlockPos> tempFields = new ArrayList<>(fields);

        for (@NotNull final BlockPos pos : tempFields)
        {
            if (colony.getWorld().isBlockPresent(pos))
            {
                if (colony.getWorld().getTileEntity(pos) instanceof ScarecrowTileEntity)
                {
                    final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) colony.getWorld().getTileEntity(pos);
                    if (scarecrow == null)
                    {
                        removeField(pos);
                    }
                }
                else
                {
                    removeField(pos);
                }
            }
        }

        if (!removedBuildings.isEmpty() && removedBuildings.size() >= buildings.values().size())
        {
            Log.getLogger()
              .warn("Colony:" + colony.getID()
                      + " is removing all buildings at once. Did you just load a backup? If not there is a chance that colony data got corrupted and you want to restore a backup.");
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
        return !wareHouses.isEmpty();
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
    public ScarecrowTileEntity getFreeField(final int owner, final World world)
    {
        for (@NotNull final BlockPos pos : fields)
        {
            final TileEntity field = world.getTileEntity(pos);
            if (field instanceof ScarecrowTileEntity && !((ScarecrowTileEntity) field).isTaken())
            {
                return (ScarecrowTileEntity) field;
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
            @Nullable final IBuilding building = IBuildingDataManager.getInstance().createFrom(colony, tileEntity);
            if (building != null)
            {
                addBuilding(building);
                tileEntity.setBuilding(building);

                Log.getLogger().info(String.format("Colony %d - new AbstractBuilding for %s at %s",
                  colony.getID(),
                  tileEntity.getBlockState().getClass(),
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

                if (world != null && !(building instanceof IRSComponent))
                {
                    building.onPlacement();

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
                  tileEntity.getBlockState().getClass(),
                  tileEntity.getPosition()), new Exception());
            }

            colony.getCitizenManager().calculateMaxCitizens();
            colony.getPackageManager().updateSubscribers();
            return building;
        }
        return null;
    }

    /**
     * Gets a rotation from a block facing.
     *
     * @param facing the block facing.
     * @return the int rotation.
     */
    public static int getRotationFromFacing(final Direction facing)
    {
        switch (facing)
        {
            case SOUTH:
                return 2;
            case EAST:
                return 1;
            case WEST:
                return 3;
            default:
                return 0;
        }
    }

    @Override
    public void removeBuilding(@NotNull final IBuilding building, final Set<ServerPlayerEntity> subscribers)
    {
        if (buildings.remove(building.getID()) != null)
        {
            for (final ServerPlayerEntity player : subscribers)
            {
                Network.getNetwork().sendToPlayer(new ColonyViewRemoveBuildingMessage(colony, building.getID()), player);
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
            wareHouses.remove(building);
        }

        colony.getRequestManager().onProviderRemovedFromColony(building);

        //Allow Citizens to fix up any data that wasn't fixed up by the AbstractBuilding's own onDestroyed
        for (@NotNull final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            citizen.onRemoveBuilding(building);
            building.cancelAllRequestsOfCitizen(citizen);
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
    public BlockPos getBestHospital(final AbstractEntityCitizen citizen)
    {
        double distance = Double.MAX_VALUE;
        BlockPos goodHospital = null;
        for (final IBuilding building : citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values())
        {
            if (building instanceof BuildingHospital && building.getBuildingLevel() > 0)
            {
                final double localDistance = building.getPosition().distanceSq(citizen.getPosition());
                if (localDistance < distance)
                {
                    distance = localDistance;
                    goodHospital = building.getPosition();
                }
            }
        }
        return goodHospital;
    }

    @Override
    public BlockPos getRandomBuilding(Predicate<IBuilding> filterPredicate)
    {
        final List<IBuilding> allowedBuildings = new ArrayList<>();
        for (final IBuilding building : buildings.values())
        {
            if (filterPredicate.test(building))
            {
                allowedBuildings.add(building);
            }
        }

        if (allowedBuildings.isEmpty())
        {
            return null;
        }

        Collections.shuffle(allowedBuildings);
        return allowedBuildings.get(0).getPosition();
    }

    @Override
    public void setTownHall(@Nullable final ITownHall building)
    {
        this.townHall = building;
    }

    @Override
    public List<IWareHouse> getWareHouses()
    {
        return wareHouses;
    }

    @Override
    public void removeWareHouse(final IWareHouse wareHouse)
    {
        wareHouses.remove(wareHouse);
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

        if (building instanceof BuildingWareHouse)
        {
            wareHouses.add((IWareHouse) building);
        }
    }

    /**
     * Sends packages to update the buildings.
     *
     * @param closeSubscribers the current event subscribers.
     * @param newSubscribers   the new event subscribers.
     */
    private void sendBuildingPackets(final Set<ServerPlayerEntity> closeSubscribers, final Set<ServerPlayerEntity> newSubscribers)
    {
        if (isBuildingsDirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayerEntity> players = new HashSet<>();
            if (isBuildingsDirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);
            for (@NotNull final IBuilding building : buildings.values())
            {
                if (building.isDirty() || !newSubscribers.isEmpty())
                {
                    players.forEach(player -> Network.getNetwork().sendToPlayer(new ColonyViewBuildingViewMessage(building), player));
                }
            }
        }
    }

    /**
     * Sends packages to update the fields.
     *
     * @param closeSubscribers the current event subscribers.
     * @param newSubscribers   the new event subscribers.
     */
    private void sendFieldPackets(final Set<ServerPlayerEntity> closeSubscribers, final Set<ServerPlayerEntity> newSubscribers)
    {
        if (isFieldsDirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayerEntity> players = new HashSet<>();
            if (isFieldsDirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);
            for (final IBuilding building : buildings.values())
            {
                if (building instanceof BuildingFarmer)
                {
                    players.forEach(player -> Network.getNetwork().sendToPlayer(new ColonyViewBuildingViewMessage(building), player));
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
        if (!fields.contains(pos))
        {
            fields.add(pos);
        }
        colony.markDirty();
    }

    @Override
    public boolean canPlaceAt(final Block block, final BlockPos pos, final PlayerEntity player)
    {
        if (block instanceof BlockHutWareHouse)
        {
            if (colony != null && (!MineColonies.getConfig().getCommon().limitToOneWareHousePerColony.get() || !colony.hasWarehouse()))
            {
                return true;
            }
            LanguageHandler.sendPlayerMessage(player, "tile.blockhut.warehouse.limit");
            return false;
        }
        else if (block instanceof BlockHutTownHall)
        {
            if (colony.hasTownHall())
            {
                if (colony.getWorld() != null && !colony.getWorld().isRemote)
                {
                    LanguageHandler.sendPlayerMessage(player, "tile.blockhuttownhall.messageplacedalready");
                }
                return false;
            }
            return true;
        }
        else if (block instanceof BlockHutTavern)
        {
            for (final IBuilding building : buildings.values())
            {
                if (building instanceof BuildingTavern)
                {
                    LanguageHandler.sendPlayerMessage(player, "tile.blockhut.tavern.limit");
                    return false;
                }
            }
        }

        return true;
    }
}
