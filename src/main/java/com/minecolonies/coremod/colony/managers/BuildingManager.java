package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.IMysticalSite;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.managers.interfaces.IBuildingManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.huts.BlockHutTavern;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.blocks.huts.BlockHutWareHouse;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewBuildingViewMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewRemoveBuildingMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

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
     * The warehouse building position. Initially null.
     */
    private final List<IMysticalSite> mysticalSites = new ArrayList<>();

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
     * Max chunk pos where a building is placed into a certain direction.
     */
    private int minChunkX;
    private int maxChunkX;
    private int minChunkZ;
    private int maxChunkZ;

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
        maxChunkX = colony.getCenter().getX() >> 4;
        minChunkX = colony.getCenter().getX() >> 4;
        maxChunkZ = colony.getCenter().getZ() >> 4;
        minChunkZ = colony.getCenter().getZ() >> 4;

        //  Buildings
        final ListNBT buildingTagList = compound.getList(TAG_BUILDINGS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.size(); ++i)
        {
            final CompoundNBT buildingCompound = buildingTagList.getCompound(i);
            @Nullable final IBuilding b = IBuildingDataManager.getInstance().createFrom(colony, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
                setMaxChunk(b);
            }
        }

        if (compound.getAllKeys().contains(TAG_NEW_FIELDS))
        {
            // Fields before Buildings, because the Farmer needs them.
            final ListNBT fieldTagList = compound.getList(TAG_NEW_FIELDS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < fieldTagList.size(); ++i)
            {
                addField(BlockPosUtil.read(fieldTagList.getCompound(i), TAG_POS));
            }
        }
    }

    /**
     * Set the max chunk direction this building is in.
     *
     * @param b the max chunk dir.
     */
    private void setMaxChunk(final IBuilding b)
    {
        final int chunkX = b.getPosition().getX() >> 4;
        final int chunkZ = b.getPosition().getZ() >> 4;
        if (chunkX >= maxChunkX)
        {
            maxChunkX = chunkX + 1;
        }

        if (chunkX <= minChunkX)
        {
            minChunkX = chunkX - 1;
        }

        if (chunkZ >= maxChunkZ)
        {
            maxChunkZ = chunkZ + 1;
        }

        if (chunkZ <= minChunkZ)
        {
            minChunkZ = chunkZ - 1;
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
            if (WorldUtil.isBlockLoaded(colony.getWorld(), building.getPosition()))
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
            if (WorldUtil.isBlockLoaded(colony.getWorld(), loc) && !building.isMatchingBlock(colony.getWorld().getBlockState(loc).getBlock()))
            {
                //  Sanity cleanup
                removedBuildings.add(building);
            }
        }

        @NotNull final ArrayList<BlockPos> tempFields = new ArrayList<>(fields);

        for (@NotNull final BlockPos pos : tempFields)
        {
            if (WorldUtil.isBlockLoaded(colony.getWorld(), pos))
            {
                if (colony.getWorld().getBlockEntity(pos) instanceof ScarecrowTileEntity)
                {
                    final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) colony.getWorld().getBlockEntity(pos);
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

    @Nullable
    @Override
    public IWareHouse getClosestWarehouseInColony(final BlockPos pos)
    {
        IWareHouse wareHouse = null;
        double dist = 0;
        for (final IWareHouse building : wareHouses)
        {
            if (building.getBuildingLevel() > 0 && building.getTileEntity() != null)
            {
                final double tempDist = building.getPosition().distSqr(pos);
                if (wareHouse == null || tempDist < dist)
                {
                    dist = tempDist;
                    wareHouse = building;
                }
            }
        }

        return wareHouse;
    }

    @Override
    public boolean isWithinBuildingZone(final Chunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        if (cap != null)
        {
            final Set<BlockPos> capList = cap.getAllClaimingBuildings().get(colony.getID());
            return capList != null && capList.size() >= MineColonies.getConfig().getServer().colonyLoadStrictness.get();
        }

        return false;
    }

    @Override
    public IBuilding getHouseWithSpareBed()
    {
        for (final IBuilding building : buildings.values())
        {
            if (building.hasModule(LivingBuildingModule.class))
            {
                if (building.getAssignedCitizen().size() < building.getMaxInhabitants())
                {
                    return building;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Map<BlockPos, IBuilding> getBuildings()
    {
        return Collections.unmodifiableMap(buildings);
    }

    @Nullable
    @Override
    public ITownHall getTownHall()
    {
        return townHall;
    }

    @Override
    public int getMysticalSiteMaxBuildingLevel()
    {
        int maxLevel = 0;
        if (hasMysticalSite())
        {
            for (final IMysticalSite mysticalSite : mysticalSites)
            {
                if (mysticalSite.getBuildingLevel() > maxLevel)
                {
                    maxLevel = mysticalSite.getBuildingLevel();
                }
            }
        }
        return maxLevel;
    }

    @Override
    public boolean hasWarehouse()
    {
        return !wareHouses.isEmpty();
    }

    @Override
    public boolean hasMysticalSite()
    {
        return !mysticalSites.isEmpty();
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    @NotNull
    @Override
    public List<BlockPos> getFields()
    {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public void addNewField(final AbstractScarecrowTileEntity tileEntity, final BlockPos pos, final World world)
    {
        addField(pos);
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
            final TileEntity field = world.getBlockEntity(pos);
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
                building.upgradeBuildingLevelToSchematicData();

                Log.getLogger().info(String.format("Colony %d - new AbstractBuilding for %s at %s",
                  colony.getID(),
                  tileEntity.getBlockState().getClass(),
                  tileEntity.getPosition()));

                building.setIsMirrored(tileEntity.isMirrored());
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

                    ConstructionTapeHelper.placeConstructionTape(building.getCorners(), world);
                }

                colony.getRequestManager().onProviderAddedToColony(building);

                setMaxChunk(building);
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
        else if (building instanceof BuildingMysticalSite)
        {
            mysticalSites.remove(building);
        }

        //Allow Citizens to fix up any data that wasn't fixed up by the AbstractBuilding's own onDestroyed
        for (@NotNull final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            citizen.onRemoveBuilding(building);
            building.cancelAllRequestsOfCitizen(citizen);
        }

        colony.getRequestManager().onProviderRemovedFromColony(building);
        colony.getRequestManager().onRequesterRemovedFromColony(building.getRequester());

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
    public BlockPos getBestBuilding(final AbstractEntityCitizen citizen, final Class<? extends IBuilding> clazz)
    {
        return getBestBuilding(citizen.blockPosition(), clazz);
    }

    @Override
    public BlockPos getBestBuilding(final BlockPos citizen, final Class<? extends IBuilding> clazz)
    {
        double distance = Double.MAX_VALUE;
        BlockPos goodCook = null;
        for (final IBuilding building : buildings.values())
        {
            if (clazz.isInstance(building) && building.getBuildingLevel() > 0)
            {
                final double localDistance = building.getPosition().distSqr(citizen);
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

    /**
     * Finds whether there is a guard building close to the given building
     *
     * @param building the building to check.
     * @return false if no guard tower close, true in other cases
     */
    @Override
    public boolean hasGuardBuildingNear(final IBuilding building)
    {
        if (building == null)
        {
            return true;
        }

        for (final IBuilding colonyBuilding : getBuildings().values())
        {
            if (colonyBuilding instanceof IGuardBuilding
                  && (colonyBuilding.getClaimRadius(colonyBuilding.getBuildingLevel()) * 16) > building.getID().distManhattan(colonyBuilding.getID()))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void guardBuildingChangedAt(final IBuilding guardBuilding, final int newLevel)
    {
        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            if (building.getPosition().getX() <= guardBuilding.getPosition().getX() + 16 * guardBuilding.getClaimRadius(newLevel)
                  && building.getPosition().getZ() <= guardBuilding.getPosition().getZ() + 16 * guardBuilding.getClaimRadius(newLevel))
            {
                if (newLevel > 0)
                {
                    building.setGuardBuildingNear(true);
                }
                else
                {
                    building.setGuardBuildingNear(hasGuardBuildingNear(building));
                }
            }
        }
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

    @Override
    public List<IMysticalSite> getMysticalSites()
    {
        return mysticalSites;
    }

    @Override
    public void removeMysticalSite(final IMysticalSite mysticalSite)
    {
        mysticalSites.remove(mysticalSite);
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
        else if (building instanceof BuildingMysticalSite)
        {
            mysticalSites.add((IMysticalSite) building);
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
            if (colony != null && (!MineColonies.getConfig().getServer().limitToOneWareHousePerColony.get() || !colony.hasWarehouse()))
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
                if (colony.getWorld() != null && !colony.getWorld().isClientSide)
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
                if (building.hasModule(TavernBuildingModule.class))
                {
                    LanguageHandler.sendPlayerMessage(player, "tile.blockhut.tavern.limit");
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onBuildingUpgradeComplete(@Nullable final IBuilding building, final int level)
    {
        if (building != null)
        {
            colony.getCitizenManager().calculateMaxCitizens();
            markBuildingsDirty();
        }
    }
}
