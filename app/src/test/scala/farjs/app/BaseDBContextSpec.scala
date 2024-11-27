package farjs.app

import farjs.app.raw.{BetterSqlite3Database, BetterSqlite3WebSQL}
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js

trait BaseDBContextSpec extends AsyncTestSpec {

  def withCtx(f: BetterSqlite3Database => Future[Assertion]): Future[Assertion] = {
    BaseDBContextSpec.contextF.flatMap(f)
  }
}

object BaseDBContextSpec {
  
  import scala.concurrent.ExecutionContext.Implicits.global

  private lazy val contextF: Future[BetterSqlite3Database] = {
    val webDb = BetterSqlite3WebSQL.openDatabase(":memory:")
    val db = webDb._db.asInstanceOf[js.Dynamic]._db.asInstanceOf[BetterSqlite3Database]
    
    FarjsDBMigrations.apply(db).map { _ =>
      db
    }
  }
}
