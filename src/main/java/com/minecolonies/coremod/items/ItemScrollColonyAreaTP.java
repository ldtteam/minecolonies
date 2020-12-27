package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.client.VanillaParticleMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

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
    public int getUseDuration(ItemStack itemStack)
    {
        return 64;
    }

    @Override
    protected ItemStack onItemUseSuccess(final ItemStack itemStack, final World world, final ServerPlayerEntity player)
    {
        itemStack.shrink(1);

        if (world.rand.nextInt(10) == 0)
        {
            // Fail chance
            player.sendStatusMessage(new TranslationTextComponent("minecolonies.scroll.failed" + (world.rand.nextInt(FAIL_RESPONSES_TOTAL) + 1)).setStyle(new Style().setColor(
              TextFormatting.GOLD)), true);
            player.dropItem(itemStack.copy(), true, false);
            itemStack.setCount(0);
            for (final ServerPlayerEntity sPlayer : getAffectedPlayers(player))
            {
                SoundUtils.playSoundForPlayer(sPlayer, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 0.3f, 1.0f);
            }
        }
        else
        {
            for (final ServerPlayerEntity sPlayer : getAffectedPlayers(player))
            {
                doTeleport(sPlayer, getColony(itemStack), itemStack);
                SoundUtils.playSoundForPlayer(sPlayer, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.1f, 1.0f);
            }
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
    protected void doTeleport(final ServerPlayerEntity player, final IColony colony, final ItemStack stack)
    {
        TeleportHelper.colonyTeleport(player, colony);
    }

    @Override
    public void onUse(World worldIn, LivingEntity entity, ItemStack stack, int count)
    {
        if (!worldIn.isRemote && worldIn.getGameTime() % 5 == 0 && entity instanceof PlayerEntity)
        {
            final ServerPlayerEntity sPlayer = (ServerPlayerEntity) entity;
            for (final Entity player : getAffectedPlayers(sPlayer))
            {
                Network.getNetwork()
                  .sendToTrackingEntity(new VanillaParticleMessage(player.getPosX(), player.getPosY(), player.getPosZ(), ParticleTypes.INSTANT_EFFECT),
                    player);
            }

            Network.getNetwork()
              .sendToPlayer(new VanillaParticleMessage(sPlayer.getPosX(), sPlayer.getPosY(), sPlayer.getPosZ(), ParticleTypes.INSTANT_EFFECT),
                sPlayer);
        }
    }

    /**
     * Get the list of players affected by the area teleport
     */
    private List<ServerPlayerEntity> getAffectedPlayers(final ServerPlayerEntity user)
    {
        return user.world.getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, user.getBoundingBox().grow(10, 2, 10));
    }

    @Override
    public void addInformation(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final ITextComponent guiHint = LanguageHandler.buildChatComponent("item.minecolonies.scroll_area_tp.tip");
        guiHint.setStyle(new Style().setColor(TextFormatting.DARK_GREEN));
        tooltip.add(guiHint);

        String colonyDesc = new TranslationTextComponent("item.minecolonies.scroll.colony.none").getFormattedText();

        final IColony colony = getColonyView(stack);
        if (colony != null)
        {
            colonyDesc = colony.getName();
        }

        final ITextComponent guiHint2 = new TranslationTextComponent("item.minecolonies.scroll.colony.tip", colonyDesc);
        guiHint2.setStyle(new Style().setColor(TextFormatting.GOLD));
        tooltip.add(guiHint2);
    }
}
