import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import MockFileReader from "../../file/MockFileReader.mjs";
import ViewerFileLine from "../../viewer/ViewerFileLine.mjs";
import ViewerFileReader from "../../viewer/ViewerFileReader.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("ViewerFileReader.test.mjs", () => {
  const bufferSize = 15;
  const maxLineLength = 10;
  const encoding = "utf-8";

  /** @type {(buf: Buffer, content: string) => Promise<number>} */
  function writeBuf(buf, content) {
    buf.write(content, 0, content.length, encoding);
    return Promise.resolve(content.length);
  }

  it("should call fileReader.open when open", async () => {
    //given
    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return Promise.resolve();
    });
    const fileReader = new MockFileReader({ open });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const filePath = "test/filePath.txt";

    //when
    await reader.open(filePath);

    //then
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [filePath]);
  });

  it("should call fileReader.close when close", async () => {
    //given
    const close = mockFunction(() => {
      return Promise.resolve();
    });
    const fileReader = new MockFileReader({ close });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);

    //when
    await reader.close();

    //then
    assert.deepEqual(close.times, 1);
  });

  it("should do nothing if position=0 when readPrevLines", async () => {
    //given
    const readBytes = mockFunction();
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const position = 0;
    const lines = 2;

    //when
    const results = await reader.readPrevLines(lines, position, 20, encoding);

    //then
    assert.deepEqual(readBytes.times, 0);
    assert.deepEqual(results, []);
  });

  it("should read file content with new line at the end when readPrevLines", async () => {
    //given
    let readBytesArgs1 = /** @type {any[]} */ ([]);
    let readBytesArgs2 = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      if (readBytes.times === 1) {
        readBytesArgs1 = [pos, buf.length];
        return writeBuf(buf, "t file\ncontent\n");
      }
      readBytesArgs2 = [pos, buf.length];
      return writeBuf(buf, "tes");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const position = 18;
    const lines = 3;

    //when
    const results = await reader.readPrevLines(lines, position, 18, encoding);

    //then
    assert.deepEqual(readBytes.times, 2);
    assert.deepEqual(readBytesArgs1, [3, bufferSize]);
    assert.deepEqual(readBytesArgs2, [0, 3]);
    assert.deepEqual(results, [
      ViewerFileLine("test file", 10),
      ViewerFileLine("content", 8),
      ViewerFileLine("", 0),
    ]);
  });

  it("should read file content without new line at the end when readPrevLines", async () => {
    //given
    let readBytesArgs1 = /** @type {any[]} */ ([]);
    let readBytesArgs2 = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      if (readBytes.times === 1) {
        readBytesArgs1 = [pos, buf.length];
        return writeBuf(buf, "st file\ncontent");
      }
      readBytesArgs2 = [pos, buf.length];
      return writeBuf(buf, "\nte");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const position = 18;
    const lines = 3;

    //when
    const results = await reader.readPrevLines(lines, position, 18, encoding);

    //then
    assert.deepEqual(readBytes.times, 2);
    assert.deepEqual(readBytesArgs1, [3, bufferSize]);
    assert.deepEqual(readBytesArgs2, [0, 3]);
    assert.deepEqual(results, [
      ViewerFileLine("", 1),
      ViewerFileLine("test file", 10),
      ViewerFileLine("content", 7),
    ]);
  });

  it("should read content from the middle of file when readPrevLines", async () => {
    //given
    let readBytesArgs = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      readBytesArgs = [pos, buf.length];
      return writeBuf(buf, "test\nfile");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const position = 9;
    const lines = 3;

    //when
    const results = await reader.readPrevLines(lines, position, 18, encoding);

    //then
    assert.deepEqual(readBytes.times, 1);
    assert.deepEqual(readBytesArgs, [0, 9]);
    assert.deepEqual(results, [
      ViewerFileLine("test", 5),
      ViewerFileLine("file", 4),
    ]);
  });

  it("should read single empty line at the start when readPrevLines", async () => {
    //given
    let readBytesArgs = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      readBytesArgs = [pos, buf.length];
      return writeBuf(buf, "\n");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const position = 1;
    const lines = 2;

    //when
    const results = await reader.readPrevLines(lines, position, 18, encoding);

    //then
    assert.deepEqual(readBytes.times, 1);
    assert.deepEqual(readBytesArgs, [0, 1]);
    assert.deepEqual(results, [ViewerFileLine("", 1)]);
  });

  it("should read long lines when readPrevLines", async () => {
    //given
    let readBytesArgs = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      readBytesArgs = [pos, buf.length];
      return writeBuf(buf, "testfilecontent");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const position = 15;
    const lines = 3;

    //when
    const results = await reader.readPrevLines(lines, position, 18, encoding);

    //then
    assert.deepEqual(readBytes.times, 1);
    assert.deepEqual(readBytesArgs, [0, bufferSize]);
    assert.deepEqual(results, [
      ViewerFileLine("testf", 5),
      ViewerFileLine("ilecontent", 10),
    ]);
  });

  it("should read single line when readPrevLines", async () => {
    //given
    let readBytesArgs = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      readBytesArgs = [pos, buf.length];
      return writeBuf(buf, "testfileco");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const position = 15;
    const lines = 1;

    //when
    const results = await reader.readPrevLines(lines, position, 18, encoding);

    //then
    assert.deepEqual(readBytes.times, 1);
    assert.deepEqual(readBytesArgs, [5, maxLineLength]);
    assert.deepEqual(results, [ViewerFileLine("testfileco", 10)]);
  });

  it("should read file content with new lines when readNextLines", async () => {
    //given
    let readBytesArgs1 = /** @type {any[]} */ ([]);
    let readBytesArgs2 = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      if (readBytes.times === 1) {
        readBytesArgs1 = [pos, buf.length];
        return writeBuf(buf, "\ntest file");
      }
      readBytesArgs2 = [pos, buf.length];
      return writeBuf(buf, "\ncontent\n");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const lines = 3;

    //when
    const results = await reader.readNextLines(lines, 0, encoding);

    //then
    assert.deepEqual(readBytes.times, 2);
    assert.deepEqual(readBytesArgs1, [0, bufferSize]);
    assert.deepEqual(readBytesArgs2, [10, bufferSize]);
    assert.deepEqual(results, [
      ViewerFileLine("", 1),
      ViewerFileLine("test file", 10),
      ViewerFileLine("content", 8),
    ]);
  });

  it("should read file content without new lines when readNextLines", async () => {
    //given
    let readBytesArgs1 = /** @type {any[]} */ ([]);
    let readBytesArgs2 = /** @type {any[]} */ ([]);
    let readBytesArgs3 = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      if (readBytes.times === 1) {
        readBytesArgs1 = [pos, buf.length];
        return writeBuf(buf, "test fi");
      }
      if (readBytes.times === 2) {
        readBytesArgs2 = [pos, buf.length];
        return writeBuf(buf, "le\ncontent");
      }
      readBytesArgs3 = [pos, buf.length];
      return writeBuf(buf, "");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const lines = 3;

    //when
    const results = await reader.readNextLines(lines, 0, encoding);

    //then
    assert.deepEqual(readBytes.times, 3);
    assert.deepEqual(readBytesArgs1, [0, bufferSize]);
    assert.deepEqual(readBytesArgs2, [7, bufferSize]);
    assert.deepEqual(readBytesArgs3, [17, bufferSize]);
    assert.deepEqual(results, [
      ViewerFileLine("test file", 10),
      ViewerFileLine("content", 7),
    ]);
  });

  it("should read long lines when readNextLines", async () => {
    //given
    let readBytesArgs1 = /** @type {any[]} */ ([]);
    let readBytesArgs2 = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      if (readBytes.times === 1) {
        readBytesArgs1 = [pos, buf.length];
        return writeBuf(buf, "testfilecontent");
      }
      readBytesArgs2 = [pos, buf.length];
      return writeBuf(buf, "");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const lines = 3;

    //when
    const results = await reader.readNextLines(lines, 0, encoding);

    //then
    assert.deepEqual(readBytes.times, 2);
    assert.deepEqual(readBytesArgs1, [0, bufferSize]);
    assert.deepEqual(readBytesArgs2, [15, bufferSize]);
    assert.deepEqual(results, [
      ViewerFileLine("testfileco", 10),
      ViewerFileLine("ntent", 5),
    ]);
  });

  it("should read single line when readNextLines", async () => {
    //given
    let readBytesArgs = /** @type {any[]} */ ([]);
    const readBytes = mockFunction((pos, buf) => {
      readBytesArgs = [pos, buf.length];
      return writeBuf(buf, "testfileco");
    });
    const fileReader = new MockFileReader({ readBytes });
    const reader = new ViewerFileReader(fileReader, bufferSize, maxLineLength);
    const lines = 1;

    //when
    const results = await reader.readNextLines(lines, 0, encoding);

    //then
    assert.deepEqual(readBytes.times, 1);
    assert.deepEqual(readBytesArgs, [0, maxLineLength]);
    assert.deepEqual(results, [ViewerFileLine("testfileco", 10)]);
  });
});
