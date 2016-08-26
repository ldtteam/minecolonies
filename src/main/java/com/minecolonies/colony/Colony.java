package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.colony.workorders.AbstractWorkOrder;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.network.messages.*;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class describes a colony and contains all the data and methods for manipulating a Colony.
 */
public class Colony implements IColony
{
    private final int id;

    //  Runtime Data
    private World world = null;

    //  Updates and Subscriptions
    private Set<EntityPlayerMP> subscribers      = new HashSet<>();
    private boolean             isDirty          = false;
    private boolean             isCitizensDirty  = false;
    private boolean             isBuildingsDirty = false;
    private boolean             manualHiring     = false;

    //  General Attributes
    private String name = "ERROR(Wasn't placed by player)";
    private final int      dimensionId;
    private       BlockPos center;

    //  Administration/permissions
    private Permissions permissions = new Permissions();
    //private int autoHostile = 0;//Off

    //  Buildings
    private BuildingTownHall townHall;
    private Map<BlockPos, AbstractBuilding> buildings = new HashMap<>();

    //  Citizenry
    private Map<Integer, CitizenData> citizens     = new HashMap<>();
    private int                       topCitizenId = 0;
    private int                       maxCitizens  = Configurations.maxCitizens;

    //  Settings
    private static final int CITIZEN_CLEANUP_TICK_INCREMENT = 5 * 20;

    //  Workload and Jobs
    private final WorkManager workManager = new WorkManager(this);

    private final MaterialSystem materialSystem = new MaterialSystem();

    private static final String TAG_ID            = "id";
    private static final String TAG_NAME          = "name";
    private static final String TAG_DIMENSION     = "dimension";
    private static final String TAG_CENTER        = "center";
    private static final String TAG_MAX_CITIZENS  = "maxCitizens";
    private static final String TAG_BUILDINGS     = "buildings";
    private static final String TAG_CITIZENS      = "citizens";
    private static final String TAG_WORK          = "work";
    private static final String TAG_MANUAL_HIRING = "manualHiring";

    /**
     * Constructor for a newly created Colony.
     *
     * @param id The id of the colony to create.
     * @param w  The world the colony exists in.
     * @param c  The center of the colony (location of Town Hall).
     */
    Colony(int id, World w, BlockPos c)
    {
        this(id, w.provider.getDimensionId());
        center = c;
        world = w;
    }

    /**
     * Base constructor.
     *
     * @param id  The current id for the colony
     * @param dim The world the colony exists in
     */
    protected Colony(int id, int dim)
    {
        this.id = id;
        this.dimensionId = dim;
    }

    /**
     * Load a saved colony
     *
     * @param compound The NBT compound containing the colony's data.
     * @return loaded colony.
     */
    public static Colony loadColony(NBTTagCompound compound)
    {
        int id = compound.getInteger(TAG_ID);
        int dimensionId = compound.getInteger(TAG_DIMENSION);
        Colony c = new Colony(id, dimensionId);
        c.readFromNBT(compound);
        return c;
    }

    /**
     * Read colony from saved data
     *
     * @param compound compount to read from
     */
    private void readFromNBT(NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        center = BlockPosUtil.readFromNBT(compound, TAG_CENTER);

        manualHiring = compound.getBoolean(TAG_MANUAL_HIRING);
        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);

        // Permissions
        permissions.loadPermissions(compound);

        //  Citizens before Buildings, because Buildings track the Citizens
        NBTTagList citizenTagList = compound.getTagList(TAG_CITIZENS, NBT.TAG_COMPOUND);
        for (int i = 0; i < citizenTagList.tagCount(); ++i)
        {
            NBTTagCompound citizenCompound = citizenTagList.getCompoundTagAt(i);
            CitizenData data = CitizenData.createFromNBT(citizenCompound, this);
            citizens.put(data.getId(), data);
            topCitizenId = Math.max(topCitizenId, data.getId());
        }

