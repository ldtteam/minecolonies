package com.minecolonies.coremod.entity.mobs;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyRelated;
import com.minecolonies.api.entity.CustomGoalSelector;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.sounds.MercenarySounds;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.entity.ai.minimal.EntityAIInteractToggleAble;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
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
import static com.minecolonies.coremod.entity.ai.minimal.EntityAIInteractToggleAble.*;

/**
 * Class for Mercenary entities, which can be spawned to protect the colony
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class EntityMercenary extends CreatureEntity implements INPC, IColonyRelated
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
     * Constructor method for Mercenaries.
     *
     * @param type  the type.
     * @param world the world.
     */
    public EntityMercenary(final EntityType<EntityMercenary> type, final World world)
    {
        super(type, world);

        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new EntityMercenaryAI(this));
        this.goalSelector.addGoal(4, new EntityAIInteractToggleAble(this, FENCE_TOGGLE, TRAP_TOGGLE, DOOR_TOGGLE));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, MonsterEntity.class, 10, true, false, e -> e instanceof IMob && !(e instanceof LlamaEntity)));

        this.forcedLoading = true;
        setCustomNameVisible(true);
        this.setPersistenceRequired();

        final ItemStack mainhand = new ItemStack(Items.GOLDEN_SWORD, 1);
        mainhand.enchant(Enchantments.FIRE_ASPECT, 1);
        this.setItemSlot(EquipmentSlotType.MAINHAND, mainhand);

        final ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET, 1);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        this.setItemSlot(EquipmentSlotType.HEAD, helmet);

        final ItemStack chest = new ItemStack(Items.GOLDEN_CHESTPLATE, 1);
        chest.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        this.setItemSlot(EquipmentSlotType.CHEST, chest);

        final ItemStack legs = new ItemStack(Items.CHAINMAIL_LEGGINGS, 1);
        this.setItemSlot(EquipmentSlotType.LEGS, legs);

        final ItemStack boots = new ItemStack(Items.CHAINMAIL_BOOTS, 1);
        this.setItemSlot(EquipmentSlotType.FEET, boots);

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
            this.remove();
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
     * Get the blockpos pos.
     * @return a blockpos.
     */
    public BlockPos blockPosition()
    {
        return new BlockPos(this.position());
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
    public void addAdditionalSaveData(final CompoundNBT compound)
    {
        compound.putLong(TAG_TIME, worldTimeAtSpawn);
        compound.putInt(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(final CompoundNBT compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        if (compound.getAllKeys().contains(TAG_COLONY_ID))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            if (colonyId != 0)
            {
                setColony(IColonyManager.getInstance().getColonyByWorld(colonyId, level));
            }
        }
        super.readAdditionalSaveData(compound);
    }

    @Override
    public ITextComponent getName()
    {
        return new StringTextComponent(ENTITY_NAME);
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
    public static AttributeModifierMap.MutableAttribute getDefaultAttributes()
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
        if (slapTimer == 0 && entityIn instanceof PlayerEntity)
        {
            slapTimer = SLAP_INTERVAL;
            entityIn.hurt(new EntityDamageSource("slap", this), 1.0f);
            this.swing(Hand.OFF_HAND);
        }

        if (slapTimer == 0 && entityIn instanceof EntityCitizen && colony != null && ((EntityCitizen) entityIn).isActive())
        {
            slapTimer = SLAP_INTERVAL;
            final IItemHandler handler = ((EntityCitizen) entityIn).getItemHandlerCitizen();
            final ItemStack stack = handler.extractItem(rand.nextInt(handler.getSlots()), 5, false);
            if (!ItemStackUtils.isEmpty(stack))
            {
                this.swing(Hand.OFF_HAND);
                LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
                  "com.minecolonies.coremod.mercenary.mercenaryStealCitizen",
                  entityIn.getName().getString(),
                  stack.getHoverName().getString());
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
        final World world = colony.getWorld();

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
        double height = colony.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, spawn.getX(), spawn.getZ());
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
    private static boolean isValidSpawnForMercenaries(final IWorld world, final BlockPos spawn, final int amountOfMercenaries)
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
}
