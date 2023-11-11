package farjs.ui.menu

import farjs.ui.ButtonsPanelProps
import farjs.ui.menu.MenuBar._
import farjs.ui.popup.PopupProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class MenuBarSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  MenuBar.popupComp = "Popup".asInstanceOf[ReactClass]
  MenuBar.buttonsPanel = "ButtonsPanel".asInstanceOf[ReactClass]
  MenuBar.subMenuComp = "SubMenu".asInstanceOf[ReactClass]
  
  private val items = js.Array(
    MenuBarItem("Menu 1", js.Array(
      "Item 1",
      SubMenu.separator,
      "Item 2",
      "Item 3"
    )),
    MenuBarItem("Menu 2", js.Array(
      "Item 4",
      "Item 5"
    )),
    MenuBarItem("Menu 3", js.Array(
      "Item 6"
    ))
  )

  it should "call onClose when onKeypress(F10)" in {
    //given
    val onClose = mockFunction[Unit]
    val props = MenuBarProps(items, (_, _) => (), onClose)
    val comp = testRender(withThemeContext(<(MenuBar())(^.plain := props)()))
    val popupProps = findPopupProps(comp)

    //then
    onClose.expects()

    //when & then
    popupProps.onKeypress.map(_.apply("f10")).getOrElse(false) shouldBe true
  }

  it should "hide sub-menu when onKeypress(escape)" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val renderer = createTestRenderer(withThemeContext(<(MenuBar())(^.plain := props)()))
    val buttonsProps = inside(findComponents(renderer.root, buttonsPanel)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    buttonsProps.actions.head.onAction()
    findComponents(renderer.root, subMenuComp) should not be empty
    val popupProps = findPopupProps(renderer.root)

    //when
    popupProps.onKeypress.map(_.apply("escape")).getOrElse(false) shouldBe true
    
    //then
    findComponents(renderer.root, subMenuComp) shouldBe Nil
  }

  it should "return false if no sub-menu when onKeypress(escape)" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val comp = testRender(withThemeContext(<(MenuBar())(^.plain := props)()))
    val popupProps = findPopupProps(comp)

    //when & then
    popupProps.onKeypress.map(_.apply("escape")).getOrElse(false) shouldBe false
  }

  it should "select sub-menu items when onKeypress(down/up)" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val renderer = createTestRenderer(withThemeContext(<(MenuBar())(^.plain := props)()))
    val buttonsProps = inside(findComponents(renderer.root, buttonsPanel)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    buttonsProps.actions.head.onAction()
    assertNativeComponent(findComponents(renderer.root, subMenuComp).head, <(subMenuComp)(^.assertPlain[SubMenuProps](inside(_) {
      case SubMenuProps(selected, items, _, _, _) =>
        selected shouldBe 0
        items.toList shouldBe List(
          "Item 1",
          SubMenu.separator,
          "Item 2",
          "Item 3"
        )
    }))())

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("up")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 3

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("down")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 0

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("down")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 2

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("up")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 0

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("down")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 2

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("down")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 3

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("down")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 0
  }

  it should "emit keypress(enter) if no sub-menu when onKeypress(down)" in {
    //given
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    val props = MenuBarProps(items, (_, _) => (), () => ())
    val comp = testRender(withThemeContext(<(MenuBar())(^.plain := props)()))
    val popupProps = findPopupProps(comp)

    //then
    onKey.expects("enter", false, false, false)

    //when
    popupProps.onKeypress.map(_.apply("down")).getOrElse(false) shouldBe true

    //cleanup
    process.stdin.removeListener("keypress", listener)
    Succeeded
  }

  it should "return true if no sub-menu when onKeypress(up)" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val comp = testRender(withThemeContext(<(MenuBar())(^.plain := props)()))
    val popupProps = findPopupProps(comp)

    //when & then
    popupProps.onKeypress.map(_.apply("up")).getOrElse(false) shouldBe true
  }
  
  it should "show next/prev sub-menu when onKeypress(right/left)" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val renderer = createTestRenderer(withThemeContext(<(MenuBar())(^.plain := props)()))
    val buttonsProps = inside(findComponents(renderer.root, buttonsPanel)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    buttonsProps.actions.head.onAction()
    findComponents(renderer.root, subMenuComp).head.props.left shouldBe 2

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("left")).getOrElse(false) shouldBe false
    findComponents(renderer.root, subMenuComp).head.props.left shouldBe 22

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("right")).getOrElse(false) shouldBe false
    findComponents(renderer.root, subMenuComp).head.props.left shouldBe 2

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("right")).getOrElse(false) shouldBe false
    findComponents(renderer.root, subMenuComp).head.props.left shouldBe 12

    //when & then
    findPopupProps(renderer.root).onKeypress.map(_.apply("left")).getOrElse(false) shouldBe false
    findComponents(renderer.root, subMenuComp).head.props.left shouldBe 2
  }

  it should "call onAction when onKeypress(enter)" in {
    //given
    val onAction = mockFunction[Int, Int, Unit]
    val props = MenuBarProps(items, onAction, () => ())
    val renderer = createTestRenderer(withThemeContext(<(MenuBar())(^.plain := props)()))
    val buttonsProps = inside(findComponents(renderer.root, buttonsPanel)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    buttonsProps.actions.head.onAction()
    findPopupProps(renderer.root).onKeypress.map(_.apply("down")).getOrElse(false) shouldBe true
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 2

    //then
    var onActionCalled = false
    onAction.expects(0, 2).onCall { (_, _) =>
      onActionCalled = true
    }
    
    //when
    findPopupProps(renderer.root).onKeypress.map(_.apply("enter")).getOrElse(false) shouldBe true

    //then
    eventually(onActionCalled shouldBe true)
  }

  it should "return false if no sub-menu when onKeypress(space)" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val comp = testRender(withThemeContext(<(MenuBar())(^.plain := props)()))
    val popupProps = findPopupProps(comp)

    //when & then
    popupProps.onKeypress.map(_.apply("space")).getOrElse(false) shouldBe false
  }

  it should "return false when onKeypress(other)" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val comp = testRender(withThemeContext(<(MenuBar())(^.plain := props)()))
    val popupProps = findPopupProps(comp)

    //when & then
    popupProps.onKeypress.map(_.apply("other")).getOrElse(false) shouldBe false
  }
  
  it should "call onAction when onClick" in {
    //given
    val onAction = mockFunction[Int, Int, Unit]
    val props = MenuBarProps(items, onAction, () => ())
    val renderer = createTestRenderer(withThemeContext(<(MenuBar())(^.plain := props)()))
    val buttonsProps = inside(findComponents(renderer.root, buttonsPanel)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }
    buttonsProps.actions.head.onAction()
    findComponents(renderer.root, subMenuComp).head.props.selected shouldBe 0

    //then
    var onActionCalled = false
    onAction.expects(0, 2).onCall { (_, _) =>
      onActionCalled = true
    }

    //when
    findComponents(renderer.root, subMenuComp).head.props.onClick(2)

    //then
    eventually(onActionCalled shouldBe true)
  }

  it should "render sub-menu" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())
    val renderer = createTestRenderer(withThemeContext(<(MenuBar())(^.plain := props)()))
    val buttonsProps = inside(findComponents(renderer.root, buttonsPanel)) {
      case List(bp) => bp.props.asInstanceOf[ButtonsPanelProps]
    }

    //when
    buttonsProps.actions.head.onAction()

    //then
    assertNativeComponent(findComponents(renderer.root, subMenuComp).head, <(subMenuComp)(^.assertPlain[SubMenuProps](inside(_) {
      case SubMenuProps(selected, items, top, left, _) =>
        selected shouldBe 0
        items.toList shouldBe List(
          "Item 1",
          SubMenu.separator,
          "Item 2",
          "Item 3"
        )
        top shouldBe 1
        left shouldBe 2
    }))())
  }
  
  it should "render main menu" in {
    //given
    val props = MenuBarProps(items, (_, _) => (), () => ())

    //when
    val result = testRender(withThemeContext(<(MenuBar())(^.plain := props)()))

    //then
    assertMenuBar(result)
  }
  
  private def findPopupProps(root: TestInstance): PopupProps = {
    inside(findComponents(root, popupComp)) {
      case List(popup) => popup.props.asInstanceOf[PopupProps]
    }
  }

  private def assertMenuBar(result: TestInstance): Assertion = {
    val theme = DefaultTheme.popup.menu
    
    assertNativeComponent(result,
      <(popupComp)(^.assertPlain[PopupProps](inside(_) {
        case PopupProps(onClose, focusable, _, _) =>
          onClose.isDefined shouldBe true
          focusable shouldBe js.undefined
      }))(
        <.box(
          ^.rbHeight := 1,
          ^.rbStyle := theme
        )(
          <.box(
            ^.rbWidth := 30,
            ^.rbHeight := 1,
            ^.rbLeft := 2
          )(
            <(buttonsPanel)(^.assertPlain[ButtonsPanelProps](inside(_) {
              case ButtonsPanelProps(top, actions, `theme`, padding, margin) =>
                top shouldBe 0
                actions.map(_.label).toList shouldBe List(
                "Menu 1",
                "Menu 2",
                "Menu 3"
              )
              padding shouldBe 2
              margin shouldBe js.undefined
            }))()
          )
        )
      )
    )
  }
}
