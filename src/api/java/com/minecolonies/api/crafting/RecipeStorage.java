package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private final List<ItemStorage> input;

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
    @NotNull
    private final List<ItemStack> alternateOutputs;

    /**
     * Secondary outputs generated for the recipe.
     */
    @NotNull
    private final List<ItemStack> secondaryOutputs;

    /**
     * Tools not consumed but damanged for the recipe.
     */
    @NotNull
    private final List<ItemStack> tools;

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
     * The hash cache
     */
    private int hash = 0;

    /**
     * The cached loot table for possible outputs
     */
    private LootTable loot;

    /**
     * The loot parameter set definition
     */
    public static final LootParameterSet recipeLootParameters = (new LootParameterSet.Builder())
                .required(LootParameters.field_237457_g_)
                .required(LootParameters.THIS_ENTITY)
                .required(LootParameters.TOOL)
                .optional(LootParameters.DAMAGE_SOURCE)
                .optional(LootParameters.KILLER_ENTITY)
                .optional(LootParameters.DIRECT_KILLER_ENTITY)
                .build();

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
    public RecipeStorage(final IToken<?> token, final List<ItemStorage> input, final int gridSize, @NotNull final ItemStack primaryOutput, final Block intermediate, final ResourceLocation source, final ResourceLocation type, final List<ItemStack> altOutputs, final List<ItemStack> secOutputs, final ResourceLocation lootTable)
    {
        this.input = Collections.unmodifiableList(input);
        this.cleanedInput = new ArrayList<>();
        this.cleanedInput.addAll(this.calculateCleanedInput());
        this.primaryOutput = primaryOutput;
        this.alternateOutputs = altOutputs != null && !altOutputs.isEmpty() ? altOutputs : ImmutableList.of();
        this.secondaryOutputs = secOutputs != null && !secOutputs.isEmpty() ? secOutputs.stream().filter(i -> i.getItem() != ModItems.buildTool.get()).collect(Collectors.toList()): this.calculateSecondaryOutputs();
        this.gridSize = gridSize;
        this.intermediate = intermediate == null ? Blocks.AIR : intermediate;
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
        this.tools = new ArrayList<>();
        this.calculateTools();
    }

    @Override
    public List<ItemStorage> getInput()
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

        for (final ItemStorage inputItem : input)
        {
            if (inputItem.isEmpty() || inputItem.getItem() == ModItems.buildTool.get())
            {
                continue;
            }

            ItemStorage storage = inputItem.copy();
            if (items.contains(storage) )
            {
                final int index = items.indexOf(storage);
                ItemStorage tempStorage = items.remove(index);
                tempStorage.setAmount(tempStorage.getAmount() + storage.getAmount());
                if(!tempStorage.matchDefinitionEquals(storage))
                {
                    int amount = tempStorage.getAmount();
                    tempStorage = new ItemStorage(tempStorage.getItemStack(), tempStorage.ignoreDamageValue() || storage.ignoreDamageValue(), tempStorage.ignoreNBT() || storage.ignoreNBT());
                    tempStorage.setAmount(amount);
                }
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
        final List<ItemStack> secondaryStacks = new ArrayList<>();
        for (final ItemStorage inputItem : input)
        {
            if (inputItem.getItem() == ModItems.buildTool.get())
            {
                continue;
            }

            final ItemStack container = inputItem.getItem().getContainerItem(inputItem.getItemStack());
            if (!ItemStackUtils.isEmpty(container))
            {
                container.setCount(inputItem.getAmount());
                secondaryStacks.add(container);
            }
        }
        return ImmutableList.copyOf(secondaryStacks);
    }

    /**
     * Calculate tools from comparing inputs and outputs. 
     */
    private void calculateTools()
    {
        for(ItemStorage item : getCleanedInput())
        {
            for(ItemStack result: getSecondaryOutputs())
            {
                if(ItemStackUtils.compareItemStacksIgnoreStackSize(item.getItemStack(), result, false, true) && result.isDamageable())
                {
                    tools.add(result);
                    secondaryOutputs.remove(result);
                    break;
                }
            }
        }
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
        final int neededMultiplier = ItemStackUtils.isEmpty(this.primaryOutput) ? qty : CraftingUtils.calculateMaxCraftingCount(qty, this);
        final List<ItemStorage> items = getCleanedInput();

        for (final ItemStorage storage : items)
        {
            final ItemStack stack = storage.getItemStack();
            final int availableCount = InventoryUtils.getItemCountInItemHandlers(
              ImmutableList.copyOf(inventories),
              itemStack -> !ItemStackUtils.isEmpty(itemStack)
                             && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, true));

            final int neededCount;
            if(!secondaryOutputs.isEmpty() || !tools.isEmpty())
            {
                if(!ItemStackUtils.compareItemStackListIgnoreStackSize(this.getCraftingToolsAndSecondaryOutputs(), stack, false, true))
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
              || cleanedInput.size() != that.cleanedInput.size()
              || alternateOutputs.size() != that.alternateOutputs.size()
              || secondaryOutputs.size() != that.secondaryOutputs.size()
              || tools.size() != that.tools.size()
              || !Objects.equals(this.recipeSource, that.recipeSource)
              || !Objects.equals(this.lootTable, that.lootTable)
              || !this.recipeType.getId().equals(that.recipeType.getId())
              || !ItemStackUtils.compareItemStacksIgnoreStackSize(primaryOutput, that.primaryOutput, false, true))
        {
            return false;
        }

        for (int i = 0; i < cleanedInput.size(); i++)
        {
            if(!cleanedInput.get(i).equals(that.cleanedInput.get(i)) || !cleanedInput.get(i).matchDefinitionEquals(that.cleanedInput.get(i)) || cleanedInput.get(i).getAmount() != that.cleanedInput.get(i).getAmount())
            {
                return false;
            }
        }

        for(int i = 0; i< alternateOutputs.size(); i++)
        {
            final ItemStack left = alternateOutputs.get(i);
            final ItemStack right = that.alternateOutputs.get(i);
            if(!ItemStackUtils.compareItemStacksIgnoreStackSize(left, right, false, true) || left.getCount() != right.getCount())
            {
                return false;
            }
        }

        for (int i = 0; i < secondaryOutputs.size(); i++)
        {
            final ItemStack left = secondaryOutputs.get(i);
            final ItemStack right = that.secondaryOutputs.get(i);
            if (!ItemStackUtils.compareItemStacksIgnoreStackSize(left, right, false, true) || left.getCount() != right.getCount())
            {
                return false;
            }
        }

        for (int i = 0; i < tools.size(); i++)
        {
            final ItemStack left = tools.get(i);
            final ItemStack right = that.tools.get(i);
            if (!ItemStackUtils.compareItemStacksIgnoreStackSize(left, right, false, true) || left.getCount() != right.getCount())
            {
                return false;
            }
        }

        return Objects.equals(intermediate, that.intermediate);
    }

    @Override
    public int hashCode()
    {
        if(hash == 0)
        {
            hash = Objects.hash(cleanedInput, 
            primaryOutput.getItem(),
            primaryOutput.getCount(),
            intermediate,
            gridSize,
            hashableItemStackList(alternateOutputs),
            hashableItemStackList(secondaryOutputs),
            hashableItemStackList(tools));
        }

        return hash;
    }

    /**
     * Convert a list of itemstacks into something hashable
     * @param items List of item stacks to convert
     * @return hashtable of items and counts
     */
    private Map<Item, Integer> hashableItemStackList(List<ItemStack> items)
    {
        Map<Item, Integer> hashableList = new HashMap<>();
        for(ItemStack item: items)
        {
            hashableList.put(item.getItem(), item.getCount());
        }
        return hashableList;
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
            for (final ItemStorage stack : input)
            {
                final ItemStack container = stack.getItem().getContainerItem(stack.getItemStack());
                if (!ItemStackUtils.isEmpty(container))
                {
                    container.setCount(stack.getAmount());
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
    public boolean fullfillRecipe(final LootContext context, final List<IItemHandler> handlers)
    {
        if (!checkForFreeSpace(handlers) || !canFullFillRecipe(1, Collections.emptyMap(), handlers.toArray(new IItemHandler[0])))
        {
            return false;
        }

        final AbstractEntityCitizen citizen = (AbstractEntityCitizen) context.get(LootParameters.THIS_ENTITY);

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
                    if(citizen != null && ItemStackUtils.compareItemStackListIgnoreStackSize(tools, stack, false, true) && ItemStackUtils.getDurability(handler.getStackInSlot(slotOfStack)) > 0 )
                    {
                        ItemStack toDamage = handler.extractItem(slotOfStack,1, false);
                        if (!ItemStackUtils.isEmpty(toDamage))
                        {
                            // The 4 parameter inner call from forge is for adding a callback to alter the damage caused,
                            // but unlike its description does not actually damage the item(despite the same function name). So used to just calculate the damage.
                            toDamage.damageItem(toDamage.getItem().damageItem(stack, 1, citizen, item -> item.sendBreakAnimation(Hand.MAIN_HAND)), citizen, item -> item.sendBreakAnimation(Hand.MAIN_HAND));
                        }
                        if (!ItemStackUtils.isEmpty(toDamage))
                        {
                            handler.insertItem(slotOfStack, toDamage, false);
                        }
                        amountNeeded -= stack.getCount();
                    }
                    else
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

        insertCraftedItems(handlers, getPrimaryOutput(), context);
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
    private void insertCraftedItems(final List<IItemHandler> handlers, ItemStack outputStack, LootContext context)
    {
        final List<ItemStack> secondaryStacks = new ArrayList<>();

        if(!ItemStackUtils.isEmpty(outputStack))
        {
            for (final IItemHandler handler : handlers)
            {
                if (InventoryUtils.addItemStackToItemHandler(handler, outputStack.copy()))
                {
                    break;
                }
            }
            secondaryStacks.addAll(secondaryOutputs);
        }

        if (loot == null && lootTable != null)
        {
            loot = context.getWorld().getServer().getLootTableManager().getLootTableFromLocation(lootTable);
        }

        if(loot != null && context != null)
        {
            secondaryStacks.addAll(loot.generate(context));
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

        for(final ItemStack item : alternateOutputs)
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

    @NotNull
    @Override
    public List<ItemStack> getAlternateOutputs()
    {
        return alternateOutputs;
    }

    @NotNull
    @Override
    public List<ItemStack> getCraftingToolsAndSecondaryOutputs()
    {
        final List<ItemStack> results = new ArrayList<>();
        results.addAll(tools);
        results.addAll(secondaryOutputs);
        return results;
    }

    @Override
    public ResourceLocation getLootTable()
    {
        return lootTable;
    }

    @NotNull
    @Override
    public List<ItemStack> getCraftingTools()
    {
        return ImmutableList.copyOf(tools);
    }

    @NotNull
    @Override
    public List<ItemStack> getSecondaryOutputs()
    {
        return ImmutableList.copyOf(secondaryOutputs);
    }
}
