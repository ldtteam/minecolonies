package com.minecolonies.api.util;

import com.google.common.collect.Lists;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.items.ModTags.fungi;
import static com.minecolonies.api.util.constant.Constants.FUEL_SLOT;
import static com.minecolonies.api.util.constant.Constants.SMELTABLE_SLOT;

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
            final Optional<EntityType<?>> type = EntityType.by(entityData);
            if (type.isPresent())
            {
                final Entity entity = type.get().create(world);
                if (entity != null)
                {
                    entity.load(entityData);
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
        if (placer != null)
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
                final ItemStack stack = ((ItemFrameEntity) entity).getItem();
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
                entity.getArmorSlots().forEach(item -> request.add(new ItemStorage(item)));
                entity.getHandSlots().forEach(item -> request.add(new ItemStorage(item)));
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
        if (Compatibility.isTinkersTool(stack, toolType))
        {
            return Compatibility.getToolLevel(stack);
        }
        if (ToolType.HOE.equals(toolType))
        {
            if (stack.getItem() instanceof HoeItem)
            {
                final HoeItem hoeItem = (HoeItem) stack.getItem();
                return hoeItem.getTier().getLevel();
            }
        }
        else if (ToolType.SWORD.equals(toolType))
        {
            if (stack.getItem() instanceof SwordItem)
            {
                final SwordItem SwordItem = (SwordItem) stack.getItem();
                return SwordItem.getTier().getLevel();
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
                return getArmorLevel(ArmorItem.getMaterial());
            }
        }
        else if (stack.getItem() instanceof FishingRodItem)
        {
            return getFishingRodLevel(stack);
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
     * Check if the first stack is a better tool than the second stack.
     * @param stack1 the first stack to check.
     * @param stack2 the second to compare with.
     * @return true if better, false if worse or either of them is not a tool.
     */
    public static boolean isBetterTool(final ItemStack stack1, final ItemStack stack2)
    {
        for (final ToolType toolType : ToolType.values())
        {
            if (isTool(stack1, toolType) && isTool(stack2, toolType) && getMiningLevel(stack1, toolType) > getMiningLevel(stack2, toolType))
            {
                 return true;
            }
        }
        return false;
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
            isATool = itemStack.getItem() instanceof HoeItem || itemStack.getToolTypes().contains(net.minecraftforge.common.ToolType.HOE);
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
                 || !Block.byItem(item).defaultBlockState().getMaterial().isSolid();
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
        final int damageReductionAmount = material.getDefenseForSlot(EquipmentSlotType.CHEST);
        if (damageReductionAmount <= ArmorMaterial.LEATHER.getDefenseForSlot(EquipmentSlotType.CHEST))
        {
            return 0;
        }
        else if (damageReductionAmount <= ArmorMaterial.GOLD.getDefenseForSlot(EquipmentSlotType.CHEST) && material != ArmorMaterial.CHAIN)
        {
            return 1;
        }
        else if (damageReductionAmount <= ArmorMaterial.CHAIN.getDefenseForSlot(EquipmentSlotType.CHEST))
        {
            return 2;
        }
        else if (damageReductionAmount <= ArmorMaterial.IRON.getDefenseForSlot(EquipmentSlotType.CHEST))
        {
            return 3;
        }
        else if (damageReductionAmount <= ArmorMaterial.DIAMOND.getDefenseForSlot(EquipmentSlotType.CHEST))
        {
            return 4;
        }

        return 5;
    }

    /**
     * Estimates the fishing rod tier from available durability and enchantment status.
     *
     * @param itemStack the tool to check.
     * @return equivalent tool level.
     */
    private static int getFishingRodLevel(final ItemStack itemStack)
    {
        if (itemStack.getItem() == Items.FISHING_ROD)
        {
            return 1;
        }
        if (!itemStack.isDamageableItem())
        {
            return 5;
        }
        final int rodDurability = itemStack.getMaxDamage();
        if (rodDurability <= (ItemTier.WOOD.getUses() + MinecoloniesAPIProxy.getInstance().getConfig().getServer().fishingRodDurabilityAdjustT1.get()))
        {
            return 1;
        }
        else if (rodDurability <= (ItemTier.IRON.getUses() + MinecoloniesAPIProxy.getInstance().getConfig().getServer().fishingRodDurabilityAdjustT2.get()))
        {
            return 2;
        }
        return 3;
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
            final ListNBT ListNBT = itemStack.getEnchantmentTags();

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
            final ListNBT t = tool.getEnchantmentTags();

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
            final ListNBT t = tool.getEnchantmentTags();

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
    public static int getSize(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return 0;
        }

        return stack.getCount();
    }

    /**
     * get the Durability of the stack.
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    public static int getDurability(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return 0;
        }

        return stack.getMaxDamage() - stack.getDamageValue();
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

        if (isEmpty(itemStack1) != isEmpty(itemStack2))
        {
            return false;
        }

        if (itemStack1.getItem() == itemStack2.getItem() && (!matchDamage || itemStack1.getDamageValue() == itemStack2.getDamageValue()))
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
                CompoundNBT nbt1 = itemStack1.getTag();
                CompoundNBT nbt2 = itemStack2.getTag();

                for(String key :nbt1.getAllKeys())
                {
                    if(!matchDamage && key.equals("Damage"))
                    {
                        continue;
                    }
                    if(!nbt2.contains(key) || !nbt1.get(key).equals(nbt2.get(key)))
                    {
                        return false;
                    }
                }
                
                return nbt1.getAllKeys().size() == nbt2.getAllKeys().size();
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
        return ItemStack.of(compound);
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

        return stack.getItem().is(ItemTags.SAPLINGS) || stack.getItem().is(fungi) || Compatibility.isDynamicTreeSapling(stack);
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasSmeltableInFurnaceAndNoFuel(final FurnaceTileEntity entity)
    {
        return !ItemStackUtils.isEmpty(entity.getItem(SMELTABLE_SLOT))
                 && ItemStackUtils.isEmpty(entity.getItem(FUEL_SLOT));
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasNeitherFuelNorSmeltAble(final FurnaceTileEntity entity)
    {
        return ItemStackUtils.isEmpty(entity.getItem(SMELTABLE_SLOT))
                 && ItemStackUtils.isEmpty(entity.getItem(FUEL_SLOT));
    }

    /**
     * Check if the furnace has fuel in it and smeltable empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasFuelInFurnaceAndNoSmeltable(final FurnaceTileEntity entity)
    {
        return ItemStackUtils.isEmpty(entity.getItem(SMELTABLE_SLOT))
                 && !ItemStackUtils.isEmpty(entity.getItem(FUEL_SLOT));
    }

    /**
     * Convert an Item string with NBT to an ItemStack
     * @param itemData ie: minecraft:potion{Potion=minecraft:water}
     * @return stack with any defined NBT
     */
    public static ItemStack idToItemStack(final String itemData)
    {
        String itemId = itemData;
        final int tagIndex = itemId.indexOf("{");
        final String tag = tagIndex > 0 ? itemId.substring(tagIndex) : null;
        itemId = tagIndex > 0 ? itemId.substring(0, tagIndex) : itemId;
        String[] split = itemId.split(":");
        if(split.length != 2)
        {
            if(split.length == 1)
            {
                final String[] tempArray ={"minecraft", split[0]};
                split = tempArray;
            }
            else
            {
                Log.getLogger().error("Unable to parse item definition: " + itemData);
            }
        }
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
        final ItemStack stack = new ItemStack(item);
        if (tag != null)
        {
            try
            {
                stack.setTag(JsonToNBT.parseTag(tag));
            }
            catch (CommandSyntaxException e1)
            {
                //Unable to parse tags, drop them.
                Log.getLogger().error("Unable to parse item definition: " + itemData);
            }
        }
        if (stack.isEmpty())
        {
            Log.getLogger().warn("Parsed item definition returned empty: " + itemData);
        }
        return stack;
    }

    /**
     * Obtains a list of all basic items in the game, plus any extra items present in the player's
     * inventory (allowing for items with custom NBT, e.g. DO blocks or dyed armour).
     *
     * @param player The player whose inventory to check.
     * @return The set of items.
     */
    public static Set<ItemStack> allItemsPlusInventory(@NotNull final PlayerEntity player)
    {
        // get all known items first
        final Set<ItemStorage> allItems = new HashSet<>(IColonyManager.getInstance().getCompatibilityManager().getSetOfAllItems());

        // plus all items from the player's inventory not already listed (adds items with extra NBT)
        for (final ItemStack stack : player.inventory.items)
        {
            if (stack.isEmpty()) continue;

            ItemStack pristine = stack;
            if (stack.isDamageableItem() && stack.isDamaged())
            {
                pristine = stack.copy();
                pristine.setDamageValue(0);
                // in case the item wasn't already in the set, we want to only store a pristine one!
            }
            allItems.add(new ItemStorage(pristine, true));
        }

        return allItems.stream().map(ItemStorage::getItemStack).collect(Collectors.toSet());
    }
}

