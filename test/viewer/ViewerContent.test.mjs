/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {import("../../viewer/ViewerContent.mjs").ViewerContentProps} ViewerContentProps
 * @import { ViewerFileViewport } from "../../viewer/ViewerFileViewport.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import EncodingsPopup from "../../file/popups/EncodingsPopup.mjs";
import TextSearchPopup from "../../file/popups/TextSearchPopup.mjs";
import ViewerInput from "../../viewer/ViewerInput.mjs";
import ViewerSearch from "../../viewer/ViewerSearch.mjs";
import MockViewerFileReader from "../../viewer/MockViewerFileReader.mjs";
import { createViewerFileViewport } from "../../viewer/ViewerFileViewport.mjs";
import ViewerContent from "../../viewer/ViewerContent.mjs";
import ViewerFileLine from "../../viewer/ViewerFileLine.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ViewerContent.viewerInput = mockComponent(ViewerInput);
ViewerContent.encodingsPopup = mockComponent(EncodingsPopup);
ViewerContent.textSearchPopup = mockComponent(TextSearchPopup);
ViewerContent.viewerSearch = mockComponent(ViewerSearch);

const { viewerInput, encodingsPopup, textSearchPopup, viewerSearch } =
  ViewerContent;

describe("ViewerContent.test.mjs", () => {
  async function TestContext(
    size = 25,
    content = "test \nfile content",
    onKeypress = () => false
  ) {
    const readPrevLines = mockFunction((...args) => {
      ctx.readPrevLinesArgs = args;
      return ctx.readP;
    });
    const readNextLines = mockFunction((...args) => {
      ctx.readNextLinesArgs = args;
      return ctx.readP;
    });
    const fileReader = new MockViewerFileReader({
      readPrevLines,
      readNextLines,
    });
    const setViewport = mockFunction(
      /** @type {(vp: ViewerFileViewport | undefined) => void} */ (
        (vp) => {
          if (vp) {
            ctx.viewport = vp;
            ctx.props = { ...ctx.props, viewport: vp };
            TestRenderer.act(() => {
              ctx.renderer.update(
                withThemeContext(h(ViewerContent, ctx.props))
              );
            });
          }
        }
      )
    );
    const ctx = (() => {
      const readPrevLinesArgs = /** @type {any[]} */ ([]);
      const readNextLinesArgs = /** @type {any[]} */ ([]);
      const readP = Promise.resolve(
        content.split("\n").map((c) => ViewerFileLine(c, c.length + 1))
      );
      const props = getViewerContentProps(
        fileReader,
        setViewport,
        onKeypress,
        size
      );
      return {
        readPrevLinesArgs,
        readPrevLines,
        readNextLinesArgs,
        readNextLines,
        readP,
        props,
        viewport: props.viewport,
        //@ts-ignore
        renderer: /** @type {TestRenderer.ReactTestRenderer} */ (undefined),
      };
    })();

    //when & then
    ctx.renderer = TestRenderer.create(
      withThemeContext(h(ViewerContent, ctx.props))
    );
    assertViewerContent(ctx.renderer.root, ctx.props, []);
    await ctx.readP;
    assert.deepEqual(ctx.readNextLines.times, 1);
    assert.deepEqual(ctx.readNextLinesArgs, [
      ctx.viewport.height,
      0.0,
      ctx.viewport.encoding,
    ]);
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(setViewport.times, 1);
    assertViewerContent(ctx.renderer.root, ctx.props, content.split("\n"));

    return ctx;
  }

  it("should move viewport when onWheel", async () => {
    //given
    const ctx = await TestContext();

    /** @type {(up: boolean, lines: number, position: number, content: string, expected: string[]) => Promise<void>} */
    async function check(up, lines, position, content, expected) {
      const readPrevLinesTimes = ctx.readPrevLines.times;
      const readNextLinesTimes = ctx.readNextLines.times;
      ctx.readP = Promise.resolve(
        content.split("\n").map((c) => ViewerFileLine(c, c.length))
      );

      //when
      ctx.renderer.root.findByType(viewerInput).props.onWheel(up);
      await ctx.readP;
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();

      //then
      if (up) {
        assert.deepEqual(ctx.readPrevLines.times, readPrevLinesTimes + 1);
        assert.deepEqual(ctx.readPrevLinesArgs, [
          lines,
          position,
          ctx.viewport.size,
          ctx.viewport.encoding,
        ]);
      } else {
        assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes + 1);
        assert.deepEqual(ctx.readNextLinesArgs, [
          lines,
          position,
          ctx.viewport.encoding,
        ]);
      }
      assertViewerContent(ctx.renderer.root, ctx.props, expected);
    }

    //when & then
    await check(false, 1, 19, "end", ["file content", "end"]);
    await check(true, 1, 6, "start", ["start", "file content", "end"]);
  });

  it("should show and close search popup when onKeypress(F7)", async () => {
    //given
    const ctx = await TestContext();

    //when & then
    ctx.renderer.root.findByType(viewerInput).props.onKeypress("f7");
    ctx.renderer.root.findByType(textSearchPopup).props.onCancel();

    //then
    assert.deepEqual(ctx.renderer.root.findAllByType(textSearchPopup), []);
  });

  it("should trigger search when onKeypress(F7)", async () => {
    //given
    const ctx = await TestContext();
    ctx.renderer.root.findByType(viewerInput).props.onKeypress("f7");
    const searchTerm = "test";

    //when & then
    ctx.renderer.root.findByType(textSearchPopup).props.onSearch(searchTerm);
    assert.deepEqual(ctx.renderer.root.findAllByType(textSearchPopup), []);

    //when & then
    ctx.renderer.root.findByType(viewerSearch).props.onComplete();
    assert.deepEqual(ctx.renderer.root.findAllByType(viewerSearch), []);
  });

  it("should show encodings popup and switch encoding when onKeypress(F8)", async () => {
    //given
    const ctx = await TestContext();

    /** @type {(encoding: string, content: string, expected: string[]) => Promise<void>} */
    async function check(encoding, content, expected) {
      const readNextLinesTimes = ctx.readNextLines.times;
      ctx.readP = Promise.resolve(
        content.split("\n").map((c) => ViewerFileLine(c, c.length))
      );

      //when
      ctx.renderer.root.findByType(encodingsPopup).props.onApply(encoding);
      await ctx.readP;
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();

      //then
      assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes + 1);
      assert.deepEqual(ctx.readNextLinesArgs, [
        ctx.viewport.height,
        ctx.viewport.position,
        encoding,
      ]);
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();
      assertViewerContent(ctx.renderer.root, ctx.props, expected, true);
    }

    ctx.renderer.root.findByType(viewerInput).props.onKeypress("f8");
    assert.deepEqual(
      ctx.renderer.root.findAllByType(encodingsPopup).length > 0,
      true
    );

    //when & then
    await check("latin1", "reload1", ["reload1"]);
    await check("utf-8", "reload2", ["reload2"]);

    //when & then
    ctx.renderer.root.findByType(encodingsPopup).props.onClose();
    assert.deepEqual(ctx.renderer.root.findAllByType(encodingsPopup).length, 0);
  });

  it("should re-load prev page if at the end when onKeypress(down)", async () => {
    //given
    const ctx = await TestContext(10, "1\n2\n3\n4\n5");

    const readPrevLinesTimes = ctx.readPrevLines.times;
    ctx.readP = Promise.resolve(
      "2\n3\n4\n5".split("\n").map((c) => ViewerFileLine(c, c.length + 1))
    );

    //when
    ctx.renderer.root.findByType(viewerInput).props.onKeypress("down");
    await ctx.readP;
    await Promise.resolve();

    //then
    assert.deepEqual(ctx.readPrevLines.times, readPrevLinesTimes + 1);
    assert.deepEqual(ctx.readPrevLinesArgs, [
      ctx.viewport.height,
      ctx.viewport.size,
      ctx.viewport.size,
      ctx.viewport.encoding,
    ]);
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    assertViewerContent(ctx.renderer.root, ctx.props, ["2", "3", "4", "5"]);
  });

  it("should move viewport when onKeypress", async () => {
    //given
    const ctx = await TestContext();

    /** @type {(key: string, lines: number, position: number, content: string, expected: string[], noop?: boolean) => Promise<void>} */
    async function check(
      key,
      lines,
      position,
      content,
      expected,
      noop = false
    ) {
      const readPrevLinesTimes = ctx.readPrevLines.times;
      const readNextLinesTimes = ctx.readNextLines.times;
      ctx.readP =
        content.length === 0
          ? Promise.resolve([])
          : Promise.resolve(
              content.split("\n").map((c) => ViewerFileLine(c, c.length))
            );

      //when
      ctx.renderer.root.findByType(viewerInput).props.onKeypress(key);
      await ctx.readP;
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();
      await Promise.resolve();

      //then
      if (!noop) {
        if (["end", "up", "pageup"].includes(key)) {
          assert.deepEqual(ctx.readPrevLines.times, readPrevLinesTimes + 1);
          assert.deepEqual(ctx.readPrevLinesArgs, [
            lines,
            position,
            ctx.viewport.size,
            ctx.viewport.encoding,
          ]);
        } else {
          assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes + 1);
          assert.deepEqual(ctx.readNextLinesArgs, [
            lines,
            position,
            ctx.viewport.encoding,
          ]);
        }
      } else {
        assert.deepEqual(ctx.readPrevLines.times, readPrevLinesTimes);
        assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes);
      }
      await Promise.resolve();
      await Promise.resolve();
      assertViewerContent(ctx.renderer.root, ctx.props, expected);
    }

    //when & then
    await check("C-r", ctx.viewport.height, 0, "new content", ["new content"]);
    await check("end", ctx.viewport.height, ctx.viewport.size, "ending", [
      "ending",
    ]);
    await check("home", ctx.viewport.height, 0, "beginning", ["beginning"]);
    await check("up", 1, 0, "already at the beginning", ["beginning"], true);
    await check("down", 1, 9, "next line 1", ["next line 1"]);
    await check("down", 1, 20, "", ["next line 1"]);
    await check("down", 1, 20, "line2", ["line2"]);
    await check("down", 1, 25, "out of file size", ["line2"], true);
    await check("up", 1, 20, "prev line", ["prev line", "line2"]);
    await check("up", 1, 11, "", ["prev line", "line2"]);
    await check("pageup", ctx.viewport.height, 11, "1\n2\n3\n4", [
      "1",
      "2",
      "3",
      "4",
      "prev line",
    ]);
    await check("pagedown", ctx.viewport.height, 20, "next paaaaaaage", [
      "next paaaaaa",
    ]);
    await check("left", ctx.viewport.height, 20, "", ["next paaaaaa"], true);
    await check("right", ctx.viewport.height, 20, "", ["ext paaaaaaa"], true);
    await check("right", ctx.viewport.height, 20, "", ["xt paaaaaaag"], true);
    await check("left", ctx.viewport.height, 20, "", ["ext paaaaaaa"], true);
    await check("f2", ctx.viewport.height, 20, "loooooooooooong line", [
      "looooooooooo",
      "ong line",
    ]);
    await check("up", 1, 20, "prev liiiiiiiiine 1", [
      "iiiiiiiine 1",
      "looooooooooo",
      "ong line",
    ]);
    await check("home", ctx.viewport.height, 0, "beginning", ["beginning"]);
    await check("down", 1, 9, "next liiiiiiiiine 1\n", ["next liiiiii"]);
    await check("right", ctx.viewport.height, 9, "", ["ext liiiiii"], true);
    await check("f2", ctx.viewport.height, 9, "next liiiiiiiiine 1", [
      "ext liiiiiii",
    ]);
  });

  it("should do nothing if onKeypress callback returns true when onKeypress(f2)", async () => {
    //given
    const ctx = await TestContext(25, "test \nfile content", () => true);
    const readPrevLinesTimes = ctx.readPrevLines.times;
    const readNextLinesTimes = ctx.readNextLines.times;

    //when
    ctx.renderer.root.findByType(viewerInput).props.onKeypress("f2");
    await Promise.resolve();
    await Promise.resolve();

    //then
    assert.deepEqual(ctx.readPrevLines.times, readPrevLinesTimes);
    assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes);
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    assertViewerContent(ctx.renderer.root, ctx.props, [
      "test ",
      "file content",
    ]);
  });

  it("should do nothing when onKeypress(unknown)", async () => {
    //given
    const ctx = await TestContext();
    const readPrevLinesTimes = ctx.readPrevLines.times;
    const readNextLinesTimes = ctx.readNextLines.times;

    //when
    ctx.renderer.root.findByType(viewerInput).props.onKeypress("unknown");
    await Promise.resolve();
    await Promise.resolve();

    //then
    assert.deepEqual(ctx.readPrevLines.times, readPrevLinesTimes);
    assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes);
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    assertViewerContent(ctx.renderer.root, ctx.props, [
      "test ",
      "file content",
    ]);
  });

  it("should reload current page if props has changed", async () => {
    //given
    const ctx = await TestContext();
    const updatedProps = {
      ...ctx.props,
      viewport: ctx.viewport.updated({
        encoding: "utf-16",
        size: 11,
        width: 61,
        height: 21,
      }),
    };
    assert.notDeepEqual(updatedProps.viewport.encoding, ctx.viewport.encoding);
    assert.notDeepEqual(updatedProps.viewport.size, ctx.viewport.size);
    assert.notDeepEqual(updatedProps.viewport.width, ctx.viewport.width);
    assert.notDeepEqual(updatedProps.viewport.height, ctx.viewport.height);

    const readNextLinesTimes = ctx.readNextLines.times;
    ctx.readP = Promise.resolve(
      "test file content2".split("\n").map((c) => ViewerFileLine(c, c.length))
    );

    //when
    TestRenderer.act(() =>
      ctx.renderer.update(withThemeContext(h(ViewerContent, updatedProps)))
    );
    await Promise.resolve();
    await Promise.resolve();

    //then
    assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes + 1);
    assert.deepEqual(ctx.readNextLinesArgs, [
      updatedProps.viewport.height,
      0,
      updatedProps.viewport.encoding,
    ]);
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    assertViewerContent(ctx.renderer.root, ctx.props, ["test file content2"]);
  });

  it("should not reload current page if props hasn't changed", async () => {
    //given
    const ctx = await TestContext();
    const updatedProps = { ...ctx.props };
    assert.deepEqual(updatedProps.viewport.encoding, ctx.viewport.encoding);
    assert.deepEqual(updatedProps.viewport.size, ctx.viewport.size);
    assert.deepEqual(updatedProps.viewport.width, ctx.viewport.width);
    assert.deepEqual(updatedProps.viewport.height, ctx.viewport.height);

    const readNextLinesTimes = ctx.readNextLines.times;

    //when
    TestRenderer.act(() =>
      ctx.renderer.update(withThemeContext(h(ViewerContent, updatedProps)))
    );
    await Promise.resolve();
    await Promise.resolve();

    //then
    assert.deepEqual(ctx.readNextLines.times, readNextLinesTimes);
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    assertViewerContent(ctx.renderer.root, ctx.props, [
      "test ",
      "file content",
    ]);
  });

  it("should call setViewport when non-empty file", async () => {
    //given
    const size = 25;

    //when
    const ctx = await TestContext(size);

    //then
    const percent = Math.trunc((19 / size) * 100);
    assert.deepEqual(ctx.viewport.progress, percent);
  });

  it("should call setViewport when empty file", async () => {
    //given
    const size = 0;

    //when
    const ctx = await TestContext(size);

    //then
    const percent = 0;
    assert.deepEqual(ctx.viewport.progress, percent);
  });
});

