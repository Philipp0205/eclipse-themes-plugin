package com.github.eclipsethemes.eclipse.adapters.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.prefs.BackingStoreException;

import com.github.eclipsethemes.EclipseThemes;
import com.github.eclipsethemes.core.models.Theme;
import com.github.eclipsethemes.core.models.ThemeType;
import com.github.eclipsethemes.eclipse.adapters.EclipseThemeAdapter;
import com.github.eclipsethemes.eclipse.preferences.PreferenceKeys;

public final class WorkbenchCssThemeAdapter extends EclipseThemeAdapter {

	static final String DARK_THEME_ID = "org.eclipse.e4.ui.css.theme.e4_dark";
	static final String LIGHT_THEME_ID = "org.eclipse.e4.ui.css.theme.e4_default";

	private static final Set<IThemeEngine> REGISTERED_ENGINES =
			Collections.newSetFromMap(new WeakHashMap<>());

	@Override
	public String getPreferencesId() {
		return EclipseThemes.PLUGIN_ID;
	}

	@Override
	public void apply(Theme theme, IEclipsePreferences preferences) {
		// Workbench styling needs the IWorkbench supplied by applyWorkbench.
	}

	@Override
	public void applyWorkbench(Theme theme, IEclipsePreferences preferences, IWorkbench workbench)
			throws BackingStoreException {
		if (!preferences.getBoolean(PreferenceKeys.APPLY_WORKBENCH_THEME, true) || workbench == null) {
			return;
		}

		Path cssFile;
		try {
			Path generated = EclipseThemes.getPluginDataDirectory().toPath().resolve("generated");
			Files.createDirectories(generated);
			cssFile = generated.resolve("workbench-overlay.css");
			Files.writeString(cssFile, WorkbenchCssGenerator.generate(theme), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Could not write the workbench CSS overlay", e);
		}

		Runnable apply = () -> applyCss(workbench, theme, cssFile);
		Display display = workbench.getDisplay();
		if (Display.getCurrent() == display) {
			apply.run();
		} else {
			display.syncExec(apply);
		}
	}

	private static void applyCss(IWorkbench workbench, Theme theme, Path cssFile) {
		IThemeEngine engine = workbench.getService(IThemeEngine.class);
		if (engine == null) {
			return;
		}

		String targetTheme = theme.getType() == ThemeType.DARK ? DARK_THEME_ID : LIGHT_THEME_ID;
		synchronized (REGISTERED_ENGINES) {
			if (REGISTERED_ENGINES.add(engine)) {
				engine.registerStylesheet(cssFile.toUri().toString());
			}
		}
		engine.setTheme(targetTheme, true);
	}
}
