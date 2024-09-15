package com.minecolonies.api.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonParser;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.util.AdvancementUtils;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.minecolonies.api.items.ModTags.fungi;
import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    /**
     * Pattern for {@link #parseIdTemplate}.
     */
    private static final Pattern TEMPLATE_PATH_PATTERN = Pattern.compile("\\[PATH(?::([^=]*)=([^]]*))?]");

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
     * List of the checked nbt sets for itemstack comparisons.
     */
    public static HashMap<Item, Set<DataComponentType<?>>> CHECKED_NBT_KEYS = new HashMap<>();

    /**
     * True if this stack is a standard food item (has at least some healing and some saturation, not purely for effects).
     */
    public static final Predicate<ItemStack> ISFOOD =
      stack ->
      {
          final FoodProperties foodProperties = stack.getFoodProperties(null);
          return ItemStackUtils.isNotEmpty(stack) && foodProperties != null && foodProperties.nutrition() > 0
                     && foodProperties.saturation() > 0 && !stack.is(ModTags.excludedFood);
      };

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
    public static final Predicate<ItemStack> IS_COMPOST = stack -> !stack.isEmpty() && stack.is(ModItems.compost);

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
    public static Entity getEntityFromEntityInfoOrNull(final CompoundTag entityData, final Level world)
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
    public static List<ItemStorage> getListOfStackForEntityInfo(final CompoundTag entityData, final Level world, final Entity placer)
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
    public static List<ItemStorage> getListOfStackForEntityInfo(final CompoundTag entityData, final Level world, final AbstractEntityCitizen placer)
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
            if (entity instanceof ItemFrame)
            {
                final ItemStack stack = ((ItemFrame) entity).getItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    ItemStackUtils.setSize(stack, 1);
                    request.add(new ItemStorage(stack));
                }
                request.add(new ItemStorage(new ItemStack(Items.ITEM_FRAME, 1)));
            }
            else if (entity instanceof ArmorStand)
            {
                request.add(new ItemStorage(entity.getPickedResult(new EntityHitResult(placer))));
                ((ArmorStand) entity).getArmorSlots().forEach(item -> request.add(new ItemStorage(item)));
                ((ArmorStand) entity).getHandSlots().forEach(item -> request.add(new ItemStorage(item)));
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
    public static boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack.isEmpty();
    }

    public static boolean isNotEmpty(@Nullable final ItemStack stack)
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
        if (!isTool(stack, toolType))
        {
            return -1;
        }

        if (toolType == ToolType.SWORD && Compatibility.isTinkersWeapon(stack))
        {
            return Compatibility.getToolLevel(stack);
        }
        else if (Compatibility.isTinkersTool(stack, toolType))
        {
            return Compatibility.getToolLevel(stack);
        }

        if (ToolType.HELMET.equals(toolType)
                || ToolType.BOOTS.equals(toolType)
                || ToolType.CHESTPLATE.equals(toolType)
                || ToolType.LEGGINGS.equals(toolType))
        {
            if (stack.getItem() instanceof final ArmorItem armorItem)
            {
                return getArmorLevel(armorItem.getMaterial().value());
            }
        }
        else if (stack.getItem() instanceof final TieredItem tieredItem)  // most tools
        {
            return (int) tieredItem.getTier().getAttackDamageBonus();
        }
        else if (toolType.equals(ToolType.FISHINGROD))
        {
            return getFishingRodLevel(stack);
        }
        else if (toolType.equals(ToolType.SHEARS))
        {
            return 0;
        }
        else if (!toolType.hasVariableMaterials())
        {
            //We need a hut level 1 minimum
            return 1;
        }
        return -1;
    }

    /**
     * Check if the first stack is a better tool than the second stack.
     *
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

        if (ToolType.AXE.equals(toolType) && itemStack.canPerformAction(ItemAbilities.AXE_DIG))
        {
            return true;
        }

        if (ToolType.SHOVEL.equals(toolType) && itemStack.canPerformAction(ItemAbilities.SHOVEL_DIG))
        {
            return true;
        }

        if (ToolType.PICKAXE.equals(toolType) && itemStack.canPerformAction(ItemAbilities.PICKAXE_DIG))
        {
            return true;
        }

        if (ToolType.HOE.equals(toolType))
        {
            for (final ItemAbility action : ItemAbilities.DEFAULT_HOE_ACTIONS)
            {
                if (!itemStack.canPerformAction(action))
                {
                    return false;
                }
            }
            return true;
        }
        if (ToolType.BOW.equals(toolType))
        {
            return itemStack.getItem() instanceof BowItem;
        }
        if (ToolType.SWORD.equals(toolType))
        {
            return itemStack.canPerformAction(ItemAbilities.SWORD_SWEEP) || Compatibility.isTinkersWeapon(itemStack);
        }
        if (ToolType.FISHINGROD.equals(toolType) && itemStack.canPerformAction(ItemAbilities.FISHING_ROD_CAST))
        {
            return true;
        }
        if (ToolType.SHEARS.equals(toolType) && itemStack.canPerformAction(ItemAbilities.SHEARS_DIG) && itemStack.canPerformAction(ItemAbilities.SHEARS_HARVEST))
        {
            return true;
        }
        if (ToolType.HELMET.equals(toolType))
        {
            return itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot());
        }
        if (ToolType.LEGGINGS.equals(toolType))
        {
            return itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot());
        }
        if (ToolType.CHESTPLATE.equals(toolType))
        {
            return itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot());
        }
        if (ToolType.BOOTS.equals(toolType))
        {
            return itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot());
        }
        if (ToolType.SHIELD.equals(toolType))
        {
            return itemStack.getItem() instanceof ShieldItem;   //canPerformAction(ItemAbilities.SHIELD_BLOCK) ?
        }
        if (ToolType.FLINT_N_STEEL.equals(toolType))
        {
            return itemStack.getItem() instanceof FlintAndSteelItem;
        }

        return false;
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
     * This routine converts the material type of armor into a numerical value for the request system.
     *
     * @param material type of material of the armor
     * @return armor level
     */
    private static int getArmorLevel(final ArmorMaterial material)
    {
        final float armorLevel = getArmorValue(material);

        if (armorLevel <= getArmorValue(ArmorMaterials.LEATHER.value()))
        {
            return 0;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.GOLD.value()))
        {
            return 1;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.CHAIN.value()))
        {
            return 2;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.IRON.value()))
        {
            return 3;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.DIAMOND.value()))
        {
            return 4;
        }

        return 5;
    }

    /**
     * Calculate the full armor level of an entire kit of armor combined.
     *
     * @param material type of material of the armor.
     * @return the armor value.
     */
    private static float getArmorValue(final ArmorMaterial material)
    {
        int value = 0;
        for (final ArmorItem.Type type : ArmorItem.Type.values())
        {
            value += Objects.requireNonNullElse(material.defense().get(type), 0);
        }
        return value + material.toughness() * 4;
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
        if (rodDurability <= (Tiers.WOOD.getUses() + 22))
        {
            return 1;
        }
        else if (rodDurability <= (Tiers.IRON.getUses() + 6))
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

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemStack.getTagEnchantments().entrySet())
        {
            final int level = entry.getIntValue();
            maxLevel = Math.max(level, maxLevel);
        }

        return Math.max(maxLevel - 1, 0);
    }

    /**
     * Calculates the fortune level this tool has.
     *
     * @param tool the tool to check.
     * @return fortune level.
     */
    public static int getFortuneOf(@Nullable final ItemStack tool, final Level level)
    {
        if (tool == null)
        {
            return 0;
        }
        //calculate fortune enchantment
        int fortune = 0;
        if (tool.isEnchanted())
        {
            return tool.getTagEnchantments().getLevel(Utils.getRegistryValue(Enchantments.FORTUNE, level));
        }
        return fortune;
    }

    /**
     * Checks if an item serves as a weapon.
     *
     * @param stack the stack to analyze.
     * @return true if it is a tool or sword.
     */
    public static boolean doesItemServeAsWeapon(@NotNull final ItemStack stack)
    {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof DiggerItem || Compatibility.isTinkersWeapon(stack);
    }

    /**
     * Assigns a string containing the grade of the toolGrade.
     *
     * @param toolGrade the number of the grade of a tool
     * @return a string corresponding to the tool
     */
    public static MutableComponent swapArmorGrade(final int toolGrade)
    {
        if (toolGrade >= 0 && toolGrade <= 4)
        {
            return Component.translatableEscape("com.minecolonies.coremod.armorlevel." + toolGrade);
        }

        // this shouldn't really ever happen, but just in case...
        return Component.translatableEscape("com.minecolonies.coremod.armorlevel.etc");
    }

    /**
     * Assigns a string containing the grade of the armor grade.
     *
     * @param toolGrade the number of the grade of an armor
     * @return a string corresponding to the armor
     */
    public static MutableComponent swapToolGrade(final int toolGrade)
    {
        if (toolGrade >= 0 && toolGrade <= 4)
        {
            return Component.translatableEscape("com.minecolonies.coremod.toollevel." + toolGrade);
        }

        return Component.translatableEscape("com.minecolonies.coremod.toollevel.etc");
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
     * get the size of the stac
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    public static int getSize(@NotNull final ItemStack stack)
    {
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
    public static boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2, final boolean matchDamage, final boolean matchNBT, final boolean min)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, matchDamage, matchNBT, false, false);
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
      final boolean min,
      final boolean matchNBTExactly)
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

            final Set<DataComponentType<?>> checkedKeys = CHECKED_NBT_KEYS.get(itemStack1.getItem());
            if (checkedKeys == null || checkedKeys.isEmpty())
            {
                return true;
            }

            for (final DataComponentType<?> key : checkedKeys)
            {
                //todo double check this works, otherwise we might have to serialize it before comparison.
                if (!Objects.equals(itemStack1.getComponents().get(key), itemStack2.getComponents().get(key)))
                {
                    return false;
                }
            }

            return itemStack1.getComponents().size() == itemStack2.getComponents().size();
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
    public static ItemStack deserializeFromNBT(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        return ItemStack.parseOptional(provider, compound);
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

        return stack.is(ItemTags.SAPLINGS) || stack.is(Tags.Items.MUSHROOMS) || stack.is(fungi) || Compatibility.isDynamicTreeSapling(stack);
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     *
     * @param entity the furnace.
     * @return true if so.
     */
    public static boolean hasSmeltableInFurnaceAndNoFuel(final FurnaceBlockEntity entity)
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
    public static boolean hasNeitherFuelNorSmeltAble(final FurnaceBlockEntity entity)
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
    public static boolean hasFuelInFurnaceAndNoSmeltable(final FurnaceBlockEntity entity)
    {
        return ItemStackUtils.isEmpty(entity.getItem(SMELTABLE_SLOT))
                 && !ItemStackUtils.isEmpty(entity.getItem(FUEL_SLOT));
    }

    /**
     * Check if the brewingStand has smeltable in it and fuel empty.
     *
     * @param entity the brewingStand.
     * @return true if so.
     */
    public static boolean hasBrewableAndNoFuel(final BrewingStandBlockEntity entity)
    {
        return !ItemStackUtils.isEmpty(entity.getItem(INGREDIENT_SLOT))
                 && ItemStackUtils.isEmpty(entity.getItem(BREWING_FUEL_SLOT));
    }

    /**
     * Check if the brewingStand has smeltable in it and fuel empty.
     *
     * @param entity the brewingStand.
     * @return true if so.
     */
    public static boolean hasNeitherFuelNorBrewable(final BrewingStandBlockEntity entity)
    {
        return ItemStackUtils.isEmpty(entity.getItem(INGREDIENT_SLOT))
                 && ItemStackUtils.isEmpty(entity.getItem(BREWING_FUEL_SLOT));
    }

    /**
     * Check if the brewingStand has fuel in it and smeltable empty.
     *
     * @param entity the brewingStand.
     * @return true if so.
     */
    public static boolean hasFuelAndNoBrewable(final BrewingStandBlockEntity entity)
    {
        return ItemStackUtils.isEmpty(entity.getItem(INGREDIENT_SLOT))
                 && !ItemStackUtils.isEmpty(entity.getItem(BREWING_FUEL_SLOT));
    }

    /**
     * Convert an Item string with NBT to an ItemStack
     *
     * @param itemData ie: minecraft:potion{"minecraft:potion_contents":{"potion":"minecraft:water"}}
     * @return stack with any defined NBT
     */
    public static ItemStack idToItemStack(final String itemData, final HolderLookup.Provider provider)
    {
        String itemId = itemData;
        final int tagIndex = itemId.indexOf("{");
        final String tag = tagIndex > 0 ? itemId.substring(tagIndex) : null;
        itemId = tagIndex > 0 ? itemId.substring(0, tagIndex) : itemId;
        final Item item;
        try
        {
            item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        }
        catch (Throwable t)
        {
            Log.getLogger().error("Unable to parse item definition: {}", itemData, t);
            return ItemStack.EMPTY;
        }
        final ItemStack stack = new ItemStack(item);
        if (tag != null)
        {
            try
            {
                stack.applyComponents(Utils.deserializeCodecMessFromJson(DataComponentPatch.CODEC, provider, JsonParser.parseString(tag)));
            }
            catch (Throwable t)
            {
                //Unable to parse tags, drop them.
                Log.getLogger().error("Unable to parse item definition: {}", itemData, t);
            }
        }
        if (stack.isEmpty())
        {
            Log.getLogger().warn("Parsed item definition returned empty: {}", itemData);
        }
        return stack;
    }

    /**
     * Parses an item id that may contain replaceable template components:
     *
     * <pre>
     *     [NS]           => {@code baseItemId.getNamespace()}
     *     [PATH]         => {@code baseItemId.getPath()}
     *     [PATH:foo=bar] => {@code baseItemId.getPath()} but with "foo" replaced with "bar"</pre>
     *
     * @param itemId     the id to parse
     * @param baseItemId the base item id to use to fill in the components
     * @return a tuple of (boolean, result), where the boolean is false if result didn't resolve to a valid item
     */
    @NotNull
    public static Tuple<Boolean, String> parseIdTemplate(@Nullable String itemId,
                                                         @NotNull final ResourceLocation baseItemId)
    {
        if (itemId == null)
        {
            return new Tuple<>(false, null);
        }

        itemId = itemId.replace("[NS]", baseItemId.getNamespace());
        itemId = TEMPLATE_PATH_PATTERN.matcher(itemId).replaceAll(m ->
        {
            if (m.group(1) != null && m.group(2) != null)
            {
                return baseItemId.getPath().replace(m.group(1), m.group(2));
            }
            return baseItemId.getPath();
        });

        return new Tuple<>(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemId)), itemId);
    }

    /**
     * Reports if this stack has a custom Tag value that is not purely a damage value.
     * @param stack the stack to inspect
     * @return      true if the stack has a non-damage tag value
     */
    public static boolean hasTag(@NotNull final ItemStack stack)
    {
        return stack.getComponents() != null && stack.getComponents().size() > (stack.isDamageableItem() ? 1 : 0);
    }

    /**
     * Obtains a list of all basic items in the game, plus any extra items present in the player's
     * inventory (allowing for items with custom NBT, e.g. DO blocks or dyed armour).
     *
     * @param player The player whose inventory to check.
     * @return The set of items.
     */
    public static Set<ItemStack> allItemsPlusInventory(@NotNull final Player player)
    {
        // get all known items first
        final Set<ItemStorage> allItems = new HashSet<>(IColonyManager.getInstance().getCompatibilityManager().getSetOfAllItems());

        // plus all items from the player's inventory not already listed (adds items with extra NBT)
        for (final ItemStack stack : player.getInventory().items)
        {
            if (stack.isEmpty())
            {
                continue;
            }

            final ItemStack pristine = stack.copy();
            pristine.setCount(1);
            if (stack.isDamageableItem() && stack.isDamaged())
            {
                pristine.setDamageValue(0);
                // in case the item wasn't already in the set, we want to only store a pristine one!
            }
            allItems.add(new ItemStorage(pristine, true));
        }

        return allItems.stream().map(ItemStorage::getItemStack).collect(Collectors.toSet());
    }

    /**
     * Consume food helper.
     *
     * @param foodStack   the itemstack of food.
     * @param citizen     the citizen entity.
     * @param inventory optional inventory to insert stack into if not citizen.
     */
    public static void consumeFood(final ItemStack foodStack, final AbstractEntityCitizen citizen, final Inventory inventory)
    {
        final ICitizenData citizenData = citizen.getCitizenData();
        ItemStack itemUseReturn = foodStack.finishUsingItem(citizen.level(), citizen);
        final double satIncrease = FoodUtils.getFoodValue(foodStack, citizen);

        citizenData.increaseSaturation(satIncrease);

        // Special handling for these as those are stackable + have a return per item.
        if (foodStack.getItem() instanceof HoneyBottleItem)
        {
            itemUseReturn = new ItemStack(Items.GLASS_BOTTLE);
        }

        if (!itemUseReturn.isEmpty() && itemUseReturn.getItem() != foodStack.getItem())
        {
            if (citizenData.getInventory().isFull() || (inventory != null && !inventory.add(itemUseReturn)))
            {
                InventoryUtils.spawnItemStack(
                  citizen.level(),
                  citizen.getX(),
                  citizen.getY(),
                  citizen.getZ(),
                  itemUseReturn
                );
            }
            else
            {
                InventoryUtils.addItemStackToItemHandler(citizenData.getInventory(), itemUseReturn);
            }
        }

        IColony citizenColony = citizen.getCitizenColonyHandler().getColony();
        if (citizenColony != null)
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(citizenColony, playerMP -> AdvancementTriggers.CITIZEN_EAT_FOOD.get().trigger(playerMP, foodStack));
        }
        citizenData.markDirty(60);
    }

    /**
     * Given an {@link Ingredient}, tries to produce a reasonable friendly UI name for its contents.
     * @param ingredient the ingredient to check.
     * @return the friendly name.
     */
    @OnlyIn(Dist.CLIENT)
    public static Component getTranslatedName(@NotNull final SizedIngredient ingredient)
    {
        if (ingredient.ingredient().hasNoItems())
        {
            return Component.empty();
        }

        final ItemStack[] items = ingredient.getItems();
        final Optional<TagKey<Item>> tag = getTagEquivalent(items);

        return Component.translatable("%sx %s", ingredient.count(), tag.map(t ->
        {
            final String key = "com.minecolonies.coremod.research.tags." + t.location();
            return (Component) (I18n.exists(key)
                    ? Component.translatable(key)
                    : Component.translatable("com.minecolonies.coremod.research.tags.other", t.location().toString()));
        }).orElseGet(() ->
        {
            if (items.length == 1)
            {
                return items[0].getItem().getDescription();
            }
            return Component.translatable(String.join("/", Collections.nCopies(items.length, "%s")),
                    Arrays.stream(items).map(ItemStack::getItem).map(Item::getDescription).toArray());
        }));
    }

    /**
     * Attempts to find a tag that exactly matches the given list of item stacks.
     * @param stacks a list of item stacks.
     * @return a tag that seems to match it, if found.
     */
    public static Optional<TagKey<Item>> getTagEquivalent(@NotNull final ItemStack[] stacks)
    {
        final List<Item> values = Arrays.stream(stacks)
                .map(ItemStack::getItem)
                .toList();

        if (values.size() <= 1)
        {
            return Optional.empty();
        }

        return BuiltInRegistries.ITEM.getTags()
                .filter(e ->
                {
                    HolderSet.Named<Item> tag = e.getSecond();
                    return areEquivalent(tag, values);
                })
                .map(Pair::getFirst)
                .findFirst();
    }

    private static boolean areEquivalent(@NotNull final HolderSet.Named<Item> tag, @NotNull final List<Item> values)
    {
        final int count = tag.size();
        if (count != values.size())
        {
            return false;
        }
        for (int i = 0; i < count; i++)
        {
            final Item tagValue = tag.get(i).value();
            final Item value = values.get(i);
            if (!value.equals(tagValue))
            {
                return false;
            }
        }
        return true;
    }
}

