package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.block.MateriallyTexturedBlockManager;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.recipe.architectscutter.ArchitectsCutterRecipe;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.client.gui.WindowSelectRes;
import com.minecolonies.core.colony.buildings.moduleviews.DOCraftingModuleView;
import com.minecolonies.core.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.minecolonies.core.util.DomumOrnamentumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.DOCRAFTING_BLOCK;
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

    /**
     * Inputs scrolling list.
     */
    private final ScrollingList inputs;
    private final List<ItemIcon> inputIcons = new ArrayList<>();

    /**
     * Outputs scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Input inv.
     */
    public final Container inputInventory = new SimpleContainer(MateriallyTexturedBlockManager.getInstance().getMaxTexturableComponentCount()) {};

    /**
     * The module view.
     */
    private final DOCraftingModuleView craftingModuleView;

    /**
     * The ingredient validator.
     */
    private final OptionalPredicate<ItemStack> validator;

    /**
     * Constructor for the minimum stock window view.
     *
     * @param building class extending
     */
    public DOCraftingWindow(final IBuildingView building, final DOCraftingModuleView view)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        this.craftingModuleView = view;

        validator = craftingModuleView.getIngredientValidator();
        inputs = this.window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        resourceList = this.window.findPaneOfTypeByID("resourcesstock", ScrollingList.class);

        inputIcons.addAll(Collections.nCopies(inputInventory.getContainerSize(), null));
        inputs.setDataProvider(inputInventory::getContainerSize, this::updateInputs);

        registerButton("add", this::addRecipe);
        registerButton(BUTTON_REQUEST, this::showRequests);
    }

    private void updateInputs(final int index, final Pane rowPane)
    {
        rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class)
                .setText(Component.translatable(DOCRAFTING_BLOCK, index + 1));

        final ItemIcon icon = rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class);
        inputIcons.set(index, icon);

        rowPane.findPaneOfTypeByID(RESOURCE_ADD, Button.class).setHandler(btn ->
        {
            new WindowSelectRes(this, (stack) -> MateriallyTexturedBlockManager.getInstance().doesItemStackContainsMaterialForSlot(index, stack), (stack, qty) -> {
                inputInventory.setItem(index, stack);
                icon.setItem(stack);
                updateStockList();
            }, false).open();
        });
    }

    private void showRequests()
    {
        new WindowSelectRequest(this.buildingView, this::matchingRequest, this::reopenWithRequest).open();
    }

    private boolean matchingRequest(@NotNull final IRequest<?> request)
    {
        final ItemStack stack = DomumOrnamentumUtils.getRequestedStack(request);
        if (stack.isEmpty()) return false;

        final MaterialTextureData textureData = MaterialTextureData.deserializeFromItemStack(stack);
        if (textureData.isEmpty()) return false;

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
            final ItemStack stack = DomumOrnamentumUtils.getRequestedStack(request);
            final IMateriallyTexturedBlock block = DomumOrnamentumUtils.getBlock(stack);
            final MaterialTextureData textureData = MaterialTextureData.deserializeFromItemStack(stack);
            if (block != null && !textureData.isEmpty())     // should always be true due to predicate, but hey you never know...
            {
                int slot = 0;
                for (final IMateriallyTexturedBlockComponent component : block.getComponents())
                {
                    final ItemStack componentBlock = new ItemStack(textureData.getTexturedComponents().getOrDefault(component.getId(), Blocks.AIR));
                    inputInventory.setItem(slot, componentBlock);
                    inputIcons.get(slot).setItem(componentBlock);
                    ++slot;
                }
                for (; slot < inputInventory.getContainerSize(); ++slot)
                {
                    inputInventory.setItem(slot, ItemStack.EMPTY);
                    inputIcons.get(slot).setItem(ItemStack.EMPTY);
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
            final ItemStack result = recipe.assemble(inputInventory, Minecraft.getInstance().level.registryAccess()).copy();
            final IMateriallyTexturedBlock doBlock = DomumOrnamentumUtils.getBlock(result);
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
            additionalOutput.add(list.get(inputIndizes.get(i)).assemble(inputInventory, Minecraft.getInstance().level.registryAccess()).copy());
        }

        final IRecipeStorage storage = StandardFactoryController.getInstance().getNewInstance(
          TypeConstants.RECIPE,
          StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
          input,
          3,
          list.get(inputIndizes.get(0)).assemble(inputInventory, Minecraft.getInstance().level.registryAccess()).copy(),
          Blocks.AIR,
          null,
          com.minecolonies.api.crafting.ModRecipeTypes.MULTI_OUTPUT_ID,
          additionalOutput,
          new ArrayList<>());

        new AddRemoveRecipeMessage(buildingView, false, storage, craftingModuleView.getProducer().getRuntimeID()).sendToServer();
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
            final ItemStack result = recipe.assemble(inputInventory, Minecraft.getInstance().level.registryAccess()).copy();
            final IMateriallyTexturedBlock doBlock = DomumOrnamentumUtils.getBlock(result);
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
                final ItemStack resource = filteredList.get(index).assemble(inputInventory, Minecraft.getInstance().level.registryAccess()).copy();

                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(resource.getHoverName());
                rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Text.class).setText(Component.literal(String.valueOf(resource.getCount())));

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }
}
