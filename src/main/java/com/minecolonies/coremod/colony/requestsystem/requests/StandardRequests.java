package com.minecolonies.coremod.colony.requestsystem.requests;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolLevelConstants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.compatibility.CompatabilityManager.ORE_STRING;

/**
 * Final class holding all the requests for requestables inside minecolonie
 */
public final class StandardRequests
{

    /**
     * private constructor to hide the implicit public one.
     */
    private StandardRequests()
    {
    }

    public static class ItemStackRequest extends AbstractRequest<Stack>
    {

        public ItemStackRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final Stack requested)
        {
            super(requester, token, requested);
        }

        public ItemStackRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final RequestState state, @NotNull final Stack requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            return new TextComponentTranslation(getRequest().getCount() + " " + getRequest().getStack().getTextComponent().getFormattedText());
        }

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            return ImmutableList.of(getRequest().getStack().copy());
        }
    }

    public static class DeliveryRequest extends AbstractRequest<Delivery>
    {

        public DeliveryRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final Delivery requested)
        {
            super(requester, token, requested);
        }

        public DeliveryRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final RequestState state, @NotNull final Delivery requested)
        {
            super(requester, token, state, requested);
        }

        /**
         * Method to get the ItemStack used for the getDelivery.
         *
         * @return The ItemStack that the Deliveryman transports around. ItemStack.Empty means no delivery possible.
         */
        @NotNull
        @Override
        public ItemStack getDelivery()
        {
            if (getResult() != null && !ItemStackUtils.isEmpty(getResult().getStack()))
            {
                return getResult().getStack();
            }

            return ItemStackUtils.EMPTY;
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_DELIVERY).appendSibling(getDelivery().getTextComponent());
        }

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            return ImmutableList.of();
        }

        @NotNull
        @Override
        public ResourceLocation getDisplayIcon()
        {
            return new ResourceLocation("minecolonies:textures/gui/citizen/colonist_button_small.png");
        }
    }

    public static class ToolRequest extends AbstractRequest<Tool>
    {

        private ImmutableList<ItemStack> toolExamples;

        public ToolRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final Tool requested)
        {
            super(requester, token, requested);
        }

        public ToolRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final RequestState state, @NotNull final Tool requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getLongDisplayString()
        {
            final ITextComponent preType = new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_TOOL_PRETYPE);

            preType.appendSibling(getRequest().getToolClass().getDisplayName());

            if (getRequest().getMinLevel() > ToolLevelConstants.TOOL_LEVEL_HAND)
            {
                preType.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_TOOL_PREMINLEVEL));
                preType.appendText(ItemStackUtils.swapToolGrade(getRequest().getMinLevel()));
            }

            if (getRequest().getMaxLevel() < ToolLevelConstants.TOOL_LEVEL_MAXIMUM)
            {
                if (getRequest().getMinLevel() > ToolLevelConstants.TOOL_LEVEL_HAND)
                {
                    preType.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_GENERAL_AND));
                }

                preType.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_TOOL_PREMAXLEVEL));
                preType.appendText(ItemStackUtils.swapToolGrade(getRequest().getMaxLevel()));
            }

            return preType;
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            return getRequest().getToolClass().getDisplayName();
        }

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            if (toolExamples == null)
            {
                toolExamples =
                  ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
                      NonNullList<ItemStack> stacks = NonNullList.create();
                      try
                      {
                          item.getSubItems(item, null, stacks);
                      }
                      catch (Exception ex)
                      {
                          Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName());
                      }

                      return stacks.stream().filter(getRequest()::matches);
                  }).collect(Collectors.toList()));
            }

            return toolExamples;
        }
    }

    public static class FoodRequest extends AbstractRequest<Food>
    {

        private static ImmutableList<ItemStack> foodExamples;

        FoodRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final Food requested)
        {
            super(requester, token, requested);
        }

        FoodRequest(
                     @NotNull final IRequester requester,
                     @NotNull final IToken<?> token,
                     @NotNull final RequestState state,
                     @NotNull final Food requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_FOOD);
        }

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            if (foodExamples == null)
            {
                foodExamples = ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false)
                                                      .filter(item -> item instanceof ItemFood)
                                                      .flatMap(item -> {
                                                          NonNullList<ItemStack> stacks = NonNullList.create();
                                                          try
                                                          {
                                                              item.getSubItems(item, null, stacks);
                                                          }
                                                          catch (Exception ex)
                                                          {
                                                              Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName());
                                                          }

                                                          return stacks.stream();
                                                      })
                                                      .collect(Collectors.toList()));
            }

            return foodExamples;
        }
    }

    public static class SmeltAbleOreRequest extends AbstractRequest<SmeltableOre>
    {

        private static ImmutableList<ItemStack> oreExamples;

        SmeltAbleOreRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final SmeltableOre requested)
        {
            super(requester, token, requested);
        }

        SmeltAbleOreRequest(
                @NotNull final IRequester requester,
                @NotNull final IToken token,
                @NotNull final RequestState state,
                @NotNull final SmeltableOre requested)
        {
            super(requester, token, state, requested);

        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE);
        }

        @Override
        public List<ItemStack> getDisplayStacks()
        {
            if (oreExamples == null)
            {
                oreExamples =
                        ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
                            NonNullList<ItemStack> stacks = NonNullList.create();
                            try
                            {
                                item.getSubItems(item,null, stacks);
                            }
                            catch (Exception ex)
                            {
                                Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName());
                            }

                            return stacks.stream().filter(ColonyManager.getCompatabilityManager()::isOre);
                        }).collect(Collectors.toList()));
            }
            return oreExamples;
        }
    }

    public static class BurnableRequest extends AbstractRequest<Burnable>
    {

        private static ImmutableList<ItemStack> burnableExamples;

        BurnableRequest(@NotNull final IRequester requester, @NotNull final IToken<?> token, @NotNull final Burnable requested)
        {
            super(requester, token, requested);
        }

        BurnableRequest(
                         @NotNull final IRequester requester,
                         @NotNull final IToken<?> token,
                         @NotNull final RequestState state,
                         @NotNull final Burnable requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_BURNABLE);
        }

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            if (burnableExamples == null)
            {
                burnableExamples =
                  ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
                      NonNullList<ItemStack> stacks = NonNullList.create();
                      try
                      {
                          item.getSubItems(item, null, stacks);
                      }
                      catch (Exception ex)
                      {
                          Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName());
                      }

                      return stacks.stream().filter(TileEntityFurnace::isItemFuel);
                  }).collect(Collectors.toList()));
            }

            return burnableExamples;
        }
    }
}
