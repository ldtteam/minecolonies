package com.minecolonies.coremod.entity.ai.citizen.netherworker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Predicate;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingNetherWorker;
import com.minecolonies.coremod.colony.jobs.JobNetherWorker;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.items.ItemAdventureToken;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PortalSize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScoreCriteria.RenderType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.IItemHandler;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGearBuilder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.*;

public class EntityAIWorkNether extends AbstractEntityAICrafting<JobNetherWorker, BuildingNetherWorker>
{

    /**
     * Delay for each of the crafting operations.
     */
    private static final int TICK_DELAY = 40;

    /**
     * NetherToken tag name for damage amount
     */
    private static final String TAG_DAMAGE = "damage-done";

    /**
     * NetherToken tag name for entity type doing the damage
     */
    private static final String TAG_ENTITY_TYPE = "entity-type";

    /**
     * NetherToken tag name for xp dropped by entity
     */
    private static final String TAG_XP_DROPPED = "xp-dropped";

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

    private final Map<EquipmentSlotType, ItemStack> virtualEquipmentSlots = new HashMap<>();


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
    protected IAIState decide()
    {
        if(job.isInNether())
        {
            if (!worker.isInvisible())
            {
                worker.setInvisible(true);
            }
            return NETHER_AWAY;
        }

        IAIState crafterState = super.decide();

        if(crafterState != IDLE && crafterState != START_WORKING)
        {
            return crafterState;
        }

        // Get Armor if available. 
        // This is async, so we could go to the nether without it. 
        checkAndRequestArmor();

        // Check for materials needed to go to the Nether: 
        IRecipeStorage rs = getOwnBuilding().getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class).getFirstRecipe(ItemStack::isEmpty);
        if(rs != null)
        {
            for(ItemStorage item : rs.getInput())
            {
                checkAndRequestMaterials(item.getItemStack(), item.getAmount());
            }
        }

        // Get other adventuring supplies. These are required. 
        // Done this way to get all the requests in parallel
        boolean haveAxe = checkForToolOrWeapon(ToolType.AXE);
        boolean havePick = checkForToolOrWeapon(ToolType.PICKAXE);
        boolean haveShovel = checkForToolOrWeapon(ToolType.SHOVEL);
        boolean haveSword = checkForToolOrWeapon(ToolType.SWORD);
        boolean haveLighter = checkForToolOrWeapon(ToolType.FLINT_N_STEEL);
        if(haveAxe || havePick || haveShovel || haveSword || haveLighter)
        {
            worker.getCitizenData().setIdleAtJob(true);
            return IDLE;
        }

