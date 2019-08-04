package com.minecolonies.blockout;

import com.minecolonies.blockout.views.View;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.minecolonies.blockout.Log.getLogger;

/**
 * Special parameters for the panes.
 */
public class PaneParams
{
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("([-+]?\\d+)(%|px)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern RGBA_PATTERN       =
      Pattern.compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([01]\\.\\d+)\\s*)?\\)", Pattern.CASE_INSENSITIVE);
    private static final char  HASH_CHAR             = '#';
    private final        Node node;
    private              View parentView;

    /**
     * Instantiates the pane parameters.
     *
     * @param n the node.
     */
    public PaneParams(final Node n)
    {
        node = n;
    }

    public String getType()
    {
        return node.getNodeName();
    }

    public View getParentView()
    {
        return parentView;
    }

    public void setParentView(final View parent)
    {
        parentView = parent;
    }

    public int getParentWidth()
    {
        return parentView != null ? parentView.getInteriorWidth() : 0;
    }

    public int getParentHeight()
    {
        return parentView != null ? parentView.getInteriorHeight() : 0;
    }

    @Nullable
    public List<PaneParams> getChildren()
    {
        List<PaneParams> list = null;

        Node child = node.getFirstChild();
        while (child != null)
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                if (list == null)
                {
                    list = new ArrayList<>();
                }

                list.add(new PaneParams(child));
            }
            child = child.getNextSibling();
        }

        return list;
    }

    @NotNull
    public String getText()
    {
        return node.getTextContent().trim();
    }

    @Nullable
    public String getLocalizedText()
    {
        return localize(node.getTextContent().trim());
    }

    @Nullable
    private static String localize(final String str)
    {
        if (str == null)
        {
            return null;
        }

        String s = str;
        int index = s.indexOf("$(");
        while (index != -1)
        {
            final int endIndex = s.indexOf(')', index);

            if (endIndex == -1)
            {
                break;
            }

            final String key = s.substring(index + 2, endIndex);
            String replacement = I18n.format(key);

            if (replacement.equals(key))
            {
                replacement = "MISSING:" + key;
            }

            s = s.substring(0, index) + replacement + s.substring(endIndex + 1);

            index = s.indexOf("$(", index + replacement.length());
        }

        return s;
    }

    /**
     * Get the string attribute.
     *
     * @param name the name to search.
     * @return the attribute.
     */
    public String getStringAttribute(final String name)
    {
        return getStringAttribute(name, "");
    }

    /**
     * Get the String attribute from the name and definition.
     *
     * @param name the name.
     * @param def  the definition.
     * @return the String.
     */
    public String getStringAttribute(final String name, final String def)
    {
        final Node attr = getAttribute(name);
        return (attr != null) ? attr.getNodeValue() : def;
    }

    private Node getAttribute(final String name)
    {
        return node.getAttributes().getNamedItem(name);
    }

    /**
     * Get the localized string attribute from the name.
     *
     * @param name the name.
     * @return the string attribute.
     */
    @Nullable
    public String getLocalizedStringAttribute(final String name)
    {
        return getLocalizedStringAttribute(name, "");
    }

    /**
     * Get the localized String attribute from the name and definition.
     *
     * @param name the name.
     * @param def  the definition.
     * @return the string.
     */
    @Nullable
    public String getLocalizedStringAttribute(final String name, final String def)
    {
        return localize(getStringAttribute(name, def));
    }

    /**
     * Get the integer attribute from the name.
     *
     * @param name the name.
     * @return the integer.
     */
    public int getIntAttribute(final String name)
    {
        return getIntAttribute(name, 0);
    }

    /**
     * Get the integer attribute from name and definition.
     *
     * @param name the name.
     * @param def  the definition.
     * @return the int.
     */
    public int getIntAttribute(final String name, final int def)
    {
        final String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Integer.parseInt(attr);
        }
        return def;
    }

    /**
     * Get the float attribute from name.
     *
     * @param name the name.
     * @return the float.
     */
    public float getFloatAttribute(final String name)
    {
        return getFloatAttribute(name, 0);
    }

    /**
     * Get the float attribute from name and definition.
     *
     * @param name the name.
     * @param def  the definition.
     * @return the float.
     */
    public float getFloatAttribute(final String name, final float def)
    {
        final String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Float.parseFloat(attr);
        }
        return def;
    }

    /**
     * Get the double attribute from name.
     *
     * @param name the name.
     * @return the double.
     */
    public double getDoubleAttribute(final String name)
    {
        return getDoubleAttribute(name, 0);
    }

    /**
     * Get the double attribute from name and definition.
     *
     * @param name the name.
     * @param def  the definition.
     * @return the double.
     */
    public double getDoubleAttribute(final String name, final double def)
    {
        final String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Double.parseDouble(attr);
        }

        return def;
    }

    /**
     * Get the boolean attribute from name.
     *
     * @param name the name.
     * @return the boolean.
     */
    public boolean getBooleanAttribute(final String name)
    {
        return getBooleanAttribute(name, false);
    }

    /**
     * Get the boolean attribute from name and definition.
     *
     * @param name the name.
     * @param def  the definition.
     * @return the boolean.
     */
    public boolean getBooleanAttribute(final String name, final boolean def)
    {
        final String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Boolean.parseBoolean(attr);
        }
        return def;
    }

    /**
     * Get the boolean attribute from name and class and definition..
     *
     * @param name  the name.
     * @param clazz the class.
     * @param def   the definition.
     * @param <T>   the type of class.
     * @return the enum attribute.
     */
    public <T extends Enum<T>> T getEnumAttribute(final String name, final Class<T> clazz, final T def)
    {
        final String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Enum.valueOf(clazz, attr);
        }
        return def;
    }

    /**
     * Get the scalable integer attribute from name and definition.
     *
     * @param name  the name.
     * @param def   the definition.
     * @param scale the scale.
     * @return the integer.
     */
    public int getScalableIntegerAttribute(final String name, final int def, final int scale)
    {
        final String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            final Matcher m = PERCENTAGE_PATTERN.matcher(attr);
            if (m.find())
            {
                return parseScalableIntegerRegexMatch(m, def, scale);
            }
        }

        return def;
    }

    private static int parseScalableIntegerRegexMatch(final Matcher m, final int def, final int scale)
    {
        try
        {
            int value = Integer.parseInt(m.group(1));

            if ("%".equals(m.group(2)))
            {
                value = scale * MathHelper.clamp(value, 0, 100) / 100;
            }
            //  DO NOT attempt to do a "value < 0" treated as (100% of parent) - abs(size)
            //  without differentiating between 'size' and 'position' value types
            //  even then, it's probably not actually necessary...

            return value;
        }
        catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException ex)
        {
            getLogger().warn(ex);
        }

        return def;
    }

    /**
     * Get the size pair attribute.
     *
     * @param name  the name.
     * @param def   the definition.
     * @param scale the scale.
     * @return the SizePair.
     */
    @Nullable
    public SizePair getSizePairAttribute(final String name, final SizePair def, final SizePair scale)
    {
        final String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            int w = def != null ? def.x : 0;
            int h = def != null ? def.y : 0;

            final Matcher m = PERCENTAGE_PATTERN.matcher(attr);
            if (m.find())
            {
                w = parseScalableIntegerRegexMatch(m, w, scale != null ? scale.x : 0);

                if (m.find() || m.find(0))
                {
                    //  If no second value is passed, use the first value
                    h = parseScalableIntegerRegexMatch(m, h, scale != null ? scale.y : 0);
                }
            }

            return new SizePair(w, h);
        }

        return def;
    }

    /**
     * Get the color attribute from name and definition.
     *
     * @param name the name.
     * @param def  the definition
     * @return int color value.
     */
    public int getColorAttribute(final String name, final int def)
    {
        final String attr = getStringAttribute(name, null);
        if (attr == null)
        {
            return def;
        }

        final Matcher m = RGBA_PATTERN.matcher(attr);

        if (attr.charAt(0) == HASH_CHAR)
        {
            //  CSS Hex format: #00112233
            return Integer.parseInt(attr.substring(1), 16);
        }
        //  CSS RGB format: rgb(255,0,0) and rgba(255,0,0,0.3)
        else if ((attr.startsWith("rgb(") || attr.startsWith("rgba(")) && m.find())
        {
            return getRGBA(attr, m);
        }
        else
        {
            return getColorByNumberOrName(def, attr);
        }
    }

    private static int getRGBA(final String attr, final Matcher m)
    {
        final int r = MathHelper.clamp(Integer.parseInt(m.group(1)), 0, 255);
        final int g = MathHelper.clamp(Integer.parseInt(m.group(2)), 0, 255);
        final int b = MathHelper.clamp(Integer.parseInt(m.group(3)), 0, 255);

        int color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

        if (attr.startsWith("rgba"))
        {
            final int alpha = (int) (Double.parseDouble(m.group(4)) * 255.0F);
            color |= MathHelper.clamp(alpha, 0, 255) << 24;
        }

        return color;
    }

    private static int getColorByNumberOrName(final int def, final String attr)
    {
        try
        {
            return Integer.parseInt(attr);
        }
        catch (final NumberFormatException ex)
        {
            return Color.getByName(attr, def);
        }
    }

    /**
     * Size pair of width and height.
     */
    public static class SizePair
    {
        private final int x;
        private final int y;

        /**
         * Instantiates a SizePair object.
         *
         * @param w width.
         * @param h height.
         */
        public SizePair(final int w, final int h)
        {
            x = w;
            y = h;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }
    }
}
