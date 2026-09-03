package com.github.eclipsethemes.eclipse.adapters.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.jupiter.api.Test;

import com.github.eclipsethemes.core.models.Color;
import com.github.eclipsethemes.core.models.Theme;
import com.github.eclipsethemes.core.models.ThemeType;
import com.github.eclipsethemes.core.models.Token;
import com.github.eclipsethemes.core.models.TokenKey;
import com.github.eclipsethemes.eclipse.preferences.AppearanceSettings;

/**
 * The appearance options are emitted as overrides appended after the base
 * templates, so these tests pin both that a toggle removes its whole block and
 * that the block still lands where the cascade lets it win.
 */
class AppearanceRefinementsTest {

	private static final AppearanceSettings ALL_ON = new AppearanceSettings(true, true, true, true, true, true, true,
			true, true);
	private static final AppearanceSettings ALL_OFF = new AppearanceSettings(false, false, false, false, false, false,
			false, false, false);

	@Test
	void flatToolbarButtonsNeutralizeTheBorderTheGenericButtonRuleReveals() {
		String css = GtkCssGenerator.generate(theme(), ALL_ON);

		// SWT renders a ToolItem as a button inside a toolbar node, so the narrower
		// selector is what keeps the generic rule from boxing every icon.
		Map<String, Set<String>> declarations = CssRules.declarations(css);
		assertTrue(declarations.getOrDefault("toolbar button", Set.of()).contains("border-color"));
		assertTrue(declarations.getOrDefault("toolbar button", Set.of()).contains("background-color"));
		assertTrue(css.contains("""
				toolbar button, toolbar togglebutton, headerbar button, notebook button, button.flat {
				    background-color: transparent;"""));
		assertTrue(css.contains("toolbar button:hover"));
	}

	@Test
	void flatToolbarButtonsComeAfterTheGenericButtonRule() {
		String css = GtkCssGenerator.generate(theme(), ALL_ON);

		// Equal specificity would be decided by order; the override must never
		// precede the generic button rule it corrects.
		assertTrue(css.indexOf("toolbar button, toolbar togglebutton") > css.indexOf("\nbutton {"));
	}

	@Test
	void scrollbarsDropTheTroughAndBorderTheTemplateSets() {
		String css = GtkCssGenerator.generate(theme(), ALL_ON);

		int override = css.lastIndexOf("scrollbar, scrollbar trough");
		assertTrue(override > css.indexOf("scrollbar, scrollbar trough"), "override must repeat the base selector");
		assertTrue(css.substring(override).contains("background-color: transparent;"));
		assertTrue(css.contains("background-clip: padding-box;"));
	}

	@Test
	void listSelectionOverridesTheEditorTextSelection() {
		String css = GtkCssGenerator.generate(theme(), ALL_ON);
		WorkbenchPalette palette = WorkbenchPalette.of(theme());

		assertTrue(css.indexOf("treeview.view:selected, treeview.view:selected:focus,") > css.indexOf("*:selected"));
		assertTrue(css.contains("background-color: " + palette.listSelection() + ";"));
		assertFalse(palette.listSelection().equals(palette.selectionBackground()));
	}

	@Test
	void focusRingsReplaceTheDashedOutline() {
		String css = GtkCssGenerator.generate(theme(), ALL_ON);

		assertTrue(css.contains("outline-style: solid;"));
		assertTrue(css.contains("outline-color: " + WorkbenchPalette.of(theme()).link() + ";"));
	}

	@Test
	void everyGtkOptionDisappearsWhenTurnedOff() {
		String css = GtkCssGenerator.generate(theme(), ALL_OFF);

		assertFalse(css.contains("toolbar button"));
		assertFalse(css.contains("background-clip"));
		assertFalse(css.contains("outline-style"));
		assertFalse(css.contains("border-radius"));
		assertFalse(css.contains("toolbar separator"));
		assertFalse(css.contains(WorkbenchPalette.of(theme()).listSelection()));
	}

