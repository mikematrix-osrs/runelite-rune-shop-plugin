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
package com.mikematrixosrs.runeshop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ItemID;

@Getter
@RequiredArgsConstructor
enum RuneData
{
	AIR("Air rune",     ItemID.AIRRUNE,    4,   5000, true,  430,  80, 100, ItemID.PACK_AIRRUNE),
	WATER("Water rune", ItemID.WATERRUNE,  4,   5000, true,  430,  80, 100, ItemID.PACK_WATERRUNE),
	EARTH("Earth rune", ItemID.EARTHRUNE,  4,   5000, true,  430,  80, 100, ItemID.PACK_EARTHRUNE),
	FIRE("Fire rune",   ItemID.FIRERUNE,   4,   5000, true,  430,  80, 100, ItemID.PACK_FIRERUNE),
	MIND("Mind rune",   ItemID.MINDRUNE,   3,   5000, true,  330,  40, 100, ItemID.PACK_MINDRUNE),
	BODY("Body rune",   ItemID.BODYRUNE,   3,   5000, false,   0,   0,   0, -1),
	CHAOS("Chaos rune", ItemID.CHAOSRUNE,  90,   250, true, 9950,  35, 100, ItemID.PACK_CHAOSRUNE),
	NATURE("Nature rune", ItemID.NATURERUNE, 180, 250, false,  0,   0,   0, -1),
	COSMIC("Cosmic rune", ItemID.COSMICRUNE, 50,  250, false,  0,   0,   0, -1),
	LAW("Law rune",     ItemID.LAWRUNE,   240,   250, false,  0,   0,   0, -1),
	DEATH("Death rune", ItemID.DEATHRUNE, 180,   250, false,  0,   0,   0, -1),
	BLOOD("Blood rune", ItemID.BLOODRUNE, 400,   250, false,  0,   0,   0, -1);

	private final String displayName;
	private final int itemId;
	private final int baseRunePrice;
	private final int runeStock;
	private final boolean hasPack;
	private final int packPrice;
	private final int packStock;
	private final int runesPerPack;
	/** Item ID of the rune pack, or -1 for runes that have no pack. */
	private final int packItemId;

	String shortName()
	{
		return displayName.replace(" rune", "");
	}
}
