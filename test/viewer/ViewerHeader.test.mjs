/**
 * @typedef {import("../../viewer/ViewerHeader.mjs").ViewerHeaderProps} ViewerHeaderProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponent, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import WithSize from "@farjs/ui/WithSize.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";
import ViewerHeader from "../../viewer/ViewerHeader.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ViewerHeader.withSizeComp = mockComponent(WithSize);
ViewerHeader.textLineComp = mockComponent(TextLine);

const { withSizeComp, textLineComp } = ViewerHeader;

describe("ViewerHeader.test.mjs", () => {
  it("should render component", () => {
    //given
    const props = getViewerHeaderProps({
      filePath: "/test/filePath",
      encoding: "utf-8",
      size: 12345.0,
      percent: 100,
    });

    //when
    const result = TestRenderer.create(
      withThemeContext(h(ViewerHeader, props))
    ).root;

    //then
    assertViewerHeader(result, props);
  });
});

/**
 * @param {Partial<ViewerHeaderProps>} props
 * @returns {ViewerHeaderProps}
 */
function getViewerHeaderProps(props = {}) {
  return {
    filePath: "test/file.txt",
    encoding: "",
    size: 0,
    column: 0,
    percent: 0,
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ViewerHeaderProps} props
 */
function assertViewerHeader(result, props) {
  assert.deepEqual(ViewerHeader.displayName, "ViewerHeader");

  const style = FileListTheme.defaultTheme.menu.item;
  const encodingWidth = Math.max(props.encoding.length, 10);
  const sizeText = formatSize(props.size);
  const sizeWidth = Math.max(sizeText.length, 12);
  const columnWidth = 8;
  const percentWidth = 4;
  const gapWidth = 2;

  assertComponents(
    result.children,
    h(withSizeComp, { render: mockFunction() })
  );

  const render = result.findByType(withSizeComp).props.render;
  const width = 80;
  const content = TestRenderer.create(render(width, 25)).root;
  const dynamicWidth =
    width -
    encodingWidth -
    sizeWidth -
    columnWidth -
    percentWidth -
    gapWidth * 3;

  assertComponent(
    content,
    h(
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
    )
  );
}
