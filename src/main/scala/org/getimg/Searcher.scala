package org.getimg
import dispatch._
import org.json4s._
import java.net.URLEncoder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success,Failure}
import com.typesafe.scalalogging.LazyLogging

class Searcher(kwd: String, apiKey: String) extends LazyLogging {
    def query() = {
      val parameter = "$format=json&Query=" + URLEncoder.encode("'" + kwd + "'", "UTF-8")
      val urlString = "https://api.datamarket.azure.com/Bing/Search/Image?" + parameter
      val req = url(urlString).as(apiKey, apiKey)
      val result = Http(req OK as.json4s.Json)
      logger.info("start to search images with keyword "+kwd)
      result
    }
    def startWrite(result : Future[org.json4s.JValue]) = {
      result onComplete {
        case Success(json) => {
          logger.info("querying with keyword finished")
          ImgFetcher(json).getImages()
        }
        case Failure(err)  => throw err
      }
    }
    def run() = {
      val result = query()
      startWrite(result)
    }
}

object Searcher{
    def apply(kwd: String, apiKey: String) = {
        new Searcher(kwd, apiKey)
    }
}
