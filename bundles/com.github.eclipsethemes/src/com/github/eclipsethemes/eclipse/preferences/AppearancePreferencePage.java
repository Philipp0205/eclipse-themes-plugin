package com.github.eclipsethemes.eclipse.preferences;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.github.eclipsethemes.EclipseThemes;
import com.github.eclipsethemes.core.models.Theme;
import com.github.eclipsethemes.eclipse.ThemeManager;

/**
 * Toggles for the chrome refinements the generated stylesheets layer on top of
 * the theme colors. Each option maps to one block in {@code GtkCssGenerator} or
 * {@code WorkbenchCssGenerator}.
 */
public class AppearancePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * @param nativeOnly styling routed through GTK, and therefore inert on
	 *                   Windows and macOS where SWT does not delegate the
	 *                   painting of these controls.
	 */
	private record Option(String key, String label, String description, boolean nativeOnly) {
	}

	private static final List<Option> TOOLBAR_OPTIONS = List.of(
			new Option(PreferenceKeys.FLAT_TOOLBAR_BUTTONS, "Flat toolbar buttons",
					"Removes the outline around every toolbar icon and shows a rounded highlight on hover instead.",
					true),
			new Option(PreferenceKeys.ROUNDED_CONTROLS, "Rounded controls, menus and tooltips",
					"Softens the corners of text fields, buttons, menus, popups and tooltips.", true),
			new Option(PreferenceKeys.DIM_SEPARATORS, "Dim toolbar separators",
					"Draws the dividers between toolbar groups as a faint hairline.", true));

	private static final List<Option> INTERACTION_OPTIONS = List.of(
			new Option(PreferenceKeys.MODERN_SCROLLBARS, "Overlay scrollbars",
					"Replaces the filled scrollbar trough with a rounded thumb over the content.", true),
			new Option(PreferenceKeys.SOLID_FOCUS_RINGS, "Solid focus rings",
					"Replaces the dashed keyboard focus rectangle with a thin accent outline.", true),
			new Option(PreferenceKeys.ACCENT_LIST_SELECTION, "Accent-tinted list selection",
					"Highlights tree and table rows with an accent tint instead of the editor's text selection color.",
					true));

	private static final List<Option> TAB_OPTIONS = List.of(
			new Option(PreferenceKeys.SQUARE_TABS, "Square tabs without a border",
					"Squares off the tab corners and removes the box drawn around the tab strip.", false),
			new Option(PreferenceKeys.CLOSE_BUTTON_ON_ACTIVE_TAB_ONLY, "Close button on the active tab only",
					"Hides the close button on tabs that are not selected.", false),
			new Option(PreferenceKeys.HIDE_TAB_MINIMIZE_MAXIMIZE, "Hide the minimize and maximize buttons",
					"Removes both buttons from the top right of every view and editor stack. "
							+ "Double-clicking a tab still maximizes it.",
					false));

	private final Map<String, Button> buttons = new LinkedHashMap<>();
	private IWorkbench workbench;

	public AppearancePreferencePage() {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, EclipseThemes.PLUGIN_ID));
		setDescription("Choose how much the workbench chrome is restyled beyond the plain theme colors.");
	}

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createGroup(container, "Toolbars and controls", TOOLBAR_OPTIONS);
		createGroup(container, "Scrolling, focus and selection", INTERACTION_OPTIONS);
		createGroup(container, "Tabs", TAB_OPTIONS);

		if (!isNativeStylingAvailable()) {
			Label note = new Label(container, SWT.WRAP);
			note.setText("Options marked with * are applied through GTK and only take effect on Linux.");
			note.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			note.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}

		Label hint = new Label(container, SWT.WRAP);
		hint.setText("Changes are applied to the running workbench when you press Apply or OK. "
				+ "A few of them, tab corners in particular, only reach parts that are reopened afterwards.");
		hint.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		GridData hintData = new GridData(SWT.FILL, SWT.TOP, true, false);
		hintData.verticalIndent = 8;
		hint.setLayoutData(hintData);

		load(AppearanceSettings.of(getPreferenceStore()));
		return container;
	}

	private void createGroup(Composite parent, String title, List<Option> options) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(title);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		boolean nativeAvailable = isNativeStylingAvailable();
		for (Option option : options) {
			Button button = new Button(group, SWT.CHECK);
			boolean inert = option.nativeOnly() && !nativeAvailable;
			button.setText(option.label() + (inert ? " *" : ""));
			button.setFont(JFaceResources.getDialogFont());
			button.setEnabled(!inert);
			buttons.put(option.key(), button);

			Label description = new Label(group, SWT.WRAP);
			description.setText(option.description());
			description.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
			data.horizontalIndent = 18;
			data.widthHint = 520;
			description.setLayoutData(data);
		}
	}

	private static boolean isNativeStylingAvailable() {
		return Platform.WS_GTK.equals(Platform.getWS());
	}

	private void load(AppearanceSettings settings) {
		select(PreferenceKeys.FLAT_TOOLBAR_BUTTONS, settings.flatToolbarButtons());
		select(PreferenceKeys.ROUNDED_CONTROLS, settings.roundedControls());
		select(PreferenceKeys.DIM_SEPARATORS, settings.dimSeparators());
		select(PreferenceKeys.MODERN_SCROLLBARS, settings.modernScrollbars());
		select(PreferenceKeys.SOLID_FOCUS_RINGS, settings.solidFocusRings());
		select(PreferenceKeys.ACCENT_LIST_SELECTION, settings.accentListSelection());
		select(PreferenceKeys.SQUARE_TABS, settings.squareTabs());
		select(PreferenceKeys.CLOSE_BUTTON_ON_ACTIVE_TAB_ONLY, settings.closeButtonOnActiveTabOnly());
		select(PreferenceKeys.HIDE_TAB_MINIMIZE_MAXIMIZE, settings.hideTabMinimizeMaximize());
	}

	private void select(String key, boolean value) {
		Button button = buttons.get(key);
		if (button != null && !button.isDisposed()) {
			button.setSelection(value);
		}
	}

	private boolean isSelected(String key) {
		Button button = buttons.get(key);
		return button != null && !button.isDisposed() && button.getSelection();
	}

	private AppearanceSettings currentSelection() {
		return new AppearanceSettings(
				isSelected(PreferenceKeys.FLAT_TOOLBAR_BUTTONS),
				isSelected(PreferenceKeys.MODERN_SCROLLBARS),
				isSelected(PreferenceKeys.SOLID_FOCUS_RINGS),
				isSelected(PreferenceKeys.ROUNDED_CONTROLS),
				isSelected(PreferenceKeys.DIM_SEPARATORS),
				isSelected(PreferenceKeys.ACCENT_LIST_SELECTION),
				isSelected(PreferenceKeys.SQUARE_TABS),
				isSelected(PreferenceKeys.CLOSE_BUTTON_ON_ACTIVE_TAB_ONLY),
				isSelected(PreferenceKeys.HIDE_TAB_MINIMIZE_MAXIMIZE));
	}

	@Override
	public boolean performOk() {
		AppearanceSettings selection = currentSelection();
		if (selection.equals(AppearanceSettings.of(getPreferenceStore()))) {
			return super.performOk();
		}
		selection.save(getPreferenceStore());
		reapplyActiveTheme();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		load(AppearanceSettings.defaults());
		super.performDefaults();
	}

	/** The options only reach the UI by way of a freshly generated stylesheet. */
	private void reapplyActiveTheme() {
		ThemeManager manager = EclipseThemes.instance().getManager();
		manager.loadThemes();
		Optional<Theme> active = manager.findById(getPreferenceStore().getString(PreferenceKeys.ACTIVE_THEME_ID));
		active.ifPresent(theme -> manager.applyTheme(workbench, theme));
	}
}
