package com.github.chenmingang

import com.github.chenmingang.util.ConfigUtil
import java.util.regex.{Matcher, Pattern}

import org.apache.spark.{SparkConf, SparkContext}

/**
  * 看大神帖子写代码
  *
  * http://www.matrix67.com/blog/archives/5044
  *
  * Created by zeal on 16-7-24.
  */
object MyFenci {

  case class KeyWord(word: String, num: Int, left: Map[String, Int], right: Map[String, Int])

  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("分词")
      .setMaster(ConfigUtil.getProperty("masterUrl"))
      .set("spark.executor.memory", "4g")


    val sc = new SparkContext(conf)

    val zhuxian = sc.textFile("src/main/resources/西游记.txt")

    val wordCount = zhuxian.flatMap(line => {
      val text = replaceSprical(line)
      var list: List[(String, Int)] = List()
      allSubStr(text, 1).split(",").filter(_.nonEmpty).foreach(w => {
        list ::= (w, 1)
      })
      list.toArray.toSeq
    }).reduceByKey(_ + _)


    val ci = zhuxian.flatMap(line => {
      val text = replaceSprical(line)
      var wordStr: String = ""

      //      wordStr = wordStr + allSubStr(text, 1)
      wordStr = wordStr + allSubStr(text, 2)
      wordStr = wordStr + allSubStr(text, 3)
      wordStr = wordStr + allSubStr(text, 4)
      //      wordStr = wordStr + allSubStr(text, 5)

      if (wordStr.length > 1) wordStr = wordStr.substring(0, wordStr.length)

      val wordArray = wordStr.split(",")

      var index: Int = 0
      var list: List[(String, KeyWord)] = List()
      while (index < wordArray.length) {
        var left = ""
        var right = ""

        val word = wordArray(index)
        if (wordArray.length == 1) {
          list ::= (word, KeyWord(word, 1, Map(), Map()))
        } else if (index == 0) {
          list ::= (word, KeyWord(word, 1, Map(), Map(word.substring(word.length - 1, word.length) -> 1)))
        } else if (index == wordArray.length - 1) {
          list ::= (word, KeyWord(word, 1, Map(wordArray(index - 1).substring(0, 1) -> 1), Map()))
        } else {
          val wordRight = wordArray(index + 1)
          list ::= (word, KeyWord(word, 1, Map(wordArray(index - 1).substring(0, 1) -> 1), Map(wordRight.substring(wordRight.length - 1, wordRight.length) -> 1)))
        }
        index += 1
      }
      list.toArray.toSeq
    }).reduceByKey((k1, k2) => {
      val num = k1.num + k2.num
      val left = mapUnion(k1.left, k2.left)
      val right = mapUnion(k1.right, k2.right)
      KeyWord(k1.word, num, left, right)
    }).map(k => {
      var num = k._2.num
      var free = 0d
      var leftFree = 0d
      var rightFree = 0d
      k._2.left.foreach(kv => {
        leftFree += -Math.log(1d / k._2.left.size) * (1d / k._2.left.size)
      })
      k._2.right.foreach(kv => {
        rightFree += -Math.log(1d / k._2.right.size) * (1d / k._2.left.size)
      })
      free = Math.min(leftFree, rightFree)
      (k._1, num, free)
    })


    var ciMap = Map[String, Int]()
    ci.collect().foreach(c => {
      ciMap = ciMap.+(c._1 -> c._2)
    })

    var totalNum = 0

    val wc = wordCount.collect()
    val wcNum = wc.size.toDouble
    wc.foreach(c => {
      ciMap = ciMap.+(c._1 -> c._2)
      totalNum += c._2
    })

    val totalNumBro = sc.broadcast(totalNum)
    val ciMapBro = sc.broadcast(ciMap)
    ci.map(s => {
      val str = s._1
      val allChildStr = allChild(str).split(",")
      var minNinggu = 100000000000d;
      allChildStr.foreach(subStr => {
        val subArr = subStr.split("-")
        if (subArr.length == 2) {
          val num1 = ciMapBro.value.get(subArr.apply(0)).get
          val num2 = ciMapBro.value.get(subArr.apply(1)).get

          val subStrLv = (s._2.toDouble / totalNumBro.value) / ((num1.toDouble / totalNumBro.value) * (num2.toDouble / totalNumBro.value))
          if (minNinggu > subStrLv) {
            minNinggu = subStrLv
          }
        }
      })
      (str, s._2, s._3, minNinggu)
      //词//频次//自由程度//凝固程度
    }).filter(_._2 > 5)
      .filter(_._3 > 2)
      .filter(_._4 > 50)
      .collect()
      .sortWith(_._2 > _._2)
      .foreach(println)

  }

  def mapUnion(map1: Map[String, Int], map2: Map[String, Int]): Map[String, Int] = {
    var map = Map[String, Int]()

    map1.foreach(kv => {
      if (map.contains(kv._1)) {
        val num = map.get(kv._1).get + 1
        map = map.+((kv._1, num))
      } else {
        map = map.+((kv._1, 1))
      }
    })
    map2.foreach(kv => {
      if (map.contains(kv._1)) {
        val num = map.get(kv._1).get + 1
        map = map.+((kv._1, num))
      } else {
        map = map.+((kv._1, 1))
      }
    })
    map
  }

  val p: Pattern = Pattern.compile("<[^>]+>|[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|-|nbsp|\n|\r| |　|」|「|》|《", Pattern.CASE_INSENSITIVE)

  def replaceSprical(str: String): String = {
    var dest: String = ""
    if (str != null) {
      val m: Matcher = p.matcher(str)
      dest = m.replaceAll("")
    }
    dest
  }

  def allSubStr(text: String, num: Int): String = {
    var result = ""
    var index: Int = num - 1
    while (index < text.length) {
      result = result + text.substring(index - (num - 1), index + 1) + ","
      index += 1
    }
    result
  }

  def allChild(str: String): String = {
    var result = "";
    if (str.length < 1) {
      return result
    }
    var i = 0
    for (i <- 1 to str.length - 1) {
      result += str.substring(0, i) + "-"
      result += str.substring(i, str.length) + ","
    }
    result = result.substring(0, result.length - 1)
    result
  }
}