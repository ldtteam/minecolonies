package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.WindowHutCook;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import com.minecolonies.coremod.util.FurnaceRecipes;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the cook building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingCook extends AbstractBuildingFurnaceUser
{
    /**
     * The cook string.
     */
    private static final String COOK_DESC = "cook";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(ItemStackUtils.ISFOOD, new Tuple<>(STACKSIZE, true));
        keepX.put(ItemStackUtils.ISCOOKABLE, new Tuple<>(STACKSIZE, true));
        keepX.put(FurnaceTileEntity::isFuel, new Tuple<>(STACKSIZE, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COOK_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobCook(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COOK_DESC;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
    }

    @Override
    public boolean canBeGathered()
    {
        return super.canBeGathered() &&
                 this.getAssignedCitizen().stream()
                   .map(c -> c.getJob(AbstractJobCrafter.class))
                   .filter(Objects::nonNull)
                   .allMatch(AbstractJobCrafter::hasTask);
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final Collection<IRequestResolver<?>> supers =
          super.createResolvers().stream()
            .filter(r -> !(r instanceof PrivateWorkerCraftingProductionResolver || r instanceof PrivateWorkerCraftingRequestResolver))
            .collect(Collectors.toList());
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new PublicWorkerCraftingRequestResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new PublicWorkerCraftingProductionResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<ItemStorage, Tuple<Integer, Boolean>> recipeOutputs = new HashMap<>();
        for (final ICitizenData citizen : getAssignedCitizen())
        {
            if (citizen.getJob() instanceof AbstractJobCrafter)
            {
                final List<IToken<?>> assignedTasks = citizen.getJob(AbstractJobCrafter.class).getAssignedTasks();
                for (final IToken<?> taskToken : assignedTasks)
                {
                    final IRequest<? extends PublicCrafting> request = (IRequest<? extends PublicCrafting>) colony.getRequestManager().getRequestForToken(taskToken);
                    final IRecipeStorage recipeStorage = getFirstRecipe(request.getRequest().getStack());
                    if (recipeStorage != null)
                    {
                        for (final ItemStorage itemStorage : recipeStorage.getCleanedInput())
                        {
                            int amount = itemStorage.getAmount();
                            if (recipeOutputs.containsKey(itemStorage))
                            {
                                amount = recipeOutputs.get(itemStorage).getA() + itemStorage.getAmount();
                            }
                            recipeOutputs.put(itemStorage, new Tuple<>(amount, false));
                        }

                        final ItemStorage output = new ItemStorage(recipeStorage.getPrimaryOutput());
                        if (recipeOutputs.containsKey(output))
                        {
                            output.setAmount(recipeOutputs.get(output).getA() + output.getAmount());
                        }
                        recipeOutputs.put(output, new Tuple<>(output.getAmount(), false));
                    }
                }
            }
        }

        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(keepX);
        toKeep.putAll(recipeOutputs.entrySet().stream().collect(Collectors.toMap(key -> (stack -> stack.isItemEqual(key.getKey().getItemStack())), Map.Entry::getValue)));
        return toKeep;
    }

    // Adding request system aware versions of the furnace recipes, to make keeping stock easier, and allow crafting chains. 
    // To find what to add, we'll run through the predefined ingredients, and if any of them are smeltable, we'll add the recipe
    // Something to consider in the future is adding the concept of 'free' recipes that don't count against the building limit. 
    @Override
    public void checkForWorkerSpecificRecipes()
    {

        ResourceLocation ingredients = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_ingredient"));

        for (final Item item : ItemTags.getCollection().getOrCreate(ingredients).getAllElements())
        {
            final ItemStack smeltingResult = FurnaceRecipes.getInstance().getSmeltingResult(new ItemStack(item, 1));
            if (ItemStackUtils.CAN_EAT.test(smeltingResult))
            {

                final IRecipeStorage storage = StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    ImmutableList.of(new ItemStack(item, 1)),
                    1,
                    smeltingResult,
                    Blocks.FURNACE);
          
                addRecipeToList(IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(storage));
            }
        }
    }
    
    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {
        Optional<Boolean> isRecipeAllowed;

        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        isRecipeAllowed = super.canRecipeBeAddedBasedOnTags(token);
        if (isRecipeAllowed.isPresent())
        {
            return isRecipeAllowed.get();
        }
        else
        {
            // Additional recipe rules

            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

            return ItemStackUtils.CAN_EAT.test(storage.getPrimaryOutput())
                     || ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance()
                                                      .getSmeltingResult(storage.getPrimaryOutput()));

            // End Additional recipe rules
        }
    }

    // Overriding this so that if the new recipe produces a smeltable, we add that furnace recipe also
    // This allows deep crafting chains of foods with many of the food expansion mods. 
    @Override
    public void addRecipeToList(final IToken<?> token)
    {
        super.addRecipeToList(token);
        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        final ItemStack input = storage.getPrimaryOutput();
        input.setCount(1);

        // Check and see if the new recipe produces a cookable food, and add the smelting recipe if so
        if(storage.getIntermediate() != Blocks.FURNACE && ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance()
                                                        .getSmeltingResult(input)) )
        {
            final IRecipeStorage cookedStorage = StandardFactoryController.getInstance().getNewInstance(
                TypeConstants.RECIPE,
                StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                ImmutableList.of(input),
                1,
                FurnaceRecipes.getInstance().getSmeltingResult(storage.getPrimaryOutput()),
                Blocks.FURNACE);
      
            super.addRecipeToList(IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(cookedStorage));            
        }
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Adaptability;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Knowledge;
    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return true;
    }

    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory)
    {
        if (stack.isEmpty())
        {
            return 0;
        }

        if (inventory && minimumStock.containsKey(new ItemStorage(stack)))
        {
            return stack.getCount();
        }

        if (ISFOOD.test(stack) && (localAlreadyKept.stream().filter(storage -> ISFOOD.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE || !inventory))
        {
            final ItemStorage kept = new ItemStorage(stack);
            if (localAlreadyKept.contains(kept))
            {
                kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
            }
            localAlreadyKept.add(kept);
            return 0;
        }

        final Predicate<ItemStack> allowedFuel = theStack -> getAllowedFuel().stream().anyMatch(fuelStack -> fuelStack.isItemEqual(theStack));
        if (allowedFuel.test(stack) && (localAlreadyKept.stream().filter(storage -> allowedFuel.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE
              || !inventory))
        {
            final ItemStorage kept = new ItemStorage(stack);
            if (localAlreadyKept.contains(kept))
            {
                kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
            }
            localAlreadyKept.add(kept);
            return 0;
        }

        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.cook;
    }

    /**
     * BuildingCook View.
     */
    public static class View extends AbstractFilterableListsView
    {
        /**
         * Instantiate the cook view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutCook(this);
        }
    }
}
