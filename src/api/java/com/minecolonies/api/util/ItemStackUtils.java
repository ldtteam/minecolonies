package com.minecolonies.api.util;

import com.google.common.collect.Lists;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.FUEL_SLOT;
import static com.minecolonies.api.util.constant.Constants.SMELTABLE_SLOT;
import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    /**
     * Variable representing the empty itemstack in 1.10. Used for easy updating to 1.11
     */
    public static final ItemStack EMPTY = ItemStack.EMPTY;

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
     * The compound id for Silk Touch enchantment.
     */
    private static final int SILK_TOUCH_ENCHANT_ID = 33;

    /**
     * Predicate describing food.
     */
    public static Predicate<ItemStack> ISFOOD;

    /**
     * Predicate describing things which work in the furnace.
     */
    public static Predicate<ItemStack> IS_SMELTABLE;

    /**
     * Predicate describing food which can be eaten (is not raw).
     */
    public static Predicate<ItemStack> CAN_EAT;

    /**
     * Predicate describing cookables.
     */
    public static Predicate<ItemStack> ISCOOKABLE;

    /**
     * Predicate to check for compost items.
     */
    public static final Predicate<ItemStack> IS_COMPOST = stack -> !stack.isEmpty() && stack.getItem() == ModItems.compost;

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
     * Get the entity of an entityInfo object.
     *
     * @param entityData the input.
     * @param world      the world.
     * @return the output object or null.
     */
    @Nullable
    public static Entity getEntityFromEntityInfoOrNull(final CompoundNBT entityData, final World world)
    {
        try
        {
            final Optional<EntityType<?>> type = EntityType.readEntityType(entityData);
            if (type.isPresent())
            {
                final Entity entity = type.get().create(world);
                if (entity != null)
                {
                    entity.read(entityData);
                    return entity;
                }
            }
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Couldn't restore entitiy", e);
            return null;
        }
        return null;
    }

    /**
     * Adds entities to the builder building if he needs it.
     *
     * @param entityData the entity info object.
     * @param world      the world.
     * @param placer     the entity placer.
     * @return a list of stacks.
     */
    public static List<ItemStorage> getListOfStackForEntityInfo(final CompoundNBT entityData, final World world, final Entity placer)
    {
        if (entityData != null)
        {
            final Entity entity = getEntityFromEntityInfoOrNull(entityData, world);
            if (entity != null)
            {
                if (EntityUtils.isEntityAtPosition(entity, world, placer))
                {
                    return Collections.emptyList();
                }
                return getListOfStackForEntity(entity, placer);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Adds entities to the builder building if he needs it.
     *
     * @param entityData the entity info object.
     * @param world      the world.
     * @param placer     the entity placer.
     * @return a list of stacks.
     */
    public static List<ItemStorage> getListOfStackForEntityInfo(final CompoundNBT entityData, final World world, final AbstractEntityCitizen placer)
    {
        if (placer instanceof Entity)
        {
            return getListOfStackForEntityInfo(entityData, world, (Entity) placer);
        }

        return Lists.newArrayList();
    }

    /**
     * Adds entities to the builder building if he needs it.
     *
     * @param entity the entity object.
     * @param placer the entity placer.
     * @return a list of stacks.
     */
    public static List<ItemStorage> getListOfStackForEntity(final Entity entity, final Entity placer)
    {
        if (entity != null)
        {
            final List<ItemStorage> request = new ArrayList<>();
            if (entity instanceof ItemFrameEntity)
            {
                final ItemStack stack = ((ItemFrameEntity) entity).getDisplayedItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    ItemStackUtils.setSize(stack, 1);
                    request.add(new ItemStorage(stack));
                }
                request.add(new ItemStorage(new ItemStack(Items.ITEM_FRAME, 1)));
            }
            else if (entity instanceof ArmorStandEntity)
            {
                request.add(new ItemStorage(entity.getPickedResult(new EntityRayTraceResult(placer))));
                entity.getArmorInventoryList().forEach(item -> request.add(new ItemStorage(item)));
                entity.getHeldEquipment().forEach(item -> request.add(new ItemStorage(item)));
            }

            /*
            todo: deactivated until forge fixes this problem.
            else if (!(entity instanceof MobEntity))
            {
                request.add(new ItemStorage(entity.getPickedResult(new EntityRayTraceResult(placer))));
            }*/

            return request.stream().filter(stack -> !stack.getItemStack().isEmpty()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Verifies if there is one tool with an acceptable level in a worker's inventory.
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

        final int level = Compatibility.isTinkersWeapon(stack) ? Compatibility.getToolLevel(stack) : getMiningLevel(stack, toolType);
        return isTool(stack, toolType) && verifyToolLevel(stack, level, minimalLevel, maximumLevel);
    }

    /**
     * Wrapper method to check if a stack is empty. Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    @NotNull
    public static Boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack.isEmpty();
    }

    public static Boolean isNotEmpty(@Nullable final ItemStack stack)
    {
        return !isEmpty(stack);
    }

    /**
     * Calculate the mining level an item has as a tool of certain type.
     *
     * @param stack    the stack to test.
     * @param toolType the tool category.
     * @return integer value for mining level &gt;= 0 is okay.
     */
    public static int getMiningLevel(@Nullable final ItemStack stack, @Nullable final IToolType toolType)
    {
        if (toolType == ToolType.NONE)
        {
            //empty hand is best on blocks who don't care (0 better 1)
            return stack == null ? 0 : 1;
        }
        if (!Compatibility.getMiningLevelCompatibility(stack, toolType.toString()))
        {
            return -1;
        }
        if (ToolType.HOE.equals(toolType))
        {
            if (stack.getItem() instanceof HoeItem)
            {
                final HoeItem hoeItem = (HoeItem) stack.getItem();
                return hoeItem.getTier().getHarvestLevel();
            }
        }
        else if (ToolType.SWORD.equals(toolType))
        {
            if (stack.getItem() instanceof SwordItem)
            {
                final SwordItem SwordItem = (SwordItem) stack.getItem();
                return SwordItem.getTier().getHarvestLevel();
            }
            else if (Compatibility.isTinkersWeapon(stack))
            {
                return Compatibility.getToolLevel(stack);
            }
        }
        else if (ToolType.HELMET.equals(toolType)
                   || ToolType.BOOTS.equals(toolType)
                   || ToolType.CHESTPLATE.equals(toolType)
                   || ToolType.LEGGINGS.equals(toolType))
        {
            if (stack.getItem() instanceof ArmorItem)
            {
                final ArmorItem ArmorItem = (ArmorItem) stack.getItem();
                return getArmorLevel(ArmorItem.getArmorMaterial());
            }
        }
        else if (!toolType.hasVariableMaterials())
        {
            //We need a hut level 1 minimum
            return 1;
        }
        else
        {
            return stack.getItem().getHarvestLevel(stack, net.minecraftforge.common.ToolType.get(toolType.getName()), null, null);
        }
        return -1;
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
            isATool = itemStack.getItem() instanceof HoeItem;
        }
        else if (ToolType.BOW.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof BowItem;
        }
        else if (ToolType.SWORD.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof SwordItem || Compatibility.isTinkersWeapon(itemStack);
        }
        else if (ToolType.FISHINGROD.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof FishingRodItem;
        }
        else if (ToolType.SHEARS.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ShearsItem;
        }
        else if (ToolType.HELMET.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ArmorItem;
        }
        else if (ToolType.LEGGINGS.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ArmorItem;
        }
        else if (ToolType.CHESTPLATE.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ArmorItem;
        }
        else if (ToolType.BOOTS.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ArmorItem;
        }
        else if (ToolType.SHIELD.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ShieldItem;
        }
        else if (ToolType.FLINT_N_STEEL.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof FlintAndSteelItem;
        }
        return isATool;
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
     * Check if an itemStack is a decorative item for the decoration step of the structure placement.
     *
     * @param stack the itemStack to test.
     * @return true if so.
     */
    public static boolean isDecoration(final ItemStack stack)
    {
        final Item item = stack.getItem();
        return item == Items.ITEM_FRAME
                 || item == Items.ARMOR_STAND
                 || !Block.getBlockFromItem(item).getDefaultState().getMaterial().isSolid();
    }

    /*
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
    */

    /**
     * This routine converts the material type of armor into a numerical value for the request system.
     *
     * @param material type of material of the armor
     * @return armor level
     */
    private static int getArmorLevel(final IArmorMaterial material)
    {
        final int damageReductionAmount = material.getDamageReductionAmount(EquipmentSlotType.CHEST);
        if (damageReductionAmount <= ArmorMaterial.LEATHER.getDamageReductionAmount(EquipmentSlotType.CHEST))
        {
            return 0;
        }
        else if (damageReductionAmount <= ArmorMaterial.GOLD.getDamageReductionAmount(EquipmentSlotType.CHEST) && material != ArmorMaterial.CHAIN)
        {
            return 1;
        }
        else if (damageReductionAmount <= ArmorMaterial.CHAIN.getDamageReductionAmount(EquipmentSlotType.CHEST))
        {
            return 2;
        }
        else if (damageReductionAmount <= ArmorMaterial.IRON.getDamageReductionAmount(EquipmentSlotType.CHEST))
        {
            return 3;
        }
        else if (damageReductionAmount <= ArmorMaterial.DIAMOND.getDamageReductionAmount(EquipmentSlotType.CHEST))
        {
            return 4;
        }

        return 5;
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
            final ListNBT ListNBT = itemStack.getEnchantmentTagList();

            if (ListNBT != null)
            {
                for (int j = 0; j < ListNBT.size(); ++j)
                {
                    final int level = ListNBT.getCompound(j).getShort("lvl");
                    maxLevel = level > maxLevel ? level : maxLevel;
                }
            }
        }
        return Math.max(maxLevel - 1, 0);
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
        if (tool.isEnchanted())
        {
            final ListNBT t = tool.getEnchantmentTagList();

            for (int i = 0; i < t.size(); i++)
            {
                final int id = t.getCompound(i).getShort(NBT_TAG_ENCHANT_ID);
                if (id == FORTUNE_ENCHANT_ID)
                {
                    fortune = t.getCompound(i).getShort(NBT_TAG_ENCHANT_LEVEL);
                }
            }
        }
        return fortune;
    }

    public static boolean hasSilkTouch(@Nullable final ItemStack tool)
    {
        if (tool == null)
        {
            return false;
        }
        boolean hasSilk = false;
        if (tool.isEnchanted())
        {
            final ListNBT t = tool.getEnchantmentTagList();

            for (int i = 0; i < t.size(); i++)
            {
                final int id = t.getCompound(i).getShort(NBT_TAG_ENCHANT_ID);
                if (id == SILK_TOUCH_ENCHANT_ID)
                {
                    hasSilk = true;
                }
            }
        }
        return hasSilk;
    }

    /**
     * Checks if an item serves as a weapon.
     *
     * @param stack the stack to analyze.
     * @return true if it is a tool or sword.
     */
    public static boolean doesItemServeAsWeapon(@NotNull final ItemStack stack)
    {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof ToolItem || Compatibility.isTinkersWeapon(stack);
    }

    /**
     * Assigns a string containing the grade of the toolGrade.
     *
     * @param toolGrade the number of the grade of a tool
     * @return a string corresponding to the tool
     */
    public static String swapArmorGrade(final int toolGrade)
    {
        switch (toolGrade)
        {
            case 0:
                return "Leather";
            case 1:
                return "Gold";
            case 2:
                return "Chain";
            case 3:
                return "Iron";
            case 4:
                return "Diamond";
            default:
                return "Better than Diamond";
        }
    }

    /**
     * Assigns a string containing the grade of the armor grade.
     *
     * @param toolGrade the number of the grade of an armor
     * @return a string corresponding to the armor
     */
    public static String swapToolGrade(final int toolGrade)
    {
        switch (toolGrade)
        {
            case 0:
                return "Wood or Gold";
            case 1:
                return "Stone";
            case 2:
                return "Iron";
            case 3:
                return "Diamond";
            default:
                return "Better than Diamond";
        }
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
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static Boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, true, true);
    }

    /**
     * get the size of the stack. This is for compatibility between 1.10 and 1.11
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    @NotNull
    public static int getSize(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return 0;
        }

        return stack.getCount();
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1  The left stack to compare.
     * @param itemStack2  The right stack to compare.
     * @param matchDamage Set to true to match damage data.
     * @param matchNBT    Set to true to match nbt
     * @return True when they are equal except the stacksize, false when not.
     */
    public static boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2, final boolean matchDamage, final boolean matchNBT)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, matchDamage, matchNBT, false);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1  The left stack to compare.
     * @param itemStack2  The right stack to compare.
     * @param matchDamage Set to true to match damage data.
     * @param matchNBT    Set to true to match nbt
     * @param min         if the count of stack2 has to be at least the same as stack1.
     * @return True when they are equal except the stacksize, false when not.
     */
    public static boolean compareItemStacksIgnoreStackSize(
      final ItemStack itemStack1,
      final ItemStack itemStack2,
      final boolean matchDamage,
      final boolean matchNBT,
      final boolean min)
    {
        if (isEmpty(itemStack1) && isEmpty(itemStack2))
        {
            return true;
        }

        if (!isEmpty(itemStack1) &&
              !isEmpty(itemStack2) &&
              itemStack1.getItem() == itemStack2.getItem() &&
              (itemStack1.getDamage() == itemStack2.getDamage() || !matchDamage))
        {
            if (!matchNBT)
            {
                // Not comparing nbt
                return true;
            }

            if (min && itemStack1.getCount() > itemStack2.getCount())
            {
                return false;
            }

            // Then sort on NBT
            if (itemStack1.hasTag() && itemStack2.hasTag())
            {
                // Then sort on stack size
                return ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
            }
            else
            {
                return (!itemStack1.hasTag() || itemStack1.getTag().isEmpty())
                         && (!itemStack2.hasTag() || itemStack2.getTag().isEmpty());
            }
        }
        return false;
    }

    /**
     * Method to check if a stack is in a list of stacks.
     *
     * @param stacks the list of stacks.
     * @param stack  the stack.
     * @return true if so.
     */
    public static boolean compareItemStackListIgnoreStackSize(final List<ItemStack> stacks, final ItemStack stack)
    {
        return compareItemStackListIgnoreStackSize(stacks, stack, true, true);
    }

    /**
     * Method to check if a stack is in a list of stacks.
     *
     * @param stacks      the list of stacks.
     * @param stack       the stack.
     * @param matchDamage if damage has to match.
     * @param matchNBT    if nbt has to match.
     * @return true if so.
     */
    public static boolean compareItemStackListIgnoreStackSize(final List<ItemStack> stacks, final ItemStack stack, final boolean matchDamage, final boolean matchNBT)
    {
        for (final ItemStack tempStack : stacks)
        {
            if (compareItemStacksIgnoreStackSize(tempStack, stack, matchDamage, matchNBT))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * set the size of the stack. This is for compatibility between 1.10 and 1.11
     *
     * @param stack to set the size to
     * @param size  of the stack
     */
    @NotNull
    public static void setSize(@NotNull final ItemStack stack, final int size)
    {
        stack.setCount(size);
    }

    /**
     * Increase or decrease the stack size.
     *
     * @param stack  to set the size to
     * @param amount to increase the stack's size of (negative value to decrease)
     */
    public static void changeSize(@NotNull final ItemStack stack, final int amount)
    {
        stack.setCount(stack.getCount() + amount);
    }

    /**
     * Update method to allow for easy reading the ItemStack data from NBT.
     *
     * @param compound The compound to read from.
     * @return The ItemStack stored in the NBT Data.
     */
    @NotNull
    public static ItemStack deserializeFromNBT(@NotNull final CompoundNBT compound)
    {
        return ItemStack.read(compound);
    }

    /**
     * Checks if a stack is a type of sapling, using Oredict
     *
     * @param stack the stack to check.
     * @return true if sapling.
     */
    public static boolean isStackSapling(@Nullable final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        return stack.getItem().isIn(ItemTags.SAPLINGS);
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasSmeltableInFurnaceAndNoFuel(final FurnaceTileEntity entity)
    {
        return !ItemStackUtils.isEmpty(entity.getStackInSlot(SMELTABLE_SLOT))
                 && ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasNeitherFuelNorSmeltAble(final FurnaceTileEntity entity)
    {
        return ItemStackUtils.isEmpty(entity.getStackInSlot(SMELTABLE_SLOT))
                 && ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Check if the furnace has fuel in it and smeltable empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasFuelInFurnaceAndNoSmeltable(final FurnaceTileEntity entity)
    {
        return ItemStackUtils.isEmpty(entity.getStackInSlot(SMELTABLE_SLOT))
                 && !ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }
}

