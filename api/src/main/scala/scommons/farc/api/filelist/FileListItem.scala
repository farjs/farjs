package scommons.farc.api.filelist

case class FileListItem(name: String,
                        isDir: Boolean,
                        isSymLink: Boolean,
                        size: Double,
                        atimeMs: Double,
                        mtimeMs: Double,
                        ctimeMs: Double,
                        birthtimeMs: Double)