	@Test
	void tabOptionsUseTheValuesTheHandlersParse() {
		String css = WorkbenchCssGenerator.generate(theme(), ALL_ON);

		// CSSPropertyCornerRadiusSWTHandler reads a plain number, and
		// CTabRendering#setCornerRadius squares off anything below 6.
		assertTrue(css.contains("swt-corner-radius: 0;"));
		assertTrue(css.contains("swt-unselected-close-visible: false;"));
		assertTrue(css.contains("swt-maximize-visible: false;"));
		assertTrue(css.contains("swt-minimize-visible: false;"));
	}

	@Test
	void squareTabsFoldTheTabStripKeylinesIntoTheTabArea() {
		String css = WorkbenchCssGenerator.generate(theme(), ALL_ON);
		WorkbenchPalette palette = WorkbenchPalette.of(theme());
		String squareTabs = block(css, "/* ===== Square tabs ===== */");

		assertTrue(squareTabs.contains("swt-outer-keyline-color: " + palette.chrome() + ";"));
		assertTrue(squareTabs.contains("swt-tab-outline: " + palette.chrome() + ";"));
		// It has to land after the base rule that paints the keyline as a border.
		assertTrue(css.indexOf("/* ===== Square tabs ===== */") > css
				.indexOf("swt-outer-keyline-color: " + palette.border() + ";"));
	}

	/** Returns one commented section of the generated stylesheet. */
	private static String block(String css, String header) {
		int start = css.indexOf(header);
		assertTrue(start >= 0, "missing section: " + header);
		int end = css.indexOf("/* =====", start + header.length());
		return end < 0 ? css.substring(start) : css.substring(start, end);
	}

	@Test
	void everyTabOptionDisappearsWhenTurnedOff() {
		String css = WorkbenchCssGenerator.generate(theme(), ALL_OFF);

		assertFalse(css.contains("swt-corner-radius"));
		assertFalse(css.contains("swt-unselected-close-visible"));
		assertFalse(css.contains("swt-maximize-visible"));
		assertFalse(css.contains("swt-minimize-visible"));
	}

	@Test
	void theEditorStackUsesTheKeylinePropertyTheEngineActuallyHandles() {
		// swt-tab-outer-keyline is not a property org.eclipse.e4.ui.css.swt
		// registers, so the rule that used it never reached the renderer.
		String css = WorkbenchCssGenerator.generate(theme(), ALL_OFF);

		assertFalse(css.contains("swt-tab-outer-keyline"));
		assertTrue(css.contains("swt-outer-keyline-color"));
	}

	@Test
	void refinementsStillOnlyUseColorsDerivedFromTheTheme() {
		WorkbenchPalette palette = WorkbenchPalette.of(theme());

		for (String css : new String[] { GtkCssGenerator.generate(theme(), ALL_ON),
				WorkbenchCssGenerator.generate(theme(), ALL_ON) }) {
			for (String color : CssRules.literalColors(css)) {
				assertTrue(CssRules.paletteColors(palette).contains(color),
						"Refined CSS contains a hard-coded color: " + color);
			}
		}
	}

	@Test
	void adaptersFallBackToTheDefaultsWithoutAPreferenceNode() {
		assertTrue(AppearanceSettings.of((IEclipsePreferences) null).equals(AppearanceSettings.defaults()));
	}

	private static Theme theme() {
		Theme theme = new Theme("mocha", "Catppuccin Mocha", "Eclipse Themes", null, null, null, ThemeType.DARK);
		theme.addToken(new Token(TokenKey.BACKGROUND, Color.ofHex("#1e1e2e"), null));
		theme.addToken(new Token(TokenKey.FOREGROUND, Color.ofHex("#cdd6f4"), null));
		theme.addToken(new Token(TokenKey.SELECTION_BACKGROUND, Color.ofHex("#3e4056"), null));
		theme.addToken(new Token(TokenKey.SELECTION_FOREGROUND, Color.ofHex("#cdd6f4"), null));
		theme.addToken(new Token(TokenKey.CURRENT_LINE, Color.ofHex("#28283d"), null));
		theme.addToken(new Token(TokenKey.DOC_LINK, Color.ofHex("#89b4fa"), null));
		return theme;
	}
}
