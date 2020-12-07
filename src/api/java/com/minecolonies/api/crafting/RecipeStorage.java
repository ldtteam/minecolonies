package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * Class used to represent a recipe in minecolonies.
 */
public class RecipeStorage implements IRecipeStorage
{

    /**
     * Type of storage this recipe represents
     */
    private final AbstractRecipeType<IRecipeStorage> recipeType;

    /**
     * Where this recipe came from
     * For custom recipes, it's the id of the recipe
     */
    private final ResourceLocation recipeSource;

    /**
     * Input required for the recipe.
     */
    @NotNull
    private final List<ItemStack> input;

    @NotNull
    private final List<ItemStorage> cleanedInput;

    /**
     * Primary output generated for the recipe.
     */
    @NotNull
    private final ItemStack primaryOutput;

    /**
     * Alternate output generated for the recipe.
     */
    @Nullable
    private final List<ItemStack> alternateOutputs;

    /**
     * Alternate output generated for the recipe.
     */
    @Nullable
    private final List<ItemStack> secondaryOutputs;

    /**
     * The intermediate required for the recipe (e.g furnace).
     */
    private final Block intermediate;

    /**
     * Grid size required for the recipe.
     */
    private final int gridSize;

    /**
     * The token of the RecipeStorage.
     */
    private final IToken<?> token;

    /**
     * The Resource location of the Loot Table to use for possible outputs
     */
    private final ResourceLocation lootTable;

    /**
     * The cached loot table for possible outputs
     */
    private LootTable loot;

    /**
     * Create an instance of the recipe storage.
     *
     * @param token         the token of the storage.
     * @param input         the list of input items (required for the recipe).
     * @param gridSize      the required grid size to make it.
     * @param primaryOutput the primary output of the recipe.
     * @param intermediate  the intermediate to use (e.g furnace).
     * @param source        the source of this recipe (ie: minecolonies:crafter/recipename, "player name", "improvement", etc)
     * @param type          What type of recipe this is. (ie: minecolonies:classic)
     * @param altOutputs    List of alternate outputs for a multi-output recipe
     * @param secOutputs    List of secondary outputs for a recipe. this includes containers, etc. 
     * @param lootTable     Loot table to use for possible alternate outputs
     */
    public RecipeStorage(final IToken<?> token, final List<ItemStack> input, final int gridSize, @NotNull final ItemStack primaryOutput, final Block intermediate, final ResourceLocation source, final ResourceLocation type, final List<ItemStack> altOutputs, final List<ItemStack> secOutputs, final ResourceLocation lootTable)
    {
        this.input = Collections.unmodifiableList(input);
        this.cleanedInput = new ArrayList<>();
        this.cleanedInput.addAll(this.calculateCleanedInput());
        this.primaryOutput = primaryOutput;
        this.alternateOutputs = altOutputs != null ? altOutputs : ImmutableList.of();
        this.secondaryOutputs = secOutputs != null ? secOutputs.stream().filter(i -> i.getItem() != ModItems.buildTool).collect(Collectors.toList()): this.calculateSecondaryOutputs();
        this.gridSize = gridSize;
        this.intermediate = intermediate;
        this.token = token;
        this.recipeSource = source;
        IForgeRegistry<RecipeTypeEntry> recipeTypes = MinecoloniesAPIProxy.getInstance().getRecipeTypeRegistry();
        if(type != null && recipeTypes.containsKey(type))
        {
            this.recipeType = recipeTypes.getValue(type).getHandlerProducer().apply(this);
        }
        else
        {
            this.recipeType = recipeTypes.getValue(recipeTypes.getDefaultKey()).getHandlerProducer().apply(this);
        }

        this.lootTable = lootTable;
    }

    @Override
    public List<ItemStack> getInput()
    {
        return new ArrayList<>(input);
    }

    @NotNull
    @Override
    public List<ItemStorage> getCleanedInput()
    {
        return this.cleanedInput;
    }

