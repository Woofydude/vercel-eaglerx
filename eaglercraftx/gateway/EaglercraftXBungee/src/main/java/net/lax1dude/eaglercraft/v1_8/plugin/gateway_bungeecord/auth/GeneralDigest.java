/*
 * Copyright (c) 2000-2021 The Legion of the Bouncy Castle Inc. (https://www.bouncycastle.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
 */

package net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.auth;

/**
 * base implementation of MD4 family style digest as outlined in "Handbook of
 * Applied Cryptography", pages 344 - 347.
 */
public abstract class GeneralDigest {
	private byte[] xBuf;
	private int xBufOff;

	private long byteCount;

	/**
	 * Standard constructor
	 */
	protected GeneralDigest() {
		xBuf = new byte[4];
		xBufOff = 0;
	}

	/**
	 * Copy constructor. We are using copy constructors in place of the
	 * Object.clone() interface as this interface is not supported by J2ME.
	 */
	protected GeneralDigest(GeneralDigest t) {
		xBuf = new byte[t.xBuf.length];
		System.arraycopy(t.xBuf, 0, xBuf, 0, t.xBuf.length);

		xBufOff = t.xBufOff;
		byteCount = t.byteCount;
	}

	public void update(byte in) {
		xBuf[xBufOff++] = in;

		if (xBufOff == xBuf.length) {
			processWord(xBuf, 0);
			xBufOff = 0;
		}

		byteCount++;
	}

	public void update(byte[] in, int inOff, int len) {
		//
		// fill the current word
		//
		while ((xBufOff != 0) && (len > 0)) {
			update(in[inOff]);

			inOff++;
			len--;
		}

		//
		// process whole words.
		//
		while (len > xBuf.length) {
			processWord(in, inOff);

			inOff += xBuf.length;
			len -= xBuf.length;
			byteCount += xBuf.length;
		}

		//
		// load in the remainder.
		//
		while (len > 0) {
			update(in[inOff]);

			inOff++;
			len--;
		}
	}

	public void finish() {
		long bitLength = (byteCount << 3);

		//
		// add the pad bytes.
		//
		update((byte) 128);

		while (xBufOff != 0) {
			update((byte) 0);
		}

		processLength(bitLength);

		processBlock();
	}

	public void reset() {
		byteCount = 0;

		xBufOff = 0;
		for (int i = 0; i < xBuf.length; i++) {
			xBuf[i] = 0;
		}
	}

	protected abstract void processWord(byte[] in, int inOff);

	protected abstract void processLength(long bitLength);

	protected abstract void processBlock();
}
