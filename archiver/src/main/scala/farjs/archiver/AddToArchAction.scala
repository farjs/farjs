package farjs.archiver

sealed trait AddToArchAction

object AddToArchAction {

  case object Add extends AddToArchAction
  case object Copy extends AddToArchAction
  case object Move extends AddToArchAction
}
