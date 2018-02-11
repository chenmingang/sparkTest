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

  def sim(a: String, b: String): Unit = {

  }


  def strEditSim(str1: String, str2: String): Double = {
    val ldNum: Int = ld(str1, str2)
    val max: Int = Math.max(str1.length, str2.length)
    1 - ldNum.toDouble / max.toDouble
  }

  def min(one: Int, two: Int, three: Int): Int = {
    var min: Int = one
    if (two < min) {
      min = two
    }
    if (three < min) {
      min = three
    }
    min
  }

  def ld(str1: String, str2: String): Int = {
    val n: Int = str1.length
    val m: Int = str2.length
    var i: Int = 0
    var j: Int = 0
    var ch1: Char = 0
    var ch2: Char = 0
    var temp: Int = 0
    if (n == 0) {
      return m
    }
    if (m == 0) {
      return n
    }
    var d = Array.ofDim[Int](n + 1, m + 1)
    i = 0
    while (i <= n) {
      d(i)(0) = i
      i += 1
    }
    j = 0
    while (j <= m) {
      d(0)(j) = j
      j += 1
    }
    i = 1
    while (i <= n) {
      ch1 = str1.charAt(i - 1)
      j = 1
      while (j <= m) {
        ch2 = str2.charAt(j - 1)
        if (ch1 == ch2) {
          temp = 0
        } else {
          temp = 1
        }
        d(i)(j) = min(d(i - 1)(j) + 1, d(i)(j - 1) + 1, d(i - 1)(j - 1) + temp)
        j += 1
      }
      i += 1
    }
    d(n)(m)
  }
}
