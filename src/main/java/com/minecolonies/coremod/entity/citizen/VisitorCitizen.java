package com.minecolonies.coremod.entity.citizen;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.coremod.entity.ai.minimal.EntityAIInteractToggleAble;
import com.minecolonies.coremod.entity.ai.minimal.EntityAIVisitor;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.*;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.MovementHandler;
import com.minecolonies.coremod.network.messages.client.ItemParticleEffectMessage;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAIInteractToggleAble.*;

/**
 * Visitor citizen entity
 */
public class VisitorCitizen extends AbstractEntityCitizen
{
    /**
     * The citizen experience handler
     */
    private ICitizenExperienceHandler citizenExperienceHandler;

    /**
     * The citizen status handler.
     */
    private ICitizenStatusHandler citizenStatusHandler;
    /**
     * It's citizen Id.
     */
    private int                   citizenId = 0;
    /**
     * The Walk to proxy (Shortest path through intermediate blocks).
     */
    private IWalkToProxy          proxy;
    /**
     * Reference to the data representation inside the colony.
     */
    @Nullable
    private ICitizenData          citizenData;

    /**
     * The citizen chat handler.
     */
    private ICitizenChatHandler      citizenChatHandler;
    /**
     * The citizen item handler.
     */
    private ICitizenItemHandler      citizenItemHandler;
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
    private ICitizenJobHandler    citizenJobHandler;

    /**
     * The citizen sleep handler.
     */
    private ICitizenSleepHandler citizenSleepHandler;

    /**
     * Whether the citizen is currently running away
     */
    private boolean currentlyFleeing = false;

    /**
     * Citizen data view.
     */
    private ICitizenDataView citizenDataView;

    /**
     * The location used for requests
     */
    private ILocation              location = null;
    private ICitizenDiseaseHandler citizenDiseaseHandler;

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type  the Entity type.
     * @param world the world.
     */
    public VisitorCitizen(final EntityType<? extends AgeableMob> type, final Level world)
    {
        super(type, world);
        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.citizenChatHandler = new CitizenChatHandler(this);
        this.citizenStatusHandler = new CitizenStatusHandler(this);
        this.citizenItemHandler = new CitizenItemHandler(this);
        this.citizenInventoryHandler = new CitizenInventoryHandler(this);
        this.citizenColonyHandler = new VisitorColonyHandler(this);
        this.citizenJobHandler = new CitizenJobHandler(this);
        this.citizenSleepHandler = new CitizenSleepHandler(this);
        this.citizenExperienceHandler = new CitizenExperienceHandler(this);
        this.citizenDiseaseHandler = new CitizenDiseaseHandler(this);

        this.moveControl = new MovementHandler(this);
        this.setPersistenceRequired();
        this.setCustomNameVisible(MineColonies.getConfig().getServer().alwaysRenderNameTag.get());
        initTasks();
    }

