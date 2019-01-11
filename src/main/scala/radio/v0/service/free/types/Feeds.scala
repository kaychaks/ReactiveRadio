package radio.v0.service
package free
package types

import radio.v0.domain.Content

// ADT for language
sealed trait Feeds[A]
case class CreateFeed[T](content: Content)
