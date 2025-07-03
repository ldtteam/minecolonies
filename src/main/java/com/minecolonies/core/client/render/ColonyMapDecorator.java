package com.minecolonies.core.client.render;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.Log;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

import static com.minecolonies.core.items.ItemColonyMap.TAG_COLONY;

public class ColonyMapDecorator implements IItemDecorator
{
    private static IColonyView colonyView;
    private static boolean render = false;
    private long lastChange;

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int xOffset, int yOffset)
    {
        final long gametime = Minecraft.getInstance().level.getGameTime();

        if (lastChange != gametime && gametime % 40 == 0)
        {
            lastChange = gametime;
            render = !render;
        }

        if (render)
        {
            final CompoundTag compoundTag = stack.getTag();
            if (compoundTag != null)
            {
                final int colonyId = compoundTag.getInt(TAG_COLONY);
                colonyView = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().level.dimension());

                if (colonyView != null)
                {
                    try
                    {
                        int count = 0;
                        for (final ICitizenDataView view : colonyView.getCitizens().values())
                        {
                            if (view.hasBlockingInteractions())
                            {
                                count++;
                            }
                        }

                        if (count > 0)
                        {
                            final PoseStack ps = graphics.pose();
                            ps.pushPose();
                            ps.translate(0, 0, 500);
                            graphics.drawCenteredString(font,
                              Component.literal(count + ""),
                              xOffset + 15,
                              yOffset - 2,
                              0xFF4500 | (255 << 24));
                            ps.popPose();
                            return true;
                        }
                    }
                    catch (Exception e)
                    {
                        Log.getLogger().error("Something went wrong with the colonymap item decorator", e);
                    }
                }
            }
        }
        return false;
    }
}