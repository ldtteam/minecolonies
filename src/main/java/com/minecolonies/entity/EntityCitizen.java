package com.minecolonies.entity;

import com.minecolonies.MineColonies;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.*;
import com.minecolonies.entity.pathfinding.PathNavigate;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BlockParticleEffectMessage;
import com.minecolonies.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.lang.reflect.Field;
import java.util.*;

public class EntityCitizen extends EntityAgeable implements IInvBasic, INpc
{
    // Because Entity UniqueIDs are not identical between client and server
    private static final int    DATA_TEXTURE         = 13;
    private static final int    DATA_LEVEL           = 14;
    private static final int    DATA_IS_FEMALE       = 20;
    private static final int    DATA_COLONY_ID       = 16;
    private static final int    DATA_CITIZEN_ID      = 17;
    private static final int    DATA_MODEL           = 18;
    private static final int    DATA_RENDER_METADATA = 19;
    /**
     * The number to calculate the experienceLevel of the citizen
     */
    private static final int EXPERIENCE_MULTIPLIER = 100;
    /**
     * Number of ticks to heal the citizens
     */
    private static final int HEAL_CITIZENS_AFTER = 200;
    /**
     * Tag's to save data to NBT
     */
    private static final String TAG_COLONY_ID        = "colony";
    private static final String TAG_CITIZEN          = "citizen";
    private static final String TAG_HELD_ITEM_SLOT   = "HeldItemSlot";
    private static final String TAG_STATUS           = "status";

    private RenderBipedCitizen.Model modelId = RenderBipedCitizen.Model.SETTLER;
    private String                   renderMetadata;
    private ResourceLocation         texture;

    protected   Status           status = Status.IDLE;
    private     InventoryCitizen inventory;

    private int         colonyId;
    private int         citizenId = 0;
    private int         level;
    private int         textureId;

    /**
     * Skill modifier defines how fast a citizen levels in a certain skill
     */
    private int         skillModifier = 0;
    private boolean     isFemale;

    private Colony      colony;
    private CitizenData citizenData;

    private Map<String, Integer> statusMessages = new HashMap<>();
    private PathNavigate newNavigator;
    private static       Field      navigatorField;

    /**
     * Citizen constructor.
     *
     * @param world the world the citizen lives in.
     */
    public EntityCitizen(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        this.enablePersistence();//Set persistenceRequired = true;
        this.setAlwaysRenderNameTag(Configurations.alwaysRenderNameTag);
        this.inventory = new InventoryCitizen("Minecolonies Inventory", false, 27);
        this.inventory.addIInvBasic(this);

        this.renderDistanceWeight = 2.0D;
        this.newNavigator = new PathNavigate(this, world);

        if (navigatorField == null)
        {
            Field[] fields = EntityLiving.class.getDeclaredFields();
            for (Field field : fields)
            {
                if (field.getType().equals(net.minecraft.pathfinding.PathNavigate.class))
                {
                    field.setAccessible(true);
                    navigatorField = field;
                    break;
                }
            }
        }

        try
        {
            navigatorField.set(this, this.newNavigator);
        }
        catch (IllegalAccessException e)
        {
            Log.logger.error("Navigator error", e);
        }

        this.getNavigator().setCanSwim(true);
        this.getNavigator().setEnterDoors(true);

        initTasks();
    }

