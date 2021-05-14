package farjs.filelist

import farjs.filelist.FileListDisk._
import scommons.nodejs.test.TestSpec

class FileListDiskSpec extends TestSpec {

  it should "parse df output" in {
    //when
    val results = fromDfCommand(
      """Filesystem   1024-blocks      Used Available Capacity  Mounted on
        |/dev/disk1s1   244912536 202577024  40612004    84%    /
        |/dev/disk1s4   244912536   1048632  40612004     3%    /private/var/vm
        |/dev/disk1s3   244912536    498596  40612004     2%    /Volumes/Recovery
        |""".stripMargin)
    
    //then
    results shouldBe List(
      FileListDisk("/", size = 250790436864.0, free = 41586692096.0, "/"),
      FileListDisk("/private/var/vm", size = 250790436864.0, free = 41586692096.0, "/private/var/vm"),
      FileListDisk("/Volumes/Recovery", size = 250790436864.0, free = 41586692096.0, "/Volumes/Recovery")
    )
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
    results shouldBe List(
      FileListDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FileListDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FileListDisk("E:", size = 0.0, free = 0.0, "")
    )
  }
}
