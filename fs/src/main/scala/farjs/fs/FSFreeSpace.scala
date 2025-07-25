package farjs.fs

import farjs.filelist.FileListActions.FileListDiskSpaceUpdatedAction
import farjs.filelist.api.FileListDir
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object FSFreeSpace extends FunctionComponent[FSFreeSpaceProps] {

  private[fs] var fsService: FSService = FSService.instance

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val currDirRef = useRef[FileListDir](null)
    currDirRef.current = props.currDir

    useLayoutEffect({ () =>
      val currDir = props.currDir
      fsService.readDisk(currDir.path).toFuture.recover {
        case _ => js.undefined
      }.foreach { maybeDisk =>
        maybeDisk.toOption.foreach { disk =>
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
