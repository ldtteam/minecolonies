package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.network.messages.server.CreateColonyMessage;
import com.minecolonies.core.network.messages.client.VanillaParticleMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * UI to found a new colony.
 */
public class WindowTownHallColonyManage extends AbstractWindowSkeleton
{
    private static final String BUTTON_CREATE = "create";

    /**
     * Townhall position
     */
    private final BlockPos pos;

    /**
     * If it is a reactivated colony.
     */
    private final boolean reactivate;

    public WindowTownHallColonyManage(final BlockPos pos, final String closestName, final int closestDistance, final String preName, final boolean reactivate)
    {
        super(MOD_ID + TOWNHALL_COLONY_MANAGEMENT_GUI);
        this.pos = pos;
        this.reactivate = reactivate;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));

        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_CREATE, this::onCreate);

        this.findPaneOfTypeByID("colonyname", TextField.class).setText(preName.isEmpty() ? Component.translatable(DEFAULT_COLONY_NAME, mc.player.getName()).getString() : preName);
        this.findPaneOfTypeByID("text1", Text.class).setText(Component.translatable("com.minecolonies.core.settlementcovenant1", Math.max(13, Minecraft.getInstance().level.getGameTime() / TICKS_SECOND / 60 / 100)));
        if (closestDistance < 1000)
        {
            this.findPaneOfTypeByID("text3", Text.class).setText(Component.translatable("com.minecolonies.core.settlementcovenant3.hasclose", Component.literal(closestName).withStyle(ChatFormatting.RED) , Component.literal(String.valueOf(closestDistance)).withStyle(ChatFormatting.RED)));
        }
        else
        {
            this.findPaneOfTypeByID("text3", Text.class).setText(Component.translatable("com.minecolonies.core.settlementcovenant3.noclose"));
        }
    }

    /**
     * On create button
     */
    public void onCreate()
    {
        final String colonyName = this.findPaneOfTypeByID("colonyname", TextField.class).getText();

        new VanillaParticleMessage(pos.getX(), pos.getY(), pos.getZ(), ParticleTypes.DRAGON_BREATH).onExecute(null, mc.player);
        Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, Minecraft.getInstance().player.blockPosition(),
          SoundEvents.CAMPFIRE_CRACKLE, SoundSource.AMBIENT, 2.5f, 0.8f);
        final BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(pos);

        new CreateColonyMessage(pos,
                reactivate,
                colonyName,
                reactivate ? "" : StructurePacks.selectedPack.getName(),
                ((TileEntityColonyBuilding) entity).getBlueprintPath()).sendToServer();

        close();
    }
}
