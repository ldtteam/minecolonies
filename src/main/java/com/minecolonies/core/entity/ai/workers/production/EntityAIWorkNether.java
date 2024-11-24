package com.minecolonies.core.entity.ai.workers.production;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.compatibility.tinkers.TinkersToolHelper;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.workers.util.GuardGear;
import com.minecolonies.api.entity.ai.workers.util.GuardGearBuilder;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.*;
import com.minecolonies.core.colony.buildings.modules.ExpeditionLogModule;
import com.minecolonies.core.colony.buildings.modules.expedition.ExpeditionLog;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingNetherWorker;
import com.minecolonies.core.colony.jobs.JobNetherWorker;
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import com.minecolonies.core.items.ItemAdventureToken;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.REGENERATION;
import static com.minecolonies.api.research.util.ResearchConstants.SATLIMIT;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.NETHERMINER_MENU;
import static com.minecolonies.core.entity.ai.workers.production.EntityAIStructureMiner.*;

public class EntityAIWorkNether extends AbstractEntityAICrafting<JobNetherWorker, BuildingNetherWorker>
{

    /**
     * Delay for each of the crafting operations.
     */
    private static final int TICK_DELAY = 40;

    /**
     * Objective to use to pass the hut level to the loottable
     */
    private static final String OBJECTIVE_HUT_LEVEL = "HutLevel";

    /**
     * Objective to use to pass the worker's secondary skill level to the loottable
     */
    private static final String OBJECTIVE_SECONDARY_SKILL = "SecSkillLevel";

    /**
     * Multiplier for damage reduction.
     */
    private static final float SECONDARY_DAMAGE_REDUCTION = .005f;

    /**
     * Virtual slots for equipment, so we can track what is "equipped" without having it visible when the citizen is invisible.
     */
    private final Map<EquipmentSlot, ItemStack> virtualEquipmentSlots = new HashMap<>();

    /**
     * Edibles that the worker will attempt to eat while in the nether (unfiltered)
     */
    final List<ItemStack> netherEdible = IColonyManager.getInstance()
                                           .getCompatibilityManager()
                                           .getEdibles(building.getBuildingLevel() - 1)
                                           .stream()
                                           .map(ItemStorage::getItemStack)
                                           .collect(Collectors.toList());

    /**
     * List of items that are required by the guard based on building level and guard level.  This array holds a pointer to the building level and then pointer to GuardGear
     */
    public final List<List<GuardGear>> itemsNeeded = new ArrayList<>();

