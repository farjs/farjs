/**
 * @import { ThemeEffects } from "@farjs/ui/theme/Theme.mjs"
 */
import path from "path";
import React, { useRef } from "react";
import TextLine from "@farjs/ui/TextLine.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import DoubleBorder from "@farjs/ui/border/DoubleBorder.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import WithStack from "@farjs/filelist/stack/WithStack.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import QuickViewDir from "./QuickViewDir.mjs";
import QuickViewFile from "./QuickViewFile.mjs";

const h = React.createElement;

const QuickViewPanel = () => {
  const {
    doubleBorderComp,
    horizontalLineComp,
    textLineComp,
    quickViewDirComp,
    quickViewFileComp,
  } = QuickViewPanel;

  const stacks = WithStacks.useStacks();
  const panelStack = WithStack.useStack();
  const inputRef = useRef(panelStack.panelInput);
  const width = panelStack.width;
  const height = panelStack.height;

  const theme = FileListTheme.useTheme().fileList;
  /** @type {ThemeEffects} */
  const style = theme.regularItem;

  const stack = !panelStack.isRight ? stacks.right.stack : stacks.left.stack;
  const maybeData = [stack.peek().getData()]
    .filter((_) => _ !== undefined)
    .flatMap((data) => {
      return [FileListState.currentItem(data.state)]
        .filter((_) => _ !== undefined)
        .map((i) => {
          return {
            data,
            currItem: i === FileListItem.up ? FileListItem.currDir : i,
          };
        });
    });

  return h(
    "box",
    { style },
    h(doubleBorderComp, {
      width: width,
      height: height,
      style,
    }),
    h(horizontalLineComp, {
      left: 0,
      top: height - 4,
      length: width,
      lineCh: SingleChars.horizontal,
      style,
      startCh: DoubleChars.leftSingle,
      endCh: DoubleChars.rightSingle,
    }),
    h(textLineComp, {
      align: TextAlign.center,
      left: 1,
      top: 0,
      width: width - 2,
      text: "Quick view",
      style,
      focused: panelStack.stack.isActive,
    }),

    maybeData.length > 0
      ? (([{ data, currItem }]) => {
          const filePath = path.join(data.state.currDir.path, currItem.name);
          return h(
            React.Fragment,
            null,
            currItem.isDir
              ? h(quickViewDirComp, {
                  dispatch: data.dispatch,
                  actions: data.actions,
                  state: data.state,
                  stack: panelStack.stack,
                  width,
                  currItem,
                })
              : h(
                  "box",
                  {
                    left: 1,
                    top: 1,
                    width: width - 2,
                    height: height - 5,
                    style,
                  },
                  h(quickViewFileComp, {
                    key: filePath,
                    dispatch: data.dispatch,
                    inputRef: inputRef,
                    isRight: panelStack.isRight,
                    filePath,
                    size: currItem.size,
                  })
                ),

            h("text", {
              width: width - 2,
              height: 2,
              left: 1,
              top: height - 3,
              style,
              content: currItem.name,
            })
          );
        })(maybeData)
      : null
  );
};

QuickViewPanel.displayName = "QuickViewPanel";
QuickViewPanel.doubleBorderComp = DoubleBorder;
QuickViewPanel.horizontalLineComp = HorizontalLine;
QuickViewPanel.textLineComp = TextLine;
QuickViewPanel.quickViewDirComp = QuickViewDir;
QuickViewPanel.quickViewFileComp = QuickViewFile;

export default QuickViewPanel;
