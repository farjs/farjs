package farjs.filelist.quickview

import farjs.filelist._
import farjs.filelist.stack.PanelStack
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.ReactClass
import scommons.react.redux.Dispatch
import scommons.react.test.TestSpec

class QuickViewPluginSpec extends TestSpec {
  
  private def createQuickViewPlugin(actions: FileListActions): QuickViewPlugin = {
    new QuickViewPlugin(actions) {
      override def apply(): ReactClass = "QuickViewPlugin".asInstanceOf[ReactClass]
    }
  }

  it should "define uiComponent and triggerKey" in {
    //given
    val actions = mock[FileListActions]
    val plugin = createQuickViewPlugin(actions)
    
    //when & then
    plugin.uiComponent shouldBe QuickViewPanel
    
    //when & then
    plugin.triggerKey shouldBe "C-q"
  }
  
  it should "remove plugin from left panel when onTrigger" in {
    //given
    val actions = mock[FileListActions]
    val plugin = createQuickViewPlugin(actions)
    val leftStack = mock[PanelStack]
    val rightStack = mock[PanelStack]
    (leftStack.peek _).expects().returning(Some((plugin(), null)))
    
    //then
    (leftStack.pop _).expects()

    //when
    plugin.onTrigger(isRight = false, leftStack, rightStack)
  }
  
  it should "remove plugin from right panel when onTrigger" in {
    //given
    val actions = mock[FileListActions]
    val plugin = createQuickViewPlugin(actions)
    val leftStack = mock[PanelStack]
    val rightStack = mock[PanelStack]
    (leftStack.peek _).expects().returning(None)
    (rightStack.peek _).expects().returning(Some((plugin(), null)))
    
    //then
    (rightStack.pop _).expects()

    //when
    plugin.onTrigger(isRight = false, leftStack, rightStack)
  }
  
  it should "add plugin to left panel when onTrigger" in {
    //given
    val actions = mock[FileListActions]
    val plugin = createQuickViewPlugin(actions)
    val leftStack = mock[PanelStack]
    val rightStack = mock[PanelStack]
    (leftStack.peek _).expects().returning(None)
    (rightStack.peek _).expects().returning(Some(("testComp".asInstanceOf[ReactClass], null)))
    
    //then
    (leftStack.push(_: ReactClass, _: QuickViewParams)).expects(plugin(), QuickViewParams())

    //when
    plugin.onTrigger(isRight = true, leftStack, rightStack)
  }
  
  it should "add plugin to right panel when onTrigger" in {
    //given
    val actions = mock[FileListActions]
    val plugin = createQuickViewPlugin(actions)
    val leftStack = mock[PanelStack]
    val rightStack = mock[PanelStack]
    (leftStack.peek _).expects().returning(None)
    (rightStack.peek _).expects().returning(Some(("testComp".asInstanceOf[ReactClass], null)))
    
    //then
    (rightStack.push(_: ReactClass, _: QuickViewParams)).expects(plugin(), QuickViewParams())

    //when
    plugin.onTrigger(isRight = false, leftStack, rightStack)
  }
  
  it should "map state to props" in {
    //given
    val actions = mock[FileListActions]
    val props = mock[Props[Unit]]
    val plugin = createQuickViewPlugin(actions)
    val dispatch = mock[Dispatch]
    val fileListsState = mock[FileListsStateDef]
    val state = mock[FileListsGlobalState]
    (state.fileListsState _).expects().returning(fileListsState)

    //when
    val result = plugin.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case QuickViewPanelProps(disp, resActions, data) =>
      disp shouldBe dispatch
      resActions shouldBe resActions
      data shouldBe fileListsState
    }
  }
}
