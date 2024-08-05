package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
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
    private final static Set<TypeToken<?>> TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Food.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_COUNT  = "Count";
    private static final String NBT_RESULT = "Result";
    private static final String NBT_EXCLUSION = "Exclusion";
    private static final String NBT_MIN_NUTRITION  = "MinNutrition";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final int count;
    private final int minNutrition;

    private final List<ItemStorage> exclusionList = new ArrayList<>();

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Food(final int count, final int minNutrition)
    {
        this.count = count;
        this.minNutrition = minNutrition;
    }

    public Food(final int count, @NotNull final ItemStack result, final int minNutrition)
    {
        this.count = count;
        this.result = result;
        this.minNutrition = minNutrition;
    }

    public Food(final int count, @NotNull final ItemStack result, List<ItemStorage> exclusionList, final int minNutrition)
    {
        this.count = count;
        this.result = result;
        this.exclusionList.addAll(exclusionList);
        this.minNutrition = minNutrition;
    }

    public Food(final int count, final List<ItemStorage> exclusionList, final int minNutrition)
    {
        this(count, ItemStackUtils.EMPTY, exclusionList, minNutrition);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param food       the input.
     * @return the compound.
     */
    public static CompoundTag serialize(@NotNull final HolderLookup.Provider provider, final IFactoryController controller, final Food food)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(NBT_COUNT, food.count);

        if (!ItemStackUtils.isEmpty(food.result))
        {
            compound.put(NBT_RESULT, food.result.save(provider));
        }
        if (!food.exclusionList.isEmpty())
        {
            @NotNull final ListTag items = new ListTag();
            for (@NotNull final ItemStorage item : food.exclusionList)
            {
                @NotNull final Tag itemCompound = item.getItemStack().save(provider);
                items.add(itemCompound);
            }
            compound.put(NBT_EXCLUSION, items);
        }
        compound.putInt(NBT_MIN_NUTRITION, food.minNutrition);
        return compound;
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static Food deserialize(@NotNull final HolderLookup.Provider provider, final IFactoryController controller, final CompoundTag compound)
    {
        final int count = compound.getInt(NBT_COUNT);
        final ItemStack result = compound.contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT), provider) : ItemStackUtils.EMPTY;
        final List<ItemStorage> items = new ArrayList<>();

        if (compound.contains(NBT_EXCLUSION))
        {
            final ListTag filterableItems = compound.getList(NBT_EXCLUSION, Tag.TAG_COMPOUND);
            for (int i = 0; i < filterableItems.size(); ++i)
            {
                items.add(new ItemStorage(ItemStack.parseOptional(provider, filterableItems.getCompound(i))));
            }
        }
        final int minNutrition = compound.getInt(NBT_MIN_NUTRITION);
        return new Food(count, result, items, minNutrition);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final RegistryFriendlyByteBuf buffer, final Food input)
    {
        buffer.writeInt(input.count);

        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            Utils.serializeCodecMess(buffer, input.result);
        }

        buffer.writeInt(input.exclusionList.size());
        for (ItemStorage item : input.exclusionList)
        {
            Utils.serializeCodecMess(buffer, item.getItemStack());
        }
        buffer.writeInt(input.minNutrition);
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Food deserialize(final IFactoryController controller, final RegistryFriendlyByteBuf buffer)
    {
        final int count = buffer.readInt();
        final ItemStack result = buffer.readBoolean() ? Utils.deserializeCodecMess(buffer) : ItemStack.EMPTY;

        List<ItemStorage> items = new ArrayList<>();
        final int itemsCount = buffer.readInt();
        for (int i = 0; i < itemsCount; ++i)
        {
            items.add(new ItemStorage(Utils.deserializeCodecMess(buffer)));
        }
        final int minNutrition = buffer.readInt();
        if (!items.isEmpty())
        {
            return new Food(count, result, items, minNutrition);
        }
        return new Food(count, result, minNutrition);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return ItemStackUtils.ISFOOD.test(stack)
                 && !exclusionList.contains(new ItemStorage(stack))
                 && !(ItemStackUtils.ISCOOKABLE.test(stack) && exclusionList.contains(new ItemStorage(MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack))))
                 && (ItemStackUtils.ISCOOKABLE.test(stack) || stack.getItem().getFoodProperties(stack, null).nutrition() >= minNutrition);
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new Food(newCount, exclusionList, minNutrition);
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

    @Override
    public boolean canBeResolvedByBuilding()
    {
        return false;
    }
}
