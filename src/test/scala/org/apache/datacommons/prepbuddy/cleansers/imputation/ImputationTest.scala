package org.apache.datacommons.prepbuddy.cleansers.imputation

import org.apache.datacommons.prepbuddy.SparkTestCase
import org.apache.datacommons.prepbuddy.rdds.TransformableRDD
import org.apache.datacommons.prepbuddy.types.CSV
import org.apache.datacommons.prepbuddy.utils.RowRecord
import org.apache.spark.rdd.RDD

class ImputationTest extends SparkTestCase {

    test("should impute value with returned value of strategy") {
        val data = Array("1,", "2,45", "3,65", "4,67", "5,23")
        val dataSet: RDD[String] = sparkContext.parallelize(data)
        val transformableRDD: TransformableRDD = new TransformableRDD(dataSet, CSV)
        val imputed: TransformableRDD = transformableRDD.impute(1, new ImputationStrategy {
            override def handleMissingData(record: RowRecord): String = "hello"

            override def prepareSubstitute(rdd: TransformableRDD, missingDataColumn: Int): Unit = {}
        })
        val expected: String = "1,hello"
        val collected: Array[String] = imputed.collect()
        assert(collected.contains(expected))
    }
}
