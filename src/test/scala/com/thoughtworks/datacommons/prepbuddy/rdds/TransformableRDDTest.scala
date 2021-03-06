package com.thoughtworks.datacommons.prepbuddy.rdds

import com.thoughtworks.datacommons.prepbuddy.SparkTestCase
import com.thoughtworks.datacommons.prepbuddy.clusterers.{Cluster, SimpleFingerprintAlgorithm, TextFacets}
import com.thoughtworks.datacommons.prepbuddy.qualityanalyzers.DECIMAL
import com.thoughtworks.datacommons.prepbuddy.types.{CSV, TSV}
import com.thoughtworks.datacommons.prepbuddy.utils.RowRecord
import org.apache.spark.rdd.RDD


class TransformableRDDTest extends SparkTestCase {

    test("textfacets highest should give one highest pair if only one pair found") {
        val data = Array("1,23", "2,45", "3,65", "4,67", "5,23")
        val dataSet: RDD[String] = sparkContext.parallelize(data)
        val transformableRDD: TransformableRDD = new TransformableRDD(dataSet, CSV)
        assert(5 == transformableRDD.count())
    }

    test("should drop the specified column from the given rdd") {
        val data = Array(
            "John,Male,21,Canada",
            "Smith, Male, 30, UK",
            "Larry, Male, 23, USA",
            "Fiona, Female,18,USA"
        )
        val dataset: RDD[String] = sparkContext.parallelize(data)
        val transformableRDD: TransformableRDD = new TransformableRDD(dataset, CSV)
        val transformedRows: Array[String] = transformableRDD.drop(2).collect()

        assert(transformedRows.contains("John,Male,Canada"))
        assert(transformedRows.contains("Smith,Male,UK"))
        assert(transformedRows.contains("Larry,Male,USA"))
        assert(transformedRows.contains("Fiona,Female,USA"))
    }

    test("should be able to drop more than one specified column from the given rdd") {
        val data = Array(
            "John,Male,21,Canada",
            "Smith, Male, 30, UK",
            "Larry, Male, 23, USA",
            "Fiona, Female,18,USA"
        )
        val dataset: RDD[String] = sparkContext.parallelize(data)
        val transformableRDD: TransformableRDD = new TransformableRDD(dataset, CSV)
        val transformedRows: Array[String] = transformableRDD.drop(2, 3).collect()

        assert(transformedRows.contains("John,Male"))
        assert(transformedRows.contains("Smith,Male"))
        assert(transformedRows.contains("Larry,Male"))
        assert(transformedRows.contains("Fiona,Female"))
    }

    test("toDoubleRdd should give double RDD of given column index") {
        val data = Array("1,23", "2,45", "3,65", "4,67", "5,23")
        val dataSet: RDD[String] = sparkContext.parallelize(data)
        val transformableRDD: TransformableRDD = new TransformableRDD(dataSet, CSV)
        val doubleRdd: RDD[Double] = transformableRDD.toDoubleRDD(0)
        val collected: Array[Double] = doubleRdd.collect()
        val expected: Double = 3

        assert(collected.contains(expected))
        assert(collected.contains(1))
        assert(collected.contains(2))
        assert(collected.contains(4))
        assert(collected.contains(5))
    }

    test("text facet should give count of Pair") {
        val dataSet = Array("X,Y", "A,B", "X,Z", "A,Q", "A,E")
        val initialRDD: RDD[String] = sparkContext.parallelize(dataSet)
        val transformableRDD: TransformableRDD = new TransformableRDD(initialRDD)
        val textFacets: TextFacets = transformableRDD.listFacets(0)
        assert(2 == textFacets.count)
    }

    test("should remove rows are based on a predicate") {
        val dataSet = Array("A,1", "B,2", "C,3", "D,4", "E,5")
        val initialRDD: RDD[String] = sparkContext.parallelize(dataSet)
        val transformableRDD: TransformableRDD = new TransformableRDD(initialRDD)
        val predicate = (record: RowRecord) => {
            val valueAt: String = record(0)
            valueAt.equals("A") || valueAt.equals("B")
        }
        val finalRDD: TransformableRDD = transformableRDD.removeRows(predicate)
        assert(3 == finalRDD.count)
    }

    test("toDoubleRDD should give rdd of double") {
        val dataSet = Array("A,1.0", "B,2.9", "C,3", "D,4", "E,w")
        val initialRDD: RDD[String] = sparkContext.parallelize(dataSet)
        val transformableRDD: TransformableRDD = new TransformableRDD(initialRDD)
        val doubleRDD: RDD[Double] = transformableRDD.toDoubleRDD(1)
        val collected: Array[Double] = doubleRDD.collect()
        assert(collected.contains(1.0))
        assert(collected.contains(2.9))
        assert(collected.contains(3.0))
        assert(collected.contains(4.0))
    }

