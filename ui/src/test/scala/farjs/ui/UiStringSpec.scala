package farjs.ui

import scommons.nodejs.test.TestSpec

class UiStringSpec extends TestSpec {

  it should "return current str if same width when ensureWidth" in {
    //given
    val str = "test"

    //when & then
    UiString(str).ensureWidth(4, ' ') should be theSameInstanceAs str
  }

  it should "pad to width if > strWidth when ensureWidth" in {
    //when & then
    UiString("Валютный").ensureWidth(8, ' ') shouldBe "Валютный"
    UiString("Валютный").ensureWidth(9, ' ') shouldBe "Валютный "
    UiString("Валютный").ensureWidth(10, ' ') shouldBe "Валютный  "
  }

  it should "cut to width if < strWidth when ensureWidth" in {
    //when & then
    UiString("Валютный").ensureWidth(7, ' ') shouldBe "Валютны"
    UiString("Валютный").ensureWidth(8, ' ') shouldBe "Валютный"
    UiString("Валютный2").ensureWidth(8, ' ') shouldBe "Валютный"
    UiString("\uD800\uDC002").ensureWidth(1, ' ') shouldBe "\uD800\uDC00"
  }

  it should "cut and pad to width if at double-width char when ensureWidth" in {
    //given
    val str = "te\uff01t"
    str.length shouldBe 4
    UiString(str).strWidth shouldBe 5
    
    //when & then
    UiString(str).ensureWidth(6, ' ') shouldBe "te\uff01t "
    UiString(str).ensureWidth(4, ' ') shouldBe "te\uff01"
    UiString(str).ensureWidth(3, ' ') shouldBe "te "
  }

  it should "return str width when strWidth" in {
    //given
    val str = "Валютный"
    str.length shouldBe 9

    //when & then
    UiString(str).strWidth shouldBe 8
    UiString("\t").strWidth shouldBe 8
    UiString("\u0000\u001b\r\n").strWidth shouldBe 0
  }
}
