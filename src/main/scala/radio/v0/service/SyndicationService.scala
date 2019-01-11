package radio.v0
package service

import cats.data.Xor
import radio.v0.domain.customexceptions.CustomException.SyndicationError

// trait FeedContentMaker[Content, Feed, FeedType, Encoding, ContentRepo] {
//   def generateFeed(c: Content,
//                    items: Vector[Content])(implicit r: ContentRepo): Xor[SyndicationError, Feed]
// }

// trait SyndicationService[Content, ItemContent, Feed] {
//   def createFeed[FC <: Content, IC <: ItemContent, FeedType, Encoding](
//       content: FC)(
//       implicit sh: FeedContentMaker[FC, IC, Feed, FeedType, Encoding, RepoContent])
//     : SyndicationOperation[Feed]

//   def addFeedToContent(f: Feed, c: Content): ServiceOperation[Content]

//   def fetchFeed[A](f: A): ServiceOperation[Feed]

// }
