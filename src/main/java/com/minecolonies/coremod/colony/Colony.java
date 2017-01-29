package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingFarmer;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.colony.buildings.BuildingTownHall;
import com.minecolonies.coremod.colony.materials.MaterialSystem;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.farmer.Field;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class describes a colony and contains all the data and methods for manipulating a Colony.
 */
public class Colony implements IColony
{
    //  Settings
    private static final int    CITIZEN_CLEANUP_TICK_INCREMENT = 5 * 20;
    private static final String TAG_ID                         = "id";
    private static final String TAG_NAME                       = "name";
    private static final String TAG_DIMENSION                  = "dimension";
    private static final String TAG_CENTER                     = "center";
    private static final String TAG_MAX_CITIZENS               = "maxCitizens";
    private static final String TAG_BUILDINGS                  = "buildings";
    private static final String TAG_CITIZENS                   = "citizens";
    private static final String TAG_ACHIEVEMENT                = "achievement";
    private static final String TAG_ACHIEVEMENT_LIST           = "achievementlist";
    private static final String TAG_WORK                       = "work";
    private static final String TAG_MANUAL_HIRING              = "manualHiring";
    private static final String TAG_WAYPOINT                   = "waypoints";
    private static final String TAG_BLOCK                      = "blockState";

    //private int autoHostile = 0;//Off
    private static final String TAG_FIELDS                  = "fields";
    private static final String TAG_MOB_KILLS               = "mobKills";
    private static final int    NUM_MOBS_ACHIEVEMENT_FIRST  = 1;
    private static final int    NUM_MOBS_ACHIEVEMENT_SECOND = 25;
    private static final int    NUM_MOBS_ACHIEVEMENT_THIRD  = 100;
    private static final int    NUM_MOBS_ACHIEVEMENT_FOURTH = 500;
    private static final int    NUM_MOBS_ACHIEVEMENT_FIFTH  = 1000;
    private static final int    CHECK_WAYPOINT_EVERY        = 100;
    private final int id;
    //  General Attributes
    private final int dimensionId;
    //  Buildings
    private final Map<BlockPos, Field>       fields    = new HashMap<>();
    //Additional Waypoints.
    private final Map<BlockPos, IBlockState> wayPoints = new HashMap<>();

    @NotNull
    private final List<Achievement> colonyAchievements;
    //  Workload and Jobs
    private final WorkManager                     workManager      = new WorkManager(this);
    private final MaterialSystem                  materialSystem   = new MaterialSystem();
    @NotNull
    private final Map<BlockPos, AbstractBuilding> buildings        = new HashMap<>();
    //  Citizenry
    @NotNull
    private final Map<Integer, CitizenData>       citizens         = new HashMap<>();
    //  Runtime Data
    @Nullable
    private       World                           world            = null;
    //  Updates and Subscriptions
    @NotNull
    private       Set<EntityPlayerMP>             subscribers      = new HashSet<>();
    private       boolean                         isDirty          = false;
    private       boolean                         isCitizensDirty  = false;
    private       boolean                         isBuildingsDirty = false;
    private       boolean                         manualHiring     = false;
    private       boolean                         isFieldsDirty    = false;
    private       String              name             = "ERROR(Wasn't placed by player)";
    private BlockPos         center;
    //  Administration/permissions
    @NotNull
    private Permissions      permissions;
    @Nullable
    private BuildingTownHall townHall;
    private       int                             topCitizenId = 0;
    private       int                             maxCitizens  = Configurations.maxCitizens;
    private       int                             killedMobs   = 0;

    /**
     * Constructor for a newly created Colony.
     *
     * @param id The id of the colony to create.
     * @param w  The world the colony exists in.
     * @param c  The center of the colony (location of Town Hall).
     */
    Colony(final int id, @NotNull final World w, final BlockPos c)
    {
        this(id, w.provider.getDimension());
        center = c;
        world = w;
        this.permissions = new Permissions(this);
    }

