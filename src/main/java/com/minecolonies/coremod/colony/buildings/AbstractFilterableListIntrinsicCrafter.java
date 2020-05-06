package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;

/**
 * Abstract class for all buildings which require a filterable list of allowed items.
 */
public abstract class AbstractFilterableListIntrinsicCrafter extends AbstractFilterableListBuilding
{
    /**
     * Tag to store the item list.
     */
    private static final String TAG_ITEMLIST = "itemList";

    /**
     * List of allowed items.
     */
    private final Map<String, List<ItemStorage>> itemsAllowed = new HashMap<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractFilterableListIntrinsicCrafter(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }
    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT filterableList = compound.getList(TAG_ITEMLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < filterableList.size(); ++i)
        {
            try
            {
                final CompoundNBT listItem = filterableList.getCompound(i);
                final String id = listItem.getString(TAG_ID);
                final ListNBT filterableItems = listItem.getList(TAG_ITEMLIST, Constants.NBT.TAG_COMPOUND);
                final List<ItemStorage> items = new ArrayList<>();
                for (int j = 0; j < filterableItems.size(); ++j)
                {
                    items.add(new ItemStorage(ItemStack.read(filterableItems.getCompound(j))));
                }
                if (!items.isEmpty())
                {
                    itemsAllowed.put(id, items);
                }
            }
            catch (Exception e)
            {
                Log.getLogger().info("Removing incompatible stack");
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT filterableListCompound = new ListNBT();
        for(@NotNull final Map.Entry<String, List<ItemStorage>> entry : itemsAllowed.entrySet())
        {
            @NotNull final CompoundNBT listCompound = new CompoundNBT();
            listCompound.putString(TAG_ID, entry.getKey());
            @NotNull final ListNBT filteredItems = new ListNBT();
            for(@NotNull final ItemStorage item : entry.getValue())
            {
                @NotNull final CompoundNBT itemCompound = new CompoundNBT();
                item.getItemStack().write(itemCompound);
                filteredItems.add(itemCompound);
            }
            listCompound.put(TAG_ITEMLIST, filteredItems);
            filterableListCompound.add(listCompound);
        }
        compound.put(TAG_ITEMLIST, filterableListCompound);
        return compound;
    }

    /**
     * Add a compostable item to the list.
     * @param id the string id of the item type.
     * @param item the item to add.
     */
    public void addItem(final String id, final ItemStorage item)
    {
        if(itemsAllowed.containsKey(id))
        {
            if (!itemsAllowed.get(id).contains(item))
            {
                final List<ItemStorage> list = itemsAllowed.get(id);
                list.add(item);
                itemsAllowed.put(id, list);
            }
        }
        else
        {
            final List<ItemStorage> list = new ArrayList<>();
            list.add(item);
            itemsAllowed.put(id, list);
        }
        markDirty();
    }

    /**
     * Check if the item is an allowed item.
     * @param item the item to check.
     * @param id the string id of the item type.
     * @return true if so.
     */
    public boolean isAllowedItem(final String id, final ItemStorage item)
    {
        return itemsAllowed.containsKey(id) && itemsAllowed.get(id).contains(item);
    }

    /**
     * Remove a compostable item from the list.
     * @param id the string id of the item type.
     * @param item the item to remove.
     */
    public void removeItem(final String id, final ItemStorage item)
    {
        if(itemsAllowed.containsKey(id) && itemsAllowed.get(id).contains(item))
        {
            final List<ItemStorage> list = itemsAllowed.get(id);
            list.remove(item);
            itemsAllowed.put(id, list);
        }
        markDirty();
    }

    /**
     * Getter of copy of all allowed items.
     * @return a copy.
     */
    public Map<String, List<ItemStorage>> getCopyOfAllowedItems()
    {
        return new HashMap<>(itemsAllowed);
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(itemsAllowed.size());
        for (final Map.Entry<String, List<ItemStorage>> entry : itemsAllowed.entrySet())
        {
            buf.writeString(entry.getKey());
            buf.writeInt(entry.getValue().size());
            for (final ItemStorage item : entry.getValue())
            {
                buf.writeItemStack(item.getItemStack());
            }
        }
    }

    /* Intrinsic Crafter Functionality follows here */

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
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
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

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
}
