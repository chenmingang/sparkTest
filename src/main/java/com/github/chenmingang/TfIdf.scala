package com.github.chenmingang

import java.util.regex.{Matcher, Pattern}

import com.github.chenmingang.util.{ConfigUtil, Fenci, WordTFIDFScore}
import org.apache.spark.mllib.feature.{HashingTF, IDF, IDFModel}
import org.apache.spark.mllib.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TfIdf {
  def main(args: Array[String]): Unit = {
//    trainWordScore
        println(sim("java开发工程师", "ios开发工程师"))
        println(sim("java开发工程师", "java"))

  }

  def sim(a: String, b: String): Double = {
    if (a == null && b == null) {
      return 1
    }
    if (a == null || b == null) {
      return 0
    }
    if (a.equals(b)) {
      return 1
    }
    var aArr = Fenci.fenciArr(a)
    var bArr = Fenci.fenciArr(b)
    val sameWord = aArr.intersect(bArr)
    var r: Double = 0d
    sameWord.foreach(word => {
      r += WordTFIDFScore.getScore(word)
    })
    //    r /= (aArr.length + bArr.length)
    r /= sameWord.length

    r += strEditSim(a, b) / 2
    r
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

  val p: Pattern = Pattern.compile("<[^>]+>|[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|-|nbsp|\n|\r| |　|」|「|》|《", Pattern.CASE_INSENSITIVE)

  def replaceSprical(str: String): String = {
    var dest: String = ""
    if (str != null) {
      val m: Matcher = p.matcher(str)
      dest = m.replaceAll("")
    }
    dest
  }

  def trainWordScore = {
    val conf = new SparkConf()
      .setAppName("tf-idf")
      .setMaster(ConfigUtil.getProperty("masterUrl"))
      //      .set("num-executors", "1")
      //      .set("spark.executor.cores", "1")
      .set("spark.default.parallelism", "1")


    val sc = new SparkContext(conf)
    //    val documents = sc.parallelize(List("1 2 4", "2 3 4")).map(i => {
    //      i.split(" ").toSeq
    //    })
    val documents = sc.textFile("src/main/resources/positionName.txt").map(i => {
      Fenci.fenciArr(i).toSeq
    })


    val hashingTF = new HashingTF()

    val mapWords = documents.flatMap(x => x)
      .map(w => (hashingTF.indexOf(w), w))
      .collect.toMap

    val tf: RDD[Vector] = hashingTF.transform(documents)

    val idf = new IDF().fit(tf)

    val tfidf: RDD[Vector] = idf.transform(tf)

    //    val tfidf = tf.map(i => {
    //      IDFModel.transform(idf.idf, i)
    //    })

    val bcWords = tf.context.broadcast(mapWords)

    val r = tfidf.flatMap {
      case SparseVector(size, indices, values) =>
        val words = indices.map(index => bcWords.value.getOrElse(index, "null"))
        var num = 0
        words.foreach(i => {
          if (i.equals("工程")) {
            var value = values.apply(num)
            var hash = indices.apply(num)
            if (value > 2)
              println(value)
          }
          num += 1
        })
        words.zip(values).toSeq
    }.distinct()

    //模型
    //    tfidf.foreach(i => {
    //      println(i)
    //    })

    //词  tfidf值
    //    r.collect().sortBy(_._2).foreach(i => {
    //      println(i)
    //    })
    r.sortBy(_._2).coalesce(1).saveAsTextFile("src/main/resources/wordTfIdf")
  }

  private object IDFModel {

    def transform(idf: Vector, v: Vector): Vector = {
      val n = v.size
      v match {
        case SparseVector(size, indices, values) =>
          val nnz = indices.length
          val newValues = new Array[Double](nnz)
          var k = 0
          while (k < nnz) {
            newValues(k) = values(k) * idf(indices(k))
            k += 1
          }
          Vectors.sparse(n, indices, newValues)
        case DenseVector(values) =>
          val newValues = new Array[Double](n)
          var j = 0
          while (j < n) {
            newValues(j) = values(j) * idf(j)
            j += 1
          }
          Vectors.dense(newValues)
        case other =>
          throw new UnsupportedOperationException(
            s"Only sparse and dense vectors are supported but got ${other.getClass}.")
      }
    }
  }

}
