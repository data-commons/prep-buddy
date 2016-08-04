package com.thoughtworks.datacommons.prepbuddy.normalizers

import com.thoughtworks.datacommons.prepbuddy.SparkTestCase
import com.thoughtworks.datacommons.prepbuddy.rdds.TransformableRDD
import org.apache.spark.rdd.RDD

class DecimalScalingNormalizerTest extends SparkTestCase {
    test("Should be able to normalize the data set with DecimalScaling Normalization") {
        val dataSet = Array("07434677419,07371326239,Incoming,211,Wed Sep 15 19:17:44 +0100 2010",
            "07641036117,01666472054,Outgoing,0,Mon Feb 11 07:18:23 +0000 1980",
            "07641036117,07371326239,Incoming,45,Mon Feb 11 07:45:42 +0000 1980",
            "07641036117,07371326239,Incoming,45,Mon Feb 11 07:45:42 +0000 1980",
            "07641036117,07681546436,Missed,12,Mon Feb 11 08:04:42 +0000 1980")


        val initialRDD: RDD[String] = sparkContext.parallelize(dataSet)
        val transformableRDD: TransformableRDD = new TransformableRDD(initialRDD)

        val finalRDD: TransformableRDD = transformableRDD.normalize(3, new DecimalScalingNormalizer)
        val normalizedDurations = finalRDD.select(3).collect
        val expected = Array("2.11", "0.0", "0.45", "0.45", "0.12")

        assert(expected sameElements normalizedDurations)

    }
}