        if (currentRecipeStorage == null)
        {
            final ICraftingBuildingModule module = getOwnBuilding().getFirstModuleOccurance(BuildingNetherWorker.CraftingModule.class);
            currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);
            if(getOwnBuilding().canDoTrip())
            {
                worker.getCitizenData().setIdleAtJob(true);
            }
            return getState();
        }
        else 
        {
            if (!getOwnBuilding().canDoTrip())
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
            
            if(checkResult == GET_RECIPE)
            {
                currentRecipeStorage = null;
                worker.getCitizenData().setIdleAtJob(true);
                return IDLE;
            }
            if(checkResult != CRAFT)
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
        if(!world.getScoreboard().hasObjective(OBJECTIVE_HUT_LEVEL))
        {
            world.getScoreboard().addObjective(OBJECTIVE_HUT_LEVEL, ScoreCriteria.DUMMY, new StringTextComponent("Nether Farm Building Level"), RenderType.INTEGER);
        }
        if(!world.getScoreboard().hasObjective(OBJECTIVE_SECONDARY_SKILL))
        {
            world.getScoreboard().addObjective(OBJECTIVE_SECONDARY_SKILL, ScoreCriteria.DUMMY, new StringTextComponent("Worker Secondary Skill Level"), RenderType.INTEGER);
        }
        final ScoreObjective hutLevelObjective = world.getScoreboard().getObjective(OBJECTIVE_HUT_LEVEL);
        final ScoreObjective secondarySkillLevelObjective = world.getScoreboard().getObjective(OBJECTIVE_SECONDARY_SKILL);

        Score s = world.getScoreboard().getOrCreatePlayerScore(worker.getScoreboardName(), hutLevelObjective);
        s.setScore(getOwnBuilding().getBuildingLevel());
        s = world.getScoreboard().getOrCreatePlayerScore(worker.getScoreboardName(), secondarySkillLevelObjective);
        s.setScore(getSecondarySkillLevel());

        // Attempt to light the portal and travel
        final BlockPos portal = getOwnBuilding().getPortalLocation();
        final BlockState block = world.getBlockState(portal);


        if(portal != null && currentRecipeStorage != null)
        {
            if (block.is(Blocks.NETHER_PORTAL))
            {
                if(walkToBlock(portal, 1))
                {
                    return getState();
                }
                worker.setInvisible(true);
                getOwnBuilding().recordTrip();
                
                job.getCraftedResults().addAll(currentRecipeStorage.fullfillRecipeAndCopy(getLootContext(), ImmutableList.of(worker.getItemHandlerCitizen()), false));

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
        //Ensure we stay put in the portal
        final BlockPos portal = getOwnBuilding().getPortalLocation();
        if(portal !=null && walkToBlock(portal, 1))
        {
            return getState();
        }

        //This is the adventure loop. 
        if (!job.getCraftedResults().isEmpty())
        {
            ItemStack currStack = job.getCraftedResults().poll();
            if(currStack.getItem() instanceof ItemAdventureToken)
            {
                if(currStack.hasTag())
                {
                    CompoundNBT tag = currStack.getTag();
                    if(tag.contains(TAG_DAMAGE))
                    {
                        
                        EntityType<?> mobType = EntityType.ZOMBIE;
                        if(tag.contains(TAG_ENTITY_TYPE))
                        {
                            mobType = EntityType.byString(tag.getString(TAG_ENTITY_TYPE)).orElse(EntityType.ZOMBIE);
                        }
                        LivingEntity mob = (LivingEntity) mobType.create(world);
                        
                        float damage = tag.getFloat(TAG_DAMAGE);
                        
                        damage -= damage * (getSecondarySkillLevel() * SECONDARY_DAMAGE_REDUCTION);

                        int slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), 
                            itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof SwordItem );
                        
                        if(slotOfStack != -1)
                        {
                            int swordDamage = (int) Math.floor(mob.getHealth() / 3f);
                            worker.getInventoryCitizen().damageInventoryItem(slotOfStack, swordDamage, worker, entity -> {});
                        } else {
                            //Double the damage, we broke our sword earlier so are punching
                            damage *= 2;
                        }

                        DamageSource source = new DamageSource("nether");

                        setEquipSlot(EquipmentSlotType.HEAD, true);
                        setEquipSlot(EquipmentSlotType.CHEST, true);
                        setEquipSlot(EquipmentSlotType.LEGS, true);
                        setEquipSlot(EquipmentSlotType.FEET, true);
                        
                        // Clear anti-hurt timers.
                        worker.hurtTime = 0;
                        worker.invulnerableTime = 0;
                        if(!worker.hurt(source, damage))
                        {
                            //Shouldn't get here, but if we do we can force the damage. 
                            damage = worker.calculateDamageAfterAbsorbs(source, damage);
                            worker.setHealth(worker.getHealth() - damage);
                        }
                        setEquipSlot(EquipmentSlotType.HEAD, false);
                        setEquipSlot(EquipmentSlotType.CHEST, false);
                        setEquipSlot(EquipmentSlotType.LEGS, false);
                        setEquipSlot(EquipmentSlotType.FEET, false);

                        if (worker.isDeadOrDying())
                        {
                            // Stop processing loot table data, as the worker died before finishing the trip.
                            InventoryUtils.clearItemHandler(worker.getItemHandlerCitizen());
                            job.getCraftedResults().clear();
                            job.getProcessedResults().clear();
                            return IDLE;
                        }
                    }
                    if (currStack.getTag().contains(TAG_XP_DROPPED))
                    {
                        worker.getCitizenExperienceHandler().addExperience(currStack.getTag().getInt(TAG_XP_DROPPED));
                    }
                }
            }
            else
            {
                boolean harvested = true;
                if(currStack.getItem() instanceof BlockItem)
                {
                    final BlockItem bi = (BlockItem) currStack.getItem();
                    final Block block = bi.getBlock();
                    final net.minecraftforge.common.ToolType toolType;

                    if(block.getHarvestTool(block.defaultBlockState()) == null)
                    {
                        toolType = net.minecraftforge.common.ToolType.PICKAXE;
                    } else {
                        toolType = block.getHarvestTool(block.defaultBlockState());
                    }

                    int slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), 
                        itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem().getToolTypes(itemStack).contains(toolType));

                    if(slotOfStack != -1)
                    {
                        harvested = !worker.getInventoryCitizen().damageInventoryItem(slotOfStack, currStack.getCount(), worker, entity -> {});
                    }
                    else
                    {
                        //we didn't have a tool to use. 
                        harvested = false; 
                    }
                }
                if(harvested)
                {
                    job.getProcessedResults().add(currStack);
                    setDelay(currStack.getCount() * TICK_DELAY);
                }
            }

            return getState();
        }

        if(!job.getProcessedResults().isEmpty())
        {
            if(!worker.isDeadOrDying())
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
    
    /**
     * Return from the nether by going visible, walking to building and preparing to close the portal
     */
    protected IAIState returnFromNether()
    {

        if (currentRecipeStorage == null)
        {  
            if(worker.isInvisible())
            {
                worker.setInvisible(false);
            }
            return IDLE;
        }

        if(worker.isInvisible())
        {
            worker.setInvisible(false);
        }

        if(walkToBuilding())
        {
            return getState();
        }
        
        worker.getCitizenData().setIdleAtJob(true);
        job.setInNether(false);
        
        //Shutdown Portal
        return NETHER_CLOSEPORTAL;
    }

    /**
     * Open the portal to the nether if it's not open
     */
    protected IAIState openPortal()
    {
        // Attempt to light the portal and travel
        final BlockPos portal = getOwnBuilding().getPortalLocation();
        final BlockState block = world.getBlockState(portal);
        if(portal != null && currentRecipeStorage != null)
        {
            final Optional<PortalSize> ps = PortalSize.findPortalShape(world, portal, p -> p.isValid(), Direction.Axis.X);

            if(!ps.isPresent())
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

        if(block.is(Blocks.NETHER_PORTAL))
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
        if(slot > -1)
        {
            worker.getInventoryCitizen().damageInventoryItem(slot, 1, worker, entity -> {});
        }
    }

    /**
     * Equip or Un-equip armor etc.
     * @param equipSlot Slot to attempt to modify
     * @param equip true if equipping, false if clearing
     */
    private void setEquipSlot(EquipmentSlotType equipSlot, boolean equip)
    {
        if(equip)
        {
            final BuildingNetherWorker building = getOwnBuilding();

            for (final List<GuardGear> itemList : itemsNeeded)
            {
                for (final GuardGear item : itemList)
                {
                    
                    if (ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())) && item.getType().equals(equipSlot) && building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
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
     * Check to see if we have the item being requested, and generate request if not. 
     * @param item
     * @param count
     * @return
     */
    protected boolean checkAndRequestMaterials(ItemStack item, int count)
    {
        final Predicate<ItemStack> isItem = checkItem -> !checkItem.isEmpty() && checkItem.getItem() == item.getItem();
        final int itemsInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), isItem);
        if (itemsInInv <= 0)
        {
            final int numberOfBooksInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), isItem);
            if (numberOfBooksInBuilding > 0)
            {
                return true;
            }
            checkIfRequestForItemExistOrCreateAsynch(new ItemStack(item.getItem(), count));
            return false;
        }
        return true;
    }

    /**
     * Make sure we have all the needed adventuring supplies
     * This is very similar to the AbstractEntityAiFight "atBuildingActions"
     * But doesn't handle shields, and doesn't equip or leave equipped armor. 
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
                        if(!virtualEquipmentSlots.containsKey(item.getType()) || ItemStackUtils.isEmpty(virtualEquipmentSlots.get(item.getType())))
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
}
