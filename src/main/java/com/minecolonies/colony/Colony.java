package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingTownhall;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.network.messages.*;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.*;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;
import java.util.stream.Collectors;

public class Colony implements IColony
{
    private         final   int                             id;

    //  Runtime Data
    private                 World                           world                           = null;

    //  Updates and Subscriptions
    private                 Set<EntityPlayerMP>             subscribers                     = new HashSet<>();
    private                 boolean                         isDirty                         = false;
    private                 boolean                         isCitizensDirty                 = false;
    private                 boolean                         isBuildingsDirty                = false;

    //  General Attributes
    private                 String                          name                            = "ERROR(Wasn't placed by player)";
    private         final   int                             dimensionId;
    private                 ChunkCoordinates                center;

    //  Administration/permissions
    private                 Permissions                     permissions                     = new Permissions();
    //private int autoHostile = 0;//Off

    //  Buildings
    private                 BuildingTownhall                townhall;
    private                 Map<ChunkCoordinates, Building> buildings                       = new HashMap<>();

    //  Citizenry
    private                 Map<Integer, CitizenData>       citizens                        = new HashMap<>();
    private                 int                             topCitizenId                    = 0;
    private                 int                             maxCitizens                     = Configurations.maxCitizens;


    //  Settings
    private static  final   int                             CITIZEN_CLEANUP_TICK_INCREMENT  = /*60*/ 5 * 20;   //  Once a minute

    //  Workload and Jobs
    private         final   WorkManager                     workManager                     = new WorkManager(this);

    private static  final   String                          TAG_ID                          = "id";
    private static  final   String                          TAG_NAME                        = "name";
    private static  final   String                          TAG_DIMENSION                   = "dimension";
    private static  final   String                          TAG_CENTER                      = "center";
    private static  final   String                          TAG_MAX_CITIZENS                = "maxCitizens";
    private static  final   String                          TAG_BUILDINGS                   = "buildings";
    private static  final   String                          TAG_CITIZENS                    = "citizens";
    private static  final   String                          TAG_WORK                        = "work";
    private static  final   String                          TAG_AUTO_HOSTILE                = "autoHostile";

    /**
     * Constructor for a newly created Colony.
     *
     * @param w The world the colony exists in
     * @param c The center of the colony (location of Town Hall).
     */
    public Colony(int id, World w, ChunkCoordinates c)
    {
        this(id, w.provider.dimensionId);
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
     * Call when a Colony will be destroyed.
     * Cleans up Citizens properly (removing their Colony)
     */
    protected void cleanup()
    {
        for (CitizenData citizen : citizens.values())
        {
            EntityCitizen actualCitizen = citizen.getCitizenEntity();
            if (actualCitizen != null)
            {
                actualCitizen.clearColony();
            }
        }
    }

    /**
     * Load a saved colony
     *
     * @param compound  The NBT compound containing the colony's data
     * @return          loaded colony
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
     * @param compound  compount to read from
     */
    protected void readFromNBT(NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        center = ChunkCoordUtils.readFromNBT(compound, TAG_CENTER);

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
            Building b = Building.createFromNBT(this, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
            }
        }

        //  Workload
        workManager.readFromNBT(compound.getCompoundTag(TAG_WORK));

        //autoHostile = compound.getInteger(TAG_AUTO_HOSTILE);
    }

    /**
     * Write colony to save data
     *
     * @param compound  compound to write to
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        //  Core attributes
        compound.setInteger(TAG_ID, id);
        compound.setInteger(TAG_DIMENSION, dimensionId);

        //  Basic data
        compound.setString(TAG_NAME, name);
        ChunkCoordUtils.writeToNBT(compound, TAG_CENTER, center);

        compound.setInteger(TAG_MAX_CITIZENS, maxCitizens);

        // Permissions
        permissions.savePermissions(compound);

        //  Buildings
        NBTTagList buildingTagList = new NBTTagList();
        for (Building b : buildings.values())
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

        //compound.setInteger(TAG_AUTO_HOSTILE, autoHostile);
    }

    /**
     * Returns the ID of the colony
     *
     * @return      Colony ID
     */
    public int getID()
    {
        return id;
    }

    /**
     * Returns the dimension ID
     *
     * @return      Dimension ID
     */
    public int getDimensionId()
    {
        return dimensionId;
    }

