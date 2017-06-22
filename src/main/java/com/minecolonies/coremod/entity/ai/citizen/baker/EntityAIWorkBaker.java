package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.BuildingBaker;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES;

/**
 * Fisherman AI class.
 * <p>
 * A fisherman has some ponds where
 * he randomly selects one and fishes there.
 * <p>
 * To keep it immersive he chooses his place at random around the pond.
 */
public class EntityAIWorkBaker extends AbstractEntityAISkill<JobBaker>
{
    /**
     * How often should intelligence factor into the fisherman's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should dexterity factor into the fisherman's skill modifier.
     */
    private static final int DEXTERITY_MULTIPLIER = 1;

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
     * Experience per product the baker gains.
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
     * Constructor for the Fisherman.
     * Defines the tasks the fisherman executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkBaker(@NotNull final JobBaker job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
                new AITarget(PREPARING, this::prepareForBaking),
                new AITarget(BAKER_KNEADING, this::kneadTheDough),
                new AITarget(BAKER_BAKING, this::bake),
                new AITarget(BAKER_TAKE_OUT_OF_OVEN, this::takeFromOven),
                new AITarget(BAKER_FINISHING, this::finishing)
        );
        worker.setSkillModifier(
                INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
                        + DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity());
        worker.setCanPickUpLoot(true);
    }

    private AIState finishing()
    {
        if (currentBakingProduct == null)
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

        worker.setHeldItem(EnumHand.MAIN_HAND, currentBakingProduct.getEndProduct());
        worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());

        if (progress >= getRequiredProgressForKneading())
        {
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
            getOwnBuilding().removeFromTasks(ProductState.BAKED, currentBakingProduct);
            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), currentBakingProduct.getEndProduct());

            final RecipeStorage storage = BakerRecipes.getRecipes().get(currentBakingProduct.getRecipeId());
            for (final ItemStack stack : storage.getSecondaryOutput())
            {
                InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), stack.copy());
            }
            worker.addExperience(XP_PER_PRODUCT);
            incrementActionsDone();
            progress = 0;
            currentBakingProduct = null;
            return PREPARING;
        }

        progress++;
        setDelay(HIT_DELAY);
        return getState();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return UNTIL_DUMP;
    }

    private AIState takeFromOven()
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
        if (bakingProduct != null)
        {
            getOwnBuilding().addToTasks(bakingProduct.getState(), bakingProduct);
        }

        currentFurnace = null;

        return START_WORKING;
    }

    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState kneadTheDough()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        if (currentBakingProduct == null)
        {
            return createNewProduct();
        }

        final RecipeStorage storage = BakerRecipes.getRecipes().get(currentBakingProduct.getRecipeId());

        if (currentBakingProduct.getState() == ProductState.UNCRAFTED)
        {
            return craftNewProduct(storage);
        }

        if (currentBakingProduct.getState() != ProductState.RAW)
        {
            return PREPARING;
        }

        worker.setHeldItem(EnumHand.MAIN_HAND, storage.getInput().get(worker.getRandom().nextInt(storage.getInput().size())).copy());
        worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());

        if (progress >= getRequiredProgressForKneading())
        {
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
            progress = 0;
            currentBakingProduct.nextState();
            getOwnBuilding().removeFromTasks(ProductState.RAW, currentBakingProduct);
            getOwnBuilding().addToTasks(ProductState.PREPARED, currentBakingProduct);
            currentBakingProduct = null;
            return PREPARING;
        }

        progress++;
        setDelay(HIT_DELAY);
        return getState();
    }

    /**
     * Craft a new product from a given Storage.
     *
     * @param storage the given storage.
     * @return the next state to transit to.
     */
    private AIState craftNewProduct(final RecipeStorage storage)
    {
        final List<ItemStack> list = new ArrayList<>();

        ItemStack copy = null;
        for (final ItemStack stack : storage.getInput())
        {
            if (stack.getItem() != Items.WHEAT)
            {
                list.add(stack);
            }
            else
            {
                copy = stack.copy();
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

        final ItemStack[] arrayToRequestAndRetrieve = list.toArray(new ItemStack[list.size()]);
        if (checkOrRequestItemsAsynch(true, arrayToRequestAndRetrieve))
        {
            tryToTakeFromListOrRequest(shouldRequest(), arrayToRequestAndRetrieve);
            return getState();
        }

        InventoryUtils.removeStacksFromItemHandler(new InvWrapper(worker.getInventoryCitizen()), list);
        currentBakingProduct.nextState();
        getOwnBuilding().removeFromTasks(ProductState.UNCRAFTED, currentBakingProduct);
        getOwnBuilding().addToTasks(ProductState.RAW, currentBakingProduct);

        return getState();
    }

    /**
     * Determines if the Baker should request to the chat.
     *
     * @return true if he has nothing to do at the moment.
     */
    private boolean shouldRequest()
    {
        final BuildingBaker bakerBuilding = getOwnBuilding();
        for (final BakingProduct bakingProduct : bakerBuilding.getFurnacesWithProduct().values())
        {
            if (bakingProduct != null)
            {
                return true;
            }
        }

        final List<BakingProduct> preparedList = bakerBuilding.getTasks().get(ProductState.PREPARED);
        final List<BakingProduct> bakedList = bakerBuilding.getTasks().get(ProductState.BAKED);

        return (preparedList == null || preparedList.isEmpty()) && (bakedList == null || bakedList.isEmpty());
    }

    /**
     * Create a new product depending on what the baker has available on resources.
     *
     * @return the next state to transit to.
     */
    private AIState createNewProduct()
    {
        progress = 0;
        final List<IItemHandler> handlers = new ArrayList<>();
        handlers.add(new InvWrapper(worker.getInventoryCitizen()));
        handlers.add(new InvWrapper(getOwnBuilding().getTileEntity()));

        for (final BlockPos pos : getOwnBuilding().getAdditionalCountainers())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityChest)
            {
                handlers.add(new InvWrapper((TileEntityChest) entity));
            }
        }

        RecipeStorage storage = null;
        int recipeId = 0;
        for (final RecipeStorage tempStorage : BakerRecipes.getRecipes())
        {
            if (tempStorage.canFullFillRecipe(handlers.toArray(new IItemHandler[handlers.size()])))
            {
                storage = tempStorage;
                break;
            }
            recipeId++;
        }

        if (storage == null)
        {
            final List<RecipeStorage> recipes = BakerRecipes.getRecipes();
            final List<ItemStack> lastRecipe = recipes.get(recipes.size() - 1).getInput();
            final ItemStack[] arrayToRequestAndRetrieve = lastRecipe.toArray(new ItemStack[lastRecipe.size()]);
            if(checkOrRequestItemsAsynch(true, arrayToRequestAndRetrieve))
            {
                tryToTakeFromListOrRequest(shouldRequest(), arrayToRequestAndRetrieve);
            }
            setDelay(UNABLE_TO_CRAFT_DELAY);
            return PREPARING;
        }

        final BakingProduct bakingProduct = new BakingProduct(storage.getPrimaryOutput().copy(), recipeId);
        getOwnBuilding().addToTasks(bakingProduct.getState(), bakingProduct);
        currentBakingProduct = bakingProduct;
        return getState();
    }

    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState bake()
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

        final IBlockState furnace = world.getBlockState(currentFurnace);
        final List<BakingProduct> bakingProducts = building.getTasks().get(ProductState.PREPARED);
        if (!(furnace.getBlock() instanceof BlockFurnace))
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
            world.setBlockState(currentFurnace, Blocks.LIT_FURNACE.getDefaultState().withProperty(BlockFurnace.FACING, furnace.getValue(BlockFurnace.FACING)));
        }
        return PREPARING;
    }

    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState prepareForBaking()
    {
        if (getOwnBuilding().getFurnaces().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
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

    /**
     * Handle an empty furnace and start baking if prepared products are rhere.
     *
     * @param map the map of furnaces.
     * @return the next state to transit to.
     */
    private static AIState handleEmptyFurnace(final Map<ProductState, List<BakingProduct>> map)
    {
        if (map.containsKey(ProductState.PREPARED))
        {
            return BAKER_BAKING;
        }
        return BAKER_KNEADING;
    }

    /**
     * Redirects the fisherman to his building.
     *
     * @return the next state.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Returns the fisherman's work building.
     *
     * @return building instance
     */
    @Override
    protected BuildingBaker getOwnBuilding()
    {
        return (BuildingBaker) worker.getWorkBuilding();
    }

    /**
     * Returns the fisherman's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public EntityCitizen getCitizen()
    {
        return worker;
    }

    private int getRequiredProgressForKneading()
    {
        return PROGRESS_MULTIPLIER / Math.min(worker.getLevel() + 1, MAX_LEVEL) * KNEADING_TIME;
    }
}
