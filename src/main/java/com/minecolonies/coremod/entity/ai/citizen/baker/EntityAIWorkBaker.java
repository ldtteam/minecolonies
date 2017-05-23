package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.minecolonies.coremod.colony.buildings.BuildingBaker;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import com.minecolonies.coremod.util.InventoryUtils;
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
import static com.minecolonies.coremod.util.constants.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES;

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
    private static final int KNEADING_TIME = 10;

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
    private static final int UNTIL_DUMP = 10;

    /**
     * Current furnace to walk to.
     */
    private BlockPos currentFurnace = null;

    /**
     * Current product to work at.
     */
    private Product currentProduct = null;

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
                new AITarget(BAKER_BAKING, this::bakeBread),
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
        if (currentProduct == null)
        {
            progress = 0;
            final List<Product> products = getOwnBuilding().getTasks().get(Product.ProductState.BAKED);
            if (products == null || products.isEmpty())
            {
                return PREPARING;
            }
            currentProduct = products.get(0);
        }

        if (currentProduct.getState() != Product.ProductState.BAKED)
        {
            return PREPARING;
        }

        worker.setHeldItem(EnumHand.MAIN_HAND, currentProduct.getEndProduct());
        worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());

        if (progress >= getRequiredProgressForKneading())
        {
            worker.setHeldItem(EnumHand.MAIN_HAND, InventoryUtils.EMPTY);
            currentProduct.nextState();
            getOwnBuilding().removeFromTasks(Product.ProductState.BAKED, currentProduct);
            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), currentProduct.getEndProduct());
            incrementActionsDone();
            progress = 0;
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

        final Product product = getOwnBuilding().getFurnacesWithProduct().get(currentFurnace);
        getOwnBuilding().removeProductFromFurnace(currentFurnace);
        if (product != null)
        {
            getOwnBuilding().addToTasks(product.getState(), product);
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

        if (currentProduct == null)
        {
            return createNewProduct();
        }

        final RecipeStorage storage = BakerRecipes.getRecipes().get(currentProduct.getRecipeId());

        if (currentProduct.getState() == Product.ProductState.UNCRAFTED)
        {
            return craftNewProduct(storage);
        }

        if (currentProduct.getState() != Product.ProductState.RAW)
        {
            return PREPARING;
        }

        worker.setHeldItem(EnumHand.MAIN_HAND, storage.getInput().get(worker.getRandom().nextInt(storage.getInput().size())));
        worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());

        if (progress >= getRequiredProgressForKneading())
        {
            worker.setHeldItem(EnumHand.MAIN_HAND, InventoryUtils.EMPTY);
            progress = 0;
            currentProduct.nextState();
            getOwnBuilding().removeFromTasks(Product.ProductState.RAW, currentProduct);
            getOwnBuilding().addToTasks(Product.ProductState.PREPARED, currentProduct);
            currentProduct = null;
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
        final List<RecipeStorage> recipes = BakerRecipes.getRecipes();
        final List<ItemStack> lastRecipe = recipes.get(recipes.size() - 1).getInput();
        if (checkOrRequestItems(lastRecipe.toArray(new ItemStack[lastRecipe.size()])))
        {
            return getState();
        }
        currentProduct.nextState();

        final List<ItemStack> list = new ArrayList<>(storage.getInput());

        //Wheat will be reduced by chance only (Between 3 and 6- getBuildingLevel, meaning 3-5, 3-4, 3-3, 3-2, 3-1)
        for (final ItemStack stack : storage.getInput())
        {
            if (stack.getItem() == Items.WHEAT)
            {
                list.remove(stack);
                final ItemStack copy = stack.copy();
                final int form = (getOwnBuilding().getMaxBuildingLevel() + 1) - (getOwnBuilding().getBuildingLevel() + copy.stackSize);
                int req = form < 0 ? -worker.getRandom().nextInt(Math.abs(form)) : worker.getRandom().nextInt(form);
                copy.stackSize += req;
                list.add(copy);
            }
        }

        getOwnBuilding().removeFromTasks(Product.ProductState.UNCRAFTED, currentProduct);
        getOwnBuilding().addToTasks(Product.ProductState.RAW, currentProduct);

        InventoryUtils.removeStacksFromItemHandler(new InvWrapper(worker.getInventoryCitizen()), list);

        return getState();
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
            checkOrRequestItems(lastRecipe.toArray(new ItemStack[lastRecipe.size()]));
            setDelay(UNABLE_TO_CRAFT_DELAY);
            return PREPARING;
        }

        final Product product = new Product(storage.getPrimaryOutput(), recipeId);
        getOwnBuilding().addToTasks(product.getState(), product);
        currentProduct = product;
        return getState();
    }

    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState bakeBread()
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
        final List<Product> products = building.getTasks().get(Product.ProductState.PREPARED);
        if (!(furnace.getBlock() instanceof BlockFurnace) || products.isEmpty())
        {
            building.removeFromFurnaces(currentFurnace);
            return START_WORKING;
        }

        final Product product = products.get(0);
        building.removeFromTasks(Product.ProductState.PREPARED, product);

        if (product != null && product.getState() == Product.ProductState.PREPARED)
        {
            building.putInFurnace(currentFurnace, product);
            product.nextState();
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
        for (final Map.Entry<BlockPos, Product> entry : getOwnBuilding().getFurnacesWithProduct().entrySet())
        {
            if (entry.getValue() == null)
            {
                emptyFurnace = true;
                currentFurnace = entry.getKey();
            }
            else if (entry.getValue().getState() == Product.ProductState.BAKED)
            {
                currentFurnace = entry.getKey();
                return BAKER_TAKE_OUT_OF_OVEN;
            }
        }

        @NotNull final Map<Product.ProductState, List<Product>> map = getOwnBuilding().getTasks();
        if (map.isEmpty())
        {
            return BAKER_KNEADING;
        }

        if (map.containsKey(Product.ProductState.BAKED))
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
    private static AIState handleEmptyFurnace(final Map<Product.ProductState, List<Product>> map)
    {
        if (map.containsKey(Product.ProductState.PREPARED))
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
        return KNEADING_TIME - getOwnBuilding().getBuildingLevel();
    }
}
