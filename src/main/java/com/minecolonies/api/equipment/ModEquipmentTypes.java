package com.minecolonies.api.equipment;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public static final RegistryObject<EquipmentTypeEntry> spear;
    public static final RegistryObject<EquipmentTypeEntry> crossbow;

    static
    {
        none = register("none",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE))
                       .setIsEquipment((itemStack, equipmentType) -> true)
                       .setEquipmentLevel((itemStack, equipmentType) -> -1)
                   .build());

        pickaxe = register("pickaxe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_PICKAXE_ACTIONS) || Compatibility.isTinkersTool(
                         itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        shovel = register("shovel",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHOVEL_ACTIONS) || Compatibility.isTinkersTool(
                         itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        axe = register("axe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_AXE_ACTIONS) || Compatibility.isTinkersTool(itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        hoe = register("hoe",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_HOE_ACTIONS) || Compatibility.isTinkersTool(itemStack,
                         equipmentType))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        sword = register("sword",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SWORD_ACTIONS)
                                                                     || Compatibility.isTinkersWeapon(itemStack)
                                                                     || Compatibility.isCustomWeapon(itemStack))
                       .setEquipmentLevel(ModEquipmentTypes::vanillaToolLevel)
                  .build());

        bow = register("bow",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof BowItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        crossbow = register("crossbow",
            builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CROSSBOW))
                .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof CrossbowItem)
                .setEquipmentLevel((itemStack, equipmentType) -> durabilityBasedLevel(itemStack, Items.CROSSBOW.getMaxDamage()))
                .build());

        fishing_rod = register("rod",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_FISHING_ROD_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        shears = register("shears",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHEARS_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        shield = register("shield",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD))
                       .setIsEquipment((itemStack, equipmentType) -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHIELD_ACTIONS))
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        helmet = register("helmet",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        leggings = register("leggings",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        chestplate = register("chestplate",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        boots = register("boots",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot()))
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        flint_and_steel = register("flintandsteel",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof FlintAndSteelItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
                  .build());

        lead = register("lead",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEAD))
                       .setIsEquipment((itemStack, equipmentType) -> itemStack.getItem() instanceof LeadItem)
                       .setEquipmentLevel((itemStack, equipmentType) -> -1)
                  .build());

        spear = register("spear",
          builder -> builder.setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SPEAR))
                      .setIsEquipment((itemStack, equipmentType) -> itemStack.is(ModItems.spear))
                      .setEquipmentLevel((itemStack, equipmentType) -> Compatibility.getItemLevel(itemStack))
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
    private static RegistryObject<EquipmentTypeEntry> register(final String id, final Consumer<EquipmentTypeEntry.Builder> consumer)
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
        if (Compatibility.isTinkersTool(itemStack, equipmentType) || Compatibility.isTinkersWeapon(itemStack))
        {
            return Compatibility.getToolLevel(itemStack);
        }
        return Compatibility.getItemLevel(itemStack);
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
     * Populate the tier registry with every item currently in the game.
     * Called once during FMLCommonSetupEvent via MineColonies.preInit.
     */
    @SuppressWarnings("null")
    public static void initRegisterEquipmentTiers()
    {
        int bowRef     = 0;
        int rodRef     = 0;
        int shearsRef  = 0;
        int shieldRef  = 0;
        int flintRef   = 0;
        int tridentRef = 0;
        try
        {
            bowRef     = new ItemStack(Items.BOW).getMaxDamage();
            rodRef     = new ItemStack(Items.FISHING_ROD).getMaxDamage();
            shearsRef  = new ItemStack(Items.SHEARS).getMaxDamage();
            shieldRef  = new ItemStack(Items.SHIELD).getMaxDamage();
            flintRef   = new ItemStack(Items.FLINT_AND_STEEL).getMaxDamage();
            tridentRef = new ItemStack(Items.TRIDENT).getMaxDamage();
        }
        catch (Exception e)
        {
            Log.getLogger().error("Failed to fetch getMaxDamage references for equipment tier registration, durability based tiers for certain items will not be registered.", e);
            return;
        }

        for (final Item item : BuiltInRegistries.ITEM)
        {
            try
            {

                final ItemStack dummy = new ItemStack(item);

                if (item instanceof final TieredItem tiered)
                {
                    Compatibility.registerItemTierIfAbsent(item, tiered.getTier(), (int) tiered.getTier().getAttackDamageBonus());
                }
                else if (item instanceof ArmorItem)
                {
                    final int level = ItemStackUtils.getArmorLevel(dummy);
                    if (level > 0)
                    {
                        Compatibility.registerItemTierIfAbsent(item, level);
                    }
                }
                else if (item instanceof BowItem)
                {
                    Compatibility.registerItemTierIfAbsent(item, durabilityBasedLevel(dummy, bowRef));
                }
                else if (canPerformDefaultActions(dummy, ToolActions.DEFAULT_FISHING_ROD_ACTIONS))
                {
                    Compatibility.registerItemTierIfAbsent(item, durabilityBasedLevel(dummy, rodRef));
                }
                else if (canPerformDefaultActions(dummy, ToolActions.DEFAULT_SHEARS_ACTIONS))
                {
                    Compatibility.registerItemTierIfAbsent(item, durabilityBasedLevel(dummy, shearsRef));
                }
                else if (canPerformDefaultActions(dummy, ToolActions.DEFAULT_SHIELD_ACTIONS))
                {
                    Compatibility.registerItemTierIfAbsent(item, durabilityBasedLevel(dummy, shieldRef));
                }
                else if (item instanceof FlintAndSteelItem)
                {
                    Compatibility.registerItemTierIfAbsent(item, durabilityBasedLevel(dummy, flintRef));
                }
                else if (item instanceof TridentItem)
                {
                    Compatibility.registerItemTierIfAbsent(item, durabilityBasedLevel(dummy, tridentRef));
                }
            }
            catch (Exception e)
            {
                Log.getLogger().error("Failed to register equipment tiers for item: " + BuiltInRegistries.ITEM.getKey(item), e);
            }
        }
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
