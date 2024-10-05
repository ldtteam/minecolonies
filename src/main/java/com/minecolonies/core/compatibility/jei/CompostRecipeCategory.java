package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.CompostRecipe;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JEI compost recipe category renderer
 */
public class CompostRecipeCategory extends AbstractRecipeCategory<CompostRecipe>
{
    private final ITickTimer timer;

    public CompostRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        super(
            ModRecipeTypes.COMPOSTING,
            Component.translatableEscape(ModBlocks.blockBarrel.getDescriptionId()),
            guiHelper.createDrawableItemLike(ModBlocks.blockBarrel),
            80,
            50
        );
        this.timer = guiHelper.createTickTimer(60, BarrelType.values().length - 2, false);
    }

    @NotNull
    public static List<CompostRecipe> findRecipes()
    {
        return IColonyManager.getInstance().getCompatibilityManager().getCopyOfCompostRecipes().entrySet().stream()
                .map(entry -> CompostRecipe.individualize(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final CompostRecipe recipe,
                          @NotNull final IFocusGroup focuses)
    {
        builder.addInputSlot(0, 0)
                .setStandardSlotBackground()
                .addIngredients(recipe.getIngredients().get(0));

        builder.addOutputSlot(62, 0)
                .setStandardSlotBackground()
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(@NotNull final CompostRecipe recipe,
                     @NotNull final IRecipeSlotsView recipeSlotsView,
                     @NotNull final GuiGraphics stack,
                     final double mouseX, final double mouseY)
    {
        final BarrelType type = BarrelType.byMetadata(this.timer.getValue());
        final BlockState barrel = ModBlocks.blockBarrel.defaultBlockState()
                .setValue(AbstractBlockBarrel.FACING, Direction.SOUTH)
                .setValue(AbstractBlockBarrel.VARIANT, type);
        RenderHelper.renderBlock(stack, barrel, 40, 20, 100, -30F, 20F, 25F);
    }
}
