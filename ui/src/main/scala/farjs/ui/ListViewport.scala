package farjs.ui

case class ListViewport(offset: Int,
                        focused: Int,
                        length: Int,
                        viewLength: Int) {

  def down: ListViewport = {
    val maxSelected = math.max(math.min(length - offset - 1, viewLength - 1), 0)
    if (focused < maxSelected) copy(focused = focused + 1)
    else if (offset < length - viewLength) copy(offset = offset + 1)
    else this
  }

  def up: ListViewport = {
    if (focused > 0) copy(focused = focused - 1)
    else if (offset > 0) copy(offset = offset - 1)
    else this
  }

  def pagedown: ListViewport = {
    val newOffset = math.max(math.min(length - viewLength, offset + viewLength), 0)
    val newFocused =
      if (newOffset == offset) math.max(math.min(length - newOffset - 1, viewLength - 1), 0)
      else math.max(math.min(length - newOffset - 1, focused), 0)

    if (offset != newOffset || focused != newFocused) {
      copy(offset = newOffset, focused = newFocused)
    }
    else this
  }

  def pageup: ListViewport = {
    val newOffset = math.max(offset - viewLength, 0)
    val newFocused =
      if (newOffset == offset) 0
      else focused
    
    if (offset != newOffset || focused != newFocused) {
      copy(offset = newOffset, focused = newFocused)
    }
    else this
  }

  def end: ListViewport = {
    val newOffset = math.max(length - viewLength, 0)
    val newFocused = math.max(math.min(length - newOffset - 1, viewLength - 1), 0)

    if (offset != newOffset || focused != newFocused) {
      copy(offset = newOffset, focused = newFocused)
    }
    else this
  }

  def home: ListViewport = {
    if (offset != 0 || focused != 0) {
      copy(offset = 0, focused = 0)
    }
    else this
  }

  def onKeypress(keyFull: String): Option[ListViewport] = {
    keyFull match {
      case "down" => Some(down)
      case "up" => Some(up)
      case "pagedown" => Some(pagedown)
      case "pageup" => Some(pageup)
      case "end" => Some(end)
      case "home" => Some(home)
      case _ => None
    }
  }

  def resize(newViewLength: Int): ListViewport = {
    if (newViewLength != viewLength) {
      val index = offset + focused
      val dx =
        if (focused >= newViewLength) focused - newViewLength + 1
        else 0

      val newOffset = math.max(math.min(length - newViewLength, offset + dx), 0)
      val newFocused = math.max(math.min(length - newOffset - 1, index - newOffset), 0)
  
      copy(offset = newOffset, focused = newFocused, viewLength = newViewLength)
    }
    else this
  }
}
