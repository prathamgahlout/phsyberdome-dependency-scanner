

package com.phsyberdome.licensedetector;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pratham Gahlout
 */


public class WeightedMinHash {
    private int numHashFunctions;
    private List<HashFunction> hashFunctions;

    public WeightedMinHash(int numHashFunctions) {
        this.numHashFunctions = numHashFunctions;
        hashFunctions = new ArrayList<>();

        // Initialize hash functions
        for (int i = 0; i < numHashFunctions; i++) {
            hashFunctions.add(Hashing.murmur3_128(i));
        }
    }

    private long hashElement(WeightedElement element, HashFunction hashFunction) {
        String elementString = element.id + "_" + element.weight;
        return hashFunction.newHasher()
                .putString(elementString, StandardCharsets.UTF_8)
                .hash()
                .asLong();
    }

    public long[] computeSignature(List<WeightedElement> weightedSet) {
        long[] signature = new long[numHashFunctions];

        for (int i = 0; i < numHashFunctions; i++) {
            long minHash = Long.MAX_VALUE;
            for (WeightedElement element : weightedSet) {
                long hashValue = hashElement(element, hashFunctions.get(i));
                minHash = Math.min(minHash, hashValue);
            }
            signature[i] = minHash;
        }

        return signature;
    }

    public double estimateJaccardSimilarity(long[] signature1, long[] signature2) {
        int matchingHashes = 0;
        for (int i = 0; i < numHashFunctions; i++) {
            if (signature1[i] == signature2[i]) {
                matchingHashes++;
            }
        }

        return (double) matchingHashes / numHashFunctions;
    }
}
