package farjs.app.filelist.fs

import farjs.filelist.FileListActions.FileListDiskSpaceUpdatedAction
import farjs.filelist.api.FileListDir
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

case class FSFreeSpaceProps(dispatch: Dispatch,
                            currDir: FileListDir)

object FSFreeSpace extends FunctionComponent[FSFreeSpaceProps] {

  private[fs] var fsService: FSService = FSService.instance

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val currDirRef = useRef[FileListDir](null)
    currDirRef.current = props.currDir

    useLayoutEffect({ () =>
      val currDir = props.currDir
      fsService.readDisk(currDir.path).recover {
        case NonFatal(_) => None
      }.foreach { maybeDisk =>
        maybeDisk.foreach { disk =>
          if (isSameInstance(currDir, currDirRef.current)) {
            props.dispatch(FileListDiskSpaceUpdatedAction(disk.free))
          }
        }
      }
      ()
    }, List(System.identityHashCode(props.currDir)))

    null
  }
  
  private def isSameInstance(o1: Any, o2: Any): Boolean =
    System.identityHashCode(o1) == System.identityHashCode(o2)
}
