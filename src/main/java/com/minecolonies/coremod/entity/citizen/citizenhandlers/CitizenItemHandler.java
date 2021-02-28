package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenItemHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.client.BlockParticleEffectMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;
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
        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote)
        {
            if (itemEntity.cannotPickup())
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
                      citizen.getPosition(),
                      SoundEvents.ENTITY_ITEM_PICKUP,
                      SoundCategory.AMBIENT,
                      (float) DEFAULT_VOLUME,
                      (float) ((citizen.getRandom().nextGaussian() * DEFAULT_PITCH_MULTIPLIER + 1.0D) * 2.0D));
                    citizen.onItemPickup(itemEntity, ItemStackUtils.getSize(itemStack) - resultingStackSize);

                    final ItemStack overrulingStack = itemStack.copy();
                    overrulingStack.setCount(ItemStackUtils.getSize(itemStack) - resultingStackSize);

                    if (citizen.getCitizenJobHandler().getColonyJob() != null)
                    {
                        citizen.getCitizenJobHandler().getColonyJob().onStackPickUp(overrulingStack);
                    }

                    if (ItemStackUtils.isEmpty(resultStack))
                    {
                        itemEntity.remove();
                    }
                }
            }
            else
            {
                itemEntity.remove();
            }
        }
    }

    /**
     * Removes the currently held item.
     */
    @Override
    public void removeHeldItem()
    {
        citizen.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStackUtils.EMPTY);
    }

    /**
     * Sets the currently held item.
     *
     * @param hand what hand we're setting
     * @param slot from the inventory slot.
     */
    @Override
    public void setHeldItem(final Hand hand, final int slot)
    {
        citizen.getCitizenData().getInventory().setHeldItem(hand, slot);
        if (hand.equals(Hand.MAIN_HAND))
        {
            citizen.setItemStackToSlot(EquipmentSlotType.MAINHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
        }
        else if (hand.equals(Hand.OFF_HAND))
        {
            citizen.setItemStackToSlot(EquipmentSlotType.OFFHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
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
        citizen.getCitizenData().getInventory().setHeldItem(Hand.MAIN_HAND, slot);
        citizen.setItemStackToSlot(EquipmentSlotType.MAINHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
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

        citizen.getLookController().setLookPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), FACING_DELTA_YAW, citizen.getVerticalFaceSpeed());

        citizen.swingArm(citizen.getActiveHand());

        final BlockState blockState = CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos);
        final Block block = blockState.getBlock();
        if (breakBlock)
        {
            if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote)
            {
                Network.getNetwork().sendToPosition(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos), BlockParticleEffectMessage.BREAK_BLOCK),
                  new PacketDistributor.TargetPoint(
                    blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_SOUND_RANGE, citizen.world.getDimensionKey()));
            }
            CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getVolume(),
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getPitch());
            WorldUtil.removeBlock(CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, false);

            damageItemInHand(citizen.getActiveHand(), 1);
        }
        else
        {
            if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote)
            {
                final BlockPos vector = blockPos.subtract(citizen.getPosition());
                final Direction facing = Direction.getFacingFromVector(vector.getX(), vector.getY(), vector.getZ()).getOpposite();

                Network.getNetwork().sendToPosition(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos), facing.ordinal()),
                  new PacketDistributor.TargetPoint(blockPos.getX(),
                    blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_PARTICLE_RANGE, citizen.world.getDimensionKey()));
            }
            CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getVolume(),
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getPitch());
        }
    }

    /**
     * Damage the current held item.
     *
     * @param damage amount of damage.
     */
    @Override
    public void damageItemInHand(final Hand hand, final int damage)
    {
        final ItemStack heldItem = citizen.getCitizenData().getInventory().getHeldItem(hand);
        //If we hit with bare hands, ignore
        if (heldItem == null || heldItem.isEmpty())
        {
            return;
        }

        //Check if the effect exists first, to avoid unnecessary calls to random number generator.
        if(citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(TOOL_DURABILITY) > 0)
        {
            if (citizen.getRandom().nextDouble() > (1 / (1 + citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(TOOL_DURABILITY))))
            {
                return;
            }
        }

        //check if tool breaks
        if (citizen.getCitizenData()
              .getInventory()
              .damageInventoryItem(citizen.getCitizenData().getInventory().getHeldItemSlot(hand), damage, citizen, item -> item.sendBreakAnimation(hand)))
        {
            if (hand == Hand.MAIN_HAND)
            {
                citizen.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStackUtils.EMPTY);
            }
            else
            {
                citizen.setItemStackToSlot(EquipmentSlotType.OFFHAND, ItemStackUtils.EMPTY);
            }
        }
    }

    /**
     * Pick up all items in a range around the citizen.
     */
    @Override
    public void pickupItems()
    {
        for (final ItemEntity item : CompatibilityUtils.getWorldFromCitizen(citizen).getLoadedEntitiesWithinAABB(ItemEntity.class,
                                                             new AxisAlignedBB(citizen.getPosition())
                                                               .expand(2.0F, 1.0F, 2.0F)
                                                               .expand(-2.0F, -1.0F, -2.0F)))
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
        return citizen.entityDropItem(itemstack, 0.0F);
    }

    /**
     * Updates the armour damage after being hit.
     *
     * @param damage damage dealt.
     */
    @Override
    public void updateArmorDamage(final double damage)
    {
        for (final ItemStack stack : citizen.getArmorInventoryList())
        {
            if (ItemStackUtils.isEmpty(stack) || !(stack.getItem() instanceof ArmorItem))
            {
                continue;
            }

            if(citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARMOR_DURABILITY) > 0)
            {
                if (citizen.getRandom().nextDouble() > (1 / (1 + citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARMOR_DURABILITY))))
                {
                    return;
                }
            }

            stack.damageItem(Math.max(1, (int) (damage / 4)), citizen, (i) -> {
                i.sendBreakAnimation(Hand.MAIN_HAND);
            });
        }
    }

    @Override
    public double applyMending(final double xp)
    {
        double localXp = xp;

        final int toolSlot =
          InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventoryCitizen(), stack -> stack.isEnchanted() && EnchantmentHelper.getEnchantments(stack).containsKey(
            Enchantments.MENDING));
        if (toolSlot == -1)
        {
            return localXp;
        }

        final ItemStack tool = citizen.getInventoryCitizen().getStackInSlot(toolSlot);
        if (!ItemStackUtils.isEmpty(tool) && tool.isDamaged())
        {
            //2 xp to heal 1 dmg
            final double dmgHealed = Math.min(localXp / 2, tool.getDamage());
            localXp -= dmgHealed * 2;
            tool.setDamage(tool.getDamage() - (int) Math.ceil(dmgHealed));
        }

        return localXp;
    }
}
