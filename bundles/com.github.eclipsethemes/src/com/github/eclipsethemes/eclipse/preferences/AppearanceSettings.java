package com.github.eclipsethemes.eclipse.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * The chrome refinements the generated stylesheets apply on top of the plain
 * theme colors.
 *
 * <p>
 * Each option is opt-out rather than opt-in because they describe how the
 * workbench is drawn, not which colors it uses, and every one of them replaces
 * a default that predates the flat look VS Code and IntelliJ established. The
 * only exception is {@link #hideTabMinimizeMaximize()}, which removes working
 * buttons instead of restyling them.
 * </p>
 */
public record AppearanceSettings(
		boolean flatToolbarButtons,
		boolean modernScrollbars,
		boolean solidFocusRings,
		boolean roundedControls,
		boolean dimSeparators,
		boolean accentListSelection,
		boolean squareTabs,
		boolean closeButtonOnActiveTabOnly,
		boolean hideTabMinimizeMaximize) {

	public static AppearanceSettings defaults() {
		return new AppearanceSettings(true, true, true, true, true, true, true, true, false);
	}

	/** Reads the options an adapter should honour from the plugin's preference node. */
	public static AppearanceSettings of(IEclipsePreferences preferences) {
		AppearanceSettings defaults = defaults();
		if (preferences == null) {
			return defaults;
		}
		return new AppearanceSettings(
				preferences.getBoolean(PreferenceKeys.FLAT_TOOLBAR_BUTTONS, defaults.flatToolbarButtons()),
				preferences.getBoolean(PreferenceKeys.MODERN_SCROLLBARS, defaults.modernScrollbars()),
				preferences.getBoolean(PreferenceKeys.SOLID_FOCUS_RINGS, defaults.solidFocusRings()),
				preferences.getBoolean(PreferenceKeys.ROUNDED_CONTROLS, defaults.roundedControls()),
				preferences.getBoolean(PreferenceKeys.DIM_SEPARATORS, defaults.dimSeparators()),
				preferences.getBoolean(PreferenceKeys.ACCENT_LIST_SELECTION, defaults.accentListSelection()),
				preferences.getBoolean(PreferenceKeys.SQUARE_TABS, defaults.squareTabs()),
				preferences.getBoolean(PreferenceKeys.CLOSE_BUTTON_ON_ACTIVE_TAB_ONLY,
						defaults.closeButtonOnActiveTabOnly()),
				preferences.getBoolean(PreferenceKeys.HIDE_TAB_MINIMIZE_MAXIMIZE,
						defaults.hideTabMinimizeMaximize()));
	}

	/** Reads the options the preference page edits. */
	public static AppearanceSettings of(IPreferenceStore store) {
		AppearanceSettings defaults = defaults();
		if (store == null) {
			return defaults;
		}
		return new AppearanceSettings(
				store.getBoolean(PreferenceKeys.FLAT_TOOLBAR_BUTTONS),
				store.getBoolean(PreferenceKeys.MODERN_SCROLLBARS),
				store.getBoolean(PreferenceKeys.SOLID_FOCUS_RINGS),
				store.getBoolean(PreferenceKeys.ROUNDED_CONTROLS),
				store.getBoolean(PreferenceKeys.DIM_SEPARATORS),
				store.getBoolean(PreferenceKeys.ACCENT_LIST_SELECTION),
				store.getBoolean(PreferenceKeys.SQUARE_TABS),
				store.getBoolean(PreferenceKeys.CLOSE_BUTTON_ON_ACTIVE_TAB_ONLY),
				store.getBoolean(PreferenceKeys.HIDE_TAB_MINIMIZE_MAXIMIZE));
	}

	public void save(IPreferenceStore store) {
		store.setValue(PreferenceKeys.FLAT_TOOLBAR_BUTTONS, flatToolbarButtons);
		store.setValue(PreferenceKeys.MODERN_SCROLLBARS, modernScrollbars);
		store.setValue(PreferenceKeys.SOLID_FOCUS_RINGS, solidFocusRings);
		store.setValue(PreferenceKeys.ROUNDED_CONTROLS, roundedControls);
		store.setValue(PreferenceKeys.DIM_SEPARATORS, dimSeparators);
		store.setValue(PreferenceKeys.ACCENT_LIST_SELECTION, accentListSelection);
		store.setValue(PreferenceKeys.SQUARE_TABS, squareTabs);
		store.setValue(PreferenceKeys.CLOSE_BUTTON_ON_ACTIVE_TAB_ONLY, closeButtonOnActiveTabOnly);
		store.setValue(PreferenceKeys.HIDE_TAB_MINIMIZE_MAXIMIZE, hideTabMinimizeMaximize);
	}
}
