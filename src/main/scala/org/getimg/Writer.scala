package org.getimg
impoty org.json4s._, org.json4s.native.JsonMethods._

class Writer(response: JValue){
  def getImages() = {
    val targets = for {
      JField("d", JObject(obj)) <- response
      JField("results", JArray(results)) <- obj
      Jobject(result) <- results
      JField("Title", JString(title)) <- result
      JField("MediaUrl", JString(mediaUrl)) <- result
    } yield (title, mediaUrl)
    targets slice(0, numImages) map { (title, mediaUrl) =>

    }
  }
}

object Writer{
  def apply(response: Future[Option[String]]){
    new Writer(response)
  }
  val numImages = 100
}