    /**
     * Initiates basic citizen tasks.
     */
    private void initTasks()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAICitizenAvoidEntity(this, EntityMob.class, 8.0F, 0.6D, 1.6D));
        this.tasks.addTask(2, new EntityAIGoHome(this));
        this.tasks.addTask(3, new EntityAISleep(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(4, new EntityAIOpenFenceGate(this, true));
        this.tasks.addTask(5, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest2(this, EntityCitizen.class, 5.0F, 0.02F));
        this.tasks.addTask(7, new EntityAICitizenWander(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));

        onJobChanged(getColonyJob());
    }

    public void onJobChanged(Job job)
    {
        //  Model
        if (job != null)
        {
            modelId = job.getModel();
        }
        else
        {
            switch (getLevel())
            {
                default:
                    modelId = RenderBipedCitizen.Model.SETTLER;
                    break;
                case 1:
                    modelId = RenderBipedCitizen.Model.CITIZEN;
                    break;
                case 2:
                    modelId = RenderBipedCitizen.Model.NOBLE;
                    break;
                case 3:
                    modelId = RenderBipedCitizen.Model.ARISTOCRAT;
                    break;
            }
        }

        dataWatcher.updateObject(DATA_MODEL, modelId.name());
        setRenderMetadata("");


        //  AI Tasks
        Object currentTasks[] = this.tasks.taskEntries.toArray();
        for (Object task : currentTasks)
        {
            if (((EntityAITasks.EntityAITaskEntry) task).action instanceof AbstractEntityAIWork)
            {
                this.tasks.removeTask(((EntityAITasks.EntityAITaskEntry) task).action);
            }
        }

        if (job != null)
        {
            job.addTasks(this.tasks);
            if (ticksExisted > 0)
            {
                BlockPosUtil.tryMoveLivingToXYZ(this, getWorkBuilding().getLocation());
            }
        }
    }

    public void setRenderMetadata(String metadata)
    {
        renderMetadata = metadata;
        dataWatcher.updateObject(DATA_RENDER_METADATA, renderMetadata);
        //Display some debug info always available while testing
        //tofo: remove this when in Beta!
        //Will help track down some hard to find bugs (Pathfinding etc.)
        if (citizenData != null)
        {
            if (this.getColonyJob() != null && Configurations.enableInDevelopmentFeatures)
            {
                setCustomNameTag(citizenData.getName() + " (" + getStatus() + ")[" + this.getColonyJob()
                                                                                         .getNameTagDescription() + "]");
            }
            else
            {
                setCustomNameTag(citizenData.getName());
            }
        }
    }

    private Job getColonyJob()
    {
        return citizenData != null ? citizenData.getJob() : null;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public int getLevel()
    {
        return level;
    }

    public BuildingWorker getWorkBuilding()
    {
        return (citizenData != null) ? citizenData.getWorkBuilding() : null;
    }

    @Override
    public PathNavigate getNavigator()
    {
        return newNavigator;
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public boolean isWorkerAtSiteWithMove(BlockPos site, int range)
    {
        return EntityUtils.isWorkerAtSiteWithMove(this, site.getX(), site.getY(), site.getZ(), range)
               //Fix for getting stuck sometimes
               || EntityUtils.isWorkerAtSite(this, site.getX(), site.getY(), site.getZ(), range + 1);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(DATA_COLONY_ID, colonyId);
        dataWatcher.addObject(DATA_CITIZEN_ID, citizenId);
        dataWatcher.addObject(DATA_TEXTURE, 0);
        dataWatcher.addObject(DATA_LEVEL, 0);
        dataWatcher.addObject(DATA_IS_FEMALE, 0);
        dataWatcher.addObject(DATA_MODEL, RenderBipedCitizen.Model.SETTLER.name());
        dataWatcher.addObject(DATA_RENDER_METADATA, "");
    }

    public <JOB extends Job> JOB getColonyJob(Class<JOB> type)
    {
        return citizenData != null ? citizenData.getJob(type) : null;
    }

    /**
     * Change the citizens Rotation to look at said block
     *
     * @param block the block he should look at
     */
    public void faceBlock(BlockPos block)
    {
        double xDifference = block.getX() - this.posX;
        double zDifference = block.getZ() - this.posZ;
        double yDifference = block.getY() - (this.posY + (double) this.getEyeHeight());

        double squareDifference      = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        double intendedRotationYaw   = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        double intendedRotationPitch = (-(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI));
        this.setRotation((float)updateRotation(this.rotationYaw, intendedRotationYaw, 30),(float)updateRotation(this.rotationPitch, intendedRotationPitch, 30));
    }

    /**
     * Returns the new rotation degree calculated from the current and intended rotation up to a max.
     *
     * @param currentRotation  the current rotation the citizen has
     * @param intendedRotation the wanted rotation he should have after applying this
     * @param maxIncrement     the 'movement speed'
     * @return a rotation value he should move
     */
    private double updateRotation(double currentRotation, double intendedRotation, double maxIncrement)
    {
        double wrappedAngle = MathHelper.wrapAngleTo180_double(intendedRotation - currentRotation);

        if (wrappedAngle > maxIncrement)
        {
            wrappedAngle = maxIncrement;
        }

        if (wrappedAngle < -maxIncrement)
        {
            wrappedAngle = -maxIncrement;
        }

        return currentRotation + wrappedAngle;
    }

    /**
     * Collect exp orbs around the entity
     */
    public void gatherXp()
    {
        for (EntityXPOrb orb : getXPOrbsOnGrid())
        {
            addExperience(orb.getXpValue());
            orb.setDead();
        }
    }

    /**
     * Add experience points to citizen.
     * Increases the citizen level if he has sufficient experience.
     * This will reset the experience.
     *
     * @param xp the amount of points added
     */
    public void addExperience(double xp)
    {
        double j = Integer.MAX_VALUE - citizenData.getExperience();
        xp=xp*skillModifier;

        double localXp = xp;
        if (localXp > j)
        {
            localXp = j;
        }
        citizenData.addExperience(localXp);

        while (EXPERIENCE_MULTIPLIER * (citizenData.getLevel()+1) * (citizenData.getLevel()+1) < citizenData.getExperience())
        {
            citizenData.increaseLevel();
        }

        //todo maybe cap the level.

        citizenData.markDirty();
    }

    /**
     * Defines the area in which the citizen automatically gathers experience
     *
     * @return a list of xp orbs around the entity
     */
    private List<EntityXPOrb> getXPOrbsOnGrid()
    {
        //todo should be fromBounds.
        AxisAlignedBB bb = AxisAlignedBB.fromBounds(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2);
        List<EntityXPOrb> retList = new ArrayList<>();
        //I know streams look better but they are flawed in type erasure
        for (Object o : worldObj.getEntitiesWithinAABB(EntityXPOrb.class, bb)){
            if(o instanceof EntityXPOrb){
                retList.add((EntityXPOrb) o);
            }
        }
        return retList;
    }

    /**
     * Returns false if the newer Entity AI code should be run
     */
    @Override
    public boolean isAIDisabled()
    {
        return false;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate()
    {
        if (recentlyHit > 0)
        {
            citizenData.markDirty();
        }
        if (worldObj.isRemote)
        {
            updateColonyClient();
        }
        else
        {
            pickupItems();
            cleanupChatMessages();
            updateColonyServer();
        }

        checkHeal();
        super.onLivingUpdate();
    }

    /**
     * Checks the citizens health status and heals the citizen if necessary
     */
    private void checkHeal()
    {
        if(citizenData!=null)
        {
            if (getOffsetTicks() % HEAL_CITIZENS_AFTER == 0 && getHealth() < getMaxHealth())
            {
                heal(1);
                citizenData.markDirty();
            }
        }
    }

    private void updateColonyClient()
    {
        if (dataWatcher.hasObjectChanged())
        {
            if (colonyId == 0)
            {
                colonyId = dataWatcher.getWatchableObjectInt(DATA_COLONY_ID);
            }

            if (citizenId == 0)
            {
                citizenId = dataWatcher.getWatchableObjectInt(DATA_CITIZEN_ID);
            }

            isFemale = dataWatcher.getWatchableObjectInt(DATA_IS_FEMALE) != 0;
            level = dataWatcher.getWatchableObjectInt(DATA_LEVEL);
            modelId = RenderBipedCitizen.Model.valueOf(dataWatcher.getWatchableObjectString(DATA_MODEL));
            textureId = dataWatcher.getWatchableObjectInt(DATA_TEXTURE);
            renderMetadata = dataWatcher.getWatchableObjectString(DATA_RENDER_METADATA);

            setTexture();

            dataWatcher.func_111144_e(); // clear hasChanges
        }

        updateArmSwingProgress();
    }

    /**
     * Sets the textures of all citizens and distinguishes between male and female
     */
    private void setTexture()
    {
        if (!worldObj.isRemote)
        {
            return;
        }

        RenderBipedCitizen.Model model = getModelID();

        String textureBase = "textures/entity/";
        textureBase += model.textureBase;
        textureBase += isFemale ? "Female" : "Male";

        int moddedTextureId = (textureId % model.numTextures) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + renderMetadata + ".png");
    }

    public RenderBipedCitizen.Model getModelID()
    {
        return modelId;
    }

    /**
     * Server-specific update for the EntityCitizen
     */
    public void updateColonyServer()
    {
        if (colonyId == 0)
        {
            setDead();
            return;
        }

        if (colony == null)
        {
            Colony c = ColonyManager.getColony(colonyId);

            if (c == null)
            {
                Log.logger.warn(String.format("EntityCitizen '%s' unable to find Colony #%d", getUniqueID(), colonyId));
                setDead();
                return;
            }

            CitizenData data = c.getCitizen(citizenId);
            if (data == null)
            {
                //  Citizen does not exist in the Colony
                Log.logger.warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen %d, but not known to colony",
                                              getUniqueID(),
                                              colonyId,
                                              citizenId));
                setDead();
                return;
            }

            EntityCitizen existingCitizen = data.getCitizenEntity();
            if (existingCitizen != null && existingCitizen != this)
            {
                // This Citizen already has a different Entity registered to it
                Log.logger.warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen #%d, but already have a citizen ('%s')",
                                              getUniqueID(),
                                              colonyId,
                                              citizenId,
                                              existingCitizen.getUniqueID()));
                if (!existingCitizen.getUniqueID().equals(this.getUniqueID()))
                {
                    setDead();
                }
                else
                {
                    data.setCitizenEntity(this);
                }
                return;
            }

            setColony(c, data);
        }
    }

    public void setColony(Colony c, CitizenData data)
    {
        if (c == null)
        {
            colony = null;
            colonyId = 0;
            citizenId = 0;
            citizenData = null;
            setDead();
            return;
        }

        colony = c;
        colonyId = colony.getID();
        citizenId = data.getId();
        citizenData = data;

        setCustomNameTag(citizenData.getName());

        isFemale = citizenData.isFemale();
        textureId = citizenData.getTextureId();

        dataWatcher.updateObject(DATA_COLONY_ID, colonyId);
        dataWatcher.updateObject(DATA_CITIZEN_ID, citizenId);
        dataWatcher.updateObject(DATA_IS_FEMALE, isFemale ? 1 : 0);
        dataWatcher.updateObject(DATA_TEXTURE, textureId);
        updateLevel();

        citizenData.setCitizenEntity(this);

        onJobChanged(getColonyJob());

        inventory.createMaterialStore(c.getMaterialSystem());
    }

    private void updateLevel()
    {
        level = citizenData != null ? citizenData.getLevel() : 0;
        dataWatcher.updateObject(DATA_LEVEL, level);
    }

    private void cleanupChatMessages()
    {
        if (statusMessages.size() > 0 && ticksExisted % 20 == 0)//Only check if there are messages and once a second
        {
            Iterator<Map.Entry<String, Integer>> it = statusMessages.entrySet().iterator();
            while (it.hasNext())
            {
                if (ticksExisted - it.next().getValue() > 20 * Configurations.chatFrequency)
                {
                    it.remove();
                }
            }
        }
    }

    /**
     * Pick up all items in a range around the citizen.
     */
    private void pickupItems()
    {
        List<EntityItem> retList = new ArrayList<>();
        //I know streams look better but they are flawed in type erasure
        for (Object o : worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(2.0F, 0.0F, 2.0F)))
        {
            if(o instanceof EntityItem)
            {
                retList.add((EntityItem) o);
            }
        }

        retList.stream()
               .filter(item -> item != null)
               .filter(item -> !item.isDead)
               .filter(item -> canPickUpLoot())
               .forEach(this::tryPickupEntityItem);
    }

    /**
     * Entities treat being on ladders as not on ground; this breaks navigation logic
     */
    @Override
    protected void updateFallState(final double y, final boolean onGroundIn, final Block blockIn, final BlockPos pos)
    {
        if (!onGround)
        {
            int px = MathHelper.floor_double(posX);
            int py = (int) posY;
            int pz = MathHelper.floor_double(posZ);

            this.onGround = worldObj.getBlockState(new BlockPos(px, py, pz)).getBlock().isLadder(worldObj, new BlockPos(px, py, pz), this);
        }

        super.updateFallState(y, onGroundIn, blockIn, pos);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        //TODO ???
        return null;
    }

    /**
     * Applies attributes like health, charisma etc to the citizens.
     */
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(100);//path finding search range
    }


    /**
     * Called when a player tries to interact with a citizen.
     * @param player which interacts with the citizen
     * @return If citizen should interact or not.
     */
    @Override
    public boolean interact(EntityPlayer player)
    {
        if (worldObj.isRemote)
        {
            CitizenData.View view = getCitizenDataView();
            if (view != null)
            {
                MineColonies.proxy.showCitizenWindow(view);
            }
        }
        return true;
    }

    private CitizenData.View getCitizenDataView()
    {
        if (colonyId != 0 && citizenId != 0)
        {
            ColonyView colonyView = ColonyManager.getColonyView(colonyId);
            if (colonyView != null)
            {
                return colonyView.getCitizen(citizenId);
            }
        }

        return null;
    }

    /**
     * Drop some experience share depending on the experience and experienceLevel.
     */
    private void dropExperience()
    {
        int experience;

        if (!this.worldObj.isRemote && (this.recentlyHit > 0 || this.isPlayer()) && this.canDropLoot() && this.worldObj.getGameRules().getBoolean("doMobLoot"))
        {
            experience = (int)(citizenData.getLevel()*100 + this.getExperiencePoints());

            while (experience > 0)
            {
                int j = EntityXPOrb.getXPSplit(experience);
                experience -= j;
                this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, j));
            }
        }

        //Spawn particle explosion of xp orbs on death
        for (int i = 0; i < 20; ++i)
        {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
                                        this.posX + (this.rand.nextDouble() * this.width * 2.0F) - (double) this.width,
                                        this.posY + (this.rand.nextDouble() * this.height),
                                        this.posZ + (this.rand.nextDouble() * this.width * 2.0F) - (double) this.width,
                                        d2,
                                        d0,
                                        d1);
        }
    }
    /**
     * Called when the mob's health reaches 0.
     * @param par1DamageSource the attacking entity
     */
    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        dropExperience();
        this.setDead();

        if (colony != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(EntityUtils.getPlayersFromUUID(worldObj, colony.getPermissions().getMessagePlayers()),
                                                        "tile.blockHutTownHall.messageColonistDead",
                                                        citizenData.getName());
            colony.removeCitizen(getCitizenData());
        }
        super.onDeath(par1DamageSource);
    }

    public CitizenData getCitizenData()
    {
        return citizenData;
    }

    public ResourceLocation getTexture()
    {
        return texture;
    }

    public boolean isFemale()
    {
        return isFemale;
    }

    public void clearColony()
    {
        setColony(null, null);
    }

    public boolean isAtHome()
    {
        BlockPos homePosition = getHomePosition();
        return homePosition != null && homePosition.distanceSq((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)) <= 16;
    }

    /**
     * Returns the home position of each citizen (His house or town hall)
     * @return location
     */
    public BlockPos getHomePosition()
    {
        BuildingHome homeBuilding = getHomeBuilding();
        if (homeBuilding != null)
        {
            return homeBuilding.getLocation();
        }
        else if (getColony() != null && getColony().getTownHall() != null)
        {
            return getColony().getTownHall().getLocation();
        }

        return null;
    }

    //TODO minecraft calls this when saving the entity
    //@Override
    //public ItemStack[] getInventory(){
    //    throw new IllegalStateException("DO NOT USE THIS METHOD, DUDE!");
    //}

    public Colony getColony()
    {
        return colony;
    }

    private BuildingHome getHomeBuilding()
    {
        return (citizenData != null) ? citizenData.getHomeBuilding() : null;
    }

    public BlockPos getPosition()
    {
        return new BlockPos(posX,posY,posZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger(TAG_STATUS, status.ordinal());
        if (colony != null && citizenData != null)
        {
            compound.setInteger(TAG_COLONY_ID, colony.getID());
            compound.setInteger(TAG_CITIZEN, citizenData.getId());
        }

        inventory.writeToNBT(compound);
        compound.setInteger(TAG_HELD_ITEM_SLOT, inventory.getHeldItemSlot());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        status = Status.values()[compound.getInteger(TAG_STATUS)];
        colonyId = compound.getInteger(TAG_COLONY_ID);
        citizenId = compound.getInteger(TAG_CITIZEN);

        if(isServerWorld())
        {
            updateColonyServer();
        }
        inventory.readFromNBT(compound);

        inventory.setHeldItem(compound.getInteger(TAG_HELD_ITEM_SLOT));
    }

    public int getOffsetTicks()
    {
        return this.ticksExisted + 7 * this.getEntityId();
    }

    public boolean isInventoryFull()
    {
        return InventoryUtils.isInventoryFull(getInventoryCitizen());
    }

    public InventoryCitizen getInventoryCitizen()
    {
        return inventory;
    }

    public DesiredActivity getDesiredActivity()
    {
        if (!worldObj.isDaytime())
        {
            return DesiredActivity.SLEEP;
        }
        else if (worldObj.isRaining())
        {
            return DesiredActivity.IDLE;
        }
        else
        {
            return DesiredActivity.WORK;
        }
    }

    /**
     * Drop the equipment for this entity.
     */
    @Override
    protected void dropEquipment(boolean par1, int par2)
    {
        //Delete stuff from Inventory Array that isn't really used.
        for (int i = 0; i <  getInventory().length; i++)
        {
            setCurrentItemOrArmor(i, null);
        }

        //Drop actual inventory
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack != null && itemstack.stackSize > 0)
            {
                entityDropItem(itemstack);
            }
        }
    }

    public EntityItem entityDropItem(ItemStack itemstack)
    {
        return entityDropItem(itemstack, 0.0F);
    }

    @Override
    public void onInventoryChanged(InventoryBasic inventoryBasic)
    {
        //TODO use in future for lumberjack rendering logs, etc
        setCurrentItemOrArmor(0, inventory.getHeldItem());
    }

    /**
     * Get the experience points the entity currently has.
     * <p>
     * todo: seems flawed and unused
     *
     * @return the amount of xp this entity has
     */
    public double getExperiencePoints()
    {
        return citizenData.getExperience();
    }

    public int findFirstSlotInInventoryWith(Item targetItem)
    {
        return InventoryUtils.findFirstSlotInInventoryWith(getInventoryCitizen(), targetItem);
    }

    public int findFirstSlotInInventoryWith(Block block)
    {
        return InventoryUtils.findFirstSlotInInventoryWith(getInventoryCitizen(), block);
    }

    public int getItemCountInInventory(Block block)
    {
        return InventoryUtils.getItemCountInInventory(getInventoryCitizen(), block);
    }

    public int getItemCountInInventory(Item targetitem)
    {
        return InventoryUtils.getItemCountInInventory(getInventoryCitizen(), targetitem);
    }

    public boolean hasItemInInventory(Block block)
    {
        return InventoryUtils.hasitemInInventory(getInventoryCitizen(), block);
    }

    public boolean hasItemInInventory(Item item)
    {
        return InventoryUtils.hasitemInInventory(getInventoryCitizen(), item);
    }

    public void setInventorySize(int newSize, boolean dropLeftovers)
    {
        if (!worldObj.isRemote)
        {
            InventoryCitizen     newInventory = new InventoryCitizen(inventory.getName(), inventory.hasCustomName(), newSize);
            ArrayList<ItemStack> leftOvers    = new ArrayList<>();
            for (int i = 0; i < inventory.getSizeInventory(); i++)
            {
                ItemStack itemstack = inventory.getStackInSlot(i);
                if (i < newInventory.getSizeInventory())
                {
                    newInventory.setInventorySlotContents(i, itemstack);
                }
                else
                {
                    if (itemstack != null){ leftOvers.add(itemstack); }
                }
            }
            inventory = newInventory;
            inventory.addIInvBasic(this);
            if (dropLeftovers)
            {
                leftOvers.stream().filter(leftover -> leftover.stackSize > 0).forEach(this::entityDropItem);
            }
        }
    }

    private void tryPickupEntityItem(EntityItem entityItem)
    {
        if (!this.worldObj.isRemote)
        {
            if (entityItem.cannotPickup())
            {
                return;
            }

            ItemStack itemStack = entityItem.getEntityItem();
            int       i         = itemStack.stackSize;

            if (i <= 0 || InventoryUtils.addItemStackToInventory(this.getInventoryCitizen(), itemStack))
            {
                this.worldObj.playSoundAtEntity(this, "random.pop", 0.2f,
                                                (float) (((this.rand.nextDouble() - this.rand.nextDouble()) * 0.7D + 1.0D) * 2.0D));
                this.onItemPickup(this, i);

                if (itemStack.stackSize <= 0)
                {
                    entityItem.setDead();
                }
            }
        }
    }

    public void setHeldItem(int slot)
    {
        inventory.setHeldItem(slot);
        setCurrentItemOrArmor(0, inventory.getStackInSlot(slot));
    }

    public void hitBlockWithToolInHand(BlockPos block)
    {
        if (block == null){ return; }
        hitBlockWithToolInHand(block, false);
    }

    /**
     * Swing entity arm, create sound and particle effects. if breakBlock is true then it will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param pos Block position
     */
    private void hitBlockWithToolInHand(BlockPos pos, boolean breakBlock)
    {
        //todo: this is not optimal but works
        getLookHelper().setLookPosition(pos.getX(), pos.getY(), pos.getZ(), 10f, getVerticalFaceSpeed());

        this.swingItem();

        Block block = worldObj.getBlockState(pos).getBlock();
        if (breakBlock)
        {
            if (!worldObj.isRemote)
            {
                MineColonies.getNetwork().sendToAllAround(
                        new BlockParticleEffectMessage(pos, worldObj.getBlockState(pos), BlockParticleEffectMessage.BREAK_BLOCK),
                        new NetworkRegistry.TargetPoint(worldObj.provider.getDimensionId(), pos.getX(), pos.getY(), pos.getZ(), 16.0D));
            }
            worldObj.playSoundEffect((float) (pos.getX() + 0.5D),
                                     (float) (pos.getY() + 0.5D),
                                     (float) (pos.getZ() + 0.5D),
                                     block.stepSound.getBreakSound(),
                                     block.stepSound.getVolume(),
                                     block.stepSound.getFrequency());
            worldObj.setBlockToAir(pos);

            damageItemInHand(1);
        }
        else
        {
            if (!worldObj.isRemote)//TODO might remove this
            {
                MineColonies.getNetwork().sendToAllAround(
                        new BlockParticleEffectMessage(pos, worldObj.getBlockState(pos), 1),//TODO correct side
                        new NetworkRegistry.TargetPoint(worldObj.provider.getDimensionId(), pos.getX(), pos.getY(), pos.getZ(), 16.0D));
            }
            worldObj.playSoundEffect((float) (pos.getX() + 0.5D), (float) (pos.getY() + 0.5D), (float) (pos.getZ() + 0.5D), block.stepSound.getStepSound(),

                                     (float) ((block.stepSound.getVolume() + 1.0D) / 8.0D),
                                     (float) (block.stepSound.getFrequency() * 0.5D));
        }
    }

    /**
     * Damage the current held item
     * @param damage amount of damage
     */
    public void damageItemInHand(int damage)
    {
        final ItemStack heldItem = getInventoryCitizen().getHeldItem();
        //If we hit with bare hands, ignore
        if(heldItem == null)
        {
            return;
        }
        heldItem.damageItem(damage, this);

        //check if tool breaks
        if (heldItem.stackSize < 1)
        {
            this.setCurrentItemOrArmor(0, null);
            getInventoryCitizen().setInventorySlotContents(getInventoryCitizen().getHeldItemSlot(), null);
        }
    }

    public void hitBlockWithToolInHand(int x, int y, int z) // TODO: why do we need here a boolean and for what is this boolean?
    {
        hitBlockWithToolInHand(new BlockPos(x,y,z), false);
    }

    public void breakBlockWithToolInHand(BlockPos pos)
    {
        if (pos == null){ return; }
        hitBlockWithToolInHand(pos, true);
    }

    public void sendLocalizedChat(String key, Object... args)
    {
        sendChat(LanguageHandler.format(key, args));
    }

    public void sendChat(String msg)
    {
        if (msg == null || msg.length() == 0 || statusMessages.containsKey(msg))
        {
            return;
        }

        statusMessages.put(msg, ticksExisted);

        LanguageHandler.sendPlayersMessage(EntityUtils.getPlayersFromUUID(worldObj, getColony().getPermissions().getMessagePlayers()),
                                           LanguageHandler.format(this.getColonyJob().getName()) + " " + this.getCustomNameTag() + ": " + msg);
    }

    /**
     * Intelligence getter
     * @return citizen intelligence value
     */
    public int getIntelligence()
    {
        return citizenData.getIntelligence();
    }
    /**
     * Charisma getter
     * @return citizen Charisma value
     */
    public int getCharisma()
    {
        return citizenData.getCharisma();
    }
    /**
     * Strength getter
     * @return citizen Strength value
     */
    public int getStrength()
    {
        return citizenData.getStrength();
    }
    /**
     * Endurance getter
     * @return citizen Endurance value
     */
    public int getEndurance()
    {
        return citizenData.getEndurance();
    }
    /**
     * Dexterity getter
     * @return citizen Dexterity value
     */
    public int getDexterity()
    {
        return citizenData.getDexterity();
    }

    /**
     * Set the skill modifier which defines how fast a citizen levels in a certain skill
     * @param modifier input modifier
     */
    public void setSkillModifier(int modifier)
    {
        skillModifier = modifier;
    }

    /**
     * ExperienceLevel getter
     * @return citizen ExperienceLevel value
     */
    public int getExperienceLevel()
    {
        return citizenData.getLevel();
    }

    public enum DesiredActivity
    {
        SLEEP,
        IDLE,
        WORK
    }

    /**
     * Used for chat messages, sounds, and other need based interactions
     * Created: June 20, 2014
     *
     * @author Colton
     */
    public enum Status
    {
        IDLE,
        SLEEPING,
        WORKING,
        GETTING_ITEMS,
        NEED_ASSISTANCE,
        PATHFINDING_ERROR
    }
}
