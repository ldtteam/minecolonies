package com.minecolonies.api.util;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.ToolLevelConstants.*;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    /**
     * Variable representing the empty itemstack in 1.10.
     * Used for easy updating to 1.11
     */
    public static final ItemStack EMPTY = null;

    /**
     * Predicate to check if an itemStack is empty.
     */
    @NotNull
    public static final Predicate<ItemStack> EMPTY_PREDICATE = ItemStackUtils::isEmpty;

    /**
     * Negation of the itemStack empty predicate (not empty).
     */
    @NotNull
    public static final Predicate<ItemStack> NOT_EMPTY_PREDICATE = EMPTY_PREDICATE.negate();

    /**
     * The compound tag for fortune enchantment id.
     */
    private static final String NBT_TAG_ENCHANT_ID = "id";

    /**
     * The compound tag for fortune enchantment level.
    */
    private static final String NBT_TAG_ENCHANT_LEVEL = "lvl";

    /**
     * The compound id for fortune enchantment.
     */
    private static final int FORTUNE_ENCHANT_ID = 35;

    /**
     * material for wood.
     */
    private static final String MATERIAL_WOOD    = "WOOD";

    /**
     * material for gold.
     */
    private static final String MATERIAL_GOLD    = "GOLD";

    /**
     * material for stone.
     */
    private static final String MATERIAL_STONE   = "STONE";

    /**
     * material for iron.
     */
    private static final String MATERIAL_IRON    = "IRON";

    /**
     * material for diamond.
     */
    private static final String MATERIAL_DIAMOND = "DIAMOND";

    /**
     * Private constructor to hide the implicit one.
     */
    private ItemStackUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * create a ItemStack from a compound
     *
     * @param compound with wich we create the ItemStack
     * @return the newly created ItemStack
     */
    public static ItemStack loadItemStackFromNBT(final NBTTagCompound compound)
    {
        return ItemStack.loadItemStackFromNBT(compound);
    }

    /**
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param stack        the stack to test.
     * @param toolType     the type of tool needed
     * @param minimalLevel the minimum level for the tool to find.
     * @param maximumLevel the maximum level for the tool to find.
     * @return true if tool is acceptable
     */
    public static boolean hasToolLevel(@Nullable final ItemStack stack, final IToolType toolType, final int minimalLevel, final int maximumLevel)
    {
        if (isEmpty(stack))
        {
            return false;
        }

        final int level = Compatibility.isTinkersWeapon(stack) ?  Compatibility.getToolLevel(stack) : getMiningLevel(stack, toolType);
        return isTool(stack, toolType) && verifyToolLevel(stack, level, minimalLevel, maximumLevel);
    }

    /**
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    public static Boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == EMPTY || stack.getItem() == null || stack.stackSize <= 0;
    }

    /**
     * Checks if this ItemStack can be used as a Tool of type.
     *
     * @param itemStack Item to check.
     * @param toolType  Type of the tool.
     * @return true if item can be used, otherwise false.
     */
    public static boolean isTool(@Nullable final ItemStack itemStack, final IToolType toolType)
    {
        if (isEmpty(itemStack))
        {
            return false;
        }

        boolean isATool = false;
        if (ToolType.AXE.equals(toolType) || ToolType.SHOVEL.equals(toolType) || ToolType.PICKAXE.equals(toolType))
        {
            isATool = getMiningLevel(itemStack, toolType) >= 0;
        }
        else if (ToolType.HOE.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemHoe;
        }
        else if (ToolType.BOW.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemBow;
        }
        else if (ToolType.SWORD.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemSword || Compatibility.isTinkersWeapon(itemStack);
        }
        else if (ToolType.FISHINGROD.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemFishingRod;
        }
        return isATool;
    }


    /**
     * Calculate the mining level an item has as a tool of certain type.
     * Suppressing Sonar Rule squid:S1142
     * This rule does "Methods should not have too many return statements"
     * I could limit the number of return by using a variable and only one return
     * but it will not make the code clearer
     *
     * @param stack    the stack to test.
     * @param toolType the tool category.
     * @return integer value for mining level &gt;= 0 is okay.
     */
    @SuppressWarnings({"deprecation", "squid:S1142"})
    public static int getMiningLevel(@Nullable final ItemStack stack, @Nullable final IToolType toolType)
    {
        if (toolType == ToolType.NONE)
        {
            //empty hand is best on blocks who don't care (0 better 1)
            return stack == null ? 0 : 1;
        }
        if (isEmpty(stack))
        {
            return -1;
        }
        if (!Compatibility.getMiningLevelCompatibility(stack, toolType.toString()))
        {
            return -1;
        }
        //todo: use 'better' version of this thing
        if (ToolType.HOE.equals(toolType))
        {
            if (stack.getItem() instanceof ItemHoe)
            {
                final ItemHoe itemHoe = (ItemHoe)stack.getItem();
                return getToolLevel(itemHoe.getMaterialName());
            }
        }
        else if (ToolType.SWORD.equals(toolType))
        {
            if (stack.getItem() instanceof ItemSword)
            {
                final ItemSword itemSword = (ItemSword)stack.getItem();
                return getToolLevel(itemSword.getToolMaterialName());
            }
        }
        else if (!toolType.hasVariableMaterials())
        {
            //We need a hut level 1 minimum
            return 1;
        }
        else
        {
            return stack.getItem().getHarvestLevel(stack, toolType.getName(), null, null);
        }
        return -1;
    }


    /**
     * Verifies if an item has an appropriated grade.
     *
     * @param itemStack    the type of tool needed
     * @param toolLevel    the tool level
     * @param minimalLevel the minimum level needed
     * @param maximumLevel the maximum level needed (usually the worker's hut level)
     * @return true if tool is acceptable
     */
    public static boolean verifyToolLevel(@NotNull final ItemStack itemStack, final int toolLevel, final int minimalLevel, final int maximumLevel)
    {
        if (toolLevel < minimalLevel)
        {
            return false;
        }
        return (toolLevel + getMaxEnchantmentLevel(itemStack) <= maximumLevel);
    }

    /**
     * Calculates the fortune level this tool has.
     *
     * @param tool the tool to check.
     * @return fortune level.
     */
    public static int getFortuneOf(@Nullable final ItemStack tool)
    {
        if (tool == null)
        {
            return 0;
        }
        //calculate fortune enchantment
        int fortune = 0;
        if (tool.isItemEnchanted())
        {
            final NBTTagList t = tool.getEnchantmentTagList();

            for (int i = 0; i < t.tagCount(); i++)
            {
                final int id = t.getCompoundTagAt(i).getShort(NBT_TAG_ENCHANT_ID);
                if (id == FORTUNE_ENCHANT_ID)
                {
                    fortune = t.getCompoundTagAt(i).getShort(NBT_TAG_ENCHANT_LEVEL);
                }
            }
        }
        return fortune;
    }

    /**
     * Calculates the max level enchantment this tool has.
     *
     * @param itemStack the tool to check.
     * @return max enchantment level.
     */
    public static int getMaxEnchantmentLevel(final ItemStack itemStack)
    {
        if (isEmpty(itemStack))
        {
            return 0;
        }
        int maxLevel = 0;
        final NBTTagList nbttaglist = itemStack.getEnchantmentTagList();

        if (nbttaglist != null)
        {
            for (int j = 0; j < nbttaglist.tagCount(); ++j)
            {
                final int level = nbttaglist.getCompoundTagAt(j).getShort("lvl");
                maxLevel = level > maxLevel ? level : maxLevel;
            }
        }
        return maxLevel;
    }


    /**
     * Checks if an item serves as a weapon.
     *
     * @param stack the stack to analyze.
     * @return true if it is a tool or sword.
     */
    public static boolean doesItemServeAsWeapon(@NotNull final ItemStack stack)
    {
        return stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool || Compatibility.isTinkersWeapon(stack);
    }


    /**
     * Assigns a string containing the grade of the toolGrade.
     *
     * @param toolGrade the number of the grade of a tool
     * @return a string corresponding to the tool
     */
    public static String swapToolGrade(final int toolGrade)
    {
        switch (toolGrade)
        {
            case TOOL_LEVEL_WOOD_OR_GOLD:
                return "Wood or Gold";
            case TOOL_LEVEL_STONE:
                return "Stone";
            case TOOL_LEVEL_IRON:
                return "Iron";
            case TOOL_LEVEL_DIAMOND:
                return "Diamond";
            default:
                return "Better than Diamond";
        }
    }

    private static int getToolLevel(final String material)
    {
        if (MATERIAL_WOOD.equals(material)
            || MATERIAL_GOLD.equals(material))
        {
            return TOOL_LEVEL_WOOD_OR_GOLD;
        }
        else if (MATERIAL_STONE.equals(material))
        {
            return TOOL_LEVEL_STONE;
        }
        else if (MATERIAL_IRON.equals(material))
        {
            return TOOL_LEVEL_IRON;
        }
        else if (MATERIAL_DIAMOND.equals(material))
        {
            return TOOL_LEVEL_DIAMOND;
        }
        return -1;
    }

    /**
     * Method to check if two ItemStacks can be merged together.
     *
     * @param existingStack The existing stack.
     * @param mergingStack  The merging stack
     * @return True when they can be merged, false when not.
     */
    @NotNull
    public static Boolean areItemStacksMergable(final ItemStack existingStack, final ItemStack mergingStack)
    {
        if (!compareItemStacksIgnoreStackSize(existingStack, mergingStack))
        {
            return false;
        }

        return existingStack.getMaxStackSize() >= (getSize(existingStack) + getSize(mergingStack));
    }

    /**
     * get the size of the stack.
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    @NotNull
    public static int getSize(final ItemStack stack)
    {
        if (isEmpty(stack))
        {
            return 0;
        }

        return stack.stackSize;
    }

    /**
     * set the size of the stack.
     *
     * @param stack to set the size to
     * @param size set to the stack
     */
    public static void setSize(@NotNull final ItemStack stack, final int size)
    {
        stack.stackSize = size;
    }

    /**
     * Increase or decrease the stack size.
     *
     * @param stack to set the size to
     * @param amount to increase the stack's size of (negative value to decrease)
     */
    public static void changeSize(@NotNull final ItemStack stack, final int amount)
    {
        stack.stackSize += amount;
    }


    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static Boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2)
    {
        if (!isEmpty(itemStack1) &&
            !isEmpty(itemStack2) &&
            itemStack1.getItem() == itemStack2.getItem() &&
            itemStack1.getItemDamage() == itemStack2.getItemDamage())
        {
            // Then sort on NBT
            if (itemStack1.hasTagCompound() && itemStack2.hasTagCompound())
            {
                // Then sort on stack size
                return ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
            }
            else
            {
                return true;
            }
        }
        return false;
    }
}

