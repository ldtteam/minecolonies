package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * JEI recipe category showing supported tool and armor levels.
 */
public class ToolRecipeCategory implements IRecipeCategory<ToolRecipeCategory.ToolUsage>
{
    private static final int WIDTH = 180;
    private static final int HEIGHT = 44;
    private static final int SLOT_X = WIDTH - 2 - ((MAX_BUILDING_LEVEL+1) * 18);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    /**
     * Constructor
     */
    public ToolRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.plateArmorChest));
        this.slot = guiHelper.getSlotDrawable();
    }

    /**
     * Generate the list of {@link ToolUsage}.
     */
    @NotNull
    public static List<ToolUsage> findRecipes()
    {
        final Map<ToolType, ToolUsage> toolItems = new HashMap<>();

        for (final ItemStack stack : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
        {
            for (final ToolType tool : ToolType.values())
            {
                if (tool == ToolType.NONE || !ItemStackUtils.isTool(stack, tool)) { continue; }

                tryAddingToolWithLevel(toolItems, tool, stack);

                if (stack.isEnchantable())
                {
                    for (int enchantLevel = 1; enchantLevel < 4; ++enchantLevel)
                    {
                        tryAddingEnchantedTool(toolItems, tool, stack, enchantLevel);
                    }
                }
            }
        }

        return toolItems.values().stream().sorted(Comparator.comparing(ToolUsage::tool)).toList();
    }

    private static void tryAddingEnchantedTool(@NotNull final Map<ToolType, ToolUsage> toolItems,
                                               @NotNull final ToolType tool,
                                               @NotNull final ItemStack stack,
                                               final int enchantLevel)
    {
        final ItemStack enchantedStack = stack.copy();

        // this list should theoretically end up applying a total of two enchants to each tool type
        tryEnchantStack(enchantedStack, Enchantments.UNBREAKING, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.MOB_LOOTING, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.FLAMING_ARROWS, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.BLOCK_FORTUNE, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.ALL_DAMAGE_PROTECTION, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.FISHING_SPEED, enchantLevel);

        tryAddingToolWithLevel(toolItems, tool, enchantedStack);
    }

    private static void tryEnchantStack(@NotNull final ItemStack stack,
                                        @NotNull final Enchantment enchantment,
                                        final int enchantLevel)
    {
        if (enchantment.canEnchant(stack) && enchantLevel >= enchantment.getMinLevel() && enchantLevel <= enchantment.getMaxLevel())
        {
            stack.enchant(enchantment, enchantLevel);
        }
    }

    private static void tryAddingToolWithLevel(@NotNull final Map<ToolType, ToolUsage> toolItems,
                                               @NotNull final ToolType tool,
                                               @NotNull final ItemStack stack)
    {
        int level = ItemStackUtils.getMiningLevel(stack, tool);
        if (level < 0) {
            return;
        }
        level = Math.min(MAX_BUILDING_LEVEL, level + ItemStackUtils.getMaxEnchantmentLevel(stack));

        final ToolUsage usage = toolItems.computeIfAbsent(tool, ToolUsage::create);

        if (stack.isEnchanted())
        {
            usage.enchantedToolLevels.get(level).add(stack);
        }
        else
        {
            usage.toolLevels.get(level).add(stack);
        }
    }

    @NotNull
    @Override
    public RecipeType<ToolUsage> getRecipeType()
    {
        return ModRecipeTypes.TOOLS;
    }

    @NotNull
    @Override
    public Component getTitle()
    {
        return Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "tools");
    }

    @NotNull
    @Override
    public IDrawable getBackground()
    {
        return this.background;
    }

    @NotNull
    @Override
    public IDrawable getIcon()
    {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final ToolUsage recipe,
                          @NotNull final IFocusGroup focuses)
    {
        int x = SLOT_X;
        int y = HEIGHT - 36;

        for (int i = 0; i <= MAX_BUILDING_LEVEL; ++i)
        {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setSlotName("L" + i)
                    .addItemStacks(recipe.toolLevels().get(i))
                    .setBackground(this.slot, -1, -1);

            builder.addSlot(RecipeIngredientRole.INPUT, x, y + 18)
                    .setSlotName("L" + i + "e")
                    .addItemStacks(recipe.enchantedToolLevels().get(i))
                    .setBackground(this.slot, -1, -1);

            x += 18;
        }
    }

    @Override
    public void draw(@NotNull final ToolUsage recipe,
                     @NotNull final IRecipeSlotsView recipeSlotsView,
                     @NotNull final PoseStack stack,
                     final double mouseX, final double mouseY)
    {
        final Minecraft mc = Minecraft.getInstance();

        final List<FormattedText> lines = mc.font.getSplitter().splitLines(recipe.tool().getDisplayName(), SLOT_X - 4, Style.EMPTY);
        final int y = HEIGHT - (36 + (lines.size() * mc.font.lineHeight)) / 2 - 1;
        for (int i = 0; i < lines.size(); ++i)
        {
            mc.font.draw(stack, Language.getInstance().getVisualOrder(lines.get(i)), 2, y + (i * mc.font.lineHeight), 0);
        }

        final float scale = 0.5F;
        stack.pushPose();
        stack.scale(scale, scale, 1.0F);
        int x = SLOT_X;
        for (int i = 0; i <= MAX_BUILDING_LEVEL; ++i)
        {
            final Component text = Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "onelevelrestriction", i);
            mc.font.draw(stack, text, (x + (18 - mc.font.width(text)*scale) / 2F) / scale, 1 / scale, 0);
            x += 18;
        }
        stack.popPose();
    }

    /**
     * Describes a tool and its level compatibility.
     * @param tool                  the tool type
     * @param toolLevels            basic items accepted at each building level.
     * @param enchantedToolLevels   (some) enchanted items accepted at each building level.
     */
    public record ToolUsage(@NotNull ToolType tool,
                            @NotNull List<List<ItemStack>> toolLevels,
                            @NotNull List<List<ItemStack>> enchantedToolLevels)
    {
        @NotNull
        public static ToolUsage create(@NotNull final ToolType tool)
        {
            final List<List<ItemStack>> basicLevels = new ArrayList<>();
            final List<List<ItemStack>> enchantedLevels = new ArrayList<>();
            for (int i = 0; i <= MAX_BUILDING_LEVEL; ++i)
            {
                basicLevels.add(new ArrayList<>());
                enchantedLevels.add(new ArrayList<>());
            }
            return new ToolUsage(tool, basicLevels, enchantedLevels);
        }
    }
}
