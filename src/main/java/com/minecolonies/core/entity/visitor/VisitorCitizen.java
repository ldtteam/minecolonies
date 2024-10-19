package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.entity.pathfinding.proxy.IWalkToProxy;
import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.entity.visitor.IVisitorType;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.WindowInteraction;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.core.entity.ai.minimal.EntityAIInteractToggleAble;
import com.minecolonies.core.entity.ai.minimal.LookAtEntityGoal;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.citizen.citizenhandlers.*;
import com.minecolonies.core.entity.pathfinding.navigation.MovementHandler;
import com.minecolonies.core.entity.pathfinding.proxy.EntityCitizenWalkToProxy;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CITIZEN;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.core.entity.ai.minimal.EntityAIInteractToggleAble.*;

/**
 * Visitor citizen entity
 */
public class VisitorCitizen extends AbstractEntityVisitor
{
    /**
     * The visitor type.
     */
    private final IVisitorType visitorType;

    /**
     * The citizen id.
     */
    private int citizenId = 0;

    /**
     * The Walk to proxy (Shortest path through intermediate blocks).
     */
    private IWalkToProxy proxy;

    /**
     * The location used for requests
     */
    private ILocation location = null;

    /**
     * Reference to the data representation inside the colony.
     */
    @Nullable
    private IVisitorData visitorData;

    /**
     * Citizen data view.
     */
    private ICitizenDataView citizenDataView;

    /**
     * The citizen item handler.
     */
    private ICitizenItemHandler citizenItemHandler;

    /**
     * The citizen inv handler.
     */
    private ICitizenInventoryHandler citizenInventoryHandler;

    /**
     * The citizen colony handler.
     */
    private ICitizenColonyHandler citizenColonyHandler;

    /**
     * The citizen job handler.
     */
    private ICitizenJobHandler citizenJobHandler;

    /**
     * The citizen sleep handler.
     */
    private ICitizenSleepHandler citizenSleepHandler;

    /**
     * The citizen experience handler
     */
    private ICitizenExperienceHandler citizenExperienceHandler;

    /**
     * The citizen disease handler.
     */
    private ICitizenDiseaseHandler citizenDiseaseHandler;

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type  the Entity type.
     * @param world the world.
     */
    public VisitorCitizen(final EntityType<? extends PathfinderMob> type, final Level world, final IVisitorType visitorType)
    {
        super(type, world);
        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.citizenItemHandler = new CitizenItemHandler(this);
        this.citizenInventoryHandler = new CitizenInventoryHandler(this);
        this.citizenColonyHandler = new VisitorColonyHandler(this);
        this.citizenJobHandler = new CitizenJobHandler(this);
        this.citizenSleepHandler = new CitizenSleepHandler(this);
        this.citizenExperienceHandler = new CitizenExperienceHandler(this);
        this.citizenDiseaseHandler = new CitizenDiseaseHandler(this);

        this.visitorType = visitorType;
        this.moveControl = new MovementHandler(this);
        this.setPersistenceRequired();
        this.setCustomNameVisible(MineColonies.getConfig().getServer().alwaysRenderNameTag.get());
        initTasks();
    }

    /**
     * Create a visitor citizen class for the given visitor type.
     *
     * @param visitorType the type of the visitor.
     * @return the visitor instance.
     */
    public static EntityType.EntityFactory<AbstractEntityVisitor> forVisitorType(final IVisitorType visitorType)
    {
        return (type, level) -> new VisitorCitizen(type, level, visitorType);
    }

