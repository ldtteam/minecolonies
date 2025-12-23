package com.minecolonies.core.client.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.controls.Button;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.MinimumStack;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.RequestTreeWindowModule;
import com.minecolonies.core.items.ItemClipboard;
import com.minecolonies.core.network.messages.server.ItemSettingMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.WindowConstants.CLIPBOARD_TOGGLE;

/**
 * ClipBoard window.
 */
public class WindowClipBoard extends AbstractWindowSkeleton
{
    /**
     * The request tree module.
     */
    private final ClipboardRequestTreeWindowModule requestTreeWindowModule;

    /**
     * Hide or show not important requests.
     */
    private boolean showImportant;

    /**
     * Constructor of the clipboard GUI.
     *
     * @param colony the colony to check the requests for.
     */
    public WindowClipBoard(final IColonyView colony, boolean showImportant)
    {
        super(new ResourceLocation(Constants.MOD_ID, "gui/windowclipboard.xml"));
        this.showImportant = showImportant;
        this.requestTreeWindowModule = registerLayoutModule(ClipboardRequestTreeWindowModule::new, new ClipboardRequestTreeWindowModule.Options(colony, () -> this.showImportant), 16, 44);

        registerButton(CLIPBOARD_TOGGLE, this::toggleImportant);
        paintButtonState();
    }

    /**
     * Toggles the visibility of non-important requests and sends a message to
     * the server to save that setting on the clipboard item.
     *
     * @see ItemSettingMessage
     */
    private void toggleImportant()
    {
        this.showImportant = !this.showImportant;

        paintButtonState();

        ItemSettingMessage hideSetting = new ItemSettingMessage();
        hideSetting.setSetting(ItemClipboard.TAG_HIDEUNIMPORTANT, this.showImportant ? 1 : 0);
        Network.getNetwork().sendToServer(hideSetting);

        requestTreeWindowModule.refreshOpenRequests();
    }

    /**
     * Paints the button state of the important toggle.
     * <p>
     * This function finds the important toggle button and sets its colors based on the state of hide.
     * If hide is true, the button is set to green. Otherwise, it is set to red.
     */
    private void paintButtonState()
    {
        final Button importantToggle = findPaneOfTypeByID("important", Button.class);

        if (this.showImportant)
        {
            importantToggle.setColors(Color.getByName("green", 0));
        }
        else
        {
            importantToggle.setColors(Color.getByName("red", 0));
        }
    }

    private static class ClipboardRequestTreeWindowModule extends RequestTreeWindowModule
    {
        private final Supplier<Boolean> showImportant;

        /**
         * Constructor to initiate the window request tree windows.
         *
         * @param parent the parenting window.
         * @param options the extra options for the module.
         */
        public ClipboardRequestTreeWindowModule(final AbstractWindowSkeleton parent, final Options options)
        {
            super(parent, options.colony);
            this.showImportant = options.showImportant;
        }

        @Override
        protected Collection<IRequest<?>> getOpenRequests()
        {
            final boolean showImportant = this.showImportant.get();
            final IRequestManager requestManager = colony.getRequestManager();
            final List<IRequest<?>> requests = new ArrayList<>();

            final List<IToken<?>> asyncRequest = new ArrayList<>();
            for (final ICitizenDataView view : this.colony.getCitizens().values())
            {
                if (view.getJobView() != null)
                {
                    asyncRequest.addAll(view.getJobView().getAsyncRequests());
                }
            }

            try
            {
                final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
                final IRetryingRequestResolver retryingRequestResolver = requestManager.getRetryingRequestResolver();

                final Set<IToken<?>> requestTokens = new HashSet<>();
                requestTokens.addAll(resolver.getAllAssignedRequests());
                requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

                for (final IToken<?> token : requestTokens)
                {
                    IRequest<?> request = requestManager.getRequestForToken(token);
                    while (request != null && request.hasParent())
                    {
                        request = requestManager.getRequestForToken(request.getParent());
                    }

                    if (request == null)
                    {
                        continue;
                    }

                    if (!showImportant && request.getType().equals(TypeToken.of(MinimumStack.class)))
                    {
                        continue;
                    }

                    if (!requests.contains(request))
                    {
                        requests.add(request);
                    }
                }

                if (!showImportant)
                {
                    requests.removeIf(req -> asyncRequest.contains(req.getId()));
                }

                final BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
                requests.sort(Comparator.comparing((IRequest<?> request) -> request.getRequester()
                    .getLocation()
                    .getInDimensionLocation()
                    .distSqr(new Vec3i(playerPos.getX(), playerPos.getY(), playerPos.getZ()))).thenComparingInt((IRequest<?> request) -> request.getId().hashCode()));
            }
            catch (Exception e)
            {
                Log.getLogger().warn("Exception trying to retrieve requests:", e);
                requestManager.reset();
                return ImmutableList.of();
            }
            return ImmutableList.copyOf(requests);
        }

        private record Options(
            IColonyView colony,
            Supplier<Boolean> showImportant) {}
    }
}
