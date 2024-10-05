package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.crafting.ToolUsage;
import com.minecolonies.core.colony.crafting.ToolsAnalyzer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * JEI recipe category showing supported tool and armor levels.
 */
public class ToolRecipeCategory extends AbstractRecipeCategory<ToolUsage>
{
    private static final int WIDTH = 180;
    private static final int HEIGHT = 44;
    private static final int SLOT_X = WIDTH - 2 - ((MAX_BUILDING_LEVEL+1) * 18);
    private static final int SLOT_Y = HEIGHT - 36;

    /**
     * Constructor
     */
    public ToolRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        super(
                ModRecipeTypes.TOOLS,
                Component.translatableEscape(TranslationConstants.PARTIAL_JEI_INFO + "tools"),
                guiHelper.createDrawableItemLike(ModItems.plateArmorChest),
                WIDTH,
                HEIGHT
        );
    }

    /**
     * Generate the list of {@link ToolUsage}.
     */
    @NotNull
    public static List<ToolUsage> findRecipes(final Level level)
    {
        return ToolsAnalyzer.findTools(level);
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final ToolUsage recipe,
                          @NotNull final IFocusGroup focuses)
    {
        int x = SLOT_X;
        int y = SLOT_Y;

        for (int i = 0; i <= MAX_BUILDING_LEVEL; ++i)
        {
            builder.addInputSlot(x, y)
                    .setSlotName("L" + i)
                    .addItemStacks(recipe.toolLevels().get(i))
                    .setStandardSlotBackground();

            builder.addInputSlot(x, y + 18)
                    .setSlotName("L" + i + "e")
                    .addItemStacks(recipe.enchantedToolLevels().get(i))
                    .setStandardSlotBackground();

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

    @Override
    public void createRecipeExtras(@NotNull final IRecipeExtrasBuilder builder,
                                   @NotNull final ToolUsage recipe,
                                   @NotNull final IFocusGroup focuses)
    {
        builder.addText(recipe.tool().getDisplayName(), SLOT_X - 6, getHeight() - SLOT_Y)
                .setPosition(2, SLOT_Y - 1)
                .setTextAlignment(VerticalAlignment.CENTER);
    }
}