    private void initTasks()
    {
        int priority = 0;
        this.goalSelector.addGoal(priority, new FloatGoal(this));
        this.goalSelector.addGoal(++priority, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(priority, new EntityAIInteractToggleAble(this, FENCE_TOGGLE, TRAP_TOGGLE, DOOR_TOGGLE));
        this.goalSelector.addGoal(++priority, new InteractGoal(this, Player.class, WATCH_CLOSEST2, 1.0F));
        this.goalSelector.addGoal(++priority, new InteractGoal(this, EntityCitizen.class, WATCH_CLOSEST2_FAR, WATCH_CLOSEST2_FAR_CHANCE));
        this.goalSelector.addGoal(++priority, new LookAtEntityGoal(this, LivingEntity.class, WATCH_CLOSEST));
        this.visitorType.createStateMachine(this);
    }

    @Override
    public boolean hurt(@NotNull final DamageSource damageSource, final float damage)
    {
        if (!(damageSource.getEntity() instanceof EntityCitizen) && super.hurt(damageSource, damage))
        {
            if (damageSource.getEntity() instanceof LivingEntity livingEntity && damage > 1.01F)
            {
                final IBuilding home = getCitizenData().getHomeBuilding();
                if (home != null && home.hasModule(BuildingModules.TAVERN_VISITOR))
                {
                    final TavernBuildingModule module = home.getModule(BuildingModules.TAVERN_VISITOR);
                    for (final Integer id : module.getExternalCitizens())
                    {
                        ICitizenData data = citizenColonyHandler.getColony().getVisitorManager().getCivilian(id);
                        if (data != null && data.getEntity().isPresent() && data.getEntity().get().getLastHurtByMob() == null)
                        {
                            data.getEntity().get().setLastHurtByMob(livingEntity);
                        }
                    }
                }

                final Entity sourceEntity = damageSource.getEntity();
                if (sourceEntity instanceof Player)
                {
                    if (sourceEntity instanceof ServerPlayer)
                    {
                        return damage <= 1 || getCitizenColonyHandler().getColony().getPermissions().hasPermission((Player) sourceEntity, Action.HURT_VISITOR);
                    }
                    else
                    {
                        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(getCitizenColonyHandler().getColonyId(), level.dimension());
                        return damage <= 1 || colonyView == null || colonyView.getPermissions().hasPermission((Player) sourceEntity, Action.HURT_VISITOR);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void die(DamageSource cause)
    {
        super.die(cause);
        if (!level.isClientSide())
        {
            visitorType.onDied(this, cause);
        }
    }

    @Override
    protected void dropEquipment()
    {
        //Drop actual inventory
        for (int i = 0; i < getInventoryCitizen().getSlots(); i++)
        {
            final ItemStack itemstack = getCitizenData().getInventory().getStackInSlot(i);
            if (ItemStackUtils.getSize(itemstack) > 0)
            {
                citizenItemHandler.entityDropItem(itemstack);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor)
    {
        super.onSyncedDataUpdated(dataAccessor);
        if (citizenColonyHandler != null)
        {
            citizenColonyHandler.onSyncDataUpdate(dataAccessor);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory playerInventory, final Player playerEntity)
    {
        return new ContainerCitizenInventory(id, playerInventory, citizenColonyHandler.getColonyId(), citizenId);
    }

    @Override
    public ICitizenDataView getCitizenDataView()
    {
        if (this.citizenDataView == null)
        {
            citizenColonyHandler.updateColonyClient();
            if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
                if (colonyView != null)
                {
                    this.citizenDataView = colonyView.getVisitor(citizenId);
                    return this.citizenDataView;
                }
            }
        }
        else
        {
            return this.citizenDataView;
        }

        return null;
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_COLONY_ID, citizenColonyHandler == null ? 0 : citizenColonyHandler.getColonyId());
        entityData.define(DATA_CITIZEN_ID, citizenId);
    }

    @Override
    public void aiStep()
    {
        super.aiStep();

        if (lastHurtByPlayerTime > 0)
        {
            markDirty(0);
        }

        if (CompatibilityUtils.getWorldFromCitizen(this).isClientSide)
        {
            citizenColonyHandler.updateColonyClient();
            if (citizenColonyHandler.getColonyId() != 0 && citizenId != 0 && getOffsetTicks() % TICKS_20 == 0)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), level.dimension());
                if (colonyView != null)
                {
                    this.citizenDataView = colonyView.getVisitor(citizenId);
                    getEntityData().set(DATA_STYLE, colonyView.getTextureStyleId());
                }
            }
        }
        else
        {
            citizenColonyHandler.registerWithColony(citizenColonyHandler.getColonyId(), citizenId);
            if (tickCount % 500 == 0)
            {
                this.setCustomNameVisible(MineColonies.getConfig().getServer().alwaysRenderNameTag.get());
            }
        }
    }

    @Override
    public ILocation getLocation()
    {
        if (location == null)
        {
            location = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ILOCATION, this);
        }
        return location;
    }

    /**
     * Checks if a worker is at his working site. If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    @Override
    public boolean isWorkerAtSiteWithMove(@NotNull final BlockPos site, final int range)
    {
        if (proxy == null)
        {
            proxy = new EntityCitizenWalkToProxy(this);
        }
        return proxy.walkToBlock(site, range, true);
    }

    @Override
    public IVisitorData getCitizenData()
    {
        return visitorData;
    }

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @Override
    @NotNull
    public InventoryCitizen getInventoryCitizen()
    {
        return getCitizenData().getInventory();
    }

    @Override
    @NotNull
    public IItemHandler getItemHandlerCitizen()
    {
        return getInventoryCitizen();
    }

    @Override
    public void setIsChild(final boolean isChild)
    {

    }

    @Override
    public void playMoveAwaySound()
    {

    }

    @Override
    public IWalkToProxy getProxy()
    {
        return proxy;
    }

    @Override
    public void decreaseSaturationForAction()
    {
        if (visitorData != null)
        {
            visitorData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost());
            visitorData.markDirty(20 * 20);
        }
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    @Override
    public void decreaseSaturationForContinuousAction()
    {
        if (visitorData != null)
        {
            visitorData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() / 100.0);
            visitorData.markDirty(20 * 60 * 2);
        }
    }

    @Override
    public ICitizenExperienceHandler getCitizenExperienceHandler()
    {
        return citizenExperienceHandler;
    }

    @Override
    public ICitizenItemHandler getCitizenItemHandler()
    {
        return citizenItemHandler;
    }

    @Override
    public ICitizenInventoryHandler getCitizenInventoryHandler()
    {
        return citizenInventoryHandler;
    }

    @Override
    public void setCitizenInventoryHandler(final ICitizenInventoryHandler citizenInventoryHandler)
    {
        this.citizenInventoryHandler = citizenInventoryHandler;
    }

    @Override
    public ICitizenColonyHandler getCitizenColonyHandler()
    {
        return citizenColonyHandler;
    }

    @Override
    public void setCitizenColonyHandler(final ICitizenColonyHandler citizenColonyHandler)
    {
        this.citizenColonyHandler = citizenColonyHandler;
    }

    @Override
    public ICitizenJobHandler getCitizenJobHandler()
    {
        return citizenJobHandler;
    }

    @Override
    public ICitizenSleepHandler getCitizenSleepHandler()
    {
        return citizenSleepHandler;
    }

    @Override
    public ICitizenDiseaseHandler getCitizenDiseaseHandler()
    {
        return citizenDiseaseHandler;
    }

    @Override
    public void setCitizenDiseaseHandler(final ICitizenDiseaseHandler citizenDiseaseHandler)
    {
        this.citizenDiseaseHandler = citizenDiseaseHandler;
    }

    @Override
    public float getRotationYaw()
    {
        return getYRot();
    }

    @Override
    public float getRotationPitch()
    {
        return getXRot();
    }

    @Override
    public boolean isDead()
    {
        return !isAlive();
    }

    @Override
    public void setCitizenSleepHandler(final ICitizenSleepHandler citizenSleepHandler)
    {

    }

    @Override
    public void setCitizenJobHandler(final ICitizenJobHandler citizenJobHandler)
    {
        this.citizenJobHandler = citizenJobHandler;
    }

    @Override
    public void setCitizenItemHandler(final ICitizenItemHandler citizenItemHandler)
    {
        this.citizenItemHandler = citizenItemHandler;
    }

    @Override
    public void setCitizenExperienceHandler(final ICitizenExperienceHandler citizenExperienceHandler)
    {
        this.citizenExperienceHandler = citizenExperienceHandler;
    }

    @Override
    public void callForHelp(final Entity attacker, final int guardHelpRange)
    {

    }

    @Override
    public void queueSound(final @NotNull SoundEvent soundEvent, final BlockPos pos, final int length, final int repetitions)
    {

    }

    @Override
    public void queueSound(final @NotNull SoundEvent soundEvent, final BlockPos pos, final int length, final int repetitions, final float volume, final float pitch)
    {

    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);

        compound.putInt(TAG_COLONY_ID, citizenColonyHandler.getColonyId());
        if (visitorData != null)
        {
            compound.putInt(TAG_CITIZEN, visitorData.getId());
        }

        citizenDiseaseHandler.write(compound);
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);

        if (compound.contains(TAG_COLONY_ID))
        {
            citizenColonyHandler.setColonyId(compound.getInt(TAG_COLONY_ID));
            if (compound.contains(TAG_CITIZEN))
            {
                citizenId = compound.getInt(TAG_CITIZEN);
                citizenColonyHandler.registerWithColony(citizenColonyHandler.getColonyId(), citizenId);
            }
        }

        citizenDiseaseHandler.read(compound);
    }

    /**
     * Called when a player tries to interact with a citizen.
     *
     * @param player which interacts with the citizen.
     * @return If citizen should interact or not.
     */
    @Override
    public InteractionResult checkAndHandleImportantInteractions(final Player player, @NotNull final InteractionHand hand)
    {
        final IColonyView iColonyView = IColonyManager.getInstance().getColonyView(citizenColonyHandler.getColonyId(), player.level.dimension());
        if (iColonyView != null && !iColonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return InteractionResult.FAIL;
        }

        if (!ItemStackUtils.isEmpty(player.getItemInHand(hand)) && player.getItemInHand(hand).getItem() instanceof NameTagItem)
        {
            return super.checkAndHandleImportantInteractions(player, hand);
        }

        final InteractionResult result = visitorType.onPlayerInteraction(this, player, level, hand);
        if (result.consumesAction())
        {
            return result;
        }

        if (CompatibilityUtils.getWorldFromCitizen(this).isClientSide)
        {
            if (player.isShiftKeyDown())
            {
                Network.getNetwork().sendToServer(new OpenInventoryMessage(iColonyView, this.getName().getString(), this.getId()));
            }
            else
            {
                final ICitizenDataView citizenDataView = getCitizenDataView();
                if (citizenDataView != null)
                {
                    new WindowInteraction(citizenDataView).open();
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public IVisitorData getCivilianData()
    {
        return visitorData;
    }

    @Override
    public void setCivilianData(@Nullable final ICivilianData data)
    {
        if (data instanceof IVisitorData newVisitorData)
        {
            this.visitorData = newVisitorData;
            data.initEntityValues();
        }
    }

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    @Override
    public void markDirty(final int time)
    {
        if (visitorData != null)
        {
            visitorData.markDirty(time);
        }
    }

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    @Override
    public int getCivilianID()
    {
        return citizenId;
    }

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    @Override
    public void setCitizenId(final int id)
    {
        this.citizenId = id;
    }

    @Override
    public void setRemoved(final RemovalReason reason)
    {
        citizenColonyHandler.onCitizenRemoved();
        super.setRemoved(reason);
    }
}