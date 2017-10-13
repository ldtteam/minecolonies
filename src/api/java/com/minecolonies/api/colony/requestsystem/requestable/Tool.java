package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent tools inside the request system.
 */
public class Tool
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TYPE  = "Type";
    private static final String NBT_MIN_LEVEL = "MinLevel";
    private static final String NBT_MAX_LEVEL  = "MaxLevel";
    private static final String NBT_RESULT = "Stack";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final IToolType toolClass;

    @NotNull
    private final Integer minLevel;

    @NotNull
    private final Integer maxLevel;

    @NotNull
    private final ItemStack result;

    public Tool(@NotNull final IToolType toolClass, @NotNull final Integer minLevel, @NotNull final Integer maxLevel)
    {
        this(toolClass, minLevel, maxLevel, ItemStackUtils.EMPTY);
    }

    public Tool(@NotNull final IToolType toolClass, @NotNull final Integer minLevel, @NotNull final Integer maxLevel, @NotNull final ItemStack result)
    {
        this.toolClass = toolClass;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.result = result;
    }

    /**
     * Returns the tool class that is requested.
     *
     * @return The tool class that is requested.
     */
    @NotNull
    public IToolType getToolClass()
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
     * The resulting stack if set during creation, else ItemStack.Empty.
     *
     * @return The resulting stack.
     */
    @NotNull
    public ItemStack getResult()
    {
        return result;
    }

    /**
     * Serializes this Tool into NBT.
     *
     * @param controller The IFactoryController used to serialize sub types.
     * @return The NBTTagCompound containing the tool data.
     */
    @NotNull
    public NBTTagCompound serialize(IFactoryController controller) {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString(NBT_TYPE, getToolClass().getName());
        compound.setInteger(NBT_MIN_LEVEL, getMinLevel());
        compound.setInteger(NBT_MAX_LEVEL, getMaxLevel());
        compound.setTag(NBT_RESULT, getResult().serializeNBT());

        return compound;
    }

    /**
     * Static method that constructs an instance from NBT.
     *
     * @param controller The {@link IFactoryController} to deserialize components with.
     * @param nbt The nbt to serialize from.
     * @return An instance of Tool with the data contained in the given NBT.
     */
    @NotNull
    public static Tool deserialize(IFactoryController controller, NBTTagCompound nbt)
    {
        //TODO: Make this universal when an API exists.
        IToolType type = ToolType.getToolType(nbt.getString(NBT_TYPE));
        Integer minLevel = nbt.getInteger(NBT_MIN_LEVEL);
        Integer maxLevel = nbt.getInteger(NBT_MAX_LEVEL);
        ItemStack result = new ItemStack(nbt.getCompoundTag(NBT_RESULT));

        return new Tool(type, minLevel, maxLevel, result);
    }

}
