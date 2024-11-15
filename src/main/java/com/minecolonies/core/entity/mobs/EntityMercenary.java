package com.minecolonies.core.entity.mobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyRelated;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.sounds.MercenarySounds;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.entity.ai.minimal.EntityAIInteractToggleAble;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.proxy.GeneralEntityWalkToProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.BASE_PATHFINDING_RANGE;
import static com.minecolonies.api.util.constant.Constants.TICKS_FOURTY_MIN;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_TIME;
import static com.minecolonies.api.util.constant.RaiderConstants.FOLLOW_RANGE;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_COLONY_MERCENARY_STEAL_CITIZEN;
import static com.minecolonies.core.entity.ai.minimal.EntityAIInteractToggleAble.*;


/**
 * Class for Mercenary entities, which can be spawned to protect the colony
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class EntityMercenary extends AbstractFastMinecoloniesEntity implements Npc, IColonyRelated
{
    /**
     * The minimum time inbetween, in ticks.
     */
    private static final int                          SLAP_INTERVAL = 100;
    /**
     * Reference to the colony the mercenary spawned in.
     */
    private              IColony                      colony;
    /**
     * This entities minecolonies-Navigator.
     */
    private              AbstractAdvancedPathNavigate newNavigator;
    /**
     * Proxy for cheaper pathing.
     */
    private              GeneralEntityWalkToProxy     proxy;
    /**
     * The timer used to check if it is ready again.
     */
    private              int                          slapTimer     = 0;

    /**
     * Random instance for rolls
     */
    private final Random rand = new Random();

    /**
     * The world time when the mercenary spawns.
     */
    private long worldTimeAtSpawn = 0;

    /**
     * Leader role for the spawn event.
     */
    private boolean isLeader = false;

    /**
     * List of soldiers to use in the spawn event.
     */
    private List<EntityMercenary> soldiers = new ArrayList<>();

    /**
     * Timer for the spawnevent to be over.
     */
    private int spawnEventTime = 0;

    /**
     * Wheter we're doing a spawnevent
     */
    private boolean doSpawnEvent = false;

    /**
     * This entities state machine
     */
    private ITickRateStateMachine<IState> stateMachine;

    /**
     * The entities name.
     */
    private static final String ENTITY_NAME = "Mercenary";

    /**
     * Colony id of the assigned colony.
     */
    private int colonyId;

    /**
     * Constructor method for Mercenaries.
     *
     * @param type  the type.
     * @param world the world.
     */
    public EntityMercenary(final EntityType<EntityMercenary> type, final Level world)
    {
        super(type, world);

        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EntityMercenaryAI(this));
        this.goalSelector.addGoal(4, new EntityAIInteractToggleAble(this, FENCE_TOGGLE, TRAP_TOGGLE, DOOR_TOGGLE));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, e -> e instanceof Enemy && !(e instanceof Llama)));

        setCustomNameVisible(true);
        this.setPersistenceRequired();

        final ItemStack mainhand = new ItemStack(Items.GOLDEN_SWORD, 1);
        mainhand.enchant(Enchantments.FIRE_ASPECT, 1);
        this.setItemSlot(EquipmentSlot.MAINHAND, mainhand);

        final ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET, 1);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        this.setItemSlot(EquipmentSlot.HEAD, helmet);

        final ItemStack chest = new ItemStack(Items.GOLDEN_CHESTPLATE, 1);
        chest.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        this.setItemSlot(EquipmentSlot.CHEST, chest);

        final ItemStack legs = new ItemStack(Items.CHAINMAIL_LEGGINGS, 1);
        this.setItemSlot(EquipmentSlot.LEGS, legs);

        final ItemStack boots = new ItemStack(Items.CHAINMAIL_BOOTS, 1);
        this.setItemSlot(EquipmentSlot.FEET, boots);

        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60);
        this.setHealth(this.getMaxHealth());

        stateMachine = new TickRateStateMachine<>(EntityMercenaryAI.State.INIT, this::handleStateException);
        stateMachine.addTransition(new TickingTransition<>(EntityMercenaryAI.State.INIT, this::isInitialized, () -> EntityMercenaryAI.State.SPAWN_EVENT, 20));
        stateMachine.addTransition(new TickingTransition<>(EntityMercenaryAI.State.SPAWN_EVENT, this::spawnEvent, () -> EntityMercenaryAI.State.ALIVE, 30));
        stateMachine.addTransition(new TickingTransition<>(EntityMercenaryAI.State.ALIVE, this::shouldDespawn, () -> EntityMercenaryAI.State.DEAD, 100));
        stateMachine.addTransition(new TickingTransition<>(EntityMercenaryAI.State.DEAD, () -> true, this::getState, 500));
    }

    /**
     * Logs exceptions of the AI.
     *
     * @param e exception to log.
     */
    private void handleStateException(final RuntimeException e)
    {
        Log.getLogger().warn("Mercenary entity threw an exception:", e);
    }

    /**
     * Checks if this entity should be despawned.
     *
     * @return true if despawned
     */
    private boolean shouldDespawn()
    {
        if (level == null || level.getGameTime() - worldTimeAtSpawn > TICKS_FOURTY_MIN || colony == null || this.isInvisible())
        {
            this.remove(RemovalReason.DISCARDED);
            return true;
        }
        return false;
    }

    /**
     * Checking prerequisites before starting AI on this entity.
     *
     * @return true when ready to start actions.
     */
    private boolean isInitialized()
    {
        if (worldTimeAtSpawn == 0)
        {
            worldTimeAtSpawn = level.getGameTime();
        }

        return level != null && colony != null && isAlive() && !isInvisible();
    }

    /**
     * Does the spawnevent, leader walks up and down the lines and mumbles.
     *
     * @return true if event is done.
     */
    private boolean spawnEvent()
    {
        if (spawnEventTime > 0)
        {
            spawnEventTime--;
        }

        if (!doSpawnEvent || spawnEventTime == 0)
        {
            return true;
        }

        // nonleader just waits
        if (!isLeader)
        {
            return false;
        }

        if (!getNavigation().isDone())
        {
            return false;
        }

        final BlockPos first = soldiers.get(0).blockPosition().offset(0, 0, 1);
        final BlockPos last = soldiers.get(soldiers.size() - 1).blockPosition().offset(0, 0, 1);

        playSound(MercenarySounds.mercenaryCelebrate, 2.0f, 1.0f);
        if (blockPosition().equals(first))
        {
            getNavigation().tryMoveToBlockPos(last, 0.5);
        }
        else
        {
            getNavigation().tryMoveToBlockPos(first, 0.5);
        }

        return false;
    }

    /**
     * Toggles the spawn event on after initializing
     */
    public void setDoSpawnEvent()
    {
        doSpawnEvent = true;
        spawnEventTime = 15;
    }

    /**
     * Sets this mercenary as leader
     *
     * @param soldiers set a leader of the list.
     */
    public void setLeader(final List<EntityMercenary> soldiers)
    {
        this.soldiers = soldiers;
        isLeader = true;
        doSpawnEvent = true;
        spawnEventTime = 17;
    }

    /**
     * Get the state of this entity
     *
     * @return state
     */
    public IState getState()
    {
        return stateMachine.getState();
    }

    @Override
    protected void playStepSound(final BlockPos pos, final BlockState blockIn)
    {
        this.playSound(MercenarySounds.mercenaryStep, 0.45F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSourceIn)
    {
        return MercenarySounds.mercenaryHurt;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return MercenarySounds.mercenaryDie;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return MercenarySounds.mercenarySay;
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        compound.putLong(TAG_TIME, worldTimeAtSpawn);
        compound.putInt(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        if (compound.contains(TAG_COLONY_ID))
        {
            colonyId = compound.getInt(TAG_COLONY_ID);
            if (colonyId != 0)
            {
                setColony(IColonyManager.getInstance().getColonyByWorld(colonyId, level));
            }
        }
        super.readAdditionalSaveData(compound);
    }

    @Override
    public Component getName()
    {
        return Component.literal(ENTITY_NAME);
    }

    @Override
    public void registerWithColony()
    {
        //Does not need to register.
    }

    /**
     * Get the default attributes with their values.
     * @return the attribute modifier map.
     */
    public static AttributeSupplier.Builder getDefaultAttributes()
    {
        return LivingEntity.createLivingAttributes()
                 .add(Attributes.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE.getDefaultValue())
                 .add(Attributes.FOLLOW_RANGE, BASE_PATHFINDING_RANGE);
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    /**
     * Set the colony to raid.
     *
     * @param colony the colony to set.
     */
    public void setColony(final IColony colony)
    {
        if (colony != null)
        {
            this.colony = colony;
            this.registerWithColony();
        }
    }

    @Override
    public boolean hurt(final DamageSource source, final float damage)
    {
        if (source.getEntity() instanceof LivingEntity)
        {
            this.setTarget((LivingEntity) source.getEntity());
        }
        return super.hurt(source, damage);
    }

    @Override
    protected void doPush(final Entity entityIn)
    {
        if (slapTimer == 0 && entityIn instanceof Player)
        {
            slapTimer = SLAP_INTERVAL;
            entityIn.hurt(entityIn.level.damageSources().source(DamageSourceKeys.SLAP, this), 1.0f);
            this.swing(InteractionHand.OFF_HAND);
        }

        if (slapTimer == 0 && entityIn instanceof EntityCitizen && colony != null && ((EntityCitizen) entityIn).isActive())
        {
            slapTimer = SLAP_INTERVAL;
            final IItemHandler handler = ((EntityCitizen) entityIn).getItemHandlerCitizen();
            final ItemStack stack = handler.extractItem(rand.nextInt(handler.getSlots()), 5, false);
            if (!ItemStackUtils.isEmpty(stack))
            {
                this.swing(InteractionHand.OFF_HAND);
                MessageUtils.format(MESSAGE_INFO_COLONY_MERCENARY_STEAL_CITIZEN, entityIn.getName().getString(), stack.getHoverName().getString()).sendTo(colony).forAllPlayers();
            }
        }
    }

    /**
     * Creates and returns the proxy when needed.
     *
     * @return the walking proxy.
     */
    public GeneralEntityWalkToProxy getProxy()
    {
        if (proxy == null)
        {
            proxy = new GeneralEntityWalkToProxy(this);
        }
        return proxy;
    }

    @NotNull
    @Override
    public AbstractAdvancedPathNavigate getNavigation()
    {
        if (this.newNavigator == null)
        {
            this.newNavigator = new MinecoloniesAdvancedPathNavigate(this, level);
            this.navigation = newNavigator;
            this.newNavigator.setCanFloat(true);
            this.newNavigator.getNodeEvaluator().setCanOpenDoors(true);
        }
        return newNavigator;
    }

    @Override
    public void aiStep()
    {
        if (level != null && !level.isClientSide)
        {
            stateMachine.tick();
        }
        if (slapTimer > 0)
        {
            slapTimer--;
        }
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public boolean requiresCustomPersistence()
    {
        return true;
    }

    /**
     * Spawns mercenaries in the given colony.
     *
     * @param colony given colony
     */
    public static void spawnMercenariesInColony(@NotNull final IColony colony)
    {
        final Level world = colony.getWorld();

        if (colony.getMercenaryUseTime() != 0 && world.getGameTime() - colony.getMercenaryUseTime() < TICKS_FOURTY_MIN)
        {
            return;
        }

        colony.usedMercenaries();

        int amountOfMercenaries = colony.getCitizenManager().getCurrentCitizenCount();
        amountOfMercenaries = amountOfMercenaries / 10;
        amountOfMercenaries += 3;

        final BlockPos spawn = EntityMercenary.findMercenarySpawnPos(colony, amountOfMercenaries);

        final List<EntityMercenary> soldiers = new ArrayList<>();
        for (int i = 0; i < amountOfMercenaries; i++)
        {
            final EntityMercenary merc = (EntityMercenary) ModEntities.MERCENARY.create(world);
            merc.setColony(colony);
            merc.setPos(spawn.getX() + i, spawn.getY(), spawn.getZ());
            merc.setDoSpawnEvent();
            soldiers.add(merc);
            world.addFreshEntity(merc);
        }

        // spawn leader for the event.
        final EntityMercenary merc = (EntityMercenary) ModEntities.MERCENARY.create(world);
        merc.setColony(colony);
        merc.setPos(spawn.getX(), spawn.getY(), spawn.getZ() + 1);
        merc.setLeader(soldiers);
        world.addFreshEntity(merc);
    }

    /**
     * Finds a spawn position for the mercenaries near the townhall.
     *
     * @param colony              Colony to look in
     * @param amountOfMercenaries amount of spawns
     * @return spawn position
     */
    private static BlockPos findMercenarySpawnPos(final IColony colony, final int amountOfMercenaries)
    {
        final Tuple<BlockPos, BlockPos> buildingArea = colony.getBuildingManager().getTownHall().getCorners();
        BlockPos spawn = new BlockPos((buildingArea.getB().getX() + buildingArea.getA().getX()) / 2, 0, buildingArea.getA().getZ());
        int height = colony.getWorld().getHeight(Heightmap.Types.WORLD_SURFACE, spawn.getX(), spawn.getZ());
        if (height > buildingArea.getB().getY())
        {
            height = buildingArea.getA().getY() + 1;
        }

        spawn = spawn.offset(0, height, 0);

        for (int i = -3; i < 4; i++)
        {
            if (isValidSpawnForMercenaries(colony.getWorld(), spawn.offset(0, 0, i), amountOfMercenaries))
            {
                spawn = spawn.offset(0, 0, i);
                break;
            }
        }

        return spawn;
    }

    /**
     * Checks if we have enough airblocks to spawn
     *
     * @param world               world to acces blocks from
     * @param spawn               spawn starting positions
     * @param amountOfMercenaries how many we're spawning
     * @return true if enough air
     */
    private static boolean isValidSpawnForMercenaries(final LevelAccessor world, final BlockPos spawn, final int amountOfMercenaries)
    {
        for (int i = 0; i < amountOfMercenaries; i++)
        {
            if (!world.isEmptyBlock(spawn.above().offset(i, 0, 0)) || !world.isEmptyBlock(spawn.above().offset(i, 0, 1)))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getTeamId()
    {
        return colonyId;
    }
}