/**
 * @param {MockViewerFileReader} fileReader
 * @param {(viewport: ViewerFileViewport | undefined) => void} setViewport
 * @param {(keyFull: string) => boolean} onKeypress
 * @param {number} size
 * @returns {ViewerContentProps}
 */
function getViewerContentProps(fileReader, setViewport, onKeypress, size) {
  const inputRef =
    /** @type {React.MutableRefObject<BlessedElement | null>} */ (
      React.createRef()
    );
  return {
    inputRef,
    viewport: createViewerFileViewport(fileReader, "utf-8", size, 12, 5),
    setViewport,
    onKeypress,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ViewerContentProps} props
 * @param {readonly string[]} content
 * @param {boolean} [hasEncodingsPopup]
 */
function assertViewerContent(
  result,
  props,
  content,
  hasEncodingsPopup = false
) {
  assert.deepEqual(ViewerContent.displayName, "ViewerContent");

  const theme = FileListTheme.defaultTheme;

  assertComponents(
    result.children,
    h(
      viewerInput,
      {
        inputRef: props.inputRef,
        onWheel: mockFunction(),
        onKeypress: mockFunction(),
      },
      ...[
        h("text", {
          width: props.viewport.width,
          height: props.viewport.height,
          style: ViewerContent.contentStyle(theme),
          wrap: false,
          content: content.length === 0 ? "" : `${content.join("\n")}\n`,
        }),

        hasEncodingsPopup
          ? h(encodingsPopup, {
              encoding: props.viewport.encoding,
              onApply: mockFunction(),
              onClose: mockFunction(),
            })
          : null,
      ].filter((_) => _ !== null)
    )
  );
}
