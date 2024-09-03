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
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Class used for storing and registering any ToolTypes.
 */
public class ModToolTypes
{
    /**
     * The Forge registry for tool types. Do not directly add to this registry. Prefer using the
     * register() function provided here.
     */
    private final static DeferredRegister<ToolTypeEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "tooltypes"), Constants.MOD_ID);

    public static String NONE_ID            = "none";
    public static String PICKAXE_ID         = "pickaxe";
    public static String SHOVEL_ID          = "shovel";
    public static String AXE_ID             = "axe";
    public static String HOE_ID             = "hoe";
    public static String SWORD_ID           = "sword";
    public static String BOW_ID             = "bow";
    public static String FISHING_ROD_ID     = "rod";
    public static String SHEARS_ID          = "shears";
    public static String SHIELD_ID          = "shield";
    public static String HELMET_ID          = "helmet";
    public static String LEGGINGS_ID        = "leggings";
    public static String CHESTPLATE_ID      = "chestplate";
    public static String BOOTS_ID           = "boots";
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

    /**
     * The list of all tool types.
     */
    public static List<RegistryObject<ToolTypeEntry>> toolTypes = new ArrayList<>();
    static
    {
        none = register(NONE_ID,
          () -> new ToolTypeEntry.Builder().setName("")
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE))
                  .setIsTool(itemStack -> true)
                  .setToolLevel(itemStack -> itemStack == null ? 0 : 1)
                  .build());

        pickaxe = register(PICKAXE_ID,
          () -> new ToolTypeEntry.Builder().setName(PICKAXE_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_PICKAXE_ACTIONS) || Compatibility.isTinkersTool(itemStack, PICKAXE_ID))
                  .setToolLevel(itemStack -> vanillaToolLevel(PICKAXE_ID, itemStack))
                  .build());

        shovel = register(SHOVEL_ID,
          () -> new ToolTypeEntry.Builder().setName(SHOVEL_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHOVEL_ACTIONS) || Compatibility.isTinkersTool(itemStack, SHOVEL_ID))
                  .setToolLevel(itemStack -> vanillaToolLevel(SHOVEL_ID, itemStack))
                  .build());

        axe = register(AXE_ID,
          () -> new ToolTypeEntry.Builder().setName(AXE_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_AXE_ACTIONS) || Compatibility.isTinkersTool(itemStack, AXE_ID))
                  .setToolLevel(itemStack -> vanillaToolLevel(AXE_ID, itemStack))
                  .build());

        hoe = register(HOE_ID,
          () -> new ToolTypeEntry.Builder().setName(HOE_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_HOE_ACTIONS) || Compatibility.isTinkersTool(itemStack, HOE_ID))
                  .setToolLevel(itemStack -> vanillaToolLevel(HOE_ID, itemStack))
                  .build());

        sword = register(SWORD_ID,
          () -> new ToolTypeEntry.Builder().setName(SWORD_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SWORD_ACTIONS) || Compatibility.isTinkersWeapon(itemStack))
                  .setToolLevel(itemStack -> {
                      final String toolId = SWORD_ID;
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

        bow = register(BOW_ID,
          () -> new ToolTypeEntry.Builder().setName(BOW_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW))
                  .setIsTool(itemStack -> itemStack.getItem() instanceof BowItem)
                  .setToolLevel(itemStack -> durabilityBasedLevel(itemStack, Items.BOW.getMaxDamage()))
                  .build());

        fishing_rod = register(FISHING_ROD_ID,
          () -> new ToolTypeEntry.Builder().setName(FISHING_ROD_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_FISHING_ROD_ACTIONS))
                  .setToolLevel(itemStack -> durabilityBasedLevel(itemStack, Items.FISHING_ROD.getMaxDamage()))
                  .build());

        shears = register(SHEARS_ID,
          () -> new ToolTypeEntry.Builder().setName(SHEARS_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHEARS_ACTIONS))
                  .setToolLevel(itemStack -> durabilityBasedLevel(itemStack, Items.SHEARS.getMaxDamage()))
                  .build());

        shield = register(SHIELD_ID,
          () -> new ToolTypeEntry.Builder().setName(SHIELD_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD))
                  .setIsTool(itemStack -> canPerformDefaultActions(itemStack, ToolActions.DEFAULT_SHIELD_ACTIONS))
                  .setToolLevel(itemStack -> durabilityBasedLevel(itemStack, Items.SHIELD.getMaxDamage()))
                  .build());

        helmet = register(HELMET_ID,
          () -> new ToolTypeEntry.Builder().setName(HELMET_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET))
                  .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.HEAD.equals(armor.getEquipmentSlot()))
                  .setToolLevel(ModToolTypes::armorLevel)
                  .build());

        leggings = register(LEGGINGS_ID,
          () -> new ToolTypeEntry.Builder().setName(LEGGINGS_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS))
                  .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.LEGS.equals(armor.getEquipmentSlot()))
                  .setToolLevel(ModToolTypes::armorLevel)
                  .build());

        chestplate = register(CHESTPLATE_ID,
          () -> new ToolTypeEntry.Builder().setName(CHESTPLATE_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE))
                  .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.CHEST.equals(armor.getEquipmentSlot()))
                  .setToolLevel(ModToolTypes::armorLevel)
                  .build());

        boots = register(BOOTS_ID,
          () -> new ToolTypeEntry.Builder().setName(BOOTS_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS))
                  .setIsTool(itemStack -> itemStack.getItem() instanceof ArmorItem armor && EquipmentSlot.FEET.equals(armor.getEquipmentSlot()))
                  .setToolLevel(ModToolTypes::armorLevel)
                  .build());

        flint_and_steel = register(FLINT_AND_STEEL_ID,
          () -> new ToolTypeEntry.Builder().setName(FLINT_AND_STEEL_ID)
                  .setDisplayName(Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER))
                  .setIsTool(itemStack -> itemStack.getItem() instanceof FlintAndSteelItem)
                  .setToolLevel(itemStack -> durabilityBasedLevel(itemStack, Items.FLINT_AND_STEEL.getMaxDamage()))
                  .build());
    }

    /**
     * Register a new tool type. This should be used rather than directly adding entries
     * to the registry.
     *
     * @param id       The tool type name
     * @param supplier A function that provides the ToolTypeEntry
     * @return The new RegistryObject
     */
    public static RegistryObject<ToolTypeEntry> register(final String id, final Supplier<ToolTypeEntry> supplier)
    {
        RegistryObject<ToolTypeEntry> entry = DEFERRED_REGISTER.register(id, supplier);
        toolTypes.add(entry);
        return entry;
    }

    /**
     * Initialize the registry
     *
     * @param eventBus The event bus
     */
    public static void init(IEventBus eventBus)
    {
        DEFERRED_REGISTER.register(eventBus);
    }

    /**
     * Get the tool level for vanilla tools.
     *
     * @param toolId    The type of vanilla tool
     * @param itemStack The item stack to check
     * @return The tool level
     */
    public static int vanillaToolLevel(final String toolId, ItemStack itemStack)
    {
        if (Compatibility.isTinkersTool(itemStack, toolId))
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
