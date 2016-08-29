package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
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

    public ItemIcon(PaneParams params)
    {
        super(params);

        String itemName = params.getStringAttribute("item", null);
        if (itemName != null)
        {
            Item item = (Item) Item.itemRegistry.getObject(new ResourceLocation(itemName));
            if (item != null)
            {
                itemStack = new ItemStack(item, 1);
            }
        }
    }

    public void setItem(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, x, y, "");
    }
}
