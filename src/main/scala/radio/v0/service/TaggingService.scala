package radio.v0
package service

import scala.concurrent.Future



trait TaggingService[Tag, Content] {

  def createTag(tag: String): ServiceOperation[Future,Tag]

  def listTaggedContents(tag: Tag): ServiceOperation[Future,Seq[Content]]

}
