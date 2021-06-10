package farjs.filelist.fs

import farjs.filelist.api.FileListDir
import scommons.nodejs._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

case class FSFreeSpaceProps(currDir: FileListDir,
                            onRender: Option[Double] => ReactElement)

object FSFreeSpace extends FunctionComponent[FSFreeSpaceProps] {

  private[fs] var fsService: FSService = new FSService(process.platform, child_process)

  protected def render(compProps: Props): ReactElement = {
    val (freeBytes, setFreeBytes) = useState(Option.empty[Double])
    val props = compProps.wrapped
    val currDirRef = useRef[FileListDir](null)
    currDirRef.current = props.currDir

    useLayoutEffect({ () =>
      val currDir = props.currDir
      fsService.readDisk(currDir.path).recover {
        case NonFatal(_) => None
      }.foreach { maybeDisk =>
        if (isSameInstance(currDir, currDirRef.current)) {
          setFreeBytes(maybeDisk.map(_.free))
        }
      }
      ()
    }, List(System.identityHashCode(props.currDir)))

    props.onRender(freeBytes)
  }
  
  private def isSameInstance(o1: Any, o2: Any): Boolean =
    System.identityHashCode(o1) == System.identityHashCode(o2)
}
