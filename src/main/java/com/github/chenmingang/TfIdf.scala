package com.github.chenmingang

import com.github.chenmingang.util.ConfigUtil
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.{SparseVector, Vector}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TfIdf {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("tf-idf")
      .setMaster(ConfigUtil.getProperty("masterUrl"))

    val sc = new SparkContext(conf)

    //    val documents = sc.parallelize(List("1 2 4", "2 3 4")).map(i => {
    //      i.split(" ").toSeq
    //    })
    val documents = sc.textFile("src/main/resources/西游记.txt").map(i => {
      i.split("").toSeq
    })


    val hashingTF = new HashingTF()

    val mapWords = documents.flatMap(x => x)
      .map(w => (hashingTF.indexOf(w), w))
      .collect.toMap

    val tf: RDD[Vector] = hashingTF.transform(documents)

    val idf = new IDF().fit(tf)

    val tfidf: RDD[Vector] = idf.transform(tf)

    val bcWords = tf.context.broadcast(mapWords)

    val r = tfidf.map {
      case SparseVector(size, indices, values) =>
        val words = indices.map(index => bcWords.value.getOrElse(index, "null"))
        words.zip(values).toSeq
    }

    //模型
    tfidf.foreach(i => {
      println(i)
    })

    //词  tfidf值
    r.foreach(i => {
      println(i)
    })

  }
}
