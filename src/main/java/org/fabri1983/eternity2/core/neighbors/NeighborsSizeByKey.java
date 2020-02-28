/**
 * Copyright (c) 2019 Fabricio Lettieri fabri1983@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fabri1983.eternity2.core.neighbors;

/**
 * This class contains the array size per key needed by {@link Neighbors}.
 * This way we have the exact array size and so we can avoid use/resize any temporal array or list.
 * Additionally, given the fact that the process of creation of arrays only involves known keys, we can skip 
 * those entries which have a value of 1 from the big switch case, and so return 1 when the key is missing. 
 * This decrease the size of methods.
 */
public class NeighborsSizeByKey {

	public static byte getSizeForKey_interior(int key) {
		switch (key) {
		case 0: return 2;
		case 1: return 4;
		case 2: return 3;
		case 3: return 2;
		case 4: return 2;
		case 5: return 3;
		case 8: return 3;
		case 9: return 3;
		case 10: return 4;
		case 11: return 2;
		case 12: return 5;
		case 13: return 2;
		case 14: return 2;
		case 15: return 4;
		case 16: return 2;
		case 32: return 2;
		case 33: return 5;
		case 34: return 3;
		case 35: return 2;
		case 36: return 3;
		case 37: return 2;
		case 38: return 3;
		case 40: return 2;
		case 41: return 3;
		case 42: return 5;
		case 43: return 3;
		case 44: return 4;
		case 45: return 3;
		case 46: return 2;
		case 47: return 3;
		case 48: return 3;
		case 64: return 2;
		case 65: return 3;
		case 67: return 2;
		case 68: return 3;
		case 69: return 3;
		case 71: return 4;
		case 72: return 5;
		case 73: return 4;
		case 74: return 2;
		case 75: return 3;
		case 76: return 4;
		case 77: return 5;
		case 80: return 3;
		case 98: return 2;
		case 99: return 2;
		case 101: return 3;
		case 102: return 4;
		case 103: return 2;
		case 106: return 4;
		case 107: return 5;
		case 108: return 6;
		case 109: return 2;
		case 110: return 4;
		case 111: return 4;
		case 128: return 3;
		case 129: return 4;
		case 130: return 5;
		case 131: return 3;
		case 133: return 2;
		case 135: return 4;
		case 136: return 2;
		case 137: return 4;
		case 140: return 3;
		case 142: return 5;
		case 143: return 2;
		case 144: return 5;
		case 160: return 6;
		case 162: return 3;
		case 163: return 2;
		case 165: return 3;
		case 166: return 2;
		case 167: return 5;
		case 168: return 4;
		case 173: return 6;
		case 174: return 4;
		case 176: return 3;
		case 193: return 2;
		case 194: return 2;
		case 195: return 2;
		case 197: return 3;
		case 198: return 5;
		case 199: return 3;
		case 200: return 3;
		case 202: return 6;
		case 203: return 5;
		case 205: return 3;
		case 206: return 2;
		case 207: return 2;
		case 208: return 3;
		case 224: return 4;
		case 225: return 2;
		case 226: return 2;
		case 227: return 2;
		case 228: return 5;
		case 229: return 3;
		case 230: return 4;
		case 231: return 4;
		case 232: return 2;
		case 233: return 2;
		case 234: return 3;
		case 238: return 2;
		case 239: return 5;
		case 240: return 4;
		case 256: return 4;
		case 257: return 3;
		case 258: return 4;
		case 259: return 2;
		case 260: return 4;
		case 261: return 7;
		case 262: return 2;
		case 263: return 3;
		case 265: return 4;
		case 266: return 5;
		case 267: return 2;
		case 269: return 2;
		case 271: return 3;
		case 289: return 4;
		case 291: return 3;
		case 292: return 5;
		case 293: return 5;
		case 294: return 2;
		case 295: return 3;
		case 296: return 3;
		case 297: return 2;
		case 299: return 2;
		case 300: return 2;
		case 301: return 4;
		case 302: return 4;
		case 303: return 2;
		case 304: return 3;
		case 321: return 2;
		case 322: return 3;
		case 323: return 3;
		case 324: return 2;
		case 325: return 2;
		case 327: return 2;
		case 328: return 3;
		case 329: return 4;
		case 330: return 5;
		case 331: return 2;
		case 333: return 4;
		case 334: return 3;
		case 335: return 4;
		case 336: return 3;
		case 353: return 3;
		case 354: return 3;
		case 355: return 4;
		case 356: return 7;
		case 358: return 5;
		case 359: return 3;
		case 360: return 3;
		case 361: return 3;
		case 364: return 2;
		case 366: return 5;
		case 367: return 3;
		case 368: return 4;
		case 384: return 6;
		case 385: return 3;
		case 386: return 2;
		case 387: return 3;
		case 388: return 3;
		case 391: return 3;
		case 392: return 6;
		case 393: return 2;
		case 395: return 3;
		case 396: return 3;
		case 397: return 2;
		case 398: return 2;
		case 399: return 3;
		case 400: return 2;
		case 416: return 2;
		case 418: return 4;
		case 419: return 4;
		case 420: return 2;
		case 421: return 3;
		case 423: return 4;
		case 425: return 3;
		case 426: return 3;
		case 427: return 4;
		case 428: return 3;
		case 429: return 4;
		case 430: return 5;
		case 431: return 2;
		case 448: return 3;
		case 449: return 4;
		case 451: return 3;
		case 452: return 3;
		case 454: return 3;
		case 455: return 4;
		case 456: return 2;
		case 457: return 4;
		case 458: return 3;
		case 459: return 5;
		case 460: return 4;
		case 461: return 3;
		case 462: return 3;
		case 464: return 3;
		case 480: return 2;
		case 481: return 2;
		case 482: return 3;
		case 483: return 3;
		case 484: return 4;
		case 485: return 2;
		case 486: return 2;
		case 488: return 3;
		case 489: return 5;
		case 491: return 4;
		case 493: return 4;
		case 495: return 4;
		case 496: return 7;
		case 512: return 4;
		case 513: return 5;
		case 514: return 5;
		case 516: return 2;
		case 517: return 2;
		case 518: return 5;
		case 519: return 2;
		case 520: return 4;
		case 523: return 3;
		case 524: return 5;
		case 527: return 5;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}

	public static byte getSizeForKey_interior_above_central(int key) {
		switch (key) {
		case 99: return 2;
		case 130: return 2;
		case 142: return 2;
		case 228: return 2;
		case 385: return 2;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
	public static byte getSizeForKey_interior_left_central(int key) {
		switch (key) {
		case 140: return 2;
		case 228: return 2;
		case 488: return 2;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
	public static byte getSizeForKey_border_right(int key) {
		switch (key) {
		case 3: return 2;
		case 6: return 2;
		case 34: return 2;
		case 36: return 2;
		case 37: return 3;
		case 70: return 2;
		case 74: return 4;
		case 107: return 2;
		case 140: return 2;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
	public static byte getSizeForKey_border_left(int key) {
		switch (key) {
		case 0: return 10;
		case 1: return 12;
		case 2: return 11;
		case 3: return 12;
		case 4: return 11;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
	public static byte getSizeForKey_border_top(int key) {
		switch (key) {
		case 0: return 12;
		case 1: return 11;
		case 2: return 11;
		case 3: return 12;
		case 4: return 10;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
	public static byte getSizeForKey_border_bottom(int key) {
		switch (key) {
		case 4: return 2;
		case 67: return 2;
		case 96: return 2;
		case 160: return 2;
		case 164: return 2;
		case 195: return 2;
		case 258: return 2;
		case 321: return 3;
		case 355: return 2;
		case 417: return 2;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
	public static byte getSizeForKey_corner_top_left() {
		// There are always only 4 tiles in top left corner when doing scan row from top-left to bottom-right
		return 4;
	}
	
	public static byte getSizeForKey_corner_top_right(int key) {
		switch (key) {
		case 4: return 2;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}

	public static byte getSizeForKey_corner_bottom_left(int key) {
		switch (key) {
		case 0: return 2;
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
	public static byte getSizeForKey_corner_bottom_right(int key) {
		switch (key) {
		// All keys have exactly one tile
		// Default size is 1 for missing keys.
		default: return 1;
		}
	}
	
}
