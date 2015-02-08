import org.getimg.Searcher
import org.scalatest._
import time._
import org.json4s._
import com.typesafe.config.ConfigFactory
import concurrent.{ScalaFutures,Futures}

class SearcherSpec extends FlatSpec with Matchers with Futures with ScalaFutures{
  val conf = ConfigFactory.load()
  "A Searcher" should "make a query based on given keywords and return JSON" in {
      val s = Searcher("keyword", conf.getString("get-img.apikey"))
      val result = s.query()
      whenReady(result, timeout(Span(5, Minutes)), interval(Span(1, Seconds))){ content =>
        val dField = (content \ "d")
        assert(dField.isInstanceOf[JValue])
        dField should not be JNothing
      }
  }
}
