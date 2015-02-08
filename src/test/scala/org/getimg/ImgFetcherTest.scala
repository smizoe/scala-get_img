import org.getimg.ImgFetcher
import org.scalatest._
import org.json4s._
import org.json4s.native.JsonMethods._
import java.nio.file.{Files,Paths}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.Await

class ImgFetcherSpec extends FlatSpec with Matchers {
    val conf = ConfigFactory.load()
    val json = parse(conf.getString("imgfetcher-test.testJSON"))
    val obj = ImgFetcher(json)
    def setTmp() = {
       val tmpDir = Files.createTempDirectory("imgfetcher")
       ImgFetcher.imgStore = tmpDir.toString
    }
    "An ImgFetcher" should "downloads a file that is located by the given URL" in {
       setTmp()
       val urlStr = conf.getString("imgfetcher-test.testUrl")
       obj.getImage(urlStr)
       val path = Paths.get(ImgFetcher.imgStore, urlStr.split("/").last)
       assert(Files.exists(path))
    }

    it should "rename the downloaded file when necessary" in {
       setTmp()
       val tasks = obj.getImages()
       for( f <- tasks){
           Await.ready(f, Duration(3, MINUTES))
       }


       for( filename <- Array("logo_col_874x288.png", "logo_col_874x288_1.png", "4682.Bing-logo-orange-RGB.jpg")){
           val path = Paths.get(ImgFetcher.imgStore, filename)
           assert(Files.exists(path))
       }
    }
}
