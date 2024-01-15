package com.minecolonies.coremod.entity.visitor;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.visitor.*;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.entity.ai.visitor.EntityAIVisitor;
import com.minecolonies.coremod.network.messages.client.ItemParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INTERACTION_VISITOR_FOOD;

/**
 * Visitor type for "regular" visitors in the tavern.
 */
public class RegularVisitorType implements IVisitorType
{
    /**
     * Extra data fields.
     */
    public static final SittingPositionData EXTRA_DATA_SITTING_POSITION = new SittingPositionData();
    public static final RecruitCostData     EXTRA_DATA_RECRUIT_COST     = new RecruitCostData();
    public static final CustomTextureData   EXTRA_DATA_CUSTOM_TEXTURE   = new CustomTextureData();

    @Override
    public ResourceLocation getId()
    {
        return ModVisitorTypes.VISITOR_TYPE_ID;
    }

    @Override
    public EntityType<? extends AbstractEntityVisitor> getEntityType()
    {
        return ModEntities.VISITOR;
    }

    @Override
    public void createStateMachine(final AbstractEntityVisitor visitor)
    {
        new EntityAIVisitor(visitor);
    }

    @Override
    public List<IVisitorExtraData<?>> getExtraDataKeys()
    {
        return List.of(EXTRA_DATA_SITTING_POSITION, EXTRA_DATA_RECRUIT_COST, EXTRA_DATA_CUSTOM_TEXTURE);
    }

    @Override
    public @NotNull InteractionResult onPlayerInteraction(final AbstractEntityVisitor visitor, final Player player, final Level level, final InteractionHand hand)
    {
        final ItemStack usedStack = player.getItemInHand(hand);
        if (ISFOOD.test(usedStack))
        {
            final ItemStack remainingItem = usedStack.finishUsingItem(level, visitor);
            if (!remainingItem.isEmpty() && remainingItem.getItem() != usedStack.getItem() && (!player.getInventory().add(remainingItem)))
            {
                InventoryUtils.spawnItemStack(
                  player.level,
                  player.getX(),
                  player.getY(),
                  player.getZ(),
                  remainingItem
                );
            }

            if (!level.isClientSide())
            {
                visitor.getCitizenData().increaseSaturation(usedStack.getItem().getFoodProperties(usedStack, visitor).getNutrition());

                visitor.playSound(SoundEvents.GENERIC_EAT, 1.5f, (float) SoundUtils.getRandomPitch(visitor.getRandom()));
                // Position needs to be centered on citizen, Eat AI wrong too?
                Network.getNetwork()
                  .sendToTrackingEntity(new ItemParticleEffectMessage(usedStack,
                    visitor.getX(),
                    visitor.getY(),
                    visitor.getZ(),
                    visitor.getXRot(),
                    visitor.getYRot(),
                    visitor.getEyeHeight()), visitor);

                visitor.getCitizenChatHandler().sendLocalizedChat(MESSAGE_INTERACTION_VISITOR_FOOD);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static class SittingPositionData extends AbstractVisitorExtraData<BlockPos>
    {
        private static final String TAG_VALUE = "value";

        public SittingPositionData()
        {
            super("sitting-pos", BlockPos.ZERO);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            BlockPosUtil.write(compound, TAG_VALUE, getValue());
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            setValue(BlockPosUtil.read(compoundTag, TAG_VALUE));
        }
    }

    public static class RecruitCostData extends AbstractVisitorExtraData<ItemStack>
    {
        private static final String TAG_VALUE = "value";

        public RecruitCostData()
        {
            super("recruit-cost", ItemStack.EMPTY);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            compound.put(TAG_VALUE, getValue().save(new CompoundTag()));
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            setValue(ItemStack.of(compoundTag.getCompound(TAG_VALUE)));
        }
    }

    public static class CustomTextureData extends AbstractVisitorExtraData<Optional<UUID>>
    {
        private static final String TAG_VALUE = "value";

        public CustomTextureData()
        {
            super("custom-texture", Optional.empty());
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag compound = new CompoundTag();
            getValue().ifPresent(val -> compound.putUUID(TAG_VALUE, val));
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag)
        {
            if (compoundTag.contains(TAG_VALUE))
            {
                setValue(Optional.of(compoundTag.getUUID(TAG_VALUE)));
            }
        }
    }
}