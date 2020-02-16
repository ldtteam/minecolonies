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
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.entity.ai.minimal.EntityAIOpenFenceGate;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.OpenDoorGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.TICKS_FOURTY_MIN;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_TIME;
import static com.minecolonies.api.util.constant.RaiderConstants.FOLLOW_RANGE;

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
    private              IColony                       colony;
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
     * @param world the world.
     */
    public EntityMercenary(final EntityType<EntityMercenary> type, final World world)
    {
        super(type, world);

        this.goalSelector = new CustomGoalSelector(this.goalSelector);
        this.targetSelector = new CustomGoalSelector(this.targetSelector);
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new EntityMercenaryAI(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new EntityAIOpenFenceGate(this, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, MonsterEntity.class, 10, true, false, e -> e instanceof IMob && !(e instanceof LlamaEntity)));

        this.forceSpawn = true;
        setCustomNameVisible(true);
        this.enablePersistence();

        final ItemStack mainhand = new ItemStack(Items.GOLDEN_SWORD, 1);
        mainhand.addEnchantment(Enchantments.FIRE_ASPECT, 1);
        this.setItemStackToSlot(EquipmentSlotType.MAINHAND, mainhand);

        final ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET, 1);
        helmet.addEnchantment(Enchantments.PROTECTION, 4);
        this.setItemStackToSlot(EquipmentSlotType.HEAD, helmet);

        final ItemStack chest = new ItemStack(Items.GOLDEN_CHESTPLATE, 1);
        chest.addEnchantment(Enchantments.PROTECTION, 4);
        this.setItemStackToSlot(EquipmentSlotType.CHEST, chest);

        final ItemStack legs = new ItemStack(Items.CHAINMAIL_LEGGINGS, 1);
        this.setItemStackToSlot(EquipmentSlotType.LEGS, legs);

        final ItemStack boots = new ItemStack(Items.CHAINMAIL_BOOTS, 1);
        this.setItemStackToSlot(EquipmentSlotType.FEET, boots);

        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);

        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60);
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
        if (world == null || world.getGameTime() - worldTimeAtSpawn > TICKS_FOURTY_MIN || colony == null || this.isInvisible())
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
            worldTimeAtSpawn = world.getGameTime();
        }

        return world != null && colony != null && isAlive() && !isInvisible();
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

        if (!getNavigator().noPath())
        {
            return false;
        }

        final BlockPos first = soldiers.get(0).getPosition().add(0, 0, 1);
        final BlockPos last = soldiers.get(soldiers.size() - 1).getPosition().add(0, 0, 1);

        playSound(SoundEvents.ENTITY_EVOKER_CELEBRATE, 2.0f, 1.0f);
        if (getPosition().equals(first))
        {
            getNavigator().tryMoveToBlockPos(last, 0.5);
        }
        else
        {
            getNavigator().tryMoveToBlockPos(first, 0.5);
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
        this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.45F, 1.0F);
    }

    @Override
    public void writeAdditional(final CompoundNBT compound)
    {
        compound.putLong(TAG_TIME, worldTimeAtSpawn);
        compound.putInt(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        super.writeAdditional(compound);
    }

    @Override
    public void readAdditional(final CompoundNBT compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        if (compound.keySet().contains(TAG_COLONY_ID))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            if (colonyId != 0)
            {
                setColony(IColonyManager.getInstance().getColonyByWorld(colonyId, world));
            }
        }
        super.readAdditional(compound);
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

    @Override
    protected void registerAttributes()
    {
        super.registerAttributes();
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
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
    public boolean attackEntityFrom(final DamageSource source, final float damage)
    {
        if (source.getTrueSource() instanceof LivingEntity)
        {
            this.setAttackTarget((LivingEntity) source.getTrueSource());
        }
        return super.attackEntityFrom(source, damage);
    }

    @Override
    protected void collideWithEntity(final Entity entityIn)
    {
        if (slapTimer == 0 && entityIn instanceof PlayerEntity)
        {
            slapTimer = SLAP_INTERVAL;
            entityIn.attackEntityFrom(new EntityDamageSource("slap", this), 1.0f);
            this.swingArm(Hand.OFF_HAND);
        }

        if (slapTimer == 0 && entityIn instanceof EntityCitizen && colony != null)
        {
            slapTimer = SLAP_INTERVAL;
            final IItemHandler handler = ((EntityCitizen) entityIn).getItemHandlerCitizen();
            final ItemStack stack = handler.extractItem(rand.nextInt(handler.getSlots()), 5, false);
            if (!ItemStackUtils.isEmpty(stack))
            {
                this.swingArm(Hand.OFF_HAND);
                LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntitys(),
                  "com.minecolonies.coremod.mercenary.mercenaryStealCitizen",
                  entityIn.getName(),
                  stack.getDisplayName());
            }
        }
    }

    /**
     * Creates and returns the proxy when needed.
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
    public AbstractAdvancedPathNavigate getNavigator()
    {
        if (this.newNavigator == null)
        {
            this.newNavigator = new MinecoloniesAdvancedPathNavigate(this, world);
            this.navigator = newNavigator;
            this.newNavigator.setCanSwim(true);
            this.newNavigator.getNodeProcessor().setCanOpenDoors(true);
        }
        return newNavigator;
    }

    @Override
    public void livingTick()
    {
        if (world != null && !world.isRemote)
        {
            stateMachine.tick();
        }
        if (slapTimer > 0)
        {
            slapTimer--;
        }
        updateArmSwingProgress();
        super.livingTick();
    }

    @Override
    public boolean preventDespawn()
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
            merc.setPosition(spawn.getX() + i, spawn.getY(), spawn.getZ());
            merc.setDoSpawnEvent();
            soldiers.add(merc);
            world.addEntity(merc);
        }

        // spawn leader for the event.
        final EntityMercenary merc = (EntityMercenary) ModEntities.MERCENARY.create(world);
        merc.setColony(colony);
        merc.setPosition(spawn.getX(), spawn.getY(), spawn.getZ() + 1);
        merc.setLeader(soldiers);
        world.addEntity(merc);
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
        final AxisAlignedBB buildingArea = colony.getBuildingManager().getTownHall().getTargetableArea(colony.getWorld());
        BlockPos spawn = new BlockPos((buildingArea.maxX + buildingArea.minX) / 2, 0, buildingArea.minZ);
        double height = colony.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, spawn.getX(), spawn.getZ());
        if (height > buildingArea.maxY)
        {
            height = buildingArea.minY + 1;
        }

        spawn = spawn.add(0, height, 0);

        for (int i = -3; i < 4; i++)
        {
            if (isValidSpawnForMercenaries(colony.getWorld(), spawn.add(0, 0, i), amountOfMercenaries))
            {
                spawn = spawn.add(0, 0, i);
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
            if (!world.isAirBlock(spawn.up().add(i, 0, 0)) || !world.isAirBlock(spawn.up().add(i, 0, 1)))
            {
                return false;
            }
        }
        return true;
    }
}
