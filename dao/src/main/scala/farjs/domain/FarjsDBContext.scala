package farjs.domain

import scommons.websql.Database
import scommons.websql.io.SqliteContext

class FarjsDBContext(db: Database) extends SqliteContext(db)
