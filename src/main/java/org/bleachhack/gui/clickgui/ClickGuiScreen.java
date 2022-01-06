/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.gui.clickgui.window.ClickGuiWindow;
import org.bleachhack.gui.clickgui.window.ClickGuiWindow.Tooltip;
import org.bleachhack.gui.window.Window;
import org.bleachhack.gui.window.WindowScreen;

import net.minecraft.client.gui.screen.Screen;

public abstract class ClickGuiScreen extends WindowScreen {

	protected int keyDown = -1;
	protected boolean lmDown = false;
	protected boolean rmDown = false;
	protected boolean lmHeld = false;
	protected int mwScroll = 0;
	
	private int warningOpacity;
	
	@Override
	public void init() {
		super.init();

		warningOpacity = 0;
	}

	@Override
	public boolean shouldPauseGame() {
		return false;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		this.renderBackground();

		for (Window w : getWindows()) {
			if (w instanceof ClickGuiWindow) {
				((ClickGuiWindow) w).updateKeys(mouseX, mouseY, keyDown, lmDown, rmDown, lmHeld, mwScroll);
			}
		}

		super.render(mouseX, mouseY, delta);

		for (Window w : getWindows()) {
			if (w instanceof ClickGuiWindow) {
				Tooltip tooltip = ((ClickGuiWindow) w).getTooltip();

				if (tooltip != null) {
					int tooltipY = tooltip.y;

					String[] split = tooltip.text.split("\n", -1 /* Adding -1 makes it keep empty splits */);
					ArrayUtils.reverse(split);
					for (String s: split) {
						/* Match lines to end of words after it reaches 22 characters long */
						Matcher mat = Pattern.compile(".{1,22}\\b\\W*").matcher(s);

						List<String> lines = new ArrayList<>();

						while (mat.find())
							lines.add(mat.group().trim());

						if (lines.isEmpty())
							lines.add(s);

						int start = tooltipY - lines.size() * 10;
						for (int l = 0; l < lines.size(); l++) {
							fill(tooltip.x, start + (l * 10) - 1,
									tooltip.x + textRenderer.getStringWidth(lines.get(l)) + 3,
									start + (l * 10) + 9, 0xff000000);

							textRenderer.method_956(lines.get(l), tooltip.x + 2, start + (l * 10), -1);
						}

						tooltipY -= lines.size() * 10;
					}
				}
			}
		}
		
		Window.fill(width / 2 - 50, -1, width / 2 - 2, 12,
				mouseX >= width / 2 - 50 && mouseX <= width / 2 - 2 && mouseY >= 0 && mouseY <= 12 ? 0x60b070f0 : 0x60606090);
		Window.fill(width / 2 + 2, -1, width / 2 + 50, 12,
				mouseX >= width / 2 + 2 && mouseX <= width / 2 + 50 && mouseY >= 0 && mouseY <= 12 ? 0x60b070f0 : 0x60606090);

		drawCenteredString(textRenderer, "Modules", width / 2 - 26, 2, 0xf0f0f0);
		drawCenteredString(textRenderer, "UI", width / 2 + 26, 2, 0xf0f0f0);
		
		if (warningOpacity > 3) {
			drawCenteredString(textRenderer, "UI not available on the main menu!", width / 2, 17,
					warningOpacity > 255 ? 0xd14a3b : (warningOpacity << 24) | 0xd14a3b);
			warningOpacity -= 3;
		}

		lmDown = false;
		rmDown = false;
		keyDown = -1;
		mwScroll = 0;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (button == 0) {
			if (mouseX >= width / 2 - 50 && mouseX <= width / 2 - 2 && mouseY >= 0 && mouseY <= 12) {
				tryOpen(ModuleClickGuiScreen.INSTANCE);
			} else if (mouseX >= width / 2 + 2 && mouseX <= width / 2 + 50 && mouseY >= 0 && mouseY <= 12) {
				tryOpen(UIClickGuiScreen.INSTANCE);
			} else {
				lmDown = true;
				lmHeld = true;
			}
		} else if (button == 1) {
			rmDown = true;
		}

		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int button) {
		if (button == 0)
			lmHeld = false;
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void keyPressed(char keyCode, int scanCode) {
		keyDown = scanCode;
		super.keyPressed(keyCode, scanCode);
	}
	private void tryOpen(Screen screen) {
		if (client.world != null) {
			client.openScreen(screen);
		} else {
			warningOpacity = 500;
		}
	}
}
