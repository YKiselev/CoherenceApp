package org.uze.coherence.jdbc;

import com.google.common.base.Preconditions;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Uze on 20.08.2014.
 */
public class InClauseMipMappingTest {

    @Test
    public void test() throws Exception {
        final Random rnd = new Random();

        for (int i = 1; i < 20; i++) {
            final int batchSize = 1000;//1 + rnd.nextInt(1000);
            final int minBatch = i;//1 + rnd.nextInt(32);
            final List<Integer> sizes = generateMipLevels(batchSize, minBatch);

            System.out.println("batchSize:" + batchSize + ", minBatch: " + minBatch + ", sizes: " + sizes);
            int min = 1000;
            int max = 0;
            double avg = 0;

            int total = 0;

            for (int j = 1; j < 10000; j += 1) {
                final int steps = approximate(j, sizes);
                total += steps;
                //System.out.println(j + "," + steps);

                if (steps < min) {
                    min = steps;
                }
                if (steps > max) {
                    max = steps;
                }
                if (avg == 0) {
                    avg = steps;
                } else {
                    avg = (avg + steps) * 0.5;
                }
            }

            System.out.println("Total steps: " + total + ", min: " + min + ", avg: " + avg + ", max: " + max);
        }
    }

    private int approximate(int dataLength, List<Integer> sizes) {
        if (dataLength < 1) {
            return 0;
        }

        Preconditions.checkNotNull(sizes);
        Preconditions.checkArgument(!sizes.isEmpty());

        int left = dataLength;
        int steps = 0;

        while (left > 0) {
            final int chunkSize = getChunkSize(left, sizes);
            left -= chunkSize;
            steps++;
        }

        return steps;
    }

    private int getChunkSize(int left, List<Integer> sizes) {
        Preconditions.checkArgument(left > 0);

        for (int size : sizes) {
            if (left >= size) {
                return size;
            }
        }

        throw new IllegalStateException("Chunk size not found! Left: " + left + ", sizes: " + sizes);
    }

    private List<Integer> generateMipLevels(int batchSize, int minBatch) {
        Preconditions.checkArgument(minBatch >= 1);
        Preconditions.checkArgument(batchSize >= 1);

        final List<Integer> result = new ArrayList<>();

        int current = batchSize;
        while (current >= minBatch) {
            result.add(current);

            int next = current / 2 - 1;
//            if ((current & 1) == 0){
//                next--;
//            }
            int c = 0;
            while (getGcd(current, next) > 1) {
                next--;
                c++;
            }
            if (c > 0) {
                System.out.println("C = " + c);
            }
            current = next;
        }

        if (minBatch > 1) {
            result.add(1);
        }

        return result;
    }

    @Test
    public void testGcd() throws Exception {
        Assert.assertEquals(1, getGcd(6, 23));
        Assert.assertEquals(1, getGcd(23, 6));

        Assert.assertEquals(1, getGcd(7, 13));
        Assert.assertEquals(1, getGcd(13, 7));

        Assert.assertEquals(1, getGcd(9, 10));
        Assert.assertEquals(1, getGcd(10, 9));

        Assert.assertEquals(1, getGcd(114, 113));
        Assert.assertEquals(1, getGcd(113, 114));

        Assert.assertEquals(1, getGcd(117, 116));

        Assert.assertFalse(1 == getGcd(6, 12));
    }

    private int getGcd(int a, int b) {
        int result = 1;
        int scale = 1;
        while (true) {
            if (a == 0) {
                result = b;
                break;
            }
            if (b == 0) {
                result = a;
                break;
            }
            if (a == b) {
                result = a;
                break;
            }
            if (a == 1) {
                result = 1;
                break;
            }
            if (b == 1) {
                result = 1;
                break;
            }
            if ((a & 1) == 0) {
                if ((b & 1) == 0) {
                    scale *= 2;
                    a = a / 2;
                    b = b / 2;
                } else {
                    a = a / 2;
                }
            } else {
                if ((b & 1) == 0) {
                    b = b / 2;
                } else {
                    if (a > b) {
                        a = (a - b) / 2;
                    } else if (b > a) {
                        int tmp = a;
                        a = (b - a) / 2;
                        b = tmp;
                    }
                }
            }
        }
        return scale * result;
    }
}
