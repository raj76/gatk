package org.broadinstitute.sting.utils;

import org.broadinstitute.sting.BaseTest;
import org.broadinstitute.sting.utils.collections.Pair;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: Ghost
 * Date: 3/5/11
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MWUnitTest extends BaseTest {
    @BeforeClass
    public void init() { }

    @Test
    private void testMWU() {
        logger.warn("Testing MWU");
        MannWhitneyU mwu = new MannWhitneyU();
        mwu.add(0, MannWhitneyU.USet.SET1);
        mwu.add(1,MannWhitneyU.USet.SET2);
        mwu.add(2,MannWhitneyU.USet.SET2);
        mwu.add(3,MannWhitneyU.USet.SET2);
        mwu.add(4,MannWhitneyU.USet.SET2);
        mwu.add(5,MannWhitneyU.USet.SET2);
        mwu.add(6,MannWhitneyU.USet.SET1);
        mwu.add(7,MannWhitneyU.USet.SET1);
        mwu.add(8,MannWhitneyU.USet.SET1);
        mwu.add(9,MannWhitneyU.USet.SET1);
        mwu.add(10,MannWhitneyU.USet.SET1);
        mwu.add(11,MannWhitneyU.USet.SET2);
        Assert.assertEquals(MannWhitneyU.calculateOneSidedU(mwu.getObservations(), MannWhitneyU.USet.SET1),25l);
        Assert.assertEquals(MannWhitneyU.calculateOneSidedU(mwu.getObservations(),MannWhitneyU.USet.SET2),11l);

        MannWhitneyU mwu2 = new MannWhitneyU();
        for ( int dp : new int[]{2,4,5,6,8} ) {
            mwu2.add(dp,MannWhitneyU.USet.SET1);
        }

        for ( int dp : new int[]{1,3,7,9,10,11,12,13} ) {
            mwu2.add(dp,MannWhitneyU.USet.SET2);
        }

        // tests using the hypothesis that set 2 dominates set 1 (U value = 10)
        Assert.assertEquals(MannWhitneyU.calculateOneSidedU(mwu2.getObservations(),MannWhitneyU.USet.SET1),10l);
        Assert.assertEquals(MannWhitneyU.calculateOneSidedU(mwu2.getObservations(),MannWhitneyU.USet.SET2),30l);
        Pair<Integer,Integer> sizes = mwu2.getSetSizes();
        Assert.assertEquals(MannWhitneyU.calculatePUniformApproximation(sizes.first,sizes.second,10l),0.4180519701814064,1e-14);
        Assert.assertEquals(MannWhitneyU.calculatePRecursively(sizes.first,sizes.second,10l,false).second,0.021756021756021756,1e-14);
        Assert.assertEquals(MannWhitneyU.calculatePNormalApproximation(sizes.first,sizes.second,10l,false).second,0.06214143703127617,1e-14);
        logger.warn("Testing two-sided");
        Assert.assertEquals((double)mwu2.runTwoSidedTest().second,2*0.021756021756021756,1e-8);

        // tests using the hypothesis that set 1 dominates set 2 (U value = 30) -- empirical should be identical, normall approx close, uniform way off
        Assert.assertEquals(MannWhitneyU.calculatePNormalApproximation(sizes.second,sizes.first,30l,true).second,2.0*0.08216463976903321,1e-14);
        Assert.assertEquals(MannWhitneyU.calculatePUniformApproximation(sizes.second,sizes.first,30l),0.0023473625009328147,1e-14);
        Assert.assertEquals(MannWhitneyU.calculatePRecursively(sizes.second,sizes.first,30l,false).second,0.021756021756021756,1e-14); // note -- exactly same value as above

        logger.warn("Set 1");
        Assert.assertEquals((double)mwu2.runOneSidedTest(MannWhitneyU.USet.SET1).second,0.021756021756021756,1e-8);
        logger.warn("Set 2");
        Assert.assertEquals((double)mwu2.runOneSidedTest(MannWhitneyU.USet.SET2).second,0.021756021756021756,1e-8);

        MannWhitneyU mwu3 = new MannWhitneyU();
        for ( int dp : new int[]{0,2,4} ) {
            mwu3.add(dp,MannWhitneyU.USet.SET1);
        }
        for ( int dp : new int[]{1,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34} ) {
            mwu3.add(dp,MannWhitneyU.USet.SET2);
        }
        long u = MannWhitneyU.calculateOneSidedU(mwu3.getObservations(),MannWhitneyU.USet.SET1);
        Pair<Integer,Integer> nums = mwu3.getSetSizes();
        Assert.assertEquals(MannWhitneyU.calculatePRecursivelyDoNotCheckValuesEvenThoughItIsSlow(nums.first,nums.second,u),3.665689149560116E-4,1e-14);
        Assert.assertEquals(MannWhitneyU.calculatePNormalApproximation(nums.first,nums.second,u,false).second,0.0032240865760884696,1e-14);
        Assert.assertEquals(MannWhitneyU.calculatePUniformApproximation(nums.first,nums.second,u),0.0026195003025784036,1e-14);

    }
}