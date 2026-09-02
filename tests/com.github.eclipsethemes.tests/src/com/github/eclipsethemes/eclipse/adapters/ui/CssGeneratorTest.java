package com.github.eclipsethemes.eclipse.adapters.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.eclipsethemes.core.models.Color;
import com.github.eclipsethemes.core.models.Theme;
import com.github.eclipsethemes.core.models.ThemeType;
import com.github.eclipsethemes.core.models.Token;
import com.github.eclipsethemes.core.models.TokenKey;

class CssGeneratorTest {

	@Test
	void workbenchCssUsesThemeSurfaceAndSelectionColors() {
		String css = WorkbenchCssGenerator.generate(darkDimmed());

		assertTrue(css.contains("DARK_BACKGROUND { color: #22272e; }"));
		assertTrue(css.contains("background-color: #22272e;"));
		assertTrue(css.contains("swt-selection-background-color: #3d4149;"));
		assertTrue(css.contains("swt-selection-foreground-color: #adbac7;"));
	}

	@Test
	void gtkCssDefinesNativeBackgroundAndSelectionColors() {
		String css = GtkCssGenerator.generate(darkDimmed());

		assertTrue(css.contains("@define-color theme_bg_color #22272e;"));
		assertTrue(css.contains("@define-color theme_fg_color #adbac7;"));
		assertTrue(css.contains("@define-color theme_selected_bg_color #3d4149;"));
		assertTrue(css.contains("@define-color theme_selected_fg_color #adbac7;"));
		assertTrue(css.contains("*:selected"));
	}

	private static Theme darkDimmed() {
		Theme theme = new Theme("test", "GitHub Dark Dimmed", "Eclipse Themes", null, null, null, ThemeType.DARK);
		theme.addToken(new Token(TokenKey.BACKGROUND, Color.ofHex("#22272e"), null));
		theme.addToken(new Token(TokenKey.FOREGROUND, Color.ofHex("#adbac7"), null));
		theme.addToken(new Token(TokenKey.SELECTION_BACKGROUND, Color.ofHex("#3d4149"), null));
		theme.addToken(new Token(TokenKey.SELECTION_FOREGROUND, Color.ofHex("#adbac7"), null));
		theme.addToken(new Token(TokenKey.CURRENT_LINE, Color.ofHex("#2d333b"), null));
		return theme;
	}
}
