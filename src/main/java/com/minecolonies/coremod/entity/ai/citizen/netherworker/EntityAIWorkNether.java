package com.minecolonies.coremod.entity.ai.citizen.netherworker;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.compatibility.tinkers.TinkersToolHelper;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGearBuilder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingNetherWorker;
import com.minecolonies.coremod.colony.jobs.JobNetherWorker;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.items.ItemAdventureToken;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PortalSize;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreCriteria.RenderType;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.*;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingNetherWorker.FOOD_EXCLUSION_LIST;

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
    private final Map<EquipmentSlotType, ItemStack> virtualEquipmentSlots = new HashMap<>();

    /**
     * Edibles that the worker will attempt to eat while in the nether (unfiltered)
     */
    final List<ItemStack> netherEdible = IColonyManager.getInstance()
      .getCompatibilityManager()
      .getEdibles(getOwnBuilding().getBuildingLevel() - 1)
      .stream()
      .map(item -> item.getItemStack())
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
        worker.setInvisible(true);
        BlockPos vaultPos = getOwnBuilding().getVaultLocation();
        if (vaultPos != null)
        {
            worker.moveTo(vaultPos.getX() + 0.5, vaultPos.getY(), vaultPos.getZ() + 0.5, worker.getRotationYaw(), worker.getRotationPitch());
            worker.getNavigation().stop();
        }
    }

    private void returnFromVault()
    {
        BlockPos vaultPos = getOwnBuilding().getVaultLocation();
        BlockPos portalPos = getOwnBuilding().getPortalLocation();
        if (portalPos != null && vaultPos != null && EntityUtils.isLivingAtSite(worker, vaultPos.getX(), vaultPos.getY(), vaultPos.getZ(), 2))
        {
            worker.moveTo(portalPos.getX() + 0.5, portalPos.getY(), portalPos.getZ() + 0.5, worker.getRotationYaw(), worker.getRotationPitch());
            worker.getNavigation().stop();
        }
        worker.setInvisible(false);
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
            returnFromVault();
        }

        IAIState crafterState = super.decide();

        if (crafterState != IDLE && crafterState != START_WORKING)
        {
            return crafterState;
        }

        // Get Armor if available. 
        // This is async, so we could go to the nether without it. 
        checkAndRequestArmor();

        // Check for materials needed to go to the Nether: 
        IRecipeStorage rs = getOwnBuilding().getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class).getFirstRecipe(ItemStack::isEmpty);
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
            return IDLE;
        }

        final BlockPos portal = getOwnBuilding().getPortalLocation();
        if (portal == null)
        {
            Log.getLogger().warn("--- Missing Portal Tag In Nether Worker Building! Aborting Operation! ---");
            return IDLE;
        }

        // Make sure we have a stash of some food 
        checkAndRequestFood(16);

        // Get other adventuring supplies. These are required. 
        // Done this way to get all the requests in parallel
        boolean missingAxe = checkForToolOrWeapon(ToolType.AXE);
        boolean missingPick = checkForToolOrWeapon(ToolType.PICKAXE);
        boolean missingShovel = checkForToolOrWeapon(ToolType.SHOVEL);
        boolean missingSword = checkForToolOrWeapon(ToolType.SWORD);
        boolean missingLighter = checkForToolOrWeapon(ToolType.FLINT_N_STEEL);
        if (missingAxe || missingPick || missingShovel || missingSword || missingLighter)
        {
            worker.getCitizenData().setIdleAtJob(true);
            return IDLE;
        }

        if (currentRecipeStorage == null)
        {
            final ICraftingBuildingModule module = getOwnBuilding().getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class);
            currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);
            if (getOwnBuilding().isReadyForTrip())
            {
                worker.getCitizenData().setIdleAtJob(true);
            }

            if (currentRecipeStorage == null && getOwnBuilding().shallClosePortalOnReturn())
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
            if (!getOwnBuilding().isReadyForTrip())
            {
                worker.getCitizenData().setIdleAtJob(false);
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
            job.setInNether(true);
            worker.getCitizenData().setIdleAtJob(true);
            return IDLE;
        }

        // Set up Objectives and scores. 
        Scoreboard sb = world.getScoreboard();
        if(sb.getObjective(OBJECTIVE_HUT_LEVEL) == null)
        {
            sb.addObjective(OBJECTIVE_HUT_LEVEL, ScoreCriteria.DUMMY, new StringTextComponent("Worker Building Level"), RenderType.INTEGER);
        }
        if(sb.getObjective(OBJECTIVE_SECONDARY_SKILL) == null)
        {
            sb.addObjective(OBJECTIVE_SECONDARY_SKILL, ScoreCriteria.DUMMY, new StringTextComponent("Worker Secondary Skill Level"), RenderType.INTEGER);
        }
        final ScoreObjective hutLevelObjective = sb.getObjective(OBJECTIVE_HUT_LEVEL);
        final ScoreObjective secondarySkillLevelObjective = sb.getObjective(OBJECTIVE_SECONDARY_SKILL);

        Score s = sb.getOrCreatePlayerScore(worker.getScoreboardName(), hutLevelObjective);
        s.setScore(getOwnBuilding().getBuildingLevel());
        s = sb.getOrCreatePlayerScore(worker.getScoreboardName(), secondarySkillLevelObjective);
        s.setScore(getSecondarySkillLevel());

        // Attempt to light the portal and travel
        final BlockPos portal = getOwnBuilding().getPortalLocation();
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
                getOwnBuilding().recordTrip();

                List<ItemStack> result = currentRecipeStorage.fullfillRecipeAndCopy(getLootContext(), ImmutableList.of(worker.getItemHandlerCitizen()), false);
                if (result != null)
                {
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
        if (getOwnBuilding().getVaultLocation() == null)
        {
            //Ensure we stay put in the portal
            final BlockPos portal = getOwnBuilding().getPortalLocation();
            if (portal != null && walkToBlock(portal, 1))
            {
                return getState();
            }
            if (!worker.isInvisible())
            {
                worker.setInvisible(true);
            }
        }

        //This is the adventure loop. 
        if (!job.getCraftedResults().isEmpty())
        {
            ItemStack currStack = job.getCraftedResults().poll();
            if (currStack.getItem() instanceof ItemAdventureToken)
            {
                if (currStack.hasTag())
                {
                    CompoundNBT tag = currStack.getTag();
                    if(tag.contains(TAG_DAMAGE))
                    {
                        int slotOfSwordStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), 
                            itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof SwordItem );
                        DamageSource source = new DamageSource("nether");

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

                        setEquipSlot(EquipmentSlotType.MAINHAND, true);
                        setEquipSlot(EquipmentSlotType.HEAD, true);
                        setEquipSlot(EquipmentSlotType.CHEST, true);
                        setEquipSlot(EquipmentSlotType.LEGS, true);
                        setEquipSlot(EquipmentSlotType.FEET, true);
                        
                        for(int hit = 0; mobHealth > 0 && ! worker.isDeadOrDying(); hit++)
                        {
                            // Clear anti-hurt timers.
                            worker.hurtTime = 0;
                            worker.invulnerableTime = 0;
                            float damageToDo = BASE_PHYSICAL_DAMAGE;

                            // Figure out who gets to hit who this round
                            boolean doDamage = worker.getRandom().nextBoolean();
                            boolean takeDamage = worker.getRandom().nextBoolean();

                            // Calculate if the sword still exists, how much damage will be done to the mob
                            if (slotOfSwordStack != -1)
                            {
                                final ItemStack sword = worker.getInventoryCitizen().getStackInSlot(slotOfSwordStack);
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
                                        sword.hurtAndBreak(1, mob, entity -> {});
                                    }
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


                        if (worker.isDeadOrDying())
                        {
                            // Stop processing loot table data, as the worker died before finishing the trip.
                            InventoryUtils.clearItemHandler(worker.getItemHandlerCitizen());
                            job.getCraftedResults().clear();
                            job.getProcessedResults().clear();
                            return IDLE;
                        }
                        else
                        {
                            // Generate loot for this mob, with all the right modifiers
                            LootContext context = this.getLootContext();
                            LootTable loot = world.getServer().getLootTables().get(mob.getLootTable());
                            List<ItemStack> mobLoot = loot.getRandomItems(context);
                            job.addProcessedResultsList(mobLoot);
                        }
                    }

                    if (currStack.getTag().contains(TAG_XP_DROPPED))
                    {
                        worker.getCitizenExperienceHandler().addExperience(worker.getCitizenItemHandler().applyMending(currStack.getTag().getInt(TAG_XP_DROPPED)));
                    }

                    setEquipSlot(EquipmentSlotType.MAINHAND, false);
                    setEquipSlot(EquipmentSlotType.HEAD, false);
                    setEquipSlot(EquipmentSlotType.CHEST, false);
                    setEquipSlot(EquipmentSlotType.LEGS, false);
                    setEquipSlot(EquipmentSlotType.FEET, false);
                }
            }
            else
            {
                int itemDelay = 0;
                if (currStack.getItem() instanceof BlockItem)
                {
                    final BlockItem bi = (BlockItem) currStack.getItem();
                    final Block block = bi.getBlock();
                    final net.minecraftforge.common.ToolType toolType;

                    if(block.getHarvestTool(block.defaultBlockState()) == null)
                    {
                        toolType = net.minecraftforge.common.ToolType.PICKAXE;
                    }
                    else 
                    {
                        toolType = block.getHarvestTool(block.defaultBlockState());
                    }

                    int slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), 
                        itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem().getToolTypes(itemStack).contains(toolType));

                    if(slotOfStack != -1)
                    {
                        ItemStack tool = worker.getInventoryCitizen().getStackInSlot(slotOfStack);
                        if (tool.getItem() instanceof ToolItem)
                        {
                            worker.setItemSlot(EquipmentSlotType.MAINHAND, tool);
                            
                            for(int i=0; i < currStack.getCount() && !tool.isEmpty() ; i++)
                            {
                                LootContext context = this.getLootContext();
                                LootTable loot = world.getServer().getLootTables().get(block.getLootTable());
                                List<ItemStack> mobLoot = loot.getRandomItems(context);

                                job.addProcessedResultsList(mobLoot);
                                tool.hurtAndBreak(1, worker, entity -> {});
                                worker.getCitizenExperienceHandler().addExperience(worker.getCitizenItemHandler().applyMending(xpOnDrop(block)));

                                itemDelay += TICK_DELAY;
                            }
                        }
                        worker.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
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

        return NETHER_RETURN;
    }

    // calculate the XP coming from certain ores
    private int xpOnDrop(Block block)
    {
        Random rnd = worker.getRandom();
        if (block == Blocks.COAL_ORE)
        {
            return MathHelper.nextInt(rnd, 0, 2);
        }
        else if (block == Blocks.DIAMOND_ORE)
        {
            return MathHelper.nextInt(rnd, 3, 7);
        }
        else if (block == Blocks.EMERALD_ORE)
        {
            return MathHelper.nextInt(rnd, 3, 7);
        }
        else if (block == Blocks.LAPIS_ORE)
        {
            return MathHelper.nextInt(rnd, 2, 5);
        }
        else if (block == Blocks.NETHER_QUARTZ_ORE)
        {
            return MathHelper.nextInt(rnd, 2, 5);
        }
        else
        {
            return block == Blocks.NETHER_GOLD_ORE ? MathHelper.nextInt(rnd, 0, 1) : 0;
        }
    }

    /**
     * Return from the nether by going visible, walking to building and preparing to close the portal
     */
    protected IAIState returnFromNether()
    {

        if (currentRecipeStorage == null)
        {
            if (worker.isInvisible())
            {
                returnFromVault();
            }
            return IDLE;
        }

        if (worker.isInvisible())
        {
            returnFromVault();
            return getState();
        }

        if (walkToBuilding())
        {
            return getState();
        }

        worker.getCitizenData().setIdleAtJob(true);
        job.setInNether(false);

        //Shutdown Portal
        if (getOwnBuilding().shallClosePortalOnReturn())
        {
            return NETHER_CLOSEPORTAL;
        }

        currentRecipeStorage = null;
        return INVENTORY_FULL;
    }

    /**
     * Open the portal to the nether if it's not open
     */
    protected IAIState openPortal()
    {
        // Attempt to light the portal and travel
        final BlockPos portal = getOwnBuilding().getPortalLocation();
        if (portal != null && currentRecipeStorage != null)
        {
            final BlockState block = world.getBlockState(portal);
            final Optional<PortalSize> ps = PortalSize.findPortalShape(world, portal, p -> p.isValid(), Direction.Axis.X);

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
        final BlockState block = world.getBlockState(getOwnBuilding().getPortalLocation());

        if (block.is(Blocks.NETHER_PORTAL))
        {
            useFlintAndSteel();
            world.setBlockAndUpdate(getOwnBuilding().getPortalLocation(), Blocks.AIR.defaultBlockState());
        }

        currentRecipeStorage = null;
        return INVENTORY_FULL;
    }

    /**
     * Helper to 'use' the flint and steel on portal open and close
     */
    private void useFlintAndSteel()
    {
        final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), i -> i.getItem() instanceof FlintAndSteelItem);
        if (slot > -1)
        {
            worker.getInventoryCitizen().damageInventoryItem(slot, 1, worker, entity -> {});
        }
    }

    /**
     * Equip or Un-equip armor etc.
     *
     * @param equipSlot Slot to attempt to modify
     * @param equip     true if equipping, false if clearing
     */
    private void setEquipSlot(EquipmentSlotType equipSlot, boolean equip)
    {
        if (equip)
        {
            final BuildingNetherWorker building = getOwnBuilding();

            for (final List<GuardGear> itemList : itemsNeeded)
            {
                for (final GuardGear item : itemList)
                {

                    if (ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())) && item.getType().equals(equipSlot)
                          && building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
                    {
                        final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), item::test);

                        if (slot > -1)
                        {
                            final ItemStack toBeEquipped = worker.getInventoryCitizen().getStackInSlot(slot);
                            worker.setItemSlot(item.getType(), toBeEquipped);
                            virtualEquipmentSlots.put(item.getType(), toBeEquipped);
                        }
                    }
                }
            }
        }
        else
        {
            worker.setItemSlot(equipSlot, ItemStack.EMPTY);
            virtualEquipmentSlots.put(equipSlot, ItemStack.EMPTY);
        }
    }

    /**
     * Put together the valid list of things to request for food
     */
    private List<ItemStack> getEdiblesList()
    {
        final List<ItemStorage> allowedItems = getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST)).getList();
        netherEdible.removeIf(item -> allowedItems.contains(new ItemStorage(item)));
        return netherEdible;
    }

    /**
     * Check if we need to request food, and do so if necessary.
     */
    protected void checkAndRequestFood(int itemCount)
    {
        List<ItemStack> edibleList = getEdiblesList();
        final IDeliverable edible = new StackList(edibleList, "Edible Food", itemCount);

        int count = InventoryUtils.getItemCountInItemHandler(worker.getItemHandlerCitizen(), edible::matches);
        if (count >= itemCount)
        {
            return;
        }

        if (!getOwnBuilding().hasWorkerOpenRequestsOfType(-1, TypeToken.of(edible.getClass()))
              && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(edible.getClass())))
        {
            getOwnBuilding().createRequest(edible, true);
        }
    }

    /**
     * Attempt to eat to restore some saturation
     */
    protected void attemptToEat()
    {
        final IDeliverable edible = new StackList(getEdiblesList(), "Edible Food", 1);
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(worker, edible::matches);
        final ICitizenData citizenData = worker.getCitizenData();
        if (slot > -1)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
            final Food itemFood = stack.getItem().getFoodProperties();
            final double satIncrease =
              itemFood.getNutrition() * (1.0 + worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(SATURATION));

            citizenData.increaseSaturation(satIncrease / 2.0);
            citizenData.getInventory().extractItem(slot, 1, false);

            final ItemStack containerItem = stack.getContainerItem();

            if (containerItem != null && !(containerItem.getItem() instanceof AirItem))
            {
                if (citizenData.getInventory().isFull())
                {
                    InventoryUtils.spawnItemStack(
                      worker.level,
                      worker.getX(),
                      worker.getY(),
                      worker.getZ(),
                      containerItem
                    );
                }
                else
                {
                    InventoryUtils.addItemStackToItemHandler(worker.getItemHandlerCitizen(), containerItem);
                }
            }
        }
    }

    /**
     * Make sure we have all the needed adventuring supplies This is very similar to the AbstractEntityAiFight "atBuildingActions" But doesn't handle shields, and doesn't equip or
     * leave equipped armor.
     */
    protected void checkAndRequestArmor()
    {
        final BuildingNetherWorker building = getOwnBuilding();
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
                    bestLevel = ItemStackUtils.getMiningLevel(virtualEquipmentSlots.get(item.getType()), item.getItemNeeded());
                }
                else
                {
                    int equipSlot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), item::test);
                    if (equipSlot > -1)
                    {
                        ItemStack invItem = worker.getItemHandlerCitizen().getStackInSlot(equipSlot);
                        if (!virtualEquipmentSlots.containsKey(item.getType()) || ItemStackUtils.isEmpty(virtualEquipmentSlots.get(item.getType())))
                        {
                            virtualEquipmentSlots.put(item.getType(), invItem);
                            bestLevel = ItemStackUtils.getMiningLevel(invItem, item.getItemNeeded());
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
                        checkForToolorWeaponASync(item.getItemNeeded(), item.getMinArmorLevel(), item.getMaxArmorLevel());
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

                            int currentLevel = ItemStackUtils.getMiningLevel(stack, item.getItemNeeded());

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

    /**
     * Checks the citizens health status and heals the citizen if necessary.
     */
    private float checkHeal(AbstractEntityCitizen citizen)
    {
        ICitizenData citizenData = citizen.getCitizenData();
        double healAmount = 0D;
        if (citizen.getHealth() < citizen.getMaxHealth())
        {
            final double limitDecrease = citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(SATLIMIT);

            if (citizenData.getSaturation() >= FULL_SATURATION + limitDecrease)
            {
                healAmount = 2 * (1.0 + citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(REGENERATION));
            }
            else if (citizenData.getSaturation() < LOW_SATURATION)
            {
                return (float) healAmount;
            }
            else
            {
                healAmount = 1 * (1.0 + citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(REGENERATION));
            }

            citizen.heal((float) healAmount);
            if (healAmount > 0.1D)
            {
                citizenData.markDirty();
            }
        }

        return (float) healAmount;
    }
}
