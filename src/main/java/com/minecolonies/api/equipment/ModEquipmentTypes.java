package com.minecolonies.api.equipment;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Class used for storing and registering any EquipmentTypes.
 */
public class ModEquipmentTypes
{
    public static final DeferredRegister<EquipmentTypeEntry> DEFERRED_REGISTER =
      DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "equipmenttypes"), Constants.MOD_ID);

    public static final ResourceLocation NONE_ID = new ResourceLocation(Constants.MOD_ID, "none");
    public static final ResourceLocation PICKAXE_ID = new ResourceLocation(Constants.MOD_ID, "pickaxe");
    public static final ResourceLocation SHOVEL_ID = new ResourceLocation(Constants.MOD_ID, "shovel");
    public static final ResourceLocation AXE_ID = new ResourceLocation(Constants.MOD_ID, "axe");
    public static final ResourceLocation HOE_ID = new ResourceLocation(Constants.MOD_ID, "hoe");
    public static final ResourceLocation SWORD_ID = new ResourceLocation(Constants.MOD_ID, "sword");
    public static final ResourceLocation BOW_ID = new ResourceLocation(Constants.MOD_ID, "bow");
    public static final ResourceLocation FISHING_ROD_ID = new ResourceLocation(Constants.MOD_ID, "rod");
    public static final ResourceLocation SHEARS_ID = new ResourceLocation(Constants.MOD_ID, "shears");
    public static final ResourceLocation SHIELD_ID = new ResourceLocation(Constants.MOD_ID, "shield");
    public static final ResourceLocation HELMET_ID = new ResourceLocation(Constants.MOD_ID, "helmet");
    public static final ResourceLocation LEGGINGS_ID = new ResourceLocation(Constants.MOD_ID, "leggings");
    public static final ResourceLocation CHESTPLATE_ID = new ResourceLocation(Constants.MOD_ID, "chestplate");
    public static final ResourceLocation BOOTS_ID = new ResourceLocation(Constants.MOD_ID, "boots");
    public static final ResourceLocation FLINT_AND_STEEL_ID = new ResourceLocation(Constants.MOD_ID, "flintandsteel");

    public static final RegistryObject<EquipmentTypeEntry> none;
    public static final RegistryObject<EquipmentTypeEntry> pickaxe;
    public static final RegistryObject<EquipmentTypeEntry> shovel;
    public static final RegistryObject<EquipmentTypeEntry> axe;
    public static final RegistryObject<EquipmentTypeEntry> hoe;
    public static final RegistryObject<EquipmentTypeEntry> sword;
    public static final RegistryObject<EquipmentTypeEntry> bow;
    public static final RegistryObject<EquipmentTypeEntry> fishing_rod;
    public static final RegistryObject<EquipmentTypeEntry> shears;
    public static final RegistryObject<EquipmentTypeEntry> shield;
    public static final RegistryObject<EquipmentTypeEntry> helmet;
    public static final RegistryObject<EquipmentTypeEntry> leggings;
    public static final RegistryObject<EquipmentTypeEntry> chestplate;
    public static final RegistryObject<EquipmentTypeEntry> boots;
    public static final RegistryObject<EquipmentTypeEntry> flint_and_steel;
    public static final RegistryObject<EquipmentTypeEntry> lead;

    static
    {
        none = register(NONE_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE))
                       .setIsEquipment((itemStack, equipmentType) -> true)
                       .setEquipmentLevel((itemStack, equipmentType) -> -1)
                   .build());

        pickaxe = register(PICKAXE_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_PICKAXE_ACTIONS) || Compatibility.isTinkersTool(
                         itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        shovel = register(SHOVEL_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHOVEL_ACTIONS) || Compatibility.isTinkersTool(
                         itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        axe = register(AXE_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_AXE_ACTIONS) || Compatibility.isTinkersTool(itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        hoe = register(HOE_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_HOE_ACTIONS) || Compatibility.isTinkersTool(itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        sword = register(SWORD_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SWORD_ACTIONS) || Compatibility.isTinkersWeapon(
                         itemStack))
                       .setEquipmentLevel((itemStack, equipmentType) -> {
                      if (Compatibility.isTinkersWeapon(itemStack))
                      {
                          return Compatibility.getToolLevel(itemStack);
                      }
                      else if (itemStack.getItem() instanceof final TieredItem tieredItem)
                      {
                          return tieredItem.getTier().getLevel();
                      }
                      return -1;
                  })
                  .build());

        bow = register(BOW_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof BowItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.BOW.getMaxDamage()))
                  .build());

        fishing_rod = register(FISHING_ROD_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_FISHING_ROD_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.FISHING_ROD.getMaxDamage()))
                  .build());

        shears = register(SHEARS_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHEARS_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.SHEARS.getMaxDamage()))
                  .build());

        shield = register(SHIELD_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHIELD_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.SHIELD.getMaxDamage()))
                  .build());

        helmet = register(HELMET_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> ItemStackUtils.getArmorLevel(itemStack))
                  .build());

        leggings = register(LEGGINGS_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> ItemStackUtils.getArmorLevel(itemStack))
                  .build());

        chestplate = register(CHESTPLATE_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> ItemStackUtils.getArmorLevel(itemStack))
                  .build());

        boots = register(BOOTS_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) ->ItemStackUtils.getArmorLevel(itemStack))
                  .build());

        flint_and_steel = register(FLINT_AND_STEEL_ID,
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof FlintAndSteelItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.FLINT_AND_STEEL.getMaxDamage()))
                  .build());

        lead = register("lead",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEAD))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof LeadItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> -1)
                  .build());

    }

    /**
     * Get the equipmentType registry.
     *
     * @return The equipmentType registry
     */
    public static IForgeRegistry<EquipmentTypeEntry> getRegistry()
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
    private static RegistryObject<EquipmentTypeEntry> register(final ResourceLocation id, final Consumer<EquipmentTypeEntry.Builder> consumer)
    {
        EquipmentTypeEntry.Builder equipmentType = new EquipmentTypeEntry.Builder()
                                           .setRegistryName(id);
        consumer.accept(equipmentType);
        return DEFERRED_REGISTER.register(id.getPath(), equipmentType::build);
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
            return tieredItem.getTier().getLevel();
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
    public static boolean canPerformDefaultActions(ItemStack itemStack, Set<ToolAction> actions)
    {
        for (final ToolAction toolAction : actions)
        {
            if (!itemStack.canPerformAction(toolAction))
            {
                return false;
            }
        }
        return true;
    }
}