    /**
     * Calculate a compressed input list from the ingredients.
     * @return a compressed and immutable list.
     */
    private List<ImmutableItemStorage> calculateCleanedInput()
    {
        final List<ItemStorage> items = new ArrayList<>();

        for (final ItemStack stack : input)
        {
            if (ItemStackUtils.isEmpty(stack) || stack.getItem() == ModItems.buildTool)
            {
                continue;
            }

            ItemStorage storage = new ItemStorage(stack.copy());
            if (items.contains(storage))
            {
                final int index = items.indexOf(storage);
                final ItemStorage tempStorage = items.remove(index);
                tempStorage.setAmount(tempStorage.getAmount() + storage.getAmount());
                storage = tempStorage;
            }
            items.add(storage);
        }

        final List<ImmutableItemStorage> immutableItems = new ArrayList<>();
        for (final ItemStorage storage : items)
        {
            immutableItems.add(new ImmutableItemStorage(storage));
        }
        return immutableItems;
    }

    /**
     * Calculate secondary stacks if they aren't provided. 
     * @return the list of secondary outputs
     */
    private List<ItemStack> calculateSecondaryOutputs()
    {
        List<ItemStack> secondaryStacks = new ArrayList<>();
        for (final ItemStack stack : input)
        {
            if (stack.getItem() == ModItems.buildTool)
            {
                continue;
            }

            final ItemStack container = stack.getItem().getContainerItem(stack);
            if (!ItemStackUtils.isEmpty(container))
            {
                container.setCount(stack.getCount());
                secondaryStacks.add(container);
            }
        }
        return ImmutableList.copyOf(secondaryStacks);
    }

    @NotNull
    @Override
    public ItemStack getPrimaryOutput()
    {
        return primaryOutput;
    }

    @Override
    public int getGridSize()
    {
        return gridSize;
    }

    @Override
    public Block getIntermediate()
    {
        return this.intermediate;
    }

