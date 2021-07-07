package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.client.VanillaParticleMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DESC;

/**
 * Teleport scroll to teleport you back to the set colony. Requires colony permissions
 */
public class ItemScrollColonyTP extends AbstractItemScroll
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param properties the properties.
     */
    public ItemScrollColonyTP(final Properties properties)
    {
        super("scroll_tp", properties);
    }

    @Override
    protected ItemStack onItemUseSuccess(final ItemStack itemStack, final World world, final ServerPlayerEntity player)
    {
        if (world.random.nextInt(10) == 0)
        {
            // Fail
            player.displayClientMessage(new TranslationTextComponent("minecolonies.scroll.failed" + (world.random.nextInt(FAIL_RESPONSES_TOTAL) + 1)).setStyle(Style.EMPTY.withColor(
              TextFormatting.GOLD)), true);

            BlockPos pos = null;
            for (final Direction dir : Direction.Plane.HORIZONTAL)
            {
                pos = BlockPosUtil.findAround(world,
                  player.blockPosition().relative(dir, 10),
                  5,
                  5,
                  (predWorld, predPos) -> predWorld.getBlockState(predPos).getMaterial() == Material.AIR && predWorld.getBlockState(predPos.above()).getMaterial() == Material.AIR);
                if (pos != null)
                {
                    break;
                }
            }

            if (pos != null)
            {
                player.addEffect(new EffectInstance(Effects.CONFUSION, TICKS_SECOND * 7));
                player.teleportTo((ServerWorld) world, pos.getX(), pos.getY(), pos.getZ(), player.yRot, player.xRot);
            }

            SoundUtils.playSoundForPlayer(player, SoundEvents.BAT_TAKEOFF, 0.4f, 1.0f);
        }
        else
        {
            // Success
            doTeleport(player, getColony(itemStack), itemStack);
            SoundUtils.playSoundForPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 0.6f, 1.0f);
        }

        itemStack.shrink(1);
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
    public void onUseTick(World worldIn, LivingEntity entity, ItemStack stack, int count)
    {
        if (!worldIn.isClientSide && worldIn.getGameTime() % 5 == 0)
        {
            Network.getNetwork()
              .sendToTrackingEntity(new VanillaParticleMessage(entity.getX(), entity.getY(), entity.getZ(), ParticleTypes.INSTANT_EFFECT),
                entity);
            Network.getNetwork()
              .sendToPlayer(new VanillaParticleMessage(entity.getX(), entity.getY(), entity.getZ(), ParticleTypes.INSTANT_EFFECT),
                (ServerPlayerEntity) entity);
        }
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = LanguageHandler.buildChatComponent("item.minecolonies.scroll_tp.tip");
        guiHint.setStyle(Style.EMPTY.withColor(TextFormatting.DARK_GREEN));
        tooltip.add(guiHint);

        String colonyDesc = new TranslationTextComponent("item.minecolonies.scroll.colony.none").getString();

        if (stack.getOrCreateTag().contains(TAG_DESC))
        {
            colonyDesc = stack.getOrCreateTag().getString(TAG_DESC);
        }
        else
        {
            final IColony colony = getColonyView(stack);
            if (colony != null)
            {
                colonyDesc = colony.getName();
                stack.getOrCreateTag().putString(TAG_DESC, colonyDesc);
            }
        }

        final IFormattableTextComponent guiHint2 = new TranslationTextComponent("item.minecolonies.scroll.colony.tip", colonyDesc);
        guiHint2.setStyle(Style.EMPTY.withColor(TextFormatting.GOLD));
        tooltip.add(guiHint2);
    }
}
