package com.minecolonies.blockout.controls;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.PaneParams;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Class of itemIcons in our GUIs.
 */
public class ItemIcon extends Pane
{
    /**
     * ItemStack represented in the itemIcon.
     */
    private ItemStack itemStack;

    /**
     * Standard constructor instantiating the itemIcon without any additional settings.
     */
    public ItemIcon()
    {
        super();
    }

    /**
     * Constructor instantiating the itemIcon with specified parameters.
     * @param params the parameters.
     */
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
        if(itemStack != null)
        {
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemStack, x, y);
            GlStateManager.disableLighting();
        }
    }
}
