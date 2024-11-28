package farjs.app

import farjs.app.raw.BetterSqlite3Database
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future

trait BaseDBContextSpec extends AsyncTestSpec {

  def withCtx(f: BetterSqlite3Database => Future[Assertion]): Future[Assertion] = {
    BaseDBContextSpec.contextF.flatMap(f)
  }
}

object BaseDBContextSpec {
  
  import scala.concurrent.ExecutionContext.Implicits.global

  private lazy val contextF: Future[BetterSqlite3Database] = {
    val db = new BetterSqlite3Database(":memory:")
    FarjsDBMigrations.apply(db).map(_ => db)
  }
}