    test("select should give selected column of the RDD") {
        val dataSet = Array("A,1.0", "B,2.9", "C,3", "D,4", "E,0")
        val initialRDD: RDD[String] = sparkContext.parallelize(dataSet)

        val transformableRDD: TransformableRDD = new TransformableRDD(initialRDD)
        val selectedColumn: RDD[String] = transformableRDD.select(1)

        assert(selectedColumn.collect sameElements Array("1.0", "2.9", "3", "4", "0"))
    }

    test("select should give multiple selected column of the RDD") {
        val dataSet = Array("A,1.0,Male", "B,2.9,Female", "C,3,Male", "D,4,Male", "E,0,Female")
        val initialRDD: RDD[String] = sparkContext.parallelize(dataSet)

        val transformableRDD: TransformableRDD = new TransformableRDD(initialRDD)
        val selectedColumns: TransformableRDD = transformableRDD.select(List(0, 2))

        assert(selectedColumns.collect sameElements Array("A,Male", "B,Female", "C,Male", "D,Male", "E,Female"))
    }

    test("listFacets should give facets of given column indexes") {
        val initialDataset: RDD[String] = sparkContext.parallelize(Array("A,B,C", "D,E,F", "G,H,I"))
        val initialRDD: TransformableRDD = new TransformableRDD(initialDataset)
        val listFacets: TextFacets = initialRDD.listFacets(1 :: 2 :: Nil)
        val listOfHighest: Array[(String, Int)] = listFacets.highest

        assert(3 equals listOfHighest.length)
    }

    test("should return a double rdd by multiplying the given column indexes") {
        val initialDataset: RDD[String] = sparkContext.parallelize(Array("1,2", "1, 3", "1,4", "1, X"))

        val initialRDD: TransformableRDD = new TransformableRDD(initialDataset)
        val doubleRdd: RDD[Double] = initialRDD.multiplyColumns(0, 1)
        val collected: Array[Double] = doubleRdd.collect()

        assert(3 equals collected.length)
        assert(collected.contains(2.0))
        assert(collected.contains(3.0))
        assert(collected.contains(4.0))
    }

    test("should return the number of columns in the record") {
        val data = Array(
            "Smith,Male,USA,12345",
            "John,Male,12343",
            "John,Male,India,12343",
            "Smith,Male,USA,12342"
        )
        val initialDataset: RDD[String] = sparkContext.parallelize(data)
        val initialRDD: TransformableRDD = new TransformableRDD(initialDataset)

        assert(4 equals initialRDD.numberOfColumns())
    }

    test("should return number of columns as zero when the rdd is empty") {
        val data = Array.empty[String]
        val initialDataset: RDD[String] = sparkContext.parallelize(data)
        val initialRDD: TransformableRDD = new TransformableRDD(initialDataset)

        assert(0 equals initialRDD.numberOfColumns())
    }

    test("should return the type of a column") {
        val data = Array("1,23.4", "2,45.1", "3,65.56", "4,67.12", "5,23.1")
        val dataSet: RDD[String] = sparkContext.parallelize(data)
        val transformableRDD: TransformableRDD = new TransformableRDD(dataSet)

        assert(DECIMAL equals transformableRDD.inferType(1))
    }

    test("should mark by given symbol to predicated row") {
        val data = Array(
            "Smith,Male,USA,12345",
            "John,Male,,12343",
            "Meeka,Female,India,12343",
            "Smith,Male,USA,12342"
        )
        val initialDataset: RDD[String] = sparkContext.parallelize(data)
        val initialRDD: TransformableRDD = new TransformableRDD(initialDataset)

        val flagged: TransformableRDD = initialRDD.flag("*", (rowRecord: RowRecord) => {
            rowRecord(1).equals("Female")
        })
        assert(5 == flagged.numberOfColumns())

        val collected: Array[String] = flagged.collect()

        assert(collected.contains("Meeka,Female,India,12343,*"))
        assert(collected.contains("Smith,Male,USA,12342,"))

    }

    test("should map on only flagged row") {
        val data = Array(
            "Smith,Male,USA,12345",
            "John,Male,,12343",
            "Meeka,Female,India,12343",
            "Smith,Male,USA,12342"
        )
        val initialDataset: RDD[String] = sparkContext.parallelize(data)
        val initialRDD: TransformableRDD = new TransformableRDD(initialDataset)

        val flagged: TransformableRDD = initialRDD.flag("*", (rowRecord: RowRecord) => {
            rowRecord(1).equals("Female")
        })
        assert(5 == flagged.numberOfColumns())

        val afterFlagMapRDD: TransformableRDD = flagged.mapByFlag("*", 4, (row: String) => "Flagged," + row)
        val collected: Array[String] = afterFlagMapRDD.collect()

        assert(collected.contains("Flagged,Meeka,Female,India,12343,*"))
        assert(collected.contains("Smith,Male,USA,12342,"))

    }

