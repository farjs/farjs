package farjs.fs

import farjs.fs.FSDisk._
import farjs.fs.FSDiskSpec.assertFSDisks
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.nodejs.test.TestSpec

class FSDiskSpec extends TestSpec {

  it should "parse df output" in {
    //when
    val results = fromDfCommand(
      """Filesystem   1024-blocks      Used Available Capacity  Mounted on
        |/dev/disk1s1   244912536 202577024  40612004    84%    /
        |/dev/disk1s4   244912536   1048632  40612004     3%    /private/var/vm
        |/dev/disk1s3   244912536    498596  40612004     2%    /Volumes/Recovery
        |""".stripMargin)
    
    //then
    assertFSDisks(results, List(
      FSDisk("/", size = 250790436864.0, free = 41586692096.0, "/"),
      FSDisk("/private/var/vm", size = 250790436864.0, free = 41586692096.0, "/private/var/vm"),
      FSDisk("/Volumes/Recovery", size = 250790436864.0, free = 41586692096.0, "/Volumes/Recovery")
    ))
  }

  it should "parse wmic logicaldisk output" in {
    //when
    val results = fromWmicLogicalDisk(
      """Caption  FreeSpace     Size          VolumeName
        |C:       81697124352   156595318784  SYSTEM
        |D:       352966430720  842915639296  DATA
        |E:
        |""".stripMargin)
    
    //then
    assertFSDisks(results, List(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    ))
  }
}

object FSDiskSpec {

  def assertFSDisks(result: Seq[FSDisk], expected: List[FSDisk])(implicit position: Position): Assertion = {
    result.size shouldBe expected.size
    result.zip(expected).foreach { case (res, item) =>
      assertFSDisk(res, item)
    }
    Succeeded
  }

  def assertFSDisk(result: FSDisk, expected: FSDisk)(implicit position: Position): Assertion = {
    inside(result) {
      case FSDisk(root, size, free, name) =>
        root shouldBe expected.root
        size shouldBe expected.size
        free shouldBe expected.free
        name shouldBe expected.name
    }
  }
}
