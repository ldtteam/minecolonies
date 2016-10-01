package com.minecolonies.entity;

import com.minecolonies.MineColonies;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.*;
import com.minecolonies.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.entity.ai.minimal.*;
import com.minecolonies.entity.pathfinding.PathNavigate;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BlockParticleEffectMessage;
import com.minecolonies.sounds.FishermanSounds;
import com.minecolonies.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * The Class used to represent the citizen entities.
 */
public class EntityCitizen extends EntityAgeable implements INpc
{
    // Because Entity UniqueIDs are not identical between client and server
    private static final int DATA_TEXTURE         = 13;
    private static final int DATA_LEVEL           = 14;
    private static final int DATA_IS_FEMALE       = 20;
    private static final int DATA_COLONY_ID       = 16;
    private static final int DATA_CITIZEN_ID      = 17;
    private static final int DATA_MODEL           = 18;
    private static final int DATA_RENDER_METADATA = 19;

    /**
     * The movement speed for the citizen to run away.
     */
    private static final int MOVE_AWAY_SPEED = 2;

    /**
     * The range for the citizen to move away.
     */
    private static final int MOVE_AWAY_RANGE = 6;

    /**
     * Number of ticks to heal the citizens
     */
    private static final int HEAL_CITIZENS_AFTER = 200;

    /**
     * Tag's to save data to NBT
     */
    private static final String TAG_COLONY_ID      = "colony";
    private static final String TAG_CITIZEN        = "citizen";
    private static final String TAG_HELD_ITEM_SLOT = "HeldItemSlot";
    private static final String TAG_STATUS         = "status";

    /**
     * The delta yaw value for looking at things.
     */
    private static final float FACING_DELTA_YAW = 10F;

    /**
     * The range in which we can hear a block break sound.
     */
    private static final double BLOCK_BREAK_SOUND_RANGE = 16.0D;

    /**
     * Modifier to lower the sound of block breaks.
     * <p>
     * Decrease this to make sounds louder.
     */
    private static final double BLOCK_BREAK_SOUND_DAMPER = 8.0D;

    /**
     * The height of half a block.
     */
    private static final double HALF_BLOCK = 0.5D;

    /**
     * Modifier for sound frequency when breaking blocks.
     */
    private static final double SOUND_FREQ_MODIFIER = 0.5D;

    /**
     * The range in which someone will see the particles from a block breaking.
     */
    private static final double BLOCK_BREAK_PARTICLE_RANGE = 16.0D;

    /**
     * Divide experience by a factor to ensure more levels fit in an int.
     */
    private static final int EXP_DIVIDER               = 10;

    /**
     * Chance the citizen will rant about bad weather. 20 ticks per 60 seconds = 5 minutes.
     */
    private static final int RANT_ABOUT_WEATHER_CHANCE = 20*60*5;
    private static Field navigatorField;
    protected Status                   status  = Status.IDLE;
    private   RenderBipedCitizen.Model modelId = RenderBipedCitizen.Model.SETTLER;
    private String           renderMetadata;
    private ResourceLocation texture;
    private InventoryCitizen inventory;
    private int colonyId;
    private int citizenId = 0;
    private int level;
    private int textureId;
    /**
     * Skill modifier defines how fast a citizen levels in a certain skill
     */
    private double skillModifier = 0;
    private boolean     female;
    @Nullable
    private Colony      colony;
    @Nullable
    private CitizenData citizenData;
    @NotNull
    private Map<String, Integer> statusMessages = new HashMap<>();
    private        PathNavigate newNavigator;

    /**
     * Citizen constructor.
     *
     * @param world the world the citizen lives in.
     */
    public EntityCitizen(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        this.enablePersistence();
        this.setAlwaysRenderNameTag(Configurations.alwaysRenderNameTag);
        this.inventory = new InventoryCitizen("Minecolonies Inventory", false, this);

        this.renderDistanceWeight = 2.0D;
        this.newNavigator = new PathNavigate(this, world);

        updateNavigatorField();

        this.newNavigator.setCanSwim(true);
        this.newNavigator.setEnterDoors(true);

        initTasks();
    }

