package com.minecolonies.coremod.entity;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingFarmer;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.minimal.*;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import com.minecolonies.coremod.entity.pathfinding.WalkToProxy;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.network.messages.BlockParticleEffectMessage;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
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
    private static final DataParameter<Integer> DATA_TEXTURE         = EntityDataManager.<Integer>createKey(EntityCitizen.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DATA_LEVEL           = EntityDataManager.<Integer>createKey(EntityCitizen.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DATA_IS_FEMALE       = EntityDataManager.<Integer>createKey(EntityCitizen.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DATA_COLONY_ID       = EntityDataManager.<Integer>createKey(EntityCitizen.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DATA_CITIZEN_ID      = EntityDataManager.<Integer>createKey(EntityCitizen.class, DataSerializers.VARINT);
    private static final DataParameter<String>  DATA_MODEL           = EntityDataManager.<String>createKey(EntityCitizen.class, DataSerializers.STRING);
    private static final DataParameter<String>  DATA_RENDER_METADATA = EntityDataManager.<String>createKey(EntityCitizen.class, DataSerializers.STRING);

    /**
     * The movement speed for the citizen to run away.
     */
    private static final int MOVE_AWAY_SPEED = 2;

    /**
     * The range for the citizen to move away.
     */
    private static final int MOVE_AWAY_RANGE = 6;

    /**
     * Number of ticks to heal the citizens.
     */
    private static final int HEAL_CITIZENS_AFTER = 100;

    /**
     * Tag's to save data to NBT.
     */
    private static final String TAG_COLONY_ID      = "colony";
    private static final String TAG_CITIZEN        = "citizen";
    private static final String TAG_HELD_ITEM_SLOT = "HeldItemSlot";
    private static final String TAG_STATUS         = "status";
    private static final String TAG_LAST_JOB           = "lastJob";


    /**
     * The delta yaw value for looking at things.
     */
    private static final float FACING_DELTA_YAW = 10F;

    /**
     * The range in which we can hear a block break sound.
     */
    private static final double BLOCK_BREAK_SOUND_RANGE = 16.0D;

    /**
     * The range in which someone will see the particles from a block breaking.
     */
    private static final double BLOCK_BREAK_PARTICLE_RANGE = 16.0D;

    /**
     * Divide experience by a factor to ensure more levels fit in an int.
     */
    private static final double EXP_DIVIDER = 100.0;

    /**
     * Chance the citizen will rant about bad weather. 20 ticks per 60 seconds = 5 minutes.
     */
    private static final int RANT_ABOUT_WEATHER_CHANCE = 20 * 60 * 5;

    /**
     * Quantity to be moved to rotate without actually moving.
     */
    private static final double MOVE_MINIMAL = 0.001D;

    /**
     * Base max health of the citizen.
     */
    private static final double BASE_MAX_HEALTH = 20D;

    /**
     * Base movement speed of every citizen.
     */
    public static final double BASE_MOVEMENT_SPEED = 0.3D;

    /**
     * Base pathfinding range of the citizen.
     */
    private static final int    BASE_PATHFINDING_RANGE = 100;
    /**
     * Height of the citizen.
     */
    private static final double CITIZEN_HEIGHT         = 1.8D;
    /**
     * Width of the citizen.
     */
    private static final double CITIZEN_WIDTH          = 0.6D;
    /**
     * Defines how far the citizen will be rendered.
     */
    private static final double RENDER_DISTANCE_WEIGHT = 2.0D;
    /**
     * Building level at which the workers work even if it is raining.
     */
    private static final int    BONUS_BUILDING_LEVEL   = 5;
    /**
     * The speed the citizen has to rotate.
     */
    private static final double ROTATION_MOVEMENT      = 30;
    /**
     * 20 ticks or also: once a second.
     */
    private static final int    TICKS_20               = 20;
    /**
     * This times the citizen id is the personal offset of the citizen.
     */
    private static final int    OFFSET_TICK_MULTIPLIER = 7;
    /**
     * Range required for the citizen to be home.
     */
    private static final double RANGE_TO_BE_HOME       = 16;
    /**
     * The last job of the citizen.
     */
    private String lastJob                             = "";
    /**
     * If the entitiy is stuck for 2 minutes do something.
     */
    private static final int MAX_STUCK_TIME = 20*60*2;
    private static Field navigatorField;
    private final InventoryCitizen inventory;
    @NotNull
    private final Map<String, Integer> statusMessages = new HashMap<>();
    private final PathNavigate newNavigator;
    protected Status                   status  = Status.IDLE;
    private   RenderBipedCitizen.Model modelId = RenderBipedCitizen.Model.SETTLER;
    private       String           renderMetadata;
    private       ResourceLocation texture;
    private       int              colonyId;
    private int citizenId = 0;
    private int         level;
    private int         textureId;
    /**
     * Walk to proxy.
     */
    private WalkToProxy proxy;
    /**
     * Skill modifier defines how fast a citizen levels in a certain skill.
     */
    private double skillModifier = 0;
    private boolean     female;
    @Nullable
    private Colony      colony;
    @Nullable
    private CitizenData citizenData;

    /**
     * The entities current Position.
     */
    private BlockPos currentPosition = null;

    /**
     * Time the entitiy is at the same position already.
     */
    private int stuckTime = 0;


    /**
     * Citizen constructor.
     *
     * @param world the world the citizen lives in.
     */
    public EntityCitizen(final World world)
    {
        super(world);
        setSize((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT);
        this.enablePersistence();
        this.setAlwaysRenderNameTag(Configurations.alwaysRenderNameTag);
        this.inventory = new InventoryCitizen("Minecolonies Inventory", false, this);
        this.newNavigator = new PathNavigate(this, world);
        updateNavigatorField();
        if (world.isRemote)
        {
            setRenderDistanceWeight(RENDER_DISTANCE_WEIGHT);
        }
        this.newNavigator.setCanSwim(true);
        this.newNavigator.setEnterDoors(true);

        initTasks();
    }

    private synchronized void updateNavigatorField()
    {
        if (navigatorField == null)
        {
            final Field[] fields = EntityLiving.class.getDeclaredFields();
            for (@NotNull final Field field : fields)
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
        catch (final IllegalAccessException e)
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

        if (this.getColonyJob() == null || !"com.minecolonies.coremod.job.Guard".equals(this.getColonyJob().getName()))
        {
            this.tasks.addTask(1, new EntityAICitizenAvoidEntity(this, EntityMob.class, 8.0F, 0.6D, 1.6D));
        }
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

    @Nullable
    public AbstractJob getColonyJob()
    {
        return citizenData == null ? null : citizenData.getJob();
    }

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    public void onJobChanged(@Nullable final AbstractJob job)
    {
        //  Model
        if (job == null)
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
        else
        {
            modelId = job.getModel();
        }

        dataManager.set(DATA_MODEL, modelId.name());
        setRenderMetadata("");


        //  AI Tasks
        @NotNull final Object[] currentTasks = this.tasks.taskEntries.toArray();
        for (@NotNull final Object task : currentTasks)
        {
            if (((EntityAITasks.EntityAITaskEntry) task).action instanceof AbstractEntityAIInteract)
            {
                this.tasks.removeTask(((EntityAITasks.EntityAITaskEntry) task).action);
            }
        }

        if (job != null)
        {
            job.addTasks(this.tasks);
            if (ticksExisted > 0 && getWorkBuilding() != null)
            {
                BlockPosUtil.tryMoveLivingToXYZ(this, getWorkBuilding().getLocation());
            }
        }
    }

    public int getLevel()
    {
        return level;
    }

    public void setRenderMetadata(final String metadata)
    {
        renderMetadata = metadata;
        dataManager.set(DATA_RENDER_METADATA, renderMetadata);
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
        return (citizenData == null) ? null : citizenData.getWorkBuilding();
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(final Status status)
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
    public boolean isWorkerAtSiteWithMove(@NotNull final BlockPos site, final int range)
    {
        if (proxy == null)
        {
            proxy = new WalkToProxy(this);
        }
        return proxy.walkToBlock(site, range, true);
    }

    /**
     * Get the job of the citizen.
     *
     * @param type of the type.
     * @param <J>  wildcard.
     * @return the job.
     */
    @Nullable
    public <J extends AbstractJob> J getColonyJob(@NotNull final Class<J> type)
    {
        return citizenData == null ? null : citizenData.getJob(type);
    }

    /**
     * Change the citizens Rotation to look at said block.
     *
     * @param block the block he should look at.
     */
    public void faceBlock(@Nullable final BlockPos block)
    {
        if (block == null)
        {
            return;
        }

        final double xDifference = block.getX() - this.posX;
        final double zDifference = block.getZ() - this.posZ;
        final double yDifference = block.getY() - (this.posY + (double) this.getEyeHeight());

        final double squareDifference = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        final double intendedRotationYaw = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        final double intendedRotationPitch = -(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI);
        this.setRotation((float) updateRotation(this.rotationYaw, intendedRotationYaw, ROTATION_MOVEMENT),
          (float) updateRotation(this.rotationPitch, intendedRotationPitch, ROTATION_MOVEMENT));

        final double goToX = xDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

        //Have to move the entity minimally into the direction to render his new rotation.
        moveEntity(goToX, 0, goToZ);
    }

    /**
     * Returns the new rotation degree calculated from the current and intended rotation up to a max.
     *
     * @param currentRotation  the current rotation the citizen has.
     * @param intendedRotation the wanted rotation he should have after applying this.
     * @param maxIncrement     the 'movement speed.
     * @return a rotation value he should move.
     */
    private static double updateRotation(final double currentRotation, final double intendedRotation, final double maxIncrement)
    {
        double wrappedAngle = MathHelper.wrapDegrees(intendedRotation - currentRotation);

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
     * Collect exp orbs around the entity.
     */
    public void gatherXp()
    {
        for (@NotNull final EntityXPOrb orb : getXPOrbsOnGrid())
        {
            addExperience(orb.getXpValue() / 2.0D);
            orb.setDead();
        }
    }

    /**
     * Defines the area in which the citizen automatically gathers experience.
     *
     * @return a list of xp orbs around the entity.
     */
    private List<EntityXPOrb> getXPOrbsOnGrid()
    {
        @NotNull final AxisAlignedBB bb = new AxisAlignedBB(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2);

        return worldObj.getEntitiesWithinAABB(EntityXPOrb.class, bb);
    }

    /**
     * Add experience points to citizen.
     * Increases the citizen level if he has sufficient experience.
     * This will reset the experience.
     *
     * @param xp the amount of points added.
     */
    public void addExperience(final double xp)
    {
        final double citizenHutLevel = getHomeBuilding() == null ? 0 : getHomeBuilding().getBuildingLevel();
        final double citizenHutMaxLevel = getHomeBuilding() == null ? 1 : getHomeBuilding().getMaxBuildingLevel();
        if (citizenHutLevel < citizenHutMaxLevel
              && Math.pow(2.0, citizenHutLevel + 1.0) < this.getExperienceLevel())
        {
            return;
        }

        final double maxValue = Integer.MAX_VALUE - citizenData.getExperience();
        double localXp = xp * skillModifier / EXP_DIVIDER;
        final double workBuildingLevel = getWorkBuilding() == null ? 0 : getWorkBuilding().getBuildingLevel();
        final double bonusXp = workBuildingLevel * (1 + citizenHutLevel) / Math.log(this.getExperienceLevel() + 2.0D);
        localXp = localXp * bonusXp;
        if (localXp > maxValue)
        {
            localXp = maxValue;
        }
        citizenData.addExperience(localXp);

        while (ExperienceUtils.getXPNeededForNextLevel(citizenData.getLevel()) < citizenData.getExperience())
        {
            citizenData.increaseLevel();
        }
        this.updateLevel();
        citizenData.markDirty();
    }

    private BuildingHome getHomeBuilding()
    {
        return (citizenData == null) ? null : citizenData.getHomeBuilding();
    }

    /**
     * ExperienceLevel getter.
     *
     * @return citizen ExperienceLevel value.
     */
    public int getExperienceLevel()
    {
        return citizenData.getLevel();
    }

    /**
     * Updates the level of the citizen.
     */
    private void updateLevel()
    {
        level = citizenData == null ? 0 : citizenData.getLevel();
        dataManager.set(DATA_LEVEL, level);
    }

    /**
     * Entities treat being on ladders as not on ground; this breaks navigation logic.
     */
    @Override
    protected void updateFallState(final double y, final boolean onGroundIn, final IBlockState state, final BlockPos pos)
    {
        if (!onGround)
        {
            final int px = MathHelper.floor_double(posX);
            final int py = (int) posY;
            final int pz = MathHelper.floor_double(posZ);

            this.onGround =
              worldObj.getBlockState(new BlockPos(px, py, pz)).getBlock().isLadder(worldObj.getBlockState(new BlockPos(px, py, pz)), worldObj, new BlockPos(px, py, pz),
                this);
        }

        super.updateFallState(y, onGroundIn, state, pos);
    }

    @Override
    public boolean attackEntityFrom(@NotNull final DamageSource damageSource, final float damage)
    {
        final Entity sourceEntity = damageSource.getEntity();
        if (sourceEntity instanceof EntityCitizen && ((EntityCitizen) sourceEntity).colonyId == this.colonyId)
        {
            return false;
        }

        final boolean result = super.attackEntityFrom(damageSource, damage);

        if (damageSource.isMagicDamage() || damageSource.isFireDamage())
        {
            return result;
        }

        updateArmorDamage(damage);

        return result;
    }

    /**
     * Called when the mob's health reaches 0.
     *
     * @param par1DamageSource the attacking entity.
     */
    @Override
    public void onDeath(final DamageSource par1DamageSource)
    {
        dropExperience();
        this.setDead();

        if (colony != null)
        {
            if (getColonyJob() instanceof JobGuard)
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "tile.blockHutTownHall.messageGuardDead",
                        citizenData.getName(), (int)posX, (int) posY, (int) posZ);
            }
            else
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "tile.blockHutTownHall.messageColonistDead",
                  citizenData.getName(), (int) posX, (int) posY, (int) posZ);
            }
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
            experience = (int) (this.getExperiencePoints());

            while (experience > 0)
            {
                final int j = EntityXPOrb.getXPSplit(experience);
                experience -= j;
                this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, j));
            }
        }

        //Spawn particle explosion of xp orbs on death
        for (int i = 0; i < 20; ++i)
        {
            final double d2 = this.rand.nextGaussian() * 0.02D;
            final double d0 = this.rand.nextGaussian() * 0.02D;
            final double d1 = this.rand.nextGaussian() * 0.02D;
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
    private double getExperiencePoints()
    {
        return citizenData.getExperience();
    }

    /**
     * Updates the armour damage after being hit.
     *
     * @param damage damage dealt.
     */
    private void updateArmorDamage(final double damage)
    {
        for (final ItemStack stack : this.getArmorInventoryList())
        {
            if (stack == null || stack.getItem() == null || !(stack.getItem() instanceof ItemArmor))
            {
                continue;
            }
            stack.damageItem((int) (damage / 2), this);

            if (stack.stackSize < 1)
            {
                setItemStackToSlot(getSlotForItemStack(stack), null);
            }
            setItemStackToSlot(getSlotForItemStack(stack), stack);
        }
    }

    /**
     * For the time being we don't want any childrens of our colonists.
     *
     * @param var1 the ageable entity.
     * @return the child.
     */
    @Override
    public EntityAgeable createChild(final EntityAgeable var1)
    {
        return null;
    }

    /**
     * Called when a player tries to interact with a citizen.
     *
     * @param player which interacts with the citizen.
     * @return If citizen should interact or not.
     */
    @Override
    public boolean processInteract(@NotNull final EntityPlayer player, final EnumHand hand, @Nullable final ItemStack stack)
    {
        if (worldObj.isRemote)
        {
            final CitizenDataView citizenDataView = getCitizenDataView();
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
        dataManager.register(DATA_COLONY_ID, colonyId);
        dataManager.register(DATA_CITIZEN_ID, citizenId);
        dataManager.register(DATA_TEXTURE, 0);
        dataManager.register(DATA_LEVEL, 0);
        dataManager.register(DATA_IS_FEMALE, 0);
        dataManager.register(DATA_MODEL, RenderBipedCitizen.Model.SETTLER.name());
        dataManager.register(DATA_RENDER_METADATA, "");
    }

    @Override
    public void writeEntityToNBT(final NBTTagCompound compound)
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
        compound.setString(TAG_LAST_JOB, lastJob);
    }

    @Override
    public void readEntityFromNBT(final NBTTagCompound compound)
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
        lastJob = compound.getString(TAG_LAST_JOB);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons.
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
            checkIfStuck();
            if (worldObj.isDaytime() && !worldObj.isRaining())
            {
                SoundUtils.playRandomSound(worldObj, this);
            }
            else if (worldObj.isRaining() && 1 >= rand.nextInt(RANT_ABOUT_WEATHER_CHANCE) && this.getColonyJob() != null)
            {
                SoundUtils.playSoundAtCitizenWithChance(worldObj, this.getPosition(), this.getColonyJob().getBadWeatherSound(), 1);
            }
        }

        if (isEntityInsideOpaqueBlock() || isInsideOfMaterial(Material.LEAVES))
        {
            getNavigator().moveAwayFromXYZ(this.getPosition(), MOVE_AWAY_RANGE, MOVE_AWAY_SPEED);
        }

        checkHeal();
        super.onLivingUpdate();
    }

    private void checkIfStuck()
    {
        if(this.currentPosition == null)
        {
            this.currentPosition = this.getPosition();
            return;
        }

        if(this.currentPosition.equals(this.getPosition()) && newNavigator != null && newNavigator.getDestination() != null)
        {
            stuckTime++;
            if(stuckTime >= MAX_STUCK_TIME)
            {
                if (newNavigator.getDestination().distanceSq(posX, posY, posZ) < MOVE_AWAY_RANGE)
                {
                    stuckTime = 0;
                    return;
                }

                @Nullable final BlockPos spawnPoint =
                        Utils.scanForBlockNearPoint
                                (worldObj, newNavigator.getDestination(), 1, 0, 1, 2,
                                        Blocks.AIR,
                                        Blocks.SNOW_LAYER,
                                        Blocks.TALLGRASS,
                                        Blocks.RED_FLOWER,
                                        Blocks.YELLOW_FLOWER,
                                        Blocks.CARPET);

                EntityUtils.setSpawnPoint(spawnPoint, this);
                if (colony != null)
                {
                    Log.getLogger().info("Teleported stuck citizen " + this.getName() + " from colony: " + colony.getID() + " to target location");
                }
                stuckTime = 0;
            }
        }
        else
        {
            stuckTime = 0;
            this.currentPosition = this.getPosition();
        }

        this.currentPosition = this.getPosition();
    }

    /**
     * Sets the last job of the citizen.
     * @param jobName the job he last had.
     */
    public void setLastJob(@NotNull String jobName)
    {
        this.lastJob = jobName;
    }

    /**
     * Getter for the last job.
     * @return the last job he had.
     */
    @NotNull
    public String getLastJob()
    {
        return this.lastJob;
    }

    private void updateColonyClient()
    {
        if (dataManager.isDirty())
        {
            if (colonyId == 0)
            {
                colonyId = dataManager.get(DATA_COLONY_ID);
            }

            if (citizenId == 0)
            {
                citizenId = dataManager.get(DATA_CITIZEN_ID);
            }

            female = dataManager.get(DATA_IS_FEMALE) != 0;
            level = dataManager.get(DATA_LEVEL);
            modelId = RenderBipedCitizen.Model.valueOf(dataManager.get(DATA_MODEL));
            textureId = dataManager.get(DATA_TEXTURE);
            renderMetadata = dataManager.get(DATA_RENDER_METADATA);
            setTexture();
            dataManager.setClean();
        }
        updateArmSwingProgress();
    }

    /**
     * Pick up all items in a range around the citizen.
     */
    private void pickupItems()
    {
        @NotNull final List<EntityItem> retList = new ArrayList<>();
        //I know streams look better but they are flawed in type erasure
        for (final Object o : worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(2.0F, 0.0F, 2.0F)))
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
        if (statusMessages.size() > 0 && ticksExisted % TICKS_20 == 0)
        {
            @NotNull final Iterator<Map.Entry<String, Integer>> it = statusMessages.entrySet().iterator();
            while (it.hasNext())
            {
                if (ticksExisted - it.next().getValue() > TICKS_20 * Configurations.chatFrequency)
                {
                    it.remove();
                }
            }
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
     * Sets the textures of all citizens and distinguishes between male and female.
     */
    private void setTexture()
    {
        if (!worldObj.isRemote)
        {
            return;
        }

        final RenderBipedCitizen.Model model = getModelID();

        String textureBase = "textures/entity/" + model.textureBase;
        textureBase += female ? "Female" : "Male";

        final int moddedTextureId = (textureId % model.numTextures) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + renderMetadata + ".png");
    }

    public int getOffsetTicks()
    {
        return this.ticksExisted + OFFSET_TICK_MULTIPLIER * this.getEntityId();
    }

    public RenderBipedCitizen.Model getModelID()
    {
        return modelId;
    }

    /**
     * Server-specific update for the EntityCitizen.
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
     * Handles extreme cases like colony or citizen is null.
     */
    private void handleNullColony()
    {
        final Colony c = ColonyManager.getColony(colonyId);

        if (c == null)
        {
            Log.getLogger().warn(String.format("EntityCitizen '%s' unable to find Colony #%d", getUniqueID(), colonyId));
            setDead();
            return;
        }

        final CitizenData data = c.getCitizen(citizenId);
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

        @Nullable final EntityCitizen existingCitizen = data.getCitizenEntity();
        if (existingCitizen != null && existingCitizen != this)
        {
            // This Citizen already has a different Entity registered to it
            handleExistingCitizen(data, existingCitizen);
            return;
        }

        setColony(c, data);
    }

    private void handleExistingCitizen(@NotNull final CitizenData data, @NotNull final EntityCitizen existingCitizen)
    {
        Log.getLogger().warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen #%d, but already have a citizen ('%s')",
          getUniqueID(),
          colonyId,
          citizenId,
          existingCitizen.getUniqueID()));
        if (existingCitizen.getUniqueID().equals(this.getUniqueID()))
        {
            data.setCitizenEntity(this);
        }
        else
        {
            setDead();
        }
    }

    /**
     * Assigns a citizen to a colony.
     *
     * @param c    the colony.
     * @param data the data of the new citizen.
     */
    public void setColony(@Nullable final Colony c, @Nullable final CitizenData data)
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

        dataManager.set(DATA_COLONY_ID, colonyId);
        dataManager.set(DATA_CITIZEN_ID, citizenId);
        dataManager.set(DATA_IS_FEMALE, female ? 1 : 0);
        dataManager.set(DATA_TEXTURE, textureId);
        updateLevel();

        citizenData.setCitizenEntity(this);

        onJobChanged(getColonyJob());
    }

    /**
     * Getter of the dataview, the clientside representation of the citizen.
     *
     * @return the view.
     */
    private CitizenDataView getCitizenDataView()
    {
        if (colonyId != 0 && citizenId != 0)
        {
            final ColonyView colonyView = ColonyManager.getColonyView(colonyId);
            if (colonyView != null)
            {
                return colonyView.getCitizen(citizenId);
            }
        }

        return null;
    }

    /**
     * Getter of the citizens random object.
     *
     * @return random object.
     */
    public Random getRandom()
    {
        return rand;
    }

    /**
     * Applies attributes like health, charisma etc to the citizens.
     */
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();

        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(BASE_MOVEMENT_SPEED);

        //path finding search range
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(BASE_PATHFINDING_RANGE);
    }

    @NotNull
    @Override
    public PathNavigate getNavigator()
    {
        return newNavigator;
    }

    /**
     * Drop the equipment for this entity.
     */
    @Override
    protected void dropEquipment(final boolean par1, final int par2)
    {
        //Drop actual inventory
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack != null && itemstack.stackSize > 0)
            {
                entityDropItem(itemstack);
            }
        }
    }

    /**
     * Returns false if the newer Entity AI code should be run.
     */
    @Override
    public boolean isAIDisabled()
    {
        return false;
    }

    /**
     * Handles the dropping of items from the entity.
     *
     * @param itemstack to drop.
     * @return the dropped item.
     */
    private EntityItem entityDropItem(@NotNull final ItemStack itemstack)
    {
        return entityDropItem(itemstack, 0.0F);
    }

    /**
     * Getter of the resource location of the texture.
     *
     * @return location of the texture.
     */
    public ResourceLocation getTexture()
    {
        return texture;
    }

    /**
     * Getter which checks if the citizen is female.
     *
     * @return true if female.
     */
    public boolean isFemale()
    {
        return female;
    }

    /**
     * Clears the colony of the citizen.
     */
    public void clearColony()
    {
        setColony(null, null);
    }

    public boolean isAtHome()
    {
        @Nullable final BlockPos homePosition = getHomePosition();
        return homePosition != null && homePosition.distanceSq((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)) <= RANGE_TO_BE_HOME;
    }

    /**
     * Returns the home position of each citizen (His house or town hall).
     *
     * @return location
     */
    @Nullable
    @Override
    public BlockPos getHomePosition()
    {
        @Nullable final BuildingHome homeBuilding = getHomeBuilding();
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
        if (this.getColonyJob() instanceof JobGuard)
        {
            return DesiredActivity.WORK;
        }

        if (!worldObj.isDaytime())
        {
            return DesiredActivity.SLEEP;
        }
        else if (worldObj.isRaining() && !shouldWorkWhileRaining())
        {
            return DesiredActivity.IDLE;
        }
        else
        {
            if(this.getNavigator() != null && (this.getNavigator().getPath() != null && this.getNavigator().getPath().getCurrentPathLength() == 0))
            {
                this.getNavigator().clearPathEntity();
            }
            return DesiredActivity.WORK;
        }
    }

    /**
     * Checks if the citizen should work even when it rains.
     *
     * @return true if his building level is bigger than 5.
     */
    private boolean shouldWorkWhileRaining()
    {
        return this.getWorkBuilding() != null && (this.getWorkBuilding().getBuildingLevel() >= BONUS_BUILDING_LEVEL);
    }

    /**
     * We override this method and execute no code to avoid citizens travelling to the nether.
     *
     * @param dimensionIn dimension to travel to.
     */
    @Override
    @Nullable
    public Entity changeDimension(final int dimensionIn)
    {
        return null;
    }

    @NotNull
    @Override
    public BlockPos getPosition()
    {
        return new BlockPos(posX, posY, posZ);
    }

    /**
     * Returns the first slot in the inventory with a specific item.
     *
     * @param targetItem the item.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public int findFirstSlotInInventoryWith(final Item targetItem, int itemDamage)
    {
        return InventoryUtils.findFirstSlotInInventoryWith(getInventoryCitizen(), targetItem, itemDamage);
    }

    /**
     * Returns the first slot in the inventory with a specific block.
     *
     * @param block the block.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public int findFirstSlotInInventoryWith(final Block block, int itemDamage)
    {
        return InventoryUtils.findFirstSlotInInventoryWith(getInventoryCitizen(), block, itemDamage);
    }

    /**
     * Returns the amount of a certain block in the inventory.
     *
     * @param block the block.
     * @param itemDamage the damage value
     * @return the quantity.
     */
    public int getItemCountInInventory(final Block block, int itemDamage)
    {
        return InventoryUtils.getItemCountInInventory(getInventoryCitizen(), block, itemDamage);
    }

    /**
     * Returns the amount of a certain item in the inventory.
     *
     * @param targetItem the block.
     * @param itemDamage the damage value.
     * @return the quantity.
     */
    public int getItemCountInInventory(final Item targetItem, int itemDamage)
    {
        return InventoryUtils.getItemCountInInventory(getInventoryCitizen(), targetItem, itemDamage);
    }

    /**
     * Checks if citizen has a certain block in the inventory.
     *
     * @param block the block.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public boolean hasItemInInventory(final Block block, int itemDamage)
    {
        return InventoryUtils.hasitemInInventory(getInventoryCitizen(), block, itemDamage);
    }

    /**
     * Checks if citizen has a certain item in the inventory.
     *
     * @param item the item.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public boolean hasItemInInventory(final Item item, int itemDamage)
    {
        return InventoryUtils.hasitemInInventory(getInventoryCitizen(), item, itemDamage);
    }

    /**
     * Citizen will try to pick up a certain item.
     *
     * @param entityItem the item he wants to pickup.
     */
    private void tryPickupEntityItem(@NotNull final EntityItem entityItem)
    {
        if (!this.worldObj.isRemote)
        {
            if (entityItem.cannotPickup())
            {
                return;
            }

            final ItemStack itemStack = entityItem.getEntityItem();

            final int i = itemStack.stackSize;
            if (i <= 0 || InventoryUtils.addItemStackToInventory(this.getInventoryCitizen(), itemStack))
            {
                this.worldObj.playSound((EntityPlayer) null,
                  this.getPosition(),
                  SoundEvents.ENTITY_ITEM_PICKUP,
                  SoundCategory.AMBIENT,
                  0.2F,
                  (float) ((this.rand.nextGaussian() * 0.7D + 1.0D) * 2.0D));
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
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);
    }

    /**
     * Sets the currently held item.
     *
     * @param slot from the inventory slot.
     */
    public void setHeldItem(final int slot)
    {
        inventory.setHeldItem(slot);
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inventory.getStackInSlot(slot));
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * Will not break the block.
     *
     * @param blockPos Block position.
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
     * @param blockPos   Block position.
     * @param breakBlock if we want to break this block.
     */
    private void hitBlockWithToolInHand(@Nullable final BlockPos blockPos, final boolean breakBlock)
    {
        if (blockPos == null)
        {
            return;
        }

        this.getLookHelper().setLookPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), FACING_DELTA_YAW, getVerticalFaceSpeed());

        this.swingArm(this.getActiveHand());

        final IBlockState blockState = worldObj.getBlockState(blockPos);
        final Block block = blockState.getBlock();
        if (breakBlock)
        {
            if (!worldObj.isRemote)
            {
                MineColonies.getNetwork().sendToAllAround(
                  new BlockParticleEffectMessage(blockPos, worldObj.getBlockState(blockPos), BlockParticleEffectMessage.BREAK_BLOCK),
                  new NetworkRegistry.TargetPoint(worldObj.provider.getDimension(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_SOUND_RANGE));
            }
            worldObj.playSound(null,
              blockPos,
              block.getSoundType(blockState, worldObj, blockPos, this).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, worldObj, blockPos, this).getVolume(),
              block.getSoundType(blockState, worldObj, blockPos, this).getPitch());
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
                  new NetworkRegistry.TargetPoint(worldObj.provider.getDimension(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_PARTICLE_RANGE));
            }
            worldObj.playSound((EntityPlayer) null,
              blockPos,
              block.getSoundType(blockState, worldObj, blockPos, this).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, worldObj, blockPos, this).getVolume(),
              block.getSoundType(blockState, worldObj, blockPos, this).getPitch());
        }
    }

    /**
     * Damage the current held item.
     *
     * @param damage amount of damage.
     */
    public void damageItemInHand(final int damage)
    {
        final ItemStack heldItem = inventory.getHeldItemMainhand();
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
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);
        }
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * This will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param blockPos Block position.
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
    public void sendLocalizedChat(final String key, final Object... args)
    {
        sendChat(key, args);
    }

    /**
     * Sends a chat string close to the citizen.
     *
     * @param msg the message string.
     */
    private void sendChat(final String key, @Nullable final Object... msg)
    {
        if (msg == null || statusMessages.containsKey(key))
        {
            return;
        }

        final TextComponentTranslation requiredItem;

        if(msg.length == 0)
        {
            requiredItem = new TextComponentTranslation(key);
        }
        else
        {
            statusMessages.put(key + msg[0], ticksExisted);
            requiredItem = new TextComponentTranslation(key, msg);
        }

        final TextComponentString citizenDescription = new TextComponentString(" ");
        citizenDescription.appendText(this.getCustomNameTag()).appendText(": ");
        final TextComponentString colonyDescription = new TextComponentString(" at " + this.getColony().getName() + ":");

        LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),  this.getColonyJob().getName(), colonyDescription, citizenDescription, requiredItem);
    }

    /**
     * Intelligence getter.
     *
     * @return citizen intelligence value.
     */
    public int getIntelligence()
    {
        return citizenData.getIntelligence();
    }

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    public int getCharisma()
    {
        return citizenData.getCharisma();
    }

    /**
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    public int getStrength()
    {
        return citizenData.getStrength();
    }

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    public int getEndurance()
    {
        return citizenData.getEndurance();
    }

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    public int getDexterity()
    {
        return citizenData.getDexterity();
    }

    /**
     * Set the skill modifier which defines how fast a citizen levels in a certain skill.
     *
     * @param modifier input modifier.
     */
    public void setSkillModifier(final int modifier)
    {
        skillModifier = modifier;
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
     * Used for chat messages, sounds, and other need based interactions.
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
