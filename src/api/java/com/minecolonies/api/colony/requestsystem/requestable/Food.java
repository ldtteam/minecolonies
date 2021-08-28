package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Eatable requestable. Delivers a stack of food.
 */
public class Food implements IDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Food.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    private static final String NBT_EXCLUSION = "Exclusion";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;

    private List<ItemStorage> exclusionList = new ArrayList<>();

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Food(final int count) {this.count = count;}

    public Food(final int count, @NotNull final ItemStack result)
    {
        this.count = count;
        this.result = result;
    }

    public Food(final int count, @NotNull final ItemStack result, List<ItemStorage> exclusionList)
    {
        this.count = count;
        this.result = result;
        this.exclusionList = exclusionList;
    }

    public Food(final int count, List<ItemStorage> exclusionList)
    {
        this(count, ItemStackUtils.EMPTY, exclusionList);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param food       the input.
     * @return the compound.
     */
    public static CompoundTag serialize(final IFactoryController controller, final Food food)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(NBT_COUNT, food.count);

        if (!ItemStackUtils.isEmpty(food.result))
        {
            compound.put(NBT_RESULT, food.result.serializeNBT());
        }
        if (!food.exclusionList.isEmpty())
        {
            @NotNull final ListTag items = new ListTag();
            for (@NotNull final ItemStorage item : food.exclusionList)
            {
                @NotNull final CompoundTag itemCompound = new CompoundTag();
                item.getItemStack().save(itemCompound);
                items.add(itemCompound);
            }
            compound.put(NBT_EXCLUSION, items);
        }
        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static Food deserialize(final IFactoryController controller, final CompoundTag compound)
    {
        final int count = compound.getInt(NBT_COUNT);
        final ItemStack result = compound.getAllKeys().contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;
        final List<ItemStorage> items = new ArrayList<>();

        if (compound.contains(NBT_EXCLUSION))
        {
            final ListTag filterableItems = compound.getList(NBT_EXCLUSION, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < filterableItems.size(); ++i)
            {
                items.add(new ItemStorage(ItemStack.of(filterableItems.getCompound(i))));
            }
        }

        return new Food(count, result, items);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final Food input)
    {
        buffer.writeInt(input.count);

        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            buffer.writeItem(input.result);
        }

        buffer.writeInt(input.exclusionList.size());
        for (ItemStorage item : input.exclusionList)
        {
            buffer.writeItem(item.getItemStack());
        }
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Food deserialize(final IFactoryController controller, final FriendlyByteBuf buffer) {
        final int count = buffer.readInt();
        final ItemStack result = buffer.readBoolean() ? buffer.readItem() : ItemStack.EMPTY;

        List<ItemStorage> items = new ArrayList<>();
        final int itemsCount = buffer.readInt();
        for (int i = 0; i < itemsCount; ++i)
        {
            items.add(new ItemStorage(buffer.readItem()));
        }

        if (!items.isEmpty())
        {
            return new Food(count, result, items);
        }
        return new Food(count, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return ItemStackUtils.ISFOOD.test(stack) && !exclusionList.contains(new ItemStorage(stack)) && !(ItemStackUtils.ISCOOKABLE.test(stack) && exclusionList.contains(new ItemStorage(MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack))));
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(@NotNull final int newCount)
    {
        return new Food(newCount);
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public int getMinimumCount()
    {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getResult()
    {
        return result;
    }

    public List<ItemStorage> getExclusionList()
    {
        return exclusionList;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Food))
        {
            return false;
        }

        final Food food = (Food) o;

        if (getCount() != food.getCount())
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), food.getResult());
    }

    @Override
    public int hashCode()
    {
        int result1 = getCount();
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
