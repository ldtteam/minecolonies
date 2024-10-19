package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.visitor.*;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.core.entity.ai.visitor.EntityAIVisitor;
import com.minecolonies.core.network.messages.client.ItemParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_COLONY_VISITOR_DIED;
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

    @Override
    public ResourceLocation getId()
    {
        return ModVisitorTypes.VISITOR_TYPE_ID;
    }

    @Override
    public Function<Level, AbstractEntityVisitor> getEntityCreator()
    {
        return ModEntities.VISITOR::create;
    }

    @Override
    public void createStateMachine(final AbstractEntityVisitor visitor)
    {
        new EntityAIVisitor(visitor);
    }

    @Override
    public List<IVisitorExtraData<?>> getExtraDataKeys()
    {
        return List.of(EXTRA_DATA_SITTING_POSITION, EXTRA_DATA_RECRUIT_COST);
    }

    @Override
    @NotNull
    public InteractionResult onPlayerInteraction(final AbstractEntityVisitor visitor, final Player player, final Level level, final InteractionHand hand)
    {
        IVisitorType.super.onPlayerInteraction(visitor, player, level, hand);

        final ItemStack usedStack = player.getItemInHand(hand);
        if (ISFOOD.test(usedStack))
        {
            final ItemStack remainingItem = usedStack.finishUsingItem(level, visitor);
            if (!remainingItem.isEmpty() && remainingItem.getItem() != usedStack.getItem() && (!player.getInventory().add(remainingItem)))
            {
                InventoryUtils.spawnItemStack(player.level, player.getX(), player.getY(), player.getZ(), remainingItem);
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

                MessageUtils.forCitizen(visitor, MESSAGE_INTERACTION_VISITOR_FOOD).sendTo(player);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onDied(final VisitorCitizen visitor, final DamageSource cause)
    {
        IColony colony = visitor.getCitizenColonyHandler().getColony();
        if (colony != null && visitor.getCitizenData() != null)
        {
            colony.getVisitorManager().removeCivilian(visitor.getCitizenData());
            if (visitor.getCitizenData().getHomeBuilding() instanceof final TavernBuildingModule tavern)
            {
                tavern.setNoVisitorTime(visitor.level.getRandom().nextInt(5000) + 30000);
            }

            final String deathLocation = BlockPosUtil.getString(visitor.blockPosition());

            MessageUtils.format(MESSAGE_INFO_COLONY_VISITOR_DIED, visitor.getCitizenData().getName(), cause.getMsgId(), deathLocation)
              .withPriority(MessagePriority.DANGER)
              .sendTo(colony)
              .forManagers();
        }
    }

    /**
     * Extra data containing the sitting position of the visitor.
     */
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

    /**
     * Extra data containing the recruitment cost of the visitor.
     */
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
}