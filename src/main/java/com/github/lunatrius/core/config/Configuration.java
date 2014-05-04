package com.github.lunatrius.core.config;

import com.google.common.base.Joiner;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class Configuration {
	private final net.minecraftforge.common.config.Configuration config;
	private static final String FORMAT_NORMAL = "%1$s (default: %2$s)";
	private static final String FORMAT_RANGE = "%1$s (range: %2$s ~ %3$s, default: %4$s)";

	public Configuration(File file) {
		this.config = new net.minecraftforge.common.config.Configuration(file);
	}

	public void load() {
		this.config.load();
	}

	public void save() {
		this.config.save();
	}

	public net.minecraftforge.common.config.Configuration getConfig() {
		return this.config;
	}

	public boolean hasCategory(String category) {
		return this.config.hasCategory(category);
	}

	public boolean hasKey(String category, String key) {
		return this.config.hasKey(category, key);
	}

	public ConfigCategory getCategory(String category) {
		return this.config.getCategory(category);
	}

	public void addCustomCategoryComment(String category, String comment) {
		this.config.addCustomCategoryComment(category, comment);
	}

	public Property get(String category, String key, String defaultValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, defaultValue);
		return property;
	}

	public Property get(String category, String key, boolean defaultValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, defaultValue);
		return property;
	}

	public Property get(String category, String key, int defaultValue, int minValue, int maxValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_RANGE, comment, minValue, maxValue, defaultValue);
		int value = property.getInt(defaultValue);
		property.set(value < minValue ? minValue : (value > maxValue ? maxValue : value));
		return property;
	}

	public Property get(String category, String key, double defaultValue, double minValue, double maxValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_RANGE, comment, minValue, maxValue, defaultValue);
		double value = property.getDouble(defaultValue);
		property.set(value < minValue ? minValue : (value > maxValue ? maxValue : value));
		return property;
	}

	public Property get(String category, String key, String[] defaultValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, getDefaultListString(defaultValue));
		return property;
	}

	public Property get(String category, String key, boolean[] defaultValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, getDefaultListString(defaultValue));
		return property;
	}

	public Property get(String category, String key, int[] defaultValue, int minValue, int maxValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_RANGE, comment, minValue, maxValue, getDefaultListString(defaultValue));
		String[] values = property.getStringList();
		for (int i = 0; i < values.length; i++) {
			int value = Integer.valueOf(values[i]);
			values[i] = String.valueOf(value < minValue ? minValue : (value > maxValue ? maxValue : value));
		}
		property.set(values);
		return property;
	}

	public Property get(String category, String key, double[] defaultValue, double minValue, double maxValue, String comment) {
		Property property = this.config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_RANGE, comment, minValue, maxValue, getDefaultListString(defaultValue));
		String[] values = property.getStringList();
		for (int i = 0; i < values.length; i++) {
			double value = Double.valueOf(values[i]);
			values[i] = String.valueOf(value < minValue ? minValue : (value > maxValue ? maxValue : value));
		}
		property.set(values);
		return property;
	}

	private String getDefaultListString(String[] defaultValues) {
		return "[" + Joiner.on(", ").join(defaultValues) + "]";
	}

	private String getDefaultListString(boolean[] defaultValues) {
		String[] strings = new String[defaultValues.length];
		for (int i = 0; i < defaultValues.length; i++) {
			strings[i] = String.valueOf(defaultValues[i]);
		}
		return getDefaultListString(strings);
	}

	private String getDefaultListString(int[] defaultValues) {
		String[] strings = new String[defaultValues.length];
		for (int i = 0; i < defaultValues.length; i++) {
			strings[i] = String.valueOf(defaultValues[i]);
		}
		return getDefaultListString(strings);
	}

	private String getDefaultListString(double[] defaultValues) {
		String[] strings = new String[defaultValues.length];
		for (int i = 0; i < defaultValues.length; i++) {
			strings[i] = String.valueOf(defaultValues[i]);
		}
		return getDefaultListString(strings);
	}
}
