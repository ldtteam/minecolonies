package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenItemHandler;
import com.minecolonies.api.util.*;
import com.minecolonies.core.Network;
import com.minecolonies.core.network.messages.client.BlockParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.ARMOR_DURABILITY;
import static com.minecolonies.api.research.util.ResearchConstants.TOOL_DURABILITY;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_PITCH_MULTIPLIER;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_VOLUME;

/**
 * Handles the citizens interaction with an item with the world.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class CitizenItemHandler implements ICitizenItemHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenItemHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Citizen will try to pick up a certain item.
     *
     * @param itemEntity the item he wants to pickup.
     */
    @Override
    public void tryPickupItemEntity(@NotNull final ItemEntity itemEntity)
    {
        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isClientSide)
        {
            if (itemEntity.hasPickUpDelay())
            {
                return;
            }

            final ItemStack itemStack = itemEntity.getItem();
            final ItemStack compareStack = itemStack.copy();

            if (citizen.getCitizenJobHandler().getColonyJob() == null || citizen.getCitizenJobHandler().getColonyJob().pickupSuccess(compareStack))
            {
                final ItemStack resultStack = InventoryUtils.addItemStackToItemHandlerWithResult(citizen.getInventoryCitizen(), itemStack);
                final int resultingStackSize = ItemStackUtils.isEmpty(resultStack) ? 0 : ItemStackUtils.getSize(resultStack);

                if (ItemStackUtils.isEmpty(resultStack) || ItemStackUtils.getSize(resultStack) != ItemStackUtils.getSize(compareStack))
                {
                    CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
                      citizen.blockPosition(),
                      SoundEvents.ITEM_PICKUP,
                      SoundSource.AMBIENT,
                      (float) DEFAULT_VOLUME,
                      (float) ((citizen.getRandom().nextGaussian() * DEFAULT_PITCH_MULTIPLIER + 1.0D) * 2.0D));
                    citizen.take(itemEntity, ItemStackUtils.getSize(itemStack) - resultingStackSize);

                    final ItemStack overrulingStack = itemStack.copy();
                    overrulingStack.setCount(ItemStackUtils.getSize(itemStack) - resultingStackSize);

                    if (citizen.getCitizenJobHandler().getColonyJob() != null)
                    {
                        citizen.getCitizenJobHandler().getColonyJob().onStackPickUp(overrulingStack);
                    }

                    if (ItemStackUtils.isEmpty(resultStack))
                    {
                        itemEntity.remove(Entity.RemovalReason.DISCARDED);
                    }
                }
            }
            else
            {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    /**
     * Removes the currently held item.
     */
    @Override
    public void removeHeldItem()
    {
        citizen.setItemSlot(EquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
    }

    /**
     * Sets the currently held item.
     *
     * @param hand what hand we're setting
     * @param slot from the inventory slot.
     */
    @Override
    public void setHeldItem(final InteractionHand hand, final int slot)
    {
        citizen.getCitizenData().getInventory().setHeldItem(hand, slot);
        if (hand.equals(InteractionHand.MAIN_HAND))
        {
            citizen.setItemSlot(EquipmentSlot.MAINHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
        }
        else if (hand.equals(InteractionHand.OFF_HAND))
        {
            citizen.setItemSlot(EquipmentSlot.OFFHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
        }
    }

    /**
     * Sets the currently held for mainHand item.
     *
     * @param slot from the inventory slot.
     */
    @Override
    public void setMainHeldItem(final int slot)
    {
        citizen.getCitizenData().getInventory().setHeldItem(InteractionHand.MAIN_HAND, slot);
        citizen.setItemSlot(EquipmentSlot.MAINHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * Will not break the block.
     *
     * @param blockPos Block position.
     */
    @Override
    public void hitBlockWithToolInHand(@Nullable final BlockPos blockPos)
    {
        if (blockPos == null)
        {
            return;
        }
        hitBlockWithToolInHand(blockPos, false);
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * If breakBlock is true then it will break the block (different sound and particles), and damage the tool in the citizens hand.
     *
     * @param blockPos   Block position.
     * @param breakBlock if we want to break this block.
     */
    @Override
    public void hitBlockWithToolInHand(@Nullable final BlockPos blockPos, final boolean breakBlock)
    {
        if (blockPos == null)
        {
            return;
        }

        citizen.getLookControl().setLookAt(blockPos.getX(), blockPos.getY(), blockPos.getZ(), FACING_DELTA_YAW, citizen.getMaxHeadXRot());

        citizen.swing(citizen.getUsedItemHand());

        final BlockState blockState = CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos);
        final Block block = blockState.getBlock();
        if (breakBlock)
        {
            if (!CompatibilityUtils.getWorldFromCitizen(citizen).isClientSide)
            {
                Network.getNetwork().sendToPosition(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos), BlockParticleEffectMessage.BREAK_BLOCK),
                  new PacketDistributor.TargetPoint(
                    blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_SOUND_RANGE, citizen.level.dimension()));
            }
            CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getBreakSound(),
              SoundSource.BLOCKS,
              (block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getVolume() + 1.0F) * 0.5F,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getPitch() * 0.8F);
            WorldUtil.removeBlock(CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, false);

            damageItemInHand(citizen.getUsedItemHand(), 1);
        }
        else
        {
            if (!CompatibilityUtils.getWorldFromCitizen(citizen).isClientSide)
            {
                final BlockPos vector = blockPos.subtract(citizen.blockPosition());
                final Direction facing = BlockPosUtil.directionFromDelta(vector.getX(), vector.getY(), vector.getZ()).getOpposite();

                Network.getNetwork().sendToPosition(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos), facing.ordinal()),
                  new PacketDistributor.TargetPoint(blockPos.getX(),
                    blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_PARTICLE_RANGE, citizen.level.dimension()));
            }
            CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getHitSound(),
              SoundSource.BLOCKS,
              (block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getVolume() + 1.0F) * 0.125F,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getPitch() * 0.5F);
        }
    }

    /**
     * Damage the current held item.
     *
     * @param damage amount of damage.
     */
    @Override
    public void damageItemInHand(final InteractionHand hand, final int damage)
    {
        final ItemStack heldItem = citizen.getCitizenData().getInventory().getHeldItem(hand);
        //If we hit with bare hands, ignore
        if (heldItem == null || heldItem.isEmpty())
        {
            return;
        }

        //Check if the effect exists first, to avoid unnecessary calls to random number generator.
        if (citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(TOOL_DURABILITY) > 0)
        {
            if (citizen.getRandom().nextDouble() > (1 / (1 + citizen.getCitizenColonyHandler()
                                                               .getColonyOrRegister()
                                                               .getResearchManager()
                                                               .getResearchEffects()
                                                               .getEffectStrength(TOOL_DURABILITY))))
            {
                return;
            }
        }

        //check if tool breaks
        if (citizen.getCitizenData()
              .getInventory()
              .damageInventoryItem(citizen.getCitizenData().getInventory().getHeldItemSlot(hand), damage, citizen, item -> item.broadcastBreakEvent(hand)))
        {
            if (hand == InteractionHand.MAIN_HAND)
            {
                citizen.setItemSlot(EquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
            }
            else
            {
                citizen.setItemSlot(EquipmentSlot.OFFHAND, ItemStackUtils.EMPTY);
            }
        }
    }

    /**
     * Pick up all items in a range around the citizen.
     */
    @Override
    public void pickupItems()
    {
        for (final ItemEntity item : CompatibilityUtils.getWorldFromCitizen(citizen).getEntitiesOfClass(ItemEntity.class,
          new AABB(citizen.blockPosition())
            .expandTowards(2.0F, 1.0F, 2.0F)
            .expandTowards(-2.0F, -1.0F, -2.0F)))
        {
            if (item != null && item.isAlive())
            {
                tryPickupItemEntity(item);
            }
        }
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * This will break the block (different sound and particles), and damage the tool in the citizens hand.
     *
     * @param blockPos Block position.
     */
    @Override
    public void breakBlockWithToolInHand(@Nullable final BlockPos blockPos)
    {
        if (blockPos == null)
        {
            return;
        }
        hitBlockWithToolInHand(blockPos, true);
    }

    /**
     * Handles the dropping of items from the entity.
     *
     * @param itemstack to drop.
     * @return the dropped item.
     */
    @Override
    public ItemEntity entityDropItem(@NotNull final ItemStack itemstack)
    {
        return citizen.spawnAtLocation(itemstack, 0.0F);
    }

    /**
     * Updates the armour damage after being hit.
     *
     * @param damage damage dealt.
     */
    @Override
    public void updateArmorDamage(final double damage)
    {
        if (citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(ARMOR_DURABILITY) > 0)
        {
            if (citizen.getRandom().nextDouble() > (1 / (1 + citizen.getCitizenColonyHandler()
                                                               .getColonyOrRegister()
                                                               .getResearchManager()
                                                               .getResearchEffects()
                                                               .getEffectStrength(ARMOR_DURABILITY))))
            {
                return;
            }
        }

        final int armorDmg = Math.max(1, (int) (damage / 4));
        for (int i = 0; i < 4; i++)
        {
            final EquipmentSlot equipmentSlot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i);
            final ItemStack equipment = citizen.getInventoryCitizen().getArmorInSlot(equipmentSlot);
            equipment.hurtAndBreak(armorDmg, citizen, (s) -> {
                s.broadcastBreakEvent(equipmentSlot);
                citizen.onArmorRemove(equipment, equipmentSlot);
                citizen.getInventoryCitizen().markDirty();
            });
        }
    }

    @Override
    public double applyMending(final double xp)
    {
        double localXp = xp;

        for (final EquipmentSlot equipmentSlot : EquipmentSlot.values())
        {
            if (localXp <= 0)
            {
                break;
            }

            final ItemStack tool;
            if (equipmentSlot.isArmor())
            {
                tool = citizen.getInventoryCitizen().getArmorInSlot(equipmentSlot);
            }
            else
            {
                tool = citizen.getInventoryCitizen().getHeldItem(equipmentSlot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            }

            if (!ItemStackUtils.isEmpty(tool) && tool.isDamaged() && tool.isEnchanted() && EnchantmentHelper.getEnchantments(tool).containsKey(Enchantments.MENDING))
            {
                //2 xp to heal 1 dmg
                final double dmgHealed = Math.min(localXp / 2, tool.getDamageValue());
                localXp -= dmgHealed * 2;
                tool.setDamageValue(tool.getDamageValue() - (int) Math.ceil(dmgHealed));
            }
        }

        return localXp;
    }
}
