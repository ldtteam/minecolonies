package com.minecolonies.coremod.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom "widget" of sorts that lays out minecraft's DyeColors as buttons to choose from.
 */
@OnlyIn(Dist.CLIENT)
public class ColorPalette
{
    public static final DyeColor[] DYES = DyeColor.values();

    public static int BUTTON_SIZE = 20;

    protected List<PaletteButton> buttons = new ArrayList<>();
    protected DyeColor selected = DyeColor.WHITE;

    public ChangeEvent onchange;

    /**
     * @param x the x position of the top left button
     * @param y the y position of the top left button
     * @param col the number of columns to sort this palette into
     * @param adder a function to add each button to the calling screen. Call with this::addButton if unsure.
     */
    public ColorPalette (int x, int y, int col, IWidgetAdder adder)
    {
        int topLeftX = x - col * BUTTON_SIZE / 2;
        int topLeftY = y - DYES.length / col * BUTTON_SIZE / 2;

        for (DyeColor color : DYES)
        {
            int posX = topLeftX + (color.getId() % col) * BUTTON_SIZE;
            int posY = topLeftY + BUTTON_SIZE * Math.floorDiv(color.getId(), col);

            PaletteButton button = new ColorPalette.PaletteButton(posX, posY, BUTTON_SIZE, color);
            buttons.add(button);
            adder.onBuild(button);
        }
    }

    /**
     * A more generic form of the constructor
     * @param gui the screen to interact with this color palette
     * @param adder the function to add the buttons. Likely this::addButton from a Screen
     */
    public ColorPalette (Screen gui, IWidgetAdder adder)
    {
        this(gui.width/2, gui.height/2, (int) Math.floor(Math.sqrt(DYES.length)), adder);
    }

    /** Used as a function processor to add the widgets to the screen */
    @OnlyIn(Dist.CLIENT)
    public interface IWidgetAdder { void onBuild(Button toAdd); }

    /** A custom function to call when the color has been changed. Edit via the onchange field. */
    @OnlyIn(Dist.CLIENT)
    public interface ChangeEvent { void onChange(DyeColor now); }

    public DyeColor getSelected() { return selected; }

    public void setSelected(DyeColor selected) { this.selected = selected; }

    public class PaletteButton extends Button
    {
        private final DyeColor color;

        public PaletteButton(int posX, int posY, int sideLength, DyeColor color)
        {
            super(posX, posY, sideLength, sideLength, Component.literal(""), pressed -> {});
            this.color = color;
        }

        @Override
        public void render(final PoseStack stack, int mouseX, int mouseY, float partialTicks)
        {
            this.active = selected != this.color;

            super.render(stack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void renderButton(final PoseStack stack, int mouseX, int mouseY, float partialTicks)
        {
            int color = this.color.getTextColor();
            boolean pressed = selected == this.color;

            this.fillButton(stack, 0, 0, 0, 0, isHovered ? 0xFFFFFF : pressed ? brighten(color, 0.5F) : 0x0);
            this.fillButton(stack,1, 1, 1, 1, brighten(color, 0.8F));

            if (pressed)
            {
                this.fillButton(stack,2, 2, 1, 1, brighten(color, 1.2F));
                this.fillButton(stack,2, 2, 2, 2, color);
                this.fillButton(stack,7, 7, 7, 7, -0xCCFFFFFF);
            }
            else
            {
                this.fillButton(stack,1, 1, 3, 2, brighten(color, 1.2F));
                this.fillButton(stack,2, 2, 3, 2, color);
            }

        }

        @Override
        public void onPress ()
        {
            selected = this.color;
            onchange.onChange(selected);
        }

        /**
         * A convenience method for filling the rectangles
         * @param t the top offset
         * @param l the left offset
         * @param b the bottom offset
         * @param r the right offset
         * @param color the color to fill with, without alpha
         */
        private void fillButton(final PoseStack stack, int t, int l, int b, int r, int color)
        {
            color += 255 << 24;
            fill(stack,
                    this.x+l, this.y+t,
                    this.x+this.width-r, this.y+this.height-b,
                    color
            );
        }

        /**
         * Adjusts the brightness of the color according to the factor
         * @param color the color to brighten or dim
         * @param factor a percentage factor to multiply the rgb values by
         * @return the adjusted color
         */
        private int brighten(int color, float factor)
        {
            int r = color >> 16;
            int g = color - (r << 16) >> 8;
            int b = color - (r << 16) - (g << 8);
            r = (int) Math.min(255, r * factor) << 16;
            g = (int) Math.min(255, g * factor) <<  8;
            b = (int) Math.min(255, b * factor);
            return r + g + b;
        }
    }
}
