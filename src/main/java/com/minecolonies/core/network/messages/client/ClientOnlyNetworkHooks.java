package com.minecolonies.core.network.messages.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Client-only implementations for common network payload callbacks. */
@OnlyIn(Dist.CLIENT)
public final class ClientOnlyNetworkHooks
{
    private ClientOnlyNetworkHooks() { }

    public static void register()
    {
        PlayAudioMessage.setClientHandler(ClientOnlyNetworkHooks::playAudio);
    }

    private static void playAudio(final PlayAudioMessage message, final Player player)
    {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
          message.soundEventId(), message.category(),
          1.0F, 1.0F, RandomSource.create(), false, 0,
          SoundInstance.Attenuation.NONE, player.getX(), player.getY(), player.getZ(), true));
    }
}
