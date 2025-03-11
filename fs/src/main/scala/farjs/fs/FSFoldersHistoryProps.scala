package farjs.fs

import scala.scalajs.js

sealed trait FSFoldersHistoryProps extends js.Object {
  val currDirPath: String
}

object FSFoldersHistoryProps {

  def apply(currDirPath: String): FSFoldersHistoryProps = {
    js.Dynamic.literal(
      currDirPath = currDirPath
    ).asInstanceOf[FSFoldersHistoryProps]
  }

  def unapply(arg: FSFoldersHistoryProps): Option[String] = {
    Some(
      arg.currDirPath
    )
  }

  def copy(p: FSFoldersHistoryProps)(currDirPath: String = p.currDirPath): FSFoldersHistoryProps = {
    FSFoldersHistoryProps(
      currDirPath = currDirPath
    )
  }
}
