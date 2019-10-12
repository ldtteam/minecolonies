package com.minecolonies.coremod.colony.requestsystem.requests;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.AbstractCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolLevelConstants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.util.text.NonSiblingFormattingTextComponent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        super();
    }

    /**
     * Request for a single ItemStack.
     */
    public static class ItemStackRequest extends AbstractRequest<Stack>
    {
        public ItemStackRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Stack requested)
        {
            super(requester, token, requested);
        }

        public ItemStackRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final Stack requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            final ITextComponent combined = new NonSiblingFormattingTextComponent();
            combined.appendSibling(new TextComponentString(getRequest().getCount() + " "));
            combined.appendSibling(getRequest().getStack().getTextComponent());
            combined.getStyle().setColor(TextFormatting.BLACK);
            return combined;
        }
    }

    /**
     * Request for a list of potential candidates.
     */
    public static class ItemStackListRequest extends AbstractRequest<StackList>
    {
        /**
         * The list to display to the player.
         */
        private ImmutableList<ItemStack> displayList;

        /**
         * The Stacklist which is being requested.
         */
        private StackList stackList;

        /**
         * Constructor of the request.
         * @param requester the requester.
         * @param token the token assigned to this request.
         * @param requested the request data.
         */
        public ItemStackListRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final StackList requested)
        {
            super(requester, token, requested);
            this.displayList = ImmutableList.copyOf(requested.getStacks());
            this.stackList = requested;
        }

        /**
         * Constructor of the request.
         * @param requester the requester.
         * @param token the token assigned to this request.
         * @param state the state of the request.
         * @param requested the request data.
         */
        public ItemStackListRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final StackList requested)
        {
            super(requester, token, state, requested);
            this.displayList = ImmutableList.copyOf(requested.getStacks());
            this.stackList = requested;
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            final ITextComponent result = new NonSiblingFormattingTextComponent();
            result.appendSibling(new TextComponentTranslation(stackList.getDescription()));
            return result;
        }

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            if (displayList.isEmpty())
            {
                return ImmutableList.of();
            }
            return displayList;
        }
    }

    /**
     * Generic delivery request.
     */
    public static class DeliveryRequest extends AbstractRequest<Delivery>
    {
        public DeliveryRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Delivery requested)
        {
            super(requester, token, requested);
        }

        public DeliveryRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final Delivery requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ImmutableList<ItemStack> getDeliveries()
        {
            //This request type has no deliverable.
            //It is the delivery.
            return ImmutableList.of();
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            final ITextComponent result = new NonSiblingFormattingTextComponent();
            result.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_DELIVERY).appendSibling( new TextComponentString(getRequest().getStack().getCount() + " ")).appendSibling(getRequest().getStack().getTextComponent()));
            return result;
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
            return new ResourceLocation("minecolonies:textures/gui/citizen/delivery.png");
        }
    }

	/**
	 * An abstract implementation for crafting requests
	 */
    public abstract static class AbstractCraftingRequest<C extends AbstractCrafting> extends AbstractRequest<C>
    {

        protected AbstractCraftingRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final C requested)
        {
            super(requester, token, requested);
        }

        protected AbstractCraftingRequest(
          @NotNull final IRequester requester,
          @NotNull final IToken token,
          @NotNull final RequestState state,
          @NotNull final C requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public final ITextComponent getShortDisplayString()
        {
            final ITextComponent result = new NonSiblingFormattingTextComponent();
            final ITextComponent preType = new TextComponentTranslation(getTranslationKey());

            result.appendSibling(preType);

            preType.appendSibling(getRequest().getStack().getTextComponent());

            return result;
        }

        protected abstract String getTranslationKey();

        @NotNull
        @Override
        public final List<ItemStack> getDisplayStacks()
        {
            return ImmutableList.of();
        }

        @NotNull
        @Override
        public final ResourceLocation getDisplayIcon()
        {
            return new ResourceLocation(getDisplayIconFile());
        }

        protected abstract String getDisplayIconFile();

    }

	/**
	 * The crafting request for private crafting of a citizen
	 */
    public static class PrivateCraftingRequest extends AbstractCraftingRequest<PrivateCrafting>
    {

        protected PrivateCraftingRequest(
          @NotNull final IRequester requester,
          @NotNull final IToken token,
          @NotNull final PrivateCrafting requested)
        {
            super(requester, token, requested);
        }

        protected PrivateCraftingRequest(
          @NotNull final IRequester requester,
          @NotNull final IToken token,
          @NotNull final RequestState state, @NotNull final PrivateCrafting requested)
        {
            super(requester, token, state, requested);
        }

        @Override
        protected String getTranslationKey()
        {
            return TranslationConstants.COM_MINECOLONIES_REQUESTS_CRAFTING;
        }

        @Override
        protected String getDisplayIconFile()
        {
            return "minecolonies:textures/gui/citizen/crafting_public.png";
        }
    }

	/**
	 * The public crafting requests, used for workers that perform crafting
	 */
    public static class PublicCraftingRequest extends AbstractCraftingRequest<PublicCrafting>
    {

        protected PublicCraftingRequest(
          @NotNull final IRequester requester,
          @NotNull final IToken token,
          @NotNull final PublicCrafting requested)
        {
            super(requester, token, requested);
        }

        protected PublicCraftingRequest(
          @NotNull final IRequester requester,
          @NotNull final IToken token,
          @NotNull final RequestState state, @NotNull final PublicCrafting requested)
        {
            super(requester, token, state, requested);
        }

        @Override
        protected String getTranslationKey()
        {
            return TranslationConstants.COM_MINECOLONIES_REQUESTS_CRAFTING;
        }

        @Override
        protected String getDisplayIconFile()
        {
            return "minecolonies:textures/gui/citizen/crafting_public.png";
        }
    }

    /**
     * Generic Tool Request.
     */
    public static class ToolRequest extends AbstractRequest<Tool>
    {
        public ToolRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Tool requested)
        {
            super(requester, token, requested);
        }

        public ToolRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final Tool requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getLongDisplayString()
        {
            final ITextComponent result = new NonSiblingFormattingTextComponent();
            final ITextComponent preType = new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_TOOL_PRETYPE);

            result.appendSibling(preType);

            result.appendSibling(getRequest().getToolClass().getDisplayName());

            if (getRequest().getMinLevel() > ToolLevelConstants.TOOL_LEVEL_HAND)
            {
                result.appendText(" ");
                result.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_TOOL_PREMINLEVEL));
                result.appendText(getRequest().isArmor() ? ItemStackUtils.swapArmorGrade(getRequest().getMinLevel()) : ItemStackUtils.swapToolGrade(getRequest().getMinLevel()));
            }

            if (getRequest().getMaxLevel() < ToolLevelConstants.TOOL_LEVEL_MAXIMUM)
            {
                if (getRequest().getMinLevel() > ToolLevelConstants.TOOL_LEVEL_HAND)
                {
                    result.appendText(" ");
                    result.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_GENERAL_AND));
                }

                result.appendText(" ");
                result.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_TOOL_PREMAXLEVEL));
                result.appendText(getRequest().isArmor() ? ItemStackUtils.swapArmorGrade(getRequest().getMaxLevel()) : ItemStackUtils.swapToolGrade(getRequest().getMaxLevel()));
            }

            return result;
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            final ITextComponent result = new NonSiblingFormattingTextComponent();
            result.appendSibling(getRequest().getToolClass().getDisplayName());
            return result;
        }
    }

    /**
     * Generic food request.
     */
    public static class FoodRequest extends AbstractRequest<Food>
    {
        /**
         * Food examples to display.
         */
        private static ImmutableList<ItemStack> foodExamples;

        FoodRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Food requested)
        {
            super(requester, token, requested);
        }

        FoodRequest(
                     @NotNull final IRequester requester,
                     @NotNull final IToken token,
                     @NotNull final RequestState state,
                     @NotNull final Food requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            final ITextComponent result = new NonSiblingFormattingTextComponent();
            result.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_FOOD));
            return result;
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
                                                          final NonNullList<ItemStack> stacks = NonNullList.create();
                                                          try
                                                          {
                                                              item.getSubItems( CreativeTabs.SEARCH, stacks);
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

    /**
     * Generic smeltable ore request.
     */
    public static class SmeltAbleOreRequest extends AbstractRequest<SmeltableOre>
    {
        /**
         * Ore examples to display.
         */
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

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            if (oreExamples == null)
            {
                oreExamples =
                        ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
                            final NonNullList<ItemStack> stacks = NonNullList.create();
                            try
                            {
                                item.getSubItems(CreativeTabs.SEARCH, stacks);
                            }
                            catch (Exception ex)
                            {
                                Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName());
                            }

                            return stacks.stream().filter(IColonyManager.getInstance().getCompatibilityManager()::isOre);
                        }).collect(Collectors.toList()));
            }
            return oreExamples;
        }
    }

    /**
     * Generic burnable request.
     */
    public static class BurnableRequest extends AbstractRequest<Burnable>
    {
        /**
         * List of burnable examples.
         */
        private static ImmutableList<ItemStack> burnableExamples;

        BurnableRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Burnable requested)
        {
            super(requester, token, requested);
        }

        BurnableRequest(
                         @NotNull final IRequester requester,
                         @NotNull final IToken token,
                         @NotNull final RequestState state,
                         @NotNull final Burnable requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getShortDisplayString()
        {
            final ITextComponent result = new NonSiblingFormattingTextComponent();
            result.appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_REQUESTS_BURNABLE));
            return result;
        }

        @NotNull
        @Override
        public List<ItemStack> getDisplayStacks()
        {
            if (burnableExamples == null)
            {
                burnableExamples =
                  ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
                      final NonNullList<ItemStack> stacks = NonNullList.create();
                      try
                      {
                          item.getSubItems( CreativeTabs.SEARCH, stacks);
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
