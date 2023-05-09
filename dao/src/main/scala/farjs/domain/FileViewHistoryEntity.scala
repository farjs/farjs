package farjs.domain

case class FileViewHistoryEntity(path: String,
                                 isEdit: Boolean,
                                 encoding: String,
                                 position: Double,
                                 wrap: Option[Boolean],
                                 column: Option[Int],
                                 updatedAt: Long)
