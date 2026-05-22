/*
 * Copyright (c) 2024, RuneLite
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.runeshop;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import com.google.inject.Provides;
import com.google.inject.Provider;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.AsyncBufferedImage;

@PluginDescriptor(
	name = "Rune Shop Calculator",
	description = "Calculate the total gp cost of buying runes from shops, accounting for price inflation and world hopping",
	tags = {"rune", "shop", "magic", "calculator", "cost", "price", "inflation", "panel"}
)
public class RuneShopPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Provider<RuneShopPanel> uiPanel;

	private NavigationButton uiNavigationButton;

	@Provides
	RuneShopConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneShopConfig.class);
	}

	@Override
	protected void startUp()
	{
		// AsyncBufferedImage IS a BufferedImage, so NavigationButton accepts it directly.
		// The icon renders transparent for a tick until the client serves the pixels,
		// at which point we remove/re-add the button to flush the toolbar render.
		final AsyncBufferedImage icon = itemManager.getImage(ItemID.CHAOSRUNE);

		uiNavigationButton = NavigationButton.builder()
			.tooltip("Rune Shop Calculator")
			.icon(icon)
			.priority(7)
			.panel(uiPanel.get())
			.build();

		clientToolbar.addNavigation(uiNavigationButton);

		icon.onLoaded(() -> SwingUtilities.invokeLater(() ->
		{
			if (uiNavigationButton != null)
			{
				clientToolbar.removeNavigation(uiNavigationButton);
				clientToolbar.addNavigation(uiNavigationButton);
			}
		}));
	}

	@Override
	protected void shutDown()
	{
		if (uiNavigationButton != null)
		{
			clientToolbar.removeNavigation(uiNavigationButton);
			uiNavigationButton = null;
		}
	}
}
