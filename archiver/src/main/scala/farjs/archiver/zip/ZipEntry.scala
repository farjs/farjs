package farjs.archiver.zip

import farjs.archiver.DateTimeUtil

case class ZipEntry(
  parent: String,
  name: String,
  isDir: Boolean = false,
  size: Double = 0.0,
  datetimeMs: Double = 0.0,
  permissions: String = ""
)

object ZipEntry {

  private val itemRegex = """([d|-].+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+)""".r

  def fromUnzipCommand(output: String): List[ZipEntry] = {
    output.trim.split('\n').flatMap { line =>
      for {
        itemRegex(permissions, _, _, length, _, _, datetime, pathName) <- itemRegex.findFirstMatchIn(line)
      } yield {
        val path = pathName.stripSuffix("/")
        val lastSlash = path.lastIndexOf('/')
        val (parent, name) =
          if (lastSlash != -1) path.splitAt(lastSlash)
          else ("", path)

        ZipEntry(
          parent = parent,
          name = name.stripPrefix("/"),
          isDir = pathName.endsWith("/"),
          size = length.toDouble,
          datetimeMs = DateTimeUtil.parseDateTime(datetime),
          permissions = permissions
        )
      }
    }.toList
  }
}
