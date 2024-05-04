package farjs.filelist

import farjs.filelist.api._
import farjs.filelist.sort.SortMode
import farjs.ui.Dispatch
import farjs.ui.task.TaskAction

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/FileListActions.mjs", JSImport.Default)
class FileListActions(var api: FileListApi) extends js.Object {

  def changeDir(dispatch: Dispatch, path: String, dir: String): TaskAction = js.native

  def updateDir(dispatch: Dispatch, path: String): TaskAction = js.native

  def createDir(dispatch: Dispatch,
                parent: String,
                dir: String,
                multiple: Boolean): TaskAction = js.native

  def deleteItems(dispatch: Dispatch,
                  parent: String,
                  items: js.Array[FileListItem]): TaskAction = js.native

  def scanDirs(parent: String,
               items: js.Array[FileListItem],
               onNextDir: js.Function2[String, js.Array[FileListItem], Boolean]): js.Promise[Boolean] = js.native

  def copyFile(srcDir: String,
               srcItem: FileListItem,
               dstFileF: js.Promise[js.UndefOr[FileTarget]],
               onProgress: js.Function1[Double, js.Promise[Boolean]]): js.Promise[Boolean] = js.native
}

object FileListActions {
  
  sealed trait FileListParamsChangedAction extends js.Object {
    val action: String
    val offset: Int
    val index: Int
    val selectedNames: js.Set[String]
  }
  object FileListParamsChangedAction {
    val name = "FileListParamsChangedAction"

    def apply(offset: Int,
              index: Int,
              selectedNames: js.Set[String]): FileListParamsChangedAction = {
      js.Dynamic.literal(
        action = name,
        offset = offset,
        index = index,
        selectedNames = selectedNames
      ).asInstanceOf[FileListParamsChangedAction]
    }

    def unapply(arg: FileListParamsChangedAction): Option[(Int, Int, js.Set[String])] =
      Some((
        arg.offset,
        arg.index,
        arg.selectedNames
      ))
  }

  sealed trait FileListDirChangedAction extends js.Object {
    val action: String
    val dir: String
    val currDir: FileListDir
  }
  object FileListDirChangedAction {
    val name = "FileListDirChangedAction"

    def apply(dir: String,
              currDir: FileListDir): FileListDirChangedAction = {
      js.Dynamic.literal(
        action = name,
        dir = dir,
        currDir = currDir
      ).asInstanceOf[FileListDirChangedAction]
    }

    def unapply(arg: FileListDirChangedAction): Option[(String, FileListDir)] =
      Some((
        arg.dir,
        arg.currDir
      ))
  }
  
  sealed trait FileListDirUpdatedAction extends js.Object {
    val action: String
    val currDir: FileListDir
  }
  object FileListDirUpdatedAction {
    val name = "FileListDirUpdatedAction"

    def apply(currDir: FileListDir): FileListDirUpdatedAction = {
      js.Dynamic.literal(
        action = name,
        currDir = currDir
      ).asInstanceOf[FileListDirUpdatedAction]
    }

    def unapply(arg: FileListDirUpdatedAction): Option[FileListDir] =
      Some(
        arg.currDir
      )
  }

  sealed trait FileListItemCreatedAction extends js.Object {
    val action: String
    val name: String
    val currDir: FileListDir
  }
  object FileListItemCreatedAction {
    val name = "FileListItemCreatedAction"

    def apply(name: String,
              currDir: FileListDir): FileListItemCreatedAction = {
      js.Dynamic.literal(
        action = FileListItemCreatedAction.name,
        name = name,
        currDir = currDir
      ).asInstanceOf[FileListItemCreatedAction]
    }

    def unapply(arg: FileListItemCreatedAction): Option[(String, FileListDir)] =
      Some((
        arg.name,
        arg.currDir
      ))
  }
  
  sealed trait FileListDiskSpaceUpdatedAction extends js.Object {
    val action: String
    val diskSpace: Double
  }
  object FileListDiskSpaceUpdatedAction {
    val name = "FileListDiskSpaceUpdatedAction"

    def apply(diskSpace: Double): FileListDiskSpaceUpdatedAction = {
      js.Dynamic.literal(
        action = name,
        diskSpace = diskSpace
      ).asInstanceOf[FileListDiskSpaceUpdatedAction]
    }

    def unapply(arg: FileListDiskSpaceUpdatedAction): Option[Double] =
      Some(
        arg.diskSpace
      )
  }

  sealed trait FileListSortAction extends js.Object {
    val action: String
    val mode: SortMode
  }
  object FileListSortAction {
    val name = "FileListSortAction"

    def apply(mode: SortMode): FileListSortAction = {
      js.Dynamic.literal(
        action = name,
        mode = mode
      ).asInstanceOf[FileListSortAction]
    }

    def unapply(arg: FileListSortAction): Option[SortMode] =
      Some(
        arg.mode
      )
  }
}
