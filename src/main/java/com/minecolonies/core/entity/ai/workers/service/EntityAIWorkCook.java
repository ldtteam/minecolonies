package com.minecolonies.core.entity.ai.workers.service;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
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
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.RestaurantMenuModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobCook;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIUsesFurnace;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.AVERAGE_SATURATION;
import static com.minecolonies.api.util.constant.CitizenConstants.FULL_SATURATION;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.StatisticsConstants.FOOD_SERVED;
import static com.minecolonies.api.util.constant.TranslationConstants.FURNACE_USER_NO_FOOD;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_CITIZEN_COOK_SERVE_PLAYER;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.RESTAURANT_MENU;

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
    private static final int LEVEL_TO_FEED_PLAYER = 10;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final Queue<AbstractEntityCitizen> citizenToServe = new ArrayDeque<>();

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final Queue<Player> playerToServe = new ArrayDeque<>();

    /**
     * Cooking icon
     */
    private final static VisibleCitizenStatus COOK =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/cook.png"), "com.minecolonies.gui.visiblestatus.cook");

    /**
     * Constructor for the Cook. Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkCook(@NotNull final JobCook job)
    {
        super(job);
        super.registerTargets(
          new AITarget(COOK_SERVE_FOOD_TO_CITIZEN, this::serveFoodToCitizen, SERVE_DELAY),
          new AITarget(COOK_SERVE_FOOD_TO_PLAYER, this::serveFoodToPlayer, SERVE_DELAY)
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
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
          new InvWrapper(furnace), RESULT_SLOT,
          worker.getInventoryCitizen());
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
    }

    @Override
    public IAIState startWorking()
    {
        return super.startWorking();
    }

    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        //Only return true if the item isn't queued for a recipe.
        return ItemStackUtils.ISCOOKABLE.test(stack) && building.getModule(RESTAURANT_MENU).getMenu().contains(new ItemStorage(MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack)));
    }

    @Override
    protected boolean reachedMaxToKeep()
    {
        if (super.reachedMaxToKeep())
        {
            return true;
        }
        final int buildingLimit = Math.max(1, building.getBuildingLevel() * building.getBuildingLevel()) * SLOT_PER_LINE;
        return InventoryUtils.getCountFromBuildingWithLimit(building,
            FoodUtils.EDIBLE.and(stack -> FoodUtils.canEatLevel(stack, building.getBuildingLevel() - 1)),
          stack -> stack.getMaxStackSize() * 6) > buildingLimit;
    }

    @Override
    public void requestSmeltable()
    {
        final RestaurantMenuModule menuModule = building.getModule(RESTAURANT_MENU);
        if (menuModule.getMenu().isEmpty() && worker.getCitizenData() != null)
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(FURNACE_USER_NO_FOOD), ChatPriority.BLOCKING));
        }
    }

    /**
     * Serve food to citizen.
     * @return next IAIState
     */
    private IAIState serveFoodToCitizen()
    {
        if (citizenToServe.isEmpty())
        {
            return START_WORKING;
        }

        worker.getCitizenData().setVisibleStatus(COOK);

        if (!building.isInBuilding(citizenToServe.peek().blockPosition()))
        {
            worker.getNavigation().stop();
            citizenToServe.poll();
            return getState();
        }

        if (walkToBlock(citizenToServe.peek().blockPosition()))
        {
            return getState();
        }

        final AbstractEntityCitizen citizen = citizenToServe.poll();
        final IItemHandler handler = citizen.getInventoryCitizen();
        final RestaurantMenuModule module = worker.getCitizenData().getWorkBuilding().getModule(RESTAURANT_MENU);
        final Predicate<ItemStack> canEatPredicate = stack -> module.getMenu().contains(new ItemStorage(stack));
        final ICitizenData citizenData = citizen.getCitizenData();

        if (InventoryUtils.isItemHandlerFull(handler))
        {
            for (int feedingAttempts = 0; feedingAttempts < 10; feedingAttempts++)
            {
                final int foodSlot = FoodUtils.getBestFoodForCitizen(worker.getInventoryCitizen(), citizenData, module.getMenu());
                if (foodSlot != -1)
                {
                    final ItemStack stack = worker.getInventoryCitizen().extractItem(foodSlot, 1, false);
                    citizenData.increaseSaturation(FoodUtils.getFoodValue(stack, worker));
                    worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(FOOD_SERVED, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());
                }
                else
                {
                    break;
                }

                if (citizenData.getSaturation() >= CitizenConstants.FULL_SATURATION)
                {
                    break;
                }
            }

            return getState();
        }
        else if (InventoryUtils.hasItemInItemHandler(handler, canEatPredicate))
        {
            return getState();
        }

        final int foodSlot = FoodUtils.getBestFoodForCitizen(worker.getInventoryCitizen(), citizenData, module.getMenu());
        if (foodSlot == -1)
        {
            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), canEatPredicate) <= 0)
            {
                citizenToServe.clear();
                return START_WORKING;
            }
            return getState();
        }

        final int countInSlot = worker.getInventoryCitizen().getStackInSlot(foodSlot).getCount();
        int homeBuildingLevel = citizen.getCitizenData().getHomeBuilding() == null ? 0 : citizen.getCitizenData().getHomeBuilding().getBuildingLevel();
        int qty = (int) (Math.max(1.0, (FULL_SATURATION - citizen.getCitizenData().getSaturation()) / FoodUtils.getFoodValue(worker.getInventoryCitizen().getStackInSlot(foodSlot), citizen)) * homeBuildingLevel/2.0);

        final int transferCount = Math.min(countInSlot, building.getBuildingLevel());
        if (InventoryUtils.transferXOfItemStackIntoNextFreeSlotInItemHandler(worker.getInventoryCitizen(), foodSlot, qty, citizenData.getInventory()))
        {
            worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().incrementBy(FOOD_SERVED, qty, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            this.incrementActionsDoneAndDecSaturation();
        }

        return getState();
    }

    /**
     * Serve food to player.
     * @return next IAIState
     */
    private IAIState serveFoodToPlayer()
    {
        if (playerToServe.isEmpty())
        {
            return START_WORKING;
        }

        worker.getCitizenData().setVisibleStatus(COOK);
        if (!building.isInBuilding(playerToServe.peek().blockPosition()))
        {
            worker.getNavigation().stop();
            playerToServe.poll();
            return START_WORKING;
        }

        if (walkToBlock(playerToServe.peek().blockPosition()))
        {
            return getState();
        }

        final Player player = playerToServe.poll();
        final IItemHandler handler = new InvWrapper(player.getInventory());
        final RestaurantMenuModule module = worker.getCitizenData().getWorkBuilding().getModule(RESTAURANT_MENU);
        final Predicate<ItemStack> canEatPredicate = stack -> module.getMenu().contains(new ItemStorage(stack));
        if (InventoryUtils.isItemHandlerFull(handler))
        {
            return getState();
        }
        else if (InventoryUtils.hasItemInItemHandler(handler, canEatPredicate))
        {
            return getState();
        }

        final int count = InventoryUtils.transferFoodUpToSaturation(worker, handler, building.getBuildingLevel() * SATURATION_TO_SERVE, canEatPredicate);
        if (count <= 0)
        {
            playerToServe.clear();
            return START_WORKING;
        }
        worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().incrementBy(FOOD_SERVED, count, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());
        MessageUtils.format(MESSAGE_INFO_CITIZEN_COOK_SERVE_PLAYER, worker.getName().getString()).sendTo(player);

        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
        return START_WORKING;
    }

    /**
     * Check if the entity to serve can eat the given stack
     *
     * @param stack   the stack to check
     * @param citizen the citizen to check for.
     * @return true if the stack can be eaten
     */
    private boolean canEat(final ItemStack stack, final AbstractEntityCitizen citizen)
    {
        final RestaurantMenuModule module = worker.getCitizenData().getWorkBuilding().getModule(RESTAURANT_MENU);
        if (!module.getMenu().contains(new ItemStorage(stack)))
        {
            return false;
        }
        if (citizen != null)
        {
            final IBuilding building = citizen.getCitizenData().getHomeBuilding();
            if (building != null)
            {
                return building.canEat(stack);
            }
        }
        return true;
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
        citizenToServe.clear();
        final List<? extends Player> playerList = WorldUtil.getEntitiesWithinBuilding(world, Player.class,
          building, player -> player != null
                                && player.getFoodData().getFoodLevel() < LEVEL_TO_FEED_PLAYER
                                && building.getColony().getPermissions().hasPermission(player, Action.MANAGE_HUTS)
        );

        playerToServe.addAll(playerList);
        final RestaurantMenuModule module = worker.getCitizenData().getWorkBuilding().getModule(RESTAURANT_MENU);

        for (final EntityCitizen citizen : WorldUtil.getEntitiesWithinBuilding(world, EntityCitizen.class, building, null))
        {
            if (citizen.getCitizenJobHandler().getColonyJob() instanceof JobCook
                  || !shouldBeFed(citizen)
                  || InventoryUtils.hasItemInItemHandler(citizen.getItemHandlerCitizen(), stack -> canEat(stack, citizen)))
            {
                continue;
            }

            if (FoodUtils.getBestFoodForCitizen(worker.getInventoryCitizen(), citizen.getCitizenData(), module.getMenu()) >= 0)
            {
                citizenToServe.add(citizen);
            }
            else
            {
                final ItemStorage storage = FoodUtils.checkForFoodInBuilding(citizen.getCitizenData(), module.getMenu(), building);
                if (storage != null)
                {
                    needsCurrently = new Tuple<>(stack -> new ItemStorage(stack).equals(storage), STACKSIZE);
                    return GATHERING_REQUIRED_MATERIALS;
                }
            }
        }

        if (!citizenToServe.isEmpty())
        {
            return COOK_SERVE_FOOD_TO_CITIZEN;
        }

        if (!playerToServe.isEmpty())
        {
            final Predicate<ItemStack> foodPredicate = stack -> module.getMenu().contains(new ItemStorage(stack));
            if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), foodPredicate))
            {
                if (InventoryUtils.hasItemInProvider(building, foodPredicate))
                {
                    needsCurrently = new Tuple<>(foodPredicate, STACKSIZE);
                    return GATHERING_REQUIRED_MATERIALS;
                }
            }
            return COOK_SERVE_FOOD_TO_PLAYER;
        }

        return START_WORKING;
    }

    /**
     * Check if the citizen can be fed.
     *
     * @return true if so.
     */
    private boolean shouldBeFed(AbstractEntityCitizen citizen)
    {
        return citizen.getCitizenData() != null
                 && !citizen.getCitizenData().isWorking()
                 && citizen.getCitizenData().getSaturation() <= AVERAGE_SATURATION
                 && !citizen.getCitizenData().justAte();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        return new Food(STACKSIZE, building.getBuildingLevel());
    }
}
