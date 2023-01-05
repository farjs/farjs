package farjs.app

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

class FarjsDBMigrationsSpec extends BaseDBContextSpec {

  it should "remove bundle.json module from cache after apply" in withCtx { _ =>
    //given
    val moduleSuffix = "farjs/domain/bundle.json"
    
    //then
    val cache = global.require.cache
    val cacheDict = cache.asInstanceOf[js.Dictionary[js.Any]]
    cacheDict.keys.find(_.endsWith(moduleSuffix)) shouldBe None
  }
}
