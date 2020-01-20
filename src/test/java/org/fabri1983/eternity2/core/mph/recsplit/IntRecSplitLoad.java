package org.fabri1983.eternity2.core.mph.recsplit;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;

public class IntRecSplitLoad {

	public static IntRecSplitEvaluator load() throws IOException, URISyntaxException {
		
		int avgBucketSize = 6954;
        int leafSize = 17;
        String fileName = "src/test/resources/super_matriz_recsplit.mphf";
        
        RandomAccessFile f = new RandomAccessFile(fileName, "r");
        byte[] data = new byte[(int) f.length()];
        f.readFully(data);
        f.close();
        
        IntRecSplitEvaluator eval = IntRecSplitBuilder.newInstance()
                .leafSize(leafSize)
                .averageBucketSize(avgBucketSize)
                .eliasFanoMonotoneLists(true)
                .buildEvaluator(new BitBuffer(data));
        
        return eval;
	}
}
