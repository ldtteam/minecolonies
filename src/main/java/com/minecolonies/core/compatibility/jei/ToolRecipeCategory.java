package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.crafting.ToolUsage;
import com.minecolonies.core.colony.crafting.ToolsAnalyzer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * JEI recipe category showing supported tool and armor levels.
 */
public class ToolRecipeCategory implements IRecipeCategory<ToolUsage>
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
        return ToolsAnalyzer.findTools();
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
        return Component.translatableEscape(TranslationConstants.PARTIAL_JEI_INFO + "tools");
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
                     @NotNull final GuiGraphics stack,
                     final double mouseX, final double mouseY)
    {
        final Minecraft mc = Minecraft.getInstance();

        final List<FormattedText> lines = mc.font.getSplitter().splitLines(recipe.tool().getDisplayName(), SLOT_X - 4, Style.EMPTY);
        final int y = HEIGHT - (36 + (lines.size() * mc.font.lineHeight)) / 2 - 1;
        for (int i = 0; i < lines.size(); ++i)
        {
            stack.drawString(mc.font, Language.getInstance().getVisualOrder(lines.get(i)), 2, y + (i * mc.font.lineHeight), 0, false);
        }

        final int scale = 2;
        stack.pose().pushPose();
        stack.pose().scale(1F / scale, 1F / scale, 1.0F);
        int x = SLOT_X;
        for (int i = 0; i <= MAX_BUILDING_LEVEL; ++i)
        {
            final Component text = Component.translatableEscape(TranslationConstants.PARTIAL_JEI_INFO + "onelevelrestriction", i);
            stack.drawString(mc.font, text, (x + (18 - mc.font.width(text)/scale) / 2) * scale, scale, 0, false);
            x += 18;
        }
        stack.pose().popPose();
    }
}
