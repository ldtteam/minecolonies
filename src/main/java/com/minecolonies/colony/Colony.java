package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.ColonyBuildingViewMessage;
import com.minecolonies.network.messages.ColonyViewCitizensMessage;
import com.minecolonies.network.messages.ColonyViewMessage;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;

public class Colony
{
    private final UUID id;

    //  Runtime Data
    private World world = null;

    //  Updates and Subscriptions
    private Set<EntityPlayerMP>    subscribers      = new HashSet<EntityPlayerMP>();
    private boolean                isDirty          = false;   //  TODO - Move to using bits, and more of them for targetted update packets
    private boolean                isCitizensDirty  = false;
    private boolean                isBuildingsDirty = false;
    private List<UUID>             removedCitizens  = new ArrayList<UUID>();
    private List<ChunkCoordinates> removedBuildings = new ArrayList<ChunkCoordinates>();

    //  General Attributes
    private String name = "ERROR(Wasn't placed by player)";
    private final int              dimensionId;
    private       ChunkCoordinates center;

    //  Administration/permissions
    private Permissions permissions = new Permissions();
    //private int autoHostile = 0;//Off

    //  Buildings
    private BuildingTownHall townhall;
    private Map<ChunkCoordinates, Building> buildings = new HashMap<ChunkCoordinates, Building>();

    //  Citizenry
    private              int                    maxCitizens                = Constants.DEFAULT_MAX_CITIZENS;
    private              Map<UUID, CitizenData> citizens                   = new HashMap<UUID, CitizenData>();
    final static private int                    CITIZEN_CLEANUP_TICK_DELAY = 60 * 20;   //  Once a minute

    //  Workload and Jobs
    private Map<ChunkCoordinates, String> buildingUpgradeMap = new HashMap<ChunkCoordinates, String>();

    private final static String TAG_ID                = "id";
    private final static String TAG_NAME              = "name";
    private final static String TAG_DIMENSION         = "dimension";
    private final static String TAG_CENTER            = "center";
    private final static String TAG_MAX_CITIZENS      = "maxCitizens";
    private final static String TAG_BUILDINGS         = "buildings";
    private final static String TAG_CITIZENS          = "citizens";
    private final static String TAG_BUILDING_UPGRADES = "buildingUpgrades";
    private final static String TAG_AUTO_HOSTILE = "autoHostile";

    /**
     * Constructor for a newly created Colony.
     *
     * @param w The world the colony exists in
     * @param c The center of the colony (location of Town Hall).
     */
    public Colony(World w, ChunkCoordinates c)
    {
        this(UUID.randomUUID(), w.provider.dimensionId);
        center = c;
        world = w;
    }

    /**
     * Base constructor.
     *
     * @param uuid The current id for the colony
     * @param dim  The world the colony exists in
     */
    protected Colony(UUID uuid, int dim)
    {
        id = uuid;
        dimensionId = dim;
    }

    /**
     * Call when a Colony will be destroyed.
     * Cleans up Citizens properly (removing their Colony)
     */
    protected void cleanup()
    {
        for(CitizenData citizen : citizens.values())
        {
            EntityCitizen actualCitizen = citizen.getCitizenEntity();
            if(actualCitizen != null)
            {
                actualCitizen.clearColony();
            }
        }
    }

    /**
     * Load a saved colony
     *
     * @param compound The NBT compound containing the colony's data
     * @return loaded colony
     */
    public static Colony loadColony(NBTTagCompound compound)
    {
        UUID id = UUID.fromString(compound.getString(TAG_ID));
        int dimensionId = compound.getInteger(TAG_DIMENSION);
        Colony c = new Colony(id, dimensionId);
        c.readFromNBT(compound);
        return c;
    }

