package farjs.filelist.sort

import farjs.filelist.api.FileListItem
import farjs.filelist.sort.FileListSort.{nextOrdering, sortItems}
import farjs.filelist.sort.SortMode._
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.react.test.TestSpec

class FileListSortSpec extends TestSpec {

  private val item0 = FileListItem.copy(FileListItem("item.bin"))(size = 2, atimeMs = 1, mtimeMs = 4, ctimeMs = 2)
  private val item1 = FileListItem.copy(FileListItem("Item.bin"))(size = 1, atimeMs = 1, mtimeMs = 3, ctimeMs = 2)
  private val item2 = FileListItem.copy(FileListItem("item2.BIN"))(size = 4, atimeMs = 2, mtimeMs = 1, ctimeMs = 3)
  private val item3 = FileListItem.copy(FileListItem("Item3.zip"))(size = 3, atimeMs = 4, mtimeMs = 2, ctimeMs = 4)
  private val item4 = FileListItem.copy(FileListItem("item4.ZIP"))(size = 3, atimeMs = 3, mtimeMs = 2, ctimeMs = 1)
  private val items = List(item0, item1, item2, item3, item4)
  private val itemsR = items.reverse
  
  it should "return next ordering when nextOrdering" in {
    //when & then
    nextOrdering(FileListSort(Name, asc = false), Name) shouldBe true
    nextOrdering(FileListSort(Name, asc = true), Name) shouldBe false
    nextOrdering(FileListSort(Unsorted, asc = false), Name) shouldBe true
    nextOrdering(FileListSort(Unsorted, asc = true), Name) shouldBe true
    nextOrdering(FileListSort(Name, asc = false), Extension) shouldBe true
    nextOrdering(FileListSort(Name, asc = true), Extension) shouldBe true
    nextOrdering(FileListSort(Name, asc = false), Unsorted) shouldBe true
    nextOrdering(FileListSort(Name, asc = true), Unsorted) shouldBe true
    nextOrdering(FileListSort(Extension, asc = false), ModificationTime) shouldBe false
    nextOrdering(FileListSort(Extension, asc = true), ModificationTime) shouldBe false
    nextOrdering(FileListSort(Name, asc = false), Size) shouldBe false
    nextOrdering(FileListSort(Name, asc = true), Size) shouldBe false
    nextOrdering(FileListSort(ModificationTime, asc = false), CreationTime) shouldBe false
    nextOrdering(FileListSort(ModificationTime, asc = true), CreationTime) shouldBe false
    nextOrdering(FileListSort(Unsorted, asc = false), AccessTime) shouldBe false
    nextOrdering(FileListSort(Unsorted, asc = true), AccessTime) shouldBe false
  }

  it should "sort items when sortItems" in {
    //given
    item1.name shouldBe "Item.bin"
    item1.nameNormalized() shouldBe "item.bin"
    item2.ext() shouldBe "BIN"
    item2.extNormalized() shouldBe "bin"
    
    //when & then
    sortItems(items, Name) shouldBe List(item1, item0, item2, item3, item4)
    sortItems(itemsR, Name) shouldBe List(item1, item0, item2, item3, item4)
    sortItems(items, Extension) shouldBe List(item2, item1, item0, item4, item3)
    sortItems(itemsR, Extension) shouldBe List(item2, item1, item0, item4, item3)
    sortItems(items, ModificationTime) shouldBe List(item2, item3, item4, item1, item0)
    sortItems(itemsR, ModificationTime) shouldBe List(item2, item3, item4, item1, item0)
    sortItems(items, Size) shouldBe List(item1, item0, item3, item4, item2)
    sortItems(itemsR, Size) shouldBe List(item1, item0, item3, item4, item2)
    sortItems(items, Unsorted) shouldBe items
    sortItems(itemsR, Unsorted) shouldBe itemsR
    sortItems(items, CreationTime) shouldBe List(item4, item1, item0, item2, item3)
    sortItems(itemsR, CreationTime) shouldBe List(item4, item1, item0, item2, item3)
    sortItems(items, AccessTime) shouldBe List(item1, item0, item2, item4, item3)
    sortItems(itemsR, AccessTime) shouldBe List(item1, item0, item2, item4, item3)
  }
}

object FileListSortSpec {

  def assertFileListSort(result: FileListSort, expected: FileListSort): Assertion = {
    inside(result) {
      case FileListSort(mode, asc) =>
        mode shouldBe expected.mode
        asc shouldBe expected.asc
    }
  }  
}
