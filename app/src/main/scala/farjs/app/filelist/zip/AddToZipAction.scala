package farjs.app.filelist.zip

sealed trait AddToZipAction

object AddToZipAction {

  case object Add extends AddToZipAction
  case object Copy extends AddToZipAction
  case object Move extends AddToZipAction
}