    /**
     * Returns the world the colony is in.
     *
     * @return      World the colony is in
     */
    public World getWorld()
    {
        return world;
    }

    @Override
    public String getName() { return name; }

    /**
     * Sets the name of the colony
     * Marks dirty
     *
     * @param n     new name
     */
    public void setName(String n)
    {
        name = n;
        markDirty();
    }

    /**
     * Returns the center of the colony
     *
     * @return      Chunk Coordinates of the center of the colony
     */
    public ChunkCoordinates getCenter() { return center; }

    /**
     * Marks the instance dirty
     */
    private void markDirty() { isDirty = true; }

    /**
     * Marks citizen data dirty
     */
    public void markCitizensDirty() { isCitizensDirty = true; }

    /**
     * Marks building data dirty
     */
    public void markBuildingsDirty() { isBuildingsDirty = true; }

    @Override
    public Permissions getPermissions()
    {
        return permissions;
    }

    /**
     * @see  {@link #isCoordInColony(World, int, int, int)}
     *
     * @param w         World to check
     * @param coord     ChunkCoordinates to check
     * @return          True if inside colony, otherwise false
     */
    public boolean isCoordInColony(World w, ChunkCoordinates coord)
    {
        return isCoordInColony(w, coord.posX, coord.posY, coord.posZ);
    }

