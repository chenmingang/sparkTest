package com.github.chenmingang.util

import java.io.InputStream

/**
  * Created by zeal on 16-7-20.
  */
object WordTFIDFScore {

  val inputStream: InputStream = getClass.getResourceAsStream("/wordTfIdf.txt")

  var wordScoreMap: Map[String, Double] = scala.collection.immutable.Map()

  var maxScore = 0D

  var file = scala.io.Source.fromInputStream(inputStream).getLines.foreach(ws => {
    val wsArr = ws.replaceFirst("\\(", "").replaceFirst("\\)", "").split(",")
    if (wsArr.length == 2) {
      var word = wsArr.apply(0)
      var score = wsArr.apply(1).toDouble
      if (score > maxScore) {
        maxScore = score
      }
      wordScoreMap.+=(word -> score)
    }
  })
  //归一化
  wordScoreMap = wordScoreMap.map(kv => {
    (kv._1, kv._2 / maxScore)
  })

  def getScore(word: String) = {
    val score = wordScoreMap.get(word)
    var result = 0d
    if (!score.equals(None)) {
      result = score.get
    }
    result
  }

  def main(args: Array[String]) {
    println(getScore("工程"))
  }
}
