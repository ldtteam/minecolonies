package com.minecolonies.api.tools;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.tools.registry.ToolTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import io.netty.util.Constant;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Class used for storing and registering any ToolTypes.
 */
public class ModToolTypes
{
    public static final DeferredRegister<ToolTypeEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "tooltypes"), Constants.MOD_ID);

    public static final RegistryObject<ToolTypeEntry> none;
    public static final RegistryObject<ToolTypeEntry> pickaxe;
    public static final RegistryObject<ToolTypeEntry> shovel;
    public static final RegistryObject<ToolTypeEntry> axe;
    public static final RegistryObject<ToolTypeEntry> hoe;
    public static final RegistryObject<ToolTypeEntry> sword;
    public static final RegistryObject<ToolTypeEntry> bow;
    public static final RegistryObject<ToolTypeEntry> fishing_rod;
    public static final RegistryObject<ToolTypeEntry> shears;
    public static final RegistryObject<ToolTypeEntry> shield;
    public static final RegistryObject<ToolTypeEntry> helmet;
    public static final RegistryObject<ToolTypeEntry> leggings;
    public static final RegistryObject<ToolTypeEntry> chestplate;
    public static final RegistryObject<ToolTypeEntry> boots;
    public static final RegistryObject<ToolTypeEntry> flint_and_steel;

    static
    {
        none = register("none",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE))
                   .setIsTool((itemStack, toolType) -> true)
                   .setToolLevel((itemStack, toolType) -> -1)
                   .build());

        pickaxe = register("pickaxe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_PICKAXE_ACTIONS) || Compatibility.isTinkersTool(itemStack, toolType))
                  .setToolLevel((itemStack, toolType) -> vanillaToolLevel(toolType, itemStack))
                  .build());

        shovel = register("shovel",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHOVEL_ACTIONS) || Compatibility.isTinkersTool(itemStack, toolType))
                  .setToolLevel((itemStack, toolType) -> vanillaToolLevel(toolType, itemStack))
                  .build());

        axe = register("axe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_AXE_ACTIONS) || Compatibility.isTinkersTool(itemStack, toolType))
                  .setToolLevel((itemStack, toolType) -> vanillaToolLevel(toolType, itemStack))
                  .build());

        hoe = register("hoe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_HOE_ACTIONS) || Compatibility.isTinkersTool(itemStack, toolType))
                  .setToolLevel((itemStack, toolType) -> vanillaToolLevel(toolType, itemStack))
                  .build());

        sword = register("sword",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SWORD_ACTIONS) || Compatibility.isTinkersWeapon(itemStack))
                  .setToolLevel((itemStack, toolType) -> {
                      if (Compatibility.isTinkersWeapon(itemStack))
                      {
                          return Compatibility.getToolLevel(itemStack);
                      }
                      else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                      {
                          return tieredItem.getTier().getLevel();
                      }
                      return -1;
                  })
                  .build());

        bow = register("bow",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW))
                  .setIsTool((itemStack, toolType) -> itemStack.getItem() instanceof BowItem)
                  .setToolLevel((itemStack, toolType) -> durabilityBasedLevel(itemStack, Items.BOW.getMaxDamage()))
                  .build());

        fishing_rod = register("rod",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_FISHING_ROD_ACTIONS))
                  .setToolLevel((itemStack, toolType) -> durabilityBasedLevel(itemStack, Items.FISHING_ROD.getMaxDamage()))
                  .build());

        shears = register("shears",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHEARS_ACTIONS))
                  .setToolLevel((itemStack, toolType) -> durabilityBasedLevel(itemStack, Items.SHEARS.getMaxDamage()))
                  .build());

        shield = register("shield",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD))
                  .setIsTool((itemStack, toolType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHIELD_ACTIONS))
                  .setToolLevel((itemStack, toolType) -> durabilityBasedLevel(itemStack, Items.SHIELD.getMaxDamage()))
                  .build());

        helmet = register("helmet",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET))
                  .setIsTool((itemStack, toolType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot()))
                  .setToolLevel((itemStack, toolType) -> ModToolTypes.armorLevel(itemStack))
                  .build());

        leggings = register("leggings",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS))
                  .setIsTool((itemStack, toolType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot()))
                  .setToolLevel((itemStack, toolType) -> ModToolTypes.armorLevel(itemStack))
                  .build());

        chestplate = register("chestplate",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE))
                  .setIsTool((itemStack, toolType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot()))
                  .setToolLevel((itemStack, toolType) -> ModToolTypes.armorLevel(itemStack))
                  .build());

        boots = register("boots",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS))
                  .setIsTool((itemStack, toolType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot()))
                  .setToolLevel((itemStack, toolType) -> ModToolTypes.armorLevel(itemStack))
                  .build());

        flint_and_steel = register("flintandsteel",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER))
                  .setIsTool((itemStack, toolType) -> itemStack.getItem() instanceof FlintAndSteelItem)
                  .setToolLevel((itemStack, toolType) -> durabilityBasedLevel(itemStack, Items.FLINT_AND_STEEL.getMaxDamage()))
                  .build());
    }
    /**
     * Get the tooltype registry.
     * @return The tooltype registry
     */
    public static IForgeRegistry<ToolTypeEntry> getRegistry()
    {
        return IMinecoloniesAPI.getInstance().getToolTypeRegistry();
    }

    private static RegistryObject<ToolTypeEntry> register(final String id, final Consumer<ToolTypeEntry.Builder> consumer)
    {
        ToolTypeEntry.Builder toolType = new ToolTypeEntry.Builder()
                                           .setRegistryName(new ResourceLocation(Constants.MOD_ID, id));
        consumer.accept(toolType);
        return DEFERRED_REGISTER.register(id, toolType::build);
    }

    /**
     * Get the tool level for vanilla tools.
     *
     * @param toolType  The type of vanilla tool
     * @param itemStack The item stack to check
     * @return The tool level
     */
    public static int vanillaToolLevel(final ToolTypeEntry toolType, ItemStack itemStack)
    {
        if (Compatibility.isTinkersTool(itemStack, toolType))
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
     * Get the armor level.
     *
     * @param itemStack The item stack to check
     * @return The armor level
     */
    public static int armorLevel(ItemStack itemStack)
    {
        if (itemStack.getItem() instanceof final ArmorItem armorItem)
        {
            return ItemStackUtils.getArmorLevel(armorItem.getMaterial());
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