    private synchronized void updateNavigatorField()
    {
        if (navigatorField == null)
        {
            Field[] fields = EntityLiving.class.getDeclaredFields();
            for (@NotNull Field field : fields)
            {
                if (field.getType().equals(net.minecraft.pathfinding.PathNavigate.class))
                {
                    field.setAccessible(true);
                    navigatorField = field;
                    break;
                }
            }
        }

        if (navigatorField == null)
        {
            throw new IllegalStateException("Navigator field should not be null, contact developers.");
        }

        try
        {
            navigatorField.set(this, this.newNavigator);
        }
        catch (IllegalAccessException e)
        {
            Log.getLogger().error("Navigator error", e);
        }
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

    public void onJobChanged(@Nullable AbstractJob job)
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
                case 1:
                    modelId = RenderBipedCitizen.Model.CITIZEN;
                    break;
                case 2:
                    modelId = RenderBipedCitizen.Model.NOBLE;
                    break;
                case 3:
                    modelId = RenderBipedCitizen.Model.ARISTOCRAT;
                    break;
                default:
                    modelId = RenderBipedCitizen.Model.SETTLER;
                    break;
            }
        }

        dataWatcher.updateObject(DATA_MODEL, modelId.name());
        setRenderMetadata("");


        //  AI Tasks
        @NotNull Object currentTasks[] = this.tasks.taskEntries.toArray();
        for (@NotNull Object task : currentTasks)
        {
            if (((EntityAITasks.EntityAITaskEntry) task).action instanceof AbstractEntityAIInteract)
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

    private AbstractJob getColonyJob()
    {
        return citizenData != null ? citizenData.getJob() : null;
    }

    public int getLevel()
    {
        return level;
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

    /**
     * calculate this workers building.
     *
     * @return the building or null if none present.
     */
    @Nullable
    public AbstractBuildingWorker getWorkBuilding()
    {
        return (citizenData != null) ? citizenData.getWorkBuilding() : null;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public boolean isWorkerAtSiteWithMove(@NotNull BlockPos site, int range)
    {
        return EntityUtils.isWorkerAtSiteWithMove(this, site.getX(), site.getY(), site.getZ(), range)
                 //Fix for getting stuck sometimes
                 || EntityUtils.isWorkerAtSite(this, site.getX(), site.getY(), site.getZ(), range + 1);
    }

    @Nullable
    public <J extends AbstractJob> J getColonyJob(@NotNull Class<J> type)
    {
        return citizenData != null ? citizenData.getJob(type) : null;
    }

    /**
     * Change the citizens Rotation to look at said block
     *
     * @param block the block he should look at
     */
    public void faceBlock(@Nullable BlockPos block)
    {
        if (block == null)
        {
            return;
        }

        double xDifference = block.getX() - this.posX;
        double zDifference = block.getZ() - this.posZ;
        double yDifference = block.getY() - (this.posY + (double) this.getEyeHeight());

        double squareDifference = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        double intendedRotationYaw = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        double intendedRotationPitch = -(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI);
        this.setRotation((float) updateRotation(this.rotationYaw, intendedRotationYaw, 30), (float) updateRotation(this.rotationPitch, intendedRotationPitch, 30));
    }

    /**
     * Returns the new rotation degree calculated from the current and intended rotation up to a max.
     *
     * @param currentRotation  the current rotation the citizen has
     * @param intendedRotation the wanted rotation he should have after applying this
     * @param maxIncrement     the 'movement speed'
     * @return a rotation value he should move
     */
    private static double updateRotation(double currentRotation, double intendedRotation, double maxIncrement)
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
        for (@NotNull EntityXPOrb orb : getXPOrbsOnGrid())
        {
            addExperience(orb.getXpValue());
            orb.setDead();
        }
    }

    /**
     * Defines the area in which the citizen automatically gathers experience
     *
     * @return a list of xp orbs around the entity
     */
    private List<EntityXPOrb> getXPOrbsOnGrid()
    {
        @NotNull AxisAlignedBB bb = AxisAlignedBB.fromBounds(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2);

        return worldObj.getEntitiesWithinAABB(EntityXPOrb.class, bb);
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
        double maxValue = Integer.MAX_VALUE - citizenData.getExperience();

        double localXp = xp * skillModifier / EXP_DIVIDER;
        if (localXp > maxValue)
        {
            localXp = maxValue;
        }
        citizenData.addExperience(localXp);

        while (ExperienceUtils.getXPNeededForNextLevel(citizenData.getLevel()) < citizenData.getExperience())
        {
            citizenData.increaseLevel();
        }

        citizenData.markDirty();
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

    /**
     * Called when the mob's health reaches 0.
     *
     * @param par1DamageSource the attacking entity
     */
    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        dropExperience();
        this.setDead();

        if (colony != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(
              colony.getMessageEntityPlayers(),
              "tile.blockHutTownHall.messageColonistDead",
              citizenData.getName());

            colony.removeCitizen(getCitizenData());
        }
        super.onDeath(par1DamageSource);
    }

    /**
     * Drop some experience share depending on the experience and experienceLevel.
     */
    private void dropExperience()
    {
        int experience;

        if (!this.worldObj.isRemote && this.recentlyHit > 0 && this.canDropLoot() && this.worldObj.getGameRules().getBoolean("doMobLoot"))
        {
            experience = (int) (citizenData.getLevel() * 100 + this.getExperiencePoints());

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

    @Nullable
    public CitizenData getCitizenData()
    {
        return citizenData;
    }

    /**
     * Get the experience points the entity currently has.
     * <p>
     *
     * @return the amount of xp this entity has
     */
    public double getExperiencePoints()
    {
        return citizenData.getExperience();
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        //TODO ???
        return null;
    }

    /**
     * Called when a player tries to interact with a citizen.
     *
     * @param player which interacts with the citizen
     * @return If citizen should interact or not.
     */
    @Override
    public boolean interact(EntityPlayer player)
    {
        if (worldObj.isRemote)
        {
            @Nullable CitizenDataView citizenDataView = getCitizenDataView();
            if (citizenDataView != null)
            {
                MineColonies.proxy.showCitizenWindow(citizenDataView);
            }
        }
        return true;
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

        if (isServerWorld())
        {
            updateColonyServer();
        }
        inventory.readFromNBT(compound);

        inventory.setHeldItem(compound.getInteger(TAG_HELD_ITEM_SLOT));
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
            if(worldObj.isDaytime() && !worldObj.isRaining())
            {
                SoundUtils.playRandomSound(worldObj, this);
            }
            else if(worldObj.isRaining() && 1 >=rand.nextInt(RANT_ABOUT_WEATHER_CHANCE))
            {
                //todo add sounds of other workers as well.
                final String badWeather = isFemale() ? FishermanSounds.Female.badWeather : FishermanSounds.Male.badWeather;
                SoundUtils.playSoundAtCitizenWithChance(worldObj, this, badWeather, 1);
            }
        }

        if (isEntityInsideOpaqueBlock() || isInsideOfMaterial(Material.leaves))
        {
            getNavigator().moveAwayFromXYZ(this.getPosition(), MOVE_AWAY_RANGE, MOVE_AWAY_SPEED);
        }

        checkHeal();
        super.onLivingUpdate();
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

            female = dataWatcher.getWatchableObjectInt(DATA_IS_FEMALE) != 0;
            level = dataWatcher.getWatchableObjectInt(DATA_LEVEL);
            modelId = RenderBipedCitizen.Model.valueOf(dataWatcher.getWatchableObjectString(DATA_MODEL));
            textureId = dataWatcher.getWatchableObjectInt(DATA_TEXTURE);
            renderMetadata = dataWatcher.getWatchableObjectString(DATA_RENDER_METADATA);

            setTexture();

            // clear hasChanges
            dataWatcher.func_111144_e();
        }

        updateArmSwingProgress();
    }

    /**
     * Pick up all items in a range around the citizen.
     */
    private void pickupItems()
    {
        @NotNull List<EntityItem> retList = new ArrayList<>();
        //I know streams look better but they are flawed in type erasure
        for (Object o : worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(2.0F, 0.0F, 2.0F)))
        {
            if (o instanceof EntityItem)
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

    private void cleanupChatMessages()
    {
        //Only check if there are messages and once a second
        if (statusMessages.size() > 0 && ticksExisted % 20 == 0)
        {
            @NotNull Iterator<Map.Entry<String, Integer>> it = statusMessages.entrySet().iterator();
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
            handleNullColony();
        }
    }

    /**
     * Checks the citizens health status and heals the citizen if necessary.
     */
    private void checkHeal()
    {
        if (citizenData != null && getOffsetTicks() % HEAL_CITIZENS_AFTER == 0 && getHealth() < getMaxHealth())
        {
            heal(1);
            citizenData.markDirty();
        }
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
        textureBase += female ? "Female" : "Male";

        int moddedTextureId = (textureId % model.numTextures) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + renderMetadata + ".png");
    }

    private void handleNullColony()
    {
        Colony c = ColonyManager.getColony(colonyId);

        if (c == null)
        {
            Log.getLogger().warn(String.format("EntityCitizen '%s' unable to find Colony #%d", getUniqueID(), colonyId));
            setDead();
            return;
        }

        CitizenData data = c.getCitizen(citizenId);
        if (data == null)
        {
            //  Citizen does not exist in the Colony
            Log.getLogger().warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen %d, but not known to colony",
              getUniqueID(),
              colonyId,
              citizenId));
            setDead();
            return;
        }

        @Nullable EntityCitizen existingCitizen = data.getCitizenEntity();
        if (existingCitizen != null && existingCitizen != this)
        {
            // This Citizen already has a different Entity registered to it
            handleExistingCitizen(data, existingCitizen);
            return;
        }

        setColony(c, data);
    }

    public int getOffsetTicks()
    {
        return this.ticksExisted + 7 * this.getEntityId();
    }

    public RenderBipedCitizen.Model getModelID()
    {
        return modelId;
    }

    private void handleExistingCitizen(@NotNull CitizenData data, @NotNull EntityCitizen existingCitizen)
    {
        Log.getLogger().warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen #%d, but already have a citizen ('%s')",
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
    }

    public void setColony(@Nullable Colony c, @NotNull CitizenData data)
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

        female = citizenData.isFemale();
        textureId = citizenData.getTextureId();

        dataWatcher.updateObject(DATA_COLONY_ID, colonyId);
        dataWatcher.updateObject(DATA_CITIZEN_ID, citizenId);
        dataWatcher.updateObject(DATA_IS_FEMALE, female ? 1 : 0);
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

    private CitizenDataView getCitizenDataView()
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
     * Applies attributes like health, charisma etc to the citizens.
     */
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);

        //path finding search range
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(100);
    }

    @Override
    public PathNavigate getNavigator()
    {
        return newNavigator;
    }

    /**
     * Drop the equipment for this entity.
     */
    @Override
    protected void dropEquipment(boolean par1, int par2)
    {
        //Delete stuff from Inventory Array that isn't really used.
        for (int i = 0; i < getInventory().length; i++)
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

    /**
     * Returns false if the newer Entity AI code should be run
     */
    @Override
    public boolean isAIDisabled()
    {
        return false;
    }

    //TODO minecraft calls this when saving the entity
    //@Override
    //public ItemStack[] getInventory(){
    //    throw new IllegalStateException("DO NOT USE THIS METHOD, DUDE!");
    //}

    public EntityItem entityDropItem(@NotNull ItemStack itemstack)
    {
        return entityDropItem(itemstack, 0.0F);
    }

    public ResourceLocation getTexture()
    {
        return texture;
    }

    public boolean isFemale()
    {
        return female;
    }

    public void clearColony()
    {
        setColony(null, null);
    }

    public boolean isAtHome()
    {
        @Nullable BlockPos homePosition = getHomePosition();
        return homePosition != null && homePosition.distanceSq((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)) <= 16;
    }

    /**
     * Returns the home position of each citizen (His house or town hall)
     *
     * @return location
     */
    @Nullable
    @Override
    public BlockPos getHomePosition()
    {
        @Nullable BuildingHome homeBuilding = getHomeBuilding();
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

    private BuildingHome getHomeBuilding()
    {
        return (citizenData != null) ? citizenData.getHomeBuilding() : null;
    }

    @Nullable
    public Colony getColony()
    {
        return colony;
    }

    public boolean isInventoryFull()
    {
        return InventoryUtils.isInventoryFull(getInventoryCitizen());
    }

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @NotNull
    public InventoryCitizen getInventoryCitizen()
    {
        return inventory;
    }

    @NotNull
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
     * We override this method and execute no code to avoid citizens travelling to the nether.
     *
     * @param dimension dimension to travel to.
     */
    @Override
    public void travelToDimension(final int dimension)
    {
        //do nothing for now. We may add some funny easter eggs here later (Phrases like: "Nah I won't go there - too hot!").
    }

    @NotNull
    @Override
    public BlockPos getPosition()
    {
        return new BlockPos(posX, posY, posZ);
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
            @NotNull InventoryCitizen newInventory = new InventoryCitizen(inventory.getName(), inventory.hasCustomName(), this);
            @NotNull ArrayList<ItemStack> leftOvers = new ArrayList<>();
            for (int i = 0; i < inventory.getSizeInventory(); i++)
            {
                ItemStack itemstack = inventory.getStackInSlot(i);
                if (i < newInventory.getSizeInventory())
                {
                    newInventory.setInventorySlotContents(i, itemstack);
                }
                else
                {
                    if (itemstack != null)
                    {
                        leftOvers.add(itemstack);
                    }
                }
            }
            inventory = newInventory;

            if (dropLeftovers)
            {
                leftOvers.stream().filter(leftover -> leftover.stackSize > 0).forEach(this::entityDropItem);
            }
        }
    }

    private void tryPickupEntityItem(@NotNull EntityItem entityItem)
    {
        if (!this.worldObj.isRemote)
        {
            if (entityItem.cannotPickup())
            {
                return;
            }

            ItemStack itemStack = entityItem.getEntityItem();

            int i = itemStack.stackSize;
            if (i <= 0 || InventoryUtils.addItemStackToInventory(this.getInventoryCitizen(), itemStack))
            {
                this.worldObj.playSoundAtEntity(this, "random.pop", 0.2F, (float) ((this.rand.nextGaussian() * 0.7D + 1.0D) * 2.0D));
                this.onItemPickup(this, i);

                if (itemStack.stackSize <= 0)
                {
                    entityItem.setDead();
                }
            }
        }
    }

    /**
     * Removes the currently held item.
     */
    public void removeHeldItem()
    {
        setCurrentItemOrArmor(0, null);
    }

    /**
     * Sets the currently held item.
     *
     * @param slot from the inventory slot.
     */
    public void setHeldItem(int slot)
    {
        inventory.setHeldItem(slot);
        setCurrentItemOrArmor(0, inventory.getStackInSlot(slot));
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * Will not break the block.
     *
     * @param blockPos Block position
     */
    public void hitBlockWithToolInHand(@Nullable final BlockPos blockPos)
    {
        if (blockPos == null)
        {
            return;
        }
        hitBlockWithToolInHand(blockPos, false);
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * If breakBlock is true then it will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param blockPos   Block position
     * @param breakBlock if we want to break this block
     */
    private void hitBlockWithToolInHand(@Nullable final BlockPos blockPos, final boolean breakBlock)
    {
        if (blockPos == null)
        {
            return;
        }

        this.getLookHelper().setLookPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), FACING_DELTA_YAW, getVerticalFaceSpeed());

        this.swingItem();

        Block block = worldObj.getBlockState(blockPos).getBlock();
        if (breakBlock)
        {
            if (!worldObj.isRemote)
            {
                MineColonies.getNetwork().sendToAllAround(
                  new BlockParticleEffectMessage(blockPos, worldObj.getBlockState(blockPos), BlockParticleEffectMessage.BREAK_BLOCK),
                  new NetworkRegistry.TargetPoint(worldObj.provider.getDimensionId(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_SOUND_RANGE));
            }
            worldObj.playSoundEffect((float) (blockPos.getX() + HALF_BLOCK),
              (float) (blockPos.getY() + HALF_BLOCK),
              (float) (blockPos.getZ() + HALF_BLOCK),
              block.stepSound.getBreakSound(),
              block.stepSound.getVolume(),
              block.stepSound.getFrequency());
            worldObj.setBlockToAir(blockPos);

            damageItemInHand(1);
        }
        else
        {
            //todo: might remove this
            if (!worldObj.isRemote)
            {
                MineColonies.getNetwork().sendToAllAround(
                  //todo: correct side
                  new BlockParticleEffectMessage(blockPos, worldObj.getBlockState(blockPos), 1),
                  new NetworkRegistry.TargetPoint(worldObj.provider.getDimensionId(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_PARTICLE_RANGE));
            }
            worldObj.playSoundEffect((float) (blockPos.getX() + HALF_BLOCK),
              (float) (blockPos.getY() + HALF_BLOCK),
              (float) (blockPos.getZ() + HALF_BLOCK),
              block.stepSound.getStepSound(),
              (float) ((block.stepSound.getVolume() + 1.0D) / BLOCK_BREAK_SOUND_DAMPER),
              (float) (block.stepSound.getFrequency() * SOUND_FREQ_MODIFIER));
        }
    }

    /**
     * Damage the current held item.
     *
     * @param damage amount of damage
     */
    public void damageItemInHand(final int damage)
    {
        final ItemStack heldItem = inventory.getHeldItem();
        //If we hit with bare hands, ignore
        if (heldItem == null)
        {
            return;
        }
        heldItem.damageItem(damage, this);

        //check if tool breaks
        if (heldItem.stackSize < 1)
        {
            getInventoryCitizen().setInventorySlotContents(getInventoryCitizen().getHeldItemSlot(), null);
            this.setCurrentItemOrArmor(0, null);
        }
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * This will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param blockPos Block position
     */
    public void breakBlockWithToolInHand(@Nullable final BlockPos blockPos)
    {
        if (blockPos == null)
        {
            return;
        }
        hitBlockWithToolInHand(blockPos, true);
    }

    /**
     * Sends a localized message from the citizen containing a language string with a key and arguments.
     *
     * @param key  the key to retrieve the string.
     * @param args additional arguments.
     */
    public void sendLocalizedChat(String key, Object... args)
    {
        sendChat(LanguageHandler.format(key, args));
    }

    /**
     * Sends a chat string close to the citizen.
     *
     * @param msg the message string.
     */
    private void sendChat(@Nullable String msg)
    {
        if (msg == null || msg.length() == 0 || statusMessages.containsKey(msg))
        {
            return;
        }

        statusMessages.put(msg, ticksExisted);

        LanguageHandler.sendPlayersMessage(
          colony.getMessageEntityPlayers(),
          //TODO does this need to go through the LanguageHandler#format?
          LanguageHandler.format(this.getColonyJob().getName()) + " " + this.getCustomNameTag() + ": " + msg);
    }

    /**
     * Intelligence getter
     *
     * @return citizen intelligence value
     */
    public int getIntelligence()
    {
        return citizenData.getIntelligence();
    }

    /**
     * Charisma getter
     *
     * @return citizen Charisma value
     */
    public int getCharisma()
    {
        return citizenData.getCharisma();
    }

    /**
     * Strength getter
     *
     * @return citizen Strength value
     */
    public int getStrength()
    {
        return citizenData.getStrength();
    }

    /**
     * Endurance getter
     *
     * @return citizen Endurance value
     */
    public int getEndurance()
    {
        return citizenData.getEndurance();
    }

    /**
     * Dexterity getter
     *
     * @return citizen Dexterity value
     */
    public int getDexterity()
    {
        return citizenData.getDexterity();
    }

    /**
     * Set the skill modifier which defines how fast a citizen levels in a certain skill
     *
     * @param modifier input modifier
     */
    public void setSkillModifier(int modifier)
    {
        skillModifier = modifier;
    }

    /**
     * ExperienceLevel getter
     *
     * @return citizen ExperienceLevel value
     */
    public int getExperienceLevel()
    {
        return citizenData.getLevel();
    }

    /**
     * Called when the citizen wakes up.
     */
    public void onWakeUp()
    {
        if (this.getWorkBuilding() instanceof BuildingFarmer)
        {
            ((BuildingFarmer) this.getWorkBuilding()).resetFields();
        }
    }

    /**
     * Enum describing the citizens activity.
     */
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
