package farjs.app.filelist.fs

import farjs.app.BaseDBContextSpec
import farjs.domain.HistoryFolder
import farjs.domain.dao.HistoryFolderDao

import scala.concurrent.Future

class FSFoldersServiceSpec extends BaseDBContextSpec {

  it should "create new record when save" in withCtx { ctx =>
    //given
    val dao = new HistoryFolderDao(ctx)
    val service = new FSFoldersService(dao)
    val path = "test/path"
    
    val beforeF = dao.deleteAll()
    
    //when
    val resultF = beforeF.flatMap { _ =>
      service.save(path)
    }

    //then
    for {
      _ <- resultF
      results <- service.getAll
    } yield {
      results shouldBe List(path)
    }
  }
  
  it should "update existing record when save" in withCtx { ctx =>
    //given
    val dao = new HistoryFolderDao(ctx)
    val service = new FSFoldersService(dao)
    val path = "test/path"
    
    val beforeF = dao.getAll
    
    //when
    val resultF = beforeF.flatMap { existing =>
      existing.map(_.path) shouldBe List(path)
      val entity = existing.head
      dao.save(HistoryFolder(path, entity.updatedAt + 1), keepFirst = 5).map { _ =>
        entity
      }
    }

    //then
    for {
      existing <- resultF
      entities <- dao.getAll
      results <- service.getAll
    } yield {
      results shouldBe List(path)
      existing.updatedAt should be < entities.head.updatedAt
    }
  }

  it should "keep first N records when save" in withCtx { ctx =>
    //given
    val dao = new HistoryFolderDao(ctx)
    val service = new FSFoldersService(dao)
    val path = "test/path"
    val keepFirst = 3
    
    val beforeF = dao.getAll
    
    //when
    val resultF = beforeF.flatMap { existing =>
      existing.map(_.path) shouldBe List(path)
      val entity = existing.head
      Future.sequence((1 to 5).toList.map { i =>
        dao.save(HistoryFolder(s"$path$i", entity.updatedAt + i), keepFirst)
      })
    }

    //then
    for {
      _ <- resultF
      results <- service.getAll
    } yield {
      results shouldBe List(
        "test/path3",
        "test/path4",
        "test/path5"
      )
    }
  }
}
