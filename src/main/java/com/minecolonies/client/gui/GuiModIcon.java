package com.minecolonies.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

public class GuiModIcon
{
    protected int x, y;
    protected ItemStack itemStack;

    public GuiModIcon(ItemStack itemStack, int x, int y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public void drawIcon(Minecraft mc, RenderItem itemRender) {
        FontRenderer fontRenderer = mc.fontRenderer;
        itemRender.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), itemStack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, mc.getTextureManager(), itemStack, x, y);
    }
}
