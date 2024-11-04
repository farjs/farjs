package farjs.fs.popups

import farjs.filelist.history.HistoryProviderSpec.withHistoryProvider
import farjs.filelist.history._
import farjs.fs.FSFoldersHistory.foldersHistoryKind
import farjs.fs.popups.FoldersHistoryPopup._
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FoldersHistoryPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FoldersHistoryPopup.listPopup = "ListPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class HistoryMocks {
    val get = mockFunction[HistoryKind, js.Promise[HistoryService]]
    val getAll = mockFunction[js.Promise[js.Array[History]]]

    val service = new MockHistoryService(
      getAllMock = getAll
    )
    val provider = new MockHistoryProvider(
      getMock = get
    )
  }

  it should "call onChangeDir when onAction" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val props = getFoldersHistoryPopupProps(onChangeDir = onChangeDir)
    val historyMocks = new HistoryMocks
    val itemsF = js.Promise.resolve[js.Array[History]](js.Array(
      History("item 1", js.undefined),
      History("item 2", js.undefined)
    ))
    var getAllCalled = false
    historyMocks.get.expects(foldersHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }
    
    val result = createTestRenderer(withHistoryProvider(
      <(FoldersHistoryPopup())(^.wrapped := props)(), historyMocks.provider
    )).root

    eventually(getAllCalled shouldBe true).map { _ =>
      val popup = inside(findComponents(result, listPopup)) {
        case List(c) => c.props.asInstanceOf[ListPopupProps]
      }

      //then
      onChangeDir.expects("item 2")
      
      //when
      popup.onAction(1)

      Succeeded
    }
  }
  
  it should "render popup" in {
    //given
    val props = getFoldersHistoryPopupProps()
    val historyMocks = new HistoryMocks
    val items = List.fill(20)("item")
    val itemsF = js.Promise.resolve[js.Array[History]](
      js.Array(items.map(i => History(i, js.undefined)): _*)
    )
    var getAllCalled = false
    historyMocks.get.expects(foldersHistoryKind)
      .returning(js.Promise.resolve[HistoryService](historyMocks.service))
    historyMocks.getAll.expects().onCall { () =>
      getAllCalled = true
      itemsF
    }
    
    //when
    val result = createTestRenderer(withHistoryProvider(
      <(FoldersHistoryPopup())(^.wrapped := props)(), historyMocks.provider
    )).root

    //then
    result.children.toList should be (empty)
    eventually(getAllCalled shouldBe true).map { _ =>
      assertFoldersHistoryPopup(result, items)
    }
  }
  
  private def getFoldersHistoryPopupProps(onChangeDir: String => Unit = _ => ()): FoldersHistoryPopupProps = {
    FoldersHistoryPopupProps(
      onChangeDir = onChangeDir,
      onClose = () => ()
    )
  }

  private def assertFoldersHistoryPopup(result: TestInstance, items: List[String]): Assertion = {
    assertComponents(result.children, List(
      <(listPopup)(^.assertPlain[ListPopupProps](inside(_) {
        case ListPopupProps(
          title,
          resItems,
          _,
          _,
          selected,
          _,
          _,
          footer,
          textPaddingLeft,
          textPaddingRight,
          itemWrapPrefixLen
        ) =>
          title shouldBe "Folders history"
          resItems.toList shouldBe items
          selected shouldBe (items.length - 1)
          footer shouldBe js.undefined
          textPaddingLeft shouldBe js.undefined
          textPaddingRight shouldBe js.undefined
          itemWrapPrefixLen shouldBe js.undefined
      }))()
    ))
  }
}
