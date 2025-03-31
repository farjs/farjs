/**
 * @typedef {import("@farjs/ui/theme/Theme.mjs").ThemeStyle} ThemeStyle
 */
import React from "react";
import Theme from "@farjs/ui/theme/Theme.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import WithSize from "@farjs/ui/WithSize.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly filePath: string;
 *  readonly encoding: string;
 *  readonly size: number;
 *  readonly column: number;
 *  readonly percent: number;
 * }} ViewerHeaderProps
 */

/**
 * @param {ViewerHeaderProps} props
 */
const ViewerHeader = (props) => {
  const { withSizeComp, textLineComp } = ViewerHeader;

  /** @type {ThemeStyle} */
  const style = Theme.useTheme().menu.item;
  const encodingWidth = Math.max(props.encoding.length, 10);
  const sizeText = formatSize(props.size);
  const sizeWidth = Math.max(sizeText.length, 12);
  const columnWidth = 8;
  const percentWidth = 4;
  const gapWidth = 2;

  return h(withSizeComp, {
    render: (width) => {
      const dynamicWidth =
        width -
        encodingWidth -
        sizeWidth -
        columnWidth -
        percentWidth -
        gapWidth * 3;

      return h(
        "box",
        { style },
        h(textLineComp, {
          align: TextAlign.left,
          left: 0,
          top: 0,
          width: dynamicWidth,
          text: props.filePath,
          style: style,
          padding: 0,
        }),
        h(textLineComp, {
          align: TextAlign.center,
          left: dynamicWidth + gapWidth,
          top: 0,
          width: encodingWidth,
          text: props.encoding,
          style: style,
          padding: 0,
        }),
        h(textLineComp, {
          align: TextAlign.right,
          left: dynamicWidth + encodingWidth + gapWidth * 2,
          top: 0,
          width: sizeWidth,
          text: sizeText,
          style: style,
          padding: 0,
        }),
        h(textLineComp, {
          align: TextAlign.left,
          left: width - columnWidth - percentWidth,
          top: 0,
          width: columnWidth,
          text: `Col ${props.column}`,
          style: style,
          padding: 0,
        }),
        h(textLineComp, {
          align: TextAlign.right,
          left: width - percentWidth,
          top: 0,
          width: percentWidth,
          text: `${props.percent}%`,
          style: style,
          padding: 0,
        })
      );
    },
  });
};

ViewerHeader.displayName = "ViewerHeader";
ViewerHeader.withSizeComp = WithSize;
ViewerHeader.textLineComp = TextLine;

export default ViewerHeader;
