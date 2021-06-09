package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.settings.FollowModeSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.network.messages.client.VanillaParticleMessage;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

import net.minecraft.item.Item.Properties;

/**
 * Magic scroll which summons guards to the users aid, with a limited duration. Only works within the same world as the colony.
 */
public class ItemScrollGuardHelp extends AbstractItemScroll
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param properties the properties.
     */
    public ItemScrollGuardHelp(final Properties properties)
    {
        super("scroll_guard_help", properties);
    }

    @Override
    protected ItemStack onItemUseSuccess(
      final ItemStack itemStack, final World world, final ServerPlayerEntity player)
    {
        final IColony colony = getColony(itemStack);
        final BlockPos buildingPos = BlockPosUtil.read(itemStack.getTag(), TAG_BUILDING_POS);
        final IBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
        if (!(building instanceof AbstractBuildingGuards))
        {
            LanguageHandler.sendPlayerMessage(player, "minecolonies.scroll.noguardbuilding");
            return itemStack;
        }

        itemStack.shrink(1);
        final List<ICitizenData> guards = building.getAssignedCitizen();

        if (world.random.nextInt(10) == 0 || colony.getWorld() != world)
        {
            // Fail
            final LlamaEntity entity = EntityType.LLAMA.create(world);
            entity.setPos(player.getX(), player.getY(), player.getZ());
            world.addFreshEntity(entity);

            player.displayClientMessage(new TranslationTextComponent("minecolonies.scroll.failed" + (world.random.nextInt(FAIL_RESPONSES_TOTAL) + 1)).setStyle(Style.EMPTY.withColor(
              TextFormatting.GOLD)), true);

            SoundUtils.playSoundForPlayer(player, SoundEvents.EVOKER_CAST_SPELL, 0.5f, 1.0f);
            return itemStack;
        }
        else
        {
            for (final ICitizenData citizenData : guards)
            {
                final AbstractJobGuard job = citizenData.getJob(AbstractJobGuard.class);
                if (job != null && job.getWorkerAI() != null && !((AbstractEntityAIGuard) job.getWorkerAI()).hasTool())
                {
                    continue;
                }

                if (citizenData.getEntity().isPresent())
                {
                    if (citizenData.getEntity().get().getCitizenDiseaseHandler().isSick())
                    {
                        continue;
                    }

                    citizenData.getEntity().get().remove();
                }

                colony.getCitizenManager().spawnOrCreateCivilian(citizenData, world, player.blockPosition(), true);
                citizenData.setNextRespawnPosition(buildingPos);

                ((AbstractBuildingGuards) building).getSetting(AbstractBuildingGuards.GUARD_TASK).set(GuardTaskSetting.FOLLOW);
                ((AbstractBuildingGuards) building).setPlayerToFollow(player);
                final FollowModeSetting grouping = ((AbstractBuildingGuards) building).getSetting(AbstractBuildingGuards.FOLLOW_MODE);
                if (grouping.getValue().equals(FollowModeSetting.LOOSE))
                {
                    grouping.trigger();
                }

                citizenData.setSaturation(100);

                colony.getPackageManager().addCloseSubscriber(player);

                if (job != null && job.getWorkerAI() != null)
                {
                    final long spawnTime = world.getGameTime() + TICKS_SECOND * 900;

                    // Timed despawn
                    job.getWorkerAI().registerTarget(new AIOneTimeEventTarget(() ->
                    {
                        if (world.getGameTime() - spawnTime > 0)
                        {
                            ((AbstractBuildingGuards) building).getSetting(AbstractBuildingGuards.GUARD_TASK).set(GuardTaskSetting.PATROL);
                            citizenData.getEntity().ifPresent(Entity::remove);
                            colony.getPackageManager().removeCloseSubscriber(player);
                            return true;
                        }
                        return false;
                    }
                      , AIWorkerState.DECIDE));
                }
            }

            SoundUtils.playSoundForPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.3f, 1.0f);
        }


        return itemStack;
    }

    @Override
    protected boolean needsColony()
    {
        return true;
    }

    @Override
    @NotNull
    public ActionResultType useOn(ItemUseContext ctx)
    {
        final ActionResultType result = super.useOn(ctx);

        if (ctx.getLevel().isClientSide)
        {
            return result;
        }

        final TileEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (te instanceof TileEntityColonyBuilding && ctx.getPlayer() != null)
        {
            final IBuilding building = ((TileEntityColonyBuilding) te).getColony().getBuildingManager().getBuilding(ctx.getClickedPos());
            if (!(building instanceof AbstractBuildingGuards))
            {
                LanguageHandler.sendPlayerMessage(ctx.getPlayer(), "minecolonies.scroll.noguardbuilding");
            }
        }

        return result;
    }

    @Override
    public void onUseTick(World worldIn, LivingEntity entity, ItemStack stack, int count)
    {
        if (!worldIn.isClientSide && worldIn.getGameTime() % 5 == 0)
        {
            Network.getNetwork()
              .sendToTrackingEntity(new VanillaParticleMessage(entity.getX(), entity.getY(), entity.getZ(), ParticleTypes.ENCHANT),
                entity);
            Network.getNetwork()
              .sendToPlayer(new VanillaParticleMessage(entity.getX(), entity.getY(), entity.getZ(), ParticleTypes.ENCHANT),
                (ServerPlayerEntity) entity);
        }
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = LanguageHandler.buildChatComponent("item.minecolonies.scroll_guard_help.tip");
        guiHint.setStyle(Style.EMPTY.withColor(TextFormatting.DARK_GREEN));
        tooltip.add(guiHint);

        String colonyDesc = new TranslationTextComponent("item.minecolonies.scroll.colony.none").getString();

        final IColony colony = getColonyView(stack);
        if (colony != null)
        {
            colonyDesc = colony.getName();
        }

        final IFormattableTextComponent guiHint2 = new TranslationTextComponent("item.minecolonies.scroll.colony.tip", colonyDesc);
        guiHint2.setStyle(Style.EMPTY.withColor(TextFormatting.GOLD));
        tooltip.add(guiHint2);
    }
}