    @Override
    public boolean isCoordInColony(World w, int x, int y, int z)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.equals(getWorld()) &&
               center.getDistanceSquared(x, center.posY, z) <= MathUtils.square(Configurations.workingRangeTownhall);
    }

    /**
     * @see {@link #getDistanceSquared(int, int, int)}
     *
     * @param coord     Chunk coordinate to get squared position
     * @return          Squared position from center
     */
    public float getDistanceSquared(ChunkCoordinates coord)
    {
        return getDistanceSquared(coord.posX, coord.posY, coord.posZ);
    }

    @Override
    public float getDistanceSquared(int posX, int posY, int posZ) //todo why do we pass in y, if we dont use it
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return center.getDistanceSquared(posX, center.posY, posZ);
    }

    /**
     * When the Colony's world is loaded, associate with it
     *
     * @param w     World object
     */
    public void onWorldLoad(World w)
    {
        if (w.provider.dimensionId == dimensionId)
        {
            world = w;
        }
    }

    /**
     * Unsets the world if the world unloads
     *
     * @param w     World object
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
     * Any per-server-tick logic should be performed here
     *
     * @param event     {@link cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        for (Building b : buildings.values())
        {
            b.onServerTick(event);
        }

        if (event.phase == TickEvent.Phase.END)
        {
            updateSubscribers();
        }
    }

    /**
     * Any per-world-tick logic should be performed here
     * NOTE: If the Colony's world isn't loaded, it won't have a worldtick.
     * Use onServerTick for logic that should _always_ run
     *
     * @param event     {@link cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent}
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
            for (CitizenData citizen : citizens.values())
            {
                EntityCitizen entity = citizen.getCitizenEntity();
                if (entity != null &&
                    entity.worldObj.getEntityByID(entity.getEntityId()) != entity)
                {
                    citizen.clearCitizenEntity();
                }
            }
        }

        //  Cleanup disappeared citizens
        //  It would be really nice if we didn't have to do this... but Citizens can disappear without dying!
        if (event.phase == TickEvent.Phase.START &&
            (event.world.getWorldTime() % CITIZEN_CLEANUP_TICK_INCREMENT) == 0)
        {
            //  Every CITIZEN_CLEANUP_TICK_INCREMENT, cleanup any 'lost' citizens

            //  Assume all chunks are loaded until we find one that isn't
            boolean allColonyChunksLoaded = true;

            int distanceFromCenter = Configurations.workingRangeTownhall + 48 /* 3 chunks */ + 15 /* round up a chunk */;
            for (int x = -distanceFromCenter; x <= distanceFromCenter; x += 16)
            {
                for (int z = -distanceFromCenter; z <= distanceFromCenter; z += 16)
                {
                    if (!event.world.blockExists(getCenter().posX + x, 128, getCenter().posZ + z))
                    {
                        allColonyChunksLoaded = false;
                        break;
                    }
                }

                if (!allColonyChunksLoaded)
                {
                    break;
                }
            }

            if (allColonyChunksLoaded)
            {
                //  All chunks within a good range of the colony should be loaded, so all citizens should be loaded
                //  If we don't have any references to them, destroy the citizen

                citizens.values().stream().filter(citizen -> citizen.getCitizenEntity() == null).forEach(citizen -> {
                    Log.logger.warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", getID(), citizen.getId()));
                    spawnCitizen(citizen);
                });
            }
        }

        //  Cleanup Buildings whose Blocks have gone AWOL
        if (event.phase == TickEvent.Phase.START)
        {
            List<Building> removedBuildings = null;

            for (Building building : buildings.values())
            {
                ChunkCoordinates loc = building.getLocation();
                if (event.world.blockExists(loc.posX, loc.posY, loc.posZ) &&
                        !building.isMatchingBlock(event.world.getBlock(loc.posX, loc.posY, loc.posZ)))
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
                removedBuildings.forEach(Building::destroy);
            }
        }

        //  Spawn Citizens
        if (event.phase == TickEvent.Phase.START &&
                townhall != null)
        {
            if (citizens.size() < maxCitizens)
            {
                int respawnInterval = Configurations.citizenRespawnInterval * 20;
                respawnInterval -= (60 * townhall.getBuildingLevel());

                if (event.world.getWorldTime() % respawnInterval == 0)
                {
                    spawnCitizen();
                }
            }
        }

        //  Tick Buildings
        for (Building building : buildings.values())
        {
            building.onWorldTick(event);
        }

        workManager.onWorldTick(event);
    }


    /**
     * Update Subscribers with Colony, Citizen, and Building Views
     */
    public void updateSubscribers()
    {
        //  Recompute subscribers every frame (for now)
        //  Subscribers = Owners + Players within (double working town hall range)
        Set<EntityPlayerMP> oldSubscribers = subscribers;
        subscribers = new HashSet<>();

        //  Add owners
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
        {
            if (o instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP)o;
                if (permissions.isSubscriber(player))
                {
                    subscribers.add(player);
                }
            }
        }

        //  Add nearby players
        if (world != null)
        {
            for (Object o : world.playerEntities)
            {
                if (o instanceof EntityPlayerMP)
                {
                    EntityPlayerMP player = (EntityPlayerMP)o;

                    if (subscribers.contains(player))
                    {
                        //  Already a subscriber
                        continue;
                    }

                    double distance = player.getDistanceSq(center.posX, center.posY, center.posZ);
                    if (distance < MathUtils.square(Configurations.workingRangeTownhall + 16))
                    {
                        //  Players become subscribers if they come within 16 blocks of the edge of the colony
                        subscribers.add(player);
                    }
                    else if (oldSubscribers.contains(player) &&
                             distance < MathUtils.square(Configurations.workingRangeTownhall * 2))
                    {
                        //  Players remain subscribers while they remain within double the colony's radius
                        subscribers.add(player);
                    }
                }
            }
        }

        //  Determine if any new subscribers were added this pass
        boolean hasNewSubscribers = false;
        for (EntityPlayerMP player : subscribers)
        {
            if (!oldSubscribers.contains(player))
            {
                hasNewSubscribers = true;
                break;
            }
        }

        if (!subscribers.isEmpty())
        {
            //  Send each type of update packet as appropriate:
            //      - To Subscribers if the data changes
            //      - To New Subscribers even if it hasn't changed

            //  ColonyView
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

            // Permissions
            if(permissions.isDirty() || hasNewSubscribers)
            {
                subscribers
                        .stream()
                        .filter(player -> permissions.isDirty() || !oldSubscribers.contains(player)).forEach(player -> {
                    Permissions.Rank rank = getPermissions().getRank(player);
                    MineColonies.getNetwork().sendTo(new PermissionsMessage.View(this, rank), player);
                });
            }

            //  Citizens
            if (isCitizensDirty || hasNewSubscribers)
            {
                for (CitizenData citizen : citizens.values())
                {
                    if (citizen.isDirty() || hasNewSubscribers)
                    {
                        ColonyViewCitizenViewMessage msg = new ColonyViewCitizenViewMessage(this, citizen);

                        subscribers.stream()
                                   .filter(player -> citizen.isDirty() || !oldSubscribers.contains(player))
                                   .forEach(player -> MineColonies.getNetwork().sendTo(msg, player));
                    }
                }
            }

            //  Buildings
            if (isBuildingsDirty || hasNewSubscribers)
            {
                for (Building building : buildings.values())
                {
                    if (building.isDirty() || hasNewSubscribers)
                    {
                        ColonyViewBuildingViewMessage msg = new ColonyViewBuildingViewMessage(building);

                        subscribers.stream()
                                   .filter(player -> building.isDirty() || !oldSubscribers.contains(player))
                                   .forEach(player -> MineColonies.getNetwork().sendTo(msg, player));
                    }
                }
            }
        }