    /**
     * Base constructor.
     *
     * @param id  The current id for the colony.
     * @param dim The world the colony exists in.
     */
    protected Colony(final int id, final int dim)
    {
        this.id = id;
        this.dimensionId = dim;
        this.permissions = new Permissions(this);
        this.colonyAchievements = new ArrayList<>();

        // Register a new event handler
        MinecraftForge.EVENT_BUS.register(new ColonyPermissionEventHandler(this));
    }

    /**
     * Load a saved colony.
     *
     * @param compound The NBT compound containing the colony's data.
     * @return loaded colony.
     */
    @NotNull
    public static Colony loadColony(@NotNull final NBTTagCompound compound)
    {
        final int id = compound.getInteger(TAG_ID);
        final int dimensionId = compound.getInteger(TAG_DIMENSION);
        @NotNull final Colony c = new Colony(id, dimensionId);
        c.readFromNBT(compound);
        return c;
    }

    /**
     * Read colony from saved data.
     *
     * @param compound compound to read from.
     */
    private void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        center = BlockPosUtil.readFromNBT(compound, TAG_CENTER);

        manualHiring = compound.getBoolean(TAG_MANUAL_HIRING);
        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);
        killedMobs = compound.getInteger(TAG_MOB_KILLS);

        // Permissions
        permissions.loadPermissions(compound);

        //  Citizens before Buildings, because Buildings track the Citizens
        final NBTTagList citizenTagList = compound.getTagList(TAG_CITIZENS, NBT.TAG_COMPOUND);
        for (int i = 0; i < citizenTagList.tagCount(); ++i)
        {
            final NBTTagCompound citizenCompound = citizenTagList.getCompoundTagAt(i);
            final CitizenData data = CitizenData.createFromNBT(citizenCompound, this);
            citizens.put(data.getId(), data);
            topCitizenId = Math.max(topCitizenId, data.getId());
        }

        //  Buildings
        final NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS, NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.tagCount(); ++i)
        {
            final NBTTagCompound buildingCompound = buildingTagList.getCompoundTagAt(i);
            @Nullable final AbstractBuilding b = AbstractBuilding.createFromNBT(this, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
            }
        }

        // Fields
        final NBTTagList fieldTagList = compound.getTagList(TAG_FIELDS, NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.tagCount(); ++i)
        {
            final NBTTagCompound fieldCompound = fieldTagList.getCompoundTagAt(i);
            final Field f = Field.createFromNBT(this, fieldCompound);
            if (f != null)
            {
                addField(f);
            }
        }

        // Restore colony achievements
        final NBTTagList achievementTagList = compound.getTagList(TAG_ACHIEVEMENT_LIST, NBT.TAG_COMPOUND);
        for (int i = 0; i < achievementTagList.tagCount(); ++i)
        {
            final NBTTagCompound achievementCompound = achievementTagList.getCompoundTagAt(i);
            final String achievementKey = achievementCompound.getString(TAG_ACHIEVEMENT);
            final StatBase statBase = StatList.getOneShotStat(achievementKey);
            if (statBase instanceof Achievement)
            {
                colonyAchievements.add((Achievement) statBase);
            }
        }

        //  Workload
        workManager.readFromNBT(compound.getCompoundTag(TAG_WORK));


        // Waypoints
        final NBTTagList wayPointTagList = compound.getTagList(TAG_WAYPOINT, NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.tagCount(); ++i)
        {
            final NBTTagCompound blockAtPos = wayPointTagList.getCompoundTagAt(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(blockAtPos, TAG_WAYPOINT);
            final IBlockState state = NBTUtil.readBlockState(blockAtPos);
            wayPoints.put(pos, state);
        }
    }

    /**
     * Add a AbstractBuilding to the Colony.
     *
     * @param building AbstractBuilding to add to the colony.
     */
    private void addBuilding(@NotNull final AbstractBuilding building)
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
     * Add a Building to the Colony.
     *
     * @param field Field to add to the colony.
     */
    private void addField(@NotNull final Field field)
    {
        fields.put(field.getID(), field);
    }

    /**
     * Write colony to save data.
     *
     * @param compound compound to write to.
     */
    protected void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        //  Core attributes
        compound.setInteger(TAG_ID, id);
        compound.setInteger(TAG_DIMENSION, dimensionId);

        //  Basic data
        compound.setString(TAG_NAME, name);
        BlockPosUtil.writeToNBT(compound, TAG_CENTER, center);

        compound.setBoolean(TAG_MANUAL_HIRING, manualHiring);
        compound.setInteger(TAG_MAX_CITIZENS, maxCitizens);

        compound.setInteger(TAG_MOB_KILLS, killedMobs);

        // Permissions
        permissions.savePermissions(compound);

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
        for (@NotNull final Field f : fields.values())
        {
            @NotNull final NBTTagCompound fieldCompound = new NBTTagCompound();
            f.writeToNBT(fieldCompound);
            fieldTagList.appendTag(fieldCompound);
        }
        compound.setTag(TAG_FIELDS, fieldTagList);

        //  Citizens
        @NotNull final NBTTagList citizenTagList = new NBTTagList();
        for (@NotNull final CitizenData citizen : citizens.values())
        {
            @NotNull final NBTTagCompound citizenCompound = new NBTTagCompound();
            citizen.writeToNBT(citizenCompound);
            citizenTagList.appendTag(citizenCompound);
        }
        compound.setTag(TAG_CITIZENS, citizenTagList);

        //  Achievements
        @NotNull final NBTTagList achievementsTagList = new NBTTagList();
        for (@NotNull final Achievement achievement : this.colonyAchievements)
        {
            @NotNull final NBTTagCompound achievementCompound = new NBTTagCompound();
            achievementCompound.setString(TAG_ACHIEVEMENT, achievement.statId);
            achievementsTagList.appendTag(achievementCompound);
        }
        compound.setTag(TAG_ACHIEVEMENT_LIST, achievementsTagList);

        //  Workload
        @NotNull final NBTTagCompound workManagerCompound = new NBTTagCompound();
        workManager.writeToNBT(workManagerCompound);
        compound.setTag(TAG_WORK, workManagerCompound);

        // Waypoints
        @NotNull final NBTTagList wayPointTagList = new NBTTagList();
        for (@NotNull final Map.Entry<BlockPos, IBlockState> entry : wayPoints.entrySet())
        {
            @NotNull final NBTTagCompound wayPointCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(wayPointCompound, TAG_WAYPOINT, entry.getKey());
            NBTUtil.writeBlockState(wayPointCompound, entry.getValue());

            wayPointTagList.appendTag(wayPointCompound);
        }
        compound.setTag(TAG_WAYPOINT, wayPointTagList);
    }

    /**
     * Returns the dimension ID.
     *
     * @return Dimension ID.
     */
    public int getDimension()
    {
        return dimensionId;
    }

    /**
     * Returns the center of the colony.
     *
     * @return Chunk Coordinates of the center of the colony.
     */
    @Override
    public BlockPos getCenter()
    {
        return center;
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
    public void setName(final String n)
    {
        name = n;
        markDirty();
    }

    /**
     * Marks the instance dirty.
     */
    private void markDirty()
    {
        isDirty = true;
    }

    @NotNull
    @Override
    public Permissions getPermissions()
    {
        return permissions;
    }

    @Override
    public boolean isCoordInColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.equals(getWorld())
                 && BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ())) <= MathUtils.square(Configurations.workingRangeTownHall);
    }

    /**
     * Returns the world the colony is in.
     *
     * @return World the colony is in.
     */
    @Nullable
    public World getWorld()
    {
        return world;
    }

    @Override
    public long getDistanceSquared(@NotNull final BlockPos pos)
    {
        return BlockPosUtil.getDistanceSquared2D(center, pos);
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    /**
     * Returns the ID of the colony.
     *
     * @return Colony ID.
     */
    @Override
    public int getID()
    {
        return id;
    }

    /**
     * Increment the mobs killed by this colony.
     * <p>
     * Will award achievements for mobs killed.
     */
    public void incrementMobsKilled()
    {
        killedMobs++;
        final int mobKills = this.getKilledMobs();
        if (mobKills >= NUM_MOBS_ACHIEVEMENT_FIRST)
        {
            this.triggerAchievement(ModAchievements.achievementKillOneMob);
        }
        if (mobKills >= NUM_MOBS_ACHIEVEMENT_SECOND)
        {
            this.triggerAchievement(ModAchievements.achievementKill25Mobs);
        }
        if (mobKills >= NUM_MOBS_ACHIEVEMENT_THIRD)
        {
            this.triggerAchievement(ModAchievements.achievementKill100Mobs);
        }
        if (mobKills >= NUM_MOBS_ACHIEVEMENT_FOURTH)
        {
            this.triggerAchievement(ModAchievements.achievementKill500Mobs);
        }
        if (mobKills >= NUM_MOBS_ACHIEVEMENT_FIFTH)
        {
            this.triggerAchievement(ModAchievements.achievementKill1000Mobs);
        }
    }

    /**
     * get the amount of killed mobs.
     *
     * @return amount of mobs killed
     */
    public int getKilledMobs()
    {
        return killedMobs;
    }

    /**
     * Triggers an achievement on this colony.
     * <p>
     * Will automatically sync to all players.
     *
     * @param achievement The achievement to trigger
     */
    public void triggerAchievement(@NotNull final Achievement achievement)
    {
        if (this.colonyAchievements.contains(achievement))
        {
            return;
        }

        this.colonyAchievements.add(achievement);

        AchievementUtils.syncAchievements(this);
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

    /**
     * When the Colony's world is loaded, associate with it.
     *
     * @param w World object.
     */
    public void onWorldLoad(@NotNull final World w)
    {
        if (w.provider.getDimension() == dimensionId)
        {
            world = w;
        }
    }

    /**
     * Unsets the world if the world unloads.
     *
     * @param w World object.
     */
    public void onWorldUnload(@NotNull final World w)
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
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {
        for (@NotNull final AbstractBuilding b : buildings.values())
        {
            b.onServerTick(event);
        }

        if (event.phase == TickEvent.Phase.END)
        {
            updateSubscribers();
        }
    }

    /**
     * Update Subscribers with Colony, Citizen, and AbstractBuilding Views.
     */
    public void updateSubscribers()
    {
        //  Recompute subscribers every frame (for now)
        //  Subscribers = Owners + Players within (double working town hall range)
        @NotNull final Set<EntityPlayerMP> oldSubscribers = subscribers;
        subscribers = new HashSet<>();

        // Add owners
        subscribers.addAll(
          this.getWorld().getMinecraftServer().getPlayerList().getPlayers()
            .stream()
            .filter(permissions::isSubscriber)
            .collect(Collectors.toList()));

        //  Add nearby players
        if (world != null)
        {
            for (final EntityPlayer o : world.playerEntities)
            {
                if (o instanceof EntityPlayerMP)
                {
                    @NotNull final EntityPlayerMP player = (EntityPlayerMP) o;

                    if (subscribers.contains(player))
                    {
                        //  Already a subscriber
                        continue;
                    }

                    final double distance = player.getDistanceSq(center);
                    if (distance < MathUtils.square(Configurations.workingRangeTownHall + 16D)
                          || (oldSubscribers.contains(player) && distance < MathUtils.square(Configurations.workingRangeTownHall * 2D)))
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
            final boolean hasNewSubscribers = ColonyUtils.hasNewSubscribers(oldSubscribers, subscribers);

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

            //Fields
            if (!isBuildingsDirty)
            {
                sendFieldPackets(oldSubscribers, hasNewSubscribers);
            }
        }

        isFieldsDirty = false;
        isDirty = false;
        isCitizensDirty = false;
        isBuildingsDirty = false;
        permissions.clearDirty();

        buildings.values().forEach(AbstractBuilding::clearDirty);
        citizens.values().forEach(CitizenData::clearDirty);
    }

    private void sendColonyViewPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (isDirty || hasNewSubscribers)
        {
            for (final EntityPlayerMP player : subscribers)
            {
                final boolean isNewSubscriber = !oldSubscribers.contains(player);
                if (isDirty || isNewSubscriber)
                {
                    MineColonies.getNetwork().sendTo(new ColonyViewMessage(this, isNewSubscriber), player);
                }
            }
        }
    }

    /**
     * Sends packages to update the permissions.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendPermissionsPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (permissions.isDirty() || hasNewSubscribers)
        {
            subscribers
              .stream()
              .filter(player -> permissions.isDirty() || !oldSubscribers.contains(player)).forEach(player ->
            {
                final Permissions.Rank rank = getPermissions().getRank(player);
                MineColonies.getNetwork().sendTo(new PermissionsMessage.View(this, rank), player);
            });
        }
    }

    /**
     * Sends packages to update the workOrders.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendWorkOrderPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (getWorkManager().isDirty() || hasNewSubscribers)
        {
            for (final AbstractWorkOrder workOrder : getWorkManager().getWorkOrders().values())
            {
                subscribers.stream().filter(player -> workManager.isDirty() || !oldSubscribers.contains(player))
                  .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewWorkOrderMessage(this, workOrder), player));
            }

            getWorkManager().setDirty(false);
        }
    }

    /**
     * Sends packages to update the citizens.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendCitizenPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (isCitizensDirty || hasNewSubscribers)
        {
            for (@NotNull final CitizenData citizen : citizens.values())
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
     * Sends packages to update the buildings.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendBuildingPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
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
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendFieldPackets(final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if ((isFieldsDirty && !isBuildingsDirty) || hasNewSubscribers)
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
     * Get the Work Manager for the Colony.
     *
     * @return WorkManager for the Colony.
     */
    @NotNull
    public WorkManager getWorkManager()
    {
        return workManager;
    }

    /**
     * Any per-world-tick logic should be performed here.
     * NOTE: If the Colony's world isn't loaded, it won't have a world tick.
     * Use onServerTick for logic that should _always_ run.
     *
     * @param event {@link TickEvent.WorldTickEvent}
     */
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
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
              .filter(ColonyUtils::isCitizenMissingFromWorld)
              .forEach(CitizenData::clearCitizenEntity);

            // TODO evaluate if this block is needed anymore
            //  Cleanup disappeared citizens
            //  It would be really nice if we didn't have to do this... but Citizens can disappear without dying!
            //  Every CITIZEN_CLEANUP_TICK_INCREMENT, cleanup any 'lost' citizens
            if ((event.world.getWorldTime() % CITIZEN_CLEANUP_TICK_INCREMENT) == 0 && areAllColonyChunksLoaded(event) && townHall != null)
            {
                //  All chunks within a good range of the colony should be loaded, so all citizens should be loaded
                //  If we don't have any references to them, destroy the citizen
                citizens.values().forEach(CitizenData::getCitizenEntity);
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
        for (@NotNull final AbstractBuilding building : buildings.values())
        {
            building.onWorldTick(event);
        }

        final Random rand = new Random();
        if (rand.nextInt(CHECK_WAYPOINT_EVERY) <= 1 && wayPoints.size() > 0)
        {
            final Object[] entries = wayPoints.entrySet().toArray();
            final int stopAt = rand.nextInt(entries.length);
            final Object obj = entries[stopAt];

            if (obj instanceof Map.Entry && ((Map.Entry) obj).getKey() instanceof BlockPos && ((Map.Entry) obj).getValue() instanceof IBlockState)
            {
                @NotNull final BlockPos key = (BlockPos) ((Map.Entry) obj).getKey();
                @NotNull final IBlockState value = (IBlockState) ((Map.Entry) obj).getValue();
                if (world != null && world.getBlockState(key).getBlock() != (value.getBlock()))
                {
                    wayPoints.remove(key);
                }
            }
        }

        workManager.onWorldTick(event);
    }

    private boolean areAllColonyChunksLoaded(@NotNull final TickEvent.WorldTickEvent event)
    {
        final int distanceFromCenter = Configurations.workingRangeTownHall + 48 /* 3 chunks */ + 15 /* round up a chunk */;
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

    private void cleanUpBuildings(@NotNull final TickEvent.WorldTickEvent event)
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

        removedBuildings.forEach(AbstractBuilding::destroy);

        @NotNull final ArrayList<Field> tempFields = new ArrayList<>(fields.values());

        for (@NotNull final Field field : tempFields)
        {
            if (event.world.isBlockLoaded(field.getLocation()))
            {
                final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) event.world.getTileEntity(field.getID());
                if (scarecrow == null)
                {
                    fields.remove(field.getID());
                }
                else
                {
                    field.setInventoryField(scarecrow.getInventoryField());
                }
            }
        }

        markFieldsDirty();
    }

    /**
     * Spawn a brand new Citizen.
     */
    private void spawnCitizen()
    {
        spawnCitizen(null);
    }

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data Data to use to spawn citizen.
     */
    public void spawnCitizen(final CitizenData data)
    {
        final BlockPos townHallLocation = townHall.getLocation();
        if (!world.isBlockLoaded(townHallLocation))
        {
            //  Chunk with TownHall Block is not loaded
            return;
        }

        final BlockPos spawnPoint = EntityUtils.getSpawnPoint(world, townHallLocation);

        if (spawnPoint != null)
        {
            final EntityCitizen entity = new EntityCitizen(world);

            CitizenData citizenData = data;
            if (citizenData == null)
            {
                //This ensures that citizen IDs are getting reused.
                //That's needed to prevent bugs when calling IDs that are not used.
                for (int i = 1; i <= this.getMaxCitizens(); i++)
                {
                    if (this.getCitizen(i) == null)
                    {
                        topCitizenId = i;
                        break;
                    }
                }

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

            entity.setPosition(spawnPoint.getX() + 0.5D, spawnPoint.getY() + 0.1D, spawnPoint.getZ() + 0.5D);
            world.spawnEntity(entity);

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

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_SETTLEMENT)
        {
            this.triggerAchievement(ModAchievements.achievementSizeSettlement);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_TOWN)
        {
            this.triggerAchievement(ModAchievements.achievementSizeTown);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_CITY)
        {
            this.triggerAchievement(ModAchievements.achievementSizeCity);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_METROPOLIS)
        {
            this.triggerAchievement(ModAchievements.achievementSizeMetropolis);
        }
    }

    /**
     * Gets the town hall of the colony.
     *
     * @return Town hall of the colony.
     */
    @Nullable
    public BuildingTownHall getTownHall()
    {
        return townHall;
    }

    /**
     * Getter of a unmodifiable version of the farmerFields map.
     *
     * @return map of fields and their id.
     */
    @NotNull
    public Map<BlockPos, Field> getFields()
    {
        return Collections.unmodifiableMap(fields);
    }

    /**
     * Get field in Colony by ID.
     *
     * @param fieldId ID (coordinates) of the field to get.
     * @return field belonging to the given ID.
     */
    public Field getField(final BlockPos fieldId)
    {
        return fields.get(fieldId);
    }

    /**
     * Returns a field which has not been taken yet.
     *
     * @param owner name of the owner of the field.
     * @return a field if there is one available, else null.
     */
    @Nullable
    public Field getFreeField(final String owner)
    {
        for (@NotNull final Field field : fields.values())
        {
            if (!field.isTaken())
            {
                field.setTaken(true);
                field.setOwner(owner);
                markFieldsDirty();
                return field;
            }
        }
        return null;
    }

    /**
     * Updates all subscribers of fields etc.
     */
    private void markFieldsDirty()
    {
        isFieldsDirty = true;
    }

    /**
     * Get building in Colony by ID.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @return AbstractBuilding belonging to the given ID.
     */
    public AbstractBuilding getBuilding(final BlockPos buildingId)
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
    @Nullable
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

    /**
     * Creates a field from a tile entity and adds it to the colony.
     *
     * @param tileEntity      the scarecrow which contains the inventory.
     * @param inventoryPlayer the inventory of the player.
     * @param pos             Position where the field has been placed.
     * @param world           the world of the field.
     */
    public void addNewField(final ScarecrowTileEntity tileEntity, final InventoryPlayer inventoryPlayer, final BlockPos pos, final World world)
    {
        @NotNull final Field field = new Field(tileEntity, inventoryPlayer, world, pos);
        //field.setCustomName(LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user.noone")));
        addField(field);
        field.calculateSize(world, pos);
        markFieldsDirty();
    }

    /**
     * Creates a building from a tile entity and adds it to the colony.
     *
     * @param tileEntity Tile entity to build a building from.
     * @return AbstractBuilding that was created and added.
     */
    @Nullable
    public AbstractBuilding addNewBuilding(@NotNull final TileEntityColonyBuilding tileEntity)
    {
        tileEntity.setColony(this);

        @Nullable final AbstractBuilding building = AbstractBuilding.create(this, tileEntity);
        if (building != null)
        {
            addBuilding(building);
            tileEntity.setBuilding(building);

            Log.getLogger().info(String.format("Colony %d - new AbstractBuilding for %s at %s",
              getID(),
              tileEntity.getBlockType().getClass(),
              tileEntity.getPosition()));
        }
        else
        {
            Log.getLogger().error(String.format("Colony %d unable to create AbstractBuilding for %s at %s",
              getID(),
              tileEntity.getBlockType().getClass(),
              tileEntity.getPosition()));
        }

        calculateMaxCitizens();
        ColonyManager.markDirty();

        return building;
    }

    /**
     * Recalculates how many citizen can be in the colony.
     */
    public void calculateMaxCitizens()
    {
        int newMaxCitizens = 0;

        for (final AbstractBuilding b : buildings.values())
        {
            if (b instanceof BuildingHome && b.getBuildingLevel() > 0)
            {
                newMaxCitizens += ((BuildingHome) b).getMaxInhabitants();
            }
        }
        // Have at least the minimum amount of citizens
        newMaxCitizens = Math.max(Configurations.maxCitizens, newMaxCitizens);
        if (maxCitizens != newMaxCitizens)
        {
            maxCitizens = newMaxCitizens;
            markDirty();
        }
    }

    /**
     * Remove a AbstractBuilding from the Colony (when it is destroyed).
     *
     * @param building AbstractBuilding to remove.
     */
    public void removeBuilding(@NotNull final AbstractBuilding building)
    {
        if (buildings.remove(building.getID()) != null)
        {
            for (final EntityPlayerMP player : subscribers)
            {
                MineColonies.getNetwork().sendTo(new ColonyViewRemoveBuildingMessage(this, building.getID()), player);
            }

            Log.getLogger().info(String.format("Colony %d - removed AbstractBuilding %s of type %s",
              getID(),
              building.getID(),
              building.getSchematicName()));
        }

        if (building instanceof BuildingTownHall)
        {
            townHall = null;
        }

        //Allow Citizens to fix up any data that wasn't fixed up by the AbstractBuilding's own onDestroyed
        for (@NotNull final CitizenData citizen : citizens.values())
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
    public void setManualHiring(final boolean manualHiring)
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
     * Returns a map of citizens in the colony.
     * The map has ID as key, and citizen data as value.
     *
     * @return Map of citizens in the colony, with as key the citizen ID, and as value the citizen data.
     */
    @NotNull
    public Map<Integer, CitizenData> getCitizens()
    {
        return Collections.unmodifiableMap(citizens);
    }

    /**
     * Removes a citizen from the colony.
     *
     * @param citizen Citizen data to remove.
     */
    public void removeCitizen(@NotNull final CitizenData citizen)
    {
        //Remove the Citizen
        citizens.remove(citizen.getId());

        for (@NotNull final AbstractBuilding building : buildings.values())
        {
            building.removeCitizen(citizen);
        }

        workManager.clearWorkForCitizen(citizen);

        //  Inform Subscribers of removed citizen
        for (final EntityPlayerMP player : subscribers)
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveCitizenMessage(this, citizen.getId()), player);
        }
    }

    /**
     * Send the message of a removed workOrder to the client.
     *
     * @param orderId the workOrder to remove.
     */
    public void removeWorkOrder(final int orderId)
    {
        //  Inform Subscribers of removed workOrder
        for (final EntityPlayerMP player : subscribers)
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
    public CitizenData getCitizen(final int citizenId)
    {
        return citizens.get(citizenId);
    }

    /**
     * Get the first unemployed citizen.
     *
     * @return Citizen with no current job.
     */
    @Nullable
    public CitizenData getJoblessCitizen()
    {
        for (@NotNull final CitizenData citizen : citizens.values())
        {
            if (citizen.getWorkBuilding() == null)
            {
                return citizen;
            }
        }

        return null;
    }

    public List<BlockPos> getDeliverymanRequired()
    {

        return citizens.values().stream()
                 .filter(citizen -> citizen.getWorkBuilding() != null && citizen.getJob() != null)
                 .filter(citizen -> !citizen.getJob().isMissingNeededItem())
                 .map(citizen -> citizen.getWorkBuilding().getLocation())
                 .collect(Collectors.toList());
    }

    @NotNull
    public MaterialSystem getMaterialSystem()
    {
        return materialSystem;
    }

    /**
     * Performed when a building of this colony finished his upgrade state.
     *
     * @param building The upgraded building.
     * @param level    The new level.
     */
    public void onBuildingUpgradeComplete(@NotNull final AbstractBuilding building, final int level)
    {
        building.onUpgradeComplete(level);
    }

    @NotNull
    public List<EntityPlayer> getMessageEntityPlayers()
    {
        return ServerUtils.getPlayersFromUUID(this.world, this.getPermissions().getMessagePlayers());
    }

    @NotNull
    public List<Achievement> getAchievements()
    {
        return Collections.unmodifiableList(this.colonyAchievements);
    }

    /**
     * Removes a field from the farmerFields list.
     *
     * @param pos the position-id.
     */
    public void removeField(final BlockPos pos)
    {
        this.markFieldsDirty();
        fields.remove(pos);
    }

    /**
     * Adds a waypoint to the colony.
     *
     * @param point the waypoint to add.
     * @param block the block at the waypoint.
     */
    public void addWayPoint(final BlockPos point, IBlockState block)
    {
        wayPoints.put(point, block);
    }

    /**
     * Returns a list of all wayPoints of the colony.
     *
     * @param position start position.
     * @param target   end position.
     * @return list of wayPoints.
     */
    @NotNull
    public List<BlockPos> getWayPoints(@NotNull final BlockPos position, @NotNull final BlockPos target)
    {
        final List<BlockPos> tempWayPoints = new ArrayList<>();
        tempWayPoints.addAll(wayPoints.keySet());
        tempWayPoints.addAll(getBuildings().keySet());

        final double maxX = Math.max(position.getX(), target.getX());
        final double maxZ = Math.max(position.getZ(), target.getZ());

        final double minX = Math.min(position.getX(), target.getX());
        final double minZ = Math.min(position.getZ(), target.getZ());

        final List<BlockPos> wayPointsCopy = new ArrayList<>(tempWayPoints);
        for (final BlockPos p : wayPointsCopy)
        {
            final int x = p.getX();
            final int z = p.getZ();
            if (x < minX || x > maxX || z < minZ || z > maxZ)
            {
                tempWayPoints.remove(p);
            }
        }

        return tempWayPoints;
    }

    /**
     * Returns a map with all buildings within the colony.
     * Key is ID (Coordinates), value is building object.
     *
     * @return Map with ID (coordinates) as key, and buildings as value.
     */
    @NotNull
    public Map<BlockPos, AbstractBuilding> getBuildings()
    {
        return Collections.unmodifiableMap(buildings);
    }
}
