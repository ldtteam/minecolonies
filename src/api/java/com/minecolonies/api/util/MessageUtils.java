package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColony;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.*;

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
        return format(new TranslationTextComponent(key, args));
    }

    /**
     * Starts a new message builder.
     *
     * @param component the component to send.
     * @return the message builder instance.
     */
    public static MessageBuilder format(ITextComponent component)
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
        private IFormattableTextComponent rootComponent;

        /**
         * The current working component.
         */
        private IFormattableTextComponent currentComponent;

        /**
         * Default constructor.
         *
         * @param component the component to begin with.
         */
        MessageBuilder(ITextComponent component)
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
        public MessageBuilder with(TextFormatting... formatting)
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
            return append(new TranslationTextComponent(key, args));
        }

        /**
         * Appends a new component to the
         *
         * @param component the component to send.
         * @return the new message builder object.
         */
        public MessageBuilder append(ITextComponent component)
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
        public IFormattableTextComponent create()
        {
            mergeComponents();
            return rootComponent;
        }

        /**
         * Send the message to one (or more) players.
         *
         * @param players the players to send the message to.
         */
        public void sendTo(PlayerEntity... players)
        {
            sendTo(Arrays.asList(players));
        }

        /**
         * Send the message to a collection of players.
         *
         * @param players the players to send the message to.
         */
        public void sendTo(Collection<PlayerEntity> players)
        {
            mergeComponents();
            for (PlayerEntity player : players)
            {
                player.sendMessage(rootComponent, player.getUUID());
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
        private final IFormattableTextComponent rootComponent;

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
        public MessageBuilderColonyPlayerSelector(final IFormattableTextComponent rootComponent, final IColony colony, final boolean alwaysShowColony)
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
        private void sendInternal(Collection<PlayerEntity> players)
        {
            for (PlayerEntity player : players)
            {
                IFormattableTextComponent fullComponent = rootComponent.copy();
                if (alwaysShowColony || !colony.isCoordInColony(player.level, player.blockPosition()))
                {
                    fullComponent = new StringTextComponent("[" + colony.getName() + "] ").append(rootComponent);
                }

                player.sendMessage(fullComponent, player.getUUID());
            }
        }
    }

    /**
     * Turns any possible text component into a formattable component.
     *
     * @param component the input component.
     * @return the formattable component.
     */
    private static IFormattableTextComponent getFormattableComponent(ITextComponent component)
    {
        if (component instanceof IFormattableTextComponent)
        {
            return (IFormattableTextComponent) component;
        }
        else
        {
            return new StringTextComponent("").append(component);
        }
    }
}
