package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemIcon extends Pane
{
    private ItemStack itemStack;

    public ItemIcon()
    {
        super();
    }

    public ItemIcon(final PaneParams params)
    {
        super(params);

        final String itemName = params.getStringAttribute("item", null);
        if (itemName != null)
        {
            final Item item = Item.REGISTRY.getObject(new ResourceLocation(itemName));
            if (item != null)
            {
                itemStack = new ItemStack(item, 1);
            }
        }
    }

    public void setItem(final ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    @Override
    protected void drawSelf(final int mx, final int my)
    {
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemStack, x, y);
        GlStateManager.disableLighting();
    }
}
