package org.getimg
import java.nio.file.{Paths, Files}
import scala.concurrent.{Future,Await}
import scala.concurrent.duration._
import scala.util.{Success,Failure}
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global

object GetImg extends LazyLogging{
    val usage = """
      Usage: getimg [--store-dir DIR] [--apikey KEY] [--max-num-img NUM] [--keyword KWD]
      --keyword and --apikey are required.
    """

    type OptionMap = Map[Symbol, String]

    def parseOpt(opts: OptionMap, list: List[String]) : OptionMap = {
        list match{
            case Nil => opts
            case "--store-dir" :: path :: tail =>
                parseOpt(opts ++ Map('store_dir -> path), tail)
            case "--apikey" :: key :: tail =>
                parseOpt(opts ++ Map('apikey -> key), tail)
            case "--max-num-img" :: value :: tail =>
                parseOpt(opts ++ Map('num_img -> value), tail)
            case "--keyword" :: kwd :: tail =>
                parseOpt(opts ++ Map('kwd -> kwd), tail)
            case option :: tail =>
                throw new RuntimeException("Unknown option:" + option)
        }
    }

    def main(args: Array[String]){
        if (args.length == 0)
            println(usage)

        val argsList = args.toList
        val options = parseOpt(Map[Symbol, String](),argsList)
        if (! options.contains('kwd))
            throw new RuntimeException("--keyword is a required option")

        val conf = ConfigFactory.load()

        // set a directory to store images
        val dir   = options.getOrElse('store_dir, "/tmp")
        val store = Paths.get(dir)
        if(!Files.exists(store))
            throw new RuntimeException(dir + " does not exist.")
        if(! Files.isDirectory(store))
            throw new RuntimeException(s"File $dir is not a directory.")
        if(!Files.isWritable(store))
            throw new RuntimeException(s"Directory $dir is not writable.")
        ImgFetcher.imgStore = dir

        // set maximal # of images fetched
        ImgFetcher.numImages = options.getOrElse('num_img, "10").toInt

        val apikey = options.getOrElse('apikey, conf.getString("get-img.apikey"))

        logger.info("finished initialization")
        val queryResult = Searcher(options('kwd), apikey).query()
        queryResult.onComplete{
            case Success(json) => {
                ImgFetcher(json).getImages().foreach{ f =>
                    val result = Await.ready(f, Duration(3, MINUTES))
                    result.value.get match {
                        case Success(_) => Unit
                        case Failure(t) => logger.warn(t.getMessage())
                    }
                }
                sys.exit()
            }
            case Failure(t)   => logger.warn(t.getMessage())
        }
    }
}
