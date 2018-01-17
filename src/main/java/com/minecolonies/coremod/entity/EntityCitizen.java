package com.minecolonies.coremod.entity;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.minimal.*;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianUtils;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.network.messages.BlockParticleEffectMessage;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.inventory.InventoryCitizen.TAG_ITEMS;

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

    private static Field navigatorField;
    @NotNull
    private final Map<String, Integer> statusMessages = new HashMap<>();
    private final PathNavigate newNavigator;
    /**
     * The 4 lines of the latest status.
     */
    private final ITextComponent[] latestStatus = new ITextComponent[MAX_LINES_OF_LATEST_LOG];
    protected Status                   status  = Status.IDLE;
    /**
     * The last job of the citizen.
     */
    private   String                   lastJob = "";
    private   RenderBipedCitizen.Model modelId = RenderBipedCitizen.Model.SETTLER;
    private String           renderMetadata;
    private ResourceLocation texture;
    private int              colonyId;
    private int citizenId = 0;
    private int          level;
    private int          textureId;
    /**
     * Walk to proxy.
     */
    private IWalkToProxy proxy;
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
     * Time the entity is at the same position already.
     */
    private int stuckTime = 0;

    /**
     * Variable to check what time it is for the citizen.
     */
    private boolean isDay = true;

    /**
     * Field to try moving away from a location in order to pass it.
     */
    private boolean triedMovingAway = false;

    private NBTTagCompound dataBackup = null;

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
        this.setAlwaysRenderNameTag(Configurations.Gameplay.alwaysRenderNameTag);
        this.newNavigator = new PathNavigate(this, world);
        updateNavigatorField();
        if (CompatibilityUtils.getWorld(this).isRemote)
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
     * Initiates citizen tasks
     * Suppressing Sonar Rule Squid:S881
     * The rule thinks we should extract ++priority in a proper statement.
     * But in this case the rule does not apply because that would remove the readability.
     */
    @SuppressWarnings(INCREMENT_AND_DECREMENT_OPERATORS_SHOULD_NOT_BE_USED_IN_A_METHOD_CALL_OR_MIXED_WITH_OTHER_OPERATORS_IN_AN_EXPRESSION)
    private void initTasks()
    {
        int priority = 0;
        this.tasks.addTask(priority, new EntityAISwimming(this));

        if (this.getColonyJob() == null || !"com.minecolonies.coremod.job.Guard".equals(this.getColonyJob().getName()))
        {
            this.tasks.addTask(++priority, new EntityAICitizenAvoidEntity(this, EntityMob.class, (float) DISTANCE_OF_ENTITY_AVOID, LATER_RUN_SPEED_AVOID, INITIAL_RUN_SPEED_AVOID));
        }
        this.tasks.addTask(++priority, new EntityAIGoHome(this));
        this.tasks.addTask(++priority, new EntityAISleep(this));
        this.tasks.addTask(++priority, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(priority, new EntityAIOpenFenceGate(this, true));
        this.tasks.addTask(++priority, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(++priority, new EntityAIWatchClosest2(this, EntityCitizen.class, 5.0F, 0.02F));
        this.tasks.addTask(++priority, new EntityAICitizenWander(this, 0.6D));
        this.tasks.addTask(++priority, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));

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

    /**
     * Get the level of the citizen.
     *
     * @return the level of the citizen.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Set the metadata for rendering.
     *
     * @param metadata the metadata required.
     */
    public void setRenderMetadata(final String metadata)
    {
        renderMetadata = metadata;
        dataManager.set(DATA_RENDER_METADATA, renderMetadata);
        //Display some debug info always available while testing
        //tofo: remove this when in Beta!
        //Will help track down some hard to find bugs (Pathfinding etc.)
        if (citizenData != null)
        {
            if (this.getColonyJob() != null && Configurations.Gameplay.enableInDevelopmentFeatures)
            {
                setCustomNameTag(citizenData.getName() + " (" + getStatus() + ")[" + this.getColonyJob().getNameTagDescription() + "]");
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

    public ILocation getLocation()
    {
        return StandardFactoryController.getInstance().getNewInstance(TypeConstants.ILOCATION, this);
    }

    /**
     * Get the latest status of the citizen.
     *
     * @return a ITextComponent with the length 4 describing it.
     */
    public ITextComponent[] getLatestStatus()
    {
        return latestStatus.clone();
    }

    /**
     * Set the latest status of the citizen and clear the existing status
     *
     * @param status the new status to set.
     */
    public void setLatestStatus(final ITextComponent... status)
    {
        boolean hasChanged = false;
        for (int i = 0; i < latestStatus.length; i++)
        {
            ITextComponent newStatus;
            if (i >= status.length)
            {
                newStatus = null;
            }
            else
            {
                newStatus = status[i];
            }

            if (!Objects.equals(latestStatus[i], newStatus))
            {
                latestStatus[i] = newStatus;
                hasChanged = true;
            }
        }

        if (hasChanged)
        {
            citizenData.markDirty();
        }
    }

    /**
     * Append to the existing latestStatus list.
     * This will override the oldest one if full and move the others one down in the array.
     *
     * @param status the latest status to append
     */
    public void addLatestStatus(final ITextComponent status)
    {
        for (int i = latestStatus.length - 1; i > 0; i--)
        {
            latestStatus[i] = latestStatus[i - 1];
        }

        latestStatus[0] = status;
        citizenData.markDirty();
    }

    /**
     * On Inventory change, mark the building dirty.
     */
    public void onInventoryChanged()
    {
        if (citizenData != null)
        {
            final AbstractBuildingWorker building = citizenData.getWorkBuilding();
            if (building != null)
            {
                building.markDirty();
            }
        }
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
            proxy = new EntityCitizenWalkToProxy(this);
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
        moveEntityWithHeading((float) goToX, (float) goToZ);
    }

    /**
     * Returns the new rotation degree calculated from the current and intended
     * rotation up to a max.
     *
     * @param currentRotation  the current rotation the citizen has.
     * @param intendedRotation the wanted rotation he should have after applying
     *                         this.
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
     * Entities treat being on ladders as not on ground; this breaks navigation
     * logic.
     */
    @Override
    protected void updateFallState(final double y, final boolean onGroundIn, final IBlockState state, final BlockPos pos)
    {
        if (!onGround)
        {
            final int px = MathHelper.floor(posX);
            final int py = (int) posY;
            final int pz = MathHelper.floor(posZ);

            this.onGround =
              CompatibilityUtils.getWorld(this).getBlockState(new BlockPos(px, py, pz)).getBlock().isLadder(world.getBlockState(
                new BlockPos(px, py, pz)), world, new BlockPos(px, py, pz), this);
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
        setLastAttacker(damageSource.getEntity());

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
     * @param damageSource the attacking entity.
     */
    @Override
    public void onDeath(final DamageSource damageSource)
    {
        double penalty = CITIZEN_DEATH_PENALTY;
        if (damageSource.getEntity() instanceof EntityPlayer)
        {
            for (final Player player : PermissionUtils.getPlayersWithAtLeastRank(colony, Rank.OFFICER))
            {
                if (player.getID().equals(damageSource.getEntity().getUniqueID()))
                {
                    penalty = CITIZEN_KILL_PENALTY;
                    break;
                }
            }
        }

        dropExperience();
        this.setDead();

        if (colony != null)
        {
            colony.decreaseOverallHappiness(penalty);
            triggerDeathAchievement(damageSource, getColonyJob());
            if (getColonyJob() instanceof JobGuard)
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "tile.blockHutTownHall.messageGuardDead",
                  citizenData.getName(), (int) posX, (int) posY, (int) posZ, damageSource.damageType);
            }
            else
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "tile.blockHutTownHall.messageColonistDead",
                  citizenData.getName(), (int) posX, (int) posY, (int) posZ, damageSource.damageType);
            }
            colony.getCitizenManager().removeCitizen(getCitizenData());
        }
        super.onDeath(damageSource);
    }

    /**
     * Drop some experience share depending on the experience and
     * experienceLevel.
     */
    private void dropExperience()
    {
        int experience;

        if (!CompatibilityUtils.getWorld(this).isRemote && this.recentlyHit > 0 && this.canDropLoot() && CompatibilityUtils.getWorld(this).getGameRules().getBoolean("doMobLoot"))
        {
            experience = (int) (this.citizenData.getExperience());

            while (experience > 0)
            {
                final int j = EntityXPOrb.getXPSplit(experience);
                experience -= j;
                CompatibilityUtils.getWorld(this).spawnEntity(new EntityXPOrb(CompatibilityUtils.getWorld(this), this.posX, this.posY, this.posZ, j));
            }
        }

        //Spawn particle explosion of xp orbs on death
        for (int i = 0; i < 20; ++i)
        {
            final double d2 = this.rand.nextGaussian() * 0.02D;
            final double d0 = this.rand.nextGaussian() * 0.02D;
            final double d1 = this.rand.nextGaussian() * 0.02D;
            CompatibilityUtils.getWorld(this).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
              this.posX + (this.rand.nextDouble() * this.width * 2.0F) - (double) this.width,
              this.posY + (this.rand.nextDouble() * this.height),
              this.posZ + (this.rand.nextDouble() * this.width * 2.0F) - (double) this.width,
              d2,
              d0,
              d1);
        }
    }

    /**
     * Trigger the corresponding death achievement.
     *
     * @param source The damage source.
     * @param job    The job of the citizen.
     */
    public void triggerDeathAchievement(final DamageSource source, final AbstractJob job)
    {
        // If the job is null, then we can trigger jobless citizen achievement
        if (job != null)
        {
            job.triggerDeathAchievement(source, this);
        }
    }

    @Nullable
    public CitizenData getCitizenData()
    {
        return citizenData;
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) new InvWrapper(getCitizenData().getInventory());
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return true;
        }

        return super.hasCapability(capability, facing);
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
            if (ItemStackUtils.isEmpty(stack) || !(stack.getItem() instanceof ItemArmor))
            {
                continue;
            }
            stack.damageItem((int) (damage / 2), this);

            if (ItemStackUtils.getSize(stack) < 1)
            {
                setItemStackToSlot(getSlotForItemStack(stack), ItemStackUtils.EMPTY);
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

    @Override
    public boolean processInteract(final EntityPlayer player, final EnumHand hand, final ItemStack stack)
    {
        final ColonyView colonyView = ColonyManager.getColonyView(colonyId);
        if (colonyView != null && !colonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return false;
        }

        if(stack != null && stack.getItem() instanceof ItemNameTag)
        {
            return super.processInteract(player, hand, stack);
        }

        if (CompatibilityUtils.getWorld(this).isRemote)
        {
            if(player.isSneaking())
            {
                MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(this.getName(), this.getEntityId()));
            }
            else
            {
                final CitizenDataView citizenDataView = getCitizenDataView();
                if (citizenDataView != null)
                {
                    MineColonies.proxy.showCitizenWindow(citizenDataView);
                }
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

        compound.setString(TAG_LAST_JOB, lastJob);
        compound.setBoolean(TAG_DAY, isDay);
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

        lastJob = compound.getString(TAG_LAST_JOB);
        isDay = compound.getBoolean(TAG_DAY);

        if (compound.hasKey(TAG_HELD_ITEM_SLOT))
        {
            this.dataBackup = compound;
        }
    }

    /**
     * Called frequently so the entity can update its state every tick as
     * required. For example, zombies and skeletons. use this to react to
     * sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate()
    {
        if (recentlyHit > 0)
        {
            citizenData.markDirty();
        }
        if (CompatibilityUtils.getWorld(this).isRemote)
        {
            updateColonyClient();
        }
        else
        {
            if (getOffsetTicks() % TICKS_20 == 0)
            {
                this.setAlwaysRenderNameTag(Configurations.Gameplay.alwaysRenderNameTag);
                pickupItems();
                cleanupChatMessages();
                updateColonyServer();
            }

            if (getColonyJob() != null || !CompatibilityUtils.getWorld(this).isDaytime())
            {
                if (ticksExisted % TICKS_20 == 0)
                {
                    checkIfStuck();

                    if (ticksExisted % (MAX_STUCK_TIME * 2 + TICKS_20) == 0)
                    {
                        triedMovingAway = false;
                    }
                }
            }
            else
            {
                setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.waitingForWork"));
            }

            if (CompatibilityUtils.getWorld(this).isDaytime() && !CompatibilityUtils.getWorld(this).isRaining() && citizenData != null)
            {
                SoundUtils.playRandomSound(CompatibilityUtils.getWorld(this), this, citizenData.getSaturation());
            }
            else if (CompatibilityUtils.getWorld(this).isRaining() && 1 >= rand.nextInt(RANT_ABOUT_WEATHER_CHANCE) && this.getColonyJob() != null)
            {
                SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorld(this), this.getPosition(), this.getColonyJob().getBadWeatherSound(), 1);
            }
        }

        if (isEntityInsideOpaqueBlock() || isInsideOfMaterial(Material.LEAVES))
        {
            getNavigator().moveAwayFromXYZ(this.getPosition(), MOVE_AWAY_RANGE, MOVE_AWAY_SPEED);
        }

        gatherXp();
        if (citizenData != null)
        {
            if (citizenData.getSaturation() <= 0)
            {
                this.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness")));
            }
            else
            {
                this.removeActivePotionEffect(Potion.getPotionFromResourceLocation("slowness"));
            }

            if (citizenData.getSaturation() < HIGH_SATURATION)
            {
                tryToEat();
            }
        }

        if (dataBackup != null)
        {
            this.getCitizenData().getInventory().readFromNBT(dataBackup.getCompoundTag(TAG_ITEMS));
            this.getCitizenData().getInventory().setHeldItem(dataBackup.getInteger(TAG_HELD_ITEM_SLOT));
            dataBackup = null;
        }

        checkHeal();
        super.onLivingUpdate();
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

    public int getOffsetTicks()
    {
        return this.ticksExisted + OFFSET_TICK_MULTIPLIER * this.getEntityId();
    }

    /**
     * Pick up all items in a range around the citizen.
     */
    private void pickupItems()
    {
        @NotNull final List<EntityItem> retList = new ArrayList<>();
        //I know streams look better but they are flawed in type erasure
        for (final Object o :
          CompatibilityUtils.getWorld(this).
                                             getEntitiesWithinAABB(EntityItem.class,
                                               new AxisAlignedBB(getPosition()).expand(2.0F, 1.0F, 2.0F)))
        {
            if (o instanceof EntityItem)
            {
                retList.add((EntityItem) o);
            }
        }

        retList.stream()
          .filter(Objects::nonNull)
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
                if (ticksExisted - it.next().getValue() > TICKS_20 * Configurations.Gameplay.chatFrequency)
                {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void setCustomNameTag(final String name)
    {
        if(citizenData != null && name != null)
        {
            if(!name.contains(citizenData.getName()) && Configurations.Gameplay.allowGlobalNameChanges >= 0)
            {
                if (Configurations.Gameplay.allowGlobalNameChanges == 0 &&
                        Arrays.stream(Configurations.Gameplay.specialPermGroup).noneMatch(owner -> owner.equals(colony.getPermissions().getOwnerName())))
                {
                    LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), CITIZEN_RENAME_NOT_ALLOWED);
                    return;
                }


                if (colony != null)
                {
                    for (final CitizenData citizen : colony.getCitizenManager().getCitizens())
                    {
                        if (citizen.getName().equals(name))
                        {
                            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), CITIZEN_RENAME_SAME);
                            return;
                        }
                    }
                    this.citizenData.setName(name);
                    this.citizenData.markDirty();
                    super.setCustomNameTag(name);
                }
                return;
            }
            super.setCustomNameTag(name);
        }
    }

    private void checkIfStuck()
    {
        if (this.currentPosition == null || newNavigator == null)
        {
            this.currentPosition = this.getPosition();
            return;
        }

        if (newNavigator.getDestination() == null || newNavigator.getDestination().distanceSq(posX, posY, posZ) < MOVE_AWAY_RANGE)
        {
            return;
        }

        if (!new AxisAlignedBB(this.currentPosition).expand(1, 1, 1)
               .intersectsWith(new AxisAlignedBB(this.getPosition())) && !triedMovingAway)
        {
            stuckTime = 0;
            this.currentPosition = this.getPosition();
            return;
        }

        stuckTime++;

        if (stuckTime >= MIN_STUCK_TIME + getRandom().nextInt(MIN_STUCK_TIME) && !triedMovingAway)
        {
            newNavigator.moveAwayFromXYZ(currentPosition, getRandom().nextInt(MOVE_AWAY_RANGE), 1);
            return;
        }

        if (stuckTime >= MAX_STUCK_TIME)
        {
            if (newNavigator.getDestination().distanceSq(posX, posY, posZ) < MOVE_AWAY_RANGE)
            {
                stuckTime = 0;
                return;
            }

            triedMovingAway = false;

            final BlockPos destination = BlockPosUtil.getFloor(newNavigator.getDestination(), CompatibilityUtils.getWorld(this));
            @Nullable final BlockPos spawnPoint =
              Utils.scanForBlockNearPoint
                      (CompatibilityUtils.getWorld(this), destination, 1, 1, 1, 3,
                        Blocks.AIR,
                        Blocks.SNOW_LAYER,
                        Blocks.TALLGRASS,
                        Blocks.RED_FLOWER,
                        Blocks.YELLOW_FLOWER,
                        Blocks.CARPET);

            WorkerUtil.setSpawnPoint(spawnPoint, this);
            if (colony != null)
            {
                Log.getLogger().info("Teleported stuck citizen " + this.getName() + " from colony: " + colony.getID() + " to target location");
            }
            stuckTime = 0;
        }

        this.currentPosition = this.getPosition();
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
     * Lets the citizen tryToEat to replentish saturation.
     */
    public void tryToEat()
    {
        final int slot = InventoryUtils.findFirstSlotInProviderWith(this,
          itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood);

        if (slot == -1)
        {
            return;
        }

        final ItemStack stack = getCitizenData().getInventory().getStackInSlot(slot);
        if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemFood && citizenData != null)
        {
            final int heal = ((ItemFood) stack.getItem()).getHealAmount(stack);
            citizenData.increaseSaturation(heal);
            getCitizenData().getInventory().decrStackSize(slot, 1);
            citizenData.markDirty();
        }
    }

    /**
     * Checks the citizens health status and heals the citizen if necessary.
     */
    private void checkHeal()
    {
        if (citizenData != null && getOffsetTicks() % HEAL_CITIZENS_AFTER == 0 && getHealth() < getMaxHealth())
        {
            int healAmount = 1;
            if (citizenData.getSaturation() >= FULL_SATURATION)
            {
                healAmount += 1;
            }
            else if (citizenData.getSaturation() < LOW_SATURATION)
            {
                healAmount = 0;
            }

            heal(healAmount);
            citizenData.markDirty();
        }
    }

    /**
     * Sets the textures of all citizens and distinguishes between male and
     * female.
     */
    private void setTexture()
    {
        if (!CompatibilityUtils.getWorld(this).isRemote)
        {
            return;
        }

        final RenderBipedCitizen.Model model = getModelID();

        String textureBase = "textures/entity/" + model.textureBase;
        textureBase += female ? "female" : "male";

        final int moddedTextureId = (textureId % model.numTextures) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + renderMetadata + ".png");
    }

    /**
     * Defines the area in which the citizen automatically gathers experience.
     *
     * @return a list of xp orbs around the entity.
     */
    private List<EntityXPOrb> getXPOrbsOnGrid()
    {
        @NotNull final AxisAlignedBB bb = new AxisAlignedBB(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2);

        return CompatibilityUtils.getWorld(this).getEntitiesWithinAABB(EntityXPOrb.class, bb);
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
        final AbstractBuilding home = getHomeBuilding();

        final double citizenHutLevel = home == null ? 0 : home.getBuildingLevel();
        final double citizenHutMaxLevel = home == null ? 1 : home.getMaxBuildingLevel();
        if (citizenData != null)
        {
            if (citizenHutLevel < citizenHutMaxLevel
                && Math.pow(2.0, citizenHutLevel + 1.0) <= this.citizenData.getLevel())
            {
                return;
            }

            final double maxValue = Integer.MAX_VALUE - citizenData.getExperience();
            double localXp = xp * skillModifier / EXP_DIVIDER;
            final double workBuildingLevel = getWorkBuilding() == null ? 0 : getWorkBuilding().getBuildingLevel();
            final double bonusXp = (workBuildingLevel * (1 + citizenHutLevel) / Math.log(this.citizenData.getLevel() + 2.0D)) / 2;
            localXp = localXp * bonusXp;
            final double saturation = citizenData.getSaturation();

            if (saturation < AVERAGE_SATURATION)
            {
                if (saturation <= 0)
                {
                    return;
                }

                if (saturation < LOW_SATURATION)
                {
                    localXp -= localXp * BIG_SATURATION_FACTOR * saturation;
                }
                else
                {
                    localXp -= localXp * LOW_SATURATION_FACTOR * saturation;
                }
            }
            else if (saturation > AVERAGE_SATURATION)
            {
                if (saturation > HIGH_SATURATION)
                {
                    localXp += localXp * BIG_SATURATION_FACTOR * saturation;
                }
                else
                {
                    localXp += localXp * LOW_SATURATION_FACTOR * saturation;
                }
            }

            if (localXp > maxValue)
            {
                localXp = maxValue;
            }

            localXp = applyMending(localXp);
            citizenData.addExperience(localXp);

            while (ExperienceUtils.getXPNeededForNextLevel(citizenData.getLevel()) < citizenData.getExperience())
            {
                citizenData.increaseLevel();
            }
            this.updateLevel();
            citizenData.markDirty();
        }
    }

    public RenderBipedCitizen.Model getModelID()
    {
        return modelId;
    }

    @Nullable
    private AbstractBuilding getHomeBuilding()
    {
        return (citizenData == null) ? null : citizenData.getHomeBuilding();
    }

    /**
     * repair random equipped/held item with mending enchant.
     *
     * @param xp amount of xp available to mend with
     * @return xp left after mending
     */
    private double applyMending(final double xp)
    {
        double localXp = xp;
        final ItemStack tool = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, this);

        if (tool != null && tool.isItemDamaged())
        {
            //2 xp to heal 1 dmg
            final double dmgHealed = Math.min(localXp / 2, tool.getItemDamage());
            localXp -= dmgHealed * 2;
            tool.setItemDamage(tool.getItemDamage() - (int) Math.ceil(dmgHealed));
        }

        return localXp;
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

        final CitizenData data = c.getCitizenManager().getCitizen(citizenId);
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
     * Updates the level of the citizen.
     */
    private void updateLevel()
    {
        level = citizenData == null ? 0 : citizenData.getLevel();
        dataManager.set(DATA_LEVEL, level);
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
     * Getter for the last job.
     *
     * @return the last job he had.
     */
    @NotNull
    public String getLastJob()
    {
        return this.lastJob;
    }

    /**
     * Sets the last job of the citizen.
     *
     * @param jobName the job he last had.
     */
    public void setLastJob(@NotNull final String jobName)
    {
        this.lastJob = jobName;
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

    @Override
    protected void updateEquipmentIfNeeded(final EntityItem itemEntity)
    {
        //Just do nothing!
    }

    /**
     * Drop the equipment for this entity.
     */
    @Override
    protected void dropEquipment(final boolean par1, final int par2)
    {
        //Drop actual inventory
        for (int i = 0; i < new InvWrapper(getInventoryCitizen()).getSlots(); i++)
        {
            final ItemStack itemstack = getCitizenData().getInventory().getStackInSlot(i);
            if (ItemStackUtils.getSize(itemstack) > 0)
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
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @NotNull
    public InventoryCitizen getInventoryCitizen()
    {
        return getCitizenData().getInventory();
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
        @Nullable final AbstractBuilding homeBuilding = getHomeBuilding();
        @Nullable final BlockPos homePosition = getHomePosition();

        if (homeBuilding instanceof BuildingHome)
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = homeBuilding.getCorners();
            return new AxisAlignedBB(corners.getFirst().getFirst(), posY - 1, corners.getSecond().getFirst(),
                    corners.getFirst().getSecond(),
                    posY + 1,
                    corners.getSecond().getSecond()).intersectsWithXZ(new Vec3d(this.getPosition()));
        }

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
        @Nullable final AbstractBuilding homeBuilding = getHomeBuilding();
        if (homeBuilding != null)
        {
            return homeBuilding.getLocation();
        }
        else if (getColony() != null && getColony().getBuildingManager().getTownHall() != null)
        {
            return getColony().getBuildingManager().getTownHall().getLocation();
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
        return InventoryUtils.isProviderFull(this);
    }

    @NotNull
    public DesiredActivity getDesiredActivity()
    {
        if (this.getColonyJob() instanceof JobGuard)
        {
            return DesiredActivity.WORK;
        }

        if (BarbarianUtils.getClosestBarbarianToEntity(this, AVOID_BARBARIAN_RANGE) != null && !(this.getColonyJob() instanceof JobGuard))
        {
            return DesiredActivity.SLEEP;
        }

        if (!CompatibilityUtils.getWorld(this).isDaytime())
        {
            if (isDay && citizenData != null)
            {
                isDay = false;
                final AbstractBuildingWorker buildingWorker = getWorkBuilding();
                final double decreaseBy = buildingWorker == null || buildingWorker.getBuildingLevel() == 0 ? 0.1
                                            : (SATURATION_DECREASE_FACTOR * Math.pow(2, buildingWorker.getBuildingLevel() - 1.0));
                citizenData.decreaseSaturation(decreaseBy);
                citizenData.markDirty();
            }

            setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.sleeping"));
            return DesiredActivity.SLEEP;
        }

        isDay = true;

        if (CompatibilityUtils.getWorld(this).isRaining() && !shouldWorkWhileRaining())
        {
            setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.waiting"), new TextComponentTranslation("com.minecolonies.coremod.status.rainStop"));
            return DesiredActivity.IDLE;
        }
        else
        {
            if (this.getNavigator() != null && (this.getNavigator().getPath() != null && this.getNavigator().getPath().getCurrentPathLength() == 0))
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
        return (this.getWorkBuilding() != null && (this.getWorkBuilding().getBuildingLevel() >= BONUS_BUILDING_LEVEL)) || Configurations.Gameplay.workersAlwaysWorkInRain;
    }

    /**
     * We override this method and execute no code to avoid citizens travelling
     * to the nether.
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

    @Override
    public EnumActionResult applyPlayerInteraction(final EntityPlayer player, final Vec3d vec, final ItemStack stack, final EnumHand hand)
    {
        SoundUtils.playInteractionSoundAtCitizenWithChance(CompatibilityUtils.getWorld(this), this.getPosition(), 100, this);
        return super.applyPlayerInteraction(player, vec, stack, hand);
    }

    /**
     * Returns the first slot in the inventory with a specific item.
     *
     * @param targetItem the item.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public int findFirstSlotInInventoryWith(final Item targetItem, final int itemDamage)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventoryCitizen()), targetItem, itemDamage);
    }

    /**
     * Returns the first slot in the inventory with a specific block.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public int findFirstSlotInInventoryWith(final Block block, final int itemDamage)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventoryCitizen()), block, itemDamage);
    }

    /**
     * Returns the amount of a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the quantity.
     */
    public int getItemCountInInventory(final Block block, final int itemDamage)
    {
        return InventoryUtils.getItemCountInItemHandler(new InvWrapper(getInventoryCitizen()), block, itemDamage);
    }

    /**
     * Returns the amount of a certain item in the inventory.
     *
     * @param targetItem the block.
     * @param itemDamage the damage value.
     * @return the quantity.
     */
    public int getItemCountInInventory(final Item targetItem, final int itemDamage)
    {
        return InventoryUtils.getItemCountInItemHandler(new InvWrapper(getInventoryCitizen()), targetItem, itemDamage);
    }

    /**
     * Checks if citizen has a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public boolean hasItemInInventory(final Block block, final int itemDamage)
    {
        return InventoryUtils.hasItemInItemHandler(new InvWrapper(getInventoryCitizen()), block, itemDamage);
    }

    /**
     * Checks if citizen has a certain item in the inventory.
     *
     * @param item       the item.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public boolean hasItemInInventory(final Item item, final int itemDamage)
    {
        return InventoryUtils.hasItemInItemHandler(new InvWrapper(getInventoryCitizen()), item, itemDamage);
    }

    /**
     * Citizen will try to pick up a certain item.
     *
     * @param entityItem the item he wants to pickup.
     */
    private void tryPickupEntityItem(@NotNull final EntityItem entityItem)
    {
        if (!CompatibilityUtils.getWorld(this).isRemote)
        {
            if (entityItem.cannotPickup())
            {
                return;
            }

            final ItemStack itemStack = entityItem.getEntityItem();
            final ItemStack compareStack = itemStack.copy();

            final ItemStack resultStack = InventoryUtils.addItemStackToItemHandlerWithResult(new InvWrapper(getInventoryCitizen()), itemStack);
            final int resultingStackSize = ItemStackUtils.isEmpty(resultStack) ? 0 : ItemStackUtils.getSize(resultStack);

            if (ItemStackUtils.isEmpty(resultStack) || ItemStackUtils.getSize(resultStack) != ItemStackUtils.getSize(compareStack))
            {
                CompatibilityUtils.getWorld(this).playSound((EntityPlayer) null,
                  this.getPosition(),
                  SoundEvents.ENTITY_ITEM_PICKUP,
                  SoundCategory.AMBIENT,
                  0.2F,
                  (float) ((this.rand.nextGaussian() * 0.7D + 1.0D) * 2.0D));
                this.onItemPickup(entityItem, ItemStackUtils.getSize(itemStack) - resultingStackSize);

                final ItemStack overrulingStack = itemStack.copy();
                overrulingStack.stackSize = ItemStackUtils.getSize(itemStack) - resultingStackSize;

                if (getColonyJob() != null)
                {
                    getColonyJob().onStackPickUp(overrulingStack);
                }

                if (ItemStackUtils.isEmpty(resultStack))
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
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
    }

    /**
     * Sets the currently held item.
     *
     * @param slot from the inventory slot.
     */
    public void setHeldItem(final int slot)
    {
        getCitizenData().getInventory().setHeldItem(slot);
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, getCitizenData().getInventory().getStackInSlot(slot));
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
     * If breakBlock is true then it will break the block (different sound and
     * particles), and damage the tool in the citizens hand.
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

        final IBlockState blockState = CompatibilityUtils.getWorld(this).getBlockState(blockPos);
        final Block block = blockState.getBlock();
        if (breakBlock)
        {
            if (!CompatibilityUtils.getWorld(this).isRemote)
            {
                MineColonies.getNetwork().sendToAllAround(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorld(this).getBlockState(blockPos), BlockParticleEffectMessage.BREAK_BLOCK),
                  new NetworkRegistry.TargetPoint(CompatibilityUtils.getWorld(this).provider.getDimension(),
                                                   blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_SOUND_RANGE));
            }
            CompatibilityUtils.getWorld(this).playSound(null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorld(this), blockPos, this).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, CompatibilityUtils.getWorld(this), blockPos, this).getVolume(),
              block.getSoundType(blockState, CompatibilityUtils.getWorld(this), blockPos, this).getPitch());
            CompatibilityUtils.getWorld(this).setBlockToAir(blockPos);

            damageItemInHand(1);
        }
        else
        {
            if (!CompatibilityUtils.getWorld(this).isRemote)
            {
                final BlockPos vector = blockPos.subtract(this.getPosition());
                final EnumFacing facing = EnumFacing.getFacingFromVector(vector.getX(), vector.getY(), vector.getZ()).getOpposite();

                MineColonies.getNetwork().sendToAllAround(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorld(this).getBlockState(blockPos), facing.ordinal()),
                  new NetworkRegistry.TargetPoint(CompatibilityUtils.getWorld(this).provider.getDimension(), blockPos.getX(),
                                                   blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_PARTICLE_RANGE));
            }
            CompatibilityUtils.getWorld(this).playSound((EntityPlayer) null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorld(this), blockPos, this).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, CompatibilityUtils.getWorld(this), blockPos, this).getVolume(),
              block.getSoundType(blockState, CompatibilityUtils.getWorld(this), blockPos, this).getPitch());
        }
    }

    //todo resolve problem if citizen don't get to replentish at home.

    /**
     * Damage the current held item.
     *
     * @param damage amount of damage.
     */
    public void damageItemInHand(final int damage)
    {
        final ItemStack heldItem = getCitizenData().getInventory().getHeldItemMainhand();
        //If we hit with bare hands, ignore
        if (heldItem == null)
        {
            return;
        }
        heldItem.damageItem(damage, this);

        //check if tool breaks
        if (ItemStackUtils.getSize(heldItem) < 1)
        {
            getInventoryCitizen().setInventorySlotContents(getInventoryCitizen().getHeldItemSlot(), ItemStackUtils.EMPTY);
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
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
     * Sends a localized message from the citizen containing a language string
     * with a key and arguments.
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

        if (msg.length == 0)
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

        final List<EntityPlayer> players = new ArrayList<>(colony.getMessageEntityPlayers());
        final EntityPlayer owner = ServerUtils.getPlayerFromUUID(CompatibilityUtils.getWorld(this), this.getColony().getPermissions().getOwner());
        if (owner != null)
        {
            players.remove(owner);
            LanguageHandler.sendPlayerMessage(owner,
              this.getColonyJob() == null ? "" : this.getColonyJob().getName(), citizenDescription, requiredItem);
        }

        LanguageHandler.sendPlayersMessage(players,
          this.getColonyJob() == null ? "" : this.getColonyJob().getName(), colonyDescription, citizenDescription, requiredItem);
    }

    /**
     * Set the skill modifier which defines how fast a citizen levels in a
     * certain skill.
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
        if (this.getWorkBuilding() != null)
        {
            setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.working"));
            this.getWorkBuilding().onWakeUp();
        }
        if(this.getColonyJob() != null)
        {
            this.getColonyJob().onWakeUp();
        }

        final AbstractBuilding homeBuilding = this.getHomeBuilding();
        if (homeBuilding != null)
        {
            homeBuilding.onWakeUp();
        }
    }

    /**
     * Play move away sound when running from an entity.
     */
    public void playMoveAwaySound()
    {
        if (getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorld(this), getPosition(),
              getColonyJob().getMoveAwaySound(), 1);
        }
    }

    /**
     * Get the path proxy of the citizen.
     * @return the proxy.
     */
    public IWalkToProxy getProxy()
    {
        return proxy;
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
