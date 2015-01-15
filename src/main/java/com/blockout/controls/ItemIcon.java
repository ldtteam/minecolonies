package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemIcon extends Pane
{
    protected static RenderItem itemRender = new RenderItem();

    private ItemStack itemStack;

    public ItemIcon(){ super(); }

    public ItemIcon(PaneParams params)
    {
        super(params);

        String itemName = params.getStringAttribute("item", null);
        if (itemName != null)
        {
            Item item = (Item)Item.itemRegistry.getObject(itemName);
            if (item != null)
            {
                itemStack = new ItemStack(item, 1);
            }
        }
    }

    public void setItem(ItemStack itemStack) { this.itemStack = itemStack; }

    @Override
    protected void drawSelf(int mx, int my)
    {
        itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemStack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemStack, x, y);
    }
}
