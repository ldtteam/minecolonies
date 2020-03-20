package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBaker;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.BAKER_HAS_NO_FURNACES_MESSAGE;
import static com.minecolonies.api.util.constant.TranslationConstants.BAKER_HAS_NO_RECIPES;

/**
 * Baker AI class.
 */
public class EntityAIWorkBaker extends AbstractEntityAISkill<JobBaker>
{
    /**
     * Times the dough needs to be kneaded.
     */
    private static final int KNEADING_TIME = 5;

    /**
     * Time the worker delays until the next hit.
     */
    private static final int HIT_DELAY = 20;

    /**
     * Unable to craft delay.
     */
    private static final int UNABLE_TO_CRAFT_DELAY = 100;

    /**
     * Make this amount of products until dumping
     */
    private static final int UNTIL_DUMP = 3;

    /**
     * Increase this value to make the product creation progress way slower.
     */
    private static final int PROGRESS_MULTIPLIER = 50;

    /**
     * Max level which should have an effect on the speed of the worker.
     */
    private static final int MAX_LEVEL = 50;

    /**
     * Experience per product the bakery gains.
     */
    private static final double XP_PER_PRODUCT = 10.0;

    /**
     * Current furnace to walk to.
     */
    private BlockPos currentFurnace = null;

    /**
     * Current product to work at.
     */
    private BakingProduct currentBakingProduct = null;

    /**
     * Progress in hitting the product.
     */
    private int progress = 0;

    /**
     * Pointer to the current recipe, when bakery starts
     * next recipe it starts checking right after this recipe.
     * So the bakery can rotate between recipes.
     */
    private int currentRecipe = -1;

    /**
     * Constructor for the Baker.
     * Defines the tasks the bakery executes.
     *
     * @param job a bakery job to use.
     */
    public EntityAIWorkBaker(@NotNull final JobBaker job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(PREPARING, this::prepareForBaking, HIT_DELAY),
          new AITarget(BAKER_KNEADING, this::kneadTheDough, HIT_DELAY),
          new AITarget(BAKER_BAKING, this::bake, HIT_DELAY),
          new AITarget(BAKER_TAKE_OUT_OF_OVEN, this::takeFromOven, HIT_DELAY),
          new AITarget(BAKER_FINISHING, this::finishing, HIT_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingBaker.class;
    }

    /**
     * Redirects the bakery to his building.
     *
     * @return the next state.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the bakery for baking and requests ingredients.
     *
     * @return the next IAIState
     */
    private IAIState prepareForBaking()
    {
        if (getOwnBuilding().getFurnaces().isEmpty())
        {
            if ( worker.getCitizenData() != null )
            {
                worker.getCitizenData().triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            return getState();
        }

        if (getOwnBuilding().getCopyOfAllowedItems().isEmpty())
        {
            if ( worker.getCitizenData() != null )
            {
                worker.getCitizenData().triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(BAKER_HAS_NO_RECIPES), ChatPriority.BLOCKING));
            }
            return getState();
        }

        boolean emptyFurnace = false;
        for (final Map.Entry<BlockPos, BakingProduct> entry : getOwnBuilding().getFurnacesWithProduct().entrySet())
        {
            if (entry.getValue() == null)
            {
                emptyFurnace = true;
                currentFurnace = entry.getKey();
            }
            else if (entry.getValue().getState() == ProductState.BAKED)
            {
                currentFurnace = entry.getKey();
                return BAKER_TAKE_OUT_OF_OVEN;
            }
        }

        @NotNull final Map<ProductState, List<BakingProduct>> map = getOwnBuilding().getTasks();
        if (map.isEmpty())
        {
            return BAKER_KNEADING;
        }

        if (map.containsKey(ProductState.BAKED))
        {
            return BAKER_FINISHING;
        }

        if (emptyFurnace)
        {
            return handleEmptyFurnace(map);
        }

        return BAKER_KNEADING;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return UNTIL_DUMP;
    }

    /**
     * @return the time required in the Kneading process
     */
    private int getRequiredProgressForKneading()
    {
        return PROGRESS_MULTIPLIER / Math.min(worker.getCitizenData().getJobModifier() + 1, MAX_LEVEL) * KNEADING_TIME;
    }

