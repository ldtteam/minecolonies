package com.minecolonies.coremod.entity;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobStudent;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.minimal.*;
import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.entity.citizenhandlers.*;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import com.minecolonies.coremod.entity.pathfinding.PathResult;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import com.minecolonies.coremod.util.PermissionUtils;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Objects;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.ColonyConstants.TEAM_COLONY_NAME;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.INCREMENT_AND_DECREMENT_OPERATORS_SHOULD_NOT_BE_USED_IN_A_METHOD_CALL_OR_MIXED_WITH_OTHER_OPERATORS_IN_AN_EXPRESSION;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * The Class used to represent the citizen entities.
 */
public class EntityCitizen extends AbstractEntityCitizen
{
    /**
     * The New PathNavigate navigator.
     */
    private final PathNavigate newNavigator;

    /**
     * The path-result of trying to move away
     */
    private PathResult moveAwayPath;

    /**
     * It's citizen Id.
     */
    private int citizenId = 0;

    /**
     * The Walk to proxy (Shortest path through intermediate blocks).
     */
    private IWalkToProxy proxy;

    /**
     * Reference to the data representation inside the colony.
     */
    @Nullable
    private CitizenData citizenData;

    /**
     * The entities current Position.
     */
    private BlockPos currentPosition = null;

    /**
     * Variable to check what time it is for the citizen.
     */
    private boolean isDay = true;

    /**
     * Backup of the citizen.
     */
    private NBTTagCompound dataBackup = null;

    /**
     * The citizen experience handler.
     */
    private final CitizenExperienceHandler citizenExperienceHandler;

    /**
     * The citizen chat handler.
     */
    private final CitizenChatHandler citizenChatHandler;

    /**
     * The citizen status handler.
     */
    private final CitizenStatusHandler citizenStatusHandler;

    /**
     * The citizen item handler.
     */
    private final CitizenItemHandler citizenItemHandler;

    /**
     * The citizen inv handler.
     */
    private final CitizenInventoryHandler citizenInventoryHandler;

    /**
     * The citizen colony handler.
     */
    private final CitizenColonyHandler citizenColonyHandler;

    /**
     * The citizen job handler.
     */
    private final CitizenJobHandler citizenJobHandler;

    /**
     * The citizen sleep handler.
     */
    private final CitizenSleepHandler citizenSleepHandler;

    /**
     * The citizen stuck handler.
     */
    private final CitizenStuckHandler citizenStuckHandler;

    /**
     * Indicate if the citizen is mourning or not.
     */
    private boolean mourning = false;

    /**
     * Indicates if the citizen is hiding from the rain or not.
     */
    private boolean hidingFromRain = false;

    /**
     * IsChild flag
     */
    private boolean isChild = false;

    /**
     * Whether the citizen is currently running away
     */
    private boolean currentlyFleeing = false;

    /**
     * Timer for the call for help cd.
     */
    private int callForHelpCooldown = 0;

    /**
     * Cooldown for calling help, in ticks.
     */
    private static final int CALL_HELP_CD = 100;

    /**
     * Citizen inv Wrapper.
     */
    private IItemHandler invWrapper;

