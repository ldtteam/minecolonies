package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.BuildingBaker;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import com.minecolonies.coremod.util.InventoryUtils;
import net.minecraft.block.BlockFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
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


    //todo first step dough: Take the wheat, by chance need between 3-5 (6-building level) so 5,4,3,2,1 (by chance could need only 1 each bread), hit for time depending on level at hut chest with wheat in hand. (Particle effects?)

    //todo prepare, hit with bread in hand on hut block for fixed time depending on his level.

    //todo ready, but bread in chest and start over.

    //todo if he has no wheat, request it to dman, always if he can't find wheat in start working and he has nothing to do at the moment

    //todo and hit with the ingredients

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
                new AITarget(BAKER_TAKE_OUT_OF_OVEN, this::takeFromOven)
        );
        worker.setSkillModifier(
                INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
                        + DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity());
        worker.setCanPickUpLoot(true);
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

        final Product product = getOwnBuilding().getFurnacesWithProduct().remove(currentFurnace);
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

        if(currentProduct == null)
        {
            final List<IItemHandler> handlers = new ArrayList<>();
            handlers.add(new InvWrapper(worker.getInventoryCitizen()));
            handlers.add(new InvWrapper(getOwnBuilding().getTileEntity()));

            for(final BlockPos pos: getOwnBuilding().getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if(entity instanceof TileEntityChest)
                {
                    handlers.add(new InvWrapper((TileEntityChest) entity));
                }
            }

            RecipeStorage storage = null;
            int recipeId = 0;
            for(final RecipeStorage tempStorage : BakerRecipes.getRecipes())
            {
                if(tempStorage.canFullFillRecipe(handlers.toArray(new IItemHandler[handlers.size()])))
                {
                    storage = tempStorage;
                    break;
                }
                recipeId++;
            }

            if(storage == null)
            {
                final List<RecipeStorage> recipes = BakerRecipes.getRecipes();
                final List<ItemStack> lastRecipe = recipes.get(recipes.size()-1).getInput();
                checkOrRequestItems(lastRecipe.toArray(new ItemStack[lastRecipe.size()]));
                setDelay(UNABLE_TO_CRAFT_DELAY);
                return PREPARING;
            }

            final Product product = new Product(storage.getPrimaryOutput(), recipeId);
            currentProduct = product;
            return getState();
        }

        if(currentProduct.getState() == Product.ProductState.UNCRAFTED)
        {
            final List<RecipeStorage> recipes = BakerRecipes.getRecipes();
            final List<ItemStack> lastRecipe = recipes.get(recipes.size()-1).getInput();
            if(checkOrRequestItems(lastRecipe.toArray(new ItemStack[lastRecipe.size()])))
            {
                return getState();
            }
            currentProduct.nextState();

            if(BakerRecipes.getRecipes().size() < currentProduct.getRecipeId())
            {
                Log.getLogger().warn("That shouldn't happen, please report it to the mod authors with this code: RECIPEGONEMAD");
                return PREPARING;
            }

            final RecipeStorage storage = BakerRecipes.getRecipes().get(currentProduct.getRecipeId());

            InventoryUtils.removeStacksFromItemHandler(new InvWrapper(worker.getInventoryCitizen()), storage.getInput());

            return PREPARING;
        }

        if(currentProduct.getState() != Product.ProductState.RAW)
        {
            return PREPARING;
        }

        worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());


        if(progress >= getRequiredProgressForKneading())
        {
            currentProduct.nextState();
            return PREPARING;
        }

        progress++;
        setDelay(HIT_DELAY);
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

        final List<Product> products = building.getTasks().get(Product.ProductState.PREPARED);
        if (!(world.getBlockState(currentFurnace).getBlock() instanceof BlockFurnace) || products.isEmpty())
        {
            building.removeFromFurnaces(currentFurnace);
            return START_WORKING;
        }

        final Product product = products.get(0);
        building.removeFromTasks(Product.ProductState.PREPARED, product);

        if (product != null && product.getState() == Product.ProductState.BAKING)
        {
            building.putInFurnace(currentFurnace, product);
            product.nextState();
            world.setBlockState(currentFurnace, Blocks.LIT_FURNACE.getDefaultState());
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
            worker.sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
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
            if (map.containsKey(Product.ProductState.PREPARED))
            {
                return BAKER_BAKING;
            }
            return BAKER_KNEADING;
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