    /**
     * Take item from oven
     * @return new state based on what is happening with
     * removal of item from furnace
     */
    private IAIState takeFromOven()
    {
        if (currentFurnace == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(currentFurnace))
        {
            return getState();
        }

        final BakingProduct bakingProduct = getOwnBuilding().getFurnacesWithProduct().get(currentFurnace);
        getOwnBuilding().removeProductFromFurnace(currentFurnace);
        worker.decreaseSaturationForAction();
        if (bakingProduct != null)
        {
            getOwnBuilding().addToTasks(bakingProduct.getState(), bakingProduct);
        }

        currentFurnace = null;

        return START_WORKING;
    }

    /**
     * Prepares the bakery for baking and requests ingredients.
     *
     * @return the next IAIState
     */
    private IAIState kneadTheDough()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        if (currentBakingProduct == null)
        {
            return createNewProduct();
        }

        if (currentBakingProduct != null)
        {
        	final IRecipeStorage storage = BakerRecipes.getRecipes().get(currentBakingProduct.getRecipeId());

        	if (currentBakingProduct.getState() == ProductState.UNCRAFTED)
	        {
	            return craftNewProduct(storage);
	        }

	        if (currentBakingProduct.getState() != ProductState.RAW)
	        {
	            return PREPARING;
	        }

	        worker.setHeldItem(Hand.MAIN_HAND, storage.getInput().get(worker.getRandom().nextInt(storage.getInput().size())).copy());
	        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getPosition());

	        if (progress >= getRequiredProgressForKneading())
	        {
	            worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
                worker.decreaseSaturationForAction();
	            progress = 0;
	            currentBakingProduct.nextState();
	            getOwnBuilding().removeFromTasks(ProductState.RAW, currentBakingProduct);
	            getOwnBuilding().addToTasks(ProductState.PREPARED, currentBakingProduct);
	            currentBakingProduct = null;
	            return PREPARING;
	        }

