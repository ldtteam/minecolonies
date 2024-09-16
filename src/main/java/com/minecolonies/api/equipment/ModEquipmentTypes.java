package com.minecolonies.api.equipment;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Class used for storing and registering any EquipmentTypes.
 */
public class ModEquipmentTypes
{
    public final static DeferredRegister<EquipmentTypeEntry> DEFERRED_REGISTER = DeferredRegister.create(CommonMinecoloniesAPIImpl.EQUIPMENT_TYPES, Constants.MOD_ID);

    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> none;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> pickaxe;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> shovel;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> axe;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> hoe;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> sword;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> bow;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> fishing_rod;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> shears;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> shield;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> helmet;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> leggings;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> chestplate;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> boots;
    public static final DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> flint_and_steel;

    static
    {
        none = register("none",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE))
                       .setIsEquipment((itemStack, equipmentType) -> true)
                       .setEquipmentLevel((itemStack, equipmentType) -> -1)
                   .build());

        pickaxe = register("pickaxe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_PICKAXE_ACTIONS) || Compatibility.isTinkersTool(
                         itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        shovel = register("shovel",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_SHOVEL_ACTIONS) || Compatibility.isTinkersTool(
                         itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        axe = register("axe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_AXE_ACTIONS) || Compatibility.isTinkersTool(itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        hoe = register("hoe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_HOE_ACTIONS) || Compatibility.isTinkersTool(itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        sword = register("sword",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_SWORD_ACTIONS) || Compatibility.isTinkersWeapon(
                         itemStack))
                       .setEquipmentLevel((itemStack, equipmentType) -> {
                      if (Compatibility.isTinkersWeapon(itemStack))
                      {
                          return Compatibility.getToolLevel(itemStack);
                      }
                      else if (itemStack.getItem() instanceof final TieredItem tieredItem)
                      {
                          // todo: Think about sth smarter in the future
                          return (int) tieredItem.getTier().getAttackDamageBonus();
                      }
                      return -1;
                  })
                  .build());

        bow = register("bow",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof BowItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.BOW.getMaxDamage(itemStack)))
                  .build());

        fishing_rod = register("rod",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_FISHING_ROD_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.FISHING_ROD.getMaxDamage(itemStack)))
                  .build());

        shears = register("shears",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_SHEARS_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.SHEARS.getMaxDamage(itemStack)))
                  .build());

        shield = register("shield",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ItemAbilities.DEFAULT_SHIELD_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.SHIELD.getMaxDamage(itemStack)))
                  .build());

        helmet = register("helmet",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> ModEquipmentTypes.armorLevel(itemStack))
                  .build());

        leggings = register("leggings",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> ModEquipmentTypes.armorLevel(itemStack))
                  .build());

        chestplate = register("chestplate",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> ModEquipmentTypes.armorLevel(itemStack))
                  .build());

        boots = register("boots",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> ModEquipmentTypes.armorLevel(itemStack))
                  .build());

        flint_and_steel = register("flintandsteel",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof FlintAndSteelItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.FLINT_AND_STEEL.getMaxDamage(itemStack)))
                  .build());
    }

    /**
     * Get the equipmentType registry.
     *
     * @return The equipmentType registry
     */
    public static Registry<EquipmentTypeEntry> getRegistry()
    {
        return IMinecoloniesAPI.getInstance().getEquipmentTypeRegistry();
    }

    /**
     * Register a new equipmentType to the registry.
     *
     * @param id The unique ID of the equipment type
     * @param consumer The consumer that builds the equipment type
     * @return The registry entry
     */
    private static DeferredHolder<EquipmentTypeEntry, EquipmentTypeEntry> register(final String id, final Consumer<EquipmentTypeEntry.Builder> consumer)
    {
        EquipmentTypeEntry.Builder equipmentType = new EquipmentTypeEntry.Builder()
                                           .setRegistryName(new ResourceLocation(Constants.MOD_ID, id));
        consumer.accept(equipmentType);
        return DEFERRED_REGISTER.register(id, equipmentType::build);
    }

    /**
     * Get the equipment level for vanilla tools.
     *
     * @param equipmentType  The type of vanilla tool
     * @param itemStack The item stack to check
     * @return The tool level
     */
    public static int vanillaToolLevel(final ItemStack itemStack, final EquipmentTypeEntry equipmentType)
    {
        if (Compatibility.isTinkersTool(itemStack, equipmentType))
        {
            return Compatibility.getToolLevel(itemStack);
        }
        else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
        {
            // todo: Think about sth smarter in the future
            return (int) tieredItem.getTier().getAttackDamageBonus();
        }
        return -1;
    }

    /**
     * Get the armor level.
     *
     * @param itemStack The item stack to check
     * @return The armor level
     */
    public static int armorLevel(ItemStack itemStack)
    {
        if (itemStack.getItem() instanceof final ArmorItem armorItem)
        {
            return ItemStackUtils.getArmorLevel(armorItem.getMaterial().value());
        }
        return -1;
    }

    /**
     * Get the durability based item level.
     *
     * @param itemStack The item stack to check
     * @return The item level
     */
    public static int durabilityBasedLevel(ItemStack itemStack, int vanillaItemDurability)
    {
        if (!itemStack.isDamageableItem())
        {
            return 5;
        }

        return Math.min(itemStack.getMaxDamage() / vanillaItemDurability, 5);
    }

    /**
     * Determine whether an item stack can perform the default actions of a given tool.
     *
     * @param itemStack The item stack to check
     * @param actions   The set of actions to compare
     * @return Whether the item stack can perform the actions
     */
    public static boolean canPerformDefaultActions(ItemStack itemStack, Set<ItemAbility> actions)
    {
        for (final ItemAbility toolAction : actions)
        {
            if (!itemStack.canPerformAction(toolAction))
            {
                return false;
            }
        }
        return true;
    }
}