    /**
     * Read colony from saved data
     *
     * @param compound
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
        NBTTagList buildingUpgradeTagList = compound.getTagList(TAG_BUILDING_UPGRADES, NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingUpgradeTagList.tagCount(); ++i)
        {
            NBTTagCompound upgrade = buildingUpgradeTagList.getCompoundTagAt(i);
            ChunkCoordinates coords = ChunkCoordUtils.readFromNBT(upgrade, TAG_ID);
            String name = upgrade.getString(TAG_NAME);
            buildingUpgradeMap.put(coords, name);
        }

        //autoHostile = compound.getInteger(TAG_AUTO_HOSTILE);
    }

    /**
     * Write colony to save data
     *
     * @param compound
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        //  Core attributes
        compound.setString(TAG_ID, id.toString());
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
        if (!buildingUpgradeMap.isEmpty())
        {
            NBTTagList buildingUpgradeTagList = new NBTTagList();
            for (Map.Entry<ChunkCoordinates, String> entry : buildingUpgradeMap.entrySet())
            {
                NBTTagCompound upgrade = new NBTTagCompound();
                ChunkCoordUtils.writeToNBT(upgrade, TAG_ID, entry.getKey());
                upgrade.setString(TAG_NAME, entry.getValue());
                buildingUpgradeTagList.appendTag(upgrade);
            }
            compound.setTag(TAG_BUILDING_UPGRADES, buildingUpgradeTagList);
        }

        //compound.setInteger(TAG_AUTO_HOSTILE, autoHostile);
    }

    public UUID getID()
    {
        return id;
    }

    public int getDimensionId()
    {
        return dimensionId;
    }

    public World getWorld()
    {
        return world;
    }

    public String getName() { return name; }
    public void setName(String n)
    {
        name = n;
        markDirty();
    }

    public ChunkCoordinates getCenter() { return center; }

    private void markDirty() { isDirty = true; }
    public void markCitizensDirty() { isCitizensDirty = true; }
    public void markBuildingsDirty() { isBuildingsDirty = true; }

    public Permissions getPermissions()
    {
        return permissions;
    }

    /**
     * Determine if a given chunk coordinate is considered to be within the colony's bounds
     *
     * @param coord
     * @return
     */
    public boolean isCoordInColony(World w, ChunkCoordinates coord)
    {
        return isCoordInColony(w, coord.posX, coord.posY, coord.posZ);
    }

