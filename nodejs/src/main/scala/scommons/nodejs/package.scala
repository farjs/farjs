package scommons

package object nodejs {

  type NodeProcess = raw.NodeProcess
  type URL = raw.URL
  type Stats = raw.Stats

  lazy val path: raw.Path = raw.Path
  lazy val fs: FS = FS
  lazy val os: raw.OS = raw.OS
  
  lazy val process: NodeProcess = raw.NodeJs.process
}
