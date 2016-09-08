package com.blockout;

import com.minecolonies.util.Log;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaneParams
{
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("([-+]?\\d+)(%|px)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern RGBA_PATTERN       =
      Pattern.compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([01]\\.\\d+)\\s*)?\\)", Pattern.CASE_INSENSITIVE);
    private Node node;
    private View parentView;

    public PaneParams(Node n)
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

    public void setParentView(View parent)
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

    @Nonnull
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
    private static String localize(String str)
    {
        if (str == null)
        {
            return null;
        }

        String s = str;
        int index = s.indexOf("$(");
        while (index != -1)
        {
            int endIndex = s.indexOf(')', index);

            if (endIndex == -1)
            {
                break;
            }

            String key = s.substring(index + 2, endIndex);
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

    public String getStringAttribute(String name)
    {
        return getStringAttribute(name, "");
    }

    public String getStringAttribute(String name, String def)
    {
        Node attr = getAttribute(name);
        return (attr != null) ? attr.getNodeValue() : def;
    }

    private Node getAttribute(String name)
    {
        return node.getAttributes().getNamedItem(name);
    }

    @Nullable
    public String getLocalizedStringAttribute(String name)
    {
        return getLocalizedStringAttribute(name, "");
    }

    @Nullable
    public String getLocalizedStringAttribute(String name, String def)
    {
        return localize(getStringAttribute(name, def));
    }

    public int getIntegerAttribute(String name)
    {
        return getIntegerAttribute(name, 0);
    }

    public int getIntegerAttribute(String name, int def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Integer.parseInt(attr);
        }
        return def;
    }

    public float getFloatAttribute(String name)
    {
        return getFloatAttribute(name, 0);
    }

    public float getFloatAttribute(String name, float def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Float.parseFloat(attr);
        }
        return def;
    }

    public double getDoubleAttribute(String name)
    {
        return getDoubleAttribute(name, 0);
    }

    public double getDoubleAttribute(String name, double def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Double.parseDouble(attr);
        }

        return def;
    }

    public boolean getBooleanAttribute(String name)
    {
        return getBooleanAttribute(name, false);
    }

    public boolean getBooleanAttribute(String name, boolean def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Boolean.parseBoolean(attr);
        }
        return def;
    }

    public <T extends Enum<T>> T getEnumAttribute(String name, Class<T> clazz, T def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Enum.valueOf(clazz, attr);
        }
        return def;
    }

    public int getScalableIntegerAttribute(String name, int def, int scale)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            Matcher m = PERCENTAGE_PATTERN.matcher(attr);
            if (m.find())
            {
                return parseScalableIntegerRegexMatch(m, def, scale);
            }
        }

        return def;
    }

    private static int parseScalableIntegerRegexMatch(Matcher m, int def, int scale)
    {
        try
        {
            int value = Integer.parseInt(m.group(1));

            if ("%".equals(m.group(2)))
            {
                value = scale * MathHelper.clamp_int(value, 0, 100) / 100;
            }
            //  DO NOT attempt to do a "value < 0" treated as (100% of parent) - abs(size)
            //  without differentiating between 'size' and 'position' value types
            //  even then, it's probably not actually necessary...

            return value;
        }
        catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException ex)
        {
            Log.logger.warn(ex);
        }

        return def;
    }

    @Nullable
    public SizePair getSizePairAttribute(String name, SizePair def, SizePair scale)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            int w = def != null ? def.x : 0;
            int h = def != null ? def.y : 0;

            Matcher m = PERCENTAGE_PATTERN.matcher(attr);
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

    public int getColorAttribute(String name, int def)
    {
        String attr = getStringAttribute(name, null);
        if (attr == null)
        {
            return def;
        }

        Matcher m = RGBA_PATTERN.matcher(attr);

        if (attr.startsWith("#"))
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

    private static int getRGBA(String attr, Matcher m)
    {
        int r = MathHelper.clamp_int(Integer.parseInt(m.group(1)), 0, 255);
        int g = MathHelper.clamp_int(Integer.parseInt(m.group(2)), 0, 255);
        int b = MathHelper.clamp_int(Integer.parseInt(m.group(3)), 0, 255);

        int color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

        if (attr.startsWith("rgba"))
        {
            int alpha = (int) (Double.parseDouble(m.group(4)) * 255.0F);
            color |= MathHelper.clamp_int(alpha, 0, 255) << 24;
        }

        return color;
    }

    private static int getColorByNumberOrName(int def, String attr)
    {
        try
        {
            return Integer.parseInt(attr);
        }
        catch (NumberFormatException ex)
        {
            return Color.getByName(attr, def);
        }
    }

    public static class SizePair
    {
        private int x;
        private int y;

        public SizePair(int w, int h)
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
