package farjs.ui

import scommons.nodejs.test.TestSpec

class UiStringSpec extends TestSpec {

  it should "return str width when strWidth" in {
    //given
    val str = "Валютный"
    str.length shouldBe 9

    //when & then
    UiString(str).strWidth shouldBe 8
    UiString("\t").strWidth shouldBe 8
    UiString("\u0000\u001b\r\n").strWidth shouldBe 0
  }

  it should "return current str when toString" in {
    //given
    val str = "test"

    //when & then
    UiString(str).toString should be theSameInstanceAs str
  }

  it should "return part of str when slice" in {
    //given
    val str = "abcd"

    //when & then
    UiString(str).slice(0, 4) should be theSameInstanceAs str
    UiString(str).slice(-1, 5) should be theSameInstanceAs str
    UiString(str).slice(0, -1) shouldBe ""
    UiString(str).slice(3, 2) shouldBe ""
    UiString(str).slice(3, 3) shouldBe ""
    UiString(str).slice(3, 4) shouldBe "d"
    UiString("").slice(0, 1) shouldBe ""
  }

  it should "handle combining chars when slice" in {
    //when & then
    UiString("Валютный").slice(0, 8) shouldBe "Валютный"
    UiString("Валютный").slice(7, 8) shouldBe "й"
    UiString("Валютный").slice(6, 7) shouldBe "ы"
    UiString("й").slice(0, 1) shouldBe "й"
    UiString("1й").slice(0, 1) shouldBe "1"
    UiString("1й").slice(0, 2) shouldBe "1й"
    UiString("й2").slice(0, 2) shouldBe "й2"
    UiString("й2").slice(0, 1) shouldBe "й"
    UiString("й2").slice(1, 2) shouldBe "2"
  }

  it should "handle surrogate chars when slice" in {
    //when & then
    UiString("\uD800\uDC002").slice(0, 2) shouldBe "\uD800\uDC002"
    UiString("\uD800\uDC002").slice(0, 1) shouldBe "\uD800\uDC00"
    UiString("\uD800\uDC002").slice(1, 2) shouldBe "2"
  }

  it should "handle double-wide chars when slice" in {
    //when & then
    UiString("te\uff012").slice(0, 5) shouldBe "te\uff012"
    UiString("te\uff012").slice(0, 4) shouldBe "te\uff01"
    UiString("te\uff012").slice(0, 3) shouldBe "te"
    UiString("te\uff012").slice(0, 2) shouldBe "te"
    UiString("te\uff012").slice(0, 1) shouldBe "t"
    UiString("te\uff012").slice(1, 2) shouldBe "e"
    UiString("te\uff012").slice(1, 3) shouldBe "e"
    UiString("te\uff012").slice(2, 3) shouldBe ""
    UiString("te\uff012").slice(2, 4) shouldBe "\uff01"
    UiString("te\uff012").slice(3, 4) shouldBe ""
    UiString("te\uff012").slice(4, 5) shouldBe "2"
    UiString("1\uff01\uff022").slice(1, 2) shouldBe ""
    UiString("1\uff01\uff022").slice(1, 3) shouldBe "\uff01"
    UiString("1\uff01\uff022").slice(1, 4) shouldBe "\uff01"
    UiString("1\uff01\uff022").slice(1, 5) shouldBe "\uff01\uff02"
    UiString("1\uff01\uff022").slice(2, 3) shouldBe ""
    UiString("1\uff01\uff022").slice(2, 4) shouldBe "\uff01"
    UiString("1\uff01\uff022").slice(2, 5) shouldBe "\uff01"
    UiString("1\uff01\uff022").slice(3, 5) shouldBe "\uff02"
  }

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
}