            progress += HIT_DELAY;
        }
        return getState();
    }

    /**
     * Prepares the bakery for baking and requests ingredients.
     *
     * @return the next IAIState
     */
    private IAIState bake()
    {
        final BuildingBaker building = getOwnBuilding();
        if (currentFurnace == null || building.getFurnacesWithProduct().get(currentFurnace) != null)
        {
            return PREPARING;
        }

        if (walkToBlock(currentFurnace))
        {
            return BAKER_BAKING;
        }

        final BlockState furnace = world.getBlockState(currentFurnace);
        final List<BakingProduct> bakingProducts = building.getTasks().get(ProductState.PREPARED);
        if (!(furnace.getBlock() instanceof FurnaceBlock))
        {
            if (bakingProducts.isEmpty())
            {
                building.removeFromTasks(ProductState.PREPARED, null);
            }
            else
            {
                building.removeFromFurnaces(currentFurnace);
            }
            return START_WORKING;
        }
        else if (bakingProducts.isEmpty())
        {
            return START_WORKING;
        }

        final BakingProduct bakingProduct = bakingProducts.get(0);
        building.removeFromTasks(ProductState.PREPARED, bakingProduct);

        if (bakingProduct != null && bakingProduct.getState() == ProductState.PREPARED)
        {
            building.putInFurnace(currentFurnace, bakingProduct);
            bakingProduct.nextState();
            world.setBlockState(currentFurnace, Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, furnace.get(FurnaceBlock.FACING)).with(FurnaceBlock.LIT, true));
        }
        return PREPARING;
    }

    /**
     * Craft a new product from a given Storage.
     *
     * @param storage the given storage.
     * @return the next state to transit to.
     */
    private IAIState craftNewProduct(final IRecipeStorage storage)
    {
        final List<ItemStack> requestList = new ArrayList<>();
        for (final ItemStorage stack : storage.getCleanedInput())
        {
            if (stack.getItem() != Items.WHEAT)
            {
                requestList.add(stack.getItemStack().copy());
            }
            else
            {
                final ItemStack copy = stack.getItemStack().copy();
                copy.setCount(copy.getMaxStackSize());
                requestList.add(copy);
            }
        }
        checkIfRequestForItemExistOrCreateAsynch(requestList.toArray(new ItemStack[0]));


        final List<IItemHandler> handlers = getOwnBuilding().getHandlers();
        if (storage.canFullFillRecipe(1, handlers.toArray(new IItemHandler[0])))
        {
            final List<ItemStack> list = new ArrayList<>();

	        ItemStack copy = null;
	        for (final ItemStorage stack : storage.getCleanedInput())
	        {
	            if (stack.getItem() != Items.WHEAT)
	            {
	                list.add(stack.getItemStack().copy());
	            }
	            else
	            {
	                copy = stack.getItemStack().copy();
	            }
	        }

	        if (copy != null)
	        {
	            //Wheat will be reduced by chance only (Between 3 and 6- getBuildingLevel, meaning 3-5, 3-4, 3-3, 3-2, 3-1)
	            final int form = (getOwnBuilding().getMaxBuildingLevel() + 1) - (getOwnBuilding().getBuildingLevel() + ItemStackUtils.getSize(copy));
	            int req = 0;
	            if (form != 0)
	            {
	                req = form < 0 ? -worker.getRandom().nextInt(Math.abs(form)) : worker.getRandom().nextInt(form);
	            }
	            ItemStackUtils.changeSize(copy, req);
	            list.add(copy);
	        }

            InventoryUtils.removeStacksFromItemHandler(worker.getInventoryCitizen(), list);
            currentBakingProduct.nextState();
            getOwnBuilding().removeFromTasks(ProductState.UNCRAFTED, currentBakingProduct);
            getOwnBuilding().addToTasks(ProductState.RAW, currentBakingProduct);
        }
        else
        {
        	setDelay(UNABLE_TO_CRAFT_DELAY);
        	return NEEDS_ITEM;
        }

        return getState();
    }

    /**
     * @return new state of the bakery
     */
    private IAIState finishing()
    {
        if (currentBakingProduct == null || currentBakingProduct.getState() != ProductState.BAKED)
        {
            progress = 0;
            final List<BakingProduct> bakingProducts = getOwnBuilding().getTasks().get(ProductState.BAKED);
            if (bakingProducts == null || bakingProducts.isEmpty())
            {
                getOwnBuilding().removeFromTasks(ProductState.BAKED, null);
                return PREPARING;
            }
            currentBakingProduct = bakingProducts.get(0);
        }

        if (currentBakingProduct.getState() != ProductState.BAKED)
        {
            return PREPARING;
        }

        worker.setHeldItem(Hand.MAIN_HAND, currentBakingProduct.getEndProduct());

        final ItemStack newItem = currentBakingProduct.getEndProduct();
        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getPosition());

        if (progress >= getRequiredProgressForKneading())
        {
            worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
            getOwnBuilding().removeFromTasks(ProductState.BAKED, currentBakingProduct);
            if (newItem != null)
            {
                InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), newItem);
            }
            worker.getCitizenExperienceHandler().addExperience(XP_PER_PRODUCT);
            incrementActionsDoneAndDecSaturation();
            progress = 0;
            currentBakingProduct = null;
            return PREPARING;
        }

        progress++;
        return getState();
    }

    /**
     * Returns the bakery's work building.
     *
     * @return building instance
     */
    @Override
    public BuildingBaker getOwnBuilding()
    {
        return (BuildingBaker) worker.getCitizenColonyHandler().getWorkBuilding();
    }

    /**
     * Handle an empty furnace and start baking if prepared products are rhere.
     *
     * @param map the map of furnaces.
     * @return the next state to transit to.
     */
    private static IAIState handleEmptyFurnace(final Map<ProductState, List<BakingProduct>> map)
    {
        if (map.containsKey(ProductState.PREPARED) && !map.get(ProductState.PREPARED).isEmpty())
        {
            return BAKER_BAKING;
        }
        return BAKER_KNEADING;
    }

    /**
     * Create a new product depending on what the bakery has available on resources.
     *
     * @return the next state to transit to.
     */
    private IAIState createNewProduct()
    {
        progress = 0;
        final BuildingBaker building = getOwnBuilding();
        currentRecipe++;
        if (currentRecipe >= building.getCopyOfAllowedItems().size())
        {
            currentRecipe = 0;
        }

        final ItemStorage itemStorage = building.getCopyOfAllowedItems().get("recipes").get(currentRecipe);
        final IRecipeStorage recipeStorage =
          BakerRecipes.getRecipes().stream().filter(recipe -> recipe.getPrimaryOutput().isItemEqual(itemStorage.getItemStack())).findFirst().orElse(null);
        if (recipeStorage == null)
        {
            setDelay(UNABLE_TO_CRAFT_DELAY);
            return IDLE;
        }

        final BakingProduct bakingProduct = new BakingProduct(recipeStorage.getPrimaryOutput().copy(), BakerRecipes.getRecipes().indexOf(recipeStorage));
        building.addToTasks(bakingProduct.getState(), bakingProduct);
        currentBakingProduct = bakingProduct;
        return getState();
    }

    /**
     * Returns the bakery's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }
}
