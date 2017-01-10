package org.kframework.minikore

import org.apache.commons.io.FileUtils

import org.kframework.minikore.MiniKore.Definition

object MiniToTextToMini {
  def apply(d: Definition): Definition = {
    val text = MiniToText.apply(d)
    val file = new java.io.File("/tmp/x")
    FileUtils.writeStringToFile(file, text)
    val d2 = TextToMini.parse(file)
    val text2 = MiniToText.apply(d2)
    assert(d == d2)
    d2
  }
}
