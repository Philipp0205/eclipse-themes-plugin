package com.github.eclipsethemes.eclipse.adapters.ui;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
	private static final String GENERATED_THEME_PREFIX = "com.github.eclipsethemes.generated.";
	private static final String DISPLAY_ENGINE_KEY = "org.eclipse.e4.ui.css.swt.theme";

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
		Display display = workbench.getDisplay();
		IThemeEngine serviceEngine = workbench.getService(IThemeEngine.class);
		IThemeEngine displayEngine = display == null ? null
				: (IThemeEngine) display.getData(DISPLAY_ENGINE_KEY);
		IThemeEngine engine = displayEngine != null ? displayEngine : serviceEngine;

		String cssUri = cssFile.toUri().toString();
		String targetTheme = theme.getType() == ThemeType.DARK ? DARK_THEME_ID : LIGHT_THEME_ID;
		String activeBefore = engine == null || engine.getActiveTheme() == null ? "null"
				: engine.getActiveTheme().getId();
		boolean cssExists = Files.isRegularFile(cssFile);
		List<String> themeIds = engine == null ? List.of()
				: engine.getThemes().stream().map(ITheme::getId).collect(Collectors.toList());
		// #region agent log
		try {
			java.util.HashMap<String, Object> data = new java.util.HashMap<>();
			data.put("engineNull", engine == null);
			data.put("serviceEngineNull", serviceEngine == null);
			data.put("displayEngineNull", displayEngine == null);
			data.put("sameEngine", serviceEngine == displayEngine);
			data.put("themeType", String.valueOf(theme.getType()));
			data.put("targetTheme", targetTheme);
			data.put("activeBefore", activeBefore);
			data.put("themeCount", themeIds.size());
			data.put("themeIds", themeIds.toString());
			data.put("cssExists", cssExists);
			data.put("cssBytes", cssExists ? Files.size(cssFile) : -1L);
			data.put("runId", "post-fix");
			Files.writeString(Path.of("/opt/cursor/logs/debug.log"),
					DbgNdjson.line("C", "WorkbenchCssThemeAdapter.applyCss", "engine_lookup", data),
					java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception ignored) {
		}
		// #endregion
		if (engine == null) {
			return;
		}

		boolean newlyRegistered = false;
		boolean hasPlatformThemes = !engine.getThemes().isEmpty();
		if (hasPlatformThemes) {
			synchronized (REGISTERED_ENGINES) {
				newlyRegistered = REGISTERED_ENGINES.add(engine);
				if (newlyRegistered) {
					engine.registerStylesheet(cssUri);
				}
			}
		}

		// #region agent log
		try {
			Files.writeString(Path.of("/opt/cursor/logs/debug.log"),
					DbgNdjson.line("D", "WorkbenchCssThemeAdapter.applyCss", "before_setTheme",
							java.util.Map.of(
									"newlyRegistered", newlyRegistered,
									"targetTheme", targetTheme,
									"activeBefore", activeBefore,
									"runId", "post-fix")),
					java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception ignored) {
		}
		// #endregion

		String appliedVia = activateTheme(engine, theme, targetTheme, cssUri);
		forceReapply(engine);
		applyToShells(display, engine);

		String activeAfter = engine.getActiveTheme() == null ? "null" : engine.getActiveTheme().getId();
		// #region agent log
		try {
			Files.writeString(Path.of("/opt/cursor/logs/debug.log"),
					DbgNdjson.line("D", "WorkbenchCssThemeAdapter.applyCss", "after_setTheme",
							java.util.Map.of(
									"activeAfter", activeAfter,
									"changed", !activeBefore.equals(activeAfter),
									"appliedVia", appliedVia,
									"runId", "post-fix")),
					java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception ignored) {
		}
		// #endregion
	}

	private static String activateTheme(IThemeEngine engine, Theme theme, String targetTheme, String cssUri) {
		engine.setTheme(targetTheme, true);
		if (engine.getActiveTheme() != null) {
			return "platform:" + engine.getActiveTheme().getId();
		}

		String needle = theme.getType() == ThemeType.DARK ? "dark" : "default";
		for (ITheme candidate : engine.getThemes()) {
			if (candidate.getId().toLowerCase().contains(needle)) {
				engine.setTheme(candidate, true);
				if (engine.getActiveTheme() != null) {
					return "matched:" + candidate.getId();
				}
			}
		}

		// No platform CSS themes available (e.g. org.eclipse.ui.themes missing):
		// register the generated overlay as its own theme so styles still apply.
		String generatedId = GENERATED_THEME_PREFIX
				+ (theme.getType() == ThemeType.DARK ? "dark" : "light");
		boolean exists = engine.getThemes().stream().anyMatch(t -> generatedId.equals(t.getId()));
		if (!exists) {
			engine.registerTheme(generatedId, "Eclipse Themes Generated", cssUri);
		}
		engine.setTheme(generatedId, true);
		return engine.getActiveTheme() == null ? "generated-failed" : "generated:" + generatedId;
	}

	private static void forceReapply(IThemeEngine engine) {
		try {
			Method reset = engine.getClass().getMethod("resetCurrentTheme");
			reset.invoke(engine);
			return;
		} catch (ReflectiveOperationException ignored) {
			// Fall through to the public force overload when present.
		}
		try {
			ITheme active = engine.getActiveTheme();
			if (active == null) {
				return;
			}
			Method forced = engine.getClass().getMethod("setTheme", ITheme.class, boolean.class, boolean.class);
			forced.invoke(engine, active, true, true);
		} catch (ReflectiveOperationException ignored) {
			ITheme active = engine.getActiveTheme();
			if (active != null) {
				engine.setTheme(active, true);
			}
		}
	}

	private static void applyToShells(Display display, IThemeEngine engine) {
		if (display == null || display.isDisposed()) {
			return;
		}
		for (Shell shell : display.getShells()) {
			if (shell != null && !shell.isDisposed()) {
				engine.applyStyles(shell, true);
			}
		}
	}
}
