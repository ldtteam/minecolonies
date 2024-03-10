package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.network.messages.server.PickupBlockMessage;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 *  UI to notify the player that a colony can't be created here.
 */
public class WindowTownHallCantCreateColony extends AbstractWindowSkeleton
{
    /**
     * Townhall position
     */
    private BlockPos pos;

    public WindowTownHallCantCreateColony(final BlockPos pos, final MutableComponent warningMsg, final boolean displayConfigTooltip)
    {
        super(MOD_ID + TOWNHALL_CANT_CREATE_GUI);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        this.pos = pos;
        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_PICKUP_BUILDING, this::pickup);
        final Text text = this.findPaneOfTypeByID("text1", Text.class);
        text.setText(warningMsg);
        if (displayConfigTooltip)
        {
            PaneBuilders.singleLineTooltip(Component.translatable("com.minecolonies.core.configsetting"), text);
        }
    }

    /**
     * When the pickup building button was clicked.
     */
    private void pickup()
    {
        Network.getNetwork().sendToServer(new PickupBlockMessage(pos));
        close();
    }
}
