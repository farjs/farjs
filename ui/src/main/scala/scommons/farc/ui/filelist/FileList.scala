package scommons.farc.ui.filelist

import scommons.farc.api.filelist._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

case class FileListProps(api: FileListApi,
                         size: (Int, Int),
                         columns: Int,
                         state: FileListState,
                         onStateChanged: js.Function1[FileListState, Unit])

object FileList extends FunctionComponent[FileListProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    val (_, height) = props.size
    val items = props.state.items
    val columnSize = height - 1 // excluding column header
    val viewSize = columnSize * props.columns
    
    val viewOffset = props.state.offset
    val focusedIndex = props.state.index
    val viewItems = items.view(viewOffset, viewOffset + viewSize)
    val maxOffset = items.size - viewItems.size
    val maxIndex = math.max(viewItems.size - 1, 0)

    useLayoutEffect({ () =>
      onChangeDir(props, viewSize, None)
    }, Nil)
    
    def focusDx(dx: Int, select: Boolean): Unit = {
      val index = focusedIndex + dx
      if (index < 0 || index > maxIndex) {
        val newOffset = viewOffset + dx
        val newIndex =
          if (newOffset < 0) 0
          else if (newOffset > maxOffset) maxIndex
          else focusedIndex

        focusItem(newOffset, newIndex, select)
      }
      else focusItem(viewOffset, index, select)
    }
    
    def focusItem(offset: Int, index: Int, select: Boolean = false): Unit = {
      val newOffset = math.min(math.max(offset, 0), maxOffset)
      val newIndex = math.min(math.max(index, 0), maxIndex)
      
      val currSelected = props.state.selectedNames
      val newSelected =
        if (select && items.nonEmpty) {
          val currIndex = viewOffset + math.min(math.max(focusedIndex, 0), maxIndex)
          val selectIndex = newOffset + newIndex
          
          val isFirst = selectIndex == 0
          val isLast = selectIndex == items.size - 1
          val selection = {
            if (isFirst && selectIndex < currIndex) items.view(selectIndex, currIndex + 1)
            else if (selectIndex < currIndex) items.view(selectIndex + 1, currIndex + 1)
            else if (isLast && selectIndex > currIndex) items.view(currIndex, selectIndex + 1)
            else items.view(currIndex, selectIndex)
          }.map(_.name).toSet
  
          val currName = items(currIndex).name
          val newSelected =
            if (currSelected.contains(currName)) currSelected -- selection
            else currSelected ++ selection
          
          newSelected - FileListItem.up.name
        }
        else currSelected

      val state = props.state.copy(offset = newOffset, index = newIndex, selectedNames = newSelected)
      if (props.state != state) {
        props.onStateChanged(state)
      }
    }
    
    <(FileListView())(^.wrapped := FileListViewProps(
      size = props.size,
      columns = props.columns,
      items = viewItems,
      focusedIndex = focusedIndex,
      selectedNames = props.state.selectedNames,
      onWheelUp = { () =>
        if (viewOffset > 0) focusItem(viewOffset - 1, focusedIndex)
        else focusItem(viewOffset, focusedIndex - 1)
      },
      onWheelDown = { () =>
        if (viewOffset < maxOffset) focusItem(viewOffset + 1, focusedIndex)
        else focusItem(viewOffset, focusedIndex + 1)
      },
      onClick = { index =>
        focusItem(viewOffset, index)
      },
      onKeypress = {
        case k if k == "up" || k == "S-up" => focusDx(- 1, k == "S-up")
        case k if k == "down" || k == "S-down" => focusDx(1, k == "S-down")
        case k if k == "left" || k == "S-left" => focusDx(-columnSize, k == "S-left")
        case k if k == "right" || k == "S-right" => focusDx(columnSize, k == "S-right")
        case k if k == "pageup" || k == "S-pageup" => focusDx(-viewSize + 1, k == "S-pageup")
        case k if k == "pagedown" || k == "S-pagedown" => focusDx(viewSize - 1, k == "S-pagedown")
        case k if k == "home" || k == "S-home" => focusItem(0, 0, k == "S-home")
        case k if k == "end" || k == "S-end" => focusItem(maxOffset, maxIndex, k == "S-end")
        case "enter" =>
          props.state.currentItem.filter(_.isDir).foreach { dir =>
            onChangeDir(props, viewSize, Some(dir.name))
          }
        case _ =>
      }
    ))()
  }
  
  private def onChangeDir(props: FileListProps, viewSize: Int, dir: Option[String]): Unit = {
    
    def recover[T](f: Future[T], value: T): Future[T] = f.recover {
      case JavaScriptException(error) =>
        println(s"$error")
        value
      case error =>
        println(s"$error")
        value
    }
    
    val future = for {
      currDir <- dir match {
        case None => props.api.currDir
        case Some(d) => props.api.changeDir(d)
      }
      files <- recover(props.api.listFiles, Nil)
    } yield {
      val items = {
        val sorted = files.sortBy(item => (!item.isDir, item.name))

        if (currDir.isRoot) sorted
        else FileListItem.up +: sorted
      }
      val (offset, index) = 
        if (dir.contains(FileListItem.up.name)) {
          val focusedDir = props.state.currDir.path
            .stripPrefix(currDir.path)
            .stripPrefix("/")
            .stripPrefix("\\")
          
          val index = math.max(items.indexWhere(_.name == focusedDir), 0)
          if (viewSize <= 0 || index < viewSize) (0, index)
          else {
            val viewIndex = index % viewSize
            (index - viewIndex, viewIndex)
          }
        }
        else (0, 0)
      
      props.onStateChanged(props.state.copy(
        offset = offset,
        index = index,
        currDir = currDir,
        items = items,
        selectedNames = Set.empty
      ))
    }

    recover(future, ())
  }
}
