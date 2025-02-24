import nodeFs from "fs/promises";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import FileReader from "../../file/FileReader.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FileReader.test.mjs", () => {
  const filePath = "test/file";

  it("should call fs.open when open", async () => {
    //given
    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return Promise.resolve(/** @type {nodeFs.FileHandle} */ ({}));
    });
    const reader = new FileReader({ open });

    //when
    await reader.open(filePath);

    //then
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [filePath, nodeFs.constants.O_RDONLY]);
  });

  it("should throw error if not open when close", async () => {
    //given
    let error = null;
    const reader = new FileReader();

    try {
      //when
      await reader.close();
    } catch (e) {
      error = e;
    }

    //then
    assert.deepEqual(error, Error("File is not open!"));
  });

  it("should recover and log error if failed when close", async () => {
    //given
    const close = mockFunction(() => {
      return Promise.reject(Error("test error"));
    });
    const open = mockFunction(() => {
      return Promise.resolve(/** @type {any} */ ({ close }));
    });
    const reader = new FileReader({ open });
    await reader.open(filePath);
    let errorLoggerArgs = /** @type {any[]} */ ([]);
    const errorLogger = mockFunction((...args) => (errorLoggerArgs = args));
    const savedConsoleError = console.error;
    console.error = errorLogger;

    //when
    await reader.close();

    //cleanup
    console.error = savedConsoleError;

    //then
    assert.deepEqual(open.times, 1);
    assert.deepEqual(close.times, 1);
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      errorLoggerArgs.map((_) => _.toString()),
      ["Failed to close file, error: Error: test error"]
    );
  });

  it("should successfully close file when close", async () => {
    //given
    const close = mockFunction(() => {
      return Promise.resolve(undefined);
    });
    const open = mockFunction(() => {
      return Promise.resolve(/** @type {any} */ ({ close }));
    });
    const reader = new FileReader({ open });
    await reader.open(filePath);

    //when
    await reader.close();

    //then
    assert.deepEqual(open.times, 1);
    assert.deepEqual(close.times, 1);
  });

  it("should throw error if not open when readBytes", async () => {
    //given
    let error = null;
    const reader = new FileReader();
    const fileBuf = Buffer.allocUnsafe(64);

    try {
      //when
      await reader.readBytes(0, fileBuf);
    } catch (e) {
      error = e;
    }

    //then
    assert.deepEqual(error, Error("File is not open!"));
  });

  it("should fail and log error if failed when readBytes", async () => {
    //given
    const error = Error("test error");
    const read = mockFunction(() => {
      return Promise.reject(error);
    });
    const open = mockFunction(() => {
      return Promise.resolve(/** @type {any} */ ({ read }));
    });
    const reader = new FileReader({ open });
    await reader.open(filePath);
    let errorLoggerArgs = /** @type {any[]} */ ([]);
    const errorLogger = mockFunction((...args) => (errorLoggerArgs = args));
    const savedConsoleError = console.error;
    console.error = errorLogger;
    const buf = Buffer.allocUnsafe(64);
    let catchedError = null;

    try {
      //when
      await reader.readBytes(0, buf);
    } catch (e) {
      catchedError = e;
    }

    //cleanup
    console.error = savedConsoleError;

    //then
    assert.deepEqual(catchedError === error, true);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(read.times, 1);
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      errorLoggerArgs.map((_) => _.toString()),
      ["Failed to read from file, error: Error: test error"]
    );
  });

  it("should successfully read data when readBytes", async () => {
    //given
    let readArgs = /** @type {any[]} */ ([]);
    const read = mockFunction((...args) => {
      readArgs = args;
      return Promise.resolve({ bytesRead: 456 });
    });
    const open = mockFunction(() => {
      return Promise.resolve(/** @type {any} */ ({ read }));
    });
    const reader = new FileReader({ open });
    await reader.open(filePath);
    const buf = Buffer.allocUnsafe(64);

    //when
    const result = await reader.readBytes(123, buf);

    //then
    assert.deepEqual(result, 456);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(read.times, 1);
    assert.deepEqual(readArgs, [buf, 0, buf.length, 123]);
  });
});
