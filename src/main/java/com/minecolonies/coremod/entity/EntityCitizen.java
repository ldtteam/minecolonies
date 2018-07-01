package com.minecolonies.coremod.entity;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.minimal.*;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianUtils;
import com.minecolonies.coremod.entity.citizenhandlers.*;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import com.minecolonies.coremod.util.PermissionUtils;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.INCREMENT_AND_DECREMENT_OPERATORS_SHOULD_NOT_BE_USED_IN_A_METHOD_CALL_OR_MIXED_WITH_OTHER_OPERATORS_IN_AN_EXPRESSION;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;
import static com.minecolonies.api.util.constant.TranslationConstants.CITIZEN_RENAME_NOT_ALLOWED;
import static com.minecolonies.api.util.constant.TranslationConstants.CITIZEN_RENAME_SAME;

/**
 * The Class used to represent the citizen entities.
 */
public class EntityCitizen extends AbstractEntityCitizen
{
    /**
     * The navigator field of the citizen.
     */
    private static Field navigatorField;

    /**
     * The New PathNavigate navigator.
     */
    private final PathNavigate newNavigator;

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
        updateNavigatorField();
        if (CompatibilityUtils.getWorld(this).isRemote)
        {
            setRenderDistanceWeight(RENDER_DISTANCE_WEIGHT);
        }
        this.newNavigator.setCanSwim(true);
        this.newNavigator.setEnterDoors(true);
        initTasks();
    }

    /**
     * Method used to update the navigator field.
     * Gets the minecraft path navigate through reflection.
     */
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

        if (citizenJobHandler.getColonyJob() == null || !"com.minecolonies.coremod.job.Guard".equals(citizenJobHandler.getColonyJob().getName()))
        {
            this.tasks.addTask(++priority, new EntityAICitizenAvoidEntity(this, EntityMob.class, (float) DISTANCE_OF_ENTITY_AVOID, LATER_RUN_SPEED_AVOID, INITIAL_RUN_SPEED_AVOID));
        }
        this.tasks.addTask(++priority, new EntityAIGoHome(this));
        this.tasks.addTask(++priority, new EntityAISleep(this));
        this.tasks.addTask(++priority, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(priority, new EntityAIOpenFenceGate(this, true));
        this.tasks.addTask(++priority, new EntityAIWatchClosest2(this, EntityPlayer.class, WATCH_CLOSEST2, 1.0F));
        this.tasks.addTask(++priority, new EntityAIWatchClosest2(this, EntityCitizen.class, WATCH_CLOSEST2_FAR, WATCH_CLOSEST2_FAR_CHANCE));
        this.tasks.addTask(++priority, new EntityAICitizenWander(this, DEFAULT_SPEED));
        this.tasks.addTask(++priority, new EntityAIWatchClosest(this, EntityLiving.class, WATCH_CLOSEST));

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
    public boolean attackEntityFrom(@NotNull final DamageSource damageSource, final float damage)
    {
        final Entity sourceEntity = damageSource.getTrueSource();
        if (sourceEntity instanceof EntityCitizen && ((EntityCitizen) sourceEntity).citizenColonyHandler.getColonyId() == citizenColonyHandler.getColonyId())
        {
            return false;
        }
        setLastAttackedEntity(damageSource.getTrueSource());
        final boolean result = super.attackEntityFrom(damageSource, damage);

        if (damageSource.isMagicDamage() || damageSource.isFireDamage())
        {
            return result;
        }

        citizenItemHandler.updateArmorDamage(damage);

        return result;
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
            return (T) new InvWrapper(inv);
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
        double penalty = CITIZEN_DEATH_PENALTY;
        if (citizenColonyHandler.getColony() != null && getCitizenData()  != null)
        {
            if (damageSource.getTrueSource() instanceof EntityPlayer)
            {
                boolean isBarbarianClose = true;
                for(final AbstractEntityBarbarian barbarian : this.getCitizenColonyHandler().getColony().getBarbManager().getHorde())
                {
                    final EntityCitizen citizen = new EntityCitizen(this.getEntityWorld());
                    if(MathUtils.twoDimDistance(barbarian.getPosition(), citizen.getPosition()) < BARB_DISTANCE_FOR_FREE_DEATH)
                    {
                        isBarbarianClose = true;
                        break;
                    }
                    else
                    {
                        isBarbarianClose = false;
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
            citizenColonyHandler.getColony().decreaseOverallHappiness(penalty);
            triggerDeathAchievement(damageSource, citizenJobHandler.getColonyJob());
            citizenChatHandler.notifyDeath(damageSource);
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
     * @return the data.
     */
    @Nullable
    public CitizenData getCitizenData()
    {
        if (citizenData == null && citizenColonyHandler.getColony() != null)
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
        final ColonyView colonyView = ColonyManager.getColonyView(citizenColonyHandler.getColonyId());
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

        compound.setString(TAG_LAST_JOB, citizenJobHandler.getLastJob());
        compound.setBoolean(TAG_DAY, isDay);
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

        citizenJobHandler.setLastJob(compound.getString(TAG_LAST_JOB));
        isDay = compound.getBoolean(TAG_DAY);

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
                citizenStatusHandler.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.waitingForWork"));
            }

            if (CompatibilityUtils.getWorld(this).isDaytime() && !CompatibilityUtils.getWorld(this).isRaining() && citizenData != null)
            {
                SoundUtils.playRandomSound(CompatibilityUtils.getWorld(this), this, citizenData.getSaturation());
            }
            else if (CompatibilityUtils.getWorld(this).isRaining() && 1 >= rand.nextInt(RANT_ABOUT_WEATHER_CHANCE) && citizenJobHandler.getColonyJob() != null)
            {
                SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorld(this), this.getPosition(), citizenJobHandler.getColonyJob().getBadWeatherSound(), 1);
            }
        }

        if (isEntityInsideOpaqueBlock() || isInsideOfMaterial(Material.LEAVES))
        {
            getNavigator().moveAwayFromXYZ(this.getPosition(), MOVE_AWAY_RANGE, MOVE_AWAY_SPEED);
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

            if (citizenData.getSaturation() < HIGH_SATURATION)
            {
                tryToEat();
            }

            if((distanceWalkedModified + 1.0) % ACTIONS_EACH_BLOCKS_WALKED == 0)
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
     * Lets the citizen tryToEat to replentish saturation.
     */
    private void tryToEat()
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
     * Getter of the dataview, the clientside representation of the citizen.
     *
     * @return the view.
     */
    private CitizenDataView getCitizenDataView()
    {
        if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
        {
            final ColonyView colonyView = ColonyManager.getColonyView(citizenColonyHandler.getColonyId());
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

        if (BarbarianUtils.getClosestBarbarianToEntity(this, AVOID_BARBARIAN_RANGE) != null && !(citizenJobHandler.getColonyJob() instanceof AbstractJobGuard))
        {
            return DesiredActivity.SLEEP;
        }

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

        isDay = true;

        if (CompatibilityUtils.getWorld(this).isRaining() && !shouldWorkWhileRaining())
        {
            citizenStatusHandler.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.waiting"), new TextComponentTranslation("com.minecolonies.coremod.status.rainStop"));
            return DesiredActivity.IDLE;
        }
        else
        {
            if (this.getNavigator().getPath() != null && this.getNavigator().getPath().getCurrentPathLength() == 0)
            {
                this.getNavigator().clearPath();
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
        return (citizenColonyHandler.getWorkBuilding() != null && (citizenColonyHandler.getWorkBuilding().getBuildingLevel() >= BONUS_BUILDING_LEVEL))
                 || Configurations.gameplay.workersAlwaysWorkInRain
                 || (citizenJobHandler.getColonyJob() instanceof AbstractJobGuard);
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
     * @return the id.
     */
    public int getCitizenId()
    {
        return citizenId;
    }

    /**
     * Setter for the citizen id.
     * @param id the id to set.
     */
    public void setCitizenId(final int id)
    {
        this.citizenId = id;
    }

    /**
     * Setter for the citizen data.
     * @param data the data to set.
     */
    public void setCitizenData(@Nullable final CitizenData data)
    {
        this.citizenData = data;
    }

    /**
     * Getter for the current position.
     * @return the current position.
     */
    public BlockPos getCurrentPosition()
    {
        return currentPosition;
    }

    /**
     * Setter for the current position.
     * @param currentPosition the position to set.
     */
    public void setCurrentPosition(final BlockPos currentPosition)
    {
        this.currentPosition = currentPosition;
    }

    ///////// -------------------- The Handlers -------------------- /////////

    /**
     * The Handler for all experience related methods.
     * @return the instance of the handler.
     */
    public CitizenExperienceHandler getCitizenExperienceHandler()
    {
        return citizenExperienceHandler;
    }

    /**
     * The Handler for all chat related methods.
     * @return the instance of the handler.
     */
    public CitizenChatHandler getCitizenChatHandler()
    {
        return citizenChatHandler;
    }

    /**
     * The Handler for all status related methods.
     * @return the instance of the handler.
     */
    public CitizenStatusHandler getCitizenStatusHandler()
    {
        return citizenStatusHandler;
    }

    /**
     * The Handler for all item related methods.
     * @return the instance of the handler.
     */
    public CitizenItemHandler getCitizenItemHandler()
    {
        return citizenItemHandler;
    }

    /**
     * The Handler for all inventory related methods.
     * @return the instance of the handler.
     */
    public CitizenInventoryHandler getCitizenInventoryHandler()
    {
        return citizenInventoryHandler;
    }

    /**
     * The Handler for all colony related methods.
     * @return the instance of the handler.
     */
    public CitizenColonyHandler getCitizenColonyHandler()
    {
        return citizenColonyHandler;
    }

    /**
     * The Handler for all job related methods.
     * @return the instance of the handler.
     */
    public CitizenJobHandler getCitizenJobHandler()
    {
        return citizenJobHandler;
    }

    /**
     * The Handler for all job related methods.
     * @return the instance of the handler.
     */
    public CitizenSleepHandler getCitizenSleepHandler()
    {
        return citizenSleepHandler;
    }
}
