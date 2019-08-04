package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
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
    private ItemStack result = ItemStackUtils.EMPTY;

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
     * Serializes this Tool into NBT.
     *
     * @param controller The IFactoryController used to serialize sub types.
     * @return The CompoundNBT containing the tool data.
     */
    @NotNull
    public static CompoundNBT serialize(final IFactoryController controller, final Tool tool)
    {
        final CompoundNBT compound = new CompoundNBT();

        compound.putString(NBT_TYPE, tool.getToolClass().getName());
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
    @NotNull
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
    public static Tool deserialize(final IFactoryController controller, final CompoundNBT nbt)
    {
        //API:Map the given strings a proper way.
        final IToolType type = ToolType.getToolType(nbt.getString(NBT_TYPE));
        final Integer minLevel = nbt.getInt(NBT_MIN_LEVEL);
        final Integer maxLevel = nbt.getInt(NBT_MAX_LEVEL);
        final ItemStack result = new ItemStack(nbt.getCompound(NBT_RESULT));

        return new Tool(type, minLevel, maxLevel, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        //API:Map the given strings a proper way.
        final boolean toolTypeResult = !ItemStackUtils.isEmpty(stack)
                && stack.getCount() >= 1
                && getToolClasses(stack).stream()
                .filter(s -> getToolClass().getName().equalsIgnoreCase(s))
                .map(ToolType::getToolType)
                .filter(t -> t != ToolType.NONE)
                .anyMatch(t -> ItemStackUtils.hasToolLevel(stack, t, getMinLevel(), getMaxLevel()));

        if (!toolTypeResult)
        {
            return stack.getItem() instanceof ItemHoe && toolClass.equals(ToolType.HOE) || stack.getItem() instanceof ItemShield && toolClass.equals(ToolType.SHIELD);
        }

        return toolTypeResult;
    }

    private Set<String> getToolClasses(final ItemStack stack)
    {
        final Set<String> set = new HashSet<>();

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
        else if(stack.getItem() instanceof  ItemShield)
        {
            set.add("shield");
        }
        else if(stack.getItem() instanceof ItemArmor)
        {
            /*
             * There is no armor class for each type of armor.
             * So what we need to do is check the equipment Slot of this
             * armor to send back what type of armor this if for the request
             * system.
             */
            final ItemArmor armor = (ItemArmor) stack.getItem();
            if (armor.armorType == EquipmentSlotType.CHEST)
            {
                set.add("chestplate");
            }
            else if (armor.armorType == EquipmentSlotType.FEET)
            {
                set.add("boots");
            }
            else if (armor.armorType == EquipmentSlotType.HEAD)
            {
                set.add("helmet");
            }
            else if (armor.armorType == EquipmentSlotType.LEGS)
            {
                set.add("leggings");
            }
        }
        return set;
    }

    /**
     * Check if the tool is armor.
     * @return true if so.
     */
    public boolean isArmor()
    {
        return toolClass == ToolType.HELMET || toolClass == ToolType.LEGGINGS || toolClass == ToolType.CHESTPLATE || toolClass == ToolType.BOOTS;
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

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Tool))
        {
            return false;
        }

        final Tool tool = (Tool) o;

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
    public int hashCode()
    {
        int result1 = getToolClass().hashCode();
        result1 = 31 * result1 + getMinLevel().hashCode();
        result1 = 31 * result1 + getMaxLevel().hashCode();
        result1 = 31 * result1 + getResult().hashCode();
        return result1;
    }
}
