package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final List<ItemStorage> cleanedInput = new ArrayList<>();;

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
    private final List<ItemStack> secondaryOutputs = new ArrayList<>();;

    /**
     * Tools not consumed but damanged for the recipe.
     */
    @NotNull
    private final List<ItemStack> tools = new ArrayList<>();;

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
     * The tool required to craft this recipe (in addition to any in the recipe itself)
     */
    private final EquipmentTypeEntry requiredTool;

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
    public static final LootContextParamSet recipeLootParameters = (new LootContextParamSet.Builder())
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.THIS_ENTITY)
                .required(LootContextParams.TOOL)
                .optional(LootContextParams.DAMAGE_SOURCE)
                .optional(LootContextParams.KILLER_ENTITY)
                .optional(LootContextParams.DIRECT_KILLER_ENTITY)
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
     * @param requiredTool  the tool needed to craft (in addition to anything in the recipe itself)
     */
    public RecipeStorage(final IToken<?> token, final List<ItemStorage> input, final int gridSize, @NotNull final ItemStack primaryOutput, final Block intermediate, final ResourceLocation source, final ResourceLocation type, final List<ItemStack> altOutputs, final List<ItemStack> secOutputs, final ResourceLocation lootTable, final EquipmentTypeEntry requiredTool)
    {
        this.input = Collections.unmodifiableList(input);
        this.primaryOutput = primaryOutput;
        this.alternateOutputs = altOutputs != null && !altOutputs.isEmpty() ? altOutputs : ImmutableList.of();
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
        this.requiredTool = requiredTool;

        this.processInputsAndTools(secOutputs);
    }

    /**
     * Process the input and tools and alter things accordingly.
     * @param secOutputs the secondary outputs coming from the constructor.
     */
    private void processInputsAndTools(@Nullable final List<ItemStack> secOutputs)
    {
        this.cleanedInput.clear();
        this.tools.clear();
        this.secondaryOutputs.clear();

        if (secOutputs != null)
        {
            for (final ItemStack secOutput : secOutputs)
            {
                if (!secOutput.isEmpty() && secOutput.getItem() != ModItems.buildTool.get())
                {
                    this.secondaryOutputs.add(secOutput);
                }
            }
        }

        final List<ItemStorage> items = new ArrayList<>();
        for (final ItemStorage input : input)
        {
            ItemStorage inputItem = input;
            if (inputItem.isEmpty() || inputItem.getItem() == ModItems.buildTool.get())
            {
                continue;
            }

            final ItemStack container = inputItem.getItemStack().getCraftingRemainingItem();
            if (secOutputs == null && !ItemStackUtils.isEmpty(container))
            {
                container.setCount(inputItem.getAmount());
                this.secondaryOutputs.add(container);
            }

            for (ItemStack result : this.secondaryOutputs)
            {
                if (ItemStackUtils.compareItemStacksIgnoreStackSize(inputItem.getItemStack(), result, false, true))
                {
                    inputItem = new ItemStorage(inputItem.getItemStack(), inputItem.getAmount(), true, inputItem.shouldIgnoreNBTValue);
                    this.tools.add(result);
                    this.secondaryOutputs.remove(result);
                    break;
                }
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

        for (final ItemStorage storage : items)
        {
            this.cleanedInput.add(new ImmutableItemStorage(storage));
        }
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
        final List<ItemStorage> items = getCleanedInput();

        for (final ItemStorage storage : items)
        {
            final ItemStack stack = storage.getItemStack();
            final int availableCount = InventoryUtils.getItemCountInItemHandlers(
              ImmutableList.copyOf(inventories),
              itemStack -> !ItemStackUtils.isEmpty(itemStack)
                             && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, !storage.ignoreNBT()));

            if (!canFulfillItemStorage(qty, existingRequirements, availableCount, storage))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canFullFillRecipe(final int qty, final Map<ItemStorage, Integer> existingRequirements, @NotNull final List<IItemHandler> citizen, @NotNull final IBuilding building)
    {
        final List<ItemStorage> items = getCleanedInput();

        for (final ItemStorage storage : items)
        {
            final ItemStack stack = storage.getItemStack();
            final int availableCount = InventoryUtils.getItemCountInItemHandlers(citizen,
              itemStack -> !ItemStackUtils.isEmpty(itemStack)
                             && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, !storage.ignoreNBT()))
                                         + InventoryUtils.getCountFromBuilding(building, storage);;

            if (!canFulfillItemStorage(qty, existingRequirements, availableCount, storage))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the available qty matches the quantity we need.
     * @param qty the number of recipe iterations.
     * @param existingRequirements the existing requirements to skip.
     * @param availableCount the available count.
     * @param storage the storage to check.
     * @return true if can fulfill, else false.
     */
    private boolean canFulfillItemStorage(final int qty, final Map<ItemStorage, Integer> existingRequirements, int availableCount, final ItemStorage storage)
    {
        final ItemStack stack = storage.getItemStack();
        final int neededCount;
        if(!secondaryOutputs.isEmpty() || !tools.isEmpty())
        {
            if(!ItemStackUtils.compareItemStackListIgnoreStackSize(this.getCraftingToolsAndSecondaryOutputs(), stack, false, !storage.ignoreNBT()))
            {
                neededCount = storage.getAmount() * qty;
            }
            else
            {
                neededCount = storage.getAmount();
            }
        }
        else
        {
            final ItemStack container = stack.getCraftingRemainingItem();
            if(ItemStackUtils.isEmpty(container) || !ItemStackUtils.compareItemStacksIgnoreStackSize(stack, container, false, !storage.ignoreNBT()))
            {
                neededCount = storage.getAmount() * qty;
            }
            else
            {
                neededCount = storage.getAmount();
            }
        }

        return availableCount >= neededCount + existingRequirements.getOrDefault(storage, 0);
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
              || requiredTool != that.requiredTool
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
            requiredTool,
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
        //Calculate space needed by the secondary outputs, but only if there is a primary output.
        if(!secondaryOutputs.isEmpty() && !ItemStackUtils.isEmpty(getPrimaryOutput()))
        {
            resultStacks.addAll(secondaryOutputs);
        }
        else
        {
            for (final ItemStorage stack : input)
            {
                final ItemStack container = stack.getItemStack().getCraftingRemainingItem();
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
     * Check for space, remove items, and insert crafted items, returning a copy of the crafted items.
     *
     * @param context loot context
     * @param handlers the handlers to use
     * @return copy of the crafted items if successful, null on failure
     */
    @Override
    public List<ItemStack> fullfillRecipeAndCopy(final LootParams context, final List<IItemHandler> handlers, boolean doInsert)
    {
        if (!checkForFreeSpace(handlers) || !canFullFillRecipe(1, Collections.emptyMap(), handlers.toArray(new IItemHandler[0])))
        {
            return null;
        }

        final AbstractEntityCitizen citizen = (AbstractEntityCitizen) context.getParamOrNull(LootContextParams.THIS_ENTITY);

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
                boolean isTool = ItemStackUtils.compareItemStackListIgnoreStackSize(tools, stack, false, !storage.ignoreNBT());
                int slotOfStack =
                  InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(handler, itemStack ->
                          ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, !storage.ignoreNBT()) &&
                                  (!isTool || !stack.isDamageableItem() || ItemStackUtils.getDurability(itemStack) > 0));

                while (slotOfStack != -1 && amountNeeded > 0)
                {
                    if(citizen != null && isTool)
                    {
                        if (stack.isDamageableItem())
                        {
                            ItemStack toDamage = handler.extractItem(slotOfStack, 1, false);
                            if (!ItemStackUtils.isEmpty(toDamage))
                            {
                                // The 4 parameter inner call from forge is for adding a callback to alter the damage caused,
                                // but unlike its description does not actually damage the item(despite the same function name). So used to just calculate the damage.
                                toDamage.hurtAndBreak(toDamage.getItem().damageItem(stack, 1, citizen, item -> item.broadcastBreakEvent(InteractionHand.MAIN_HAND)), citizen, item -> item.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                            }
                            if (!ItemStackUtils.isEmpty(toDamage))
                            {
                                handler.insertItem(slotOfStack, toDamage, false);
                            }
                        }
                        --amountNeeded;
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
                            return null;
                        }

                        amountNeeded -= count;
                        if (amountNeeded > 0)
                        {
                            slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(handler,
                            itemStack -> !ItemStackUtils.isEmpty(itemStack) && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, false, !storage.ignoreNBT()));
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
                return null;
            }
        }

        return insertCraftedItems(handlers, getPrimaryOutput(), context, doInsert);
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
    private List<ItemStack> insertCraftedItems(final List<IItemHandler> handlers, ItemStack outputStack, LootParams context, boolean doInsert)
    {
        final List<ItemStack> resultStacks = new ArrayList<>();
        final List<ItemStack> secondaryStacks = new ArrayList<>();

        if(!ItemStackUtils.isEmpty(outputStack))
        {
            resultStacks.add(outputStack.copy());
            if(doInsert)
            {
                for (final IItemHandler handler : handlers)
                {
                    if (InventoryUtils.addItemStackToItemHandler(handler, outputStack.copy()))
                    {
                        break;
                    }
                }
            }
            secondaryStacks.addAll(secondaryOutputs);
        }

        if (loot == null && lootTable != null)
        {
            loot = context.getLevel().getServer().getLootData().getLootTable(lootTable);
        }

        if(loot != null && context != null)
        {
            secondaryStacks.addAll(loot.getRandomItems(context));
        }

        resultStacks.addAll(secondaryStacks.stream().map(ItemStack::copy).collect(Collectors.toList()));
        if(doInsert)
        {
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

        return Collections.unmodifiableList(resultStacks);
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
            this.lootTable,         // loot table
            this.requiredTool
            );

    }

    @Override
    public RecipeStorage getClassicForMultiOutput(final Predicate<ItemStack> stackPredicate)
    {
        if(!getPrimaryOutput().isEmpty() && stackPredicate.test(getPrimaryOutput()))
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
    public EquipmentTypeEntry getRequiredTool()
    {
        return this.requiredTool;
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
