package org.getimg
import dispatch._

class Searcher(kwd: String, apiKey: String){
    def query() = {
      val svc = url()
      val result = Http(svc OK as.json4s.Json)
      result onComplete {
        case Success(json) => Writer(json).getImages()
        case Failure(err)  => throw err
      }
    }
}

object Searcher{
    def apply(kwd: String, apiKey: String) = {
        new Searcher(kwd, apiKey)
    }
}
