package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.CAN_EAT;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
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
    private static final int LEVEL_TO_FEED_PLAYER = 10;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<AbstractEntityCitizen> citizenToServe = new ArrayList<>();

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<PlayerEntity> playerToServe = new ArrayList<>();

    /**
     * Cooking icon
     */
    private final static VisibleCitizenStatus COOK =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/cook.png"), "com.minecolonies.gui.visiblestatus.cook");

    /**
     * The list of items needed for the assistant
     */
    private Set<ItemStack> assistantTests = new HashSet<>();

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
    protected void extractFromFurnace(final FurnaceTileEntity furnace)
    {
        if(!getOwnBuilding().getIsCooking())
        {
            InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
                new InvWrapper(furnace), RESULT_SLOT,
                worker.getInventoryCitizen());
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            this.incrementActionsDoneAndDecSaturation();
        }
    }

    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        //Only return true if the item isn't queued for a recipe. 
        if(!getOwnBuilding().getIsCooking() )
        {
            return ItemStackUtils.ISCOOKABLE.test(stack) && !isItemStackForAssistant(stack);
        }
        return false;
    }

    @Override
    protected boolean reachedMaxToKeep()
    {
        return InventoryUtils.getCountFromBuilding(getOwnBuilding(), ItemStackUtils.ISFOOD)
                 > Math.max(1, getOwnBuilding().getBuildingLevel() * getOwnBuilding().getBuildingLevel()) * SLOT_PER_LINE;
    }

    @Override
    public void requestSmeltable()
    {
        final IRequestable smeltable = getSmeltAbleClass();
        if (smeltable != null && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(smeltable.getClass()))
        && !getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData(),
                req -> req.getShortDisplayString().getSiblings().contains(new TranslationTextComponent(COM_MINECOLONIES_REQUESTS_FOOD))))
        {
            worker.getCitizenData().createRequestAsync(smeltable);
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
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SERVING));

        if (citizenToServe.isEmpty() && playerToServe.isEmpty())
        {
            return START_WORKING;
        }

        worker.getCitizenData().setVisibleStatus(COOK);

        final Entity living = citizenToServe.isEmpty() ? playerToServe.get(0) : citizenToServe.get(0);

        if (!getOwnBuilding().isInBuilding(living.getPositionVec()))
        {
            worker.getNavigator().clearPath();
            removeFromQueue();
            return START_WORKING;
        }

        if (walkToBlock(new BlockPos(living.getPositionVec())))
        {
            return getState();
        }

        final IItemHandler handler = citizenToServe.isEmpty() ? new InvWrapper(playerToServe.get(0).inventory) : citizenToServe.get(0).getInventoryCitizen();

        if (InventoryUtils.isItemHandlerFull(handler))
        {
            if (!citizenToServe.isEmpty())
            {
                final int foodSlot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), stack -> ItemStackUtils.CAN_EAT.test(stack) && canEat(stack));
                if (foodSlot != -1)
                {
                    final ItemStack stack = worker.getInventoryCitizen().extractItem(foodSlot, 1, false);
                    if (stack.getItem().isFood())
                    {
                        citizenToServe.get(0).getCitizenData().increaseSaturation(stack.getItem().getFood().getHealing() / 2.0);
                    }
                }
            }

            removeFromQueue();
            return getState();
        }
        else if (InventoryUtils.hasItemInItemHandler(handler, stack -> CAN_EAT.test(stack) && canEat(stack)))
        {
            removeFromQueue();
            return getState();
        }

        InventoryUtils.transferFoodUpToSaturation(worker, handler, getOwnBuilding().getBuildingLevel() * SATURATION_TO_SERVE, stack -> CAN_EAT.test(stack) && canEat(stack));

        if (!citizenToServe.isEmpty() && citizenToServe.get(0).getCitizenData() != null)
        {
            citizenToServe.get(0).getCitizenData().setJustAte(true);
        }

        if (citizenToServe.isEmpty() && living instanceof PlayerEntity)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) living, "com.minecolonies.coremod.cook.serve.player", worker.getName().getString());
        }
        removeFromQueue();

        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
        return START_WORKING;
    }

    /**
     * Check if the entity to serve can eat the given stack
     * @param stack the stack to check
     * @return true if the stack can be eaten
     */
    private boolean canEat(final ItemStack stack)
    {
        if (!citizenToServe.isEmpty())
        {
            final IBuildingWorker building = citizenToServe.get(0).getCitizenData().getWorkBuilding();
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
        this.assistantTests.clear(); //Clear the cache of current pending work

        citizenToServe.clear();
        final List<AbstractEntityCitizen> citizenList = WorldUtil.getEntitiesWithinBuilding(world, AbstractEntityCitizen.class, getOwnBuilding(), null)
                                                          .stream()
                                                          .filter(cit -> !(cit.getCitizenJobHandler().getColonyJob() instanceof JobCook) && cit.shouldBeFed())
                                                          .sorted(Comparator.comparingInt(a -> (a.getCitizenJobHandler().getColonyJob() == null ? 1 : 0)))
                                                          .collect(Collectors.toList());

        final List<PlayerEntity> playerList = WorldUtil.getEntitiesWithinBuilding(world, PlayerEntity.class,
          getOwnBuilding(), player -> player != null
                      && player.getFoodStats().getFoodLevel() < LEVEL_TO_FEED_PLAYER
                      && getOwnBuilding().getColony().getPermissions().hasPermission(player, Action.MANAGE_HUTS)
        );

        if (!citizenList.isEmpty() || !playerList.isEmpty())
        {
            citizenToServe.addAll(citizenList);
            playerToServe.addAll(playerList);

            if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.CAN_EAT.test(stack)))
            {
                return COOK_SERVE_FOOD_TO_CITIZEN;
            }
            else if (!InventoryUtils.hasItemInProvider(getOwnBuilding(), stack -> ItemStackUtils.CAN_EAT.test(stack) && !isItemStackForAssistant(stack)))
            {
                return START_WORKING;
            }

            needsCurrently = new Tuple<>(stack -> ItemStackUtils.CAN_EAT.test(stack) && !isItemStackForAssistant(stack), STACKSIZE);
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
        if(this.assistantTests.isEmpty())
        {
            this.assistantTests.addAll(getOwnBuilding().getAssistantItems());
        }

        return ItemStackUtils.compareItemStackListIgnoreStackSize(new ArrayList<>(assistantTests), stack, true, true);
    }


    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        final List<ItemStorage> allowedItems = getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST))
                                                 .map(ItemListModule::getList).orElse(ImmutableList.of());
        if (!allowedItems.isEmpty())
        {
            if (IColonyManager.getInstance().getCompatibilityManager().getEdibles().size() <= allowedItems.size())
            {
                if (worker.getCitizenData() != null)
                {
                    worker.getCitizenData()
                            .triggerInteraction(new StandardInteraction(new TranslationTextComponent(FURNACE_USER_NO_FOOD), ChatPriority.BLOCKING));
                    return null;
                }
            }
            return new Food(STACKSIZE, allowedItems);
        }
        return new Food(STACKSIZE);
    }

}
