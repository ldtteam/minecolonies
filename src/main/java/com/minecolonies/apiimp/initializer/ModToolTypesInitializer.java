package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.items.ModToolTypes;
import com.minecolonies.api.items.registry.ToolTypeEntry;
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
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModToolTypesInitializer {
    public final static DeferredRegister<ToolTypeEntry> DEFERRED_REGISTER = DeferredRegister.create(
            new ResourceLocation(Constants.MOD_ID, "tooltypes"), Constants.MOD_ID);

    private ModToolTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModToolTypesInitializer but this is a Utility class.");
    }

    static {
        ModToolTypes.none = register(ModToolTypes.NONE_ID, () -> new ToolTypeEntry.Builder()
                .setName("")
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE))
                .setToolLevel(itemStack -> itemStack == null ? 0 : 1)
                .build());

        ModToolTypes.pickaxe = register(ModToolTypes.PICKAXE_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.PICKAXE_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE))
                .setIsTool(itemStack -> itemStack.canPerformAction(ToolActions.PICKAXE_DIG))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.PICKAXE_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersTool(itemStack, toolId))
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

        ModToolTypes.shovel = register(ModToolTypes.SHOVEL_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.SHOVEL_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL))
                .setIsTool(itemStack -> itemStack.canPerformAction(ToolActions.SHOVEL_DIG))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.SHOVEL_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersTool(itemStack, toolId))
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

        ModToolTypes.axe = register(ModToolTypes.AXE_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.AXE_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE))
                .setIsTool(itemStack -> itemStack.canPerformAction(ToolActions.AXE_DIG))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.AXE_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersTool(itemStack, toolId))
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

        ModToolTypes.hoe = register(ModToolTypes.HOE_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.HOE_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE))
                .setIsTool(itemStack -> {
                    for (final ToolAction action : ToolActions.DEFAULT_HOE_ACTIONS)
                    {
                        if (!itemStack.canPerformAction(action))
                        {
                            return false;
                        }
                    }
                    return true;
                })
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.HOE_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersTool(itemStack, toolId))
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

        ModToolTypes.sword = register(ModToolTypes.SWORD_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.SWORD_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD))
                .setIsTool(itemStack ->  itemStack.canPerformAction(ToolActions.SWORD_SWEEP) || Compatibility.isTinkersWeapon(itemStack))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.SWORD_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersWeapon(itemStack))
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

        ModToolTypes.bow = register(ModToolTypes.BOW_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.BOW_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW))
                .setIsTool(itemStack -> itemStack.getItem() instanceof BowItem)
                .setToolLevel(itemStack -> {
                    if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 1;
                })
                .build());

        ModToolTypes.fishing_rod = register(ModToolTypes.FISHING_ROD_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.FISHING_ROD_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD))
                .setIsTool(itemStack -> itemStack.canPerformAction(ToolActions.FISHING_ROD_CAST))
                .setToolLevel(itemStack -> {
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
                })
                .build());

        ModToolTypes.shears = register(ModToolTypes.SHEARS_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.SHEARS_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS))
                .setIsTool(itemStack ->  itemStack.canPerformAction(ToolActions.SHEARS_DIG) && itemStack.canPerformAction(ToolActions.SHEARS_HARVEST))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.SHEARS_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersTool(itemStack, toolId))
                    {
                        return Compatibility.getToolLevel(itemStack);
                    }
                    else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 0;
                })
                .build());

        ModToolTypes.shield = register(ModToolTypes.SHIELD_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.SHIELD_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ShieldItem)
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.SHIELD_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersTool(itemStack, toolId))
                    {
                        return Compatibility.getToolLevel(itemStack);
                    }
                    else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 1;
                })
                .build());

        ModToolTypes.helmet = register(ModToolTypes.HELMET_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.HELMET_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot()))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.HELMET_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (itemStack.getItem() instanceof final ArmorItem armorItem)
                    {
                        return ItemStackUtils.getArmorLevel(armorItem.getMaterial());
                    }
                    else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 1;
                })
                .build());

        ModToolTypes.leggings = register(ModToolTypes.LEGGINGS_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.LEGGINGS_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot()))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.LEGGINGS_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (itemStack.getItem() instanceof final ArmorItem armorItem)
                    {
                        return ItemStackUtils.getArmorLevel(armorItem.getMaterial());
                    }
                    else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 1;
                })
                .build());

        ModToolTypes.chestplate = register(ModToolTypes.CHESTPLATE_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.CHESTPLATE_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot()))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.CHESTPLATE_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (itemStack.getItem() instanceof final ArmorItem armorItem)
                    {
                        return ItemStackUtils.getArmorLevel(armorItem.getMaterial());
                    }
                    else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 1;
                })
                .build());

        ModToolTypes.boots = register(ModToolTypes.BOOTS_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.BOOTS_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot()))
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.BOOTS_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (itemStack.getItem() instanceof final ArmorItem armorItem)
                    {
                        return ItemStackUtils.getArmorLevel(armorItem.getMaterial());
                    }
                    else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 1;
                })
                .build());

        ModToolTypes.flint_and_steel = register(ModToolTypes.FLINT_AND_STEEL_ID, () -> new ToolTypeEntry.Builder()
                .setName(ModToolTypes.FLINT_AND_STEEL_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER))
                .setIsTool(itemStack -> itemStack.getItem() instanceof FlintAndSteelItem)
                .setToolLevel(itemStack -> {
                    final String toolId = ModToolTypes.FLINT_AND_STEEL_ID;
                    if (!Compatibility.getMiningLevelCompatibility(itemStack, toolId))
                    {
                        return -1;
                    }
                    else if (Compatibility.isTinkersTool(itemStack, toolId))
                    {
                        return Compatibility.getToolLevel(itemStack);
                    }
                    else if (itemStack.getItem() instanceof final TieredItem tieredItem)  // most tools
                    {
                        return tieredItem.getTier().getLevel();
                    }
                    return 1;
                })
                .build());
    }

    private static RegistryObject<ToolTypeEntry> register(final String path, final Supplier<ToolTypeEntry> supplier)
    {
        ModToolTypes.toolTypes.add(new ResourceLocation(Constants.MOD_ID, path));
        return DEFERRED_REGISTER.register(path, supplier);
    }
}
