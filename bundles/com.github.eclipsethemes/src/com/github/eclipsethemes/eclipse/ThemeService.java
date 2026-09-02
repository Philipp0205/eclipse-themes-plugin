package com.github.eclipsethemes.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.prefs.BackingStoreException;

import com.github.eclipsethemes.core.models.Theme;
import com.github.eclipsethemes.eclipse.adapters.EclipseThemeAdapter;

public final class ThemeService {

	private final AdapterRegistry registry;
	private final ILog log;

	public ThemeService(AdapterRegistry registry, ILog log) {
		this.registry = registry;
		this.log = log;
	}

	public void applyTheme(IWorkbench workbench, Theme theme) {
		// #region agent log
		try { java.nio.file.Files.writeString(java.nio.file.Path.of("/opt/cursor/logs/debug.log"), com.github.eclipsethemes.eclipse.adapters.ui.DbgNdjson.line("E","ThemeService.applyTheme","start", java.util.Map.of("themeId", theme.getId(), "themeType", String.valueOf(theme.getType()), "adapterCount", registry.getAdapters().size(), "workbenchNull", workbench == null)), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); } catch (Exception ignored) {}
		// #endregion
		registry.getAdapters().forEach((adapter, plugin) -> {
			// If a required plugin is declared, skip when it is not installed
			boolean skipped = plugin != null && Platform.getBundle(plugin) == null;
			// #region agent log
			try { java.nio.file.Files.writeString(java.nio.file.Path.of("/opt/cursor/logs/debug.log"), com.github.eclipsethemes.eclipse.adapters.ui.DbgNdjson.line("E","ThemeService.applyTheme","adapter", java.util.Map.of("adapter", adapter.getClass().getSimpleName(), "plugin", String.valueOf(plugin), "skipped", skipped)), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); } catch (Exception ignored) {}
			// #endregion
			if (skipped) return;

			try {
				String prefNodeId = adapter.getPreferencesId();
				IEclipsePreferences prefs = prefNodeId != null
						? InstanceScope.INSTANCE.getNode(prefNodeId)
						: null;
				adapter.applyWorkbench(theme, prefs, workbench);
			} catch (BackingStoreException e) {
				log.error("Could not apply theme via adapter " + adapter.getClass().getSimpleName(), e);
			} catch (RuntimeException e) {
				log.error("Could not apply workbench theme via adapter " + adapter.getClass().getSimpleName(), e);
			}
		});
	}
}
