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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.google.inject.Singleton;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;

@Singleton
class RuneShopPanel extends PluginPanel
{
	// ── Palette ────────────────────────────────────────────────────────────────
	private static final Color ROW_BG        = new Color(50, 50, 50);
	private static final Color ROW_BG_ALT    = new Color(40, 40, 40);
	private static final Color LABEL_COLOR   = new Color(180, 180, 180);
	private static final Color INPUT_BG      = new Color(28, 28, 28);
	private static final Color INPUT_BORDER  = new Color(80, 80, 80);
	private static final Color RESULT_BG     = new Color(0, 23, 46);
	private static final Color RESULT_BORDER = new Color(10, 58, 107);
	private static final Color RESULT_ALT    = new Color(2, 40, 79);
	private static final Color GOLD          = new Color(255, 215, 0);

	// ── Injected ───────────────────────────────────────────────────────────────
	private final ItemManager    itemManager;
	private final RuneShopConfig config;

	// ── Per-rune UI ────────────────────────────────────────────────────────────
	/** Rune icon toggle = "is this rune included in the calculation?" */
	private final Map<RuneData, RuneToggleIcon> runeIcons    = new EnumMap<>(RuneData.class);
	/** Pack icon toggle = "buy packs for this rune?" (pack runes only) */
	private final Map<RuneData, RuneToggleIcon> packIcons    = new EnumMap<>(RuneData.class);
	private final Map<RuneData, JTextField>     amountFields  = new EnumMap<>(RuneData.class);
	/** Max packs per world field — only present for pack runes, disabled unless pack icon active */
	private final Map<RuneData, JTextField>     maxPackFields = new EnumMap<>(RuneData.class);

	private JTextField inflationField;
	private JCheckBox  selectAllBox;
	private JPanel     resultSection;

	// ══════════════════════════════════════════════════════════════════════════
	@Inject
	RuneShopPanel(ItemManager itemManager, RuneShopConfig config)
	{
		super();
		getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.itemManager = itemManager;
		this.config      = config;

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(6, 6, 6, 6));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(buildInflationRow());
		add(Box.createVerticalStrut(4));
		add(buildSelectAllRow());
		add(Box.createVerticalStrut(5));
		add(buildSeparator());
		add(Box.createVerticalStrut(2));

		int idx = 0;
		for (RuneData rune : RuneData.values())
		{
			add(buildRuneRow(rune, idx++ % 2 == 0 ? ROW_BG : ROW_BG_ALT));
		}

		add(Box.createVerticalStrut(10));
		add(buildCalcButton());
		add(Box.createVerticalStrut(10));

