package com.minecolonies.api.util;

import com.google.common.collect.Lists;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.happiness.ExpirationBasedHappinessModifier;
import com.minecolonies.api.entity.citizen.happiness.StaticHappinessSupplier;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.items.CheckedNbtKey;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.core.items.ItemBowlFood;
import com.minecolonies.core.util.AdvancementUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.minecolonies.api.items.ModTags.fungi;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.HappinessConstants.HADGREATFOOD;

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
     * List of the checked nbt sets for itemstack comparisons.
     */
    public static HashMap<Item, Set<CheckedNbtKey>> CHECKED_NBT_KEYS = new HashMap<>();

    /**
     * True if this stack is a standard food item (has at least some healing and some saturation, not purely for effects).
     */
    public static final Predicate<ItemStack> ISFOOD =
      stack ->
      {
          final FoodProperties foodProperties = stack.isEdible() ? stack.getFoodProperties(null) : null;
          return ItemStackUtils.isNotEmpty(stack) && foodProperties != null && foodProperties.getNutrition() > 0
                     && foodProperties.getSaturationModifier() > 0 && !stack.is(ModTags.excludedFood);
      };

    /**
     * Predicate describing things which work in the furnace.
     */
    public static Predicate<ItemStack> IS_SMELTABLE;

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
     * Verifies if there is equipment with an acceptable level in a worker's inventory.
     *
     * @param stack        the stack to test.
     * @param equipmentType     the type of equipment needed
     * @param minimalLevel the minimum level for the equipment to find.
     * @param maximumLevel the maximum level for the equipment to find.
     * @return true if equipment is acceptable
     */
    public static boolean hasEquipmentLevel(@Nullable final ItemStack stack, final EquipmentTypeEntry equipmentType, final int minimalLevel, final int maximumLevel)
    {
        if (isEmpty(stack))
        {
            return false;
        }

        return equipmentType.checkIsEquipment(stack) && verifyEquipmentLevel(stack, equipmentType.getMiningLevel(stack), minimalLevel, maximumLevel);
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
     * Verifies if an item has an appropriated grade.
     *
     * @param itemStack    the equipment
     * @param equipmentLevel    the equipment level
     * @param minimalLevel the minimum level needed
     * @param maximumLevel the maximum level needed (usually the worker's hut level)
     * @return true if equipment is acceptable
     */
    public static boolean verifyEquipmentLevel(@NotNull final ItemStack itemStack, final int equipmentLevel, final int minimalLevel, final int maximumLevel)
    {
        if (equipmentLevel < minimalLevel)
        {
            return false;
        }
        return (equipmentLevel + getMaxEnchantmentLevel(itemStack) <= maximumLevel);
    }

    /**
     * Calculates the max level enchantment this equipment has.
     *
     * @param itemStack the equipment to check.
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
            final ListTag ListNBT = itemStack.getEnchantmentTags();

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
     * This routine converts the material type of armor into a numerical value for the request system.
     *
     * @param material type of material of the armor
     * @return armor level
     */
    public static int getArmorLevel(final ArmorMaterial material)
    {
        final float armorLevel = getArmorValue(material);

        if (armorLevel <= getArmorValue(ArmorMaterials.LEATHER))
        {
            return 0;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.GOLD))
        {
            return 1;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.CHAIN))
        {
            return 2;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.IRON))
        {
            return 3;
        }
        else if (armorLevel <= getArmorValue(ArmorMaterials.DIAMOND))
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
            value += material.getDefenseForType(type);
        }
        return value + material.getToughness() * 4;
    }

    /**
     * Check if the first stack is better equipment than the second stack.
     *
     * @param stack1 the first stack to check.
     * @param stack2 the second to compare with.
     * @return true if better, false if worse or either of them are not equipment.
     */
    public static boolean isBetterEquipment(final ItemStack stack1, final ItemStack stack2)
    {
        for (EquipmentTypeEntry equipmentType : ModEquipmentTypes.getRegistry())
        {
            if (equipmentType.checkIsEquipment(stack1) && equipmentType.checkIsEquipment(stack2) && equipmentType.getMiningLevel(stack1) > equipmentType.getMiningLevel(stack2))
            {
                return true;
            }
        }
        return false;
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
            final ListTag t = tool.getEnchantmentTags();

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
            return Component.translatable("com.minecolonies.coremod.armorlevel." + toolGrade);
        }

        // this shouldn't really ever happen, but just in case...
        return Component.translatable("com.minecolonies.coremod.armorlevel.etc");
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
            return Component.translatable("com.minecolonies.coremod.toollevel." + toolGrade);
        }

        return Component.translatable("com.minecolonies.coremod.toollevel.etc");
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

            if (itemStack1.hasTag() || itemStack2.hasTag())
            {
                if (matchNBTExactly)
                {
                    return Objects.equals(itemStack1.getTag(), itemStack2.getTag());
                }
                final Set<CheckedNbtKey> checkedKeys = CHECKED_NBT_KEYS.getOrDefault(itemStack1.getItem(), null);
                if (checkedKeys == null)
                {
                    return itemStack1.hasTag() && itemStack2.hasTag() && itemStack1.getTag().equals(itemStack2.getTag());
                }
                if (itemStack1.hasTag() != itemStack2.hasTag() && !checkedKeys.isEmpty())
                {
                    return false;
                }

                if (checkedKeys.isEmpty())
                {
                    return true;
                }

                CompoundTag nbt1 = itemStack1.getTag();
                CompoundTag nbt2 = itemStack2.getTag();

                for (final CheckedNbtKey key : checkedKeys)
                {
                    if (!key.matches(nbt1, nbt2))
                    {
                        return false;
                    }
                }
            }
            return true;
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
    public static ItemStack deserializeFromNBT(@NotNull final CompoundTag compound)
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
        if (split.length != 2)
        {
            if (split.length == 1)
            {
                final String[] tempArray = {"minecraft", split[0]};
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
                stack.setTag(TagParser.parseTag(tag));
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
     * Parses an item string (formatted for {@link #idToItemStack}) that may
     * contain replaceable template components:
     *
     * <pre>
     *     [NS]           => {@code baseItemId.getNamespace()}
     *     [PATH]         => {@code baseItemId.getPath()}
     *     [PATH:foo=bar] => {@code baseItemId.getPath()} but with "foo" replaced with "bar"</pre>
     *
     * @param value      the value to parse
     * @param baseItemId the base item id to use to fill in the components
     * @return a tuple of (boolean, result), where the boolean is false if result didn't resolve to a valid item
     */
    @NotNull
    public static Tuple<Boolean, String> parseIdTemplate(@Nullable final String value,
                                                         @NotNull final ResourceLocation baseItemId)
    {
        if (value == null)
        {
            return new Tuple<>(false, null);
        }

        final int nbtIndex = value.indexOf('{');
        String itemId = nbtIndex < 0 ? value : value.substring(0, nbtIndex);

        itemId = itemId.replace("[NS]", baseItemId.getNamespace());
        itemId = TEMPLATE_PATH_PATTERN.matcher(itemId).replaceAll(m ->
        {
            if (m.group(1) != null && m.group(2) != null)
            {
                return baseItemId.getPath().replace(m.group(1), m.group(2));
            }
            return baseItemId.getPath();
        });

        return new Tuple<>(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemId)),
                itemId + (nbtIndex >= 0 ? value.substring(nbtIndex) : ""));
    }

    /**
     * Reports if this stack has a custom Tag value that is not purely a damage value.
     * @param stack the stack to inspect
     * @return      true if the stack has a non-damage tag value
     */
    public static boolean hasTag(@NotNull final ItemStack stack)
    {
        return stack.getTag() != null && stack.getTag().size() > (stack.isDamageableItem() ? 1 : 0);
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
        else if (foodStack.getItem() instanceof ItemBowlFood)
        {
            itemUseReturn = new ItemStack(Items.BOWL);
        }

        if (!itemUseReturn.isEmpty() && itemUseReturn.getItem() != foodStack.getItem())
        {
            if (citizenData.getInventory().isFull() || (inventory != null && !inventory.add(itemUseReturn)))
            {
                InventoryUtils.spawnItemStack(
                  citizen.level,
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

        if (foodStack.getItem() instanceof IMinecoloniesFoodItem foodItem && foodItem.getTier() >= 3)
        {
            citizen.getCitizenData().getCitizenHappinessHandler().addModifier(new ExpirationBasedHappinessModifier(HADGREATFOOD, 2.0, new StaticHappinessSupplier(2.0), 5));
        }

        IColony citizenColony = citizen.getCitizenColonyHandler().getColonyOrRegister();
        if (citizenColony != null)
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(citizenColony, playerMP -> AdvancementTriggers.CITIZEN_EAT_FOOD.trigger(playerMP, foodStack));
        }
        citizenData.markDirty(60);
    }
}

