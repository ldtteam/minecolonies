package com.minecolonies.core.items;

import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.network.messages.client.VanillaParticleMessage;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.TOOL_GENERIC_SCROLL_BUFF_DESCRIPTION;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Magic scroll which applies a regeneration buff to the user and all citizens around
 */
public class ItemScrollBuff extends AbstractItemScroll
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param properties the properties.
     */
    public ItemScrollBuff(final Properties properties)
    {
        super("scroll_buff", properties);
    }

    @Override
    protected ItemStack onItemUseSuccess(final ItemStack itemStack, final Level world, final ServerPlayer player)
    {
        if (world.random.nextInt(8) > 0)
        {
            for (final LivingEntity entity : world.getEntitiesOfClass(EntityCitizen.class, player.getBoundingBox().inflate(15, 2, 15)))
            {
                addRegenerationWithParticles(entity);
            }

            addRegenerationWithParticles(player);
            // Send to player additionally, as players do not track themselves
            new VanillaParticleMessage(player.getX(), player.getY(), player.getZ(), ParticleTypes.HEART).sendToPlayer(player);
            SoundUtils.playSoundForPlayer(player, SoundEvents.PLAYER_LEVELUP, 0.2f, 1.0f);
        }
        else
        {
            player.displayClientMessage(Component.translatable("minecolonies.scroll.failed" + (world.random.nextInt(FAIL_RESPONSES_TOTAL) + 1)).setStyle(Style.EMPTY.withColor(
              ChatFormatting.GOLD)), true);
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, TICKS_SECOND * 10));
            SoundUtils.playSoundForPlayer(player, SoundEvents.TOTEM_USE, 0.04f, 1.0f);
        }

        itemStack.shrink(1);
        return itemStack;
    }

    /**
     * Adds a regeneration potion instance and displays particles
     *
     * @param entity entity to apply to
     */
    private void addRegenerationWithParticles(final LivingEntity entity)
    {
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, TICKS_SECOND * 60));
        new VanillaParticleMessage(entity.getX(), entity.getY(), entity.getZ(), ParticleTypes.HEART).sendToTrackingEntity(entity);
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatable(TOOL_GENERIC_SCROLL_BUFF_DESCRIPTION);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
        tooltip.add(guiHint);
    }

    @Override
    protected boolean needsColony()
    {
        return false;
    }
}
