package com.minecolonies.core.client.gui.generic;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.minecolonies.core.client.gui.blockui.RotatingItemIcon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Rendering a resource item in a list.
 */
public class ResourceItem
{
    /**
     * Color constants for resources list.
     */
    public static final int RED       = Color.getByName("red", 0);
    public static final int DARKGREEN = Color.getByName("darkgreen", 0);
    public static final int BLACK     = Color.getByName("black", 0);
    public static final int ORANGE    = Color.getByName("orange", 0);

    /**
     * Default constructor.
     */
    private ResourceItem()
    {
    }

    /**
     * Update method to render a single resource item.
     * If the
     *
     * @param resource the resource to update the pane with.
     * @param index    the item index.
     * @param rowPane  the parenting pane.
     * @param <T>      the generic type of the resource instance.
     */
    public static <T extends Resource> void updateResourcePane(final T resource, final Player player, final int index, @NotNull final Pane rowPane)
    {
        final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
        final Text resourceMissingLabel = rowPane.findPaneOfTypeByID(RESOURCE_MISSING, Text.class);
        final Text neededLabel = rowPane.findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Text.class);
        final Button addButton = rowPane.findPaneOfTypeByID(RESOURCE_ADD, Button.class);

        ResourceAvailability availabilityStatus = resource.getAvailabilityStatus();
        if (!availabilityStatus.equals(ResourceAvailability.NOT_NEEDED) && player.isCreative())
        {
            availabilityStatus = ResourceAvailability.HAVE_ENOUGH;
        }

        switch (availabilityStatus)
        {
            case DONT_HAVE:
                addButton.disable();
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
                break;
            case NEED_MORE:
                addButton.enable();
                resourceLabel.setColors(ORANGE);
                resourceMissingLabel.setColors(ORANGE);
                neededLabel.setColors(ORANGE);
                break;
            case HAVE_ENOUGH:
                addButton.enable();
                resourceLabel.setColors(DARKGREEN);
                resourceMissingLabel.setColors(DARKGREEN);
                neededLabel.setColors(DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                addButton.disable();
                resourceLabel.setColors(BLACK);
                resourceMissingLabel.setColors(BLACK);
                neededLabel.setColors(BLACK);
                break;
        }

        // Position the addResource Button to the right
        final int buttonX = rowPane.getWidth() - addButton.getWidth() - (rowPane.getHeight() - addButton.getHeight()) / 2;
        final int buttonY = rowPane.getHeight() - addButton.getHeight() - 2;
        addButton.setPosition(buttonX, buttonY);

        resourceLabel.setText(resource.getName());
        final int missing = resource.getAmountPlayer() + resource.getAmountAvailable() - resource.getAmount();
        if (missing < 0)
        {
            resourceMissingLabel.setText(Component.literal(Integer.toString(missing)));
        }
        else
        {
            resourceMissingLabel.clearText();
        }

        neededLabel.setText(Component.literal(resource.getAmountAvailable() + " / " + resource.getAmount()));
        rowPane.findPaneOfTypeByID(RESOURCE_ID, Text.class).setText(Component.literal(Integer.toString(index)));
        rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class).setText(Component.literal(Integer.toString(resource.getAmount() - resource.getAmountAvailable())));

        RotatingItemIcon icon = rowPane.findPaneOfTypeByID(RESOURCE_ICON, RotatingItemIcon.class);
        if (icon == null)
        {
            icon = new RotatingItemIcon();
            icon.setID(RESOURCE_ICON);
            icon.setPosition(1, 1);
            icon.setSize(16, 16);
            icon.putInside((View) rowPane);
        }
        icon.setItems(resource.getIcon());
    }

    /**
     * Availability status of the resource. according to the builder's chest, inventory and the player's inventory
     */
    public enum ResourceAvailability
    {
        NOT_NEEDED(5),
        IN_DELIVERY(4),
        DONT_HAVE(3),
        NEED_MORE(2),
        HAVE_ENOUGH(1);

        /**
         * The order this availability item should show up in a GUI.
         */
        private final int order;

        /**
         * Default constructor.
         *
         * @param order the order this availability item should show up in a GUI.
         */
        ResourceAvailability(final int order)
        {
            this.order = order;
        }

        /**
         * Get the order this availability item should show up in a GUI.
         */
        public Integer getOrder()
        {
            return order;
        }
    }

    /**
     * Defines a basic resource class, intended for providing all the info on how to render a resource.
     */
    public interface Resource
    {
        /**
         * Get the name of the requested items.
         *
         * @return the component.
         */
        Component getName();

        /**
         * Get the item stacks to show for the icon.
         *
         * @return the item stacks.
         */
        List<ItemStack> getIcon();

        /**
         * Get the availability status for this resource.
         *
         * @return the amount.
         */
        default ResourceAvailability getAvailabilityStatus()
        {
            if (getAmount() > getAmountAvailable())
            {
                if (getAmountPlayer() == 0)
                {
                    if (getAmountInDelivery() > 0)
                    {
                        return ResourceAvailability.IN_DELIVERY;
                    }
                    return ResourceAvailability.DONT_HAVE;
                }
                if (getAmountPlayer() < (getAmount() - getAmountAvailable()))
                {
                    if (getAmountInDelivery() > 0)
                    {
                        return ResourceAvailability.IN_DELIVERY;
                    }
                    return ResourceAvailability.NEED_MORE;
                }
                return ResourceAvailability.HAVE_ENOUGH;
            }
            return ResourceAvailability.NOT_NEEDED;
        }

        /**
         * Get how many items are needed.
         *
         * @return the amount.
         */
        int getAmount();

        /**
         * Get how many items are currently already available.
         *
         * @return the amount.
         */
        int getAmountAvailable();

        /**
         * Get how many items the player has in its inventory.
         *
         * @return the amount.
         */
        int getAmountPlayer();

        /**
         * Get how many items are currently in delivery.
         *
         * @return the amount.
         */
        int getAmountInDelivery();
    }

    /**
     * Comparator class for resources.
     * <p>
     * This is use in the gui to order the list of resources needed.
     */
    public static class ResourceComparator implements Comparator<Resource>, Serializable
    {
        @Serial
        private static final long serialVersionUID = 1;

        /**
         * Compare to resource together.
         * <p>
         * We want the item available in the player inventory first and the one not needed last In alphabetical order otherwise
         */
        @Override
        public int compare(final Resource resource1, final Resource resource2)
        {
            final ResourceAvailability status1 = resource1.getAvailabilityStatus();
            final ResourceAvailability status2 = resource2.getAvailabilityStatus();
            if (Objects.equals(status1, status2))
            {
                return resource1.getName().toString().compareTo(resource2.getName().toString());
            }

            return status1.getOrder().compareTo(status2.getOrder());
        }
    }
}