    @Override
    public boolean canFullFillRecipe(final int qty, final Map<ItemStorage, Integer> existingRequirements, @NotNull final IItemHandler... inventories)
    {
        final int neededMultiplier = CraftingUtils.calculateMaxCraftingCount(qty, this);
        final List<ItemStorage> items = getCleanedInput();

        for (final ItemStorage storage : items)
        {
            final ItemStack stack = storage.getItemStack();
            final int availableCount = InventoryUtils.getItemCountInItemHandlers(
              ImmutableList.copyOf(inventories),
              itemStack -> !ItemStackUtils.isEmpty(itemStack)
                             && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, true));

            final int neededCount;
            if(!secondaryOutputs.isEmpty())
            {
                if(!ItemStackUtils.compareItemStackListIgnoreStackSize(secondaryOutputs, stack, false, true))
                {
                    neededCount = storage.getAmount() * neededMultiplier;
                }
                else
                {
                    neededCount = storage.getAmount();
                }
            }
            else
            {
                final ItemStack container = stack.getItem().getContainerItem(stack);
                if(ItemStackUtils.isEmpty(container) || !ItemStackUtils.compareItemStacksIgnoreStackSize(stack, container, false, true))
                {
                    neededCount = storage.getAmount() * neededMultiplier;
                }
                else
                {
                    neededCount = storage.getAmount();
                }
            }

            if (availableCount < neededCount + existingRequirements.getOrDefault(storage, 0))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RecipeStorage))
        {
            return false;
        }

        final RecipeStorage that = (RecipeStorage) o;

        if (gridSize != that.gridSize
              || input.size() != that.input.size()
              || cleanedInput.size() != that.cleanedInput.size()
              || (alternateOutputs.size() != that.alternateOutputs.size())
              || ((secondaryOutputs == null) != (that.secondaryOutputs == null))
              || (secondaryOutputs != null && secondaryOutputs.size() != that.secondaryOutputs.size())
              || !ItemStackUtils.compareItemStacksIgnoreStackSize(primaryOutput, that.primaryOutput, false, true))
        {
            return false;
        }

        for (int i = 0; i < cleanedInput.size(); i++)
        {
            if(!cleanedInput.get(i).equals(that.cleanedInput.get(i)) || cleanedInput.get(i).getAmount() != that.cleanedInput.get(i).getAmount())
            {
                return false;
            }
        }

        
        if(this.recipeSource != null && !this.recipeSource.equals(that.recipeSource))
        {
            return false;
        }

        if(this.recipeSource == null && that.recipeSource != null)
        {
            return false; 
        }

        if(this.lootTable != null && !this.lootTable.equals(that.lootTable))
        {
            return false;
        }

        if(this.lootTable == null && that.lootTable != null)
        {
            return false; 
        }

        if(!this.recipeType.getId().equals(that.recipeType.getId()))
        {
            return false;
        }
        


        for(int i = 0; i< alternateOutputs.size(); i++)
        {
            ItemStack left = alternateOutputs.get(i);
            ItemStack right = that.alternateOutputs.get(i);
            if(!ItemStackUtils.compareItemStacksIgnoreStackSize(left, right, false, true) || left.getCount() != right.getCount())
            {
                return false;
            }
        }

        if(secondaryOutputs != null)
        {
            if(that.secondaryOutputs == null)
            {
                return false;
            }
            for(int i = 0; i< secondaryOutputs.size(); i++)
            {
                if(!ItemStackUtils.compareItemStacksIgnoreStackSize(secondaryOutputs.get(i),that.secondaryOutputs.get(i), false, true))
                {
                    return false;
                }
            }
        }

        return intermediate == null ? (that.intermediate == null) : intermediate.equals(that.intermediate);
    }

    @Override
    public int hashCode()
    {
        int result = input.hashCode();
        result = 31 * result + primaryOutput.hashCode();
        result = 31 * result + (intermediate != null ? intermediate.hashCode() : 0);
        result = 31 * result + gridSize;
        if(recipeSource != null)
        {
            result = 31 * result + recipeSource.hashCode();
        }
        if(recipeType != null && !(recipeType instanceof ClassicRecipe))
        {
            result = 31 * result + recipeType.hashCode();
        }
        if(alternateOutputs != null && !alternateOutputs.isEmpty())
        {
            result = 31 * result + alternateOutputs.hashCode();
        }
        if(secondaryOutputs != null && !secondaryOutputs.isEmpty())
        {
            result = 31 * result + secondaryOutputs.hashCode();
        }
        return result;
    }

    /**
     * Check for free space in the handlers.
     *
     * @param handlers the handlers to check.
     * @return true if enough space.
     */
    private boolean checkForFreeSpace(final List<IItemHandler> handlers)
    {
        final List<ItemStack> resultStacks = new ArrayList<>();
        //Calculate space needed by the secondary outputs
        if(!secondaryOutputs.isEmpty())
        {
            resultStacks.addAll(secondaryOutputs);
        }
        else
        {
            for (final ItemStack stack : input)
            {
                final ItemStack container = stack.getItem().getContainerItem(stack);
                if (!ItemStackUtils.isEmpty(container))
                {
                    container.setCount(stack.getCount());
                    resultStacks.add(container);
                }
            }
        }
        //Include the primary output in the space check
        resultStacks.add(getPrimaryOutput());
        if (resultStacks.size() > getInput().size())
        {
            int freeSpace = 0;
            for (final IItemHandler handler : handlers)
            {
                freeSpace += handler.getSlots() - InventoryUtils.getAmountOfStacksInItemHandler(handler);
            }

            return freeSpace >= resultStacks.size() - getInput().size();
        }
        return true;
    }

    /**
     * Check for space, remove items, and insert crafted items.
     *
     * @param handlers the handlers to use.
     * @return true if succesful.
     */
    @Override
    public boolean fullfillRecipe(final World world, final List<IItemHandler> handlers)
    {
        if (!checkForFreeSpace(handlers) || !canFullFillRecipe(1, Collections.emptyMap(), handlers.toArray(new IItemHandler[0])))
        {
            return false;
        }

        for (final ItemStorage storage : getCleanedInput())
        {
            final ItemStack stack = storage.getItemStack();
            int amountNeeded = storage.getAmount();

            if (amountNeeded == 0)
            {
                break;
            }

            for (final IItemHandler handler : handlers)
            {
                int slotOfStack =
                  InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(handler, itemStack -> !ItemStackUtils.isEmpty(itemStack) && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, true));

                while (slotOfStack != -1 && amountNeeded > 0)
                {
                    final int count = ItemStackUtils.getSize(handler.getStackInSlot(slotOfStack));
                    final ItemStack extractedStack = handler.extractItem(slotOfStack, amountNeeded, false).copy();

                    //This prevents the AI and for that matter the server from getting stuck in case of an emergency.
                    //Deletes some items, but hey.
                    if (ItemStackUtils.isEmpty(extractedStack))
                    {
                        handler.insertItem(slotOfStack, extractedStack, false);
                        return false;
                    }

                    amountNeeded -= count;
                    if (amountNeeded > 0)
                    {
                        slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(handler,
                          itemStack -> !ItemStackUtils.isEmpty(itemStack) && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, true));
                    }
                }

                // stop looping handlers if we have what we need
                if (amountNeeded <= 0)
                {
                    break;
                }
            }

            if (amountNeeded > 0)
            {
                return false;
            }
        }

        insertCraftedItems(handlers, getPrimaryOutput(), world);
        return true;
    }

    @Override
    public IToken<?> getToken()
    {
        return token;
    }

    /**
     * Inserted the resulting items into the itemHandlers.
     *
     * @param handlers the handlers.
     */
    private void insertCraftedItems(final List<IItemHandler> handlers, ItemStack outputStack, World world)
    {
        for (final IItemHandler handler : handlers)
        {
            if (InventoryUtils.addItemStackToItemHandler(handler, outputStack.copy()))
            {
                break;
            }
        }

        if (loot == null && lootTable != null)
        {
            loot = world.getServer().getLootTableManager().getLootTableFromLocation(lootTable);
        }
        
        final List<ItemStack> secondaryStacks = new ArrayList<>();

        if(loot != null)
        {
            secondaryStacks.addAll(loot.generate((new LootContext.Builder((ServerWorld) world)).build(LootParameterSets.EMPTY)));
        }

        if(!secondaryOutputs.isEmpty())
        {
            secondaryStacks.addAll(secondaryOutputs);
        }
        else
        {
            for (final ItemStack stack : input)
            {
                if (stack.getItem() == ModItems.buildTool)
                {
                    continue;
                }

                final ItemStack container = stack.getItem().getContainerItem(stack);
                if (!ItemStackUtils.isEmpty(container))
                {
                    container.setCount(stack.getCount());
                    secondaryStacks.add(container);
                }
            }
        }
        for (final ItemStack stack : secondaryStacks)
        {
            for (final IItemHandler handler : handlers)
            {
                if (InventoryUtils.addItemStackToItemHandler(handler, stack.copy()))
                {
                    break;
                }
            }
        }
    }

    @Override
    public RecipeStorage getClassicForMultiOutput(final ItemStack requiredOutput)
    {
        return new RecipeStorage(
            StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
            this.input,
            this.gridSize,
            requiredOutput,
            intermediate,
            this.recipeSource,
            ModRecipeTypes.CLASSIC_ID,
            null,                   // alternate outputs
            this.secondaryOutputs,  // secondary output
            this.lootTable          // loot table
            );

    }

    @Override
    public RecipeStorage getClassicForMultiOutput(final Predicate<ItemStack> stackPredicate)
    {
        if(stackPredicate.test(getPrimaryOutput()))
        {
            return getClassicForMultiOutput(getPrimaryOutput());
        }

        for(ItemStack item : getAlternateOutputs())
        {
            if(stackPredicate.test(item))
            {
                return getClassicForMultiOutput(item);
            }
        }

        return null; 
    }

    @Override
    public AbstractRecipeType<IRecipeStorage> getRecipeType()
    {
        return recipeType;
    }

    @Override
    public ResourceLocation getRecipeSource()
    {
        return recipeSource; 
    }

    @Override
    public List<ItemStack> getAlternateOutputs()
    {
        return alternateOutputs;
    }

    @Override
    public List<ItemStack> getSecondaryOutputs()
    {
        return secondaryOutputs;
    }

    @Override
    public ResourceLocation getLootTable()
    {
        return lootTable;
    }
}
