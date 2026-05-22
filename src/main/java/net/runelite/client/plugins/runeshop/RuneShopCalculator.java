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

class RuneShopCalculator
{
	static class SimResult
	{
		final long cost;
		final int worlds;

		SimResult(long cost, int worlds)
		{
			this.cost = cost;
			this.worlds = worlds;
		}
	}

	/**
	 * Simulates buying {@code qty} units from a shop with price inflation per unit purchased.
	 * Price resets when stock (or maxPerWorld, whichever is lower) runs out and you hop worlds.
	 *
	 * @param basePrice    base price of a single unit
	 * @param stock        units available per world
	 * @param qty          total units to buy
	 * @param rate         fractional price increase per unit (e.g. 0.001 for 0.1%)
	 * @param maxPerWorld  hard cap on units to buy per world, 0 = unlimited
	 */
	static SimResult simulate(int basePrice, int stock, int qty, double rate, int maxPerWorld)
	{
		if (qty <= 0)
		{
			return new SimResult(0, 0);
		}

		long cost = 0;
		int remaining = qty;
		int worlds = 0;

		while (remaining > 0)
		{
			worlds++;
			int cap = (maxPerWorld > 0) ? Math.min(stock, maxPerWorld) : stock;
			int toBuy = Math.min(remaining, cap);

			for (int i = 0; i < toBuy; i++)
			{
				cost += (long) Math.floor(basePrice * (1.0 + rate * i));
			}

			remaining -= toBuy;
		}

		return new SimResult(cost, worlds);
	}
}
