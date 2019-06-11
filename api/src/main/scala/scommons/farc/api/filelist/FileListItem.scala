package scommons.farc.api.filelist

case class FileListItem(name: String,
                        isDir: Boolean = false,
                        isSymLink: Boolean = false,
                        size: Double = 0.0,
                        atimeMs: Double = 0.0,
                        mtimeMs: Double = 0.0,
                        ctimeMs: Double = 0.0,
                        birthtimeMs: Double = 0.0)
