package com.minecolonies.core.client.render;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.items.component.ColonyId;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import java.util.HashSet;
import java.util.Set;

public class ClipBoardDecorator implements IItemDecorator
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
            colonyView = ColonyId.readColonyViewFromItemStack(stack);

                if (colonyView != null)
                {
                    final Set<IToken<?>> asyncRequest = new HashSet<>();
                    for (final ICitizenDataView view : colonyView.getCitizens().values())
                    {
                        if (view.getJobView() != null)
                        {
                            asyncRequest.addAll(view.getJobView().getAsyncRequests());
                        }
                    }

                    final IRequestManager requestManager = colonyView.getRequestManager();
                    if (requestManager != null)
                    {
                        final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
                        final IRetryingRequestResolver retryingRequestResolver = requestManager.getRetryingRequestResolver();

                        final Set<IToken<?>> requestTokens = new HashSet<>();
                        requestTokens.addAll(resolver.getAllAssignedRequests());
                        requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

                        int count = 0;
                        for (final IToken<?> reqId : requestTokens)
                        {
                            if (!asyncRequest.contains(reqId))
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
                }
        }
        return false;
    }
}