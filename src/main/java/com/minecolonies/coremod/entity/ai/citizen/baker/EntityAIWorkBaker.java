package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.minecolonies.coremod.colony.buildings.BuildingBaker;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.block.BlockFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Current furnace to walk to.
     */
    private BlockPos currentFurnace = null;


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
        if(currentFurnace == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(currentFurnace))
        {
            return getState();
        }

        final Product product = getOwnBuilding().getFurnacesWithProduct().remove(currentFurnace);
        if(product != null)
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
        //todo decide which task to do
       walkToBlock(getOwnBuilding().getLocation());
       int i = 0;
       while (i <= 10)
       {
           if (hasNotDelayed(100))
           {
               worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());
               i++;
           }
       }
       return BAKER_BAKING;
    }

    /**
     * Prepares the baker for baking and requests ingredients.
     *
     * @return the next AIState
     */
    private AIState bakeBread()
    {
        final BuildingBaker building = getOwnBuilding();
        if(currentFurnace == null || building.getFurnacesWithProduct().get(currentFurnace) != null)
        {
            return PREPARING;
        }

        if(walkToBlock(currentFurnace))
        {
            return BAKER_BAKING;
        }

        final List<Product> products = building.getTasks().get(Product.ProductState.PREPARED);
        if(!(world.getBlockState(currentFurnace).getBlock() instanceof BlockFurnace) || products.isEmpty())
        {
            building.removeFromFurnaces(currentFurnace);
            return START_WORKING;
        }

        final Product product = products.get(0);
        building.removeFromTasks(Product.ProductState.PREPARED, product);

        if(product != null && product.getState() == Product.ProductState.BAKING)
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
        if(getOwnBuilding().getFurnaces().isEmpty())
        {
            worker.sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
            return getState();
        }

        boolean emptyFurnace = false;
        for(final Map.Entry<BlockPos, Product> entry: getOwnBuilding().getFurnacesWithProduct().entrySet())
        {
            if(entry.getValue() == null)
            {
                emptyFurnace = true;
                currentFurnace = entry.getKey();
            }
            else if(entry.getValue().getState() == Product.ProductState.BAKED)
            {
                currentFurnace = entry.getKey();
                return BAKER_TAKE_OUT_OF_OVEN;
            }
        }

        @NotNull final Map<Product.ProductState, List<Product>> map = getOwnBuilding().getTasks();
        if(map.isEmpty())
        {
            return BAKER_KNEADING;
        }

        if(map.containsKey(Product.ProductState.BAKED))
        {
            return BAKER_FINISHING;
        }

        if(emptyFurnace)
        {
            if(map.containsKey(Product.ProductState.PREPARED))
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
}