		resultSection = new JPanel();
		resultSection.setLayout(new BoxLayout(resultSection, BoxLayout.Y_AXIS));
		resultSection.setBackground(ColorScheme.DARK_GRAY_COLOR);
		resultSection.setAlignmentX(LEFT_ALIGNMENT);
		add(resultSection);
	}

	// ── Header ─────────────────────────────────────────────────────────────────

	private JPanel buildInflationRow()
	{
		JPanel p = flowRow(ColorScheme.DARK_GRAY_COLOR);
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		p.add(label("Inflation %:", LABEL_COLOR));
		inflationField = makeTextField(5);
		inflationField.setText(String.valueOf(config.inflationPercent()));
		inflationField.setToolTipText("Price increase per unit purchased (0.1 = 0.1%)");
		p.add(inflationField);
		return p;
	}

	private JPanel buildSelectAllRow()
	{
		JPanel p = flowRow(ColorScheme.DARK_GRAY_COLOR);
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		selectAllBox = new JCheckBox("Select all runes");
		selectAllBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		selectAllBox.setForeground(LABEL_COLOR);
		selectAllBox.setFont(FontManager.getRunescapeSmallFont());
		selectAllBox.addItemListener(e ->
			runeIcons.values().forEach(ic -> ic.setSelected(selectAllBox.isSelected())));
		p.add(selectAllBox);
		return p;
	}

	private JPanel buildSeparator()
	{
		JPanel sep = new JPanel();
		sep.setBackground(new Color(70, 70, 70));
		sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		sep.setAlignmentX(LEFT_ALIGNMENT);
		return sep;
	}

	private JButton buildCalcButton()
	{
		JButton btn = new JButton("Calculate");
		btn.setBackground(new Color(46, 139, 87));
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setFont(FontManager.getRunescapeBoldFont());
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
		btn.setAlignmentX(LEFT_ALIGNMENT);
		btn.addActionListener(e -> calculate());
		return btn;
	}

	// ── Rune rows ──────────────────────────────────────────────────────────────
	//
	//  All runes:   [rune icon]  [______Qty_______]
	//  Pack runes:  [pack icon]  [___Max packs/W__]  (disabled until pack icon selected)
	//
	private JPanel buildRuneRow(RuneData rune, Color bg)
	{
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
		outer.setBackground(bg);
		outer.setBorder(BorderFactory.createCompoundBorder(
			new MatteBorder(0, 0, 1, 0, new Color(30, 30, 30)),
			new EmptyBorder(5, 6, 5, 6)
		));
		outer.setAlignmentX(LEFT_ALIGNMENT);
		// Row heights accommodate 16 pt font fields (~30 px) + icon (24 px) + padding
		outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, rune.isHasPack() ? 88 : 50));

		// ── Line 1: [rune icon]  [field — placeholder "Qty"] ──────────────────
		RuneToggleIcon runeIcon = new RuneToggleIcon(rune.getItemId(), itemManager, bg,
			rune.getDisplayName() + " — click to select");
		runeIcons.put(rune, runeIcon);

		JTextField amtField = makePlaceholderTextField(7, "Qty");
		amtField.setToolTipText("Amount to buy — supports 10k, 1.5m");
		amountFields.put(rune, amtField);

		JPanel line1 = new JPanel(new BorderLayout(6, 0));
		line1.setBackground(bg);
		line1.add(runeIcon, BorderLayout.WEST);
		line1.add(amtField, BorderLayout.CENTER);

		outer.add(line1);

		// Bidirectional sync: rune icon ↔ qty field.
		// boolean[] lets both the DocumentListener and the setOnToggle lambda share
		// a single guard without a field, preventing circular update loops.
		boolean[] guard = {false};
		amtField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void insertUpdate(DocumentEvent e)
			{
				if (!guard[0] && !amtField.getText().isEmpty())
				{
					guard[0] = true;
					runeIcon.setSelected(true);
					guard[0] = false;
				}
			}
			@Override public void removeUpdate(DocumentEvent e)
			{
				// invokeLater: removeUpdate fires mid-mutation; we must wait for
				// the document to finish before reading getText().
				SwingUtilities.invokeLater(() ->
				{
					if (!guard[0] && amtField.getText().isEmpty() && runeIcon.isSelected())
					{
						guard[0] = true;
						runeIcon.setSelected(false);
						guard[0] = false;
					}
				});
			}
			@Override public void changedUpdate(DocumentEvent e) { }
		});
		runeIcon.setOnToggle(sel ->
		{
			if (!guard[0] && !sel && !amtField.getText().isEmpty())
			{
				guard[0] = true;
				amtField.setText("");
				guard[0] = false;
			}
		});

		// ── Line 2 (pack runes only): [pack icon]  [field — "Max packs/W"] ────
		if (rune.isHasPack())
		{
			JTextField maxField = makePlaceholderTextField(7, "Max packs/W");
			maxField.setEnabled(false);
			maxField.setToolTipText("Max packs to buy per world (blank = full stock)");
			maxPackFields.put(rune, maxField);

			RuneToggleIcon packIcon = new RuneToggleIcon(rune.getPackItemId(), itemManager, bg,
				"Buy packs of " + rune.getRunesPerPack() + " — click to enable");
			packIcons.put(rune, packIcon);

			boolean[] packGuard = {false};
			packIcon.setOnToggle(sel ->
			{
				maxField.setEnabled(sel);
				if (!packGuard[0] && !sel && !maxField.getText().isEmpty())
				{
					packGuard[0] = true;
					maxField.setText("");
					packGuard[0] = false;
				}
			});
			maxField.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override public void insertUpdate(DocumentEvent e) { }
				@Override public void removeUpdate(DocumentEvent e)
				{
					SwingUtilities.invokeLater(() ->
					{
						if (!packGuard[0] && maxField.getText().isEmpty() && packIcon.isSelected())
						{
							packGuard[0] = true;
							packIcon.setSelected(false);
							packGuard[0] = false;
						}
					});
				}
				@Override public void changedUpdate(DocumentEvent e) { }
			});

			JPanel line2 = new JPanel(new BorderLayout(6, 0));
			line2.setBackground(bg);
			line2.setBorder(new EmptyBorder(4, 0, 0, 0));
			line2.add(packIcon, BorderLayout.WEST);
			line2.add(maxField, BorderLayout.CENTER);

			outer.add(line2);
		}

		return outer;
	}

	// ── Calculation ────────────────────────────────────────────────────────────

	private void calculate()
	{
		double rate;
		try
		{
			rate = Double.parseDouble(inflationField.getText().trim()) / 100.0;
			if (rate < 0) rate = 0;
		}
		catch (NumberFormatException e)
		{
			rate = config.inflationPercent() / 100.0;
			inflationField.setText(String.valueOf(config.inflationPercent()));
		}

		resultSection.removeAll();

		long    totalCost  = 0;
		long    totalRunes = 0;
		boolean anyResult  = false;

		JPanel table = new JPanel(new GridBagLayout());
		table.setBackground(RESULT_BG);
		table.setBorder(BorderFactory.createLineBorder(RESULT_BORDER));
		table.setAlignmentX(LEFT_ALIGNMENT);
		table.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		GridBagConstraints c = new GridBagConstraints();
		c.fill   = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4, 6, 4, 6);
		c.gridy  = 0;

		String[] headers = {"Rune", "Cost", "Worlds", "Avg gp"};
		double[] weights = {2.8,     2.2,    1.5,      2.0};
		addHeaderRow(table, c, headers, weights);

		for (RuneData rune : RuneData.values())
		{
			if (!runeIcons.get(rune).isSelected()) continue;

			int qty = parseAmount(amountFields.get(rune).getText());
			if (qty <= 0) continue;

			anyResult = true;
			boolean usePacks    = rune.isHasPack()
				&& packIcons.containsKey(rune)
				&& packIcons.get(rune).isSelected();
			int     maxPerWorld = (usePacks && maxPackFields.containsKey(rune))
				? parseAmount(maxPackFields.get(rune).getText()) : 0;

			long   runeCost;
			int    runeCount;
			String worldsStr;

			if (usePacks)
			{
				int packs    = qty / rune.getRunesPerPack();
				int leftover = qty % rune.getRunesPerPack();
				RuneShopCalculator.SimResult ps = RuneShopCalculator.simulate(
					rune.getPackPrice(), rune.getPackStock(), packs, rate, maxPerWorld);
				RuneShopCalculator.SimResult rs = RuneShopCalculator.simulate(
					rune.getBaseRunePrice(), rune.getRuneStock(), leftover, rate, 0);
				runeCost  = ps.cost + rs.cost;
				runeCount = packs * rune.getRunesPerPack() + leftover;
				if (packs > 0 && leftover > 0)
					worldsStr = ps.worlds + "p/" + rs.worlds + "r";
				else if (packs > 0)
					worldsStr = String.valueOf(ps.worlds);
				else
					worldsStr = String.valueOf(rs.worlds);
			}
			else
			{
				RuneShopCalculator.SimResult sim = RuneShopCalculator.simulate(
					rune.getBaseRunePrice(), rune.getRuneStock(), qty, rate, 0);
				runeCost  = sim.cost;
				runeCount = qty;
				worldsStr = String.valueOf(sim.worlds);
			}

			totalCost  += runeCost;
			totalRunes += runeCount;

			c.gridy++;
			Color rowBg   = c.gridy % 2 == 0 ? RESULT_ALT : RESULT_BG;
			String avgStr = runeCount > 0
				? String.format("%.2f", (double) runeCost / runeCount) : "-";
			addResultRow(table, c, weights,
				rune.shortName(), formatGp(runeCost), worldsStr, avgStr, rowBg);
		}

		if (!anyResult)
		{
			JLabel msg = label("Click a rune icon to select it, then enter amounts.",
				ColorScheme.LIGHT_GRAY_COLOR);
			msg.setAlignmentX(LEFT_ALIGNMENT);
			resultSection.add(msg);
			resultSection.revalidate();
			resultSection.repaint();
			return;
		}

		resultSection.add(table);
		resultSection.add(Box.createVerticalStrut(8));
		resultSection.add(buildTotalsCard(totalRunes, totalCost));
		resultSection.revalidate();
		resultSection.repaint();
	}

	// ── Result table helpers ───────────────────────────────────────────────────

	private void addHeaderRow(JPanel table, GridBagConstraints c,
		String[] headers, double[] weights)
	{
		for (int i = 0; i < headers.length; i++)
		{
			GridBagConstraints gc = (GridBagConstraints) c.clone();
			gc.gridx   = i;
			gc.weightx = weights[i];
			JLabel lbl = new JLabel(headers[i]);
			lbl.setForeground(Color.WHITE);
			lbl.setFont(FontManager.getRunescapeBoldFont());
			lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, RESULT_BORDER));
			table.add(lbl, gc);
		}
	}

	private void addResultRow(JPanel table, GridBagConstraints c, double[] weights,
		String rune, String cost, String worlds, String avg, Color bg)
	{
		String[] vals = {rune, cost, worlds, avg};
		for (int i = 0; i < vals.length; i++)
		{
			GridBagConstraints gc = (GridBagConstraints) c.clone();
			gc.gridx   = i;
			gc.weightx = weights[i];
			JLabel lbl = label(vals[i], new Color(220, 220, 220));
			lbl.setOpaque(true);
			lbl.setBackground(bg);
			lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
			table.add(lbl, gc);
		}
	}

	private JPanel buildTotalsCard(long totalRunes, long totalCost)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(RESULT_BG);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(RESULT_BORDER),
			new EmptyBorder(8, 10, 8, 10)
		));
		card.setAlignmentX(LEFT_ALIGNMENT);
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		addTotalRow(card, "Total runes:", formatCompact(totalRunes));
		addTotalRow(card, "Total cost:",  formatGp(totalCost) + " gp");
		if (totalRunes > 0)
			addTotalRow(card, "Avg gp/rune:",
				String.format("%.2f", (double) totalCost / totalRunes));
		return card;
	}

	private void addTotalRow(JPanel card, String key, String value)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(RESULT_BG);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		JLabel k = new JLabel(key);
		k.setForeground(Color.LIGHT_GRAY);
		k.setFont(FontManager.getRunescapeBoldFont());
		JLabel v = label(value, GOLD);
		row.add(k, BorderLayout.WEST);
		row.add(v, BorderLayout.EAST);
		card.add(row);
	}

	// ── Swing helpers ──────────────────────────────────────────────────────────

	private static JPanel flowRow(Color bg)
	{
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		p.setBackground(bg);
		p.setAlignmentX(LEFT_ALIGNMENT);
		return p;
	}

	private static JLabel label(String text, Color color)
	{
		JLabel l = new JLabel(text);
		l.setForeground(color);
		l.setFont(FontManager.getRunescapeSmallFont());
		return l;
	}

	/** Plain text field — used for inputs without placeholder text (e.g. inflation %). */
	private JTextField makeTextField(int cols)
	{
		JTextField tf = new JTextField(cols);
		tf.setBackground(INPUT_BG);
		tf.setForeground(Color.WHITE);
		tf.setCaretColor(Color.WHITE);
		tf.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(INPUT_BORDER),
			new EmptyBorder(3, 5, 3, 5)
		));
		tf.setFont(FontManager.getRunescapeSmallFont());
		return tf;
	}

	/** Text field that shows {@code placeholder} in grey when the field is empty. */
	private JTextField makePlaceholderTextField(int cols, String placeholder)
	{
		JTextField tf = new PlaceholderTextField(cols, placeholder);
		tf.setBackground(INPUT_BG);
		tf.setForeground(Color.WHITE);
		tf.setCaretColor(Color.WHITE);
		tf.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(INPUT_BORDER),
			new EmptyBorder(3, 5, 3, 5)
		));
		tf.setFont(FontManager.getRunescapeSmallFont());
		return tf;
	}

	// ── Parsing / formatting ───────────────────────────────────────────────────

	private static int parseAmount(String text)
	{
		if (text == null || text.isBlank()) return 0;
		String s = text.trim().toLowerCase().replace(",", "");
		try
		{
			if (s.endsWith("m")) return (int)(Double.parseDouble(s.substring(0, s.length() - 1)) * 1_000_000);
			if (s.endsWith("k")) return (int)(Double.parseDouble(s.substring(0, s.length() - 1)) * 1_000);
			return (int) Double.parseDouble(s);
		}
		catch (NumberFormatException e) { return 0; }
	}

	private static String formatGp(long gp)
	{
		if (gp >= 1_000_000) return String.format("%.2fm", gp / 1_000_000.0);
		if (gp >= 1_000)     return String.format("%.1fk", gp / 1_000.0);
		return String.valueOf(gp);
	}

	private static String formatCompact(long n)
	{
		if (n >= 1_000_000) return String.format("%.1fm", n / 1_000_000.0);
		if (n >= 1_000)     return String.format("%.1fk", n / 1_000.0);
		return String.valueOf(n);
	}

	// ══════════════════════════════════════════════════════════════════════════
	// Inner class: clickable icon toggle (used for both rune and pack icons)
	// ══════════════════════════════════════════════════════════════════════════

	/**
	 * A 24×24 square that renders an OSRS item icon and acts as a boolean toggle.
	 *
	 * <ul>
	 *   <li><b>Unselected</b> — icon dimmed with a dark overlay</li>
	 *   <li><b>Hovered</b>    — icon at full brightness</li>
	 *   <li><b>Selected</b>   — icon at full brightness + gold border</li>
	 * </ul>
	 *
	 * An optional {@link Consumer}{@code <Boolean>} callback fires on every toggle.
	 */
	private static final class RuneToggleIcon extends JPanel
	{
		private static final int   SIZE           = 24;
		private static final int   ICON_SIZE      = 18;
		private static final Color BORDER_GOLD    = new Color(255, 215, 0);
		private static final Color OVERLAY_HOVER  = new Color(200, 200, 200, 50);
		private static final Color OVERLAY_DIM    = new Color(0, 0, 0, 140);

		private boolean            selected = false;
		private boolean            hovered  = false;
		private Image              iconImage;
		private Consumer<Boolean>  onToggle;

		RuneToggleIcon(int itemId, ItemManager itemManager, Color bg, String tooltip)
		{
			setPreferredSize(new Dimension(SIZE, SIZE));
			setMaximumSize(new Dimension(SIZE, SIZE));
			setMinimumSize(new Dimension(SIZE, SIZE));
			setBackground(bg);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setToolTipText(tooltip);

			AsyncBufferedImage img = itemManager.getImage(itemId);
			img.onLoaded(() -> SwingUtilities.invokeLater(() ->
			{
				iconImage = img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
				repaint();
			}));

			addMouseListener(new MouseAdapter()
			{
				@Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
				@Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
				@Override public void mousePressed(MouseEvent e) { setSelected(!selected); }
			});
		}

		/** Register a callback that fires with the new selected state on each click. */
		void setOnToggle(Consumer<Boolean> cb) { this.onToggle = cb; }

		boolean isSelected() { return selected; }

		void setSelected(boolean sel)
		{
			selected = sel;
			if (onToggle != null) onToggle.accept(sel);
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

			if (iconImage != null)
			{
				int off = (SIZE - ICON_SIZE) / 2;
				g2.drawImage(iconImage, off, off, this);
			}

			if (selected)
			{
				g2.setColor(new Color(255, 215, 0, 40));
				g2.fillRect(0, 0, SIZE, SIZE);
				g2.setColor(BORDER_GOLD);
				g2.setStroke(new BasicStroke(2f));
				g2.drawRect(1, 1, SIZE - 2, SIZE - 2);
			}
			else if (hovered)
			{
				g2.setColor(OVERLAY_HOVER);
				g2.fillRect(0, 0, SIZE, SIZE);
			}
			else
			{
				g2.setColor(OVERLAY_DIM);
				g2.fillRect(0, 0, SIZE, SIZE);
			}

			g2.dispose();
		}
	}

	// ══════════════════════════════════════════════════════════════════════════
	// Inner class: text field with greyed-out placeholder text
	// ══════════════════════════════════════════════════════════════════════════

	/**
	 * A {@link JTextField} that paints a greyed-out hint string when the field is
	 * empty. The hint disappears as soon as the user starts typing and reappears
	 * when the field is cleared.
	 */
	private static final class PlaceholderTextField extends JTextField
	{
		private final String placeholder;

		PlaceholderTextField(int cols, String placeholder)
		{
			super(cols);
			this.placeholder = placeholder;
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (!getText().isEmpty()) return;

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(isEnabled() ? new Color(110, 110, 110) : new Color(75, 75, 75));
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			Insets ins = getInsets();
			int x = ins.left;
			int y = ins.top
				+ (getHeight() - ins.top - ins.bottom - fm.getHeight()) / 2
				+ fm.getAscent();
			g2.drawString(placeholder, x, y);
			g2.dispose();
		}
	}
}
