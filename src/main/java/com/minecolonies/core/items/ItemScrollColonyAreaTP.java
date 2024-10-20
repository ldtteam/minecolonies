package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.items.component.Desc;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.network.messages.client.VanillaParticleMessage;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Colony teleport scroll, which teleports the user and any nearby players to the colony, invite a friend-style
 */
public class ItemScrollColonyAreaTP extends AbstractItemScroll
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param properties the properties.
     */
    public ItemScrollColonyAreaTP(final Properties properties)
    {
        super("scroll_area_tp", properties);
    }

    @Override
    public int getUseDuration(final ItemStack itemStack, final LivingEntity livingEntity)
    {
        return 64;
    }

    @Override
    protected ItemStack onItemUseSuccess(final ItemStack itemStack, final Level world, final ServerPlayer player)
    {
        if (world.random.nextInt(10) == 0)
        {
            // Fail chance
            player.displayClientMessage(Component.translatableEscape(
              "minecolonies.scroll.failed" + (world.random.nextInt(FAIL_RESPONSES_TOTAL) + 1)).setStyle(Style.EMPTY.withColor(
              ChatFormatting.GOLD)), true);

            itemStack.shrink(1);
            if (!ItemStackUtils.isEmpty(itemStack))
            {
                player.drop(itemStack.copy(), true, false);
                itemStack.setCount(0);
            }

            for (final ServerPlayer sPlayer : getAffectedPlayers(player))
            {
                SoundUtils.playSoundForPlayer(sPlayer, SoundEvents.EVOKER_PREPARE_SUMMON, 0.3f, 1.0f);
            }
        }
        else
        {
            for (final ServerPlayer sPlayer : getAffectedPlayers(player))
            {
                doTeleport(sPlayer, getColony(itemStack), itemStack);
                SoundUtils.playSoundForPlayer(sPlayer, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.1f, 1.0f);
            }

            itemStack.shrink(1);
        }

        return itemStack;
    }

    @Override
    protected boolean needsColony()
    {
        return true;
    }

    /**
     * Does the teleport action
     *
     * @param player user of the item
     * @param colony colony to teleport to
     */
    protected void doTeleport(final ServerPlayer player, final IColony colony, final ItemStack stack)
    {
        TeleportHelper.colonyTeleport(player, colony);
    }

    @Override
    public void onUseTick(Level worldIn, LivingEntity entity, ItemStack stack, int count)
    {
        if (!worldIn.isClientSide && worldIn.getGameTime() % 5 == 0 && entity instanceof Player)
        {
            final ServerPlayer sPlayer = (ServerPlayer) entity;
            for (final Entity player : getAffectedPlayers(sPlayer))
            {
                new VanillaParticleMessage(player.getX(), player.getY(), player.getZ(), ParticleTypes.INSTANT_EFFECT).sendToTrackingEntity(player);
            }

            new VanillaParticleMessage(sPlayer.getX(), sPlayer.getY(), sPlayer.getZ(), ParticleTypes.INSTANT_EFFECT).sendToPlayer(sPlayer);
        }
    }

    /**
     * Get the list of players affected by the area teleport
     */
    private List<ServerPlayer> getAffectedPlayers(final ServerPlayer user)
    {
        return user.level().getEntitiesOfClass(ServerPlayer.class, user.getBoundingBox().inflate(10, 2, 10));
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(TOOL_COLONY_TELEPORT_AREA_SCROLL_DESCRIPTION);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
        tooltip.add(guiHint);

        MutableComponent colonyDesc = Component.translatable(TOOL_COLONY_TELEPORT_SCROLL_NO_COLONY);

        final IColony colony = getColonyView(stack);
        if (colony != null)
        {
            colonyDesc = Component.literal(colony.getName());
        }

        final MutableComponent guiHint2 = Component.translatableEscape(TOOL_COLONY_TELEPORT_SCROLL_COLONY_NAME, colonyDesc);
        guiHint2.setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
        tooltip.add(guiHint2);
    }
}
