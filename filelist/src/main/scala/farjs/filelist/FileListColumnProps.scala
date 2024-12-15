package farjs.filelist

import farjs.filelist.api.FileListItem

import scala.scalajs.js

sealed trait FileListColumnProps extends js.Object {
  val width: Int
  val height: Int
  val left: Int
  val borderCh: String
  val items: js.Array[FileListItem]
  val focusedIndex: Int
  val selectedNames: js.Set[String]
}

object FileListColumnProps {

  def apply(width: Int,
            height: Int,
            left: Int,
            borderCh: String,
            items: js.Array[FileListItem],
            focusedIndex: Int,
            selectedNames: js.Set[String]): FileListColumnProps = {

    js.Dynamic.literal(
      width = width,
      height = height,
      left = left,
      borderCh = borderCh,
      items = items,
      focusedIndex = focusedIndex,
      selectedNames = selectedNames
    ).asInstanceOf[FileListColumnProps]
  }

  def unapply(arg: FileListColumnProps): Option[(Int, Int, Int, String, js.Array[FileListItem], Int, js.Set[String])] = {
    Some((
      arg.width,
      arg.height,
      arg.left,
      arg.borderCh,
      arg.items,
      arg.focusedIndex,
      arg.selectedNames
    ))
  }

  def copy(p: FileListColumnProps)(width: Int = p.width,
                                   height: Int = p.height,
                                   left: Int = p.left,
                                   borderCh: String = p.borderCh,
                                   items: js.Array[FileListItem] = p.items,
                                   focusedIndex: Int = p.focusedIndex,
                                   selectedNames: js.Set[String] = p.selectedNames): FileListColumnProps = {

    FileListColumnProps(
      width = width,
      height = height,
      left = left,
      borderCh = borderCh,
      items = items,
      focusedIndex = focusedIndex,
      selectedNames = selectedNames
    )
  }
}
