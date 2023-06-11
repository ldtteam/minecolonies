package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColony;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple class for containing reusable player messaging logic.
 */
public class MessageUtils
{
    /**
     * Starts a new message builder.
     *
     * @param key  the translation key.
     * @param args the arguments for the translation component.
     * @return the message builder instance.
     */
    public static MessageBuilder format(String key, Object... args)
    {
        return format(Component.translatable(key, args));
    }

    /**
     * Starts a new message builder.
     *
     * @param component the component to send.
     * @return the message builder instance.
     */
    public static MessageBuilder format(Component component)
    {
        return new MessageBuilder(component);
    }

    /**
     * Starting class for the message building. Contains primary logic for sending the messages.
     */
    public static class MessageBuilder
    {
        /**
         * The stored text component to use when sending the message.
         */
        private MutableComponent rootComponent;

        /**
         * The current working component.
         */
        private MutableComponent currentComponent;

        /**
         * Default constructor.
         *
         * @param component the component to begin with.
         */
        MessageBuilder(Component component)
        {
            this.currentComponent = getFormattableComponent(component);
        }

        /**
         * Applies a style to the text component. Can be used to color a message, make it bold, italic, etc.
         *
         * @param style the style to use.
         * @return the original message builder object.
         */
        public MessageBuilder with(Style style)
        {
            currentComponent.setStyle(style.applyTo(currentComponent.getStyle()));
            return this;
        }

        /**
         * Applies formatting to the text component. Can be used to color a message, make it bold, italic, etc.
         *
         * @param formatting the text formatting to use.
         * @return the original message builder object.
         */
        public MessageBuilder with(ChatFormatting... formatting)
        {
            currentComponent.setStyle(currentComponent.getStyle().applyFormats(formatting));
            return this;
        }

        /**
         * Starts a new builder object to append an additional component to the original one.
         *
         * @param key  the translation key.
         * @param args the arguments for the translation component.
         * @return the new message builder object.
         */
        public MessageBuilder append(String key, Object... args)
        {
            return append(Component.translatable(key, args));
        }

        /**
         * Appends a new component to the
         *
         * @param component the component to send.
         * @return the new message builder object.
         */
        public MessageBuilder append(Component component)
        {
            mergeComponents();
            currentComponent = getFormattableComponent(component);
            return this;
        }

        /**
         * Creates a text component that can be used as an argument to other components.
         *
         * @return the text component.
         */
        public MutableComponent create()
        {
            mergeComponents();
            return rootComponent;
        }

        /**
         * Send the message to one (or more) players.
         *
         * @param players the players to send the message to.
         */
        public void sendTo(Player... players)
        {
            sendTo(Arrays.asList(players));
        }

        /**
         * Send the message to a collection of players.
         *
         * @param players the players to send the message to.
         */
        public void sendTo(Collection<Player> players)
        {
            mergeComponents();
            for (Player player : players)
            {
                player.displayClientMessage(rootComponent, false);
            }
        }

        /**
         * Send a message to a given colony, this method returns a class from which you will need
         * to select which kind of members of the colony to send the message to.
         *
         * @param colony the reference to the colony.
         * @return the message builder colony player selector.
         */
        public MessageBuilderColonyPlayerSelector sendTo(IColony colony)
        {
            return sendTo(colony, false);
        }

        /**
         * Send a message to a given colony, this method returns a class from which you will need
         * to select which kind of members of the colony to send the message to.
         *
         * @param colony           the reference to the colony.
         * @param alwaysShowColony whether we always want to include the colony name in front of the message.
         * @return the message builder colony player selector.
         */
        public MessageBuilderColonyPlayerSelector sendTo(IColony colony, boolean alwaysShowColony)
        {
            mergeComponents();
            return new MessageBuilderColonyPlayerSelector(rootComponent, colony, alwaysShowColony);
        }

        /**
         * Merges the current working component back into the root component,
         * allowing for a new component to be worked on.
         */
        private void mergeComponents()
        {
            if (rootComponent == null)
            {
                rootComponent = currentComponent;
            }
            else
            {
                rootComponent.append(currentComponent);
            }
            currentComponent = null;
        }
    }

    public static class MessageBuilderColonyPlayerSelector
    {
        /**
         * The stored text component to use when sending the message.
         */
        private final MutableComponent rootComponent;

        /**
         * The colony this message originated from.
         */
        private final IColony colony;

        /**
         * Determines whether we always want to include the colony name in front of the message.
         */
        private final boolean alwaysShowColony;

        /**
         * Default constructor.
         *
         * @param rootComponent    the completed component to send.
         * @param colony           the reference to the colony.
         * @param alwaysShowColony whether we always want to include the colony name in front of the message.
         */
        public MessageBuilderColonyPlayerSelector(final MutableComponent rootComponent, final IColony colony, final boolean alwaysShowColony)
        {
            this.rootComponent = rootComponent;
            this.colony = colony;
            this.alwaysShowColony = alwaysShowColony;
        }

        /**
         * Sends the message to all players inside the colony.
         */
        public void forAllPlayers()
        {
            sendInternal(colony.getMessagePlayerEntities());
        }

        /**
         * Sends the message to all colony manager.
         */
        public void forManagers()
        {
            sendInternal(colony.getImportantMessageEntityPlayers());
        }

        /**
         * Internal helper method to send the message correctly.
         *
         * @param players the collection of players to send the message to.
         */
        private void sendInternal(Collection<Player> players)
        {
            for (Player player : players)
            {
                MutableComponent fullComponent = rootComponent.copy();
                if (alwaysShowColony || !colony.isCoordInColony(player.level(), player.blockPosition()))
                {
                    fullComponent = Component.literal("[" + colony.getName() + "] ").append(rootComponent);
                }

                player.displayClientMessage(fullComponent, false);
            }
        }
    }

    /**
     * Turns any possible text component into a formattable component.
     *
     * @param component the input component.
     * @return the formattable component.
     */
    private static MutableComponent getFormattableComponent(Component component)
    {
        return component.copy();
    }
}
