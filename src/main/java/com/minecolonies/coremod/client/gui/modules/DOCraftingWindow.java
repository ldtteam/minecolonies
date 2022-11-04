package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.block.MateriallyTexturedBlockManager;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.client.gui.WindowSelectRes;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Cook window class. Specifies the extras the composter has for its list.
 */
public class DOCraftingWindow extends AbstractModuleWindow
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/layouthuts/layoutdocrafting.xml";

    private static final String[] BLOCKICONS = new String[] { BLOCK1, BLOCK2, BLOCK3 };

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Input inv.
     */
    public final Container inputInventory = new SimpleContainer(MateriallyTexturedBlockManager.getInstance().getMaxTexturableComponentCount()) {};

    /**
     * The module view.
     */
    private final CraftingModuleView craftingModuleView;

    @Nullable private final ICraftingBuildingModule fakeModule;

    /**
     * Constructor for the minimum stock window view.
     *
     * @param building class extending
     */
    public DOCraftingWindow(final IBuildingView building, final CraftingModuleView view)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        this.craftingModuleView = view;

        this.fakeModule = createFakeCraftingModule();

        resourceList = this.window.findPaneOfTypeByID("resourcesstock", ScrollingList.class);

        registerButton(BLOCK1_ADD, this::addBlock1);
        registerButton(BLOCK2_ADD, this::addBlock2);
        registerButton(BLOCK3_ADD, this::addBlock3);
        registerButton(ADD, this::addRecipe);
        registerButton(BUTTON_REQUEST, this::showRequests);
    }

    /**
     * Creates a new crafting module of the correct type but not connected to the building (since we can't get the
     * real module from the view, and we're on client side anyway.  But this is safe because JEI does it too).
     */
    private ICraftingBuildingModule createFakeCraftingModule()
    {
        for (final Supplier<IBuildingModule> producer : buildingView.getBuildingType().getModuleProducers())
        {
            final IBuildingModule module = producer.get();

            if (module instanceof final ICraftingBuildingModule crafting &&
                    craftingModuleView.getId().equals(crafting.getId()))
            {
                return crafting;
            }
        }

        // this shouldn't happen, but...
        return null;
    }

    private void showRequests()
    {
        new WindowSelectRequest(this.buildingView,
                this::matchingRequest, this::reopenWithRequest).open();
    }

    /**
     * Extracts the Domum Ornamentum block type from the stack.
     * @param stack the stack to inspect.
     * @return the {@link IMateriallyTexturedBlock}, or null if the stack doesn't have one.
     */
    @Nullable
    private IMateriallyTexturedBlock getDoBlock(@NotNull final ItemStack stack)
    {
        return stack.getItem() instanceof BlockItem bi &&
                bi.getBlock() instanceof IMateriallyTexturedBlock doBlock ? doBlock : null;
    }

    /**
     * Extracts the first acceptable Domum Ornamentum stack from the given request.
     * @param request the request to inspect.
     * @return the first acceptable DO stack, or empty if this isn't a request for a DO block.
     */
    @NotNull
    private ItemStack getRequestedDoStack(@NotNull final IRequest<?> request)
    {
        if (request.getRequest() instanceof IConcreteDeliverable deliverable)
        {
            for (final ItemStack stack : deliverable.getRequestedItems())
            {
                if (getDoBlock(stack) != null)
                {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    private MaterialTextureData getTextureData(@NotNull final ItemStack stack)
    {
        if (!stack.hasTag()) return MaterialTextureData.EMPTY;
        final CompoundTag tag = stack.getOrCreateTag().getCompound("textureData");
        return MaterialTextureData.deserializeFromNBT(tag);
    }

    private boolean matchingRequest(@NotNull final IRequest<?> request)
    {
        final ItemStack stack = getRequestedDoStack(request);
        if (stack.isEmpty()) return false;

        final MaterialTextureData textureData = getTextureData(stack);
        if (fakeModule == null || textureData.isEmpty()) return false;

        final OptionalPredicate<ItemStack> validator = fakeModule.getIngredientValidator();
        for (final Block block : textureData.getTexturedComponents().values())
        {
            if (validator.test(new ItemStack(block)).orElse(false))
            {
                return true;
            }
        }

        return false;
    }

    private void reopenWithRequest(@Nullable final IRequest<?> request)
    {
        if (request != null)
        {
            final ItemStack stack = getRequestedDoStack(request);
            final IMateriallyTexturedBlock block = getDoBlock(stack);
            final MaterialTextureData textureData = getTextureData(stack);
            if (block != null && !textureData.isEmpty())     // should always be true due to predicate, but hey you never know...
            {
                int slot = 0;
                for (final IMateriallyTexturedBlockComponent component : block.getComponents())
                {
                    final ItemStack componentBlock = new ItemStack(textureData.getTexturedComponents().getOrDefault(component.getId(), Blocks.AIR));
                    inputInventory.setItem(slot, componentBlock);
                    findPaneOfTypeByID(BLOCKICONS[slot], ItemIcon.class).setItem(componentBlock);
                    ++slot;
                }
                for (; slot < 3; ++slot)
                {
                    inputInventory.setItem(slot, ItemStack.EMPTY);
                    findPaneOfTypeByID(BLOCKICONS[slot], ItemIcon.class).setItem(ItemStack.EMPTY);
                }
                updateStockList();
            }
        }

        open();
    }

    private void addRecipe()
    {
        final List<ArchitectsCutterRecipe> list = Minecraft.getInstance().level.getRecipeManager().getRecipesFor(ModRecipeTypes.ARCHITECTS_CUTTER.get(), inputInventory, Minecraft.getInstance().level);
        final Map<Integer, List<Integer>> map = new HashMap<>();

        if (inputInventory.isEmpty() || list.isEmpty())
        {
            return;
        }

        for (final ArchitectsCutterRecipe recipe : list)
        {
            final ItemStack result = recipe.assemble(inputInventory).copy();
            final IMateriallyTexturedBlock doBlock = getDoBlock(result);
            if (doBlock != null)
            {
                final int components = doBlock.getComponents().size();
                final List<Integer> inputList = map.getOrDefault(components, new ArrayList<>());
                inputList.add(list.indexOf(recipe));
                map.put(components, inputList);
            }
        }

        final List<ItemStorage> input = new ArrayList<>();
        for (int i = 0; i < inputInventory.getContainerSize(); ++i)
        {
            final ItemStack atPos = inputInventory.getItem(i).copy();
            if (!ItemStackUtils.isEmpty(atPos))
            {
                atPos.setCount(1);
                input.add(new ItemStorage(atPos));
            }
        }

        final List<Integer> inputIndizes = map.get(input.size());
        if (inputIndizes == null)
        {
            return;
        }

        final List<ItemStack> additionalOutput = new ArrayList<>();
        for (int i = 1; i < inputIndizes.size(); i++)
        {
            additionalOutput.add(list.get(inputIndizes.get(i)).assemble(inputInventory).copy());
        }

        final IRecipeStorage storage = StandardFactoryController.getInstance().getNewInstance(
          TypeConstants.RECIPE,
          StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
          input,
          3,
          list.get(inputIndizes.get(0)).assemble(inputInventory).copy(),
          Blocks.AIR,
          null,
          com.minecolonies.api.crafting.ModRecipeTypes.MULTI_OUTPUT_ID,
          additionalOutput,
          new ArrayList<>());

        Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(buildingView, false, storage, craftingModuleView.getId()));
    }

    /**
     * Add the stack.
     */
    private void addBlock1()
    {
        new WindowSelectRes(this, buildingView, (stack) -> MateriallyTexturedBlockManager.getInstance().doesItemStackContainsMaterialForSlot(0, stack), (stack, qty) -> {
            inputInventory.setItem(0, stack);
            findPaneOfTypeByID(BLOCK1, ItemIcon.class).setItem(stack);
            updateStockList();
        }, false).open();
    }

    /**
     * Add the stack.
     */
    private void addBlock2()
    {
        new WindowSelectRes(this, buildingView, (stack) -> MateriallyTexturedBlockManager.getInstance().doesItemStackContainsMaterialForSlot(1, stack), (stack, qty) -> {
            inputInventory.setItem(1, stack);
            findPaneOfTypeByID(BLOCK2, ItemIcon.class).setItem(stack);
            updateStockList();
        }, false).open();
    }

    /**
     * Add the stack.
     */
    private void addBlock3()
    {
        new WindowSelectRes(this, buildingView, (stack) -> MateriallyTexturedBlockManager.getInstance().doesItemStackContainsMaterialForSlot(2, stack), (stack, qty) -> {
            inputInventory.setItem(2, stack);
            updateStockList();
            findPaneOfTypeByID(BLOCK3, ItemIcon.class).setItem(stack);
        }, false).open();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStockList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateStockList()
    {
        resourceList.enable();
        resourceList.show();

        final List<ArchitectsCutterRecipe> list = Minecraft.getInstance().level.getRecipeManager().getRecipesFor(ModRecipeTypes.ARCHITECTS_CUTTER.get(), inputInventory, Minecraft.getInstance().level);
        int inputCount = 0;
        for (int i = 0; i < inputInventory.getContainerSize(); i++)
        {
            if (!inputInventory.getItem(i).isEmpty())
            {
                inputCount++;
            }
        }

        final List<ArchitectsCutterRecipe> filteredList = new ArrayList<>();
        for (final ArchitectsCutterRecipe recipe : list)
        {
            final ItemStack result = recipe.assemble(inputInventory).copy();
            final IMateriallyTexturedBlock doBlock = getDoBlock(result);
            if (doBlock != null && doBlock.getComponents().size() == inputCount)
            {
                filteredList.add(recipe);
            }
        }

        //Creates a dataProvider for the unemployed resourceList.
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return filteredList.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = filteredList.get(index).assemble(inputInventory).copy();

                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(resource.getHoverName());
                rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Text.class).setText(Component.literal(String.valueOf(resource.getCount())));

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }
}
