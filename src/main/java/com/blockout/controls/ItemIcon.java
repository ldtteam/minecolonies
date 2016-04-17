package com.blockout.controls;

import com.blockout.Pane;
import net.minecraft.item.ItemStack;

public class ItemIcon extends Pane
{

    private ItemStack itemStack;

    public void setItem(ItemStack itemStack) { this.itemStack = itemStack; }

    @Override
    protected void drawSelf(int mx, int my)
    {
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, x, y, "");
    }
}
