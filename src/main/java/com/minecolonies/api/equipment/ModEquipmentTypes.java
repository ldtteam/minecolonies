package com.minecolonies.api.equipment;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.equipment.registry.types.*;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
import java.util.function.Function;

/**
 * Class used for storing and registering any EquipmentTypes.
 */
public class ModEquipmentTypes
{
    public static final DeferredRegister<EquipmentTypeEntry> DEFERRED_REGISTER =
        DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "equipmenttypes"), Constants.MOD_ID);

    public static final RegistryObject<EquipmentTypeEntry> pickaxe;
    public static final RegistryObject<EquipmentTypeEntry> shovel;
    public static final RegistryObject<EquipmentTypeEntry> axe;
    public static final RegistryObject<EquipmentTypeEntry> hoe;
    public static final RegistryObject<EquipmentTypeEntry> sword;
    public static final RegistryObject<EquipmentTypeEntry> bow;
    public static final RegistryObject<EquipmentTypeEntry> crossbow;
    public static final RegistryObject<EquipmentTypeEntry> fishing_rod;
    public static final RegistryObject<EquipmentTypeEntry> shears;
    public static final RegistryObject<EquipmentTypeEntry> shield;
    public static final RegistryObject<EquipmentTypeEntry> helmet;
    public static final RegistryObject<EquipmentTypeEntry> leggings;
    public static final RegistryObject<EquipmentTypeEntry> chestplate;
    public static final RegistryObject<EquipmentTypeEntry> boots;
    public static final RegistryObject<EquipmentTypeEntry> flint_and_steel;
    public static final RegistryObject<EquipmentTypeEntry> lead;
    public static final RegistryObject<EquipmentTypeEntry> spear;
    static
    {
        pickaxe = register("pickaxe",
            name -> new DiggerEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE),
                ToolActions.DEFAULT_PICKAXE_ACTIONS,
                BlockTags.MINEABLE_WITH_PICKAXE));

        shovel = register("shovel",
            name -> new DiggerEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL),
                ToolActions.DEFAULT_SHOVEL_ACTIONS,
                BlockTags.MINEABLE_WITH_SHOVEL));

        axe = register("axe",
            name -> new DiggerEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE),
                ToolActions.DEFAULT_AXE_ACTIONS,
                BlockTags.MINEABLE_WITH_AXE));

        hoe = register("hoe",
            name -> new DiggerEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE),
                ToolActions.DEFAULT_HOE_ACTIONS,
                BlockTags.MINEABLE_WITH_HOE));

        sword = register("sword", name -> new DiggerEquipmentTypeEntry(name, Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD), ToolActions.DEFAULT_SWORD_ACTIONS));

        bow = register("bow",
            name -> new DurabilityEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW),
                itemStack -> itemStack.getItem() instanceof BowItem,
                Items.BOW.getMaxDamage()));

        crossbow = register("crossbow",
            name -> new DurabilityEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_CROSSBOW),
                itemStack -> itemStack.getItem() instanceof CrossbowItem,
                Items.CROSSBOW.getMaxDamage()));

        fishing_rod = register("rod",
            name -> new DurabilityEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD),
                itemStack -> ModEquipmentTypes.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_FISHING_ROD_ACTIONS),
                Items.FISHING_ROD.getMaxDamage()));

        shears = register("shears",
            name -> new DurabilityEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS),
                itemStack -> ModEquipmentTypes.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHEARS_ACTIONS),
                Items.SHEARS.getMaxDamage()));

        shield = register("shield",
            name -> new DurabilityEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD),
                itemStack -> ModEquipmentTypes.canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHIELD_ACTIONS),
                Items.SHIELD.getMaxDamage()));

        helmet = register("helmet", name -> new ArmorEquipmentTypeEntry(name, Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET), EquipmentSlot.HEAD));

        leggings = register("leggings", name -> new ArmorEquipmentTypeEntry(name, Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS), EquipmentSlot.LEGS));

        chestplate = register("chestplate", name -> new ArmorEquipmentTypeEntry(name, Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE), EquipmentSlot.CHEST));

        boots = register("boots", name -> new ArmorEquipmentTypeEntry(name, Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS), EquipmentSlot.FEET));

        flint_and_steel = register("flintandsteel",
            name -> new DurabilityEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER),
                itemStack -> itemStack.getItem() instanceof FlintAndSteelItem,
                Items.FLINT_AND_STEEL.getMaxDamage()));

        lead = register("lead", name -> new SimpleItemEquipmentTypeEntry(name, Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEAD), Items.LEAD, 1));

        spear = register("spear",
            name -> new DurabilityEquipmentTypeEntry(name,
                Component.translatable(ToolTranslationConstants.TOOL_TYPE_SPEAR),
                itemStack -> itemStack.is(ModItems.spear),
                ModItems.spear.getMaxDamage()));
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
     * @param id      The unique ID of the equipment type
     * @param factory a factory constructing the equipment type given its registry name
     * @return The registry entry
     */
    private static RegistryObject<EquipmentTypeEntry> register(final String id, final Function<ResourceLocation, EquipmentTypeEntry> factory)
    {
        return DEFERRED_REGISTER.register(id, () -> factory.apply(new ResourceLocation(Constants.MOD_ID, id)));
    }

    /**
     * Determine whether an item stack can perform the default actions of a given tool.
     *
     * @param itemStack The item stack to check
     * @param actions   The set of actions to compare
     * @return Whether the item stack can perform the actions
     */
    private static boolean canPerformDefaultActions(ItemStack itemStack, Set<ToolAction> actions)
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
