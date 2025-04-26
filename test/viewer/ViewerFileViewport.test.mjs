import assert from "node:assert/strict";
import MockViewerFileReader from "../../viewer/MockViewerFileReader.mjs";
import ViewerFileLine from "../../viewer/ViewerFileLine.mjs";
import { createViewerFileViewport } from "../../viewer/ViewerFileViewport.mjs";
import mockFunction from "mock-fn";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("ViewerFileViewport.test.mjs", () => {
  it("should handle unicode characters when content", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "win",
      123,
      10,
      3
    ).updated({ linesData: [ViewerFileLine("Валютный 123", 1)] });

    //when & then
    assert.deepEqual(viewport.content, "Валютный 1" + "\n");
  });

  it("should replace control characters with spaces when content", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "win",
      123,
      15,
      3
    ).updated({
      linesData: [
        ViewerFileLine("\t\rline1\n\u0000\u0007\u0008\u000b\u001b", 1),
        ViewerFileLine("\u007fline2", 2),
      ],
    });

    //when & then
    assert.deepEqual(viewport.content, "\t\rline1\n     \n" + " line2\n");
  });

  it("should return list of indexes of long lines when scrollIndicators", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "win",
      123,
      10,
      3
    ).updated({
      column: 1,
      linesData: [
        ViewerFileLine("Валютный 12", 1),
        ViewerFileLine("Валютный 123", 2),
      ],
    });

    //when & then
    assert.deepEqual(viewport.scrollIndicators, [1]);
  });

  it("should return current view progress in percents when progress", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "win",
      100,
      10,
      3
    ).updated({
      position: 5,
      linesData: [
        ViewerFileLine("Валютный 12", 25),
        ViewerFileLine("Валютный 123", 20),
      ],
    });

    //when & then
    assert.deepEqual(viewport.progress, 50);
  });

  it("should return 0 progress if size=0 when progress", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "win",
      0,
      10,
      3
    ).updated({
      position: 5,
      linesData: [
        ViewerFileLine("Валютный 12", 25),
        ViewerFileLine("Валютный 123", 20),
      ],
    });

    //when & then
    assert.deepEqual(viewport.progress, 0);
  });

  it("should return this when moveUp(from=0.0)", async () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "win",
      0,
      10,
      3
    ).updated({
      linesData: [
        ViewerFileLine("Валютный 12", 25),
        ViewerFileLine("Валютный 123", 20),
      ],
    });

    //when
    const result = await viewport.moveUp(1);

    //then
    assert.deepEqual(result === viewport, true);
  });

  it("should return this if empty data when moveUp", async () => {
    //given
    let readPrevLinesArgs = /** @type {any[]} */ ([]);
    const readPrevLines = mockFunction((...args) => {
      readPrevLinesArgs = args;
      return Promise.resolve([]);
    });
    const fileReader = new MockViewerFileReader({ readPrevLines });
    const encoding = "utf-8";
    const size = 123;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      size,
      10,
      3
    );
    const lines = 1;
    const from = 12;

    //when
    const result = await viewport.moveUp(lines, from);

    //then
    assert.deepEqual(readPrevLines.times, 1);
    assert.deepEqual(readPrevLinesArgs, [lines, from, size, encoding]);
    assert.deepEqual(result === viewport, true);
  });

  it("should return updated instance when moveUp(from < size)", async () => {
    //given
    let readPrevLinesArgs = /** @type {any[]} */ ([]);
    const readPrevLines = mockFunction((...args) => {
      readPrevLinesArgs = args;
      return Promise.resolve([ViewerFileLine("test line", 9)]);
    });
    const fileReader = new MockViewerFileReader({ readPrevLines });
    const encoding = "utf-8";
    const width = 6;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      123,
      width,
      3
    ).updated({
      wrap: true,
      position: 12,
      linesData: [
        ViewerFileLine("existing line1", 1),
        ViewerFileLine("existing line2", 2),
      ],
    });
    const lines = 1;
    const from = 100;

    //when
    const result = await viewport.moveUp(lines, from);

    //then
    assert.deepEqual(readPrevLines.times, 1);
    assert.deepEqual(readPrevLinesArgs, [lines, from, 123, encoding]);
    assert.deepEqual(
      { ...result },
      {
        ...viewport,
        position: 94,
        linesData: [
          ViewerFileLine("t line", width),
          ViewerFileLine("existing line1", 1),
          ViewerFileLine("existing line2", 2),
        ],
      }
    );
  });

  it("should return updated instance when moveUp(from=size)", async () => {
    //given
    let readPrevLinesArgs = /** @type {any[]} */ ([]);
    const readPrevLines = mockFunction((...args) => {
      readPrevLinesArgs = args;
      return Promise.resolve([ViewerFileLine("test line", 9)]);
    });
    const fileReader = new MockViewerFileReader({ readPrevLines });
    const encoding = "utf-8";
    const position = 123;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      position,
      10,
      3
    ).updated({
      position,
      linesData: [
        ViewerFileLine("existing line1", 1),
        ViewerFileLine("existing line2", 2),
      ],
    });
    const lines = 1;

    //when
    const result = await viewport.moveUp(lines);

    //then
    assert.deepEqual(readPrevLines.times, 1);
    assert.deepEqual(readPrevLinesArgs, [lines, position, position, encoding]);
    assert.deepEqual(
      { ...result },
      {
        ...viewport,
        position: 114,
        linesData: [ViewerFileLine("test line", 9)],
      }
    );
  });

  it("should call moveUp if end of file and same lines when moveDown", async () => {
    //given
    let readPrevLinesArgs = /** @type {any[]} */ ([]);
    const readPrevLines = mockFunction((...args) => {
      readPrevLinesArgs = args;
      return Promise.resolve([ViewerFileLine("test line", 9)]);
    });
    const fileReader = new MockViewerFileReader({ readPrevLines });
    const encoding = "utf-8";
    const position = 12;
    const size = 17;
    const height = 1;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      size,
      10,
      height
    ).updated({
      position,
      linesData: [ViewerFileLine("existing line1", 5)],
    });
    const lines = 2;

    //when
    const result = await viewport.moveDown(lines);

    //then
    assert.deepEqual(readPrevLines.times, 1);
    assert.deepEqual(readPrevLinesArgs, [height, size, size, encoding]);
    assert.deepEqual(
      { ...result },
      {
        ...viewport,
        position: 8,
        linesData: [ViewerFileLine("test line", 9)],
      }
    );
  });

  it("should return this if end of file and not same lines when moveDown", async () => {
    //given
    const fileReader = new MockViewerFileReader();
    const encoding = "utf-8";
    const position = 13;
    const size = 17;
    const height = 1;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      size,
      10,
      height
    ).updated({
      position,
      linesData: [
        ViewerFileLine("existing line1", 2),
        ViewerFileLine("existing line2", 3),
      ],
    });
    const lines = 3;

    //when
    const result = await viewport.moveDown(lines);

    //then
    assert.deepEqual(result === viewport, true);
  });

  it("should return this if empty data when moveDown", async () => {
    //given
    let readNextLinesArgs = /** @type {any[]} */ ([]);
    const readNextLines = mockFunction((...args) => {
      readNextLinesArgs = args;
      return Promise.resolve([]);
    });
    const fileReader = new MockViewerFileReader({ readNextLines });
    const encoding = "utf-8";
    const position = 12;
    const size = 123;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      size,
      10,
      3
    ).updated({
      position,
      linesData: [
        ViewerFileLine("existing line1", 2),
        ViewerFileLine("existing line2", 3),
      ],
    });
    const nextPosition = 17;
    const lines = 1;

    //when
    const result = await viewport.moveDown(lines);

    //then
    assert.deepEqual(readNextLines.times, 1);
    assert.deepEqual(readNextLinesArgs, [lines, nextPosition, encoding]);
    assert.deepEqual(result === viewport, true);
  });

  it("should return updated instance when moveDown", async () => {
    //given
    let readNextLinesArgs = /** @type {any[]} */ ([]);
    const readNextLines = mockFunction((...args) => {
      readNextLinesArgs = args;
      return Promise.resolve([ViewerFileLine("test line", 9)]);
    });
    const fileReader = new MockViewerFileReader({ readNextLines });
    const encoding = "utf-8";
    const position = 12;
    const size = 123;
    const width = 6;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      size,
      width,
      3
    ).updated({
      wrap: true,
      position,
      linesData: [
        ViewerFileLine("existing line1", 2),
        ViewerFileLine("existing line2", 3),
      ],
    });
    const nextPosition = 17;
    const lines = 1;

    //when
    const result = await viewport.moveDown(lines);

    //then
    assert.deepEqual(readNextLines.times, 1);
    assert.deepEqual(readNextLinesArgs, [lines, nextPosition, encoding]);
    assert.deepEqual(
      { ...result },
      {
        ...viewport,
        position: 14,
        linesData: [
          ViewerFileLine("existing line2", 3),
          ViewerFileLine("test l", width),
        ],
      }
    );
  });

  it("should return updated instance when reload()", async () => {
    //given
    let readNextLinesArgs = /** @type {any[]} */ ([]);
    const readNextLines = mockFunction((...args) => {
      readNextLinesArgs = args;
      return Promise.resolve([ViewerFileLine("test line", 9)]);
    });
    const fileReader = new MockViewerFileReader({ readNextLines });
    const encoding = "utf-8";
    const position = 12;
    const size = 123;
    const height = 3;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      size,
      10,
      height
    ).updated({
      position,
      linesData: [
        ViewerFileLine("existing line1", 2),
        ViewerFileLine("existing line2", 3),
      ],
    });
    const from = position;

    //when
    const result = await viewport.reload();

    //then
    assert.deepEqual(readNextLines.times, 1);
    assert.deepEqual(readNextLinesArgs, [height, from, encoding]);
    assert.deepEqual(
      { ...result },
      {
        ...viewport,
        linesData: [ViewerFileLine("test line", 9)],
      }
    );
  });

  it("should return updated instance when reload(from)", async () => {
    //given
    let readNextLinesArgs = /** @type {any[]} */ ([]);
    const readNextLines = mockFunction((...args) => {
      readNextLinesArgs = args;
      return Promise.resolve([ViewerFileLine("test line", 9)]);
    });
    const fileReader = new MockViewerFileReader({ readNextLines });
    const encoding = "utf-8";
    const position = 12;
    const size = 123;
    const width = 6;
    const height = 3;
    const viewport = createViewerFileViewport(
      fileReader,
      encoding,
      size,
      width,
      height
    ).updated({
      wrap: true,
      position,
      linesData: [
        ViewerFileLine("existing line1", 2),
        ViewerFileLine("existing line2", 3),
      ],
    });
    const from = 15;

    //when
    const result = await viewport.reload(from);

    //then
    assert.deepEqual(readNextLines.times, 1);
    assert.deepEqual(readNextLinesArgs, [height, from, encoding]);
    assert.deepEqual(
      { ...result },
      {
        ...viewport,
        position: from,
        linesData: [ViewerFileLine("test l", width), ViewerFileLine("ine", 3)],
      }
    );
  });

  it("should return input data if wrap=false when _doWrap", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(fileReader, "win", 123, 10, 3);
    const data = [ViewerFileLine("test line 1", 1)];

    //when
    //@ts-ignore
    const result = viewport._doWrap(1, false)(data);

    //then
    assert.deepEqual(result === data, true);
  });

  it("should return wrapped data if up=false when _doWrap", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "utf-8",
      123,
      6,
      3,
      true
    );
    const data = [ViewerFileLine("test1 2", 30), ViewerFileLine("test3 4", 40)];

    //when
    //@ts-ignore
    const result = viewport._doWrap(3, false)(data);

    //then
    assert.deepEqual(result, [
      ViewerFileLine("test1 ", 6),
      ViewerFileLine("2", 24),
      ViewerFileLine("test3 ", 6),
    ]);
  });

  it("should return wrapped data if up=true when _doWrap", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "utf-8",
      123,
      6,
      3,
      true
    );
    const data = [ViewerFileLine("test1 2", 30), ViewerFileLine("test3 4", 40)];

    //when
    //@ts-ignore
    const result = viewport._doWrap(3, true)(data);

    //then
    assert.deepEqual(result, [
      ViewerFileLine("est1 2", 6),
      ViewerFileLine("t", 34),
      ViewerFileLine("est3 4", 6),
    ]);
  });

  it("should return new instance with updated data when updated", () => {
    //given
    const fileReader = new MockViewerFileReader();
    const viewport = createViewerFileViewport(
      fileReader,
      "utf8",
      123,
      80,
      25,
      true,
      1,
      2,
      [ViewerFileLine("test line", 3)]
    );
    const data = {
      encoding: "utf16",
      size: 456,
      width: 1,
      height: 2,
      wrap: false,
      column: 3,
      position: 4,
      linesData: [ViewerFileLine("test line2", 5)],
    };

    //when
    const result = viewport.updated(data);

    //then
    assert.deepEqual(
      { ...result },
      {
        fileReader,
        ...data,
        _content: undefined,
        _scrollIndicators: undefined,
        _progress: undefined,
      }
    );
  });

  it("should return new instance with defatult params when create", () => {
    //given
    const fileReader = new MockViewerFileReader();

    //when
    const result = createViewerFileViewport(fileReader, "utf8", 123, 80, 25);

    //then
    assert.deepEqual(
      { ...result },
      {
        fileReader,
        encoding: "utf8",
        size: 123,
        width: 80,
        height: 25,
        wrap: false,
        column: 0,
        position: 0,
        linesData: [],
        _content: undefined,
        _scrollIndicators: undefined,
        _progress: undefined,
      }
    );
  });
});
