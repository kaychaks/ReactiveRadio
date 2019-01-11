package radio.v0
package domain

import java.nio.file.Path
import java.time.Duration

case class Media(id: ID,
                 filePath: Option[Path],
                 size: Long,
                 duration: Option[Duration] = None)
