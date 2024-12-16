package farjs.filelist

import farjs.filelist.api.FileListItem
import scommons.react.blessed.BlessedScreen

import scala.scalajs.js

sealed trait FileListViewProps extends js.Object {
  val width: Int
  val height: Int
  val columns: Int
  val items: js.Array[FileListItem]
  val focusedIndex: Int
  val selectedNames: js.Set[String]
  val onWheel: js.Function1[Boolean, Unit]
  val onClick: js.Function1[Int, Unit]
  val onKeypress: js.Function2[BlessedScreen, String, Unit]
}

object FileListViewProps {

  def apply(width: Int,
            height: Int,
            columns: Int,
            items: js.Array[FileListItem],
            focusedIndex: Int = -1,
            selectedNames: js.Set[String] = js.Set[String](),
            onWheel: js.Function1[Boolean, Unit] = _ => (),
            onClick: js.Function1[Int, Unit] = _ => (),
            onKeypress: js.Function2[BlessedScreen, String, Unit] = (_, _) => ()): FileListViewProps = {

    js.Dynamic.literal(
      width = width,
      height = height,
      columns = columns,
      items = items,
      focusedIndex = focusedIndex,
      selectedNames = selectedNames,
      onWheel = onWheel,
      onClick = onClick,
      onKeypress = onKeypress
    ).asInstanceOf[FileListViewProps]
  }

  def unapply(arg: FileListViewProps): Option[
    (Int, Int, Int, js.Array[FileListItem], Int, js.Set[String], js.Function1[Boolean, Unit], js.Function1[Int, Unit], js.Function2[BlessedScreen, String, Unit])
  ] = {
    Some((
      arg.width,
      arg.height,
      arg.columns,
      arg.items,
      arg.focusedIndex,
      arg.selectedNames,
      arg.onWheel,
      arg.onClick,
      arg.onKeypress
    ))
  }

  def copy(p: FileListViewProps)(width: Int = p.width,
                                 height: Int = p.height,
                                 columns: Int = p.columns,
                                 items: js.Array[FileListItem] = p.items,
                                 focusedIndex: Int = p.focusedIndex,
                                 selectedNames: js.Set[String] = p.selectedNames,
                                 onWheel: js.Function1[Boolean, Unit] = p.onWheel,
                                 onClick: js.Function1[Int, Unit] = p.onClick,
                                 onKeypress: js.Function2[BlessedScreen, String, Unit] = p.onKeypress): FileListViewProps = {

    FileListViewProps(
      width = width,
      height = height,
      columns = columns,
      items = items,
      focusedIndex = focusedIndex,
      selectedNames = selectedNames,
      onWheel = onWheel,
      onClick = onClick,
      onKeypress = onKeypress
    )
  }
}
