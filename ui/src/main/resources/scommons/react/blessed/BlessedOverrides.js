const blessed = require("blessed")

const colors = blessed.colors
const unicode = blessed.unicode
const Element = blessed.widget.Element

const apply = function () {

  Element.prototype.render = function() {
    this._emit('prerender');
  
    this.parseContent();
  
    var coords = this._getCoords(true);
    if (!coords) {
      delete this.lpos;
      return;
    }
  
    if (coords.xl - coords.xi <= 0) {
      coords.xl = Math.max(coords.xl, coords.xi);
      return;
    }
  
    if (coords.yl - coords.yi <= 0) {
      coords.yl = Math.max(coords.yl, coords.yi);
      return;
    }
  
    var lines = this.screen.lines
      , xi = coords.xi
      , xl = coords.xl
      , yi = coords.yi
      , yl = coords.yl
      , x
      , y
      , cell
      , attr
      , ch
      , content = this._pcontent
      , ci = this._clines.ci[coords.base]
      , battr
      , dattr
      , c
      , visible
      , i
      , bch = this.ch;
  
    // Clip content if it's off the edge of the screen
    // if (xi + this.ileft < 0 || yi + this.itop < 0) {
    //   var clines = this._clines.slice();
    //   if (xi + this.ileft < 0) {
    //     for (var i = 0; i < clines.length; i++) {
    //       var t = 0;
    //       var csi = '';
    //       var csis = '';
    //       for (var j = 0; j < clines[i].length; j++) {
    //         while (clines[i][j] === '\x1b') {
    //           csi = '\x1b';
    //           while (clines[i][j++] !== 'm') csi += clines[i][j];
    //           csis += csi;
    //         }
    //         if (++t === -(xi + this.ileft) + 1) break;
    //       }
    //       clines[i] = csis + clines[i].substring(j);
    //     }
    //   }
    //   if (yi + this.itop < 0) {
    //     clines = clines.slice(-(yi + this.itop));
    //   }
    //   content = clines.join('\n');
    // }
  
    if (coords.base >= this._clines.ci.length) {
      ci = this._pcontent.length;
    }
  
    this.lpos = coords;
  
    if (this.border && this.border.type === 'line') {
      this.screen._borderStops[coords.yi] = true;
      this.screen._borderStops[coords.yl - 1] = true;
      // if (!this.screen._borderStops[coords.yi]) {
      //   this.screen._borderStops[coords.yi] = { xi: coords.xi, xl: coords.xl };
      // } else {
      //   if (this.screen._borderStops[coords.yi].xi > coords.xi) {
      //     this.screen._borderStops[coords.yi].xi = coords.xi;
      //   }
      //   if (this.screen._borderStops[coords.yi].xl < coords.xl) {
      //     this.screen._borderStops[coords.yi].xl = coords.xl;
      //   }
      // }
      // this.screen._borderStops[coords.yl - 1] = this.screen._borderStops[coords.yi];
    }
  
    dattr = this.sattr(this.style);
    attr = dattr;
  
    // If we're in a scrollable text box, check to
    // see which attributes this line starts with.
    if (ci > 0) {
      attr = this._clines.attr[Math.min(coords.base, this._clines.length - 1)];
    }
  
    if (this.border) xi++, xl--, yi++, yl--;
  
    // If we have padding/valign, that means the
    // content-drawing loop will skip a few cells/lines.
    // To deal with this, we can just fill the whole thing
    // ahead of time. This could be optimized.
    if (this.tpadding || (this.valign && this.valign !== 'top')) {
      if (this.style.transparent) {
//        for (y = Math.max(yi, 0); y < yl; y++) {
//          if (!lines[y]) break;
//          for (x = Math.max(xi, 0); x < xl; x++) {
//            if (!lines[y][x]) break;
//            lines[y][x][0] = colors.blend(attr, lines[y][x][0]);
//            // lines[y][x][1] = bch;
//            lines[y].dirty = true;
//          }
//        }
      } else {
        this.screen.fillRegion(dattr, bch, xi, xl, yi, yl);
      }
    }
  
    if (this.tpadding) {
      xi += this.padding.left, xl -= this.padding.right;
      yi += this.padding.top, yl -= this.padding.bottom;
    }
  
    // Determine where to place the text if it's vertically aligned.
    if (this.valign === 'middle' || this.valign === 'bottom') {
      visible = yl - yi;
      if (this._clines.length < visible) {
        if (this.valign === 'middle') {
          visible = visible / 2 | 0;
          visible -= this._clines.length / 2 | 0;
        } else if (this.valign === 'bottom') {
          visible -= this._clines.length;
        }
        ci -= visible * (xl - xi);
      }
    }
  
    // Draw the content and background.
    for (y = yi; y < yl; y++) {
      if (!lines[y]) {
        if (y >= this.screen.height || yl < this.ibottom) {
          break;
        } else {
          continue;
        }
      }
      for (x = xi; x < xl; x++) {
        cell = lines[y][x];
        if (!cell) {
          if (x >= this.screen.width || xl < this.iright) {
            break;
          } else {
            continue;
          }
        }
  
        ch = content[ci++] || bch;
  
        // if (!content[ci] && !coords._contentEnd) {
        //   coords._contentEnd = { x: x - xi, y: y - yi };
        // }
  
        // Handle escape codes.
        while (ch === '\x1b') {
          if (c = /^\x1b\[[\d;]*m/.exec(content.substring(ci - 1))) {
            ci += c[0].length - 1;
            attr = this.screen.attrCode(c[0], attr, dattr);
            // Ignore foreground changes for selected items.
            if (this.parent._isList && this.parent.interactive
                && this.parent.items[this.parent.selected] === this
                && this.parent.options.invertSelected !== false) {
              attr = (attr & ~(0x1ff << 9)) | (dattr & (0x1ff << 9));
            }
            ch = content[ci] || bch;
            ci++;
          } else {
            break;
          }
        }
  
        // Handle newlines.
        if (ch === '\t') ch = bch;
        if (ch === '\n') {
          // If we're on the first cell and we find a newline and the last cell
          // of the last line was not a newline, let's just treat this like the
          // newline was already "counted".
          if (x === xi && y !== yi && content[ci - 2] !== '\n') {
            x--;
            continue;
          }
          // We could use fillRegion here, name the
          // outer loop, and continue to it instead.
          ch = bch;
          for (; x < xl; x++) {
            cell = lines[y][x];
            if (!cell) break;
            if (this.style.transparent) {
//              lines[y][x][0] = colors.blend(attr, lines[y][x][0]);
//              if (content[ci]) lines[y][x][1] = ch;
//              lines[y].dirty = true;
            } else {
              if (attr !== cell[0] || ch !== cell[1]) {
                lines[y][x][0] = attr;
                lines[y][x][1] = ch;
                lines[y].dirty = true;
              }
            }
          }
          continue;
        }
  
        if (this.screen.fullUnicode && content[ci - 1]) {
          var point = unicode.codePointAt(content, ci - 1);
          // Handle combining chars:
          // Make sure they get in the same cell and are counted as 0.
          if (unicode.combining[point]) {
            if (point > 0x00ffff) {
              ch = content[ci - 1] + content[ci];
              ci++;
            }
            if (x - 1 >= xi) {
              lines[y][x - 1][1] += ch;
            } else if (y - 1 >= yi) {
              lines[y - 1][xl - 1][1] += ch;
            }
            x--;
            continue;
          }
          // Handle surrogate pairs:
          // Make sure we put surrogate pair chars in one cell.
          if (point > 0x00ffff) {
            ch = content[ci - 1] + content[ci];
            ci++;
          }
        }
  
        if (this._noFill) continue;
  
        if (this.style.transparent) {
//          lines[y][x][0] = colors.blend(attr, lines[y][x][0]);
//          if (content[ci]) lines[y][x][1] = ch;
//          lines[y].dirty = true;
        } else {
          if (attr !== cell[0] || ch !== cell[1]) {
            lines[y][x][0] = attr;
            lines[y][x][1] = ch;
            lines[y].dirty = true;
          }
        }
      }
    }
  
    // Draw the scrollbar.
    // Could possibly draw this after all child elements.
    if (this.scrollbar) {
      // XXX
      // i = this.getScrollHeight();
      i = Math.max(this._clines.length, this._scrollBottom());
    }
    if (coords.notop || coords.nobot) i = -Infinity;
    if (this.scrollbar && (yl - yi) < i) {
      x = xl - 1;
      if (this.scrollbar.ignoreBorder && this.border) x++;
      if (this.alwaysScroll) {
        y = this.childBase / (i - (yl - yi));
      } else {
        y = (this.childBase + this.childOffset) / (i - 1);
      }
      y = yi + ((yl - yi) * y | 0);
      if (y >= yl) y = yl - 1;
      cell = lines[y] && lines[y][x];
      if (cell) {
        if (this.track) {
          ch = this.track.ch || ' ';
          attr = this.sattr(this.style.track,
            this.style.track.fg || this.style.fg,
            this.style.track.bg || this.style.bg);
          this.screen.fillRegion(attr, ch, x, x + 1, yi, yl);
        }
        ch = this.scrollbar.ch || ' ';
        attr = this.sattr(this.style.scrollbar,
          this.style.scrollbar.fg || this.style.fg,
          this.style.scrollbar.bg || this.style.bg);
        if (attr !== cell[0] || ch !== cell[1]) {
          lines[y][x][0] = attr;
          lines[y][x][1] = ch;
          lines[y].dirty = true;
        }
      }
    }
  
    if (this.border) xi--, xl++, yi--, yl++;
  
    if (this.tpadding) {
      xi -= this.padding.left, xl += this.padding.right;
      yi -= this.padding.top, yl += this.padding.bottom;
    }
  
    // Draw the border.
    if (this.border) {
      battr = this.sattr(this.style.border);
      y = yi;
      if (coords.notop) y = -1;
      for (x = xi; x < xl; x++) {
        if (!lines[y]) break;
        if (coords.noleft && x === xi) continue;
        if (coords.noright && x === xl - 1) continue;
        cell = lines[y][x];
        if (!cell) continue;
        if (this.border.type === 'line') {
          if (x === xi) {
            ch = '\u250c'; // '┌'
            if (!this.border.left) {
              if (this.border.top) {
                ch = '\u2500'; // '─'
              } else {
                continue;
              }
            } else {
              if (!this.border.top) {
                ch = '\u2502'; // '│'
              }
            }
          } else if (x === xl - 1) {
            ch = '\u2510'; // '┐'
            if (!this.border.right) {
              if (this.border.top) {
                ch = '\u2500'; // '─'
              } else {
                continue;
              }
            } else {
              if (!this.border.top) {
                ch = '\u2502'; // '│'
              }
            }
          } else {
            ch = '\u2500'; // '─'
          }
        } else if (this.border.type === 'bg') {
          ch = this.border.ch;
        }
        if (!this.border.top && x !== xi && x !== xl - 1) {
          ch = ' ';
          if (dattr !== cell[0] || ch !== cell[1]) {
            lines[y][x][0] = dattr;
            lines[y][x][1] = ch;
            lines[y].dirty = true;
            continue;
          }
        }
        if (battr !== cell[0] || ch !== cell[1]) {
          lines[y][x][0] = battr;
          lines[y][x][1] = ch;
          lines[y].dirty = true;
        }
      }
      y = yi + 1;
      for (; y < yl - 1; y++) {
        if (!lines[y]) continue;
        cell = lines[y][xi];
        if (cell) {
          if (this.border.left) {
            if (this.border.type === 'line') {
              ch = '\u2502'; // '│'
            } else if (this.border.type === 'bg') {
              ch = this.border.ch;
            }
            if (!coords.noleft)
            if (battr !== cell[0] || ch !== cell[1]) {
              lines[y][xi][0] = battr;
              lines[y][xi][1] = ch;
              lines[y].dirty = true;
            }
          } else {
            ch = ' ';
            if (dattr !== cell[0] || ch !== cell[1]) {
              lines[y][xi][0] = dattr;
              lines[y][xi][1] = ch;
              lines[y].dirty = true;
            }
          }
        }
        cell = lines[y][xl - 1];
        if (cell) {
          if (this.border.right) {
            if (this.border.type === 'line') {
              ch = '\u2502'; // '│'
            } else if (this.border.type === 'bg') {
              ch = this.border.ch;
            }
            if (!coords.noright)
            if (battr !== cell[0] || ch !== cell[1]) {
              lines[y][xl - 1][0] = battr;
              lines[y][xl - 1][1] = ch;
              lines[y].dirty = true;
            }
          } else {
            ch = ' ';
            if (dattr !== cell[0] || ch !== cell[1]) {
              lines[y][xl - 1][0] = dattr;
              lines[y][xl - 1][1] = ch;
              lines[y].dirty = true;
            }
          }
        }
      }
      y = yl - 1;
      if (coords.nobot) y = -1;
      for (x = xi; x < xl; x++) {
        if (!lines[y]) break;
        if (coords.noleft && x === xi) continue;
        if (coords.noright && x === xl - 1) continue;
        cell = lines[y][x];
        if (!cell) continue;
        if (this.border.type === 'line') {
          if (x === xi) {
            ch = '\u2514'; // '└'
            if (!this.border.left) {
              if (this.border.bottom) {
                ch = '\u2500'; // '─'
              } else {
                continue;
              }
            } else {
              if (!this.border.bottom) {
                ch = '\u2502'; // '│'
              }
            }
          } else if (x === xl - 1) {
            ch = '\u2518'; // '┘'
            if (!this.border.right) {
              if (this.border.bottom) {
                ch = '\u2500'; // '─'
              } else {
                continue;
              }
            } else {
              if (!this.border.bottom) {
                ch = '\u2502'; // '│'
              }
            }
          } else {
            ch = '\u2500'; // '─'
          }
        } else if (this.border.type === 'bg') {
          ch = this.border.ch;
        }
        if (!this.border.bottom && x !== xi && x !== xl - 1) {
          ch = ' ';
          if (dattr !== cell[0] || ch !== cell[1]) {
            lines[y][x][0] = dattr;
            lines[y][x][1] = ch;
            lines[y].dirty = true;
          }
          continue;
        }
        if (battr !== cell[0] || ch !== cell[1]) {
          lines[y][x][0] = battr;
          lines[y][x][1] = ch;
          lines[y].dirty = true;
        }
      }
    }
  
    if (this.shadow) {
      // right
      y = Math.max(yi + 1, 0);
      for (; y < yl + 1; y++) {
        if (!lines[y]) break;
        x = xl;
        for (; x < xl + 2; x++) {
          if (!lines[y][x]) break;
          // lines[y][x][0] = colors.blend(this.dattr, lines[y][x][0]);
          //lines[y][x][0] = colors.blend(lines[y][x][0]);
          lines[y][x][0] &= ~0x1ff; //bg
          lines[y][x][0] &= ~(0x1ff << 9); //fg
          lines[y].dirty = true;
        }
      }
      // bottom
      y = yl;
      for (; y < yl + 1; y++) {
        if (!lines[y]) break;
        for (x = Math.max(xi + 2, 0); x < xl; x++) {
          if (!lines[y][x]) break;
          // lines[y][x][0] = colors.blend(this.dattr, lines[y][x][0]);
          //lines[y][x][0] = colors.blend(lines[y][x][0]);
          lines[y][x][0] &= ~0x1ff; //bg
          lines[y][x][0] &= ~(0x1ff << 9); //fg
          lines[y].dirty = true;
        }
      }
    }
  
    this.children.forEach(function(el) {
      if (el.screen._ci !== -1) {
        el.index = el.screen._ci++;
      }
      // if (el.screen._rendering) {
      //   el._rendering = true;
      // }
      el.render();
      // if (el.screen._rendering) {
      //   el._rendering = false;
      // }
    });
  
    this._emit('render', [coords]);
  
    return coords;
  };
  
  Element.prototype._render = Element.prototype.render;
  
  //console.log("Blessed overrides!");
}

module.exports = apply