        //  Buildings
        NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS, NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.tagCount(); ++i)
        {
            NBTTagCompound buildingCompound = buildingTagList.getCompoundTagAt(i);
            AbstractBuilding b = AbstractBuilding.createFromNBT(this, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
            }
        }

        //  Workload
        workManager.readFromNBT(compound.getCompoundTag(TAG_WORK));
    }

    /**
     * Write colony to save data.
     *
     * @param compound compound to write to.
     */
    protected void writeToNBT(NBTTagCompound compound)
    {
        //  Core attributes
        compound.setInteger(TAG_ID, id);
        compound.setInteger(TAG_DIMENSION, dimensionId);

        //  Basic data
        compound.setString(TAG_NAME, name);
        BlockPosUtil.writeToNBT(compound, TAG_CENTER, center);

        compound.setBoolean(TAG_MANUAL_HIRING, manualHiring);
        compound.setInteger(TAG_MAX_CITIZENS, maxCitizens);

        // Permissions
        permissions.savePermissions(compound);

        //  Buildings
        NBTTagList buildingTagList = new NBTTagList();
        for (AbstractBuilding b : buildings.values())
        {
            NBTTagCompound buildingCompound = new NBTTagCompound();
            b.writeToNBT(buildingCompound);
            buildingTagList.appendTag(buildingCompound);
        }
        compound.setTag(TAG_BUILDINGS, buildingTagList);

        //  Citizens
        NBTTagList citizenTagList = new NBTTagList();
        for (CitizenData citizen : citizens.values())
        {
            NBTTagCompound citizenCompound = new NBTTagCompound();
            citizen.writeToNBT(citizenCompound);
            citizenTagList.appendTag(citizenCompound);
        }
        compound.setTag(TAG_CITIZENS, citizenTagList);

        //  Workload
        NBTTagCompound workManagerCompound = new NBTTagCompound();
        workManager.writeToNBT(workManagerCompound);
        compound.setTag(TAG_WORK, workManagerCompound);
    }

    /**
     * Returns the ID of the colony.
     *
     * @return Colony ID.
     */
    public int getID()
    {
        return id;
    }

    /**
     * Returns the dimension ID.
     *
     * @return Dimension ID.
     */
    public int getDimensionId()
    {
        return dimensionId;
    }

    /**
     * Returns the world the colony is in.
     *
     * @return World the colony is in.
     */
    public World getWorld()
    {
        return world;
    }

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the colony.
     * Marks dirty.
     *
     * @param n new name.
     */
    public void setName(String n)
    {
        name = n;
        markDirty();
    }

    /**
     * Returns the center of the colony.
     *
     * @return Chunk Coordinates of the center of the colony.
     */
    public BlockPos getCenter()
    {
        return center;
    }

    /**
     * Marks the instance dirty.
     */
    private void markDirty()
    {
        isDirty = true;
    }

    /**
     * Marks citizen data dirty.
     */
    public void markCitizensDirty()
    {
        isCitizensDirty = true;
    }

    /**
     * Marks building data dirty.
     */
    public void markBuildingsDirty()
    {
        isBuildingsDirty = true;
    }

    @Override
    public Permissions getPermissions()
    {
        return permissions;
    }

    @Override
    public boolean isCoordInColony(World w, BlockPos pos)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.equals(getWorld()) &&
                BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ())) <= MathUtils.square(Configurations.workingRangeTownHall);
    }

    @Override
    public float getDistanceSquared(BlockPos pos)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ()));
    }

    /**
     * When the Colony's world is loaded, associate with it.
     *
     * @param w World object.
     */
    public void onWorldLoad(World w)
    {
        if (w.provider.getDimensionId() == dimensionId)
        {
            world = w;
        }
    }

    /**
     * Unsets the world if the world unloads.
     *
     * @param w World object.
     */
    public void onWorldUnload(World w)
    {
        if (!w.equals(world))
        {
            throw new IllegalStateException("Colony's world does not match the event.");
        }

        world = null;
    }

    /**
     * Any per-server-tick logic should be performed here.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        for (AbstractBuilding b : buildings.values())
        {
            b.onServerTick(event);
        }

        if (event.phase == TickEvent.Phase.END)
        {
            updateSubscribers();
        }
    }

    /**
     * Any per-world-tick logic should be performed here.
     * NOTE: If the Colony's world isn't loaded, it won't have a world tick.
     * Use onServerTick for logic that should _always_ run.
     *
     * @param event {@link TickEvent.WorldTickEvent}
     */
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.world != getWorld())
        {
            throw new IllegalStateException("Colony's world does not match the event.");
        }

        if (event.phase == TickEvent.Phase.START)
        {
            //  Detect CitizenData whose EntityCitizen no longer exist in world, and clear the mapping
            //  Consider handing this in an ChunkUnload Event instead?
            citizens.values()
                    .stream()
                    .filter(Colony::isCitizenMissingFromWorld)
                    .forEach(CitizenData::clearCitizenEntity);

            //  Cleanup disappeared citizens
            //  It would be really nice if we didn't have to do this... but Citizens can disappear without dying!
            //  Every CITIZEN_CLEANUP_TICK_INCREMENT, cleanup any 'lost' citizens
            if ((event.world.getWorldTime() % CITIZEN_CLEANUP_TICK_INCREMENT) == 0 && areAllColonyChunksLoaded(event))
            {
                //  All chunks within a good range of the colony should be loaded, so all citizens should be loaded
                //  If we don't have any references to them, destroy the citizen
                citizens.values().stream().filter(citizen -> citizen.getCitizenEntity() == null)
                        .forEach(citizen -> {
                            Log.logger.warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", getID(), citizen.getId()));
                            spawnCitizen(citizen);
                        });
            }

            //  Cleanup Buildings whose Blocks have gone AWOL
            cleanUpBuildings(event);

            //  Spawn Citizens
            if (townHall != null && citizens.size() < maxCitizens)
            {
                int respawnInterval = Configurations.citizenRespawnInterval * 20;
                respawnInterval -= (60 * townHall.getBuildingLevel());

                if (event.world.getWorldTime() % respawnInterval == 0)
                {
                    spawnCitizen();
                }
            }
        }

        //  Tick Buildings
        for (AbstractBuilding building : buildings.values())
        {
            building.onWorldTick(event);
        }

        workManager.onWorldTick(event);
    }

    /**
     * Update Subscribers with Colony, Citizen, and AbstractBuilding Views.
     */
    public void updateSubscribers()
    {
        //  Recompute subscribers every frame (for now)
        //  Subscribers = Owners + Players within (double working town hall range)
        Set<EntityPlayerMP> oldSubscribers = subscribers;
        subscribers = new HashSet<>();

        // Add owners
        subscribers.addAll(
                MinecraftServer.getServer().getConfigurationManager().playerEntityList
                        .stream()
                        .filter(permissions::isSubscriber)
                        .collect(Collectors.toList()));

        //  Add nearby players
        if (world != null)
        {
            for (EntityPlayer o : world.playerEntities)
            {
                if (o instanceof EntityPlayerMP)
                {
                    EntityPlayerMP player = (EntityPlayerMP) o;

                    if (subscribers.contains(player))
                    {
                        //  Already a subscriber
                        continue;
                    }

                    double distance = player.getDistanceSq(center);
                    if (distance < MathUtils.square(Configurations.workingRangeTownHall + 16D) ||
                            (oldSubscribers.contains(player) && distance < MathUtils.square(Configurations.workingRangeTownHall * 2D)))
                    {
                        // Players become subscribers if they come within 16 blocks of the edge of the colony
                        // Players remain subscribers while they remain within double the colony's radius
                        subscribers.add(player);
                    }
                }
            }
        }

        if (!subscribers.isEmpty())
        {
            //  Determine if any new subscribers were added this pass
            boolean hasNewSubscribers = hasNewSubscribers(oldSubscribers, subscribers);

            //  Send each type of update packet as appropriate:
            //      - To Subscribers if the data changes
            //      - To New Subscribers even if it hasn't changed

            //ColonyView
            sendColonyViewPackets(oldSubscribers, hasNewSubscribers);

            //Permissions
            sendPermissionsPackets(oldSubscribers, hasNewSubscribers);

            //WorkOrders
            sendWorkOrderPackets(oldSubscribers, hasNewSubscribers);

            //Citizens
            sendCitizenPackets(oldSubscribers, hasNewSubscribers);

            //Buildings
            sendBuildingPackets(oldSubscribers, hasNewSubscribers);
        }

        isDirty = false;
        isCitizensDirty = false;
        isBuildingsDirty = false;
        permissions.clearDirty();

        buildings.values().forEach(AbstractBuilding::clearDirty);
        citizens.values().forEach(CitizenData::clearDirty);
    }

    /**
     * Sends packages to update the buildings.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendBuildingPackets(Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers)
    {
        if (isBuildingsDirty || hasNewSubscribers)
        {
            for (AbstractBuilding building : buildings.values())
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
     * Sends packages to update the citizens.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendCitizenPackets(Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers)
    {
        if (isCitizensDirty || hasNewSubscribers)
        {
            for (CitizenData citizen : citizens.values())
            {
                if (citizen.isDirty() || hasNewSubscribers)
                {
                    subscribers.stream()
                            .filter(player -> citizen.isDirty() || !oldSubscribers.contains(player))
                            .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewCitizenViewMessage(this, citizen), player));
                }
            }
        }
    }

    /**
     * Sends packages to update the workOrders.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendWorkOrderPackets(Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers)
    {
        if (getWorkManager().isDirty() || hasNewSubscribers)
        {
            for (AbstractWorkOrder workOrder : getWorkManager().getWorkOrders().values())
            {
                subscribers.stream().filter(player -> workManager.isDirty() || !oldSubscribers.contains(player))
                        .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewWorkOrderMessage(this, workOrder), player));
            }

            getWorkManager().setDirty(false);
        }
    }

    /**
     * Sends packages to update the permissions.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendPermissionsPackets(Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers)
    {
        if (permissions.isDirty() || hasNewSubscribers)
        {
            subscribers
                    .stream()
                    .filter(player -> permissions.isDirty() || !oldSubscribers.contains(player)).forEach(player -> {
                Permissions.Rank rank = getPermissions().getRank(player);
                MineColonies.getNetwork().sendTo(new PermissionsMessage.View(this, rank), player);
            });
        }
    }

    private void sendColonyViewPackets(Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers)
    {
        if (isDirty || hasNewSubscribers)
        {
            for (EntityPlayerMP player : subscribers)
            {
                boolean isNewSubscriber = !oldSubscribers.contains(player);
                if (isDirty || isNewSubscriber)
                {
                    MineColonies.getNetwork().sendTo(new ColonyViewMessage(this, isNewSubscriber), player);
                }
            }
        }
    }

    private static boolean hasNewSubscribers(Set<EntityPlayerMP> oldSubscribers, Set<EntityPlayerMP> subscribers)
    {
        for (EntityPlayerMP player : subscribers)
        {
            if (!oldSubscribers.contains(player))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isCitizenMissingFromWorld(CitizenData citizen)
    {
        EntityCitizen entity = citizen.getCitizenEntity();

        return entity != null && entity.worldObj.getEntityByID(entity.getEntityId()) != entity;
    }

    private boolean areAllColonyChunksLoaded(TickEvent.WorldTickEvent event)
    {
        int distanceFromCenter = Configurations.workingRangeTownHall + 48 /* 3 chunks */ + 15 /* round up a chunk */;
        for (int x = -distanceFromCenter; x <= distanceFromCenter; x += 16)
        {
            for (int z = -distanceFromCenter; z <= distanceFromCenter; z += 16)
            {
                if (!event.world.isBlockLoaded(new BlockPos(getCenter().getX() + x, 128, getCenter().getZ() + z)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void cleanUpBuildings(TickEvent.WorldTickEvent event)
    {
        List<AbstractBuilding> removedBuildings = null;

        for (AbstractBuilding building : buildings.values())
        {
            BlockPos loc = building.getLocation();
            if (event.world.isBlockLoaded(loc) && !building.isMatchingBlock(event.world.getBlockState(loc).getBlock()))
            {
                //  Sanity cleanup
                if (removedBuildings == null)
                {
                    removedBuildings = new ArrayList<>();
                }
                removedBuildings.add(building);
            }
        }

        if (removedBuildings != null)
        {
            removedBuildings.forEach(AbstractBuilding::destroy);
        }
    }

    /**
     * Spawn a brand new Citizen
     */
    private void spawnCitizen()
    {
        spawnCitizen(null);
    }

    /**
     * Spawn a citizen with specific citizen data
     *
     * @param data Data to use to spawn citizen
     */
    private void spawnCitizen(CitizenData data)
    {
        if (!world.isBlockLoaded(center))
        {
            //  Chunk with TownHall Block is not loaded
            return;
        }

        BlockPos spawnPoint =
                Utils.scanForBlockNearPoint(world, center, 1, 0, 1, 2, Blocks.air, Blocks.snow_layer);

        if (spawnPoint != null)
        {
            EntityCitizen entity = new EntityCitizen(world);

            CitizenData citizenData = data;
            if (citizenData == null)
            {
                topCitizenId++;
                citizenData = new CitizenData(topCitizenId, this);
                citizenData.initializeFromEntity(entity);

                citizens.put(citizenData.getId(), citizenData);

                if (getMaxCitizens() == getCitizens().size())
                {
                    //TODO: add Colony Name prefix?
                    LanguageHandler.sendPlayersLocalizedMessage(
                            this.getMessageEntityPlayers(),
                            "tile.blockHutTownHall.messageMaxSize");
                }
            }

            entity.setColony(this, citizenData);

            entity.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
            world.spawnEntityInWorld(entity);

            checkAchievements();

            markCitizensDirty();
        }
    }

    /**
     * Checks if the achievements are valid.
     */
    private void checkAchievements()
    {
        // the colonies size
        final int size = this.citizens.size();


        final ArrayList<Consumer<EntityPlayer>> consumers = new ArrayList<>();
        if (size >= ModAchievements.ACHIEVEMENT_SIZE_SETTLEMENT)
        {
            consumers.add(player -> player.triggerAchievement(ModAchievements.achievementSizeSettlement));
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_TOWN)
        {
            consumers.add(player -> player.triggerAchievement(ModAchievements.achievementSizeTown));
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_CITY)
        {
            consumers.add(player -> player.triggerAchievement(ModAchievements.achievementSizeCity));
        }
        this.getPermissions().getPlayers().values().stream()
                .map(Permissions.Player::getID)
                .map(ServerUtils::getPlayerFromUUID)
                .forEach(player -> consumers.forEach(consumer -> consumer.accept(player)));
    }

    /**
     * Returns a map with all buildings within the colony.
     * Key is ID (Coordinates), value is building object.
     *
     * @return Map with ID (coordinates) as key, and buildings as value.
     */
    public Map<BlockPos, AbstractBuilding> getBuildings()
    {
        return Collections.unmodifiableMap(buildings);
    }

    /**
     * Gets the town hall of the colony.
     *
     * @return Town hall of the colony.
     */
    public BuildingTownHall getTownHall()
    {
        return townHall;
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    /**
     * Get building in Colony by ID.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @return AbstractBuilding belonging to the given ID.
     */
    public AbstractBuilding getBuilding(BlockPos buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Get building in Colony by ID. The building will be casted to the provided type.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @param type       Type of building.
     * @param <B>        Building class.
     * @return the building with the specified id.
     */
    public <B extends AbstractBuilding> B getBuilding(BlockPos buildingId, Class<B> type)
    {
        try
        {
            return type.cast(buildings.get(buildingId));
        }
        catch (ClassCastException e)
        {
            Log.logger.warn("getBuilding called with wrong type: ", e);
            return null;
        }
    }

    /**
     * Add a AbstractBuilding to the Colony.
     *
     * @param building AbstractBuilding to add to the colony.
     */
    private void addBuilding(AbstractBuilding building)
    {
        buildings.put(building.getID(), building);
        building.markDirty();

        //  Limit 1 town hall
        if (building instanceof BuildingTownHall && townHall == null)
        {
            townHall = (BuildingTownHall) building;
        }
    }

    /**
     * Creates a building from a tile entity and adds it to the colony.
     *
     * @param tileEntity Tile entity to build a building from.
     * @return AbstractBuilding that was created and added.
     */
    public AbstractBuilding addNewBuilding(TileEntityColonyBuilding tileEntity)
    {
        tileEntity.setColony(this);

        AbstractBuilding building = AbstractBuilding.create(this, tileEntity);
        if (building != null)
        {
            addBuilding(building);
            tileEntity.setBuilding(building);

            Log.logger.info(String.format("Colony %d - new AbstractBuilding for %s at %s",
                    getID(),
                    tileEntity.getBlockType().getClass(),
                    tileEntity.getPosition()));
        }
        else
        {
            Log.logger.error(String.format("Colony %d unable to create AbstractBuilding for %s at %s",
                    getID(),
                    tileEntity.getBlockType().getClass(),
                    tileEntity.getPosition()));
        }

        calculateMaxCitizens();
        ColonyManager.markDirty();

        return building;
    }

    /**
     * Remove a AbstractBuilding from the Colony (when it is destroyed).
     *
     * @param building AbstractBuilding to remove.
     */
    public void removeBuilding(AbstractBuilding building)
    {
        if (buildings.remove(building.getID()) != null)
        {
            for (EntityPlayerMP player : subscribers)
            {
                MineColonies.getNetwork().sendTo(new ColonyViewRemoveBuildingMessage(this, building.getID()), player);
            }

            Log.logger.info(String.format("Colony %d - removed AbstractBuilding %s of type %s",
                    getID(),
                    building.getID(),
                    building.getSchematicName()));
        }

        if (building == townHall)
        {
            townHall = null;
        }

        //Allow Citizens to fix up any data that wasn't fixed up by the AbstractBuilding's own onDestroyed
        for (CitizenData citizen : citizens.values())
        {
            citizen.onRemoveBuilding(building);
        }

        calculateMaxCitizens();

        ColonyManager.markDirty();
    }

    /**
     * Getter which checks if jobs should be manually allocated.
     *
     * @return true of false.
     */
    public boolean isManualHiring()
    {
        return manualHiring;
    }

    /**
     * Setter to set the job allocation manual or automatic.
     *
     * @param manualHiring true if manual, false if automatic.
     */
    public void setManualHiring(boolean manualHiring)
    {
        this.manualHiring = manualHiring;
        markDirty();
    }

    /**
     * Returns the max amount of citizens in the colony.
     *
     * @return Max amount of citizens.
     */
    public int getMaxCitizens()
    {
        return maxCitizens;
    }

    /**
     * Recalculates how many citizen can be in the colony.
     */
    public void calculateMaxCitizens()
    {
        int newMaxCitizens = Configurations.maxCitizens;

        for (AbstractBuilding b : buildings.values())
        {
            if (b instanceof BuildingHome &&
                    b.getBuildingLevel() > 0)
            {
                newMaxCitizens += ((BuildingHome) b).getMaxInhabitants();
            }
        }

        if (maxCitizens != newMaxCitizens)
        {
            maxCitizens = newMaxCitizens;
            markDirty();
        }
    }

    /**
     * Returns a map of citizens in the colony.
     * The map has ID as key, and citizen data as value.
     *
     * @return Map of citizens in the colony, with as key the citizen ID, and as value the citizen data.
     */
    public Map<Integer, CitizenData> getCitizens()
    {
        return Collections.unmodifiableMap(citizens);
    }

    /**
     * Removes a citizen from the colony.
     *
     * @param citizen Citizen data to remove.
     */
    public void removeCitizen(CitizenData citizen)
    {
        //Remove the Citizen
        citizens.remove(citizen.getId());

        for (AbstractBuilding building : buildings.values())
        {
            building.removeCitizen(citizen);
        }

        workManager.clearWorkForCitizen(citizen);

        //  Inform Subscribers of removed citizen
        for (EntityPlayerMP player : subscribers)
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveCitizenMessage(this, citizen.getId()), player);
        }
    }

    /**
     * Send the message of a removed workOrder to the client.
     *
     * @param orderId the workOrder to remove.
     */
    public void removeWorkOrder(int orderId)
    {
        //  Inform Subscribers of removed workOrder
        for (EntityPlayerMP player : subscribers)
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveWorkOrderMessage(this, orderId), player);
        }
    }

    /**
     * Get citizen by ID.
     *
     * @param citizenId ID of the Citizen.
     * @return CitizenData associated with the ID, or null if it was not found.
     */
    public CitizenData getCitizen(int citizenId)
    {
        return citizens.get(citizenId);
    }

    /**
     * Get the first unemployed citizen.
     *
     * @return Citizen with no current job.
     */
    public CitizenData getJoblessCitizen()
    {
        for (CitizenData citizen : citizens.values())
        {
            if (citizen.getWorkBuilding() == null)
            {
                return citizen;
            }
        }

        return null;
    }

    /**
     * Get the Work Manager for the Colony.
     *
     * @return WorkManager for the Colony.
     */
    public WorkManager getWorkManager()
    {
        return workManager;
    }

    public List<BlockPos> getDeliverymanRequired()
    {

        return citizens.values().stream()
                .filter(citizen -> citizen.getWorkBuilding() != null && citizen.getJob() != null)
                .filter(citizen -> !citizen.getJob().isMissingNeededItem())
                .map(citizen -> citizen.getWorkBuilding().getLocation())
                .collect(Collectors.toList());
    }

    public MaterialSystem getMaterialSystem()
    {
        return materialSystem;
    }

    /**
     * Performed when a building of this colony finished his upgrade state.
     *
     * @param building The upgraded building
     * @param level    The new level
     */
    public void onBuildingUpgradeComplete(AbstractBuilding building, int level)
    {
        building.onUpgradeComplete(level);
    }

    public List<EntityPlayer> getMessageEntityPlayers()
    {
        return ServerUtils.getPlayersFromUUID(this.world, this.getPermissions().getMessagePlayers());
    }
}