    public EntityAIWorkNether(@NotNull JobNetherWorker job)
    {
        super(job);
        super.registerTargets(
          new AITarget(NETHER_LEAVE, this::leaveForNether, TICK_DELAY),
          new AITarget(NETHER_AWAY, this::stayInNether, TICK_DELAY),
          new AITarget(NETHER_RETURN, this::returnFromNether, TICK_DELAY),
          new AITarget(NETHER_OPENPORTAL, this::openPortal, TICK_DELAY),
          new AITarget(NETHER_CLOSEPORTAL, this::closePortal, TICK_DELAY)
        );
        worker.setCanPickUpLoot(true);

        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_IRON, ARMOR_LEVEL_MAX, LEATHER_BUILDING_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_CHAIN, ARMOR_LEVEL_DIAMOND, LEATHER_BUILDING_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_IRON, LEATHER_BUILDING_LEVEL_RANGE, IRON_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_CHAIN, LEATHER_BUILDING_LEVEL_RANGE, CHAIN_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_GOLD, LEATHER_BUILDING_LEVEL_RANGE, GOLD_BUILDING_LEVEL_RANGE));
    }

    @Override
    protected void updateRenderMetaData()
    {
        StringBuilder renderData = new StringBuilder(getState() == CRAFT
                                                       || getState() == NETHER_LEAVE
                                                       || getState() == NETHER_RETURN
                                                       || getState() == NETHER_OPENPORTAL
                                                       || getState() == NETHER_CLOSEPORTAL ? RENDER_META_WORKING : "");

        for (int slot = 0; slot < worker.getInventoryCitizen().getSlots(); slot++)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
            if (stack.getItem() == Items.TORCH && renderData.indexOf(RENDER_META_TORCH) == -1)
            {
                renderData.append(RENDER_META_TORCH);
            }
            else if (stack.canPerformAction(ToolActions.PICKAXE_DIG) && renderData.indexOf(RENDER_META_PICKAXE) == -1)
            {
                renderData.append(RENDER_META_PICKAXE);
            }
            else if (stack.canPerformAction(ToolActions.SHOVEL_DIG) && renderData.indexOf(RENDER_META_SHOVEL) == -1)
            {
                renderData.append(RENDER_META_SHOVEL);
            }
        }

        worker.setRenderMetadata(renderData.toString());
    }

    @Override
    public Class<BuildingNetherWorker> getExpectedBuildingClass()
    {
        return BuildingNetherWorker.class;
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return START_WORKING;
    }

    @Override
    public boolean canBeInterrupted()
    {
        return !worker.isInvisible();
    }

    private void goToVault()
    {
        worker.playSound(SoundEvents.PORTAL_TRIGGER, worker.getRandom().nextFloat() * 0.5F + 0.25F, 0.25F);
        worker.setInvisible(true);
        worker.setSilent(true);
        BlockPos vaultPos = building.getVaultLocation();
        if (vaultPos != null)
        {
            TeleportHelper.teleportCitizen(worker, world, vaultPos);
        }
    }

    private void returnFromVault(final boolean force)
    {
        BlockPos vaultPos = building.getVaultLocation();
        BlockPos portalPos = building.getPortalLocation();
        if (portalPos != null && vaultPos != null && EntityUtils.isLivingAtSite(worker, vaultPos.getX(), vaultPos.getY(), vaultPos.getZ(), 2))
        {
            TeleportHelper.teleportCitizen(worker, world, portalPos);
            worker.setSilent(false);
            worker.playSound(SoundEvents.PORTAL_TRIGGER, worker.getRandom().nextFloat() * 0.5F + 0.25F, 0.25F);

            if (!force)
            {
                return;
            }
        }
        worker.setInvisible(false);
        worker.setSilent(false);
    }

    @Override
    protected IAIState decide()
    {
        if (job.isInNether())
        {
            if (!worker.isInvisible())
            {
                goToVault();
            }
            return NETHER_AWAY;
        }

        if (worker.isInvisible())
        {
            returnFromVault(true);
        }

        IAIState crafterState = super.decide();

        if (crafterState != IDLE && crafterState != START_WORKING)
        {
            return crafterState;
        }

        // Get Armor if available. 
        // This is async, so we could go to the nether without it. 
        checkAndRequestArmor();
        // Get food if available.
        final IAIState tempState = checkAndRequestFood();
        if (tempState != getState())
        {
            return tempState;
        }

        // Check for materials needed to go to the Nether: 
        IRecipeStorage rs = building.getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class).getFirstRecipe(ItemStack::isEmpty);
        boolean hasItemsAvailable = true;
        if (rs != null)
        {
            for (ItemStorage item : rs.getInput())
            {
                if (!checkIfRequestForItemExistOrCreateAsync(new ItemStack(item.getItem(), 1), item.getAmount(), item.getAmount()))
                {
                    hasItemsAvailable = false;
                }
            }
        }

        if (!hasItemsAvailable)
        {
            setDelay(60);
            return IDLE;
        }

        final BlockPos portal = building.getPortalLocation();
        if (portal == null)
        {
            Log.getLogger().warn("--- Missing Portal Tag In Nether Worker Building! Aborting Operation! ---");
            setDelay(120);
            return IDLE;
        }

        // Get other adventuring supplies. These are required. 
        // Done this way to get all the requests in parallel
        boolean missingAxe = checkForToolOrWeapon(ModEquipmentTypes.axe.get());
        boolean missingPick = checkForToolOrWeapon(ModEquipmentTypes.pickaxe.get());
        boolean missingShovel = checkForToolOrWeapon(ModEquipmentTypes.shovel.get());
        boolean missingSword = checkForToolOrWeapon(ModEquipmentTypes.sword.get());
        boolean missingLighter = checkForToolOrWeapon(ModEquipmentTypes.flint_and_steel.get());
        if (missingAxe || missingPick || missingShovel || missingSword || missingLighter)
        {
            worker.getCitizenData().setIdleAtJob(true);
            setDelay(60);
            return IDLE;
        }

        if (currentRecipeStorage == null)
        {
            final ICraftingBuildingModule module = building.getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class);
            currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);
            if (building.isReadyForTrip())
            {
                worker.getCitizenData().setIdleAtJob(true);
            }

            if (currentRecipeStorage == null && building.shallClosePortalOnReturn())
            {
                final BlockState block = world.getBlockState(portal);
                if (block.is(Blocks.NETHER_PORTAL))
                {
                    return NETHER_CLOSEPORTAL;
                }
            }

            return getState();
        }
        else
        {
            if (!building.isReadyForTrip())
            {
                worker.getCitizenData().setIdleAtJob(false);
                setDelay(120);
                return IDLE;
            }
            if (walkTo != null || walkToBuilding())
            {
                return getState();
            }
            if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
            {
                return INVENTORY_FULL;
            }

            IAIState checkResult = checkForItems(currentRecipeStorage);

            if (checkResult == GET_RECIPE)
            {
                currentRecipeStorage = null;
                worker.getCitizenData().setIdleAtJob(true);
                setDelay(60);
                return IDLE;
            }
            if (checkResult != CRAFT)
            {
                return checkResult;
            }
        }
        return NETHER_LEAVE;
    }

    /**
     * Leave for the Nether by walking to the portal and going invisible.
     */
    protected IAIState leaveForNether()
    {
        if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
        {
            return INVENTORY_FULL;
        }

        if (currentRecipeStorage == null)
        {
            job.setInNether(false);
            worker.getCitizenData().setIdleAtJob(true);
            return IDLE;
        }

        // Set up Objectives and scores. 
        if (!world.getScoreboard().hasObjective(OBJECTIVE_HUT_LEVEL))
        {
            world.getScoreboard().addObjective(OBJECTIVE_HUT_LEVEL, ObjectiveCriteria.DUMMY, Component.literal("Worker Building Level"), ObjectiveCriteria.RenderType.INTEGER);
        }
        if (!world.getScoreboard().hasObjective(OBJECTIVE_SECONDARY_SKILL))
        {
            world.getScoreboard()
              .addObjective(OBJECTIVE_SECONDARY_SKILL, ObjectiveCriteria.DUMMY, Component.literal("Worker Secondary Skill Level"), ObjectiveCriteria.RenderType.INTEGER);
        }
        final Objective hutLevelObjective = world.getScoreboard().getObjective(OBJECTIVE_HUT_LEVEL);
        final Objective secondarySkillLevelObjective = world.getScoreboard().getObjective(OBJECTIVE_SECONDARY_SKILL);

        Score s = world.getScoreboard().getOrCreatePlayerScore(worker.getScoreboardName(), hutLevelObjective);
        s.setScore(building.getBuildingLevel());
        s = world.getScoreboard().getOrCreatePlayerScore(worker.getScoreboardName(), secondarySkillLevelObjective);
        s.setScore(getSecondarySkillLevel());

        final ExpeditionLog expeditionLog = building.getFirstModuleOccurance(ExpeditionLogModule.class).getLog();
        expeditionLog.reset();
        expeditionLog.setStatus(ExpeditionLog.Status.STARTING);
        expeditionLog.setCitizen(worker);

        // Attempt to light the portal and travel
        final BlockPos portal = building.getPortalLocation();
        if (portal != null && currentRecipeStorage != null)
        {
            final BlockState block = world.getBlockState(portal);
            if (block.is(Blocks.NETHER_PORTAL))
            {
                if (walkToBlock(portal, 1))
                {
                    return getState();
                }
                goToVault();
                building.recordTrip();
                job.setInNether(true);

                expeditionLog.setStatus(ExpeditionLog.Status.IN_PROGRESS);
                logAllEquipment(expeditionLog, false);

                List<ItemStack> result = currentRecipeStorage.fullfillRecipeAndCopy(getLootContext(), ImmutableList.of(worker.getItemHandlerCitizen()), false);
                if (result != null)
                {
                    // by default all the adventure tokens are at the end (due to loot tables); space them better
                    result = new ArrayList<>(result);
                    Collections.shuffle(result, worker.getCitizenData().getRandom());
                    job.addCraftedResultsList(result);
                }

                worker.getCitizenData().setIdleAtJob(false);
                return NETHER_AWAY;
            }
            return NETHER_OPENPORTAL;
        }
        worker.getCitizenData().setIdleAtJob(true);
        return IDLE;
    }

    /**
     * Stay "in the Nether" and process the queues
     */
    protected IAIState stayInNether()
    {
        if (building.getVaultLocation() == null)
        {
            //Ensure we stay put in the portal
            final BlockPos portal = building.getPortalLocation();
            if (portal != null && walkToBlock(portal, 1))
            {
                return getState();
            }
            if (!worker.isInvisible())
            {
                worker.setInvisible(true);
            }
        }

        final ExpeditionLog expeditionLog = building.getFirstModuleOccurance(ExpeditionLogModule.class).getLog();

        //This is the adventure loop. 
        if (!job.getCraftedResults().isEmpty())
        {
            ItemStack currStack = job.getCraftedResults().poll();
            if (currStack.getItem() instanceof ItemAdventureToken)
            {
                if (currStack.hasTag())
                {
                    CompoundTag tag = currStack.getTag();
                    if (tag.contains(TAG_DAMAGE))
                    {
                        equipArmor(true);
                        worker.setItemSlot(EquipmentSlot.MAINHAND, findTool(ModEquipmentTypes.sword.get()));

                        DamageSource source = world.damageSources().source(DamageSourceKeys.NETHER);

                        //Set up the mob to do battle with
                        EntityType<?> mobType = EntityType.ZOMBIE;
                        if (tag.contains(TAG_ENTITY_TYPE))
                        {
                            mobType = EntityType.byString(tag.getString(TAG_ENTITY_TYPE)).orElse(EntityType.ZOMBIE);
                        }
                        LivingEntity mob = (LivingEntity) mobType.create(world);
                        float mobHealth = mob.getHealth();

                        // Calculate how much damage the mob will do if it lands a hit (Before armor)
                        float incomingDamage = tag.getFloat(TAG_DAMAGE);
                        incomingDamage -= incomingDamage * (getSecondarySkillLevel() * SECONDARY_DAMAGE_REDUCTION);

                        for (int hit = 0; mobHealth > 0 && !worker.isDeadOrDying(); hit++)
                        {
                            // Clear anti-hurt timers.
                            worker.hurtTime = 0;
                            worker.invulnerableTime = 0;
                            float damageToDo = BASE_PHYSICAL_DAMAGE;

                            // Figure out who gets to hit who this round
                            boolean doDamage = worker.getRandom().nextBoolean();
                            boolean takeDamage = worker.getRandom().nextBoolean();

                            // Calculate if the sword still exists, how much damage will be done to the mob
                            final ItemStack sword = worker.getItemBySlot(EquipmentSlot.MAINHAND);
                            if (!sword.isEmpty())
                            {
                                if (sword.getItem() instanceof SwordItem)
                                {
                                    damageToDo += ((SwordItem) sword.getItem()).getDamage();
                                }
                                else
                                {
                                    damageToDo += TinkersToolHelper.getDamage(sword);
                                }
                                damageToDo += EnchantmentHelper.getDamageBonus(sword, mob.getMobType()) / 2.5;
                                if (doDamage)
                                {
                                    sword.hurtAndBreak(1, worker, entity -> {
                                        // the sword broke; try to find another sword
                                        worker.setItemSlot(EquipmentSlot.MAINHAND, findTool(ModEquipmentTypes.sword.get()));
                                    });
                                }
                            }

                            // Hit the mob
                            if (doDamage)
                            {
                                mobHealth -= damageToDo;
                            }

                            // Get hit by the mob
                            if (takeDamage && !worker.hurt(source, incomingDamage))
                            {
                                //Shouldn't get here, but if we do we can force the damage. 
                                incomingDamage = worker.calculateDamageAfterAbsorbs(source, incomingDamage);
                                worker.setHealth(worker.getHealth() - incomingDamage);
                            }

                            // Every other round, heal up if possible, to compensate for all of this happening in a single tick. 
                            if (hit % 2 == 0)
                            {
                                float healAmount = checkHeal(worker);
                                final float saturationFactor = 0.25f;
                                if (healAmount > 0)
                                {
                                    worker.heal(healAmount);
                                    worker.getCitizenData().decreaseSaturation(healAmount * saturationFactor);
                                }
                            }
                            else
                            {
                                if (worker.getCitizenData().getSaturation() < AVERAGE_SATURATION)
                                {
                                    attemptToEat();
                                }
                            }
                        }

                        expeditionLog.setCitizen(worker);
                        logAllEquipment(expeditionLog, true);

                        if (worker.isDeadOrDying())
                        {
                            expeditionLog.setKilled();

                            // Stop processing loot table data, as the worker died before finishing the trip.
                            InventoryUtils.clearItemHandler(worker.getItemHandlerCitizen());
                            job.getCraftedResults().clear();
                            job.getProcessedResults().clear();
                            return IDLE;
                        }
                        else
                        {
                            // Generate loot for this mob, with all the right modifiers
                            LootParams context = this.getLootContext();
                            LootTable loot = world.getServer().getLootData().getLootTable(mob.getLootTable());
                            List<ItemStack> mobLoot = loot.getRandomItems(context);
                            job.addProcessedResultsList(mobLoot);

                            expeditionLog.addMob(mobType);
                            expeditionLog.addLoot(mobLoot);
                        }

                        worker.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        equipArmor(false);
                    }

                    if (currStack.getTag().contains(TAG_XP_DROPPED))
                    {
                        worker.getCitizenExperienceHandler().addExperience(worker.getCitizenItemHandler().applyMending(currStack.getTag().getInt(TAG_XP_DROPPED)));
                    }
                }
            }
            else if (!currStack.isEmpty())
            {
                int itemDelay = 0;
                if (currStack.getItem() instanceof BlockItem bi)
                {
                    final Block block = bi.getBlock();

                    ItemStack tool = findTool(block.defaultBlockState(), worker.blockPosition());
                    if (tool.getItem() instanceof TieredItem)
                    {
                        worker.setItemSlot(EquipmentSlot.MAINHAND, tool);

                        for (int i = 0; i < currStack.getCount() && !tool.isEmpty(); i++)
                        {
                            LootParams context = this.getLootContext();
                            LootTable loot = world.getServer().getLootData().getLootTable(block.getLootTable());
                            List<ItemStack> mobLoot = loot.getRandomItems(context);

                            job.addProcessedResultsList(mobLoot);
                            expeditionLog.addLoot(mobLoot);
                            tool.hurtAndBreak(1, worker, entity -> {});
                            if (tool.isEmpty())
                            {
                                // it's unlikely the worker will have a spare tool (mobs probably don't drop any), but
                                // just in case, let's not be silly and ignore it if we do have one
                                tool = findTool(block.defaultBlockState(), worker.blockPosition());
                                worker.setItemSlot(EquipmentSlot.MAINHAND, tool);
                            }
                            worker.getCitizenExperienceHandler().addExperience(worker.getCitizenItemHandler().applyMending(xpOnDrop(block)));

                            itemDelay += TICK_DELAY;
                        }

                        worker.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        logAllEquipment(expeditionLog, false);
                    }
                    else
                    {
                        //we didn't have a tool to use. 
                        itemDelay = TICK_DELAY;
                    }
                }
                else
                {
                    job.addProcessedResultsList(ImmutableList.of(currStack));
                    expeditionLog.addLoot(Collections.singletonList(currStack));
                    itemDelay = TICK_DELAY * currStack.getCount();
                }
                setDelay(itemDelay);
            }

            return getState();
        }

        if (!job.getProcessedResults().isEmpty())
        {
            if (!worker.isDeadOrDying())
            {
                expeditionLog.setStatus(ExpeditionLog.Status.RETURNING_HOME);
                ItemStack item = job.getProcessedResults().poll();

                if (InventoryUtils.addItemStackToItemHandler(worker.getItemHandlerCitizen(), item))
                {
                    worker.decreaseSaturationForContinuousAction();
                    worker.getCitizenExperienceHandler().addExperience(0.2);
                }
            }
            else
            {
                job.getProcessedResults().clear();
            }
            return getState();
        }

        expeditionLog.setStatus(ExpeditionLog.Status.COMPLETED);

        return NETHER_RETURN;
    }

    // calculate the XP coming from certain ores
    private int xpOnDrop(Block block)
    {
        RandomSource rnd = worker.getRandom();
        if (block == Blocks.COAL_ORE)
        {
            return rnd.nextInt(0, 2);
        }
        else if (block == Blocks.DIAMOND_ORE)
        {
            return rnd.nextInt(3, 7);
        }
        else if (block == Blocks.EMERALD_ORE)
        {
            return rnd.nextInt(3, 7);
        }
        else if (block == Blocks.LAPIS_ORE)
        {
            return rnd.nextInt(2, 5);
        }
        else if (block == Blocks.NETHER_QUARTZ_ORE)
        {
            return rnd.nextInt(2, 5);
        }
        else
        {
            return block == Blocks.NETHER_GOLD_ORE ? rnd.nextInt(0, 1) : 0;
        }
    }

    /**
     * Return from the nether by going visible, walking to building and preparing to close the portal
     */
    protected IAIState returnFromNether()
    {
        if (worker.isInvisible())
        {
            // we deliberately let this loop twice to give the worker time to teleport before becoming visible again
            returnFromVault(false);
            return getState();
        }

        //Shutdown Portal
        if (building.shallClosePortalOnReturn() && world.getBlockState(building.getPortalLocation()).is(Blocks.NETHER_PORTAL))
        {
            return NETHER_CLOSEPORTAL;
        }

        if (walkToBuilding())
        {
            return getState();
        }

        worker.getCitizenData().setIdleAtJob(true);
        job.setInNether(false);

        currentRecipeStorage = null;
        return INVENTORY_FULL;
    }

    /**
     * Open the portal to the nether if it's not open
     */
    protected IAIState openPortal()
    {
        // Attempt to light the portal and travel
        final BlockPos portal = building.getPortalLocation();
        if (portal != null && currentRecipeStorage != null)
        {
            if (walkToBlock(portal, 1))
            {
                return getState();
            }

            final BlockState block = world.getBlockState(portal);
            final Optional<PortalShape> ps = PortalShape.findPortalShape(world, portal, p -> p.isValid(), Direction.Axis.X);

            if (!ps.isPresent())
            {
                // Can't find the portal
                return IDLE;
            }

            if (!block.is(Blocks.NETHER_PORTAL))
            {
                useFlintAndSteel();
                ps.get().createPortalBlocks();
                return NETHER_LEAVE;
            }
        }
        return START_WORKING;
    }

    /**
     * Close the nether portal while idle around the building
     */
    protected IAIState closePortal()
    {
        final BlockPos portal = building.getPortalLocation();
        final BlockState block = world.getBlockState(portal);

        if (block.is(Blocks.NETHER_PORTAL))
        {
            if (walkToBlock(portal, 1))
            {
                return getState();
            }

            useFlintAndSteel();
            world.setBlockAndUpdate(building.getPortalLocation(), Blocks.AIR.defaultBlockState());
        }

        if (job.isInNether())
        {
            return NETHER_RETURN;
        }

        currentRecipeStorage = null;
        return INVENTORY_FULL;
    }

    /**
     * Helper to 'use' the flint and steel on portal open and close
     */
    private void useFlintAndSteel()
    {
        final ItemStack tool = findTool(ModEquipmentTypes.flint_and_steel.get());
        tool.hurtAndBreak(1, worker, entity -> {});
    }

    private ItemStack findItem(@NotNull final Predicate<ItemStack> predicate)
    {
        int slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), predicate);
        return slotOfStack < 0 ? ItemStack.EMPTY : worker.getInventoryCitizen().getStackInSlot(slotOfStack);
    }

    private ItemStack findTool(@NotNull final EquipmentTypeEntry tool)
    {
        return findItem(stack -> ItemStackUtils.hasEquipmentLevel(stack, tool, 0, building.getMaxEquipmentLevel()));
    }

    private ItemStack findTool(@NotNull final BlockState target, final BlockPos pos)
    {
        int slotOfStack = getMostEfficientTool(target, pos);
        return slotOfStack < 0 ? ItemStack.EMPTY : worker.getInventoryCitizen().getStackInSlot(slotOfStack);
    }

    /**
     * Equip or Un-equip armor etc.
     *
     * @param equipSlot Slot to attempt to modify
     * @param equip     true if equipping, false if clearing
     */
    private void setEquipSlot(EquipmentSlot equipSlot, boolean equip)
    {
        if (equip)
        {
            for (final List<GuardGear> itemList : itemsNeeded)
            {
                for (final GuardGear item : itemList)
                {
                    if (item.getType().equals(equipSlot)
                          && building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
                    {
                        if (!item.test(worker.getInventoryCitizen().getArmorInSlot(item.getType())))
                        {
                            final int toBeEquipped = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), item);
                            if (toBeEquipped > -1)
                            {
                                final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(toBeEquipped);
                                worker.getInventoryCitizen().transferArmorToSlot(item.getType(), toBeEquipped);
                                virtualEquipmentSlots.put(item.getType(), stack);
                            }
                        }
                    }
                }
            }
        }
        else
        {
            worker.getInventoryCitizen().moveArmorToInventory(equipSlot);
            virtualEquipmentSlots.put(equipSlot, ItemStack.EMPTY);
        }
    }

    private void equipArmor(final boolean equip)
    {
        setEquipSlot(EquipmentSlot.HEAD, equip);
        setEquipSlot(EquipmentSlot.CHEST, equip);
        setEquipSlot(EquipmentSlot.LEGS, equip);
        setEquipSlot(EquipmentSlot.FEET, equip);
    }

    private void logAllEquipment(@NotNull final ExpeditionLog expeditionLog, final boolean alreadyEquipped)
    {
        if (!alreadyEquipped)
        {
            equipArmor(true);
        }

        final IDeliverable edible = new StackList(getEdiblesList(), "Edible Food", 1);

        final List<ItemStack> equipment = new ArrayList<>();
        equipment.add(findTool(ModEquipmentTypes.sword.get()));

        equipment.add(worker.getInventoryCitizen().getArmorInSlot(EquipmentSlot.HEAD));
        equipment.add(worker.getInventoryCitizen().getArmorInSlot(EquipmentSlot.CHEST));
        equipment.add(worker.getInventoryCitizen().getArmorInSlot(EquipmentSlot.LEGS));
        equipment.add(worker.getInventoryCitizen().getArmorInSlot(EquipmentSlot.FEET));

        equipment.add(findTool(ModEquipmentTypes.pickaxe.get()));
        equipment.add(findTool(ModEquipmentTypes.axe.get()));
        equipment.add(findTool(ModEquipmentTypes.shovel.get()));
        equipment.add(findItem(edible::matches));
        expeditionLog.setEquipment(equipment);

        if (!alreadyEquipped)
        {
            equipArmor(false);
        }
    }

    /**
     * Put together the valid list of things to request for food
     */
    private List<ItemStack> getEdiblesList()
    {
        final Set<ItemStorage> allowedItems = building.getModule(NETHERMINER_MENU).getMenu();
        netherEdible.removeIf(item -> allowedItems.contains(new ItemStorage(item)));
        return netherEdible;
    }

    /**
     * Attempt to eat to restore some saturation
     */
    protected void attemptToEat()
    {
        final IDeliverable edible = new StackList(getEdiblesList(), "Edible Food", 1);
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(worker, edible::matches);
        if (slot > -1)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
            ItemStackUtils.consumeFood(stack, worker, null);
        }
    }

    /**
     * Make sure we have all the needed adventuring supplies This is very similar to the AbstractEntityAiFight "atBuildingActions" But doesn't handle shields, and doesn't equip or
     * leave equipped armor.
     */
    protected void checkAndRequestArmor()
    {
        for (final List<GuardGear> itemList : itemsNeeded)
        {
            for (final GuardGear item : itemList)
            {
                if (!(building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired()))
                {
                    continue;
                }

                int bestSlot = -1;
                int bestLevel = -1;
                IItemHandler bestHandler = null;

                if (virtualEquipmentSlots.containsKey(item.getType()) && !ItemStackUtils.isEmpty(virtualEquipmentSlots.get(item.getType())))
                {
                    bestLevel = item.getItemNeeded().getMiningLevel(virtualEquipmentSlots.get(item.getType()));
                }
                else
                {
                    ItemStack invItem = findItem(item::test);
                    if (!invItem.isEmpty())
                    {
                        if (!virtualEquipmentSlots.containsKey(item.getType()) || ItemStackUtils.isEmpty(virtualEquipmentSlots.get(item.getType())))
                        {
                            virtualEquipmentSlots.put(item.getType(), invItem);
                            bestLevel = item.getItemNeeded().getMiningLevel(invItem);
                        }
                    }
                    else
                    {
                        virtualEquipmentSlots.put(item.getType(), ItemStack.EMPTY);
                    }
                }

                final Map<IItemHandler, List<Integer>> items = InventoryUtils.findAllSlotsInProviderWith(building, item::test);
                if (items.isEmpty())
                {
                    // None found, check for equipped
                    if (ItemStackUtils.isEmpty(virtualEquipmentSlots.get(item.getType())))
                    {
                        // create request
                        checkForToolOrWeaponAsync(item.getItemNeeded(), item.getMinArmorLevel(), item.getMaxArmorLevel());
                    }
                }
                else
                {
                    // Compare levels
                    for (Map.Entry<IItemHandler, List<Integer>> entry : items.entrySet())
                    {
                        for (final Integer slot : entry.getValue())
                        {
                            final ItemStack stack = entry.getKey().getStackInSlot(slot);
                            if (ItemStackUtils.isEmpty(stack))
                            {
                                continue;
                            }

                            int currentLevel = item.getItemNeeded().getMiningLevel(stack);

                            if (currentLevel > bestLevel)
                            {
                                bestLevel = currentLevel;
                                bestSlot = slot;
                                bestHandler = entry.getKey();
                            }
                        }
                    }
                }

                // Transfer if needed
                if (bestHandler != null)
                {
                    if (!ItemStackUtils.isEmpty(virtualEquipmentSlots.get(item.getType())))
                    {
                        final int slot =
                          InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), stack -> stack == virtualEquipmentSlots.get(item.getType()));
                        if (slot > -1)
                        {
                            InventoryUtils.transferItemStackIntoNextFreeSlotInProvider(worker.getInventoryCitizen(), slot, building);
                        }
                    }

                    // Used for further comparisons, set to the right inventory slot afterwards
                    virtualEquipmentSlots.put(item.getType(), bestHandler.getStackInSlot(bestSlot));
                    InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(bestHandler, bestSlot, worker.getInventoryCitizen());
                }
            }
        }
    }

    protected IAIState checkAndRequestFood()
    {
        if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> building.getModule(NETHERMINER_MENU).getMenu().contains(new ItemStorage(stack))) > 16)
        {
            // We have enough food.
            return getState();
        }

        if (InventoryUtils.hasBuildingEnoughElseCount(building, stack -> building.getModule(NETHERMINER_MENU).getMenu().contains(new ItemStorage(stack)), 1) >= 1)
        {
            needsCurrently = new Tuple<>(stack -> building.getModule(NETHERMINER_MENU).getMenu().contains(new ItemStorage(stack)), 16);
            return GATHERING_REQUIRED_MATERIALS;
        }
        return getState();
    }

    /**
     * Checks the citizens health status and heals the citizen if necessary.
     */
    private float checkHeal(AbstractEntityCitizen citizen)
    {
        ICitizenData citizenData = citizen.getCitizenData();
        double healAmount = 0D;
        if (citizen.getHealth() < citizen.getMaxHealth())
        {
            final double limitDecrease = citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(SATLIMIT);

            if (citizenData.getSaturation() >= FULL_SATURATION + limitDecrease)
            {
                healAmount = 2 * (1.0 + citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(REGENERATION));
            }
            else if (citizenData.getSaturation() < LOW_SATURATION)
            {
                return (float) healAmount;
            }
            else
            {
                healAmount = 1 * (1.0 + citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(REGENERATION));
            }

            citizen.heal((float) healAmount);
        }

        return (float) healAmount;
    }
}
