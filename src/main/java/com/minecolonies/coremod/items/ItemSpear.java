package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.SpearEntity;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.ModelSpear;
import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemSpear extends TridentItem
{
    public ItemSpear(final Properties properties)
    {
        super(properties.durability(250).tab(ModCreativeTabs.MINECOLONIES).setISTER(() -> SpearTileRenderer::new));
        setRegistryName(new ResourceLocation(Constants.MOD_ID, "spear"));
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull LivingEntity entityLiving, int timeLeft)
    {
        if (entityLiving instanceof PlayerEntity)
        {
            PlayerEntity playerEntity = (PlayerEntity) entityLiving;
            int i = this.getUseDuration(stack) - timeLeft;
            if (i >= 10)
            {
                if (!worldIn.isClientSide)
                {
                    stack.hurtAndBreak(1, playerEntity, playerEntity1 -> playerEntity1.broadcastBreakEvent(entityLiving.getUsedItemHand()));
                    SpearEntity spearProjectileEntity = new SpearEntity(worldIn, playerEntity, stack);
                    spearProjectileEntity.shootFromRotation(playerEntity, playerEntity.xRot, playerEntity.yRot, 0.0F, 2.5F, 1.0F);
                    if (playerEntity.abilities.instabuild)
                    {
                        spearProjectileEntity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                    }

                    worldIn.addFreshEntity(spearProjectileEntity);
                    worldIn.playSound(null, spearProjectileEntity, SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    if (!playerEntity.abilities.instabuild)
                    {
                        playerEntity.inventory.removeItem(stack);
                    }
                }

                SoundEvent soundEvent = SoundEvents.TRIDENT_THROW;
                playerEntity.awardStat(Stats.ITEM_USED.get(this));
                worldIn.playSound(null, playerEntity, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand)
    {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1)
        {
            return ActionResult.fail(itemstack);
        }
        else
        {
            playerEntity.startUsingItem(hand);
            return ActionResult.consume(itemstack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SpearTileRenderer extends ItemStackTileEntityRenderer
    {
        private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/spear.png");
        private final        ModelSpear       model   = new ModelSpear();

        @Override
        public void renderByItem(
          ItemStack stack,
          @NotNull ItemCameraTransforms.TransformType transformType,
          MatrixStack matrixStack,
          @NotNull IRenderTypeBuffer buffer,
          int combinedLight,
          int combinedOverlay)
        {
            matrixStack.pushPose();
            model.renderToBuffer(matrixStack, ItemRenderer.getFoilBuffer(buffer, model.renderType(TEXTURE), false, stack.hasFoil()), combinedLight, combinedOverlay, 1, 1, 1, 1);
            matrixStack.popPose();
        }
    }
}
