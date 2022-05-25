package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.CAN_EAT;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.FURNACE_USER_NO_FOOD;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_CITIZEN_COOK_SERVE_PLAYER;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook.FOOD_EXCLUSION_LIST;

/**
 * Cook AI class.
 */
public class EntityAIWorkCook extends AbstractEntityAIUsesFurnace<JobCook, BuildingCook>
{
    /**
     * The amount of food which should be served to the worker.
     */
    public static final int SATURATION_TO_SERVE = 16;

    /**
     * Delay between each serving.
     */
    private static final int SERVE_DELAY = 30;

    /**
     * Level at which the cook should give some food to the player.
     */
    private static final int LEVEL_TO_FEED_PLAYER      = 10;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<AbstractEntityCitizen> citizenToServe = new ArrayList<>();

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<Player> playerToServe = new ArrayList<>();

    /**
     * Cooking icon
     */
    private final static VisibleCitizenStatus COOK =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/cook.png"), "com.minecolonies.gui.visiblestatus.cook");

    /**
     * The list of items needed for the assistant
     */
    private Set<ItemStorage> reservedItemCache = new HashSet<>();

    /**
     * Constructor for the Cook. Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkCook(@NotNull final JobCook job)
    {
        super(job);
        super.registerTargets(
          new AITarget(COOK_SERVE_FOOD_TO_CITIZEN, this::serveFoodToCitizen, SERVE_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingCook> getExpectedBuildingClass()
    {
        return BuildingCook.class;
    }

    /**
     * Very simple action, cook straightly extract it from the furnace.
     *
     * @param furnace the furnace to retrieve from.
     */
    @Override
    protected void extractFromFurnace(final FurnaceBlockEntity furnace)
    {
        if(!building.getIsCooking())
        {
            InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
                new InvWrapper(furnace), RESULT_SLOT,
                worker.getInventoryCitizen());
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            this.incrementActionsDoneAndDecSaturation();
        }
    }

    @Override
    public IAIState startWorking()
    {
        reservedItemCache.clear();
        return super.startWorking();
    }

    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        //Only return true if the item isn't queued for a recipe. 
        if(!building.getIsCooking() )
        {
            return ItemStackUtils.ISCOOKABLE.test(stack) && !isItemStackForAssistant(stack)
                     && !building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST)).isItemInList(new ItemStorage(MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack)));
        }
        return false;
    }

    @Override
    protected boolean reachedMaxToKeep()
    {
        if (super.reachedMaxToKeep())
        {
            return true;
        }
        final int buildingLimit = Math.max(1, building.getBuildingLevel() * building.getBuildingLevel()) * SLOT_PER_LINE;
        return InventoryUtils.getCountFromBuildingWithLimit(building, ItemStackUtils.CAN_EAT.and(stack -> stack.getItem().getFoodProperties().getNutrition() >= building.getBuildingLevel() - 1), stack -> stack.getMaxStackSize() * 6) > buildingLimit;
    }

    @Override
    public void requestSmeltable()
    {
        final IRequestable smeltable = getSmeltAbleClass();
        if (smeltable != null
              && !building.hasWorkerOpenRequestsOfType(-1, TypeToken.of(smeltable.getClass()))
              && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(smeltable.getClass())))
        {
            building.createRequest(smeltable, true);
        }
    }

    /**
     * Serve food to customer
     * <p>
     * If no customer, transition to START_WORKING. If we need to walk to the customer, repeat this state with tiny delay. If the customer has a full inventory, report and remove
     * customer, delay and repeat this state. If we have food, then COOK_SERVE. If no food in the building, transition to START_WORKING. If we were able to get the stored food,
     * then COOK_SERVE. If food is no longer available, delay and transition to START_WORKING. Otherwise, give the customer some food, then delay and repeat this state.
     *
     * @return next IAIState
     */
    private IAIState serveFoodToCitizen()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SERVING));

        if (citizenToServe.isEmpty() && playerToServe.isEmpty())
        {
            return START_WORKING;
        }

        worker.getCitizenData().setVisibleStatus(COOK);

        final Entity living = citizenToServe.isEmpty() ? playerToServe.get(0) : citizenToServe.get(0);

        if (!building.isInBuilding(living.blockPosition()))
        {
            worker.getNavigation().stop();
            removeFromQueue();
            return START_WORKING;
        }

        if (walkToBlock(new BlockPos(living.position())))
        {
            return getState();
        }

        final IItemHandler handler = citizenToServe.isEmpty() ? new InvWrapper(playerToServe.get(0).getInventory()) : citizenToServe.get(0).getInventoryCitizen();

        if (InventoryUtils.isItemHandlerFull(handler))
        {
            if (!citizenToServe.isEmpty())
            {
                final int foodSlot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), stack -> ItemStackUtils.CAN_EAT.test(stack) && canEat(stack, citizenToServe.isEmpty() ? null : citizenToServe.get(0)));
                if (foodSlot != -1)
                {
                    final ItemStack stack = worker.getInventoryCitizen().extractItem(foodSlot, 1, false);
                    if (stack.isEdible())
                    {
                        citizenToServe.get(0).getCitizenData().increaseSaturation(stack.getItem().getFoodProperties().getNutrition() / 2.0);
                    }
                }
            }

            removeFromQueue();
            return getState();
        }
        else if (InventoryUtils.hasItemInItemHandler(handler, stack -> CAN_EAT.test(stack) && canEat(stack, citizenToServe.isEmpty() ? null : citizenToServe.get(0))))
        {
            removeFromQueue();
            return getState();
        }

        if (!InventoryUtils.transferFoodUpToSaturation(worker, handler, building.getBuildingLevel() * SATURATION_TO_SERVE, stack -> CAN_EAT.test(stack) && canEat(stack, citizenToServe.isEmpty() ? null : citizenToServe.get(0))))
        {
            removeFromQueue();
            return getState();
        }

        if (!citizenToServe.isEmpty() && citizenToServe.get(0).getCitizenData() != null)
        {
            citizenToServe.get(0).getCitizenData().setJustAte(true);
        }

        if (citizenToServe.isEmpty() && living instanceof Player)
        {
            MessageUtils.format(MESSAGE_INFO_CITIZEN_COOK_SERVE_PLAYER, worker.getName().getString()).sendTo((Player) living);
        }
        removeFromQueue();

        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
        return START_WORKING;
    }

    /**
     * Check if the entity to serve can eat the given stack
     * @param stack the stack to check
     * @param citizen the citizen to check for.
     * @return true if the stack can be eaten
     */
    private boolean canEat(final ItemStack stack, final AbstractEntityCitizen citizen)
    {
        final ItemListModule module = worker.getCitizenData().getWorkBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST));
        if (module.isItemInList(new ItemStorage(stack)))
        {
            return false;
        }
        if (citizen != null )
        {
            final IBuilding building = citizen.getCitizenData().getWorkBuilding();
            if (building != null)
            {
                return building.canEat(stack);
            }
        }
        return true;
    }

    /**
     * Remove the last citizen or player from the queue.
     */
    private void removeFromQueue()
    {
        if (citizenToServe.isEmpty())
        {
            playerToServe.remove(0);
        }
        else
        {
            citizenToServe.remove(0);
        }
    }

    /**
     * Checks if the cook has anything important to do before going to the default furnace user jobs. First calculate the building range if not cached yet. Then check for citizens
     * around the building. If no citizen around switch to default jobs. If citizens around check if food in inventory, if not, switch to gather job. If food in inventory switch to
     * serve job.
     *
     * @return the next IAIState to transfer to.
     */
    @Override
    protected IAIState checkForImportantJobs()
    {
        this.reservedItemCache.clear(); //Clear the cache of current pending work

        citizenToServe.clear();
        final List<AbstractEntityCitizen> citizenList = WorldUtil.getEntitiesWithinBuilding(world, AbstractEntityCitizen.class, building, null)
                                                          .stream()
                                                          .filter(cit -> !(cit.getCitizenJobHandler().getColonyJob() instanceof JobCook)
                                                                           && cit.shouldBeFed()
                                                                           && !InventoryUtils.hasItemInItemHandler(cit.getItemHandlerCitizen(), stack -> CAN_EAT.test(stack) && canEat(stack, cit)))
                                                          .sorted(Comparator.comparingInt(a -> (a.getCitizenJobHandler().getColonyJob() == null ? 1 : 0)))
                                                          .collect(Collectors.toList());

        final List<? extends Player> playerList = WorldUtil.getEntitiesWithinBuilding(world, Player.class,
          building, player -> player != null
                      && player.getFoodData().getFoodLevel() < LEVEL_TO_FEED_PLAYER
                      && building.getColony().getPermissions().hasPermission(player, Action.MANAGE_HUTS)
        );

        playerToServe.addAll(playerList);

        boolean hasFoodInBuilding = false;
        for (final AbstractEntityCitizen citizen : citizenList)
        {
            final Predicate<ItemStack> foodPredicate = stack -> ItemStackUtils.CAN_EAT.test(stack) && !isItemStackForAssistant(stack) && canEat(stack, citizen);
            if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), foodPredicate))
            {
                citizenToServe.add(citizen);
            }
            else
            {
                if (InventoryUtils.hasItemInProvider(building, foodPredicate))
                {
                    hasFoodInBuilding = true;
                    needsCurrently = new Tuple<>(foodPredicate, STACKSIZE);
                }
            }
        }

        if (!citizenToServe.isEmpty())
        {
            return COOK_SERVE_FOOD_TO_CITIZEN;
        }

        if (hasFoodInBuilding)
        {
            return GATHERING_REQUIRED_MATERIALS;
        }

        return START_WORKING;
    }

    /**
     * Check if the stack we're using is needed by the assistant
     * @param stack the stack to check
     * @return true if the assistant needs this for a recipe
     */
    private boolean isItemStackForAssistant(ItemStack stack)
    {
        if(this.reservedItemCache.isEmpty())
        {
            this.reservedItemCache.addAll(building.getFirstModuleOccurance(BuildingCook.CraftingModule.class).reservedStacks().keySet());
        }

        return reservedItemCache.contains(new ItemStorage(stack));
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        final List<ItemStorage> blockedItems = new ArrayList<>(building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST)).getList());
        for (final Map.Entry<ItemStorage, Integer> content: building.getTileEntity().getAllContent().entrySet())
        {
            if (content.getValue() > content.getKey().getItemStack().getMaxStackSize() * 6 && ItemStackUtils.CAN_EAT.test(content.getKey().getItemStack()))
            {
                blockedItems.add(content.getKey());
            }
        }

        blockedItems.removeIf(item -> item.getItem().getFoodProperties() == null || item.getItem().getFoodProperties().getNutrition() < building.getBuildingLevel() - 1);
        if (!blockedItems.isEmpty())
        {
            if (IColonyManager.getInstance().getCompatibilityManager().getEdibles(building.getBuildingLevel() - 1).size() <= blockedItems.size())
            {
                if (worker.getCitizenData() != null)
                {
                    worker.getCitizenData()
                            .triggerInteraction(new StandardInteraction(new TranslatableComponent(FURNACE_USER_NO_FOOD), ChatPriority.BLOCKING));
                    return null;
                }
            }
            return new Food(STACKSIZE, blockedItems, building.getBuildingLevel() - 1);
        }
        return new Food(STACKSIZE, building.getBuildingLevel() - 1);
    }
}
