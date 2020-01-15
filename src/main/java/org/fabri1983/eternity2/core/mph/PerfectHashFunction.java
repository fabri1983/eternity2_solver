package org.fabri1983.eternity2.core.mph;

/**
 * This class generated manually from the output algorithm in phash.c produced by the 
 * Bob Jenkins' Minimal Perfect Hash function algorithm, taking as input the misc/super_matriz_decimal.txt file.
 * See README.md file on how to generate that function.
 * 
 * PHASHRANGE 8192, for the 6954 keys, which means super_matriz[] size must be 8192. 
 */
public class PerfectHashFunction {

	// PHASHLEN 0x400 = 1024
	private static short tab[] = { 7425, 2987, 6669, 322, 7733, 5355, 6669, 1905, 6159, 2534, 7585, 2572, 992, 3380,
			7084, 2798, 1516, 3950, 6967, 6383, 608, 6528, 1819, 4351, 2478, 359, 3204, 7795, 5389, 992, 131, 6557,
			7622, 5628, 7745, 4055, 6601, 7798, 5985, 5743, 7535, 3056, 1060, 5151, 3822, 6255, 684, 941, 844, 7646,
			4262, 1057, 4703, 2616, 7017, 5522, 3561, 3155, 0, 8056, 183, 6863, 414, 1810, 6299, 2918, 1533, 6849, 6576,
			5225, 3210, 4600, 1403, 8070, 183, 1533, 1444, 5913, 2981, 2903, 3120, 5311, 2517, 5871, 5779, 1446, 6636,
			4610, 0, 3812, 6322, 6799, 7425, 416, 6323, 3842, 1041, 4942, 5743, 522, 7452, 7414, 1403, 5325, 6651, 4055,
			1377, 7822, 6788, 5016, 4115, 3816, 7809, 7164, 1723, 7377, 1298, 2932, 3320, 7745, 3879, 3855, 5512, 608,
			2084, 6639, 2918, 3210, 131, 7622, 1810, 2518, 3824, 3879, 4491, 3210, 5345, 7622, 8070, 0, 8180, 2478,
			7575, 5743, 0, 6029, 322, 7414, 6675, 7966, 4600, 7672, 4781, 6299, 479, 1338, 1403, 4600, 3290, 12, 4383,
			4942, 7452, 5521, 3521, 2976, 6283, 4672, 6675, 2139, 4491, 4351, 1094, 1403, 2028, 1091, 5355, 859, 6110,
			2918, 6675, 4306, 6856, 237, 3959, 7571, 1591, 669, 3527, 3259, 7891, 3896, 5218, 7412, 7086, 4999, 7798,
			1334, 6210, 4274, 2844, 1766, 359, 4303, 5743, 3083, 5389, 3950, 6543, 5216, 3465, 6503, 3210, 223, 7323,
			3120, 1060, 7992, 4942, 8015, 5196, 2209, 3521, 3685, 706, 4672, 592, 479, 5764, 348, 2055, 6026, 3509,
			5562, 1607, 2119, 2918, 4813, 3969, 8190, 5963, 532, 1293, 4230, 4715, 4310, 2976, 2546, 2773, 922, 7437,
			3366, 5311, 6385, 3913, 7907, 6283, 1444, 2774, 7841, 4099, 7414, 8166, 7733, 6029, 6383, 3083, 4600, 3404,
			6576, 6849, 5355, 1338, 4491, 1403, 322, 5389, 5336, 3950, 131, 4999, 859, 6861, 322, 7609, 3761, 3584,
			5890, 4813, 7525, 4901, 6601, 359, 747, 5325, 1516, 2987, 4064, 2918, 8044, 2400, 6847, 3017, 6091, 6029,
			1905, 2653, 237, 2906, 218, 3822, 7857, 1377, 2139, 3561, 2794, 2820, 7323, 3921, 8056, 3822, 523, 6675,
			7907, 2139, 5345, 6255, 2775, 4986, 6999, 6322, 2478, 2932, 1533, 5978, 2844, 6870, 7134, 1524, 8070, 5345,
			4400, 3056, 3155, 2185, 522, 2987, 7452, 7733, 7555, 1231, 3361, 6029, 2269, 4220, 4564, 2400, 7646, 6576,
			5871, 3584, 4262, 6283, 1293, 6255, 6029, 359, 7634, 6460, 7525, 1679, 5389, 3950, 2987, 2932, 4600, 4974,
			8162, 7310, 3969, 3950, 1338, 5325, 190, 3150, 223, 414, 310, 3056, 6759, 1232, 6528, 6849, 508, 4666, 4099,
			3509, 846, 2976, 218, 2093, 896, 2600, 998, 5663, 7745, 7733, 3850, 3056, 1403, 7653, 5774, 5021, 282, 8070,
			642, 1094, 1896, 1819, 6788, 540, 1091, 522, 2870, 236, 2669, 414, 2903, 5743, 6029, 7745, 237, 578, 131,
			7460, 2320, 2209, 6323, 2199, 7891, 4813, 6383, 6322, 7017, 1810, 6613, 6837, 4064, 1232, 3963, 6918, 6601,
			4400, 5448, 6299, 3210, 5586, 6285, 183, 3366, 760, 5000, 896, 6861, 1194, 5769, 4927, 5220, 508, 2577,
			7077, 1905, 6917, 5628, 5562, 1533, 2191, 3304, 4099, 237, 1785, 5923, 2269, 6890, 6663, 209, 628, 5586,
			1146, 3680, 4672, 4901, 7822, 4211, 6759, 4858, 3155, 992, 4529, 2110, 2794, 5112, 2055, 5207, 3879, 7516,
			6799, 6675, 4491, 0, 5000, 6863, 2280, 3083, 1905, 5499, 2798, 1267, 4027, 1524, 3210, 1232, 1403, 1041,
			7733, 3017, 5862, 7585, 8166, 2679, 1622, 6543, 6048, 3913, 914, 4832, 7425, 3822, 3146, 5016, 3123, 3507,
			2478, 1968, 1858, 8070, 7452, 5628, 2794, 7086, 8056, 7414, 2987, 2209, 5586, 7482, 1232, 5978, 2918, 2820,
			3303, 7907, 1146, 578, 3027, 2478, 4986, 4491, 5018, 3579, 1538, 684, 5512, 2021, 4922, 3303, 5769, 7592,
			528, 5522, 522, 2987, 5499, 3969, 5930, 7174, 6184, 508, 2774, 6283, 5000, 8065, 7672, 7891, 785, 6510,
			1814, 1607, 7622, 5395, 8180, 4262, 5389, 859, 1538, 1857, 5663, 653, 4400, 131, 7059, 4566, 1041, 2820,
			330, 7622, 4462, 4262, 2844, 2666, 6026, 5890, 3050, 0, 6849, 4927, 3831, 5995, 2883, 4535, 1298, 4927, 639,
			6847, 4564, 1751, 565, 4491, 922, 0, 6110, 1905, 168, 2250, 6675, 2028, 8056, 3625, 4491, 322, 5345, 7425,
			3822, 3950, 4567, 2209, 1940, 6029, 330, 4331, 5047, 4266, 7077, 706, 5688, 7798, 7388, 1057, 4491, 0, 4751,
			4064, 4813, 7084, 4164, 1729, 12, 3842, 2403, 5905, 218, 4672, 3210, 5389, 157, 7992, 299, 3465, 6126, 2160,
			6523, 2653, 5220, 3975, 2359, 750, 532, 7084, 1451, 6651, 6503, 3465, 2773, 3669, 3452, 7452, 1814, 7891,
			46, 330, 1548, 5325, 1533, 5196, 4986, 1428, 747, 1338, 1403, 2794, 1236, 1897, 5866, 4462, 7516, 2918,
			5198, 4310, 419, 2903, 6383, 4871, 713, 3601, 1570, 7863, 4474, 8131, 6352, 2572, 2185, 6808, 1041, 7733,
			6321, 859, 5769, 3937, 6503, 2532, 2110, 4099, 4055, 528, 5019, 922, 4372, 8065, 2454, 7101, 4042, 3304,
			2018, 8128, 5156, 7525, 6008, 3452, 3290, 7241, 8093, 7609, 1766, 5628, 2235, 5688, 0, 1480, 3325, 8103,
			183, 5196, 2209, 3452, 6799, 2517, 5345, 7452, 5923, 4813, 3850, 5534, 131, 1480, 7907, 5157, 4474, 3579,
			7992, 806, 2209, 5930, 7508, 7155, 6759, 7134, 5952, 5544, 4486, 4672, 3680, 7798, 1105, 6967, 6184, 565,
			2918, 5345, 5743, 7733, 4303, 1403, 7425, 1858, 3587, 6322, 0, 4368, 7891, 2465, 7646, 896, 3141, 4077,
			4535, 1199, 5184, 6352, 2244, 7886, 4098, 5952, 2191, 578, 3451, 3879, 608, 5522, 4237, 2616, 6849, 1825,
			4303, 3584, 7425, 3056, 2295, 7560, 6861, 3816, 3625, 5921, 1570, 3295, 0, 6503, 1785, 4164, 3656, 2185,
			6669, 4512, 5917, 6689, 7733, 1307, 6831, 3909, 7622, 1723, 578, 669, 4020, 2864, 4303, 2918, 6576, 608,
			7745, 6921, 4535, 8070, 4115, 416, 5218, 3969, 0, 3879, 2653, 593, 5689, 3615, 2110, 4672, 6137, 6321, 5764,
			2857, 2400, 355, 2171, 0, 4143, 359, 1232, 3921, 2794, 433, 7722, 4211, 1210, 2139, 1533, 6029, 2478, 1810,
			1968, 5985, 3004, 192, 795, 6921, 1825, 131, 6430, 3921, 6195, 2021, 8065, 4630, 6109, 5521, 157, 3845, 876,
			2620, 3304, 12, 3083, 4266, 7634, 192, 12, 7414, 6849, 1905, 6601, 5512, 3123, 2258, 5389, 46, 1192, 1701,
			3699, 5499, 7560, 5278, 5743, 5452, 71, 3050, 7872, 522, 2820, 8044, 6601, 6843, 6283, 5866, 7907, 613,
			6528, 3686, 5562, 922, 4610, 4055, 6178, 237, 3210, 6383, 3584, 2976, 5127, 7084, 1334, 5628, 3304, 4474,
			6943, 7841, 4077, 5311, 1109, 3913, 2215, 3404, 16, 2139, 4530, 4870, 922, 6714, 1428, 6322, 4400, 4974,
			3822, 2987, 5512, 3155, 3879, 4262 };

	public static int hash(int val) {
		val += 0x6902a4cc; // PHASHSALT 0x6902a4cc = 1761780940
		val ^= (val >>> 16);
		val += (val << 8);
		val ^= (val >>> 4);
		int b = (val >>> 8) & 0x3ff; // 0x3ff = 1023 => & 0x3ff is the fastest way of doing % 0x400 (PHASHLEN 1024)
		int a = (val + (val << 1)) >>> 19;
		int rsl = (a ^ tab[b]);
		return rsl;
	}
	
}
