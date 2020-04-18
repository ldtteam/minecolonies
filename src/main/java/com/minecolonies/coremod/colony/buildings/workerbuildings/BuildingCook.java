package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.WindowHutCook;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.MINIMUM_STOCK;
import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_QUANTITY;
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
     * Minimum stock it can hold per level.
     */
    private static final int STOCK_PER_LEVEL   = 5;

    /**
     * The minimum stock.
     */
    private Map<ItemStorage, Integer> minimumStock = new HashMap<>();

    /**
     * The minimum stock tag.
     */
    private static final String TAG_MINIMUM_STOCK = "minstock";

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
    public IJob createJob(final ICitizenData citizen)
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
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        minimumStock.clear();

        final ListNBT minimumStockTagList = compound.getList(TAG_MINIMUM_STOCK, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < minimumStockTagList.size(); i++)
        {
            final CompoundNBT compoundNBT = minimumStockTagList.getCompound(i);
            minimumStock.put(new ItemStorage(ItemStack.read(compoundNBT)), compoundNBT.getInt(TAG_QUANTITY));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final ListNBT minimumStockTagList = new ListNBT();
        for (@NotNull final Map.Entry<ItemStorage, Integer> entry: minimumStock.entrySet())
        {
            final CompoundNBT compoundNBT = new CompoundNBT();
            entry.getKey().getItemStack().write(compoundNBT);
            compoundNBT.putInt(TAG_QUANTITY, entry.getValue());
            minimumStockTagList.add(compoundNBT);
        }
        compound.put(TAG_MINIMUM_STOCK, minimumStockTagList);

        return compound;
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
            .filter(r -> !(r instanceof PrivateWorkerCraftingProductionResolver || r instanceof PrivateWorkerCraftingRequestResolver)).collect(
            Collectors.toList());
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
                for (final IToken taskToken : assignedTasks)
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

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
       if (!AbstractBuildingCrafter.canBuildingCanLearnMoreRecipes(getBuildingLevel(), super.getRecipes().size()))
       {
           return false;
       }

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if(storage == null)
        {
            return false;
        }

        for (final ItemStorage input : storage.getCleanedInput())
        {
            if (Tags.Items.CROPS_WHEAT.contains(input.getItem()))
            {
                return false;
            }
        }

       return ItemStackUtils.CAN_EAT.test(storage.getPrimaryOutput()) || ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance().getSmeltingResult(storage.getPrimaryOutput()));
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

        if (ISFOOD.test(stack) && localAlreadyKept.stream().filter(storage -> ISFOOD.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE || !inventory)
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
        if (allowedFuel.test(stack) && localAlreadyKept.stream().filter(storage -> allowedFuel.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE || !inventory)
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

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(minimumStock.size());
        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            buf.writeItemStack(entry.getKey().getItemStack());
            buf.writeInt(entry.getValue());
        }
        buf.writeBoolean(minimumStock.size() >= minimumStockSize());
    }

    /**
     * Calculate the minimum stock size.
     * @return the size.
     */
    private int minimumStockSize()
    {
        double increase = 1;
        final MultiplierModifierResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect(MINIMUM_STOCK, MultiplierModifierResearchEffect.class);
        if (effect != null)
        {
            increase = 1 + effect.getEffect();
        }

        return (int) (getBuildingLevel() * STOCK_PER_LEVEL * increase);
    }

    /**
     * Regularly tick this building and check if we  got the minimum stock(like once a minute is still fine)
     * - If not: Check if there is a request for this already.
     * -> If not: Create a request.
     * - If so: Check if there is a request for this still.
     * -> If so: cancel it.
     */
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        final Collection<IToken<?>> list = getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(com.minecolonies.api.colony.requestsystem.requestable.Stack.class), new ArrayList<>());

        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            final ItemStack itemStack = entry.getKey().getItemStack().copy();
            final int count = getTileEntity().getItemCount(stack -> !stack.isEmpty() && stack.isItemEqual(itemStack));
            final int delta = entry.getValue() * itemStack.getMaxStackSize() - count;
            final IToken<?> request = getMatchingRequest(itemStack, list);
            if (delta > 0)
            {
                if (request == null)
                {
                    itemStack.setCount(Math.min(itemStack.getMaxStackSize(), delta));
                    final com.minecolonies.api.colony.requestsystem.requestable.Stack stack = new Stack(itemStack);
                    createRequest(getMainCitizen(), stack, true);
                }
            }
            else if (request != null)
            {
                getColony().getRequestManager().updateRequestState(request, RequestState.CANCELLED);
            }
        }
    }

    /**
     * Add the minimum stock of the warehouse to this building.
     * @param itemStack the itemStack to add.
     * @param quantity the quantity.
     */
    public void addMinimumStock(final ItemStack itemStack, final int quantity)
    {
        if (minimumStock.containsKey(new ItemStorage(itemStack)) || minimumStock.size() < minimumStockSize())
        {
            minimumStock.put(new ItemStorage(itemStack), quantity);
            markDirty();
        }
    }

    /**
     * Check if the building is already requesting this stack.
     * @param stack the stack to check.
     * @return the token if so.
     */
    private IToken<?> getMatchingRequest(final ItemStack stack, final Collection<IToken<?>> list)
    {
        for (final IToken<?> token : list)
        {
            final IRequest<?> iRequest = colony.getRequestManager().getRequestForToken(token);
            if (iRequest != null && iRequest.getRequest() instanceof Stack && ((Stack) iRequest.getRequest()).getStack().isItemEqual(stack))
            {
                return token;
            }
        }
        return null;
    }

    /**
     * BuildingCook View.
     */
    public static class View extends AbstractFilterableListsView
    {
        /**
         * The minimum stock.
         */
        private List<com.minecolonies.api.util.Tuple<ItemStorage, Integer>> minimumStock = new ArrayList<>();

        /**
         * If the warehouse reached the minimum stock limit.
         */
        private boolean reachedLimit = false;

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

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);

            minimumStock.clear();
            final int size = buf.readInt();
            for(int i = 0; i < size; i++)
            {
                minimumStock.add(new com.minecolonies.api.util.Tuple<>(new ItemStorage(buf.readItemStack()), buf.readInt()));
            }
            reachedLimit = buf.readBoolean();
        }

        /**
         * The minimum stock.
         * @return the stock.
         */
        public List<com.minecolonies.api.util.Tuple<ItemStorage, Integer>> getStock()
        {
            return minimumStock;
        }

        /**
         * Check if the warehouse has reached the limit.
         * @return true if so.
         */
        public boolean hasReachedLimit()
        {
            return reachedLimit;
        }
    }
}
