package com.minecolonies.api.util;

import com.minecolonies.api.compatibility.Compatibility;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    /**
     * Variable representing the empty itemstack in 1.10.
     * Used for easy updating to 1.11
     */
    public static final ItemStack EMPTY = ItemStack.EMPTY;

    /**
     * Predicate to check if an itemStack is empty.
     */
    @NotNull
    public static final Predicate<ItemStack> EMPTY_PREDICATE = ItemStackUtils::isItemStackEmpty;

    /**
     * Negation of the itemStack empty predicate (not empty).
     */
    @NotNull
    public static final Predicate<ItemStack> NOT_EMPTY_PREDICATE = EMPTY_PREDICATE.negate();

    /**
     * The compound id for fortune enchantment.
     */
    private static final int FORTUNE_ENCHANT_ID = 35;

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
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param stack        the stack to test.
     * @param toolType     the type of tool needed
     * @param minimalLevel the minimum level for the tool to find.
     * @param maximumLevel the maximum level for the tool to find.
     * @return true if tool is acceptable
     */
    public static boolean hasToolLevel(@Nullable final ItemStack stack, final ToolType toolType, final int minimalLevel, final int maximumLevel)
    {
        if (isItemStackEmpty(stack))
        {
            return false;
        }

        final int level = getMiningLevel(stack, toolType);
        return isTool(stack, toolType) && verifyToolLevel(stack, level, minimalLevel, maximumLevel);
    }

    /**
     * Checks if this ItemStack can be used as an Axe.
     *
     * @param itemStack Item to check.
     * @return True if item is axe, otherwise false.
     */
    public static boolean isAxe(@Nullable final ItemStack itemStack)
    {
        return isTool(itemStack, ToolType.AXE);
    }

    /**
     * Checks if this ItemStack can be used as a Hoe.
     *
     * @param itemStack Item to check.
     * @return True if item is hoe, otherwise false.
     */
    public static boolean isHoe(@Nullable final ItemStack itemStack)
    {
        return isTool(itemStack, ToolType.HOE);
    }

    /**
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    @NotNull
    public static Boolean isItemStackEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack == EMPTY || stack.getCount() <= 0;
    }

    /**
     * Checks if this ItemStack can be used as a Pick axe.
     *
     * @param itemStack Item to check.
     * @return True if item is a pick axe, otherwise false.
     */
    public static boolean isPickaxe(@Nullable final ItemStack itemStack)
    {
        return isTool(itemStack, ToolType.PICKAXE);
    }


    /**
     * Checks if this ItemStack can be used as a Shovel.
     *
     * @param itemStack Item to check.
     * @return True if item is shovel, otherwise false.
     */
    public static boolean isShovel(@Nullable final ItemStack itemStack)
    {
        return isTool(itemStack, ToolType.SHOVEL);
    }

    /**
     * Checks if this ItemStack can be used as a Tool of type.
     *
     * @param itemStack Item to check.
     * @param toolType  Type of the tool.
     * @return true if item can be used, otherwise false.
     */
    public static boolean isTool(@Nullable final ItemStack itemStack, final ToolType toolType)
    {
        if (isItemStackEmpty(itemStack))
        {
            return false;
        }

        boolean isATool = false;
        switch (toolType)
        {
            case AXE:
            case SHOVEL:
            case PICKAXE:
                isATool = getMiningLevel(itemStack, toolType) >= 0;
                break;
            case HOE:
                isATool = itemStack.getItem() instanceof ItemHoe;
                break;
            case BOW:
                isATool = itemStack.getItem() instanceof ItemBow;
                break;
            case SWORD:
                isATool = itemStack.getItem() instanceof ItemSword;
                break;
            case FISHINGROD:
                isATool = itemStack.getItem() instanceof ItemFishingRod;
                break;
            case NONE:
            default:
                isATool = false;
                break;
        }
        return isATool;
    }


    /**
     * Calculate the mining level an item has as a tool of certain type.
     *
     * @param stack    the stack to test.
     * @param toolType the tool category.
     * @return integer value for mining level &gt;= 0 is okay.
     */
    @SuppressWarnings(DEPRECATION)
    public static int getMiningLevel(@Nullable final ItemStack stack, @Nullable final ToolType toolType)
    {
        if (toolType == ToolType.NONE)
        {
            //empty hand is best on blocks who don't care (0 better 1)
            return stack == null ? 0 : 1;
        }
        if (stack == null || stack == ItemStack.EMPTY)
        {
            return -1;
        }
        if (!Compatibility.getMiningLevelCompatibility(stack, toolType.toString()))
        {
            return -1;
        }
        //todo: use 'better' version of this thing
        int level = -1;
        switch(toolType)
        {
            case HOE:
                if (stack.getItem() instanceof ItemHoe)
                {
                    final ItemHoe itemHoe = (ItemHoe)stack.getItem();
                    level = getToolLevel(itemHoe.getMaterialName());
                }
                break;
            case SWORD:
                if (stack.getItem() instanceof ItemSword)
                {
                    final ItemSword itemSword = (ItemSword)stack.getItem();
                    level = getToolLevel(itemSword.getToolMaterialName());
                }
                break;
            case BOW:
            case FISHINGROD:
                level = 0;
                break;
            default:
                stack.getItem().getHarvestLevel(stack, toolType.getName(), null, null);
                break;
        }
        return level;
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
                final int id = t.getCompoundTagAt(i).getShort("id");
                if (id == FORTUNE_ENCHANT_ID)
                {
                    fortune = t.getCompoundTagAt(i).getShort("lvl");
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
        if (itemStack == null)
        {
            return 0;
        }
        int maxLevel = 0;
        if (itemStack != null)
        {
            final NBTTagList nbttaglist = itemStack.getEnchantmentTagList();

            if (nbttaglist != null)
            {
                for (int j = 0; j < nbttaglist.tagCount(); ++j)
                {
                    final int level = nbttaglist.getCompoundTagAt(j).getShort("lvl");
                    maxLevel = level > maxLevel ? level : maxLevel;
                }
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
        return stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool;
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
            case 0:
            case 1:
                return "Wood or Gold";
            case 2:
                return "Stone";
            case 3:
                return "Iron";
            case 4:
                return "Diamond";
            default:
                return "Better than Diamond";
        }
    }

    private static int getToolLevel(final String material)
    {
        if ("WOOD".equals(material)
            || "GOLD".equals(material))
        {
            return 0;
        }
        else if ("STONE".equals(material))
        {
            return 1;
        }
        else if ("IRON".equals(material))
        {
            return 2;
        }
        else if ("DIAMOND".equals(material))
        {
            return 3;
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

        return existingStack.getMaxStackSize() >= (getItemStackSize(existingStack) + getItemStackSize(mergingStack));
    }

    /**
     * get the size of the stack.
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    @NotNull
    public static int getItemStackSize(final ItemStack stack)
    {
        if (ItemStackUtils.isItemStackEmpty(stack))
        {
            return 0;
        }

        return stack.getCount();
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
        if (!ItemStackUtils.isItemStackEmpty(itemStack1) &&
            !ItemStackUtils.isItemStackEmpty(itemStack2) &&
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