    test("should replace cluster's values with new value") {
        val data = Array(
            "one two, three",
            "two one, four"
        )
        val initialDataset: RDD[String] = sparkContext.parallelize(data)
        val initialRDD: TransformableRDD = new TransformableRDD(initialDataset)
        val listOfClusters: List[Cluster] = initialRDD.clusters(0,
            new SimpleFingerprintAlgorithm()).getClustersWithSizeGreaterThan(0)
        val cluster: Cluster = listOfClusters.head

        val replacedRDD: TransformableRDD = initialRDD.replaceValues(cluster, "One", 0)
        val collected: Array[String] = replacedRDD.collect()

        assert(collected.contains("One,four"))
        assert(collected.contains("One,three"))
    }

    test("shouldMergeAllTheColumnsOfGivenTransformableRDDToTheCurrentTransformableRDD") {
        val initialSpelledNumbers: RDD[String] = sparkContext.parallelize(Array(
            "One,Two,Three",
            "Four,Five,Six",
            "Seven,Eight,Nine",
            "Ten,Eleven,Twelve"
        ))
        val spelledNumbers: TransformableRDD = new TransformableRDD(initialSpelledNumbers)
        val initialNumericData: RDD[String] = sparkContext.parallelize(Array(
            "1\t2\t3",
            "4\t5\t6",
            "7\t8\t9",
            "10\t11\t12"
        ))
        val numericData: TransformableRDD = new TransformableRDD(initialNumericData, TSV)

        val result: Array[String] = spelledNumbers.addColumnsFrom(numericData).collect()

        assert(result.contains("One,Two,Three,1,2,3"))
        assert(result.contains("Four,Five,Six,4,5,6"))
        assert(result.contains("Seven,Eight,Nine,7,8,9"))
        assert(result.contains("Ten,Eleven,Twelve,10,11,12"))
    }

    test("should remove the outliers from a dataset") {
        val dataSet: RDD[String] = sparkContext.parallelize(Array(
            "10.2", "14.1", "14.4", "14.4",
            "14.4", "14.5", "14.5", "14.6",
            "14.7", "14.7", "14.7", "14.9",
            "15.1", "15.9", "16.4"
        ))
        val transformableRDD: TransformableRDD = new TransformableRDD(dataSet)
        val withoutOutliers: TransformableRDD = transformableRDD.removeOutliers(0)

        val actual: Array[String] = withoutOutliers.collect()
        val expected = Array(
            "14.1", "14.4", "14.4", "14.4",
            "14.5", "14.5", "14.6", "14.7",
            "14.7", "14.7", "14.9", "15.1"
        )
        assert(expected.sameElements(actual))
    }

    test("should be able to select columns by column name") {
        val dataSet: RDD[String] = sparkContext.parallelize(Array(
            "2, 1, 4, 4",
            "4, 5, 5, 6",
            "7, 7, 7, 9",
            "1, 9, 4, 9"
        ))
        val schema: Map[String, Int] = Map("First" -> 0, "Second" -> 1, "Third" -> 2, "Fourth" -> 3)

        val transformableRDD: TransformableRDD = new TransformableRDD(dataSet).useSchema(schema)
        val secondColumnValues: RDD[String] = transformableRDD.select("Second")

        val actual: Array[String] = secondColumnValues.collect()
        val expected = Array("1", "5", "7", "9")
        assertResult(expected)(actual)
    }

    test("when column is selected by name, new RDD should have column names by default from previous rdd") {
        val dataSet: RDD[String] = sparkContext.parallelize(Array(
            "2, 1, 4, 4",
            "4, 5, 5, 6",
            "7, 7, 7, 9",
            "1, 9, 4, 9"
        ))
        val schema: Map[String, Int] = Map("First" -> 0, "Second" -> 1, "Third" -> 2, "Fourth" -> 3)

        val transformableRDD: TransformableRDD = new TransformableRDD(dataSet).useSchema(schema)
        val secondThirdColumnValues: TransformableRDD = transformableRDD.select(List("Second", "Third"))

        val actual: Array[String] = secondThirdColumnValues.select("Second").collect()
        val expected = Array("1", "5", "7", "9")
        assertResult(expected)(actual)
    }
}