    public boolean isCoordInColony(World w, int x, int y, int z)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.equals(getWorld()) &&
                center.getDistanceSquared(x, center.posY, z) <= Utils.square(Configurations.workingRangeTownhall);
    }

    public float getDistanceSquared(ChunkCoordinates coord)
    {
        return getDistanceSquared(coord.posX, coord.posY, coord.posZ);
    }

    public float getDistanceSquared(int posX, int posY, int posZ)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return center.getDistanceSquared(posX, center.posY, posZ);
    }

    /**
     * When the Colony's world is loaded, associate with it
     *
     * @param w
     */
    public void onWorldLoad(World w)
    {
        if (w.provider.dimensionId == dimensionId)
        {
            world = w;
        }
    }

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
     * @param event
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
     * @param event
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
            (event.world.getWorldInfo().getWorldTime() % CITIZEN_CLEANUP_TICK_DELAY) == 0)
        {
            //  Every CITIZEN_CLEANUP_TICK_DELAY, cleanup any 'lost' citizens

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

                for (Iterator<Map.Entry<UUID, CitizenData>> it = citizens.entrySet().iterator(); it.hasNext(); )
                {
                    Map.Entry<UUID, CitizenData> entry = it.next();
                    CitizenData citizen = entry.getValue();
                    if (citizen.getCitizenEntity() == null)
                    {
                        MineColonies.logger.warn(String.format("Citizen '%s' has gone AWOL, respawning them!", entry.getKey().toString()));
                        spawnCitizen(citizen);
                    }
                }
            }
        }

        //  Cleanup Buildings whose Blocks have gone AWOL
        if (event.phase == TickEvent.Phase.START)
        {
            for (Iterator<Map.Entry<ChunkCoordinates, Building>> it = buildings.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry<ChunkCoordinates, Building> entry = it.next();
                Building building = entry.getValue();

                ChunkCoordinates loc = building.getLocation();
                if (event.world.blockExists(loc.posX, loc.posY, loc.posZ) &&
                        !building.isMatchingBlock(event.world.getBlock(loc.posX, loc.posY, loc.posZ)))
                {
                    //  Sanity cleanup
                    it.remove();

                    removedBuildings.add(building.getLocation());
                    markDirty();

                    building.destroy();
                }
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

                if (event.world.getWorldInfo().getWorldTime() % respawnInterval == 0)
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
    }


    /**
     * Update Subscribers with Colony, Citizen, and Building Views
     */
    public void updateSubscribers()
    {
        //  Recompute subscribers every frame (for now)
        //  Subscribers = Owners + Players within (double working town hall range)
        Set<EntityPlayerMP> oldSubscribers = subscribers;
        subscribers = new HashSet<EntityPlayerMP>();

        //  Add owners
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
        {
            if (o instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP)o;
                if (permissions.getSubscribers().contains(player.getGameProfile().getId()))//TODO: adapt to new permissions
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
                    if (distance < Utils.square(Configurations.workingRangeTownhall + 16))
                    {
                        //  Players become subscribers if they come within 16 blocks of the edge of the colony
                        subscribers.add(player);
                    }
                    else if (oldSubscribers.contains(player) &&
                            distance < Utils.square(Configurations.workingRangeTownhall * 2))
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
                NBTTagCompound compound = new NBTTagCompound();
                ColonyView.createNetworkData(this, compound);

                for (EntityPlayerMP player : subscribers)
                {
                    boolean isNewSubscriber = !oldSubscribers.contains(player);
                    if (isDirty || isNewSubscriber)
                    {
                        MineColonies.network.sendTo(new ColonyViewMessage(id, compound, isNewSubscriber), player);
                    }
                }
            }

            // Permissions
            if(permissions.isDirty() || hasNewSubscribers)
            {
                NBTTagCompound compound = new NBTTagCompound();
                permissions.createViewNetworkData(compound);
                PermissionsMessage.View msg = new PermissionsMessage.View(id, compound);

                for (EntityPlayerMP player : subscribers)
                {
                    if (permissions.isDirty() || !oldSubscribers.contains(player))
                    {
                        MineColonies.network.sendTo(msg, player);
                    }
                }
            }
            //  Citizens
            if (isCitizensDirty || hasNewSubscribers)
            {
                for (CitizenData citizen : citizens.values())
                {
                    if (citizen.isDirty() || hasNewSubscribers)
                    {
                        NBTTagCompound compound = new NBTTagCompound();
                        citizen.createViewNetworkData(compound);
                        ColonyViewCitizensMessage msg = new ColonyViewCitizensMessage(id, citizen.getId(), compound);

                        for (EntityPlayerMP player : subscribers)
                        {
                            if (citizen.isDirty() || !oldSubscribers.contains(player))
                            {
                                MineColonies.network.sendTo(msg, player);
                            }
                        }
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
                        NBTTagCompound compound = new NBTTagCompound();
                        building.createViewNetworkData(compound);
                        ColonyBuildingViewMessage msg = new ColonyBuildingViewMessage(id, building.getID(), compound);

                        for (EntityPlayerMP player : subscribers)
                        {
                            if (building.isDirty() || !oldSubscribers.contains(player))
                            {
                                MineColonies.network.sendTo(msg, player);
                            }
                        }
                    }
                }
            }
        }

        removedCitizens.clear();
        removedBuildings.clear();   //  We will have set these

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

        for (Building building : buildings.values())
        {
            building.clearDirty();
        }
        for (CitizenData citizen : citizens.values())
        {
            citizen.clearDirty();
        }
    }

    /**
     * Spawn a brand new Citizen
     */
    private void spawnCitizen()
    {
        spawnCitizen(null);
    }

    private void spawnCitizen(CitizenData data)
    {
        int xCoord = center.posX, yCoord = center.posY, zCoord = center.posZ;

        if (!world.blockExists(center.posX, center.posY, center.posZ))
        {
            //  Chunk with TownHall Block is not loaded
            return;
        }

        ChunkCoordinates spawnPoint = Utils.scanForBlockNearPoint(world, Blocks.air, xCoord, yCoord, zCoord, 1, 0, 1);
        if(spawnPoint == null)
        {
            spawnPoint = Utils.scanForBlockNearPoint(world, Blocks.snow_layer, xCoord, yCoord, zCoord, 1, 0, 1);
        }

        if(spawnPoint != null)
        {
            EntityCitizen entity = null;

            if (data == null)
            {
                entity = new EntityCitizen(world);

                data = CitizenData.createFromEntity(entity, this);
                citizens.put(data.getId(), data);
                entity.setColony(this, data);

                if (getMaxCitizens() == getCitizens().size())
                {
                    LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, permissions.getMessagePlayers()), "tile.blockHutTownhall.messageMaxSize");//TODO: add Colony Name prefix?
                }
            }
            else
            {
                //  TODO: Restore the actual EntityCitizen subclass
                //  (Although the current code is pretty good about detecting and fixing the issue)
                entity = new EntityCitizen(world, data.getId());
                entity.setColony(this, data);
            }

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

    public Map<ChunkCoordinates, Building> getBuildings()
    {
        return Collections.unmodifiableMap(buildings);
    }

    public BuildingTownHall getTownhall()
    {
        return townhall;
    }

    public List<UUID> getRemovedCitizens() { return Collections.unmodifiableList(removedCitizens); }
    public List<ChunkCoordinates> getRemovedBuildings() { return Collections.unmodifiableList(removedBuildings); }

    /**
     * Get building in Colony by ID
     *
     * @param buildingId
     * @return
     */
    public Building getBuilding(ChunkCoordinates buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Add a Building to the Colony
     *
     * @param building
     */
    private void addBuilding(Building building)
    {
        buildings.put(building.getLocation(), building);
        building.markDirty();

        if (building instanceof BuildingTownHall)
        {
            //  Limit 1 town hall
            if (townhall == null)
            {
                townhall = (BuildingTownHall) building;
            }
        }
    }

    public Building addNewBuilding(TileEntityColonyBuilding tileEntity)
    {
        tileEntity.setColony(this);

        Building building = Building.create(this, tileEntity);
        if (building != null)
        {
            addBuilding(building);
            tileEntity.setBuilding(building);
        }
        return building;
    }

    /**
     * Remove a Building from the Colony (when it is destroyed)
     *
     * @param building
     */
    public void removeBuilding(Building building)
    {
        if (buildings.remove(building.getLocation()) != null)
        {
            removedBuildings.add(building.getLocation());
            markDirty();
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
    }

    /*
     *
     * CITIZENS
     *
     */

    public int getMaxCitizens() { return maxCitizens; }
    //public void setMaxCitizens();

    public Map<UUID, CitizenData> getCitizens() { return Collections.unmodifiableMap(citizens); }

    public List<EntityCitizen> getActiveCitizenEntities()
    {
        List<EntityCitizen> activeCitizens = new ArrayList<EntityCitizen>();

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

    public boolean isCitizen(UUID c) { return citizens.containsKey(c); }

    public void removeCitizen(EntityCitizen citizen)
    {
        removeCitizen(citizen.getCitizenData());
    }

    public void removeCitizen(CitizenData citizen)
    {
        removedCitizens.add(citizen.getId());
        citizens.remove(citizen.getId());
        markDirty();

//        if (citizen.getHomeBuilding() != null)
//        {
//            citizen.getHomeBuilding().removeCitizen(citizen);
//        }
//
//        if (citizen.getWorkBuilding() != null)
//        {
//            citizen.getWorkBuilding().removeCitizen(citizen);
//        }

        for (Building building : buildings.values())
        {
            building.removeCitizen(citizen);
        }
    }

    /**
     * Get citizen by ID
     * @param citizenId
     * @return
     */
    public CitizenData getCitizen(UUID citizenId)
    {
        return citizens.get(citizenId);
    }

    /**
     * Get citizen's entity by ID
     * @param citizenId
     * @return
     */
    public EntityCitizen getCitizenEntity(UUID citizenId)
    {
        CitizenData citizen = citizens.get(citizenId);
        return (citizen != null) ? citizen.getCitizenEntity() : null;
    }

    /**
     * Get an idle citizen
     *
     * @return Citizen with no current job
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

    public void addBuildingForUpgrade(Building building, int level)
    {
        String upgradeName = building.getSchematicName() + level;
        buildingUpgradeMap.put(building.getID(), upgradeName);
    }

    public void removeBuildingForUpgrade(ChunkCoordinates pos)
    {
        buildingUpgradeMap.remove(pos);
    }

    public Map<ChunkCoordinates, String> getBuildingUpgrades()
    {
        return buildingUpgradeMap;
    }

    public List<ChunkCoordinates> getDeliverymanRequired()
    {
        List<ChunkCoordinates> deliverymanRequired = new ArrayList<ChunkCoordinates>();

        for (CitizenData citizen : citizens.values())
        {
            EntityCitizen entity = citizen.getCitizenEntity();
            if (citizen.getWorkBuilding() != null &&
                    entity instanceof EntityWorker)
            {
                EntityWorker worker = (EntityWorker)entity;
                if (!worker.hasItemsNeeded())
                {
                    deliverymanRequired.add(citizen.getWorkBuilding().getLocation());
                }
            }
        }

        return deliverymanRequired;
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
