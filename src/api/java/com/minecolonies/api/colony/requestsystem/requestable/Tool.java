package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Class used to represent tools inside the request system.
 */
public class Tool implements IDeliverable
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TYPE      = "Type";
    private static final String NBT_MIN_LEVEL = "MinLevel";
    private static final String NBT_MAX_LEVEL = "MaxLevel";
    private static final String NBT_RESULT    = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final IToolType toolClass;

    @NotNull
    private final Integer minLevel;

    @NotNull
    private final Integer maxLevel;

    @NotNull
    private ItemStack result;

    public Tool(@NotNull final IToolType toolClass, @NotNull final Integer minLevel, @NotNull final Integer maxLevel)
    {
        this(toolClass, minLevel, maxLevel, ItemStackUtils.EMPTY);
    }

    public Tool(@NotNull final IToolType toolClass, @NotNull final Integer minLevel, @NotNull final Integer maxLevel, final ItemStack result)
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
     * @return The NBTTagCompound containing the tool data.
     */
    @NotNull
    public static NBTTagCompound serialize(IFactoryController controller, Tool tool)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString(NBT_TYPE, tool.getToolClass().getName());
        compound.setInteger(NBT_MIN_LEVEL, tool.getMinLevel());
        compound.setInteger(NBT_MAX_LEVEL, tool.getMaxLevel());

        if(tool.getResult() != null)
        {
            compound.setTag(NBT_RESULT, tool.getResult().serializeNBT());
        }
        return compound;
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
    }    /**
     * The resulting stack if set during creation, else ItemStack.Empty.
     *
     * @return The resulting stack.
     */
    public ItemStack getResult()
    {
        return result;
    }

    /**
     * Static method that constructs an instance from NBT.
     *
     * @param controller The {@link IFactoryController} to deserialize components with.
     * @param nbt        The nbt to serialize from.
     * @return An instance of Tool with the data contained in the given NBT.
     */
    @NotNull
    public static Tool deserialize(IFactoryController controller, NBTTagCompound nbt)
    {
        //API:Map the given strings a proper way.
        IToolType type = ToolType.getToolType(nbt.getString(NBT_TYPE));
        Integer minLevel = nbt.getInteger(NBT_MIN_LEVEL);
        Integer maxLevel = nbt.getInteger(NBT_MAX_LEVEL);

        ItemStack result;
        if(nbt.hasKey(NBT_RESULT))
        {
            result = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(NBT_RESULT));
        }
        else
        {
            result = null;
        }

        return new Tool(type, minLevel, maxLevel, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        //API:Map the given strings a proper way.
        boolean toolTypeResult = !ItemStackUtils.isEmpty(stack)
                                   && stack.stackSize >= 1
                                   && getToolClasses(stack).stream()
                                        .filter(s -> getToolClass().getName().equalsIgnoreCase(s))
                                        .map(ToolType::getToolType)
                                        .filter(t -> t != ToolType.NONE)
                                        .anyMatch(t -> ItemStackUtils.hasToolLevel(stack, t, getMinLevel(), getMaxLevel()));

        if (!toolTypeResult)
        {
            return stack.getItem() instanceof ItemHoe && toolClass.equals(ToolType.HOE);
        }

        return toolTypeResult;
    }

    private Set<String> getToolClasses(final ItemStack stack)
    {
        final Set set = new HashSet();

        if(ItemStackUtils.isEmpty(stack))
        {
            return set;
        }

        set.addAll(stack.getItem().getToolClasses(stack));

        if(stack.getItem() instanceof ItemBow)
        {
            set.add("bow");
        }
        else if(stack.getItem() instanceof ItemSword || Compatibility.isTinkersWeapon(stack))
        {
            set.add("weapon");
        }
        else if(stack.getItem() instanceof ItemHoe)
        {
            set.add("hoe");
        }
        else if(stack.getItem() instanceof ItemFishingRod)
        {
            set.add("rod");
        }
        else if(stack.getItem() instanceof  ItemShears)
        {
            set.add("shears");
        }
        return set;
    }

    @Override
    public int getCount()
    {
        return 1;
    }



    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }
}
