package farjs.app.util

import scommons.react.blessed._
import scommons.react.test._

class ColorPanelSpec extends TestSpec with TestRendererUtils {

  it should "render component" in {
    //given
    val comp = <(ColorPanel())()()

    //when
    val result = testRender(comp)

    //then
    assertNativeComponent(result,
      <.log(
        ^.rbAutoFocus := false,
        ^.rbMouse := true,
        ^.rbTags := true,
        ^.rbScrollbar := true,
        ^.rbScrollable := true,
        ^.rbAlwaysScroll := true,
        ^.content := List(
          "{white-fg}{#000-bg}#000{/}{white-fg}{#005-bg}#005{/}{white-fg}{#008-bg}#008{/}{white-fg}{#00a-bg}#00a{/}{black-fg}{#00c-bg}#00c{/}{black-fg}{#00e-bg}#00e{/}",
          "{white-fg}{#050-bg}#050{/}{white-fg}{#055-bg}#055{/}{white-fg}{#058-bg}#058{/}{white-fg}{#05a-bg}#05a{/}{black-fg}{#05c-bg}#05c{/}{black-fg}{#05e-bg}#05e{/}",
          "{white-fg}{#080-bg}#080{/}{white-fg}{#085-bg}#085{/}{white-fg}{#088-bg}#088{/}{white-fg}{#08a-bg}#08a{/}{black-fg}{#08c-bg}#08c{/}{black-fg}{#08e-bg}#08e{/}",
          "{white-fg}{#0a0-bg}#0a0{/}{white-fg}{#0a5-bg}#0a5{/}{white-fg}{#0a8-bg}#0a8{/}{white-fg}{#0aa-bg}#0aa{/}{black-fg}{#0ac-bg}#0ac{/}{black-fg}{#0ae-bg}#0ae{/}",
          "{black-fg}{#0c0-bg}#0c0{/}{black-fg}{#0c5-bg}#0c5{/}{black-fg}{#0c8-bg}#0c8{/}{black-fg}{#0ca-bg}#0ca{/}{black-fg}{#0cc-bg}#0cc{/}{black-fg}{#0ce-bg}#0ce{/}",
          "{black-fg}{#0e0-bg}#0e0{/}{black-fg}{#0e5-bg}#0e5{/}{black-fg}{#0e8-bg}#0e8{/}{black-fg}{#0ea-bg}#0ea{/}{black-fg}{#0ec-bg}#0ec{/}{black-fg}{#0ee-bg}#0ee{/}",
          "{white-fg}{#500-bg}#500{/}{white-fg}{#505-bg}#505{/}{white-fg}{#508-bg}#508{/}{white-fg}{#50a-bg}#50a{/}{black-fg}{#50c-bg}#50c{/}{black-fg}{#50e-bg}#50e{/}",
          "{white-fg}{#550-bg}#550{/}{white-fg}{#555-bg}#555{/}{white-fg}{#558-bg}#558{/}{white-fg}{#55a-bg}#55a{/}{black-fg}{#55c-bg}#55c{/}{black-fg}{#55e-bg}#55e{/}",
          "{white-fg}{#580-bg}#580{/}{white-fg}{#585-bg}#585{/}{white-fg}{#588-bg}#588{/}{white-fg}{#58a-bg}#58a{/}{black-fg}{#58c-bg}#58c{/}{black-fg}{#58e-bg}#58e{/}",
          "{white-fg}{#5a0-bg}#5a0{/}{white-fg}{#5a5-bg}#5a5{/}{white-fg}{#5a8-bg}#5a8{/}{white-fg}{#5aa-bg}#5aa{/}{black-fg}{#5ac-bg}#5ac{/}{black-fg}{#5ae-bg}#5ae{/}",
          "{black-fg}{#5c0-bg}#5c0{/}{black-fg}{#5c5-bg}#5c5{/}{black-fg}{#5c8-bg}#5c8{/}{black-fg}{#5ca-bg}#5ca{/}{black-fg}{#5cc-bg}#5cc{/}{black-fg}{#5ce-bg}#5ce{/}",
          "{black-fg}{#5e0-bg}#5e0{/}{black-fg}{#5e5-bg}#5e5{/}{black-fg}{#5e8-bg}#5e8{/}{black-fg}{#5ea-bg}#5ea{/}{black-fg}{#5ec-bg}#5ec{/}{black-fg}{#5ee-bg}#5ee{/}",
          "{white-fg}{#800-bg}#800{/}{white-fg}{#805-bg}#805{/}{white-fg}{#808-bg}#808{/}{white-fg}{#80a-bg}#80a{/}{black-fg}{#80c-bg}#80c{/}{black-fg}{#80e-bg}#80e{/}",
          "{white-fg}{#850-bg}#850{/}{white-fg}{#855-bg}#855{/}{white-fg}{#858-bg}#858{/}{white-fg}{#85a-bg}#85a{/}{black-fg}{#85c-bg}#85c{/}{black-fg}{#85e-bg}#85e{/}",
          "{white-fg}{#880-bg}#880{/}{white-fg}{#885-bg}#885{/}{white-fg}{#888-bg}#888{/}{white-fg}{#88a-bg}#88a{/}{black-fg}{#88c-bg}#88c{/}{black-fg}{#88e-bg}#88e{/}",
          "{white-fg}{#8a0-bg}#8a0{/}{white-fg}{#8a5-bg}#8a5{/}{white-fg}{#8a8-bg}#8a8{/}{white-fg}{#8aa-bg}#8aa{/}{black-fg}{#8ac-bg}#8ac{/}{black-fg}{#8ae-bg}#8ae{/}",
          "{black-fg}{#8c0-bg}#8c0{/}{black-fg}{#8c5-bg}#8c5{/}{black-fg}{#8c8-bg}#8c8{/}{black-fg}{#8ca-bg}#8ca{/}{black-fg}{#8cc-bg}#8cc{/}{black-fg}{#8ce-bg}#8ce{/}",
          "{black-fg}{#8e0-bg}#8e0{/}{black-fg}{#8e5-bg}#8e5{/}{black-fg}{#8e8-bg}#8e8{/}{black-fg}{#8ea-bg}#8ea{/}{black-fg}{#8ec-bg}#8ec{/}{black-fg}{#8ee-bg}#8ee{/}",
          "{white-fg}{#a00-bg}#a00{/}{white-fg}{#a05-bg}#a05{/}{white-fg}{#a08-bg}#a08{/}{white-fg}{#a0a-bg}#a0a{/}{black-fg}{#a0c-bg}#a0c{/}{black-fg}{#a0e-bg}#a0e{/}",
          "{white-fg}{#a50-bg}#a50{/}{white-fg}{#a55-bg}#a55{/}{white-fg}{#a58-bg}#a58{/}{white-fg}{#a5a-bg}#a5a{/}{black-fg}{#a5c-bg}#a5c{/}{black-fg}{#a5e-bg}#a5e{/}",
          "{white-fg}{#a80-bg}#a80{/}{white-fg}{#a85-bg}#a85{/}{white-fg}{#a88-bg}#a88{/}{white-fg}{#a8a-bg}#a8a{/}{black-fg}{#a8c-bg}#a8c{/}{black-fg}{#a8e-bg}#a8e{/}",
          "{white-fg}{#aa0-bg}#aa0{/}{white-fg}{#aa5-bg}#aa5{/}{white-fg}{#aa8-bg}#aa8{/}{white-fg}{#aaa-bg}#aaa{/}{black-fg}{#aac-bg}#aac{/}{black-fg}{#aae-bg}#aae{/}",
          "{black-fg}{#ac0-bg}#ac0{/}{black-fg}{#ac5-bg}#ac5{/}{black-fg}{#ac8-bg}#ac8{/}{black-fg}{#aca-bg}#aca{/}{black-fg}{#acc-bg}#acc{/}{black-fg}{#ace-bg}#ace{/}",
          "{black-fg}{#ae0-bg}#ae0{/}{black-fg}{#ae5-bg}#ae5{/}{black-fg}{#ae8-bg}#ae8{/}{black-fg}{#aea-bg}#aea{/}{black-fg}{#aec-bg}#aec{/}{black-fg}{#aee-bg}#aee{/}",
          "{black-fg}{#c00-bg}#c00{/}{black-fg}{#c05-bg}#c05{/}{black-fg}{#c08-bg}#c08{/}{black-fg}{#c0a-bg}#c0a{/}{black-fg}{#c0c-bg}#c0c{/}{black-fg}{#c0e-bg}#c0e{/}",
          "{black-fg}{#c50-bg}#c50{/}{black-fg}{#c55-bg}#c55{/}{black-fg}{#c58-bg}#c58{/}{black-fg}{#c5a-bg}#c5a{/}{black-fg}{#c5c-bg}#c5c{/}{black-fg}{#c5e-bg}#c5e{/}",
          "{black-fg}{#c80-bg}#c80{/}{black-fg}{#c85-bg}#c85{/}{black-fg}{#c88-bg}#c88{/}{black-fg}{#c8a-bg}#c8a{/}{black-fg}{#c8c-bg}#c8c{/}{black-fg}{#c8e-bg}#c8e{/}",
          "{black-fg}{#ca0-bg}#ca0{/}{black-fg}{#ca5-bg}#ca5{/}{black-fg}{#ca8-bg}#ca8{/}{black-fg}{#caa-bg}#caa{/}{black-fg}{#cac-bg}#cac{/}{black-fg}{#cae-bg}#cae{/}",
          "{black-fg}{#cc0-bg}#cc0{/}{black-fg}{#cc5-bg}#cc5{/}{black-fg}{#cc8-bg}#cc8{/}{black-fg}{#cca-bg}#cca{/}{black-fg}{#ccc-bg}#ccc{/}{black-fg}{#cce-bg}#cce{/}",
          "{black-fg}{#ce0-bg}#ce0{/}{black-fg}{#ce5-bg}#ce5{/}{black-fg}{#ce8-bg}#ce8{/}{black-fg}{#cea-bg}#cea{/}{black-fg}{#cec-bg}#cec{/}{black-fg}{#cee-bg}#cee{/}",
          "{black-fg}{#e00-bg}#e00{/}{black-fg}{#e05-bg}#e05{/}{black-fg}{#e08-bg}#e08{/}{black-fg}{#e0a-bg}#e0a{/}{black-fg}{#e0c-bg}#e0c{/}{black-fg}{#e0e-bg}#e0e{/}",
          "{black-fg}{#e50-bg}#e50{/}{black-fg}{#e55-bg}#e55{/}{black-fg}{#e58-bg}#e58{/}{black-fg}{#e5a-bg}#e5a{/}{black-fg}{#e5c-bg}#e5c{/}{black-fg}{#e5e-bg}#e5e{/}",
          "{black-fg}{#e80-bg}#e80{/}{black-fg}{#e85-bg}#e85{/}{black-fg}{#e88-bg}#e88{/}{black-fg}{#e8a-bg}#e8a{/}{black-fg}{#e8c-bg}#e8c{/}{black-fg}{#e8e-bg}#e8e{/}",
          "{black-fg}{#ea0-bg}#ea0{/}{black-fg}{#ea5-bg}#ea5{/}{black-fg}{#ea8-bg}#ea8{/}{black-fg}{#eaa-bg}#eaa{/}{black-fg}{#eac-bg}#eac{/}{black-fg}{#eae-bg}#eae{/}",
          "{black-fg}{#ec0-bg}#ec0{/}{black-fg}{#ec5-bg}#ec5{/}{black-fg}{#ec8-bg}#ec8{/}{black-fg}{#eca-bg}#eca{/}{black-fg}{#ecc-bg}#ecc{/}{black-fg}{#ece-bg}#ece{/}",
          "{black-fg}{#ee0-bg}#ee0{/}{black-fg}{#ee5-bg}#ee5{/}{black-fg}{#ee8-bg}#ee8{/}{black-fg}{#eea-bg}#eea{/}{black-fg}{#eec-bg}#eec{/}{black-fg}{#eee-bg}#eee{/}",
          "",
          "{white-fg}{#000-bg}#000{/}{white-fg}{#333-bg}#333{/}{white-fg}{#555-bg}#555{/}{white-fg}{#888-bg}#888{/}{white-fg}{#aaa-bg}#aaa{/}{black-fg}{#ccc-bg}#ccc{/}{black-fg}{#fff-bg}#fff{/}"
        ).mkString("\n")
      )()
    )
  }
}