    /**
     * Citizen constructor.
     *
     * @param world the world the citizen lives in.
     */
    public EntityCitizen(final World world)
    {
        super(world);
        this.citizenExperienceHandler = new CitizenExperienceHandler(this);
        this.citizenChatHandler = new CitizenChatHandler(this);
        this.citizenStatusHandler = new CitizenStatusHandler(this);
        this.citizenItemHandler = new CitizenItemHandler(this);
        this.citizenInventoryHandler = new CitizenInventoryHandler(this);
        this.citizenColonyHandler = new CitizenColonyHandler(this);
        this.citizenJobHandler = new CitizenJobHandler(this);
        this.citizenSleepHandler = new CitizenSleepHandler(this);
        this.citizenStuckHandler = new CitizenStuckHandler(this);

        setSize((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT);
        this.enablePersistence();
        this.setAlwaysRenderNameTag(Configurations.gameplay.alwaysRenderNameTag);
        this.newNavigator = new PathNavigate(this, world);
        this.navigator = newNavigator;
        if (CompatibilityUtils.getWorld(this).isRemote)
        {
            setRenderDistanceWeight(RENDER_DISTANCE_WEIGHT);
        }
        this.newNavigator.setCanSwim(true);
        this.newNavigator.setEnterDoors(true);
        initTasks();
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
        if (citizenJobHandler.getColonyJob() == null || !"com.minecolonies.coremod.job.Guard".equals(citizenJobHandler.getColonyJob().getName()))
        {
            this.tasks.addTask(++priority, new EntityAICitizenAvoidEntity(this, EntityMob.class, (float) DISTANCE_OF_ENTITY_AVOID, LATER_RUN_SPEED_AVOID, INITIAL_RUN_SPEED_AVOID));
        }
        this.tasks.addTask(++priority, new EntityAIEatTask(this));
        this.tasks.addTask(++priority, new EntityAISleep(this));
        this.tasks.addTask(++priority, new EntityAIGoHome(this));
        this.tasks.addTask(++priority, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(priority, new EntityAIOpenFenceGate(this, true));
        this.tasks.addTask(++priority, new EntityAIWatchClosest2(this, EntityPlayer.class, WATCH_CLOSEST2, 1.0F));
        this.tasks.addTask(++priority, new EntityAIWatchClosest2(this, EntityCitizen.class, WATCH_CLOSEST2_FAR, WATCH_CLOSEST2_FAR_CHANCE));
        this.tasks.addTask(++priority, new EntityAICitizenWander(this, DEFAULT_SPEED, 1.0D));
        this.tasks.addTask(++priority, new EntityAIWatchClosest(this, EntityLiving.class, WATCH_CLOSEST));
        this.tasks.addTask(++priority, new EntityAIMournCitizen(this, DEFAULT_SPEED));

        citizenJobHandler.onJobChanged(citizenJobHandler.getColonyJob());
    }

    /**
     * Set the metadata for rendering.
     *
     * @param metadata the metadata required.
     */
    @Override
    public void setRenderMetadata(final String metadata)
    {
        super.setRenderMetadata(metadata);
        dataManager.set(DATA_RENDER_METADATA, getRenderMetadata());
        //Display some debug info always available while testing
        //tofo: remove this when in Beta!
        //Will help track down some hard to find bugs (Pathfinding etc.)
        if (citizenData != null)
        {
            if (citizenJobHandler.getColonyJob() != null && Configurations.gameplay.enableInDevelopmentFeatures)
            {
                setCustomNameTag(citizenData.getName() + " (" + citizenStatusHandler.getStatus() + ")[" + citizenJobHandler.getColonyJob().getNameTagDescription() + "]");
            }
            else
            {
                setCustomNameTag(citizenData.getName());
            }
        }
    }

    /**
     * Get the ILocation of the citizen.
     *
     * @return an ILocation object which contains the dimension and is unique.
     */
    public ILocation getLocation()
    {
        return StandardFactoryController.getInstance().getNewInstance(TypeConstants.ILOCATION, this);
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

    @Override
    public Team getTeam()
    {
        return this.world.getScoreboard().getTeam(TEAM_COLONY_NAME + this.getCitizenColonyHandler().getColonyId());
    }

    @Override
    public boolean attackEntityFrom(@NotNull final DamageSource damageSource, final float damage)
    {
        if (damageSource.getDamageType().equals(DamageSource.IN_WALL.getDamageType()) && citizenSleepHandler.isAsleep()
              || Compatibility.isDynTreePresent() && damageSource.damageType.equals(Compatibility.getDynamicTreeDamage()) || this.getIsInvulnerable())
        {
            return false;
        }

        final Entity sourceEntity = damageSource.getTrueSource();
        if (sourceEntity instanceof EntityCitizen)
        {
            if (((EntityCitizen) sourceEntity).citizenColonyHandler.getColonyId() == citizenColonyHandler.getColonyId())
            {
                return false;
            }

            final Colony attackerColony = ((EntityCitizen) sourceEntity).citizenColonyHandler.getColony();
            if (attackerColony != null && citizenColonyHandler.getColony() != null)
            {
                final Permissions permission = attackerColony.getPermissions();
                citizenColonyHandler.getColony().getPermissions().addPlayer(permission.getOwner(), permission.getOwnerName(), Rank.HOSTILE);
            }
        }

        if (sourceEntity instanceof EntityPlayer && getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
        {
            if (!AbstractBuildingGuards.checkIfGuardShouldTakeDamage(this, (EntityPlayer) sourceEntity))
            {
                return false;
            }
        }

        // Maxdmg cap so citizens need a certain amount of hits to die, so we get more gameplay value and less scaling issues.
        final float damageInc = damage > (getMaxHealth() * 0.2f) ? (getMaxHealth() * 0.2f) : damage;

        if (!world.isRemote)
        {
            performMoveAway(sourceEntity);
        }
        setLastAttackedEntity(damageSource.getTrueSource());
        final boolean result = super.attackEntityFrom(damageSource, damageInc);

        if (damageSource.isMagicDamage() || damageSource.isFireDamage())
        {
            return result;
        }

        citizenItemHandler.updateArmorDamage(damageInc);
        if (citizenData != null)
        {
            getCitizenData().getCitizenHappinessHandler().setDamageModifier();
        }

        return result;
    }

    /**
     * Run away from an attacker
     *
     * @param attacker the attacking Entity
     */
    private void performMoveAway(@Nullable final Entity attacker)
    {
        this.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.avoiding"));

        // Environmental damage
        if (!(attacker instanceof EntityLivingBase))
        {
            moveAwayPath = this.getNavigator().moveAwayFromEntityLiving(this, 5, INITIAL_RUN_SPEED_AVOID);
            return;
        }

        // Makes the avoidance AI take over.
        currentlyFleeing = true;

        if ((getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard))
        {
            // 30 Blocks range
            callForHelp(attacker, 900);
            return;
        }
        else
        {
            callForHelp(attacker, MAX_GUARD_CALL_RANGE);
        }

        moveAwayPath = this.getNavigator().moveAwayFromEntityLiving(attacker, 15, INITIAL_RUN_SPEED_AVOID);
    }

    /**
     * Calls a guard for help against an attacker.
     *
     * @param attacker         the attacking entity
     * @param guardHelpRange   the squaredistance in which we search for nearby guards
     */
    public void callForHelp(final Entity attacker, final int guardHelpRange)
    {
        if (!(attacker instanceof EntityLivingBase) || !Configurations.gameplay.citizenCallForHelp || callForHelpCooldown != 0)
        {
            return;
        }

        callForHelpCooldown = CALL_HELP_CD;

        long guardDistance = guardHelpRange;
        EntityCitizen guard = null;

        for (final CitizenData entry : getCitizenColonyHandler().getColony().getCitizenManager().getCitizens())
        {
            if (entry.getCitizenEntity().isPresent())
            {
                final long tdist = BlockPosUtil.getDistanceSquared(entry.getCitizenEntity().get().getPosition(), getPosition());

                // Checking for guard nearby
                if (entry.getJob() instanceof AbstractJobGuard)
                {
                    if (tdist < guardDistance && ((AbstractEntityAIGuard) entry.getJob().getWorkerAI()).canHelp())
                    {
                        guardDistance = tdist;
                        guard = entry.getCitizenEntity().get();
                    }
                }
            }
        }

        if (guard != null)
        {
            ((AbstractEntityAIGuard) guard.getCitizenData().getJob().getWorkerAI()).startHelpCitizen(this, (EntityLivingBase) attacker);
        }
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public <T> T getCapability(@NotNull final Capability<T> capability, final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            final CitizenData data = getCitizenData();
            if (data == null)
            {
                return super.getCapability(capability, facing);
            }
            final InventoryCitizen inv = data.getInventory();

            if (invWrapper == null)
            {
                invWrapper = new InvWrapper(inv);
            }

            return (T) invWrapper;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(@NotNull final Capability<?> capability, final EnumFacing facing)
    {
        if (getCitizenData() == null)
        {
            return super.hasCapability(capability, facing);
        }

        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    protected void damageShield(final float damage)
    {
        if (getHeldItem(getActiveHand()).getItem() instanceof ItemShield)
        {
            citizenItemHandler.damageItemInHand(this.getActiveHand(), (int) damage);
        }
        super.damageShield(damage);
    }

    /**
     * Called when the mob's health reaches 0.
     *
     * @param damageSource the attacking entity.
     */
    @Override
    public void onDeath(@NotNull final DamageSource damageSource)
    {
        currentlyFleeing = false;
        double penalty = CITIZEN_DEATH_PENALTY;
        if (citizenColonyHandler.getColony() != null && getCitizenData() != null)
        {
            if (damageSource.getTrueSource() instanceof EntityPlayer && !world.isRemote)
            {
                boolean isBarbarianClose = false;
                for (final AbstractEntityMinecoloniesMob barbarian : this.getCitizenColonyHandler().getColony().getRaiderManager().getHorde((WorldServer) world))
                {
                    if (MathUtils.twoDimDistance(barbarian.getPosition(), this.getPosition()) < BARB_DISTANCE_FOR_FREE_DEATH)
                    {
                        isBarbarianClose = true;
                        break;
                    }
                }
                for (final Player player : PermissionUtils.getPlayersWithAtLeastRank(citizenColonyHandler.getColony(), Rank.OFFICER))
                {
                    if (player.getID().equals(damageSource.getTrueSource().getUniqueID()) && !isBarbarianClose)
                    {
                        penalty = CITIZEN_KILL_PENALTY;
                        break;
                    }
                }
            }

            citizenExperienceHandler.dropExperience();
            this.setDead();
            citizenColonyHandler.getColony().getHappinessData().setDeathModifier(penalty, citizenJobHandler.getColonyJob() instanceof AbstractJobGuard);
            triggerDeathAchievement(damageSource, citizenJobHandler.getColonyJob());
            citizenChatHandler.notifyDeath(damageSource);
            if (!(citizenJobHandler.getColonyJob() instanceof AbstractJobGuard)
                  && (damageSource != DamageSource.IN_WALL))
            {
                citizenColonyHandler.getColony().setNeedToMourn(true, citizenData.getName());
            }
            citizenColonyHandler.getColony().getCitizenManager().removeCitizen(getCitizenData());
        }
        super.onDeath(damageSource);
    }

    /**
     * Trigger the corresponding death achievement.
     *
     * @param source The damage source.
     * @param job    The job of the citizen.
     */
    private void triggerDeathAchievement(final DamageSource source, final AbstractJob job)
    {
        // If the job is null, then we can trigger jobless citizen achievement
        if (job != null)
        {
            job.triggerDeathAchievement(source, this);
        }
    }

    /**
     * Getter for the citizendata.
     * Tries to get it from the colony is the data is null.
     *
     * @return the data.
     */
    @Nullable
    public CitizenData getCitizenData()
    {
        if (citizenData == null && citizenColonyHandler != null && citizenColonyHandler.getColony() != null)
        {
            final CitizenData data = citizenColonyHandler.getColony().getCitizenManager().getCitizen(citizenId);
            if (data != null)
            {
                citizenData = data;
            }
        }
        return citizenData;
    }

    /**
     * Called when a player tries to interact with a citizen.
     *
     * @param player which interacts with the citizen.
     * @return If citizen should interact or not.
     */
    @Override
    public boolean processInteract(final EntityPlayer player, @NotNull final EnumHand hand)
    {
        final ColonyView colonyView = ColonyManager.getColonyView(citizenColonyHandler.getColonyId(), player.world.provider.getDimension());
        if (colonyView != null && !colonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return false;
        }

        if (!ItemStackUtils.isEmpty(player.getHeldItem(hand)) && player.getHeldItem(hand).getItem() instanceof ItemNameTag)
        {
            return super.processInteract(player, hand);
        }

        if (CompatibilityUtils.getWorld(this).isRemote)
        {
            if (player.isSneaking())
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
        dataManager.register(DATA_COLONY_ID, citizenColonyHandler == null ? 0 : citizenColonyHandler.getColonyId());
        dataManager.register(DATA_CITIZEN_ID, citizenId);
    }

    @Override
    public void writeEntityToNBT(final NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger(TAG_STATUS, citizenStatusHandler.getStatus().ordinal());
        if (citizenColonyHandler.getColony() != null && citizenData != null)
        {
            compound.setInteger(TAG_COLONY_ID, citizenColonyHandler.getColony().getID());
            compound.setInteger(TAG_CITIZEN, citizenData.getId());
        }

        compound.setBoolean(TAG_DAY, isDay);
        compound.setBoolean(TAG_CHILD, isChild);
        compound.setBoolean(TAG_MOURNING, mourning);
    }

    @Override
    public void readEntityFromNBT(final NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        citizenStatusHandler.setStatus(Status.values()[compound.getInteger(TAG_STATUS)]);
        citizenColonyHandler.setColonyId(compound.getInteger(TAG_COLONY_ID));
        citizenId = compound.getInteger(TAG_CITIZEN);

        if (isServerWorld())
        {
            citizenColonyHandler.updateColonyServer();
        }

        isDay = compound.getBoolean(TAG_DAY);
        setIsChild(compound.getBoolean(TAG_CHILD));

        if (compound.hasKey(TAG_MOURNING))
        {
            mourning = compound.getBoolean(TAG_MOURNING);
        }

        if (compound.hasKey(TAG_HELD_ITEM_SLOT) || compound.hasKey(TAG_OFFHAND_HELD_ITEM_SLOT))
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
        super.onLivingUpdate();

        if (callForHelpCooldown > 0)
        {
            callForHelpCooldown--;
        }

        if (recentlyHit > 0)
        {
            markDirty();
        }
        if (CompatibilityUtils.getWorld(this).isRemote)
        {
            citizenColonyHandler.updateColonyClient();
        }
        else
        {
            if (getOffsetTicks() % TICKS_20 == 0)
            {
                final ItemStack hat = getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                if (LocalDate.now(Clock.systemDefaultZone()).getMonth() == Month.DECEMBER
                      && Configurations.gameplay.holidayFeatures
                      && !(getCitizenJobHandler().getColonyJob() instanceof JobStudent))
                {
                    if (hat.isEmpty())
                    {
                        this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.santaHat));
                    }
                }
                else if (!hat.isEmpty() && hat.getItem() == ModItems.santaHat)
                {
                    this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStackUtils.EMPTY);
                }

                this.setAlwaysRenderNameTag(Configurations.gameplay.alwaysRenderNameTag);
                citizenItemHandler.pickupItems();
                citizenChatHandler.cleanupChatMessages();
                citizenColonyHandler.updateColonyServer();

                if (citizenData != null)
                {
                    citizenData.setLastPosition(getPosition());
                }
            }

            if (citizenJobHandler.getColonyJob() != null || !CompatibilityUtils.getWorld(this).isDaytime())
            {
                citizenStuckHandler.onUpdate();
            }
            else
            {
                if (isMourning())
                {
                    citizenStatusHandler.setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_MOURN));
                }
                else
                {
                    citizenStatusHandler.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.waitingForWork"));
                }
            }

            if (CompatibilityUtils.getWorld(this).isDaytime() && !CompatibilityUtils.getWorld(this).isRaining() && citizenData != null)
            {
                SoundUtils.playRandomSound(CompatibilityUtils.getWorld(this), this, citizenData.getSaturation());
            }
            else if (citizenStatusHandler.getStatus() != Status.SLEEPING && CompatibilityUtils.getWorld(this).isRaining() && 1 >= rand.nextInt(RANT_ABOUT_WEATHER_CHANCE)
                       && citizenJobHandler.getColonyJob() != null)
            {
                SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorld(this), this.getPosition(), citizenJobHandler.getColonyJob().getBadWeatherSound(), 1);
            }
        }

        if ((isEntityInsideOpaqueBlock() || isInsideOfMaterial(Material.LEAVES)) && (moveAwayPath == null || !moveAwayPath.isInProgress()))
        {
            moveAwayPath = getNavigator().moveAwayFromXYZ(this.getPosition(), MOVE_AWAY_RANGE, MOVE_AWAY_SPEED);
        }

        citizenExperienceHandler.gatherXp();
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

            if (citizenData.getSaturation() >= HIGH_SATURATION)
            {
                citizenData.getCitizenHappinessHandler().setSaturated();
            }

            if ((distanceWalkedModified + 1.0) % ACTIONS_EACH_BLOCKS_WALKED == 0)
            {
                decreaseSaturationForAction();
            }
        }

        if (dataBackup != null)
        {
            final NBTTagList nbttaglist = dataBackup.getTagList("Inventory", 10);
            this.getCitizenData().getInventory().readFromNBT(nbttaglist);
            if (dataBackup.hasKey(TAG_HELD_ITEM_SLOT))
            {
                this.getCitizenData().getInventory().setHeldItem(EnumHand.MAIN_HAND, dataBackup.getInteger(TAG_HELD_ITEM_SLOT));
            }

            if (dataBackup.hasKey(TAG_OFFHAND_HELD_ITEM_SLOT))
            {
                this.getCitizenData().getInventory().setHeldItem(EnumHand.OFF_HAND, dataBackup.getInteger(TAG_OFFHAND_HELD_ITEM_SLOT));
            }

            dataBackup = null;
        }

        checkHeal();
    }

    @Override
    public void setCustomNameTag(@NotNull final String name)
    {
        if (citizenData != null && citizenColonyHandler.getColony() != null)
        {
            if (!name.contains(citizenData.getName()) && Configurations.gameplay.allowGlobalNameChanges >= 0)
            {
                if (Configurations.gameplay.allowGlobalNameChanges == 0 &&
                      Arrays.stream(Configurations.gameplay.specialPermGroup).noneMatch(owner -> owner.equals(citizenColonyHandler.getColony().getPermissions().getOwnerName())))
                {
                    LanguageHandler.sendPlayersMessage(citizenColonyHandler.getColony().getMessageEntityPlayers(), CITIZEN_RENAME_NOT_ALLOWED);
                    return;
                }


                if (citizenColonyHandler.getColony() != null)
                {
                    for (final CitizenData citizen : citizenColonyHandler.getColony().getCitizenManager().getCitizens())
                    {
                        if (citizen.getName().equals(name))
                        {
                            LanguageHandler.sendPlayersMessage(citizenColonyHandler.getColony().getMessageEntityPlayers(), CITIZEN_RENAME_SAME);
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
     * Applies healthmodifiers for Guards based on level
     */
    public void increaseHPForGuards()
    {
        if (getCitizenData() != null)
        {
            // Remove old mod first
            removeHealthModifier(GUARD_HEALTH_MOD_LEVEL_NAME);

            // +1 Heart on levels 6,12,18,25,34,43,54 ...
            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME, (int) (getCitizenData().getLevel() / (5.0 + getCitizenData().getLevel() / 20.0) * 2), 0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(healthModLevel);
        }
    }

    /**
     * Remove all healthmodifiers from a citizen
     */
    public void removeAllHealthModifiers()
    {
        for (final AttributeModifier mod : getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifiers())
        {
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(mod);
        }
        if (getHealth() > getMaxHealth())
        {
            setHealth(getMaxHealth());
        }
    }

    /**
     * Remove healthmodifier by name.
     *
     * @param modifierName Name of the modifier to remove, see e.g. GUARD_HEALTH_MOD_LEVEL_NAME
     */
    public void removeHealthModifier(final String modifierName)
    {
        for (final AttributeModifier mod : getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifiers())
        {
            if (mod.getName().equals(modifierName))
            {
                getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(mod);
            }
        }
        if (getHealth() > getMaxHealth())
        {
            setHealth(getMaxHealth());
        }
    }

    /**
     * Getter of the dataview, the clientside representation of the citizen.
     *
     * @return the view.
     */
    private CitizenDataView getCitizenDataView()
    {
        if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
        {
            final ColonyView colonyView = ColonyManager.getColonyView(citizenColonyHandler.getColonyId(), world.provider.getDimension());
            if (colonyView != null)
            {
                return colonyView.getCitizen(citizenId);
            }
        }

        return null;
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
        for (int i = 0; i < new InvWrapper(getInventoryCitizen()).getSlots(); i++)
        {
            final ItemStack itemstack = getCitizenData().getInventory().getStackInSlot(i);
            if (ItemStackUtils.getSize(itemstack) > 0)
            {
                citizenItemHandler.entityDropItem(itemstack);
            }
        }
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

    @NotNull
    public IItemHandler getItemHandlerCitizen()
    {
        return new InvWrapper(getInventoryCitizen());
    }

    /**
     * Returns the home position of each citizen (His house or town hall).
     *
     * @return location
     */
    @NotNull
    @Override
    public BlockPos getHomePosition()
    {
        @Nullable final AbstractBuilding homeBuilding = citizenColonyHandler.getHomeBuilding();
        if (homeBuilding != null)
        {
            return homeBuilding.getLocation();
        }
        else if (citizenColonyHandler.getColony() != null && citizenColonyHandler.getColony().getBuildingManager().getTownHall() != null)
        {
            return citizenColonyHandler.getColony().getBuildingManager().getTownHall().getLocation();
        }

        return super.getHomePosition();
    }

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    public void markDirty()
    {
        if (citizenData != null)
        {
            citizenData.markDirty();
        }
    }

    @NotNull
    public DesiredActivity getDesiredActivity()
    {
        if (citizenJobHandler.getColonyJob() instanceof AbstractJobGuard)
        {
            return DesiredActivity.WORK;
        }

        if (getCitizenColonyHandler().getColony() != null && (getCitizenColonyHandler().getColony().isMourning() && mourning))
        {
            return DesiredActivity.MOURN;
        }

        if (getCitizenColonyHandler().getColony() != null && !world.isRemote && (!getCitizenColonyHandler().getColony().getRaiderManager().getHorde((WorldServer) world).isEmpty()))
        {
            isDay = false;
            return DesiredActivity.SLEEP;
        }

        // Random delay of 60 seconds to detect a new day/night/rain/sun
        if (Colony.shallUpdate(world, TICKS_SECOND * SECONDS_A_MINUTE))
        {
            if (!CompatibilityUtils.getWorld(this).isDaytime())
            {
                if (isDay && citizenData != null)
                {
                    isDay = false;
                    final double decreaseBy = citizenColonyHandler.getPerBuildingFoodCost() * 2;
                    citizenData.decreaseSaturation(decreaseBy);
                    citizenData.markDirty();
                }

                citizenStatusHandler.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.sleeping"));
                return DesiredActivity.SLEEP;
            }


            if (citizenSleepHandler.isAsleep())
            {
                citizenSleepHandler.onWakeUp();
            }
            isDay = true;


            if (CompatibilityUtils.getWorld(this).isRaining() && !shouldWorkWhileRaining())
            {
                hidingFromRain = true;
                citizenStatusHandler.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.waiting"),
                  new TextComponentTranslation("com.minecolonies.coremod.status.rainStop"));
                return DesiredActivity.IDLE;
            }
            else
            {
                hidingFromRain = false;
                if (this.getNavigator().getPath() != null && this.getNavigator().getPath().getCurrentPathLength() == 0)
                {
                    this.getNavigator().clearPath();
                }
                return DesiredActivity.WORK;
            }
        }
        return isDay ? hidingFromRain ? DesiredActivity.IDLE : DesiredActivity.WORK : DesiredActivity.SLEEP;
    }

    /**
     * Checks if the citizen should work even when it rains.
     *
     * @return true if his building level is bigger than 5.
     */
    private boolean shouldWorkWhileRaining()
    {
        return Configurations.gameplay.workersAlwaysWorkInRain ||
                 (citizenColonyHandler.getWorkBuilding() != null && citizenColonyHandler.getWorkBuilding().canWorkDuringTheRain());
    }

    /**
     * Sets the size of the citizen entity
     *
     * @param width  Width
     * @param height Height
     */
    public void setCitizensize(final @NotNull float width, final @NotNull float height)
    {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isChild()
    {
        return isChild;
    }

    /**
     * Sets whether this entity is a child
     *
     * @param isChild boolean
     */
    public void setIsChild(final boolean isChild)
    {
        if (isChild && !this.isChild)
        {
            tasks.addTask(50, new EntityAICitizenChild(this));
            setCitizensize((float) CITIZEN_WIDTH / 2, (float) CITIZEN_HEIGHT / 2);
        }
        else
        {
            setCitizensize((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT);
        }
        this.isChild = isChild;
        this.getDataManager().set(DATA_IS_CHILD, isChild);
        markDirty();
    }

    /**
     * Play move away sound when running from an entity.
     */
    public void playMoveAwaySound()
    {
        if (citizenJobHandler.getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorld(this), getPosition(),
              citizenJobHandler.getColonyJob().getMoveAwaySound(), 1);
        }
    }

    /**
     * Get the path proxy of the citizen.
     *
     * @return the proxy.
     */
    public IWalkToProxy getProxy()
    {
        return proxy;
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    public void decreaseSaturationForAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost());
            citizenData.markDirty();
        }
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    public void decreaseSaturationForContinuousAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() / 100.0);
            citizenData.markDirty();
        }
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof EntityCitizen)
        {
            final EntityCitizen citizen = (EntityCitizen) obj;
            return citizen.citizenColonyHandler.getColonyId() == this.citizenColonyHandler.getColonyId() && citizen.citizenId == this.citizenId;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (citizenColonyHandler == null)
        {
            return super.hashCode();
        }
        return Objects.hash(citizenId, citizenColonyHandler.getColonyId());
    }

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    public int getCitizenId()
    {
        return citizenId;
    }

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    public void setCitizenId(final int id)
    {
        this.citizenId = id;
    }

    /**
     * Setter for the citizen data.
     *
     * @param data the data to set.
     */
    public void setCitizenData(@Nullable final CitizenData data)
    {
        this.citizenData = data;
    }

    /**
     * Getter for the current position.
     * Only approximated position, used for stuck checking.
     *
     * @return the current position.
     */
    public BlockPos getCurrentPosition()
    {
        return currentPosition;
    }

    /**
     * Setter for the current position.
     *
     * @param currentPosition the position to set.
     */
    public void setCurrentPosition(final BlockPos currentPosition)
    {
        this.currentPosition = currentPosition;
    }

    /**
     * Spawn eating particles for the citizen.
     */
    public void spawnEatingParticle()
    {
        updateItemUse(getHeldItemMainhand(), EATING_PARTICLE_COUNT);
    }

    ///////// -------------------- The Handlers -------------------- /////////

    /**
     * The Handler for all experience related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenExperienceHandler getCitizenExperienceHandler()
    {
        return citizenExperienceHandler;
    }

    /**
     * The Handler for all chat related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenChatHandler getCitizenChatHandler()
    {
        return citizenChatHandler;
    }

    /**
     * The Handler for all status related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenStatusHandler getCitizenStatusHandler()
    {
        return citizenStatusHandler;
    }

    /**
     * The Handler for all item related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenItemHandler getCitizenItemHandler()
    {
        return citizenItemHandler;
    }

    /**
     * The Handler for all inventory related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenInventoryHandler getCitizenInventoryHandler()
    {
        return citizenInventoryHandler;
    }

    /**
     * The Handler for all colony related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenColonyHandler getCitizenColonyHandler()
    {
        return citizenColonyHandler;
    }

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenJobHandler getCitizenJobHandler()
    {
        return citizenJobHandler;
    }

    /**
     * The Handler for all job related methods.
     *
     * @return the instance of the handler.
     */
    public CitizenSleepHandler getCitizenSleepHandler()
    {
        return citizenSleepHandler;
    }

    /**
     * The Handler to check if a citizen is stuck.
     *
     * @return the instance of the handler.
     */
    public CitizenStuckHandler getCitizenStuckHandler()
    {
        return citizenStuckHandler;
    }

    /**
     * Check if the citizen can eat now by considering the state and the job tasks.
     *
     * @return true if so.
     */
    public boolean isOkayToEat()
    {
        return !getCitizenSleepHandler().isAsleep() && getDesiredActivity() != DesiredActivity.SLEEP && (citizenJobHandler.getColonyJob() == null
                                                                                                           || citizenJobHandler.getColonyJob().isOkayToEat());
    }

    /**
     * Check if the citizen can be fed.
     *
     * @return true if so.
     */
    public boolean shouldBeFed()
    {
        return this.getCitizenData() != null && this.getCitizenData().getSaturation() <= CitizenConstants.AVERAGE_SATURATION && !this.getCitizenData().justAte();
    }

    /**
     * Check if the citizen is just idling at their job and can eat now.
     *
     * @return true if so.
     */
    public boolean isIdlingAtJob()
    {
        return isOkayToEat() && (citizenJobHandler.getColonyJob() == null || citizenJobHandler.getColonyJob().isIdling());
    }

    /**
     * Call this to set if the citizen should mourn or not.
     *
     * @param mourning indicate if the citizen should mourn
     */
    public void setMourning(final boolean mourning)
    {
        this.mourning = mourning;
    }

    /**
     * Returns a value that indicate if the citizen is in mourning.
     *
     * @return indicate if the citizen is mouring
     */
    public boolean isMourning()
    {
        return mourning;
    }

    /**
     * Get if the citizen is fleeing from an attacker.
     */
    public boolean isCurrentlyFleeing()
    {
        return currentlyFleeing;
    }

    /**
     * Sets the fleeing state
     *
     * @param fleeing true if fleeing.
     */
    public void setFleeingState(final boolean fleeing)
    {
        currentlyFleeing = fleeing;
    }

    /**
     * Overrides the default despawning which is true.
     *
     * @return false
     */
    @Override
    protected boolean canDespawn()
    {
        return false;
    }
}
