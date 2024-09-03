package com.minecolonies.api.items;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.items.registry.ToolTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModToolTypes {
    public final static DeferredRegister<ToolTypeEntry> DEFERRED_REGISTER = DeferredRegister.create(
            new ResourceLocation(Constants.MOD_ID, "tooltypes"), Constants.MOD_ID);

    public static String NONE_ID = "none";
    public static String PICKAXE_ID = "pickaxe";
    public static String SHOVEL_ID = "shovel";
    public static String AXE_ID = "axe";
    public static String HOE_ID = "hoe";
    public static String SWORD_ID = "sword";
    public static String BOW_ID = "bow";
    public static String FISHING_ROD_ID = "rod";
    public static String SHEARS_ID = "shears";
    public static String SHIELD_ID = "shield";
    public static String HELMET_ID = "helmet";
    public static String LEGGINGS_ID = "leggings";
    public static String CHESTPLATE_ID = "chestplate";
    public static String BOOTS_ID = "boots";
    public static String FLINT_AND_STEEL_ID = "flintandsteel";

    public static RegistryObject<ToolTypeEntry> none;
    public static RegistryObject<ToolTypeEntry> pickaxe;
    public static RegistryObject<ToolTypeEntry> shovel;
    public static RegistryObject<ToolTypeEntry> axe;
    public static RegistryObject<ToolTypeEntry> hoe;
    public static RegistryObject<ToolTypeEntry> sword;
    public static RegistryObject<ToolTypeEntry> bow;
    public static RegistryObject<ToolTypeEntry> fishing_rod;
    public static RegistryObject<ToolTypeEntry> shears;
    public static RegistryObject<ToolTypeEntry> shield;
    public static RegistryObject<ToolTypeEntry> helmet;
    public static RegistryObject<ToolTypeEntry> leggings;
    public static RegistryObject<ToolTypeEntry> chestplate;
    public static RegistryObject<ToolTypeEntry> boots;
    public static RegistryObject<ToolTypeEntry> flint_and_steel;

    public static List<RegistryObject<ToolTypeEntry>> toolTypes = new ArrayList<>();

    private static RegistryObject<ToolTypeEntry> register(final String id, final Supplier<ToolTypeEntry> supplier)
    {
        RegistryObject<ToolTypeEntry> entry = DEFERRED_REGISTER.register(id, supplier);
        toolTypes.add(entry);
        return entry;
    }

    static {
        none = register(NONE_ID, () -> new ToolTypeEntry.Builder()
                .setName("")
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE))
                .setIsTool(itemStack -> true)
                .setToolLevel(itemStack -> itemStack == null ? 0 : 1)
                .build());

        pickaxe = register(PICKAXE_ID, () -> new ToolTypeEntry.Builder()
                .setName(PICKAXE_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_PICKAXE_ACTIONS) || Compatibility.isTinkersTool(itemStack, PICKAXE_ID))
                .setToolLevel(itemStack -> ItemStackUtils.vanillaToolLevel(PICKAXE_ID, itemStack))
                .build());

        shovel = register(SHOVEL_ID, () -> new ToolTypeEntry.Builder()
                .setName(SHOVEL_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHOVEL_ACTIONS) || Compatibility.isTinkersTool(itemStack, SHOVEL_ID))
                .setToolLevel(itemStack -> ItemStackUtils.vanillaToolLevel(SHOVEL_ID, itemStack))
                .build());

        axe = register(AXE_ID, () -> new ToolTypeEntry.Builder()
                .setName(AXE_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_AXE_ACTIONS) || Compatibility.isTinkersTool(itemStack, AXE_ID))
                .setToolLevel(itemStack -> ItemStackUtils.vanillaToolLevel(AXE_ID, itemStack))
                .build());

        hoe = register(HOE_ID, () -> new ToolTypeEntry.Builder()
                .setName(HOE_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_HOE_ACTIONS) || Compatibility.isTinkersTool(itemStack, HOE_ID))
                .setToolLevel(itemStack -> ItemStackUtils.vanillaToolLevel(HOE_ID, itemStack))
                .build());

        sword = register(SWORD_ID, () -> new ToolTypeEntry.Builder()
                .setName(SWORD_ID)
                .setVariableMaterials(true)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SWORD_ACTIONS)  || Compatibility.isTinkersWeapon(itemStack))
                .setToolLevel(itemStack -> {
                    final String toolId = SWORD_ID;
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

        bow = register(BOW_ID, () -> new ToolTypeEntry.Builder()
                .setName(BOW_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW))
                .setIsTool(itemStack -> itemStack.getItem() instanceof BowItem)
                .setToolLevel(itemStack -> 1)
                .build());

        fishing_rod = register(FISHING_ROD_ID, () -> new ToolTypeEntry.Builder()
                .setName(FISHING_ROD_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_FISHING_ROD_ACTIONS))
                .setToolLevel(itemStack -> ItemStackUtils.durabilityBasedLevel(itemStack, Items.FISHING_ROD.getMaxDamage()))
                .build());

        shears = register(SHEARS_ID, () -> new ToolTypeEntry.Builder()
                .setName(SHEARS_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHEARS_ACTIONS))
                .setToolLevel(itemStack -> ItemStackUtils.durabilityBasedLevel(itemStack, Items.SHEARS.getMaxDamage()))
                .build());

        shield = register(SHIELD_ID, () -> new ToolTypeEntry.Builder()
                .setName(SHIELD_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD))
                .setIsTool(itemStack -> ItemStackUtils.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHIELD_ACTIONS))
                .setToolLevel(itemStack -> ItemStackUtils.durabilityBasedLevel(itemStack, Items.SHIELD.getMaxDamage()))
                .build());

        helmet = register(HELMET_ID, () -> new ToolTypeEntry.Builder()
                .setName(HELMET_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot()))
                .setToolLevel(ItemStackUtils::armorLevel)
                .build());

        leggings = register(LEGGINGS_ID, () -> new ToolTypeEntry.Builder()
                .setName(LEGGINGS_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot()))
                .setToolLevel(ItemStackUtils::armorLevel)
                .build());

        chestplate = register(CHESTPLATE_ID, () -> new ToolTypeEntry.Builder()
                .setName(CHESTPLATE_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot()))
                .setToolLevel(ItemStackUtils::armorLevel)
                .build());

        boots = register(BOOTS_ID, () -> new ToolTypeEntry.Builder()
                .setName(BOOTS_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS))
                .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot()))
                .setToolLevel(ItemStackUtils::armorLevel)
                .build());

        flint_and_steel = register(FLINT_AND_STEEL_ID, () -> new ToolTypeEntry.Builder()
                .setName(FLINT_AND_STEEL_ID)
                .setVariableMaterials(false)
                .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER))
                .setIsTool(itemStack -> itemStack.getItem() instanceof FlintAndSteelItem)
                .setToolLevel(itemStack -> ItemStackUtils.durabilityBasedLevel(itemStack, Items.FLINT_AND_STEEL.getMaxDamage()))
                .build());
    }
}
