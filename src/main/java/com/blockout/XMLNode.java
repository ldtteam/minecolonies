package com.blockout;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.util.MathHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLNode
{
    Node node;

    public XMLNode(Node n)
    {
        node = n;
    }

    public String getName() { return node.getNodeName(); }

    public List<XMLNode> getChildren()
    {
        List<XMLNode> list = null;

        Node child = node.getFirstChild();
        while (child != null)
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                if (list == null)
                {
                    list = new ArrayList<XMLNode>();
                }

                list.add(new XMLNode(child));
            }
            child = child.getNextSibling();
        }

        return list;
    }

    public String getStringAttribute(String name) { return getStringAttribute(name, ""); }
    public String getStringAttribute(String name, String def)
    {
        Node attr = getAttribute(name);
        return (attr != null) ? attr.getNodeValue() : def;
    }

    public String getLocalizedStringAttribute(String name) { return getLocalizedStringAttribute(name, ""); }
    public String getLocalizedStringAttribute(String name, String def)
    {
        return Localize(getStringAttribute(name, def));
    }

    public int getIntegerAttribute(String name) { return getIntegerAttribute(name, 0); }
    public int getIntegerAttribute(String name, int def)
    {
        Node attr = getAttribute(name);
        if (attr != null)
        {
            try { return Integer.parseInt(attr.getNodeValue()); }
            catch (NumberFormatException ex) {}
        }
        return def;
    }

    public float getFloatAttribute(String name) { return getFloatAttribute(name, 0); }
    public float getFloatAttribute(String name, float def)
    {
        Node attr = getAttribute(name);
        if (attr != null)
        {
            try { return Float.parseFloat(attr.getNodeValue()) ; }
            catch (NumberFormatException ex) {}
        }
        return def;
    }

    public double getDoubleAttribute(String name) { return getDoubleAttribute(name, 0); }
    public double getDoubleAttribute(String name, double def)
    {
        Node attr = getAttribute(name);
        if (attr != null)
        {
            try { return Double.parseDouble(attr.getNodeValue()); }
            catch (NumberFormatException ex) {}
        }

        return def;
    }

    public boolean getBooleanAttribute(String name) { return getBooleanAttribute(name, false); }
    public boolean getBooleanAttribute(String name, boolean def)
    {
        Node attr = getAttribute(name);
        if (attr != null)
        {
            return Boolean.parseBoolean(attr.getNodeValue());
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnumAttribute(String name, T def)
    {
        Node attr = getAttribute(name);
        if (attr != null)
        {
            try { return def.valueOf((Class<T>)def.getClass(), attr.getNodeValue()); }
            catch (IllegalArgumentException exc) {}
        }
        return def;
    }

    static Pattern rgbaPattern = Pattern.compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([01]\\.\\d+)\\s*)?\\)", Pattern.CASE_INSENSITIVE);
    public int getColorAttribute(String name, int def)
    {
        Node attr = getAttribute(name);
        if (attr != null)
        {
            String value = attr.getNodeValue();
            if (value.startsWith("#"))
            {
                //  CSS Hex format: #00112233
                try{ return Integer.parseInt(value.substring(1), 16); }
                catch (NumberFormatException ex){}
            }
            else if (value.startsWith("rgb(") || value.startsWith("rgba("))
            {
                //  CSS RGB format: rgb(255,0,0) and rgba(255,0,0,0.3)
                Matcher m = rgbaPattern.matcher(value);

                if (m.find())
                {
                    try
                    {
                        int r = MathHelper.clamp_int(Integer.parseInt(m.group(1)), 0, 255);
                        int g = MathHelper.clamp_int(Integer.parseInt(m.group(2)), 0, 255);
                        int b = MathHelper.clamp_int(Integer.parseInt(m.group(3)), 0, 255);

                        int color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

                        if (value.startsWith("rgba"))
                        {
                            int alpha = (int)(Float.parseFloat(m.group(4)) * 255.0f);
                            color |= MathHelper.clamp_int(alpha, 0, 255) << 24;
                        }

                        return color;
                    }
                    catch (Exception ex)
                    {
                        //  NumberFormatException | NullPointerException | IndexOutOfBoundsException | IllegalStateException ex
                    }
                }
            }
            else
            {
                //  Integer
                try{ return Integer.parseInt(value); } catch (NumberFormatException ex){}

                return Loader.getColorByName(value, def);
            }
        }
        return def;
    }

    private Node getAttribute(String name)
    {
        return node.getAttributes().getNamedItem(name);
    }

    private static String Localize(String str)
    {
        if (str == null)
        {
            return str;
        }

        int index = str.indexOf("$(");
        while (index != -1)
        {
            int endIndex = str.indexOf(")", index);

            if (endIndex == -1)
            {
                break;
            }

            String key = str.substring(index + 2, endIndex);
            String replacement = LanguageRegistry.instance().getStringLocalization(key);

            if (replacement == null)
            {
                replacement = "MISSING:" + key;
            }

            str = str.substring(0, index) + replacement + str.substring(endIndex + 1);

            index = str.indexOf("$(", index + replacement.length());
        }

        return str;
    }
}
