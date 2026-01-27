package farjs.filelist.util

import farjs.filelist.util.ChildProcess._
import scommons.nodejs.{Buffer, fs, os, path, raw}
import org.scalatest.Succeeded
import scommons.nodejs.stream.Readable
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class ChildProcessSpec extends AsyncTestSpec {
  
  private type Callback = js.Function3[js.Error, js.Object, js.Object, Unit]
  
  private val defaultOpts: ChildProcessOptions = new ChildProcessOptions {
    override val windowsHide = true
  }

  it should "exec cd .." in {
    //when
    val (_, resultF) = child_process.exec("cd ..", Some(defaultOpts))

    //then
    resultF.map { case (stdout, stderr) =>
      stdout.toString shouldBe ""
      stderr.toString shouldBe ""
    }
  }
  
  it should "fail if unknown command when exec 123" in {
    //when
    val (_, resultF) = child_process.exec("123", Some(defaultOpts))

    //then
    resultF.failed.map(inside(_) { case js.JavaScriptException(err: js.Error) =>
      val resultError = err.asInstanceOf[js.Dynamic]
      resultError.code.asInstanceOf[Int] should be > 0
      resultError.stdout.toString shouldBe ""
      resultError.stderr.toString should not be empty
    })
  }
  
  it should "call native exec and return failed future" in {
    //given
    val native = raw.ChildProcess.asInstanceOf[js.Dynamic]
    val nativeExec = native.exec
    val execMock =
      mockFunction[String, js.UndefOr[ChildProcessOptions], js.UndefOr[Callback], raw.ChildProcess]
    native.exec = execMock
    val command = "some command"
    val expectedError = js.Error("some error")
    val expectedStdout = new js.Object {}
    val expectedStderr = new js.Object {}
    val expectedResult = new js.Object {}.asInstanceOf[raw.ChildProcess]
    
    //then
    execMock.expects(*, *, *).onCall { (resCommand, resOptions, callback) =>
      resCommand shouldBe command
      resOptions.isEmpty shouldBe true
      callback.get(expectedError, expectedStdout, expectedStderr)
      
      expectedResult
    }

    //when
    val (child, resultF) = child_process.exec(command)

    //then
    child should be theSameInstanceAs expectedResult

    resultF.failed.map(inside(_) { case js.JavaScriptException(err: js.Error) =>
      err should be theSameInstanceAs expectedError

      val resultError = err.asInstanceOf[js.Dynamic]
      resultError.stdout should be theSameInstanceAs expectedStdout
      resultError.stderr should be theSameInstanceAs expectedStderr
      
      //cleanup
      native.exec = nativeExec
      Succeeded
    })
  }
  
  it should "call native exec and return successful future" in {
    //given
    val native = raw.ChildProcess.asInstanceOf[js.Dynamic]
    val nativeExec = native.exec
    val execMock =
      mockFunction[String, js.UndefOr[ChildProcessOptions], js.UndefOr[Callback], raw.ChildProcess]
    native.exec = execMock
    val command = "some command"
    val options = new ChildProcessOptions {}
    val expectedStdout = new js.Object {}
    val expectedStderr = new js.Object {}
    val expectedResult = new js.Object {}.asInstanceOf[raw.ChildProcess]
    
    //then
    execMock.expects(*, *, *).onCall { (resCommand, resOptions, callback) =>
      resCommand shouldBe command
      resOptions should be theSameInstanceAs options
      callback.get(null, expectedStdout, expectedStderr)
      
      expectedResult
    }

    //when
    val (child, resultF) = child_process.exec(command, Some(options))

    //then
    child should be theSameInstanceAs expectedResult

    resultF.map { case (stdout, stderr) =>
      stdout should be theSameInstanceAs expectedStdout
      stderr should be theSameInstanceAs expectedStderr
      
      //cleanup
      native.exec = nativeExec
      Succeeded
    }
  }
  
  it should "call native spawn with args and options" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "scommons-nodejs-"))
    val file = path.join(tmpDir, "example.txt")
    val expectedContent = "hello, world!!!"
    fs.writeFileSync(file, expectedContent)
    val stdoutStream = fs.createReadStream(file)
    val onceMock = mockFunction[String, js.Function, raw.EventEmitter]
    val rawProcess = literal(
      "stdout" -> stdoutStream,
      "stderr" -> Readable.from(Buffer.from("")),
      "once" -> onceMock
    ).asInstanceOf[raw.ChildProcess]

    val native = raw.ChildProcess.asInstanceOf[js.Dynamic]
    val nativeSpawn = native.spawn
    val spawnMock =
      mockFunction[String, js.UndefOr[js.Array[String]], js.UndefOr[ChildProcessOptions], raw.ChildProcess]
    native.spawn = spawnMock
    val command = "some command"
    val args = List("some", "args")
    val options = new ChildProcessOptions {}
    
    //then
    spawnMock.expects(*, *, *).onCall { (resCommand, resArgs, resOptions) =>
      resCommand shouldBe command
      resArgs.get.toList shouldBe args
      resOptions should be theSameInstanceAs options
      rawProcess
    }
    var exitCallback: js.Function1[Int, js.Any] = null
    onceMock.expects("exit", *).onCall { (_, callback) =>
      exitCallback = callback.asInstanceOf[js.Function1[Int, js.Any]]
      rawProcess
    }
    onceMock.expects("error", *)
    
    //when
    val resultF = child_process.spawn(command, args, Some(options))

    //then
    (for {
      result <- resultF
      output <- loop(result.stdout, "")
      _ = exitCallback(0)
      _ <- result.exitP.toFuture
    } yield {
      result.child should be theSameInstanceAs rawProcess
      result.stdout.readable shouldBe stdoutStream
      output shouldBe expectedContent
    }).andThen {
      case _ =>
        //cleanup
        native.spawn = nativeSpawn
        fs.unlinkSync(file)
        fs.existsSync(file) shouldBe false
        fs.rmdirSync(tmpDir)
        fs.existsSync(tmpDir) shouldBe false
    }
  }
  
  it should "call native spawn without args or options" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "scommons-nodejs-"))
    val file = path.join(tmpDir, "example.txt")
    val expectedContent = "hello, world!!!"
    fs.writeFileSync(file, expectedContent)
    val stdoutStream = fs.createReadStream(file)
    val onceMock = mockFunction[String, js.Function, raw.EventEmitter]
    val rawProcess = literal(
      "stdout" -> stdoutStream,
      "stderr" -> Readable.from(Buffer.from("")),
      "once" -> onceMock
    ).asInstanceOf[raw.ChildProcess]

    val native = raw.ChildProcess.asInstanceOf[js.Dynamic]
    val nativeSpawn = native.spawn
    val spawnMock =
      mockFunction[String, js.UndefOr[js.Array[String]], js.UndefOr[ChildProcessOptions], raw.ChildProcess]
    native.spawn = spawnMock
    val command = "some command"
    
    //then
    spawnMock.expects(*, *, *).onCall { (resCommand, resArgs, resOptions) =>
      resCommand shouldBe command
      resArgs.isEmpty shouldBe true
      resOptions.isEmpty shouldBe true
      
      rawProcess
    }
    var exitCallback: js.Function1[Int, js.Any] = null
    onceMock.expects("exit", *).onCall { (_, callback) =>
      exitCallback = callback.asInstanceOf[js.Function1[Int, js.Any]]
      rawProcess
    }
    onceMock.expects("error", *)

    //when
    val resultF = child_process.spawn(command)

    //then
    (for {
      result <- resultF
      output <- loop(result.stdout, "")
      _ = exitCallback(0)
      _ <- result.exitP.toFuture
    } yield {
      result.child should be theSameInstanceAs rawProcess
      result.stdout.readable shouldBe stdoutStream
      output shouldBe expectedContent
    }).andThen {
      case _ =>
        //cleanup
        native.spawn = nativeSpawn
        fs.unlinkSync(file)
        fs.existsSync(file) shouldBe false
        fs.rmdirSync(tmpDir)
        fs.existsSync(tmpDir) shouldBe false
    }
  }

  def loop(reader: StreamReader, result: String): Future[String] = {
    reader.readNextBytes(5).toFuture.map(_.toOption).flatMap {
      case None => Future.successful(result)
      case Some(content) =>
        loop(reader, result + content.toString)
    }
  }
}
