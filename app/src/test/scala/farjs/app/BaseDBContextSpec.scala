package farjs.app

import farjs.app.raw.BetterSqlite3WebSQL
import farjs.domain._
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec
import scommons.websql.Database

import scala.concurrent.Future

trait BaseDBContextSpec extends AsyncTestSpec {

  def withCtx(f: FarjsDBContext => Future[Assertion]): Future[Assertion] = {
    BaseDBContextSpec.contextF.flatMap(f)
  }
}

object BaseDBContextSpec {
  
  import scala.concurrent.ExecutionContext.Implicits.global

  private lazy val contextF: Future[FarjsDBContext] = {
    val db = BetterSqlite3WebSQL.openDatabase(":memory:")
    
    FarjsDBMigrations.apply(db).map { _ =>
      new FarjsDBContext(new Database(db))
    }
  }
}
