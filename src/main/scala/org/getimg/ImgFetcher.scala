package org.getimg
import scala.concurrent.Future
import org.json4s._, org.json4s.native.JsonMethods._
import java.net.URL
import java.nio.channels.{Channels,FileChannel,ReadableByteChannel,FileLock}
import java.nio.file.{Files,Paths,StandardOpenOption}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext.Implicits.global

class ImgFetcher(response: JValue) extends LazyLogging{
  def transferUntilEnd(rch: ReadableByteChannel, wch: FileChannel, bytesRead: Long, remainingBytes: Long) : Unit = {
    val numBytes = wch.transferFrom(rch, bytesRead, remainingBytes)
    if(remainingBytes - numBytes > 0)
        transferUntilEnd(rch, wch, bytesRead + numBytes, remainingBytes - numBytes)
  }

  def getLockedChannel(fileName :String, index :Int) : (FileLock, FileChannel) = {

      // given a fileName (and 'index'), returns a FileChannel Object
      //  that refers to a file named sililarly to fileName with its FileLock

      val newFileName = if(index != 0) {
             // preserve file extention and add a sequence number
             val replaced = fileName.reverse.replaceFirst("""\.""", s"_${index}.".reverse).reverse
             if (replaced.length == fileName.length)
                 fileName + index.toString
             else
                 replaced
          } else {
             fileName
          }
      val candidate = Paths.get(ImgFetcher.imgStore, newFileName)

      if (Files.exists(candidate))
          getLockedChannel(fileName, index + 1)
      else{

          val candidateChannel = FileChannel.open(candidate, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
          val lock = candidateChannel.tryLock()

          if (lock == null){
            candidateChannel.close()
            getLockedChannel(fileName, index + 1)
          }else
            (lock, candidateChannel)
      }
  }

  def getImage(mediaUrl: String) : Unit = {

      logger.info("start getting "+mediaUrl)
      val url = new URL(mediaUrl)
      val conn = url.openConnection()
      val imgSize = conn.getContentLengthLong()
      val ch = Channels.newChannel(conn.getInputStream())
      val fileName = Paths.get(url.getPath()).getFileName().toString
      val (lock, outFileChannel) = getLockedChannel(fileName, 0)
      transferUntilEnd(ch, outFileChannel, 0, imgSize)
      logger.info("finished getting "+mediaUrl)
      lock.release()
      outFileChannel.close()
  }

  def getImages() :List[Future[Unit]] = {
    val targets = for {
      JObject(json) <- response
      JField("d", JObject(obj)) <- json
      JField("results", JArray(results)) <- obj
      JObject(result) <- results
      JField("MediaUrl", JString(mediaUrl)) <- result
    } yield mediaUrl

    targets.slice(0, ImgFetcher.numImages).map{ mediaUrl =>
      Future {
          getImage(mediaUrl)
      }
    }

  }
}

object ImgFetcher{
  def apply(response: JValue) = {
    new ImgFetcher(response)
  }
  var numImages = 10
  var imgStore  = "/tmp"
}