    private void initTasks()
    {
        int priority = 0;
        this.goalSelector.addGoal(priority, new FloatGoal(this));
        this.goalSelector.addGoal(++priority, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(priority, new EntityAIInteractToggleAble(this, FENCE_TOGGLE, TRAP_TOGGLE, DOOR_TOGGLE));
        this.goalSelector.addGoal(++priority, new InteractGoal(this, Player.class, WATCH_CLOSEST2, 1.0F));
        this.goalSelector.addGoal(++priority, new InteractGoal(this, EntityCitizen.class, WATCH_CLOSEST2_FAR, WATCH_CLOSEST2_FAR_CHANCE));
        this.goalSelector.addGoal(++priority, new LookAtPlayerGoal(this, LivingEntity.class, WATCH_CLOSEST));
        this.goalSelector.addGoal(++priority, new EntityAIVisitor(this));
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

    @Override
    public boolean hurt(@NotNull final DamageSource damageSource, final float damage)
    {
        if ( !( damageSource.getEntity() instanceof EntityCitizen ) && super.hurt(damageSource, damage))
        {
            if (damageSource.getEntity() instanceof LivingEntity && damage > 1.01f)
            {
                final IBuilding home = getCitizenData().getHomeBuilding();
                if (home.hasModule(TavernBuildingModule.class))
                {
                    final TavernBuildingModule module = home.getFirstModuleOccurance(TavernBuildingModule.class);
                    for (final Integer id : module.getExternalCitizens())
                    {
                        ICitizenData data = citizenColonyHandler.getColony().getVisitorManager().getCivilian(id);
                        if (data != null && data.getEntity().isPresent() && data.getEntity().get().getLastHurtByMob() == null)
                        {
                            data.getEntity().get().setLastHurtByMob((LivingEntity) damageSource.getEntity());
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

    @Nullable
    @Override
    public ICitizenData getCitizenData()
    {
        return citizenData;
    }

    @Override
    public ICivilianData getCivilianData()
    {
        return citizenData;
    }

    @Override
    public void setCivilianData(@Nullable final ICivilianData data)
    {
        if (data != null && data instanceof IVisitorData)
        {
            this.citizenData = (IVisitorData) data;
            data.initEntityValues();
        }
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

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    @Override
    public void markDirty()
    {
        if (citizenData != null)
        {
            citizenData.markDirty();
        }
    }

    @NotNull
    @Override
    public DesiredActivity getDesiredActivity()
    {
        return DesiredActivity.IDLE;
    }

    @Override
    public void setCitizensize(@NotNull final float width, @NotNull final float height)
    {
        this.dimensions = new EntityDimensions(width, height, false);
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
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost());
            citizenData.markDirty();
        }
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    @Override
    public void decreaseSaturationForContinuousAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() / 100.0);
            citizenData.markDirty();
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
    public ICitizenExperienceHandler getCitizenExperienceHandler()
    {
        return citizenExperienceHandler;
    }

    @Override
    public ICitizenChatHandler getCitizenChatHandler()
    {
        return citizenChatHandler;
    }

    @Override
    public ICitizenStatusHandler getCitizenStatusHandler()
    {
        return citizenStatusHandler;
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
    public boolean isOkayToEat()
    {
        return false;
    }

    @Override
    public boolean shouldBeFed()
    {
        return false;
    }

    @Override
    public boolean isIdlingAtJob()
    {
        return false;
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
    public void setCitizenChatHandler(final ICitizenChatHandler citizenChatHandler)
    {
        this.citizenChatHandler = citizenChatHandler;
    }

    @Override
    public void setCitizenExperienceHandler(final ICitizenExperienceHandler citizenExperienceHandler)
    {
        this.citizenExperienceHandler = citizenExperienceHandler;
    }

    @Override
    public boolean isCurrentlyFleeing()
    {
        return currentlyFleeing;
    }

    @Override
    public void callForHelp(final Entity attacker, final int guardHelpRange)
    {

    }

    @Override
    public void setFleeingState(final boolean fleeing)
    {
        currentlyFleeing = fleeing;
    }

    @javax.annotation.Nullable
    @Override
    public AbstractContainerMenu createMenu(
      final int id, final Inventory playerInventory, final Player playerEntity)
    {
        return new ContainerCitizenInventory(id, playerInventory, citizenColonyHandler.getColonyId(), citizenId);
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

        final InteractionResult result = directPlayerInteraction(player, hand);
        if (result != null)
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
                    MineColonies.proxy.showCitizenWindow(citizenDataView);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Direct interaction on right click
     *
     * @param player
     * @param hand
     * @return
     */
    private InteractionResult directPlayerInteraction(final Player player, final InteractionHand hand)
    {
        final ItemStack usedStack = player.getItemInHand(hand);
        if (ISFOOD.test(usedStack))
        {
            usedStack.setCount(usedStack.getCount() - 1);
            player.setItemInHand(hand, usedStack);

            if (!level.isClientSide())
            {
                getCitizenData().increaseSaturation(usedStack.getItem().getFoodProperties().getNutrition());

                playSound(SoundEvents.GENERIC_EAT, 1.5f, (float) SoundUtils.getRandomPitch(getRandom()));
                // Position needs to be centered on citizen, Eat AI wrong too?
                Network.getNetwork()
                  .sendToTrackingEntity(new ItemParticleEffectMessage(usedStack,
                    getX(),
                    getY(),
                    getZ(),
                    getXRot(),
                    getYRot(),
                    getEyeHeight()), this);

                player.sendMessage(new TranslatableComponent("com.minecolonies.coremod.interaction.visitor.food", getCitizenData().getName()), player.getUUID());
            }
            return InteractionResult.CONSUME;
        }
        return null;
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

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons. use this to react to sunlight and start to burn.
     */
    @Override
    public void aiStep()
    {
        super.aiStep();

        if (lastHurtByPlayerTime > 0)
        {
            markDirty();
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
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_STATUS, citizenStatusHandler.getStatus().ordinal());
        if (citizenColonyHandler.getColony() != null && citizenData != null)
        {
            compound.putInt(TAG_COLONY_ID, citizenColonyHandler.getColony().getID());
            compound.putInt(TAG_CITIZEN, citizenData.getId());
        }

        citizenDiseaseHandler.write(compound);
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);

        citizenStatusHandler.setStatus(Status.values()[compound.getInt(TAG_STATUS)]);
        citizenColonyHandler.setColonyId(compound.getInt(TAG_COLONY_ID));
        citizenId = compound.getInt(TAG_CITIZEN);

        if (isEffectiveAi())
        {
            citizenColonyHandler.registerWithColony(citizenColonyHandler.getColonyId(), citizenId);
        }

        citizenDiseaseHandler.read(compound);
    }

    @Override
    public void die(DamageSource cause)
    {
        super.die(cause);
        if (!level.isClientSide())
        {
            IColony colony = getCitizenColonyHandler().getColony();
            if (colony != null && getCitizenData() != null)
            {
                colony.getVisitorManager().removeCivilian(getCitizenData());
                if (getCitizenData().getHomeBuilding() instanceof TavernBuildingModule)
                {
                    TavernBuildingModule tavern = (TavernBuildingModule) getCitizenData().getHomeBuilding();
                    tavern.setNoVisitorTime(level.getRandom().nextInt(5000) + 30000);
                }

                final String deathLocation = BlockPosUtil.getString(blockPosition());

                LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(),
                  "com.minecolonies.coremod.gui.tavern.visitordeath",
                  getCitizenData().getName(),
                  cause.getMsgId(),
                  deathLocation);
            }
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

    // TODO:REMOVE DEBUG
    @Override
    public void setPos(final double x, final double y, final double z)
    {
        super.setPos(x, y, z);
        if (level.isClientSide)
        {
            return;
        }

        if (citizenStatusHandler != null && x < 1 && x > -1 && z < 1 && z > -1)
        {
            Log.getLogger().error("Visitor entity set to zero pos, report to mod author:", new Exception());
            remove(RemovalReason.DISCARDED);

            if (getCitizenData() != null && citizenColonyHandler.getColony() != null)
            {
                citizenColonyHandler.getColony().getVisitorManager().removeCivilian(getCitizenData());
            }
        }
    }
}
