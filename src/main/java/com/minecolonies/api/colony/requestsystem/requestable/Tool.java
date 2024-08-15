package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
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
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Tool.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

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
     * @param tool       the tool to serialize.
     * @return The CompoundTag containing the tool data.
     */
    @NotNull
    public static CompoundTag serialize(@NotNull final HolderLookup.Provider provider, final IFactoryController controller, final Tool tool)
    {
        final CompoundTag compound = new CompoundTag();

        compound.putString(NBT_TYPE, tool.getToolClass().getName());
        compound.putInt(NBT_MIN_LEVEL, tool.getMinLevel());
        compound.putInt(NBT_MAX_LEVEL, tool.getMaxLevel());
        compound.put(NBT_RESULT, tool.getResult().saveOptional(provider));

        return compound;
    }

    /**
     * Static method that constructs an instance from NBT.
     *
     * @param controller The {@link IFactoryController} to deserialize components with.
     * @param nbt        The nbt to serialize from.
     * @return An instance of Tool with the data contained in the given NBT.
     */
    @NotNull
    public static Tool deserialize(@NotNull final HolderLookup.Provider provider, final IFactoryController controller, final CompoundTag nbt)
    {
        //API:Map the given strings a proper way.
        final IToolType type = ToolType.getToolType(nbt.getString(NBT_TYPE));
        final Integer minLevel = nbt.getInt(NBT_MIN_LEVEL);
        final Integer maxLevel = nbt.getInt(NBT_MAX_LEVEL);
        final ItemStack result = ItemStack.parseOptional(provider, nbt.getCompound(NBT_RESULT));

        return new Tool(type, minLevel, maxLevel, result);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final RegistryFriendlyByteBuf buffer, final Tool input)
    {
        buffer.writeUtf(input.getToolClass().getName());
        buffer.writeInt(input.getMinLevel());
        buffer.writeInt(input.getMaxLevel());
        buffer.writeBoolean(!ItemStackUtils.isEmpty(input.result));
        if (!ItemStackUtils.isEmpty(input.result))
        {
            Utils.serializeCodecMess(buffer, input.result);
        }
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Tool deserialize(final IFactoryController controller, final RegistryFriendlyByteBuf buffer)
    {
        final IToolType type = ToolType.getToolType(buffer.readUtf(32767));
        final int minLevel = buffer.readInt();
        final int maxLevel = buffer.readInt();
        final ItemStack result = buffer.readBoolean() ? Utils.deserializeCodecMess(buffer) : ItemStack.EMPTY;

        return new Tool(type, minLevel, maxLevel, result);
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

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        //API:Map the given strings a proper way.
        final boolean toolTypeResult = !ItemStackUtils.isEmpty(stack)
                                         && stack.getCount() >= 1
                                         && getToolClasses(stack).stream()
                                              .filter(s -> getToolClass().getName().equalsIgnoreCase(s))
                                              .map(ToolType::getToolType)
                                              .anyMatch(t -> t != ToolType.NONE && (stack.getDamageValue() > 0 || !stack.isDamaged()) && ItemStackUtils.hasToolLevel(stack,
                                                t,
                                                getMinLevel(),
                                                getMaxLevel()));

        if (!toolTypeResult)
        {
            return (stack.getItem() instanceof ShieldItem && toolClass.equals(ToolType.SHIELD))
                     || (stack.getItem() instanceof FlintAndSteelItem && toolClass.equals(ToolType.FLINT_N_STEEL));
        }

        return toolTypeResult;
    }

    private Set<String> getToolClasses(final ItemStack stack)
    {
        final Set<String> set = new HashSet<>();

        if (ItemStackUtils.isEmpty(stack))
        {
            return set;
        }

        if (stack.canPerformAction(ItemAbilities.AXE_DIG))
        {
            set.add(ToolType.AXE.getName());
        }

        if (stack.canPerformAction(ItemAbilities.PICKAXE_DIG))
        {
            set.add(ToolType.PICKAXE.getName());
        }

        if (stack.canPerformAction(ItemAbilities.SHOVEL_DIG))
        {
            set.add(ToolType.SHOVEL.getName());
        }

        if (stack.canPerformAction(ItemAbilities.HOE_DIG))
        {
            set.add(ToolType.HOE.getName());
        }

        if (stack.canPerformAction(ItemAbilities.SWORD_SWEEP))
        {
            set.add(ToolType.SWORD.getName());
        }

        if (stack.canPerformAction(ItemAbilities.SHEARS_DIG))
        {
            set.add(ToolType.SHEARS.getName());
        }

        if (stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST))
        {
            set.add(ToolType.FISHINGROD.getName());
        }

        if (stack.getItem() instanceof BowItem)
        {
            set.add("bow");
        }
        else if (stack.getItem() instanceof SwordItem || Compatibility.isTinkersWeapon(stack))
        {
            set.add("weapon");
        }
        else if (stack.getItem() instanceof ShieldItem)
        {
            set.add("shield");
        }
        else if (stack.getItem() instanceof ArmorItem)
        {
            /*
             * There is no armor class for each type of armor.
             * So what we need to do is check the equipment Slot of this
             * armor to send back what type of armor this if for the request
             * system.
             */
            final ArmorItem armor = (ArmorItem) stack.getItem();
            if (armor.getEquipmentSlot() == EquipmentSlot.CHEST)
            {
                set.add("chestplate");
            }
            else if (armor.getEquipmentSlot() == EquipmentSlot.FEET)
            {
                set.add("boots");
            }
            else if (armor.getEquipmentSlot() == EquipmentSlot.HEAD)
            {
                set.add("helmet");
            }
            else if (armor.getEquipmentSlot() == EquipmentSlot.LEGS)
            {
                set.add("leggings");
            }
        }
        return set;
    }

    /**
     * Check if the tool is armor.
     *
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
    public int getMinimumCount()
    {
        return 1;
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

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
