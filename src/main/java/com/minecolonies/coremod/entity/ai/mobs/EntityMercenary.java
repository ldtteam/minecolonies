package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.IColonyRelatedEntity;
import com.minecolonies.coremod.entity.ai.statemachine.basestatemachine.IStateMachine;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.coremod.entity.pathfinding.GeneralEntityWalkToProxy;
import com.minecolonies.coremod.entity.pathfinding.PathNavigate;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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
public class EntityMercenary extends EntityCreature implements INpc, IColonyRelatedEntity
{
    /**
     * The minimum time inbetween, in ticks.
     */
    private static final int                      SLAP_INTERVAL = 100;
    /**
     * Reference to the colony the mercenary spawned in.
     */
    private              Colony                   colony;
    /**
     * This entities minecolonies-Navigator.
     */
    private              PathNavigate             newNavigator;
    /**
     * Proxy for cheaper pathing.
     */
    private              GeneralEntityWalkToProxy proxy;
    /**
     * The timer used to check if it is ready again.
     */
    private              int                      slapTimer     = 0;

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
    private IStateMachine<TickingTransition> stateMachine;

    /**
     * The entities name.
     */
    private static final String ENTITY_NAME = "Mercenary";

    public EntityMercenary(final World world)
    {
        this(world, null);
    }

    /**
     * Constructor method for Mercenaries.
     *
     * @param world the world.
     */
    public EntityMercenary(final World world, final Colony colony)
    {
        super(world);
        this.colony = colony;
        this.forceSpawn = true;
        this.setSize(1.0f, 2.0f);
        setAlwaysRenderNameTag(true);
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

        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60);
        this.setHealth(this.getMaxHealth());

        stateMachine = new TickRateStateMachine(EntityMercenaryAI.State.INIT, this::handleStateException);
        stateMachine.addTransition(new TickingTransition(EntityMercenaryAI.State.INIT, this::isInitialized, () -> EntityMercenaryAI.State.SPAWN_EVENT, 20));
        stateMachine.addTransition(new TickingTransition(EntityMercenaryAI.State.SPAWN_EVENT, this::spawnEvent, () -> EntityMercenaryAI.State.ALIVE, 30));
        stateMachine.addTransition(new TickingTransition(EntityMercenaryAI.State.ALIVE, this::shouldDespawn, () -> EntityMercenaryAI.State.DEAD, 100));
        stateMachine.addTransition(new TickingTransition(EntityMercenaryAI.State.DEAD, () -> true, this::getState, 500));
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
        if (world == null || world.getTotalWorldTime() - worldTimeAtSpawn > TICKS_FOURTY_MIN || colony == null || this.isInvisible())
        {
            this.setDead();
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
            worldTimeAtSpawn = world.getTotalWorldTime();
        }

        return world != null && colony != null && !isDead && !isInvisible();
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

        playSound(SoundEvents.ENTITY_EVOCATION_ILLAGER_AMBIENT, 2.0f, 1.0f);
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
    public IAIState getState()
    {
        return stateMachine.getState();
    }

    @Override
    public void playStepSound(final BlockPos pos, final Block blockIn)
    {
        this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.45F, 1.0F);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        compound.putLong(TAG_TIME, worldTimeAtSpawn);
        compound.putInt(TAG_COLONY_ID, this.colony == null ? 0 : colony.getID());
        return super.write(compound);
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        worldTimeAtSpawn = compound.getLong(TAG_TIME);
        if (compound.keySet().contains(TAG_COLONY_ID))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            if (colonyId != 0)
            {
                setColony(ColonyManager.getColonyByWorld(colonyId, world));
            }
        }
        super.readFromNBT(compound);
    }

    @Override
    public String getName()
    {
        return ENTITY_NAME;
    }

    @Override
    public void registerWithColony()
    {
    }

    @Override
    public Colony getColony()
    {
        return colony;
    }

    /**
     * Set the colony to raid.
     *
     * @param colony the colony to set.
     */
    public void setColony(final Colony colony)
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
        if (source.getTrueSource() instanceof LivingEntityBase)
        {
            this.setAttackTarget((LivingEntityBase) source.getTrueSource());
        }
        return super.attackEntityFrom(source, damage);
    }

    @Override
    protected void collideWithEntity(final Entity entityIn)
    {
        if (slapTimer == 0 && entityIn instanceof PlayerEntity)
        {
            slapTimer = SLAP_INTERVAL;
            entityIn.attackEntityFrom(new EntityDamageSource("Slap", this), 1.0f);
            this.swingArm(EnumHand.OFF_HAND);
        }

        if (slapTimer == 0 && entityIn instanceof EntityCitizen && colony != null)
        {
            slapTimer = SLAP_INTERVAL;
            final IItemHandler handler = ((EntityCitizen) entityIn).getItemHandlerCitizen();
            final ItemStack stack = handler.extractItem(rand.nextInt(handler.getSlots()), 5, false);
            if (!ItemStackUtils.isEmpty(stack))
            {
                this.swingArm(EnumHand.OFF_HAND);
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

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityMercenaryAI(this));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityMob.class, false));
    }

    @NotNull
    @Override
    public PathNavigate getNavigator()
    {
        if (this.newNavigator == null)
        {
            this.newNavigator = new PathNavigate(this, world);
            this.navigator = newNavigator;
            this.newNavigator.setCanSwim(true);
            this.newNavigator.setEnterDoors(true);
        }
        return newNavigator;
    }

    @Override
    public void onLivingUpdate()
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
        super.onLivingUpdate();
    }

    @Override
    public boolean canDespawn()
    {
        return false;
    }

    /**
     * Spawns mercenaries in the given colony.
     *
     * @param colony given colony
     */
    public static void spawnMercenariesInColony(@NotNull final Colony colony)
    {
        final World world = colony.getWorld();

        if (colony.getMercenaryUseTime() != 0 && world.getTotalWorldTime() - colony.getMercenaryUseTime() < TICKS_FOURTY_MIN)
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
            final EntityMercenary merc = new EntityMercenary(world, colony);
            merc.setPosition(spawn.getX() + i, spawn.getY(), spawn.getZ());
            merc.setDoSpawnEvent();
            soldiers.add(merc);
            world.addEntity(merc);
        }

        // spawn leader for the event.
        final EntityMercenary merc = new EntityMercenary(world, colony);
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
    private static BlockPos findMercenarySpawnPos(final Colony colony, final int amountOfMercenaries)
    {
        final AxisAlignedBB buildingArea = colony.getBuildingManager().getTownHall().getTargetableArea(colony.getWorld());
        BlockPos spawn = new BlockPos((buildingArea.maxX + buildingArea.minX) / 2, 0, buildingArea.minZ);
        double height = colony.getWorld().getHeight(spawn.getX(), spawn.getZ());
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
    private static boolean isValidSpawnForMercenaries(final IBlockAccess world, final BlockPos spawn, final int amountOfMercenaries)
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