//        for (EntityPlayerMP oldPlayers : oldSubscribers)
//        {
//            if (!subscribers.contains(oldPlayers))
//            {
//                //  This player should no longer subscribe
//            }
//        }

        isDirty = false;
        isCitizensDirty = false;
        isBuildingsDirty = false;
        permissions.clearDirty();

        buildings.values().forEach(Building::clearDirty);
        citizens.values().forEach(CitizenData::clearDirty);
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
     * @param data      Data to use to spawn citizen
     */
    private void spawnCitizen(CitizenData data)
    {
        int xCoord = center.posX, yCoord = center.posY, zCoord = center.posZ;

        if (!world.blockExists(center.posX, center.posY, center.posZ))
        {
            //  Chunk with TownHall Block is not loaded
            return;
        }

        ChunkCoordinates spawnPoint =
                Utils.scanForBlockNearPoint(world, new ChunkCoordinates(xCoord, yCoord, zCoord), 1, 0, 1, 2, Blocks.air, Blocks.snow_layer);

        if(spawnPoint != null)
        {
            EntityCitizen entity = new EntityCitizen(world);

            if (data == null)
            {
                data = new CitizenData(++topCitizenId, this);
                data.initializeFromEntity(entity);

                citizens.put(data.getId(), data);

                if (getMaxCitizens() == getCitizens().size())
                {
                    //TODO: add Colony Name prefix?
                    LanguageHandler.sendPlayersLocalizedMessage(
                            Utils.getPlayersFromUUID(world, permissions.getMessagePlayers()),
                            "tile.blockHutTownhall.messageMaxSize");
                }
            }

            entity.setColony(this, data);

            entity.setPosition(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ);
            world.spawnEntityInWorld(entity);

            markCitizensDirty();
        }
    }

    /*
     *
     * BUILDINGS
     *
     */

    /**
     * Returns a map with all buildings within the colony
     * Key is ID (Coordinates), value is building object
     *
     * @return      Map with ID (coordinates) as key, and buildings as value
     */
    public Map<ChunkCoordinates, Building> getBuildings()
    {
        return Collections.unmodifiableMap(buildings);
    }

    /**
     * Gets the town hall of the colony
     *
     * @return      Town hall of the colony
     */
    public BuildingTownhall getTownhall()
    {
        return townhall;
    }

    @Override
    public boolean hasTownhall() { return townhall != null; }

    /**
     * Get building in Colony by ID
     *
     * @param buildingId    ID (coordinates) of the building to get
     * @return              Building belonging to the given ID
     */
    public Building getBuilding(ChunkCoordinates buildingId)
    {
        return buildings.get(buildingId);
    }

    //TODO document
    /**
     * Get building in Colony by ID
     *
     * @param buildingId    ID (coordinates) of the building to get
     * @param type          Type of building
     * @param <BUILDING>
     * @return
     */
    public <BUILDING extends Building> BUILDING getBuilding(ChunkCoordinates buildingId, Class<BUILDING> type)
    {
        try
        {
            return type.cast(buildings.get(buildingId));
        }
        catch (ClassCastException ignored) {}

        return null;
    }

    /**
     * Add a Building to the Colony
     *
     * @param building      Building to add to the colony
     */
    private void addBuilding(Building building)
    {
        buildings.put(building.getID(), building);
        building.markDirty();

        if (building instanceof BuildingTownhall)
        {
            //  Limit 1 town hall
            if (townhall == null)
            {
                townhall = (BuildingTownhall) building;
            }
        }
    }

    /**
     * Creates a building from a tile entity and adds it to the colony
     *
     * @param tileEntity    Tile entity to build a building from
     * @return              Building that was created and added
     */
    public Building addNewBuilding(TileEntityColonyBuilding tileEntity)
    {
        tileEntity.setColony(this);

        Building building = Building.create(this, tileEntity);
        if (building != null)
        {
            addBuilding(building);
            tileEntity.setBuilding(building);

            Log.logger.info(String.format("Colony %d - new Building for %s at %s",
                                          getID(),
                                          tileEntity.getBlockType().getClass(),
                                          tileEntity.getPosition()));
        }
        else
        {
            Log.logger.error(String.format("Colony %d unable to create Building for %s at %s",
                                           getID(),
                                           tileEntity.getBlockType().getClass(),
                                           tileEntity.getPosition()));
        }

        calculateMaxCitizens();

        ColonyManager.markDirty();

        return building;
    }

    /**
     * Remove a Building from the Colony (when it is destroyed)
     *
     * @param building          Building to remove
     */
    public void removeBuilding(Building building)
    {
        if (buildings.remove(building.getID()) != null)
        {
            ColonyViewRemoveBuildingMessage msg = new ColonyViewRemoveBuildingMessage(this, building.getID());
            for (EntityPlayerMP player : subscribers)
            {
                MineColonies.getNetwork().sendTo(msg, player);
            }

            Log.logger.info(String.format("Colony %d - removed Building %s of type %s",
                                          getID(),
                                          building.getID(),
                                          building.getSchematicName()));
        }

        if (building == townhall)
        {
            townhall = null;
        }

        //  Allow Citizens to fix up any data that wasn't fixed up by the Building's own onDestroyed
        for (CitizenData citizen : citizens.values())
        {
            citizen.onRemoveBuilding(building);
        }

        calculateMaxCitizens();

        ColonyManager.markDirty();
    }

    /*
     *
     * CITIZENS
     *
     */

    /**
     * Returns the max amount of citizens in the colony
     *
     * @return          Max amount of citizens
     */
    public int getMaxCitizens()
    {
        return maxCitizens;
    }
    //public void setMaxCitizens();

    /**
     * Recalculates how many citizen can be in the colony
     */
    public void calculateMaxCitizens()
    {
        int newMaxCitizens = Configurations.maxCitizens;

        for (Building b : buildings.values())
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
     * Returns a map of citizens in the colony
     * The map has ID as key, and citizen data as value
     *
     * @return          Map of citizens in the colony, with as key the citizen ID, and as value the citiizen data
     */
    public Map<Integer, CitizenData> getCitizens() { return Collections.unmodifiableMap(citizens); }

    public List<EntityCitizen> getActiveCitizenEntities()
    {
        List<EntityCitizen> activeCitizens = new ArrayList<>();

        for (CitizenData citizen : citizens.values())
        {
            EntityCitizen actualCitizen = citizen.getCitizenEntity();
            if (actualCitizen != null)
            {
                activeCitizens.add(actualCitizen);
            }
        }

        return activeCitizens;
    }

    /**
     * Removes a citizen from the colony
     *
     * @param citizen   Citizen data to remove
     */
    public void removeCitizen(CitizenData citizen)
    {
        //  Remove the Citizen
        citizens.remove(citizen.getId());

        for (Building building : buildings.values())
        {
            building.removeCitizen(citizen);
        }

        workManager.clearWorkForCitizen(citizen);

        //  Inform Subscribers of removed citizen
        ColonyViewRemoveCitizenMessage msg = new ColonyViewRemoveCitizenMessage(this, citizen.getId());
        for (EntityPlayerMP player : subscribers)
        {
            MineColonies.getNetwork().sendTo(msg, player);
        }
    }

    /**
     * Get citizen by ID
     *
     * @param citizenId ID of the Citizen
     * @return          CitizenData associated with the ID, or null if it was not found
     */
    public CitizenData getCitizen(int citizenId)
    {
        return citizens.get(citizenId);
    }

    /**
     * Get the first unemployed citizen
     *
     * @return          Citizen with no current job
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
     * Get the Work Manager for the Colony
     *
     * @return          WorkManager for the Colony
     */
    public WorkManager getWorkManager()
    {
        return workManager;
    }

    public List<ChunkCoordinates> getDeliverymanRequired()
    {

        return citizens.values().stream()
                       .filter(citizen -> citizen.getWorkBuilding() != null && citizen.getJob() != null)
                       .filter(citizen -> !citizen.getJob().isMissingNeededItem())
                       .map(citizen -> citizen.getWorkBuilding().getLocation())
                       .collect(Collectors.toList());
    }

    //public int getAutoHostile()
    //{
    //    return autoHostile;
    //}

    //public void setAutoHostile(int value)
    //{
    //    autoHostile = value;
    //}
}
