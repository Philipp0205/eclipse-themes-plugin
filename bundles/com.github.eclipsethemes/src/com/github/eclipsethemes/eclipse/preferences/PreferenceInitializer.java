package com.github.eclipsethemes.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.github.eclipsethemes.EclipseThemes;
import com.github.eclipsethemes.core.Constants;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, EclipseThemes.PLUGIN_ID);
		store.setDefault(PreferenceKeys.ACTIVE_THEME_ID, Constants.DEFAULT_LIGHT_THEME_NAME);
		store.setDefault(PreferenceKeys.APPLY_WORKBENCH_THEME, true);

		AppearanceSettings appearance = AppearanceSettings.defaults();
		store.setDefault(PreferenceKeys.FLAT_TOOLBAR_BUTTONS, appearance.flatToolbarButtons());
		store.setDefault(PreferenceKeys.MODERN_SCROLLBARS, appearance.modernScrollbars());
		store.setDefault(PreferenceKeys.SOLID_FOCUS_RINGS, appearance.solidFocusRings());
		store.setDefault(PreferenceKeys.ROUNDED_CONTROLS, appearance.roundedControls());
		store.setDefault(PreferenceKeys.DIM_SEPARATORS, appearance.dimSeparators());
		store.setDefault(PreferenceKeys.ACCENT_LIST_SELECTION, appearance.accentListSelection());
		store.setDefault(PreferenceKeys.SQUARE_TABS, appearance.squareTabs());
		store.setDefault(PreferenceKeys.CLOSE_BUTTON_ON_ACTIVE_TAB_ONLY, appearance.closeButtonOnActiveTabOnly());
		store.setDefault(PreferenceKeys.HIDE_TAB_MINIMIZE_MAXIMIZE, appearance.hideTabMinimizeMaximize());
	}
}
