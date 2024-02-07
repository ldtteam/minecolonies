package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deliverable that can only be fulfilled by a stack whos item is contained in a given tag with a given minimal amount of items.
 */
public class RequestTag implements IDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>> TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(RequestTag.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TAG      = "Tag";
    private static final String NBT_RESULT   = "Result";
    private static final String NBT_COUNT    = "Count";
    private static final String NBT_MINCOUNT = "MinCount";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * The tag.
     */
    @NotNull
    private final TagKey<Item> theTag;

    /**
     * The result of the request.
     */
    @NotNull
    private ItemStack result;

    /**
     * The required count.
     */
    private int count;

    /**
     * The required count.
     */
    private int minCount;

    /**
     * Create a Tag deliverable.
     *
     * @param tag   the required containing tag.
     * @param count the count.
     */
    public RequestTag(@NotNull final TagKey<Item> tag, final int count)
    {
        this(tag, count, count);
    }

    /**
     * Create a Tag deliverable.
     *
     * @param tag      the required containing tag.
     * @param count    the count.
     * @param minCount the min count.
     */
    public RequestTag(@NotNull final TagKey<Item> tag, final int count, final int minCount)
    {
        this(tag, ItemStackUtils.EMPTY, count, minCount);
    }

    /**
     * Create a Tag deliverable.
     *
     * @param tag      the required containing tag.
     * @param result   the result stack.
     * @param count    the count.
     * @param minCount the min count.
     */
    public RequestTag(@NotNull final TagKey<Item> tag, @NotNull final ItemStack result, final int count, final int minCount)
    {
        this.theTag = tag;
        this.result = result;
        this.count = count;
        this.minCount = minCount;
    }

    /**
     * Method called to check if a given stack matches this deliverable. The first stack that returns true from this method is returned as a Deliverable.
     *
     * @param stack The stack to test.
     * @return true when the stack matches. False when not.
     */
    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        return stack.is(theTag);
    }

    /**
     * Method called to get the amount of items that need to be in the stack.
     *
     * @return The amount of items.
     */
    @Override
    public int getCount()
    {
        return count;
    }

    /**
     * Method called to get the minimum amount required to fulfill this request.
     *
     * @return the minimum amount.
     */
    @Override
    public int getMinimumCount()
    {
        return minCount;
    }

    /**
     * Method to get the result of the delivery.
     *
     * @return The result of the delivery.
     */
    @NotNull
    @Override
    public ItemStack getResult()
    {
        return result;
    }

    @NotNull
    public TagKey<Item> getTag()
    {
        return theTag;
    }

    /**
     * Method to set the result of a delivery.
     *
     * @param result The result of the delivery.
     */
    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    /**
     * Creates a new instance of this requestable with the given count.
     *
     * @param newCount The new requestable, with the requested count.
     * @return the deliverable.
     */
    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new RequestTag(theTag, result, newCount, minCount);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final RequestTag tag1 = (RequestTag) o;
        return getCount() == tag1.getCount() &&
                 getMinimumCount() == tag1.getMinimumCount() &&
                 getTag().equals(tag1.getTag()) &&
                 getResult().equals(tag1.getResult());
    }

    @Override
    public int hashCode()
    {
        int result1 = theTag.location().toString().hashCode();
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param input      the input.
     * @return the compound.
     */
    public static CompoundTag serialize(final IFactoryController controller, final RequestTag input)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putString(NBT_TAG, input.getTag().location().toString());
        if (!ItemStackUtils.isEmpty(input.getResult()))
        {
            compound.put(NBT_RESULT, input.getResult().save(new CompoundTag()));
        }
        compound.putInt(NBT_COUNT, input.getCount());
        compound.putInt(NBT_MINCOUNT, input.getMinimumCount());
        return compound;
    }

    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final RequestTag input)
    {
        buffer.writeResourceLocation(input.getTag().location());
        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.getResult()));

        if (!ItemStackUtils.isEmpty(input.getResult()))
        {
            buffer.writeItem(input.getResult());
        }
        buffer.writeInt(input.getCount());
        buffer.writeInt(input.getMinimumCount());
    }

    public static RequestTag deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final TagKey<Item> theTag = ItemTags.create(buffer.readResourceLocation());
        final ItemStack result = buffer.readBoolean() ? buffer.readItem() : ItemStack.EMPTY;
        final int count = buffer.readInt();
        final int minCount = buffer.readInt();

        return new RequestTag(theTag, result, count, minCount);
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param compound   the compound.
     * @return the deliverable.
     */
    public static RequestTag deserialize(final IFactoryController controller, final CompoundTag compound)
    {
        final TagKey<Item> theTag = ItemTags.create(new ResourceLocation(compound.getString(NBT_TAG)));
        final ItemStack result = compound.contains(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_RESULT)) : ItemStackUtils.EMPTY;

        int count = compound.getInt("size");
        int minCount = count;
        if (compound.contains(NBT_COUNT))
        {
            count = compound.getInt(NBT_COUNT);
            minCount = compound.getInt(NBT_MINCOUNT);
        }
        return new RequestTag(theTag, result, count, minCount);
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
