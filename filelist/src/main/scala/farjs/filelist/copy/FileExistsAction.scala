package farjs.filelist.copy

sealed trait FileExistsAction

object FileExistsAction {

  case object Overwrite extends FileExistsAction
  case object All extends FileExistsAction
  case object Skip extends FileExistsAction
  case object SkipAll extends FileExistsAction
  case object Append extends FileExistsAction
}
