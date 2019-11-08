package com.minecolonies.api.util;

import com.google.common.collect.Lists;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.compatibility.candb.ChiselAndBitsCheck;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.*;
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
    public static final Predicate<ItemStack> ISFOOD = itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood;

    /**
     * Predicate describing things which work in the furnace.
     */
    public static final Predicate<ItemStack> IS_SMELTABLE = itemStack -> !ItemStackUtils.isEmpty(FurnaceRecipes.instance().getSmeltingResult(itemStack));

    /**
     * Predicate describing food which can be eaten (is not raw).
     */
    public static final Predicate<ItemStack> CAN_EAT =
      itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood && !ISFOOD.test(FurnaceRecipes.instance().getSmeltingResult(itemStack));

    /**
     * Predicate describing cookables.
     */
    public static final Predicate<ItemStack> ISCOOKABLE = itemStack -> ISFOOD.test(FurnaceRecipes.instance().getSmeltingResult(itemStack));

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
     * Get itemStack of tileEntityData. Retrieve the data from the tileEntity.
     *
     * @param compound the tileEntity stored in a compound.
     * @param world    the world.
     * @return the list of itemstacks.
     */
    public static List<ItemStack> getItemStacksOfTileEntity(final NBTTagCompound compound, final World world)
    {
        final List<ItemStack> items = new ArrayList<>();
        if (compound != null)
        {
            final TileEntity tileEntity = TileEntity.create(world, compound);
            if (tileEntity instanceof TileEntityFlowerPot)
            {
                items.add(((TileEntityFlowerPot) tileEntity).getFlowerItemStack());
            }
            else if (tileEntity instanceof TileEntityLockable)
            {
                for (int i = 0; i < ((TileEntityLockable) tileEntity).getSizeInventory(); i++)
                {
                    final ItemStack stack = ((TileEntityLockable) tileEntity).getStackInSlot(i);
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        items.add(stack);
                    }
                }
            }
            else if (tileEntity != null && ChiselAndBitsCheck.isChiselAndBitsTileEntity(tileEntity))
            {
                items.addAll(ChiselAndBitsCheck.getBitStacks(tileEntity));
            }
            else if (tileEntity instanceof TileEntityBed)
            {
                items.add(new ItemStack(Items.BED, 1, ((TileEntityBed) tileEntity).getColor().getMetadata()));
            }
            else if (tileEntity instanceof TileEntityBanner)
            {
                items.add(((TileEntityBanner) tileEntity).getItem());
            }
        }
        return items;
    }

    /**
     * Get the entity of an entityInfo object.
     *
     * @param entityData the input.
     * @param world      the world.
     * @return the output object or null.
     */
    @Nullable
    public static Entity getEntityFromEntityInfoOrNull(final NBTTagCompound entityData, final World world)
    {
        try
        {
            return EntityList.createEntityFromNBT(entityData, world);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Couldn't restore entitiy", e);
            return null;
        }
    }

    /**
     * Adds entities to the builder building if he needs it.
     *
     * @param entityData the entity info object.
     * @param world      the world.
     * @param placer     the entity placer.
     * @return a list of stacks.
     */
    public static List<ItemStorage> getListOfStackForEntityInfo(final NBTTagCompound entityData, final World world, final Entity placer)
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
    public static List<ItemStorage> getListOfStackForEntityInfo(final NBTTagCompound entityData, final World world, final AbstractEntityCitizen placer)
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
            if (entity instanceof EntityItemFrame)
            {
                final ItemStack stack = ((EntityItemFrame) entity).getDisplayedItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    ItemStackUtils.setSize(stack, 1);
                    request.add(new ItemStorage(stack));
                }
                request.add(new ItemStorage(new ItemStack(Items.ITEM_FRAME, 1)));
            }
            else if (entity instanceof EntityArmorStand)
            {
                request.add(new ItemStorage(entity.getPickedResult(new RayTraceResult(placer))));
                entity.getArmorInventoryList().forEach(item -> request.add(new ItemStorage(item)));
                entity.getHeldEquipment().forEach(item -> request.add(new ItemStorage(item)));
            }
            else if (!(entity instanceof EntityMob))
            {
                request.add(new ItemStorage(entity.getPickedResult(new RayTraceResult(placer))));
            }

            return request.stream().filter(stack -> !stack.getItemStack().isEmpty()).collect(Collectors.toList());
        }
        return Collections.emptyList();
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

        final int level = Compatibility.isTinkersWeapon(stack) ? Compatibility.getToolLevel(stack) : getMiningLevel(stack, toolType);
        return isTool(stack, toolType) && verifyToolLevel(stack, level, minimalLevel, maximumLevel);
    }

    /**
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    @NotNull
    public static Boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack == EMPTY || stack.getCount() <= 0;
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
    @SuppressWarnings(DEPRECATION)
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
        //todo: use 'better' version of this thing
        if (ToolType.HOE.equals(toolType))
        {
            if (stack.getItem() instanceof ItemHoe)
            {
                final ItemHoe itemHoe = (ItemHoe) stack.getItem();
                return getToolLevel(itemHoe.getMaterialName());
            }
        }
        else if (ToolType.SWORD.equals(toolType))
        {
            if (stack.getItem() instanceof ItemSword)
            {
                final ItemSword itemSword = (ItemSword) stack.getItem();
                return getToolLevel(itemSword.getToolMaterialName());
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
            if (stack.getItem() instanceof ItemArmor)
            {
                final ItemArmor itemArmor = (ItemArmor) stack.getItem();
                return getArmorLevel(itemArmor.getArmorMaterial());
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
        else if (ToolType.SHEARS.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemShears;
        }
        else if (ToolType.HELMET.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemArmor;
        }
        else if (ToolType.LEGGINGS.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemArmor;
        }
        else if (ToolType.CHESTPLATE.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemArmor;
        }
        else if (ToolType.BOOTS.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemArmor;
        }
        else if (ToolType.SHIELD.equals(toolType))
        {
            isATool = itemStack.getItem() instanceof ItemShield;
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
                 || item == Items.BANNER
                 || !(item instanceof ItemBlock)
                 || !Block.getBlockFromItem(item).getDefaultState().getMaterial().isSolid();
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
     * This routine converts the material type of armor
     * into a numerical value for the request system.
     *
     * @param material type of material of the armor
     * @return armor level
     */
    private static int getArmorLevel(final ArmorMaterial material)
    {
        final int damageReductionAmount = material.getDamageReductionAmount(EntityEquipmentSlot.CHEST);
        if (damageReductionAmount <= ArmorMaterial.LEATHER.getDamageReductionAmount(EntityEquipmentSlot.CHEST))
        {
            return 0;
        }
        else if (damageReductionAmount <= ArmorMaterial.GOLD.getDamageReductionAmount(EntityEquipmentSlot.CHEST) && material != ArmorMaterial.CHAIN)
        {
            return 1;
        }
        else if (damageReductionAmount <= ArmorMaterial.CHAIN.getDamageReductionAmount(EntityEquipmentSlot.CHEST))
        {
            return 2;
        }
        else if (damageReductionAmount <= ArmorMaterial.IRON.getDamageReductionAmount(EntityEquipmentSlot.CHEST))
        {
            return 3;
        }
        else if (damageReductionAmount <= ArmorMaterial.DIAMOND.getDamageReductionAmount(EntityEquipmentSlot.CHEST))
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

    public static boolean hasSilkTouch(@Nullable final ItemStack tool)
    {
        if (tool == null)
        {
            return false;
        }
        boolean hasSilk = false;
        if (tool.isItemEnchanted())
        {
            final NBTTagList t = tool.getEnchantmentTagList();

            for (int i = 0; i < t.tagCount(); i++)
            {
                final int id = t.getCompoundTagAt(i).getShort(NBT_TAG_ENCHANT_ID);
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
        return stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool || Compatibility.isTinkersWeapon(stack);
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
     * get the size of the stack.
     * This is for compatibility between 1.10 and 1.11
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
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @param matchMeta  Set to true to match meta data.
     * @param matchNBT   Set to true to match nbt
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static Boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2, final boolean matchMeta, final boolean matchNBT)
    {
        if (!isEmpty(itemStack1) &&
              !isEmpty(itemStack2) &&
              itemStack1.getItem() == itemStack2.getItem() &&
              (itemStack1.getItemDamage() == itemStack2.getItemDamage() || !matchMeta))
        {
            // Then sort on NBT
            if (itemStack1.hasTagCompound() && itemStack2.hasTagCompound())
            {
                // Then sort on stack size
                return ItemStack.areItemStackTagsEqual(itemStack1, itemStack2) || !matchNBT;
            }
            else
            {
                return true;
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
     * @param stacks    the list of stacks.
     * @param stack     the stack.
     * @param matchMeta if meta has to match.
     * @param matchNBT  if nbt has to match.
     * @return true if so.
     */
    public static boolean compareItemStackListIgnoreStackSize(final List<ItemStack> stacks, final ItemStack stack, final boolean matchMeta, final boolean matchNBT)
    {
        for (final ItemStack tempStack : stacks)
        {
            if (compareItemStacksIgnoreStackSize(tempStack, stack, matchMeta, matchNBT))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * set the size of the stack.
     * This is for compatibility between 1.10 and 1.11
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
    public static ItemStack deserializeFromNBT(@NotNull final NBTTagCompound compound)
    {
        return new ItemStack(compound);
    }

    /**
     * Check if the itemStack is some preferrable type of fuel.
     *
     * @param stack the itemStack to test.
     * @return true if so.
     */
    public boolean isPreferrableFuel(@NotNull final ItemStack stack)
    {
        return stack.isItemEqualIgnoreDurability(new ItemStack(Items.COAL))
                 || stack.isItemEqualIgnoreDurability(new ItemStack(Blocks.LOG))
                 || stack.isItemEqualIgnoreDurability(new ItemStack(Blocks.LOG2));
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

        for (final int oreId : OreDictionary.getOreIDs(stack))
        {
            if (OreDictionary.getOreName(oreId).equals(SAPLINGS))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasSmeltableInFurnaceAndNoFuel(final TileEntityFurnace entity)
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
    public static boolean hasNeitherFuelNorSmeltAble(final TileEntityFurnace entity)
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
    public static boolean hasFuelInFurnaceAndNoSmeltable(final TileEntityFurnace entity)
    {
        return ItemStackUtils.isEmpty(entity.getStackInSlot(SMELTABLE_SLOT))
                 && !ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }
}

