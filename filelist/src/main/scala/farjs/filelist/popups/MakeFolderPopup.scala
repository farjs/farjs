package farjs.filelist.popups

import farjs.filelist.FileListServices
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class MakeFolderPopupProps(multiple: Boolean,
                                onOk: (String, Boolean) => Unit,
                                onCancel: () => Unit)

object MakeFolderPopup extends FunctionComponent[MakeFolderPopupProps] {

  private[popups] var modalComp: UiComponent[ModalProps] = Modal
  private[popups] var textLineComp: ReactClass = TextLine
  private[popups] var comboBoxComp: UiComponent[ComboBoxProps] = ComboBox
  private[popups] var horizontalLineComp: ReactClass = HorizontalLine
  private[popups] var checkBoxComp: UiComponent[CheckBoxProps] = CheckBox
  private[popups] var buttonsPanelComp: ReactClass = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])
    val props = compProps.wrapped
    val (folderName, setFolderName) = useState("")
    val (multiple, setMultiple) = useState(props.multiple)
    val (width, height) = (75, 10)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular

    val onOk: js.Function0[Unit] = { () =>
      if (folderName.nonEmpty) {
        props.onOk(folderName, multiple)
      }
    }
    
    val actions = js.Array(
      ButtonsPanelAction("[ OK ]", onOk),
      ButtonsPanelAction("[ Cancel ]", props.onCancel)
    )

    useLayoutEffect({ () =>
      services.mkDirsHistory.getAll.map { items =>
        val itemsReversed = items.reverse
        itemsReversed.headOption.foreach { last =>
          setFolderName(last)
        }
        setItems(Some(js.Array(itemsReversed: _*)))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(modalComp())(^.plain := ModalProps("Make Folder", width, height, theme, props.onCancel))(
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.left,
          left = contentLeft,
          top = 1,
          width = contentWidth,
          text = "Create the folder",
          style = theme,
          padding = 0
        ))(),
        <(comboBoxComp())(^.plain := ComboBoxProps(
          left = contentLeft,
          top = 2,
          width = contentWidth,
          items = items,
          value = folderName,
          onChange = { value =>
            setFolderName(value)
          },
          onEnter = onOk
        ))(),
        
        <(horizontalLineComp)(^.plain := HorizontalLineProps(
          left = 0,
          top = 3,
          length = width - paddingHorizontal * 2,
          lineCh = SingleChars.horizontal,
          style = theme,
          startCh = DoubleChars.leftSingle,
          endCh = DoubleChars.rightSingle
        ))(),
        <(checkBoxComp())(^.plain := CheckBoxProps(
          left = contentLeft,
          top = 4,
          value = multiple,
          label = "Process multiple names",
          style = theme,
          onChange = { () =>
            setMultiple(!multiple)
          }
        ))(),
        
        <(horizontalLineComp)(^.plain := HorizontalLineProps(
          left = 0,
          top = 5,
          length = width - paddingHorizontal * 2,
          lineCh = SingleChars.horizontal,
          style = theme,
          startCh = DoubleChars.leftSingle,
          endCh = DoubleChars.rightSingle
        ))(),
        <(buttonsPanelComp)(^.plain := ButtonsPanelProps(
          top = 6,
          actions = actions,
          style = theme,
          margin = 2
        ))()
      )
    }.orNull
  }
}
