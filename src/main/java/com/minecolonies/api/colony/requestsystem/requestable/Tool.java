package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.tools.ModToolTypes;
import com.minecolonies.api.tools.registry.ToolTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class used to represent tools inside the request system.
 */
public class Tool implements IDeliverable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>> TYPE_TOKENS =
      ReflectionUtils.getSuperClasses(TypeToken.of(Tool.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TYPE      = "Type";
    private static final String NBT_MIN_LEVEL = "MinLevel";
    private static final String NBT_MAX_LEVEL = "MaxLevel";
    private static final String NBT_RESULT    = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ToolTypeEntry toolClass;

    @NotNull
    private final Integer minLevel;

    @NotNull
    private final Integer maxLevel;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Tool(@NotNull final ToolTypeEntry toolClass, @NotNull final Integer minLevel, @NotNull final Integer maxLevel)
    {
        this(toolClass, minLevel, maxLevel, ItemStackUtils.EMPTY);
    }

    public Tool(@NotNull final ToolTypeEntry toolClass, @NotNull final Integer minLevel, @NotNull final Integer maxLevel, @NotNull final ItemStack result)
    {
        this.toolClass = toolClass;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.result = result;
    }

    /**
     * Serializes this Tool into NBT.
     *
     * @param controller The IFactoryController used to serialize sub types.
     * @param tool       the tool to serialize.
     * @return The CompoundTag containing the tool data.
     */
    @NotNull
    public static CompoundTag serialize(final IFactoryController controller, final Tool tool)
    {
        final CompoundTag compound = new CompoundTag();

        compound.putString(NBT_TYPE, tool.getToolClass().getRegistryName().toString());
        compound.putInt(NBT_MIN_LEVEL, tool.getMinLevel());
        compound.putInt(NBT_MAX_LEVEL, tool.getMaxLevel());
        compound.put(NBT_RESULT, tool.getResult().serializeNBT());

        return compound;
    }

    /**
     * Returns the tool class that is requested.
     *
     * @return The tool class that is requested.
     */
    @NotNull
    public ToolTypeEntry getToolClass()
    {
        return toolClass;
    }

    /**
     * The minimal tool level requested.
     *
     * @return The minimal tool level requested.
     */
    @NotNull
    public Integer getMinLevel()
    {
        return minLevel;
    }

    /**
     * The maximum tool level requested.
     *
     * @return The maximum tool level requested.
     */
    @NotNull
    public Integer getMaxLevel()
    {
        return maxLevel;
    }

    /**
     * Static method that constructs an instance from NBT.
     *
     * @param controller The {@link IFactoryController} to deserialize components with.
     * @param nbt        The nbt to serialize from.
     * @return An instance of Tool with the data contained in the given NBT.
     */
    @NotNull
    public static Tool deserialize(final IFactoryController controller, final CompoundTag nbt)
    {
        //API:Map the given strings a proper way.
        String resLoc = nbt.getString(NBT_TYPE);
        final ToolTypeEntry type = ModToolTypes.getRegistry().getValue(ToolTypeEntry.parseResourceLocation(resLoc));
        final Integer minLevel = nbt.getInt(NBT_MIN_LEVEL);
        final Integer maxLevel = nbt.getInt(NBT_MAX_LEVEL);
        final ItemStack result = ItemStack.of(nbt.getCompound(NBT_RESULT));

        return new Tool(type, minLevel, maxLevel, result);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final Tool input)
    {
        buffer.writeResourceLocation(input.getToolClass().getRegistryName());
        buffer.writeInt(input.getMinLevel());
        buffer.writeInt(input.getMaxLevel());
        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            buffer.writeItem(input.result);
        }
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Tool deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        ResourceLocation resLoc = ToolTypeEntry.parseResourceLocation(buffer.readResourceLocation());
        final ToolTypeEntry type = ModToolTypes.getRegistry().getValue(resLoc);
        final int minLevel = buffer.readInt();
        final int maxLevel = buffer.readInt();
        final ItemStack result = buffer.readBoolean() ? buffer.readItem() : ItemStack.EMPTY;

        return new Tool(type, minLevel, maxLevel, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        return ItemStackUtils.hasToolLevel(stack, getToolClass(), getMinLevel(), getMaxLevel());
    }

    @Override
    public int getCount()
    {
        return 1;
    }

    @Override
    public int getMinimumCount()
    {
        return 1;
    }

    /**
     * The resulting stack if set during creation, else ItemStack.Empty.
     *
     * @return The resulting stack.
     */
    @NotNull
    public ItemStack getResult()
    {
        return result;
    }

    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    @Override
    public IDeliverable copyWithCount(final int newCount)
    {
        return new Tool(this.toolClass, this.minLevel, this.maxLevel, this.result);
    }

    /**
     * Check if the tool is armor.
     *
     * @return true if so.
     */
    public boolean isArmor()
    {
        return toolClass == ModToolTypes.helmet.get() || toolClass == ModToolTypes.leggings.get() || toolClass == ModToolTypes.chestplate.get()
                 || toolClass == ModToolTypes.boots.get();
    }

    @Override
    public int hashCode()
    {
        int result1 = getToolClass().hashCode();
        result1 = 31 * result1 + getMinLevel().hashCode();
        result1 = 31 * result1 + getMaxLevel().hashCode();
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final Tool tool))
        {
            return false;
        }

        if (!getToolClass().equals(tool.getToolClass()))
        {
            return false;
        }
        if (!getMinLevel().equals(tool.getMinLevel()))
        {
            return false;
        }
        if (!getMaxLevel().equals(tool.getMaxLevel()))
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getResult(), tool.getResult());
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
