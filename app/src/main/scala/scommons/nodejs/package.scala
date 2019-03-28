package scommons

package object nodejs {

  type NodeProcess = raw.NodeProcess
  
  val process: NodeProcess = raw.NodeJsNative.process
}
