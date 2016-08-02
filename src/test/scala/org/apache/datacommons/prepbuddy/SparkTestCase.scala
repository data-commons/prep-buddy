package org.apache.datacommons.prepbuddy

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class SparkTestCase extends FunSuite with BeforeAndAfterEach {
    var sparkContext: SparkContext = null

    override def beforeEach() {
        val sparkConf: SparkConf = new SparkConf().setAppName(getClass.getName).setMaster("local[2]")
        Logger.getLogger("org").setLevel(Level.OFF)
        Logger.getLogger("akka").setLevel(Level.OFF)
        sparkContext = new SparkContext(sparkConf)
    }

    override def afterEach() {
        sparkContext.stop()
    }
}
