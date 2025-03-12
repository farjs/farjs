package farjs.fs

import farjs.filelist.api.FileListDir
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait FSFreeSpaceProps extends js.Object {
  val dispatch: Dispatch
  val currDir: FileListDir
}

object FSFreeSpaceProps {

  def apply(dispatch: Dispatch, currDir: FileListDir): FSFreeSpaceProps = {
    js.Dynamic.literal(
      dispatch = dispatch,
      currDir = currDir
    ).asInstanceOf[FSFreeSpaceProps]
  }

  def unapply(arg: FSFreeSpaceProps): Option[(Dispatch, FileListDir)] = {
    Some((
      arg.dispatch,
      arg.currDir
    ))
  }

  def copy(p: FSFreeSpaceProps)(dispatch: Dispatch = p.dispatch,
                                currDir: FileListDir = p.currDir): FSFreeSpaceProps = {
    FSFreeSpaceProps(
      dispatch = dispatch,
      currDir = currDir
    )
  }
}
