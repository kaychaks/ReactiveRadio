package radio.v0
package domain

import java.net.URL

case class Tag(
                tagName: String,
                tagID: ID,
                tagURL: Option[URL] = None,
                taggedContentIds: Vector[ID]
              )
